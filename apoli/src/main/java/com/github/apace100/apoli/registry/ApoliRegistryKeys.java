package com.github.apace100.apoli.registry;

import com.github.apace100.apoli.Apoli;
import com.github.apace100.apoli.power.Power;
import com.github.apace100.apoli.power.type.PowerType;
import com.github.apace100.calio.api.data.Serializable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class ApoliRegistryKeys {

    public static final RegistryKey<Registry<Power>> POWER;
    public static final RegistryKey<Registry<Serializable.Serializer<? extends PowerType>>> POWER_TYPE;

    static {
        POWER = RegistryKey.ofRegistry(Identifier.of(Apoli.MODID, "power"));
        POWER_TYPE = RegistryKey.ofRegistry(Identifier.of(Apoli.MODID, "power_type"));
    }

}
