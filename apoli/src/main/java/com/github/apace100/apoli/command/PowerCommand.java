package com.github.apace100.apoli.command;

import com.github.apace100.apoli.Apoli;
import com.github.apace100.apoli.attachment.PowerHolderApi;
import com.github.apace100.apoli.power.Power;
import com.github.apace100.apoli.attachment.ApoliEntityApis;
import com.github.apace100.apoli.registry.ApoliRegistryKeys;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
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
    public static void register(CommandRegistryAccess registryAccess, LiteralCommandNode<ServerCommandSource> apoliNode) {

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

        LiteralCommandNode<ServerCommandSource> listNode = CommandManager
                .literal("list")
                .then(CommandManager.argument("target", PowerHolderArgumentType.holder())
                        .executes(PowerCommand::listPowers))
                .build();

        apoliNode.addChild(powerNode);
        powerNode.addChild(grantNode);
        powerNode.addChild(revokeNode);
        powerNode.addChild(listNode);

    }

    private static int grantPower(CommandContext<ServerCommandSource> context, boolean isSourceSpecified) throws CommandSyntaxException {

        ServerCommandSource source = context.getSource();

        List<LivingEntity> targets = PowerHolderArgumentType.getHolders(context, "targets");
        List<LivingEntity> processedTargets = new LinkedList<>();

        RegistryEntry<Power> power = RegistryEntryArgumentType.getRegistryEntry(context, "power", ApoliRegistryKeys.POWER);
        Identifier powerSource = isSourceSpecified ? IdentifierArgumentType.getIdentifier(context, "source") : POWER_SOURCE;

        for (LivingEntity target : targets) {

            PowerHolderApi api = ApoliEntityApis.POWER_HOLDER.find(target, null);

            if (api == null) {
                break;
            }

            if (api.hasPowerWithSource(power, powerSource)) {
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

            if (api == null) {
                break;
            }

            if (!api.hasPowerWithSource(power, powerSource)) {
                continue;
            }

            api.removePower(power, powerSource);
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
}
