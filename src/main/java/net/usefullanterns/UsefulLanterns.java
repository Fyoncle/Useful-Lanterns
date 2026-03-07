package net.usefullanterns;

import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.block.LanternBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.usefullanterns.render.LanternTrinketRenderer;

public class UsefulLanterns {

    public static final String MOD_ID = "usefullanterns";

    @Environment(EnvType.CLIENT)
    public static void onInitializeClient() {
        UsefulLanternsConfig.init();
        LanternTrinketRenderer renderer = new LanternTrinketRenderer();

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            for (Item item : Registries.ITEM) {
                if (item instanceof BlockItem bi && bi.getBlock() instanceof LanternBlock) {
                    TrinketRendererRegistry.registerRenderer(item, renderer);
                }
            }
        });
    }
}