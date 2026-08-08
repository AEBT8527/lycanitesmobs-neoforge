package com.lycanitesmobs.client.event;

import net.neoforged.fml.common.EventBusSubscriber;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.event.mobevent.ClientMobEventEvents;
import com.lycanitesmobs.client.event.mobevent.MobEventPlayerClient;
import com.lycanitesmobs.client.gui.screen.base.BaseOverlayScreen;
import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.client.manager.KeyManager;
import com.lycanitesmobs.client.manager.OverlayManager;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.data.config.ConfigDebug;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.util.CreatureRelationshipEntry;
import com.lycanitesmobs.core.item.summoningstaff.ItemStaffSummoning;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import static com.lycanitesmobs.client.gui.screen.base.BaseOverlayScreen.GUI_ICONS_LOCATION;

public class OverlayEvents {
    // ==================================================
    //                  Draw Game Overlay
    // ==================================================
    public static void renderHud(net.minecraft.client.gui.GuiGraphicsExtractor guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        if (ClientManager.getInstance().getClientPlayer() == null)
            return;
        Player player = ClientManager.getInstance().getClientPlayer();
        var minecraft = Minecraft.getInstance();
        var drawhelper = OverlayManager.getInstance().drawHelper;
        var baseOverlayScreen = BaseOverlayScreen.newInstance();
        // 26.x: gui pose is a 2D Matrix3x2fStack and blend state is pipeline-managed.
        var matrixStack = guiGraphics.pose();
        matrixStack.pushMatrix();

        int sWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth(); // getMainWindow()
        int sHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        // ========== Mob/World Events Title ==========
        matrixStack.pushMatrix();
        ClientMobEventEvents.render(player.level(), guiGraphics, baseOverlayScreen, sWidth, sHeight);
        matrixStack.popMatrix();
        // ========== Summoning Focus Bar ==========
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if (playerExt != null && !minecraft.player.getAbilities().instabuild && (
                minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemStaffSummoning
                        || minecraft.player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof ItemStaffSummoning
        )) {
            int barYSpace = 10;
            int barXSpace = -1;

            int summonBarWidth = 9;
            int summonBarHeight = 9;
            int summonBarX = (sWidth / 2) + 10;
            int summonBarY = sHeight - 30 - summonBarHeight;

            summonBarY -= barYSpace;
            if (minecraft.player.isEyeInFluid(FluidTags.WATER))
                summonBarY -= barYSpace;

            var emptyTex = TextureManager.getTexture("GUIPetSpiritEmpty");
            var fullTex = TextureManager.getTexture("GUIPetSpiritUsed");
            var fillTex = TextureManager.getTexture("GUIPetSpiritFilling");

            for (int n = 0; n < 10; n++) {
                int x = summonBarX + ((summonBarWidth + barXSpace) * (9 - n));
                drawhelper.drawTexture(guiGraphics, emptyTex, x, summonBarY, 0, 1, 1, summonBarWidth, summonBarHeight);

                int threshold = playerExt.getSummonFocusMax() - (n * playerExt.getSummonFocusCharge());
                if (playerExt.getSummonFocus() >= threshold) {
                    drawhelper.drawTexture(guiGraphics, fullTex, x, summonBarY, 0, 1, 1, summonBarWidth, summonBarHeight);
                } else if (playerExt.getSummonFocus() + playerExt.getSummonFocusCharge() > threshold) {
                    float scale = (float) (playerExt.getSummonFocus() % playerExt.getSummonFocusCharge())
                            / (float) playerExt.getSummonFocusCharge();
                    int w = Math.round(summonBarWidth * scale);
                    if (w > 0) {
                        drawhelper.drawTexture(guiGraphics, fillTex,
                                x, summonBarY,
                                0, scale, 1,
                                w, summonBarHeight);
                    }
                }
            }
        }


        // ========== Mount Stamina Bar ==========
        if (minecraft.player.getVehicle() != null && minecraft.player.getVehicle() instanceof RideableCreatureEntity) {
            RideableCreatureEntity mount = (RideableCreatureEntity) minecraft.player.getVehicle();
            float mountStamina = mount.getStaminaPercent();

            // Mount Controls Message:
            if (baseOverlayScreen.mountMessageTime > 0) {
                MutableComponent mountMessage = Component.translatable("gui.mount.controls.prefix")
                        .append(" ").append(KeyManager.instance.mountAbility.getTranslatedKeyMessage())
                        .append(" ").append(Component.translatable("gui.mount.controls.ability"));
//						.append(" ").append(KeyHandler.instance.dismount.getTranslatedKeyMessage())
//						.append(" ").append(Component.translatable("gui.mount.controls.dismount"));
                minecraft.gui.setOverlayMessage(mountMessage, false);
            }

            // Mount Ability Stamina Bar:
            // icons.png was removed in 1.21+ and the sprite overload set changed again in 26.x;
            // draw a simple two-tone bar (background + fill) with gui fill() for now.
            int staminaBarWidth = 182;
            int staminaBarHeight = 5;
            int staminaEnergyWidth = (int) ((float) (staminaBarWidth + 1) * mountStamina);
            int staminaBarX = (sWidth / 2) - (staminaBarWidth / 2);
            int staminaBarY = sHeight - 32 + 3;

            guiGraphics.fill(staminaBarX, staminaBarY, staminaBarX + staminaBarWidth, staminaBarY + staminaBarHeight, 0xAA202020);
            if (staminaEnergyWidth > 0)
                guiGraphics.fill(staminaBarX, staminaBarY, staminaBarX + Math.min(staminaEnergyWidth, staminaBarWidth), staminaBarY + staminaBarHeight, 0xFF57C447);

            if (baseOverlayScreen.mountMessageTime > 0)
                baseOverlayScreen.mountMessageTime--;
        } else
            baseOverlayScreen.mountMessageTime = baseOverlayScreen.mountMessageTimeMax;

        // ========== Taming Reputation Bar ==========
        HitResult mouseOver = Minecraft.getInstance().hitResult;
        if (mouseOver instanceof EntityHitResult) {
            Entity mouseOverEntity = ((EntityHitResult) mouseOver).getEntity();
            if (mouseOverEntity instanceof BaseCreatureEntity) {
                BaseCreatureEntity creatureEntity = (BaseCreatureEntity) mouseOverEntity;
                CreatureInfo creatureInfo = creatureEntity.getCreatureInfo();
                CreatureRelationshipEntry relationshipEntry = creatureEntity.getRelationshipEntry(player);
                if (relationshipEntry != null && relationshipEntry.getReputation() > 0 && !creatureEntity.isTamed()) {
                    float barWidth = 100;
                    float barHeight = 11;
                    float barX = ((float) minecraft.getWindow().getGuiScaledWidth() / 2) - (barWidth / 2);
                    float barY = (float) minecraft.getWindow().getGuiScaledHeight() * 0.75F;
                    float barCenter = barX + (barWidth / 2);

                    drawhelper.drawTexture(guiGraphics, TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);
                    float reputationNormal = Math.min(1, (float) relationshipEntry.getReputation() / creatureInfo.getTamingReputation());
                    String barFillTexture = "GUIPetBarRespawn";
                    if (relationshipEntry.getReputation() >= creatureInfo.getFriendlyReputation()) {
                        barFillTexture = "GUIPetBarHealth";
                    }
                    drawhelper.drawTexture(guiGraphics, TextureManager.getTexture(barFillTexture), barX, barY, 0, reputationNormal, 1, barWidth * reputationNormal, barHeight);
                    String reputationText = Component.translatable("entity.reputation").getString() + ": " + relationshipEntry.getReputation() + "/" + creatureInfo.getTamingReputation();
                    drawhelper.draw(guiGraphics, reputationText, barCenter - ((float) drawhelper.getStringWidth(reputationText) / 2), barY + 2, 0xFFFFFF);
                }
            }
        }
        matrixStack.popMatrix();
    }

