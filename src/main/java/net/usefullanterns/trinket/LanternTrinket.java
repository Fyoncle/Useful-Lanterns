package net.usefullanterns.trinket;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketEnums;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class LanternTrinket implements Trinket {

    public static final LanternTrinket INSTANCE = new LanternTrinket();

    private LanternTrinket() {
    }

    @Override
    public boolean canEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        String group = slot.inventory().getSlotType().getGroup();
        return group.equals("legs") || group.equals("lantern");
    }

    @Override
    public TrinketEnums.DropRule getDropRule(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return TrinketEnums.DropRule.DEFAULT;
    }
}