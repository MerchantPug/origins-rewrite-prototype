package com.github.apace100.apoli.network;

import com.github.apace100.apoli.network.s2c.SyncPowerHolderAttachmentS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class ApoliPackets {

    public static void register() {
        PayloadTypeRegistry.playS2C().register(SyncPowerHolderAttachmentS2CPacket.ID, SyncPowerHolderAttachmentS2CPacket.CODEC);
    }

    public static void registerS2C() {
        ClientPlayNetworking.registerGlobalReceiver(SyncPowerHolderAttachmentS2CPacket.ID, (payload, context) -> payload.handle());
    }

}
