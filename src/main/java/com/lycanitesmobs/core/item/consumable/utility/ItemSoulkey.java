package com.lycanitesmobs.core.item.consumable.utility;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.altar.AltarInfo;
import com.lycanitesmobs.core.item.base.BaseItem;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ItemSoulkey extends BaseItem {
    public int variant = 0; // 0 = Standard, 1 = Diamond, 2 = Emerald

    // ==================================================
    //                   Constructor
    // ==================================================
    public ItemSoulkey(Item.Properties properties, String itemName, int variant) {
        super(properties);
        this.itemName = itemName;
        this.variant = variant;
        this.setup();
    }


    // ==================================================
    //                       Use
    // ==================================================
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        if (!AltarInfo.checkAltarsEnabled() && !player.level().isClientSide()) {
            MutableComponent message = Component.translatable("message.soulkey.disabled");
            player.sendSystemMessage(message);
            return InteractionResult.FAIL;
        }

        // Get Possible Altars:
        List<AltarInfo> possibleAltars = new ArrayList<>();
        if (AltarInfo.getAltars().isEmpty())
            LMHelperClass.logWarningMessage("No altars have been registered, Soulkeys will not work at all.");
        for (AltarInfo altarInfo : AltarInfo.getAltars().values()) {
            if (altarInfo.checkBlockEvent(player, world, pos) && altarInfo.quickCheck(player, world, pos)) {
                possibleAltars.add(altarInfo);
            }
        }
        if (possibleAltars.isEmpty()) {
            MutableComponent message = Component.translatable("message.soulkey.none");
            player.sendSystemMessage(message);
            return InteractionResult.FAIL;
        }

        // Activate First Valid Altar:
        for (AltarInfo altarInfo : possibleAltars) {
            if (altarInfo.fullCheck(player, world, pos)) {

                // Valid Altar:
                if (!player.level().isClientSide()) {
                    if (!altarInfo.activate(player, world, pos, this.variant)) {
                        MutableComponent message = Component.translatable("message.soulkey.badlocation");
                        player.sendSystemMessage(message);
                        return InteractionResult.FAIL;
                    }
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }
                    MutableComponent message = Component.translatable("message.soulkey.active");
                    player.sendSystemMessage(message);
                }
                return InteractionResult.SUCCESS;
            }
        }
        if (!player.level().isClientSide()) {
            MutableComponent message = Component.translatable("message.soulkey.invalid");
            player.sendSystemMessage(message);
        }

        return InteractionResult.FAIL;
    }
}
