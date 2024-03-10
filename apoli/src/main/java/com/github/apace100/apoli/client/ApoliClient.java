package com.github.apace100.apoli.client;

import com.github.apace100.apoli.network.ApoliPackets;
import net.fabricmc.api.ClientModInitializer;

public class ApoliClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        ApoliPackets.registerS2C();

    }
}
