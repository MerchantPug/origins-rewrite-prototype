package com.github.apace100.apoli.power.type;

import com.github.apace100.apoli.registry.ApoliRegistries;
import com.github.apace100.calio.api.IdentifierAlias;
import com.github.apace100.calio.api.data.Serializable;
import net.minecraft.registry.Registry;

public class PowerTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();

    public static void register() {

        ALIASES.addNamespaceAlias("minecraft", "apoli"); // FIXME: For testing stuff. Remove when done -eggohito

        register(SimplePowerType::getFactory);
        register(MultiplePowerType::getFactory);

    }

    public static void register(Serializable.Supplier<PowerType> supplier) {
        Serializable.Factory<? extends PowerType> factory = supplier.getFactory();
        Registry.register(ApoliRegistries.POWER_TYPE, factory.id(), factory.serializer());
    }

}