    // ==================================================
    //                 Debug Overlay
    // ==================================================
    public static void onGameOverlay(OverlayManager.Text event) {
        if (!ConfigDebug.INSTANCE.creatureOverlay.get()) {
            return;
        }

        // Entity:
        HitResult mouseOver = Minecraft.getInstance().hitResult;
        if (mouseOver instanceof EntityHitResult) {
            Entity mouseOverEntity = ((EntityHitResult) mouseOver).getEntity();
            if (mouseOverEntity instanceof BaseCreatureEntity) {
                BaseCreatureEntity mouseOverCreature = (BaseCreatureEntity) mouseOverEntity;
                event.getLeft().add("");
                event.getLeft().add("Target Creature: " + mouseOverCreature.getName().getString());
                event.getLeft().add("Distance To player: " + mouseOverCreature.distanceTo(Minecraft.getInstance().player));
                event.getLeft().add("Elements: " + mouseOverCreature.getElementNames().getString());
                event.getLeft().add("Subspecies: " + mouseOverCreature.getSubspeciesIndex());
                event.getLeft().add("Variant: " + mouseOverCreature.getVariantIndex());
                event.getLeft().add("Level: " + mouseOverCreature.getMobLevel());
                event.getLeft().add("Experience: " + mouseOverCreature.getExperience() + "/" + mouseOverCreature.getExperienceForNextLevel());
                event.getLeft().add("Size: " + mouseOverCreature.getSizeScale());
                event.getLeft().add("");
                event.getLeft().add("Health: " + mouseOverCreature.getHealth() + "/" + mouseOverCreature.getMaxHealth() + " Fresh: " + mouseOverCreature.getCreatureStats().getHealth());
                event.getLeft().add("Speed: " + mouseOverCreature.getAttribute(Attributes.MOVEMENT_SPEED).getValue() + "/" + mouseOverCreature.getCreatureStats().getSpeed());
                event.getLeft().add("");
                event.getLeft().add("Defense: " + mouseOverCreature.getCreatureStats().getDefense());
                event.getLeft().add("Armor: " + mouseOverCreature.getArmorValue());
                event.getLeft().add("");
                event.getLeft().add("Damage: " + mouseOverCreature.getCreatureStats().getDamage());
                event.getLeft().add("Melee Speed: " + mouseOverCreature.getCreatureStats().getAttackSpeed());
                event.getLeft().add("Melee Range: " + mouseOverCreature.getPhysicalRange());
                event.getLeft().add("Ranged Speed: " + mouseOverCreature.getCreatureStats().getRangedSpeed());
                event.getLeft().add("Pierce: " + mouseOverCreature.getCreatureStats().getPierce());
                event.getLeft().add("");
                event.getLeft().add("Effect Duration: " + mouseOverCreature.getCreatureStats().getEffect() + " Base Seconds");
                event.getLeft().add("Effect Amplifier: x" + mouseOverCreature.getCreatureStats().getAmplifier());
                event.getLeft().add("");
                event.getLeft().add("Has Attack Target: " + mouseOverCreature.hasAttackTarget());
                event.getLeft().add("Has Avoid Target: " + mouseOverCreature.hasAvoidTarget());
                event.getLeft().add("Has Master Target: " + mouseOverCreature.hasMaster());
                event.getLeft().add("Has Parent Target: " + mouseOverCreature.hasParent());

                event.getLeft().add("");
                CreatureRelationshipEntry relationshipEntry = mouseOverCreature.getRelationshipEntry(Minecraft.getInstance().player);
                event.getLeft().add("Reputation with Player: " + (relationshipEntry != null ? relationshipEntry.getReputation() : 0) + "/" + mouseOverCreature.getTamingReputation());

                if (mouseOverEntity instanceof TameableCreatureEntity) {
                    TameableCreatureEntity mouseOverTameable = (TameableCreatureEntity) mouseOverCreature;
                    event.getLeft().add("Owner ID: " + (mouseOverTameable.getOwnerId() != null ? mouseOverTameable.getOwnerId().toString() : "None"));
                    event.getLeft().add("Owner Name: " + mouseOverTameable.getOwnerName().getString());
                }
            }
        }
    }
}
