package com.lycanitesmobs.core.item.summoningstaff;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemStaffSturdy extends ItemStaffSummoning {
	
	// ==================================================
	//                   Constructor
	// ==================================================
    public ItemStaffSturdy(Item.Properties properties, String itemName, String textureName) {
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
    	return 2;
    }
    public float getSummonCostMod() {
    	return 1.0F;
    }
    
    // ========== Summon Duration ==========
    public int getSummonDuration() {
    	return 120 * 20;
    }
    
	
	// ==================================================
	//                     Repairs
	// ==================================================
    // TODO 26.x: repairability moved to the REPAIRABLE data component.
}
