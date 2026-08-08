package com.lycanitesmobs.client.event.mobevent;

import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.client.manager.OverlayManager;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.client.gui.screen.base.BaseOverlayScreen;
import com.lycanitesmobs.core.event.mobevent.MobEvent;
import com.lycanitesmobs.core.event.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.lwjgl.opengl.GL11;

public class MobEventPlayerClient {

    // Properties:
    public MobEvent mobEvent;

    // Properties:
    public int ticks = 0;
    public Level world;
    public SimpleSoundInstance sound;

    /**
     * True if the started event was already running and show display as 'Event Extended' in chat.
     **/
    public boolean extended = false;


    // ==================================================
    //                     Constructor
    // ==================================================
    public MobEventPlayerClient(MobEvent mobEvent, Level world) {
        this.mobEvent = mobEvent;
        this.world = world;
        if (!world.isClientSide())
            LMHelperClass.logWarningMessage("Created a MobEventClient with a server side world, this shouldn't happen, things are going to get weird!");
    }


    // ==================================================
    //                       Start
    // ==================================================
    public void onStart(Player player) {
        if (!this.extended) {
            this.ticks = 0;
        }
        MutableComponent eventMessage = Component.translatable("event." + (extended ? "extended" : "started") + ".prefix")
                .append(" ")
                .append(this.mobEvent.getTitle())
                .append(" ")
                .append(Component.translatable("event." + (extended ? "extended" : "started") + ".suffix"));
        player.sendSystemMessage(eventMessage);

        if (player.getAbilities().instabuild && !MobEventPlayerServer.shouldTestOnCreative() && "world".equalsIgnoreCase(this.mobEvent.getChannel())) {
            return;
        }

        this.playSound();
    }

    public void playSound() {
        if (ObjectManager.getSound("mobevent_" + this.mobEvent.getTitleName().toLowerCase()) == null) {
            LMHelperClass.logWarning("MobEvent", "Sound missing for: " + this.mobEvent.getTitle());
            return;
        }
        this.sound = new MobEventSound(ObjectManager.getSound("mobevent_" + this.mobEvent.getTitleName().toLowerCase()), SoundSource.RECORDS, ClientManager.getInstance().getClientPlayer(), this.world.getRandom(), 1.0F, 1.0F);
        Minecraft.getInstance().getSoundManager().play(this.sound);
    }


    // ==================================================
    //                      Finish
    // ==================================================
    public void onFinish(Player player) {
        MutableComponent eventMessage = Component.translatable("event.finished.prefix")
                .append(" ")
                .append(this.mobEvent.getTitle())
                .append(" ")
                .append(Component.translatable("event.finished.suffix"));
        player.sendSystemMessage(eventMessage);
    }


    // ==================================================
    //                      Update
    // ==================================================
    public void onUpdate() {
        this.ticks++;
    }


    // ==================================================
    //                       GUI
    // ==================================================
    public void onGUIUpdate(GuiGraphicsExtractor matrixStack, BaseOverlayScreen gui, int sWidth, int sHeight) {
        Player player = ClientManager.getInstance().getClientPlayer();
        if (player.getAbilities().instabuild && !MobEventPlayerServer.shouldTestOnCreative() && "world".equalsIgnoreCase(this.mobEvent.getChannel())) {
            return;
        }
        if (this.world == null || this.world != player.level()) return;
        if (!this.world.isClientSide()) return;

        int introTime = 12 * 20;
        if (this.ticks > introTime) return;
        int startTime = 2 * 20;
        int stopTime = 4 * 20;
        float animation = 1.0F;

        if (this.ticks < startTime)
            animation = (float) this.ticks / (float) startTime;
        else if (this.ticks > introTime - stopTime)
            animation = ((float) (introTime - this.ticks) / (float) stopTime);

        if (animation <= 0F) {
            return;
        }

        int width = 256;
        int height = 256;
        int x = (sWidth / 2) - (width / 2);
        int y = (sHeight / 2) - (height / 2);
        int u = width;
        int v = height;
        x += 3 - (this.ticks % 6);
        y += 2 - (this.ticks % 4);

        // 26.x: no bound-texture state or RenderSystem color; blit takes the texture directly.
        // (The old animated alpha fade is dropped; gui pipeline handles blending.)
        OverlayManager.getInstance().drawHelper.drawTexturedModalRect(matrixStack, this.getTexture(), x, y, u, v, width, height);
    }


    public Identifier getTexture() {
        if (TextureManager.getTexture("guimobevent" + this.mobEvent.getTitleName()) == null)
            TextureManager.addTexture("guimobevent" + this.mobEvent.getTitleName(), "textures/mobevents/" + this.mobEvent.getTitleName().toLowerCase() + ".png");
        return TextureManager.getTexture("guimobevent" + this.mobEvent.getTitleName());
    }
}
