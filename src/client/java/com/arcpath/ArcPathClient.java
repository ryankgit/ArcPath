package com.arcpath;

import com.arcpath.config.ArcPathConfig;
import com.arcpath.input.ArcPathKeyBinds;
import com.arcpath.trajectory.DebugRenderer;
import com.arcpath.trajectory.ProjectileType;
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
        DebugRenderer.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) 
                return;

            // clear debug history when player switches to a non-throwable
            var heldItem = client.player.getMainHandItem();
            if (ProjectileType.fromItem(heldItem.getItem()).isEmpty())
                DebugRenderer.clear();

            while (ArcPathKeyBinds.TOGGLE_ARC.consumeClick()) {
                ArcPathState.toggleEnabled();
                // clear debug lines when arc is toggled off
                if (!ArcPathState.isEnabled())
                    DebugRenderer.clear();
            }
            while (ArcPathKeyBinds.TOGGLE_DEBUG.consumeClick()) {
                ArcPathState.toggleDebugMode();
            }
        });
    }
}