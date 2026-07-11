package com.lycanitesmobs.core.item.consumable.holiday;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.item.CustomItemEntity;
import com.lycanitesmobs.core.data.info.ObjectLists;
import com.lycanitesmobs.core.item.base.BaseItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class ItemHalloweenTreat extends BaseItem {

    // ==================================================
    //                   Constructor
    // ==================================================
    public ItemHalloweenTreat(Item.Properties properties) {
        super(properties);
        this.itemName = "halloweentreat";
        this.setup();
        ObjectManager.addSound(this.itemName + "_good", "item." + this.itemName + ".good");
        ObjectManager.addSound(this.itemName + "_bad", "item." + this.itemName + ".bad");
    }


    // ==================================================
    //                    Item Use
    // ==================================================
    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
        }

        if (!world.isClientSide()) {
            if (player.getRandom().nextBoolean())
                this.openGood(itemStack, world, player);
            else
                this.openBad(itemStack, world, player);
        }

        return InteractionResult.SUCCESS.heldItemTransformedTo(itemStack);
    }


    // ==================================================
    //                       Good
    // ==================================================
    public void openGood(ItemStack itemStack, Level world, Player player) {
        MutableComponent message = Component.translatable("item.lycanitesmobs." + this.itemName + ".good");
        player.sendSystemMessage(message);
        this.playSound(world, player.position().x(), player.position().y(), player.position().z(), ObjectManager.getSound(this.itemName + "_good"), SoundSource.AMBIENT, 5.0F, 1.0F);

        // Three Random Treats:
        List<ItemStack> dropStacks = ObjectLists.getItems("halloween_treats");
        if (dropStacks == null || dropStacks.isEmpty())
            return;
        ItemStack dropStack = dropStacks.get(player.getRandom().nextInt(dropStacks.size()));
        dropStack.setCount(1 + player.getRandom().nextInt(4));
        CustomItemEntity entityItem = new CustomItemEntity(world, player.position().x(), player.position().y(), player.position().z(), dropStack);
        entityItem.setPickUpDelay(10);
        DeferredLevelActionManager.spawnEntity(world, player.blockPosition(), null, entityItem);
    }


    // ==================================================
    //                       Bad
    // ==================================================
    public void openBad(ItemStack itemStack, Level world, Player player) {
        MutableComponent message = Component.translatable("item.lycanitesmobs." + this.itemName + ".bad");
        player.sendSystemMessage(message);
        this.playSound(world, player.position().x(), player.position().y(), player.position().z(), ObjectManager.getSound(this.itemName + "_bad"), SoundSource.AMBIENT, 5.0F, 1.0F);

        // One Random Trick:
        List<EntityType> entityTypes = ObjectLists.getEntites("halloween_tricks");
        if (entityTypes.isEmpty())
            return;
        EntityType entityType = entityTypes.get(player.getRandom().nextInt(entityTypes.size()));
        if (entityType != null) {
            Entity entity = entityType.create(world, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
            if (entity != null) {
                entity.snapTo(player.position().x(), player.position().y(), player.position().z(), player.yRotO, player.xRotO);

                // Themed Names:
                if (entity instanceof BaseCreatureEntity) {
                    BaseCreatureEntity entityCreature = (BaseCreatureEntity) entity;
                    entityCreature.addLevel(world.getRandom().nextInt(10));
                    if (entityCreature.getCreatureDefinitionName().equals("ent"))
                        entityCreature.setCustomName(Component.literal("Twisted Ent"));
                    else if (entityCreature.getCreatureDefinitionName().equals("treant"))
                        entityCreature.setCustomName(Component.literal("Wicked Treant"));
                    else if (entityCreature.getCreatureDefinitionName().equals("epion"))
                        entityCreature.setCustomName(Component.literal("Vampire Bat"));
                    else if (entityCreature.getCreatureDefinitionName().equals("grue"))
                        entityCreature.setCustomName(Component.literal("Shadow Clown"));
                }

                DeferredLevelActionManager.spawnEntity(world, player.blockPosition(), null, entity);
            }
        }
    }


    // ==================================================
    //                       Lists
    // ==================================================
    public static void createObjectLists() {
        // Halloween Treats:
        ObjectLists.addItem("halloween_treats", Items.DIAMOND);
        ObjectLists.addItem("halloween_treats", Items.GOLD_INGOT);
        ObjectLists.addItem("halloween_treats", Items.EMERALD);
        ObjectLists.addItem("halloween_treats", Blocks.IRON_BLOCK);
        ObjectLists.addItem("halloween_treats", Items.ENDER_PEARL);
        ObjectLists.addItem("halloween_treats", Items.BLAZE_ROD);
        ObjectLists.addItem("halloween_treats", Items.GLOWSTONE_DUST);
        ObjectLists.addItem("halloween_treats", ObjectManager.getItem("mosspie"));
        ObjectLists.addItem("halloween_treats", ObjectManager.getItem("bulwarkburger"));
        ObjectLists.addItem("halloween_treats", ObjectManager.getItem("paleosalad"));
        ObjectLists.addItem("halloween_treats", ObjectManager.getItem("searingtaco"));
        ObjectLists.addItem("halloween_treats", ObjectManager.getItem("devillasagna"));
        ObjectLists.addFromConfig("halloween_treats");

        // Halloween Mobs:
        ObjectLists.addEntity("halloween_tricks", "behemoth");
        ObjectLists.addEntity("halloween_tricks", "ent");
        ObjectLists.addEntity("halloween_tricks", "treant");
        ObjectLists.addEntity("halloween_tricks", "wraith");
        ObjectLists.addEntity("halloween_tricks", "grue");
        ObjectLists.addEntity("halloween_tricks", "reaper");
        ObjectLists.addEntity("halloween_tricks", "epion");
        ObjectLists.addEntity("halloween_tricks", "tpumpkyn");
    }
}
