package com.lycanitesmobs.core.item.consumable.holiday;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
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
import net.minecraft.world.level.Level;

import java.util.List;

public class ItemWinterGiftLarge extends BaseItem {

    // ==================================================
    //                   Constructor
    // ==================================================
    public ItemWinterGiftLarge(Item.Properties properties) {
        super(properties);
        this.itemName = "wintergiftlarge";
        this.setup();
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
            this.open(itemStack, world, player);
        }

        return InteractionResult.SUCCESS.heldItemTransformedTo(itemStack);
    }


    // ==================================================
    //                       Open
    // ==================================================
    public void open(ItemStack itemStack, Level world, Player player) {
        MutableComponent message = Component.translatable("item.lycanitesmobs." + this.itemName + ".bad");
        player.sendSystemMessage(message);
        this.playSound(world, player.blockPosition(), ObjectManager.getSound(this.itemName + "_bad"), SoundSource.AMBIENT, 5.0F, 1.0F);

        // Lots of Random Tricks:
        List<EntityType> entityTypes = ObjectLists.getEntites("winter_tricks");
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
                    if (entityCreature.getCreatureDefinitionName().equals("wildkin"))
                        entityCreature.setCustomName(Component.literal("Gooderness"));
                    else if (entityCreature.getCreatureDefinitionName().equals("jabberwock"))
                        entityCreature.setCustomName(Component.literal("Rudolph"));
                    else if (entityCreature.getCreatureDefinitionName().equals("ent"))
                        entityCreature.setCustomName(Component.literal("Salty Tree"));
                    else if (entityCreature.getCreatureDefinitionName().equals("treant"))
                        entityCreature.setCustomName(Component.literal("Salty Tree"));
                    else if (entityCreature.getCreatureDefinitionName().equals("reaper"))
                        entityCreature.setCustomName(Component.literal("Satan Claws"));
                    else if (entityCreature.getCreatureDefinitionName().equals("behemoth"))
                        entityCreature.setCustomName(Component.literal("Krampus"));
                }

                DeferredLevelActionManager.spawnEntity(world, player.blockPosition(), null, entity);
            }
        }
    }
}
