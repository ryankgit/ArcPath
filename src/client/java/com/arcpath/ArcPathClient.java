package com.arcpath;

import com.arcpath.config.ArcPathConfig;
import com.arcpath.trajectory.TrajectoryRenderer;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArcPathClient implements ClientModInitializer {

    public static final String MOD_ID = "arcpath";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] Initializing ArcPath", MOD_ID);
        ArcPathConfig.register();
        TrajectoryRenderer.register();
    }
}