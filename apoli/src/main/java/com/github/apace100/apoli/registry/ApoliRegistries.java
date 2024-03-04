package com.github.apace100.apoli.registry;

import com.github.apace100.apoli.power.type.PowerType;
import com.github.apace100.calio.api.data.Serializable;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.Registry;

public class ApoliRegistries {

    public static final Registry<Serializable.Serializer<? extends PowerType>> POWER_TYPE;

    static {
        POWER_TYPE = FabricRegistryBuilder.createSimple(ApoliRegistryKeys.POWER_TYPE)
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();
    }

}
