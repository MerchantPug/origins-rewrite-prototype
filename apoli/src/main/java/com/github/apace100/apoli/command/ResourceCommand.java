package com.github.apace100.apoli.command;

import com.github.apace100.apoli.attachment.ApoliEntityApis;
import com.github.apace100.apoli.attachment.PowerHolderApi;
import com.github.apace100.apoli.power.Power;
import com.github.apace100.apoli.power.type.PowerTypes;
import com.github.apace100.apoli.power.type.ResourcePowerType;
import com.github.apace100.apoli.registry.ApoliRegistryKeys;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * <p>
 * A resource command class. Structured similarly to the repo below:
 * https://github.com/falkreon/BrigadierAdvice/tree/1.19
 * </p>
 *
 * <p>TODO: Document this - Pug
 */
public class ResourceCommand {

    public static void register(CommandRegistryAccess registryAccess, LiteralCommandNode<ServerCommandSource> baseNode) {

        LiteralCommandNode<ServerCommandSource> resourceNode = CommandManager
                .literal("resource")
                .build();

        LiteralCommandNode<ServerCommandSource> getNode = CommandManager
                .literal("get")
                        .then(CommandManager.argument("target", PowerHolderArgumentType.holder())
                                .then(CommandManager.argument("power", RegistryEntryArgumentType.registryEntry(registryAccess, ApoliRegistryKeys.POWER))
                                        .executes(ResourceCommand::getResource)))
                .build();

        LiteralCommandNode<ServerCommandSource> setNode = CommandManager
                .literal("set")
                .then(CommandManager.argument("target", PowerHolderArgumentType.holder())
                        .then(CommandManager.argument("power", RegistryEntryArgumentType.registryEntry(registryAccess, ApoliRegistryKeys.POWER))
                                .then(CommandManager.argument("value", IntegerArgumentType.integer())
                                        .executes(ResourceCommand::setResource))))
                .build();

        LiteralCommandNode<ServerCommandSource> hasNode = CommandManager
                .literal("has")
                .then(CommandManager.argument("target", PowerHolderArgumentType.holder())
                        .then(CommandManager.argument("power", RegistryEntryArgumentType.registryEntry(registryAccess, ApoliRegistryKeys.POWER))
                                .executes(ResourceCommand::hasResource)))
                .build();

        baseNode.addChild(resourceNode);
        resourceNode.addChild(getNode);
        resourceNode.addChild(setNode);
        resourceNode.addChild(hasNode);

    }

    private static int getResource(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerCommandSource source = context.getSource();

        LivingEntity target = PowerHolderArgumentType.getHolder(context, "target");
        RegistryEntry<Power> power = RegistryEntryArgumentType.getRegistryEntry(context, "power", ApoliRegistryKeys.POWER);

        PowerHolderApi api = ApoliEntityApis.POWER_HOLDER.find(target, null);

        // TODO: Move the concept of resources to a common class.
        if (isResourceInvalid(api, power)) {
            source.sendError(Text.translatable("commands.scoreboard.players.get.null", power.getKey().get().getValue().toString(), target.getName().getString()));
            return 0;
        }

        ResourcePowerType type = api.getPowerType(power);
        int value = type.getValue(target);

        source.sendFeedback(() -> Text.translatable("commands.scoreboard.players.get.success", target.getName().getString(), value, power.getKey().get().getValue().toString()), true);

        return value;

    }

    private static int setResource(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerCommandSource source = context.getSource();

        LivingEntity target = PowerHolderArgumentType.getHolder(context, "target");
        RegistryEntry<Power> power = RegistryEntryArgumentType.getRegistryEntry(context, "power", ApoliRegistryKeys.POWER);
        int value = IntegerArgumentType.getInteger(context, "value");

        PowerHolderApi api = ApoliEntityApis.POWER_HOLDER.find(target, null);

        // TODO: Move the concept of resources to a common class.
        if (isResourceInvalid(api, power)) {
            source.sendError(Text.translatable("argument.scoreHolder.empty"));
            return 0;
        }

        ResourcePowerType type = api.getPowerType(power);
        value = type.setValue(target, value);
        api.sync();

        int finalValue = value;
        source.sendFeedback(() -> Text.translatable("commands.scoreboard.players.set.success.single", power.getKey().get().getValue().toString(), target.getName().getString(), finalValue), true);

        return value;

    }

    private static int hasResource(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerCommandSource source = context.getSource();

        LivingEntity target = PowerHolderArgumentType.getHolder(context, "target");
        RegistryEntry<Power> power = RegistryEntryArgumentType.getRegistryEntry(context, "power", ApoliRegistryKeys.POWER);

        PowerHolderApi api = ApoliEntityApis.POWER_HOLDER.find(target, null);

        // TODO: Move the concept of resources to a common class.
        if (isResourceInvalid(api, power)) {
            source.sendError(Text.translatable("commands.execute.conditional.fail"));
            return 0;
        }

        source.sendFeedback(() -> Text.translatable("commands.execute.conditional.pass"), true);
        return 1;

    }

    private static boolean isResourceInvalid(PowerHolderApi api, RegistryEntry<Power> power) {
        return api == null || !api.hasPower(power) || power.value().getType().getSerializer() != PowerTypes.RESOURCE;
    }

}
