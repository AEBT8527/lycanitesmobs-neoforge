package com.lycanitesmobs.client.event;

import net.neoforged.fml.common.EventBusSubscriber;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.renderer.item.EquipmentItemDecorator;
import com.lycanitesmobs.core.manager.ObjectManager;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = LycanitesMobs.MODID, value = Dist.CLIENT)
public class EquipmentItemDecorationRegistration {
    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        Item equipmentItem = ObjectManager.getItem("equipment");
        if (equipmentItem != null) {
            event.register(equipmentItem, new EquipmentItemDecorator());
        }
    }
}
