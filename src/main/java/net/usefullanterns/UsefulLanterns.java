package net.usefullanterns;

import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.block.LanternBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.usefullanterns.render.LanternTrinketRenderer;
import net.usefullanterns.trinket.LanternRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsefulLanterns implements ModInitializer {

    public static final String MOD_ID = "usefullanterns";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Environment(EnvType.CLIENT)
    public static void onInitializeClient() {
        UsefulLanternsConfig.get();
        LanternTrinketRenderer renderer = new LanternTrinketRenderer();

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            int count = 0;
            for (Item item : Registries.ITEM) {
                if (item instanceof BlockItem bi && bi.getBlock() instanceof LanternBlock) {
                    TrinketRendererRegistry.registerRenderer(item, renderer);
                    count++;
                }
            }
            LOGGER.info("Useful Lanterns: Registered 3D renderers for {} lanterns.", count);
        });
    }

    @Override
    public void onInitialize() {
        LanternRegistrar.register();
    }
}