package com.lycanitesmobs.core.item.summoningstaff;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemStaffSavage extends ItemStaffSummoning {
	
	// ==================================================
	//                   Constructor
	// ==================================================
    public ItemStaffSavage(Item.Properties properties, String itemName, String textureName) {
        super(properties, itemName, textureName);
    }
	
    
	// ==================================================
	//                       Use
	// ==================================================
    // ========== Rapid Time ==========
    @Override
    public int getRapidTime(ItemStack itemStack) {
        return 20;
    }
    
    // ========== Summon Cost ==========
    public int getSummonCostBoost() {
    	return 0;
    }
    public float getSummonCostMod() {
    	return 1.0F;
    }
    
    // ========== Summon Duration ==========
    public int getSummonDuration() {
    	return 60 * 20;
    }
    
    // ========== Summon Amount ==========
    public int getSummonAmount() {
    	return 2;
    }
    
    // ========== Minion Effects ==========
    public void applyMinionEffects(BaseCreatureEntity minion) {
    	minion.setHealth(minion.getHealth() / 2);
    }
    
	
	// ==================================================
	//                     Repairs
	// ==================================================
    // TODO 26.x: repairability moved to the REPAIRABLE data component.
}
