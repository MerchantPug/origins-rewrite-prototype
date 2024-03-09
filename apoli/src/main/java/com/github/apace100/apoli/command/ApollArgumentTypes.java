package com.github.apace100.apoli.command;

import com.github.apace100.apoli.Apoli;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;

public class ApollArgumentTypes {
    public static void register() {
        ArgumentTypeRegistry.registerArgumentType(Apoli.identifier("power_holder"), PowerHolderArgumentType.class, ConstantArgumentSerializer.of(PowerHolderArgumentType::holder));
        ArgumentTypeRegistry.registerArgumentType(Apoli.identifier("power_holders"), PowerHolderArgumentType.class, ConstantArgumentSerializer.of(PowerHolderArgumentType::holders));
    }
}
