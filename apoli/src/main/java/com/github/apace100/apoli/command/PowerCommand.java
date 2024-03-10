package com.github.apace100.apoli.command;

import com.github.apace100.apoli.Apoli;
import com.github.apace100.apoli.attachment.PowerHolderApi;
import com.github.apace100.apoli.power.Power;
import com.github.apace100.apoli.attachment.ApoliEntityApis;
import com.github.apace100.apoli.registry.ApoliRegistryKeys;
import com.github.apace100.calio.util.JsonTextFormatter;
import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * An Apoli command class. Structured similarly to the repo below:
 * https://github.com/falkreon/BrigadierAdvice/tree/1.19
 * </p>
 *
 * <p>TODO: Document this - Pug
 */
public class PowerCommand {
    public static final Identifier POWER_SOURCE = Apoli.identifier("command");
    public static void register(CommandRegistryAccess registryAccess, LiteralCommandNode<ServerCommandSource> baseNode) {

        LiteralCommandNode<ServerCommandSource> powerNode = CommandManager
                .literal("power")
                .build();

        LiteralCommandNode<ServerCommandSource> grantNode = CommandManager
                .literal("grant")
                .then(CommandManager.argument("targets", PowerHolderArgumentType.holders())
                        .then(CommandManager.argument("power", RegistryEntryArgumentType.registryEntry(registryAccess, ApoliRegistryKeys.POWER))
                                .executes(context -> grantPower(context, false)))
                .then(CommandManager.argument("source", IdentifierArgumentType.identifier())
                        .executes(context -> grantPower(context, true))))
                .build();

        LiteralCommandNode<ServerCommandSource> revokeNode = CommandManager
                .literal("revoke")
                .then(CommandManager.argument("targets", PowerHolderArgumentType.holders())
                        .then(CommandManager.argument("power", RegistryEntryArgumentType.registryEntry(registryAccess, ApoliRegistryKeys.POWER))
                                .executes(context -> revokePower(context, false))))
                .then(CommandManager.argument("source", IdentifierArgumentType.identifier())
                        .executes(context -> revokePower(context, true)))
                .build();

        LiteralCommandNode<ServerCommandSource> revokeAllNode = CommandManager
                .literal("revokeall")
                .then(CommandManager.argument("targets", PowerHolderArgumentType.holders())
                    .then(CommandManager.argument("source", IdentifierArgumentType.identifier())
                        .executes(PowerCommand::revokeAllPowersFromSource)))
                .build();

        LiteralCommandNode<ServerCommandSource> removeNode = CommandManager
                .literal("remove")
                .then(CommandManager.argument("targets", PowerHolderArgumentType.holders())
                        .then(CommandManager.argument("power", RegistryEntryArgumentType.registryEntry(registryAccess, ApoliRegistryKeys.POWER))
                                .executes(PowerCommand::removePower)))
                .build();

        LiteralCommandNode<ServerCommandSource> clearNode = CommandManager
                .literal("clear")
                .executes(context -> PowerCommand.clearAllPowers(context, true))
                .then(CommandManager.argument("targets", PowerHolderArgumentType.holders())
                        .executes(context -> PowerCommand.clearAllPowers(context, false)))
                .build();

        LiteralCommandNode<ServerCommandSource> listNode = CommandManager
                .literal("list")
                .then(CommandManager.argument("target", PowerHolderArgumentType.holder())
                        .executes(PowerCommand::listPowers))
                .build();

        LiteralCommandNode<ServerCommandSource> hasNode = CommandManager
                .literal("has")
                .then(CommandManager.argument("targets", PowerHolderArgumentType.holders())
                        .then(CommandManager.argument("power", RegistryEntryArgumentType.registryEntry(registryAccess, ApoliRegistryKeys.POWER))
                                .executes(PowerCommand::hasPower)))
                .build();

        LiteralCommandNode<ServerCommandSource> dumpNode = CommandManager
                .literal("dump")
                        .then(CommandManager.argument("power", RegistryEntryArgumentType.registryEntry(registryAccess, ApoliRegistryKeys.POWER))
                                .executes(context -> dumpPowerJson(context, false))
                                .then(CommandManager.argument("indent",  IntegerArgumentType.integer(0))
                                        .executes(context -> dumpPowerJson(context, true))))
                .build();

        baseNode.addChild(powerNode);
        powerNode.addChild(grantNode);
        powerNode.addChild(revokeNode);
        powerNode.addChild(revokeAllNode);
        powerNode.addChild(removeNode);
        powerNode.addChild(clearNode);
        powerNode.addChild(listNode);
        powerNode.addChild(hasNode);
        powerNode.addChild(dumpNode);

    }

