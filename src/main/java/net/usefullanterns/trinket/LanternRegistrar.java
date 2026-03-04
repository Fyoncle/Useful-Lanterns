package net.usefullanterns.trinket;

import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.LanternBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.usefullanterns.UsefulLanterns;

public final class LanternRegistrar {

    private LanternRegistrar() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            int count = 0;
            for (Item item : Registries.ITEM) {
                if (registerIfLantern(item)) count++;
            }
            UsefulLanterns.LOGGER.info(
                    "Useful Lanterns: Registered Trinket Behaviour for {} lanterns.", count);
        });
    }

    private static boolean registerIfLantern(Item item) {
        if (item instanceof BlockItem bi && bi.getBlock() instanceof LanternBlock) {
            TrinketsApi.registerTrinket(item, LanternTrinket.INSTANCE);
            return true;
        }
        return false;
    }
}