package com.github.apace100.apoli.power.type;

import com.github.apace100.apoli.registry.ApoliRegistries;
import com.github.apace100.calio.api.IdentifierAlias;
import com.github.apace100.calio.api.data.Serializable;
import net.minecraft.registry.Registry;

public class PowerTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();

    public static final Serializable.Serializer<SimplePowerType> SIMPLE = register(SimplePowerType::getFactory);
    public static final Serializable.Serializer<MultiplePowerType> MULTIPLE = register(MultiplePowerType::getFactory);
    public static final Serializable.Serializer<ResourcePowerType> RESOURCE = register(ResourcePowerType::getFactory);

    public static void register() {

        ALIASES.addNamespaceAlias("minecraft", "apoli"); // FIXME: For testing stuff. Remove when done -eggohito

    }

    public static <T extends PowerType> Serializable.Serializer<T> register(Serializable.Supplier<T> supplier) {
        Serializable.Factory<T> factory = supplier.getFactory();
        Registry.register(ApoliRegistries.POWER_TYPE, factory.id(), factory.serializer());
        return factory.serializer();
    }

}
