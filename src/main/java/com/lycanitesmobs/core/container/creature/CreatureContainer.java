package com.lycanitesmobs.core.container.creature;

import com.lycanitesmobs.core.container.base.BaseContainer;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

import java.util.List;

public class CreatureContainer extends BaseContainer {
	public static final MenuType<CreatureContainer> TYPE = (MenuType<CreatureContainer>) IMenuTypeExtension.create(CreatureContainer::new);
	protected final BaseCreatureEntity creature;

	/**
	 * Client Constructor
	 * @param windowId The window id for the gui screen to use.
	 * @param playerInventory The accessing player's inventory.
	 * @param extraData A packet sent from the server to create the Container from.
	 */
	public CreatureContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
		this(windowId, playerInventory, (BaseCreatureEntity)playerInventory.player.getCommandSenderWorld().getEntity(extraData.readInt()));
	}

	public BaseCreatureEntity getCreature() {
		return this.creature;
	}

	public CreatureInventory getCreatureInventory() {
		return this.creature.getCreatureInventory();
	}

	public List<Slot> getCreatureSlotView() {
		if (this.specialStart < 0 || this.inventoryFinish < 0 || this.specialStart >= this.slots.size()) {
			return List.of();
		}
		return this.slots.subList(this.specialStart, Math.min(this.inventoryFinish + 1, this.slots.size()));
	}

	/**
	 * Main Constructor
	 * @param windowId The window id for the gui screen to use.
	 * @param playerInventory The accessing player's inventory.
	 * @param creature The creature to access.
	 */
	public CreatureContainer(int windowId, Inventory playerInventory, BaseCreatureEntity creature) {
		super(TYPE, windowId);
		this.creature = creature;

		// Player Inventory:
		this.addPlayerSlots(playerInventory, 0, 0);

		// Creature Equipment:
		this.specialStart = this.slots.size();
		this.drawCreatureEquipment(creature, 8, 18);
		this.specialFinish = this.slots.size() - 1;

		// Creature Inventory
		this.inventoryStart = this.slots.size();
		CreatureInventory creatureInventory = creature.getCreatureInventory();
		if(creatureInventory.getItemSlotsSize() > 0)
			this.addSlotsByColumn(creatureInventory, 8 + (18 * 4), 18, 5, 0, creatureInventory.getActiveItemSlotsSize() - 1);
		this.inventoryFinish = this.slots.size() - 1;
	}


	
	
	// ==================================================
  	//                    Draw Slots
  	// ==================================================
	public void drawCreatureEquipment(BaseCreatureEntity creature, int equipX, int equipY) {
		CreatureInventory creatureInventory = creature.getCreatureInventory();

		// Creature Accessories:
		if(creature instanceof RideableCreatureEntity) {
			this.addSlot(creatureInventory, creatureInventory.getSlotFromType("saddle"), equipX, equipY);
			equipY += 18;
		}
		if(creature.getBagSize() > 0) {
			this.addSlot(creatureInventory, creatureInventory.getSlotFromType("bag"), equipX, equipY);
			equipY += 18;
		}

		// Weapon and Dye slots will go here.
		
		// Creature Armor:
		equipX += 18;
		equipY = 18;
		if(creatureInventory.useAdvancedArmor()) {
			this.addSlot(creatureInventory, creatureInventory.getSlotFromType("head"), equipX, equipY);
			equipY += 18;
		}
		this.addSlot(creatureInventory, creatureInventory.getSlotFromType("chest"), equipX, equipY);
		equipY += 18;
		if(creatureInventory.useAdvancedArmor()) {
			this.addSlot(creatureInventory, creatureInventory.getSlotFromType("legs"), equipX, equipY);
			equipY += 18;
			this.addSlot(creatureInventory, creatureInventory.getSlotFromType("feet"), equipX, equipY);
			equipY += 18;
		}
	}
	
	
	// ==================================================
  	//                  Container Closed
  	// ==================================================
	@Override
	public void removed(Player player) {
		super.removed(player);
	}
}
