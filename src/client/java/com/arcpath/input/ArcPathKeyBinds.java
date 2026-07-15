package com.arcpath.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class ArcPathKeyBinds {
    private ArcPathKeyBinds() { }

    public static final KeyMapping.Category ARCPATH_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("arcpath", "arcpath"));

    public static KeyMapping TOGGLE_ARC;

    public static void register() {
        TOGGLE_ARC = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.arcpath.toggle_arc",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_Y,
            ARCPATH_CATEGORY
        ));
    }
}