    private static int grantPower(CommandContext<ServerCommandSource> context, boolean isSourceSpecified) throws CommandSyntaxException {

        ServerCommandSource source = context.getSource();

        List<LivingEntity> targets = PowerHolderArgumentType.getHolders(context, "targets");
        List<LivingEntity> processedTargets = new LinkedList<>();

        RegistryEntry<Power> power = RegistryEntryArgumentType.getRegistryEntry(context, "power", ApoliRegistryKeys.POWER);
        Identifier powerSource = isSourceSpecified ? IdentifierArgumentType.getIdentifier(context, "source") : POWER_SOURCE;

        for (LivingEntity target : targets) {

            PowerHolderApi api = ApoliEntityApis.POWER_HOLDER.find(target, null);

            if (api == null || api.hasPowerWithSource(power, powerSource)) {
                continue;
            }

            api.addPower(power, powerSource);
            api.sync();
            processedTargets.add(target);

        }

        Text powerTypeName = power.value().getName();
        Text targetName = targets.get(0).getName();

        int targetsSize = targets.size();
        int processedTargetsSize = processedTargets.size();

        if (processedTargetsSize == 0) {

            if (targetsSize == 1) {
                source.sendError(Text.translatable("commands.apoli.grant.fail.single", targetName, powerTypeName, powerSource.toString()));
            } else {
                source.sendError(Text.translatable("commands.apoli.grant.fail.multiple", targetsSize, powerTypeName, powerSource.toString()));
            }

            return processedTargetsSize;

        }

        Text processedTargetName = processedTargets.get(0).getName();
        if (isSourceSpecified) {
            if (processedTargetsSize == 1) {
                source.sendFeedback(() -> Text.translatable("commands.apoli.grant_from_source.success.single", processedTargetName, powerTypeName, powerSource.toString()), true);
            } else {
                source.sendFeedback(() -> Text.translatable("commands.apoli.grant_from_source.success.multiple", processedTargetsSize, powerTypeName, powerSource.toString()), true);
            }
        } else {
            if (processedTargetsSize == 1) {
                source.sendFeedback(() -> Text.translatable("commands.apoli.grant.success.single", processedTargetName, powerTypeName), true);
            } else {
                source.sendFeedback(() -> Text.translatable("commands.apoli.grant.success.multiple", processedTargetsSize, powerTypeName), true);
            }
        }

        return processedTargetsSize;

    }

    private static int revokePower(CommandContext<ServerCommandSource> context, boolean isSourceSpecified) throws CommandSyntaxException {

        ServerCommandSource source = context.getSource();

        List<LivingEntity> targets = PowerHolderArgumentType.getHolders(context, "targets");
        List<LivingEntity> processedTargets = new LinkedList<>();

        RegistryEntry<Power> power = RegistryEntryArgumentType.getRegistryEntry(context, "power", ApoliRegistryKeys.POWER);
        Identifier powerSource = isSourceSpecified ? IdentifierArgumentType.getIdentifier(context, "source") : POWER_SOURCE;

        for (LivingEntity target : targets) {

            PowerHolderApi api = ApoliEntityApis.POWER_HOLDER.find(target, null);

            if (api == null || !api.hasPowerWithSource(power, powerSource)) {
                continue;
            }

            api.revokePower(power, powerSource);
            api.sync();

            processedTargets.add(target);

        }

        Text powerTypeName = power.value().getName();
        Text targetName = targets.get(0).getName();

        int targetsSize = targets.size();
        int processedTargetsSize = processedTargets.size();

        if (processedTargetsSize == 0) {

            if (targetsSize == 1) {
                source.sendError(Text.translatable("commands.apoli.revoke.fail.single", targetName, powerTypeName, powerSource.toString()));
            } else {
                source.sendError(Text.translatable("commands.apoli.revoke.fail.multiple", powerTypeName, powerSource.toString()));
            }

            return processedTargetsSize;

        }

        Text processedTargetName = processedTargets.get(0).getName();
        if (isSourceSpecified) {
            if (processedTargetsSize == 1) {
                source.sendFeedback(() -> Text.translatable("commands.apoli.revoke_from_source.success.single", processedTargetName, powerTypeName, powerSource.toString()), true);
            } else {
                source.sendFeedback(() -> Text.translatable("commands.apoli.revoke_from_source.success.multiple", processedTargetsSize, powerTypeName, powerSource.toString()), true);
            }
        } else {
            if (processedTargetsSize == 1) {
                source.sendFeedback(() -> Text.translatable("commands.apoli.revoke.success.single", processedTargetName, powerTypeName), true);
            } else {
                source.sendFeedback(() -> Text.translatable("commands.apoli.revoke.success.multiple", processedTargetsSize, powerTypeName), true);
            }
        }

        return processedTargetsSize;

    }

