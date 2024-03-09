package com.github.apace100.apoli.attachment;

import com.github.apace100.apoli.attachment.PowerHolderApi;
import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;

import java.util.Map;
import java.util.WeakHashMap;

public class ApoliEntityApis {
    public static final EntityApiLookup<PowerHolderApi, Void> POWER_HOLDER = EntityApiLookup.get(PowerHolderApi.ID, PowerHolderApi.class, Void.class);

    private static final Map<LivingEntity, PowerHolderApi> POWER_HOLDER_API_CACHE = new WeakHashMap<>(1028);

    public static void register() {
        for (EntityType<?> entityType : Registries.ENTITY_TYPE) {
            if (entityType.getBaseClass().isAssignableFrom(LivingEntity.class)) {
                POWER_HOLDER.registerForType((entity, unused) -> {
                    if (entity instanceof LivingEntity living) {
                        return POWER_HOLDER_API_CACHE.computeIfAbsent(living, PowerHolderApi::new);
                    }
                    return null;
                }, entityType);
            }
        }
    }
}
