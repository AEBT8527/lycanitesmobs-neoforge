package com.lycanitesmobs.client.event.tooltip;

import net.neoforged.fml.common.EventBusSubscriber;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.tooltip.*;
import com.lycanitesmobs.core.item.tooltip.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = LycanitesMobs.MODID, value = Dist.CLIENT)
public class EquipmentTooltipClientRegistration {
    @SubscribeEvent
    public static void registerFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(EquipmentSeparatorTooltipComponent.class, EquipmentSeparatorClientTooltipComponent::new);
        event.register(EquipmentStatBarTooltipComponent.class, EquipmentStatBarClientTooltipComponent::new);
        event.register(EquipmentElementTitleComponent.class, EquipmentElementTitleClientComponent::new);
        event.register(EquipmentElementIconsComponent.class, EquipmentElementIconsClientComponent::new);
        event.register(EquipmentSlotIconTooltipComponent.class, EquipmentSlotIconClientTooltipComponent::new);
        event.register(EquipmentSlotTitleComponent.class, EquipmentSlotTitleClientComponent::new);
        event.register(EquipmentSlotRowTooltipComponent.class, EquipmentSlotRowClientTooltipComponent::new);

    }

}