    private static int revokeAllPowersFromSource(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerCommandSource source = context.getSource();

        List<LivingEntity> targets = PowerHolderArgumentType.getHolders(context, "targets");
        List<LivingEntity> processedTargets = new LinkedList<>();

        Identifier powerSource = IdentifierArgumentType.getIdentifier(context, "source");

        int revokedPowers = 0;

        for (LivingEntity target : targets) {

            PowerHolderApi api = ApoliEntityApis.POWER_HOLDER.find(target, null);

            if (api == null) {
                continue;
            }

            revokedPowers += api.removeAllPowersFromSource(powerSource);
            api.sync();

            processedTargets.add(target);

        }

        Text targetName = targets.get(0).getName();

        int targetsSize = targets.size();
        int processedTargetsSize = processedTargets.size();

        if (processedTargetsSize == 0) {
            if (targetsSize == 1) {
                source.sendError(Text.translatable("commands.apoli.revoke_all.fail.single", targetName, powerSource.toString()));
            } else {
                source.sendError(Text.translatable("commands.apoli.revoke_all.fail.multiple", powerSource.toString()));
            }
        } else {

            Text processedTargetName = processedTargets.get(0).getName();
            int finalRevokedPowers = revokedPowers;

            if (processedTargetsSize == 1) {
                source.sendFeedback(() -> Text.translatable("commands.apoli.revoke_all.success.single", processedTargetName, finalRevokedPowers, powerSource.toString()), true);
            } else {
                source.sendFeedback(() -> Text.translatable("commands.apoli.revoke_all.success.multiple", processedTargetsSize, finalRevokedPowers, powerSource.toString()), true);
            }

        }

        return processedTargetsSize;

    }

    private static int removePower(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerCommandSource source = context.getSource();

        List<LivingEntity> targets = PowerHolderArgumentType.getHolders(context, "targets");
        List<LivingEntity> processedTargets = new LinkedList<>();

        RegistryEntry<Power> power = RegistryEntryArgumentType.getRegistryEntry(context, "power", ApoliRegistryKeys.POWER);

        for (LivingEntity target : targets) {

            PowerHolderApi api = ApoliEntityApis.POWER_HOLDER.find(target, null);

            if (api == null || !api.hasPower(power)) {
                continue;
            }

            api.removePower(power);
            api.sync();

            processedTargets.add(target);

        }

        Text powerTypeName = power.value().getName();
        Text targetName = targets.get(0).getName();

        int targetsSize = targets.size();
        int processedTargetsSize = processedTargets.size();

        if (processedTargetsSize == 0) {

            if (targetsSize == 1) {
                source.sendError(Text.translatable("commands.apoli.remove.fail.single", targetName, powerTypeName));
            } else {
                source.sendError(Text.translatable("commands.apoli.remove.fail.multiple", powerTypeName));
            }

            return processedTargetsSize;

        }

        Text processedTargetName = processedTargets.get(0).getName();
        if (processedTargetsSize == 1) {
            source.sendFeedback(() -> Text.translatable("commands.apoli.remove.success.single", processedTargetName, powerTypeName), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.apoli.remove.success.multiple", processedTargetsSize, powerTypeName), true);
        }

        return processedTargetsSize;

    }

    private static int clearAllPowers(CommandContext<ServerCommandSource> context, boolean onlyTargetSelf) throws CommandSyntaxException {

        ServerCommandSource source = context.getSource();

        List<LivingEntity> targets = new LinkedList<>();
        List<LivingEntity> processedTargets = new LinkedList<>();

        if (!onlyTargetSelf) {
            targets.addAll(PowerHolderArgumentType.getHolders(context, "targets"));
        } else {

            Entity self = source.getEntityOrThrow();
            if (!(self instanceof LivingEntity livingSelf)) {
                throw PowerHolderArgumentType.HOLDER_NOT_FOUND.create(self.getName());
            }

            targets.add(livingSelf);

        }

        int clearedPowers = 0;

        for (LivingEntity target : targets) {

            PowerHolderApi api = ApoliEntityApis.POWER_HOLDER.find(target, null);

            if (api == null) {
                continue;
            }

            clearedPowers += api.clearPowers();

            api.sync();
            processedTargets.add(target);

        }

        Text targetName = targets.get(0).getName();

        int targetsSize = targets.size();
        int processedTargetsSize = processedTargets.size();

        if (processedTargetsSize == 0) {
            if (targetsSize == 1) {
                source.sendError(Text.translatable("commands.apoli.clear.fail.single", targetName));
            } else {
                source.sendError(Text.translatable("commands.apoli.clear.fail.multiple"));
            }
        } else {

            Text processedTargetName = processedTargets.get(0).getName();
            int finalClearedPowers = clearedPowers;

            if (processedTargetsSize == 1) {
                source.sendFeedback(() -> Text.translatable("commands.apoli.clear.success.single", processedTargetName, finalClearedPowers), true);
            } else {
                source.sendFeedback(() -> Text.translatable("commands.apoli.clear.success.multiple", processedTargetsSize, finalClearedPowers), true);
            }

        }

        return clearedPowers;

    }

    private static int listPowers(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerCommandSource source = context.getSource();
        LivingEntity target = PowerHolderArgumentType.getHolder(context, "target");

        List<Text> powersTooltip = new LinkedList<>();
        int powers = 0;

        PowerHolderApi api = ApoliEntityApis.POWER_HOLDER.find(target, null);

        if (api != null) {
            for (RegistryEntry<Power> power : api.powerList()) {

                List<Text> sourcesTooltip = new LinkedList<>();
                api.getSources(power).forEach(id -> sourcesTooltip.add(Text.of(id.toString())));

                HoverEvent sourceHoverEvent = new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Text.translatable("commands.apoli.list.sources", Texts.join(sourcesTooltip, Text.of(",")))
                );

                Text powerTooltip = Text.literal(power.getKey().get().getValue().toString())
                        .setStyle(Style.EMPTY.withHoverEvent(sourceHoverEvent));

                powersTooltip.add(powerTooltip);
                powers++;

            }
        }

        if (powers == 0) {
            source.sendError(Text.translatable("commands.apoli.list.fail", target.getName()));
        } else {
            int finalPowers = powers;
            source.sendFeedback(() -> Text.translatable("commands.apoli.list.pass", target.getName(), finalPowers, Texts.join(powersTooltip, Text.of(", "))), true);
        }

        return powers;

    }

