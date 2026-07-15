package com.arcpath;

import com.arcpath.config.ArcPathConfig;
import com.arcpath.input.ArcPathKeyBinds;
import com.arcpath.trajectory.TrajectoryRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArcPathClient implements ClientModInitializer {

    public static final String MOD_ID = "arcpath";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] Initializing ArcPath", MOD_ID);

        ArcPathConfig.register();
        ArcPathKeyBinds.register();
        TrajectoryRenderer.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ArcPathKeyBinds.TOGGLE_ARC.consumeClick()) {
                ArcPathState.toggle();
            }
        });
    }
}