    private static int hasPower(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerCommandSource source = context.getSource();

        List<LivingEntity> targets = PowerHolderArgumentType.getHolders(context, "targets");
        List<LivingEntity> processedTargets = new LinkedList<>();

        RegistryEntry<Power> power = RegistryEntryArgumentType.getRegistryEntry(context, "power", ApoliRegistryKeys.POWER);

        for (LivingEntity target : targets) {
            PowerHolderApi api = ApoliEntityApis.POWER_HOLDER.find(target, null);

            if (api == null) {
                continue;
            }

            if (api.hasPower(power)) {
                processedTargets.add(target);
            }
        }

        int targetsSize = targets.size();
        int processedTargetsSize = processedTargets.size();

        if (processedTargetsSize == 0) {
            if (targetsSize == 1) {
                source.sendError(Text.translatable("commands.execute.conditional.fail"));
            } else {
                source.sendError(Text.translatable("commands.execute.conditional.fail_count", targetsSize));
            }
        } else {
            if (processedTargetsSize == 1) {
                source.sendFeedback(() -> Text.translatable("commands.execute.conditional.pass"), true);
            } else {
                source.sendFeedback(() -> Text.translatable("commands.execute.conditional.pass_count", processedTargetsSize), true);
            }
        }

        return processedTargets.size();

    }

    private static int dumpPowerJson(CommandContext<ServerCommandSource> context, boolean indentSpecified) throws CommandSyntaxException {

        ServerCommandSource source = context.getSource();
        RegistryEntry<Power> power = RegistryEntryArgumentType.getRegistryEntry(context, "power", ApoliRegistryKeys.POWER);

        String indent = Strings.repeat(" ", indentSpecified ? IntegerArgumentType.getInteger(context, "indent") : 4);
        DataResult<JsonElement> jsonElement = Power.CODEC.encodeStart(JsonOps.INSTANCE, power.value());

        if (jsonElement.error().isPresent()) {
            source.sendError(Text.translatable("commands.apoli.dump.fail", power.getKey().get().getValue().toString()));
            return 0;
        }

        source.sendFeedback(() -> new JsonTextFormatter(indent).apply(jsonElement.get().orThrow()), false);

        return 1;

    }
}
