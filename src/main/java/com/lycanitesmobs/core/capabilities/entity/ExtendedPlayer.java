package com.lycanitesmobs.core.capabilities.entity;

import net.minecraft.resources.ResourceLocation;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.data.info.creature.CreatureKnowledge;
import com.lycanitesmobs.core.data.info.creature.CreatureType;
import com.lycanitesmobs.core.data.info.gui.Beastiary;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.util.VersionChecker;
import com.lycanitesmobs.core.data.config.ConfigExtra;
import com.lycanitesmobs.core.data.config.ConfigPlayer;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.special.PortalEntity;
import com.lycanitesmobs.core.item.summoningstaff.ItemStaffSummoning;
import com.lycanitesmobs.core.network.message.*;
import com.lycanitesmobs.core.entity.pets.PetEntry;
import com.lycanitesmobs.core.manager.PetManager;
import com.lycanitesmobs.core.entity.pets.PlayerFamiliars;
import com.lycanitesmobs.core.entity.pets.SummonSet;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.IdentityHashMap;

public class ExtendedPlayer {
    /** Serializes this attachment via the existing NBT read/write methods. */
    public static final IAttachmentSerializer<CompoundTag, ExtendedPlayer> SERIALIZER = new IAttachmentSerializer<>() {
        @Override
        public CompoundTag write(ExtendedPlayer attachment, HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            attachment.writeNBT(tag);
            return tag;
        }

        @Override
        public ExtendedPlayer read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
            ExtendedPlayer attachment = new ExtendedPlayer();
            attachment.readNBT(tag);
            return attachment;
        }
    };

    protected static Map<Player, ExtendedPlayer> clientExtendedPlayers = new IdentityHashMap<>();
    protected static Map<Player, ExtendedPlayer> serverExtendedPlayers = new HashMap<>();
    protected static Map<UUID, CompoundTag> backupNBTTags = new HashMap<>();

    // Player Info and Containers:
    protected Player player;
    protected Beastiary beastiary;
    protected PetManager petManager;
    protected long timePlayed = 0;

    // Beastiary Menu:
    protected CreatureType selectedCreatureType;
    protected CreatureInfo selectedCreature;
    protected int selectedSubspecies = 0;
    protected int selectedVariant = 0;
    protected int selectedPetType = 0;
    protected PetEntry selectedPet;

    protected long currentTick = 0;
    protected boolean needsFirstSync = true;
    /**
     * Set for a few seconds after a player breaks a block.
     **/
    protected BlockState justBrokenBlock;
    /**
     * How many ticks left until the justBrokenBlock should be cleared.
     **/
    protected int justBrokenClearTime;

    // Action Controls:
    protected byte controlStates = 0;

    public static enum CONTROL_ID {
        JUMP((byte) 1), MOUNT_DISMOUNT((byte) 2), MOUNT_ABILITY((byte) 4), MOUNT_INVENTORY((byte) 8), ATTACK((byte) 16), DESCEND((byte) 32);
        private final byte id;

        CONTROL_ID(byte i) {
            id = i;
        }

        public byte id() {
            return this.id;
        }
    }

    // Big-entity reach modifier (applied transiently when the crosshair lands on an oversized creature).
    private static final ResourceLocation BIG_ENTITY_REACH_UUID = ResourceLocation.fromNamespaceAndPath("lycanitesmobs", "big_entity_reach");
    private static final String BIG_ENTITY_REACH_NAME = "LycanitesBigEntityReach";
    private static final double BIG_ENTITY_SIZE_THRESHOLD = 4.0;
    private static final double BIG_ENTITY_REACH_MAX_SCAN = 4.0;

    // Spirit:
    protected int spiritCharge = 100;
    protected int spiritMax = (this.spiritCharge * 10);
    protected int spirit = this.spiritMax;
    protected int spiritReserved = 0;

    // Summoning:
    protected int selectedSummonSet = 1;
    protected int summonFocusCharge = 600;
    protected int summonFocusMax = (this.summonFocusCharge * 10);
    protected int summonFocusRecharge = 5;
    protected int summonFocus = this.summonFocusMax;
    protected Map<Integer, SummonSet> summonSets = new HashMap<>();
    protected int summonSetMax = 5;
    protected PortalEntity staffPortal;

    // Creature Studying:
    protected int creatureStudyCooldown = 0;
    protected int creatureStudyCooldownMax = 200;

    // Initial Setup:
    private boolean initialSetup = false;

    // ==================================================
    //                   Get for Player
    // ==================================================
    public static ExtendedPlayer getForPlayer(Player player) {
        if (player == null) {
            // LMHelperClass.logWarningMessage("Tried to access an ExtendedPlayer from a null Player.");
            return null;
        }

        // Client Side:
        if (player.getCommandSenderWorld().isClientSide) {
            ExtendedPlayer extendedPlayer = clientExtendedPlayers.computeIfAbsent(player, ignored -> new ExtendedPlayer());
            extendedPlayer.setPlayer(player);
            return extendedPlayer;
        }

        // Server Side:
        ExtendedPlayer iExtendedPlayer = player.getData(com.lycanitesmobs.core.capabilities.LycanitesAttachments.EXTENDED_PLAYER);
        serverExtendedPlayers.put(player, iExtendedPlayer);
        if (iExtendedPlayer.getPlayer() != player)
            iExtendedPlayer.setPlayer(player);
        return iExtendedPlayer;
    }

    public static void unloadClientPlayer(Player player) {
        clientExtendedPlayers.remove(player);
    }


    // ==================================================
    //                    Constructor
    // ==================================================
    public ExtendedPlayer() {
        this.beastiary = new Beastiary(this);
        this.petManager = new PetManager(this.player);
        this.creatureStudyCooldownMax = CreatureManager.getInstance().getConfig().creatureStudyCooldown();
        if (ConfigPlayer.INSTANCE != null) {
            this.summonFocusRecharge = ConfigPlayer.INSTANCE.summoningFocusRecharge.get();
        }
    }


    // ==================================================
    //                    Player Entity
    // ==================================================

    /**
     * Called when the player entity is being cloned, backups all data so that it can be loaded into a new ExtendedPlayer for the clone.
     **/
    public void backupPlayer() {
        if (this.player != null && !this.player.getCommandSenderWorld().isClientSide()) {
            CompoundTag nbtTagCompound = new CompoundTag();
            this.writeNBT(nbtTagCompound);
            backupNBTTags.put(this.player.getUUID(), nbtTagCompound);
        }
    }

    /**
     * Initially sets the player entity and loads any backup data, if the entity is being cloned from another call backupPlayer() instead so that the clone's ExtendedPlayer can load it.
     **/
    public void setPlayer(Player player) {
        this.player = player;
        this.petManager.setHost(player);
        this.player.getCommandSenderWorld();
        if (this.player.getCommandSenderWorld().isClientSide)
            return;
        if (backupNBTTags.containsKey(this.player.getUUID())) {
            this.readNBT(backupNBTTags.get(this.player.getUUID()));
            backupNBTTags.remove(this.player.getUUID());
        }
    }

    public Player getPlayer() {
        return this.player;
    }

    public PetManager getPetManager() {
        return this.petManager;
    }

    public long getTimePlayed() {
        return this.timePlayed;
    }

    public CreatureType getSelectedCreatureType() {
        return this.selectedCreatureType;
    }

    public CreatureInfo getSelectedCreature() {
        return this.selectedCreature;
    }

    public int getSelectedSubspecies() {
        return this.selectedSubspecies;
    }

    public int getSelectedVariant() {
        return this.selectedVariant;
    }

    public int getSelectedPetType() {
        return this.selectedPetType;
    }

    public PetEntry getSelectedPet() {
        return this.selectedPet;
    }

    public boolean hasSelectedCreature() {
        return this.selectedCreature != null;
    }

    public boolean hasSelectedCreatureType() {
        return this.selectedCreatureType != null;
    }

    public boolean hasSelectedPet() {
        return this.selectedPet != null;
    }

    public boolean isSelectedCreature(CreatureInfo creatureInfo) {
        return this.selectedCreature != null && this.selectedCreature.equals(creatureInfo);
    }

    public boolean isSelectedCreatureType(CreatureType creatureType) {
        return this.selectedCreatureType != null && this.selectedCreatureType.equals(creatureType);
    }

    public boolean isSelectedPet(PetEntry petEntry) {
        return this.selectedPet != null && this.selectedPet.equals(petEntry);
    }

    public boolean isSelectedSubspeciesVariant(int subspecies, int variant) {
        return this.selectedSubspecies == subspecies && this.selectedVariant == variant;
    }

    public void selectCreatureType(CreatureType creatureType) {
        this.selectedCreatureType = creatureType;
        this.selectedCreature = null;
    }

    public void selectCreature(CreatureInfo creatureInfo) {
        this.selectedCreature = creatureInfo;
        this.resetSelectedSubspeciesVariant();
    }

    public void selectSubspeciesVariant(int subspecies, int variant) {
        this.selectedSubspecies = subspecies;
        this.selectedVariant = variant;
    }

    public void resetSelectedSubspeciesVariant() {
        this.selectSubspeciesVariant(0, 0);
    }

    public void selectPetType(int petType) {
        this.selectedPetType = petType;
    }

    public void selectPet(PetEntry petEntry) {
        this.selectedPet = petEntry;
    }

    public void clearSelectedPet() {
        this.selectedPet = null;
    }

    /**
     * Scans the player's crosshair for a large creature and applies a transient reach modifier so that
     * vanilla's attack-range check (and any mod that reads {@link NeoForgeMod#ENTITY_REACH}) succeeds on
     * oversized mobs whose center is farther than vanilla's base reach even when the model is touching
     * the player. The modifier is cleared every tick and only re-applied while the crosshair is actually
     * on a large entity, so smaller targets in the area are unaffected. Server-side only; Forge syncs
     * the attribute to the client automatically.
     **/
    public void updateBigEntityReach() {
        if (this.player == null || this.player.level().isClientSide)
            return;
        AttributeInstance reachAttr = this.player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE);
        if (reachAttr == null)
            return;
        reachAttr.removeModifier(BIG_ENTITY_REACH_UUID);
        if (this.player.isSpectator())
            return;

        Vec3 eyePos = this.player.getEyePosition();
        Vec3 lookVec = this.player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(BIG_ENTITY_REACH_MAX_SCAN));
        AABB scanBox = new AABB(eyePos, endPos).inflate(1.0);

        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                this.player.level(),
                this.player,
                eyePos,
                endPos,
                scanBox,
                e -> e instanceof LivingEntity && e.isPickable() && !e.isSpectator() && isLargeEntity(e)
        );
        if (hit == null)
            return;

        double currentReach = reachAttr.getValue();
        double needed = eyePos.distanceTo(hit.getLocation()) + 0.5;
        if (needed <= currentReach)
            return;

        reachAttr.addTransientModifier(new AttributeModifier(
                BIG_ENTITY_REACH_UUID,
                needed - currentReach,
                AttributeModifier.Operation.ADD_VALUE
        ));
    }

    /**
     * An entity is "large" for reach-override purposes when its effective hit box (applying the
     * creature's hit-area scales, if any) exceeds {@link #BIG_ENTITY_SIZE_THRESHOLD} on either axis.
     **/
    private static boolean isLargeEntity(Entity entity) {
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();
        if (entity instanceof BaseCreatureEntity creature) {
            width *= creature.getHitAreaWidthScale();
            height *= creature.getHitAreaHeightScale();
        }
        return width > BIG_ENTITY_SIZE_THRESHOLD || height > BIG_ENTITY_SIZE_THRESHOLD;
    }


    // ==================================================
    //                       Update
    // ==================================================

    /**
     * Called by the EventListener, runs any logic on the main player entity's main update loop.
     **/
    public void onUpdate() {
        if (this.player == null)
            return;
        this.timePlayed++;
        // this.updateBigEntityReach();
        boolean creative = this.player.getAbilities().instabuild;

        // Stats:
        boolean sync = false;
        if (this.justBrokenClearTime > 0) {
            if (--this.justBrokenClearTime <= 0) {
                this.justBrokenBlock = null;
            }
        }

        // Spirit Stat Update:
        this.spirit = Math.min(Math.max(this.spirit, 0), this.spiritMax - this.spiritReserved);
        if (this.spirit < this.spiritMax - this.spiritReserved) {
            this.spirit += 10;
            if (!this.player.getCommandSenderWorld().isClientSide && this.currentTick % 20 == 0 || this.spirit == this.spiritMax - this.spiritReserved) {
                sync = true;
            }
        }

        // Summoning Focus Stat Update:
        this.summonFocus = Math.min(Math.max(this.summonFocus, 0), this.summonFocusMax);
        if (this.summonFocus < this.summonFocusMax) {
            this.summonFocus += this.summonFocusRecharge;
            if (!this.player.getCommandSenderWorld().isClientSide && !creative && this.currentTick % 20 == 0
                    || this.summonFocus < this.summonFocusMax
                    || this.player.getMainHandItem().getItem() instanceof ItemStaffSummoning
                    || this.player.getOffhandItem().getItem() instanceof ItemStaffSummoning) {
                sync = true;
            }
        }

        // Creature Study Cooldown Update:
        if (this.creatureStudyCooldown > 0) {
            this.creatureStudyCooldown--;
            sync = true;
        }

        // Sync Stats To Client:
        if (!this.player.getCommandSenderWorld().isClientSide) {
            if (sync) {
                MessagePlayerStats message = new MessagePlayerStats(this);
                LycanitesMobs.PACKET_MANAGER.sendToPlayer(message, (ServerPlayer) this.player);
            }
        }

        // Initial Setup:
        if (!this.initialSetup) {

            // Pet Manager Setup:
            if (!this.player.getCommandSenderWorld().isClientSide) {
                this.loadFamiliars();
            }

            // Mod Version Check:
            if (this.player.getCommandSenderWorld().isClientSide) {
                VersionChecker versionChecker = VersionChecker.getInstance();
                VersionChecker.VersionInfo latestVersion = versionChecker.getLatestVersion();
                versionChecker.setEnabled(ConfigExtra.INSTANCE.versionCheckerEnabled.get());
                if (latestVersion != null && latestVersion.isNewer && versionChecker.isEnabled()) {
                    String versionText = Component.translatable("lyc.version.newer").getString().replace("{current}", LycanitesMobs.versionNumber).replace("{latest}", latestVersion.versionNumber);
                    this.player.displayClientMessage(Component.literal(versionText), false);
                }
            }

            this.initialSetup = true;
        }

        // Initial Network Sync:
        if (!this.player.getCommandSenderWorld().isClientSide && this.needsFirstSync) {
            this.beastiary.sendAllToClient();
            this.sendAllSummonSetsToPlayer();
            MessageSummonSetSelection message = new MessageSummonSetSelection(this);
            LycanitesMobs.PACKET_MANAGER.sendToPlayer(message, (ServerPlayer) this.player);
            this.sendPetEntriesToPlayer("");
        }

        if (!this.player.getCommandSenderWorld().isClientSide) {
            PlayerFamiliars.INSTANCE.applyPendingFamiliars(this.player);
        }

        // Pet Manager:
        this.petManager.onUpdate(this.player.getCommandSenderWorld());

        this.currentTick++;
        this.needsFirstSync = false;
    }

    /**
     * Sets the block that the player has just broken and resets the clear timer.
     *
     * @param blockState The block state that the player has just broken.
     */
    public void setJustBrokenBlock(BlockState blockState) {
        this.justBrokenBlock = blockState;
        this.justBrokenClearTime = 60;
    }

    public int getSpiritCharge() {
        return this.spiritCharge;
    }

    public int getSpiritMax() {
        return this.spiritMax;
    }

    public int getSpirit() {
        return this.spirit;
    }

    public int getSpiritReserved() {
        return this.spiritReserved;
    }

    public int getSpiritMaxUnits() {
        return this.spiritCharge <= 0 ? 0 : Math.round((float) this.spiritMax / this.spiritCharge);
    }

    public int getSpiritReservedUnits() {
        return this.spiritCharge <= 0 ? 0 : (int) Math.floor((double) this.spiritReserved / this.spiritCharge);
    }

    public int getSpiritAvailableUnits() {
        return this.spiritCharge <= 0 ? 0 : (int) Math.floor((double) this.spirit / this.spiritCharge);
    }

    public float getSpiritFillRatio() {
        if (this.spiritCharge <= 0) {
            return 0;
        }
        return ((float) this.spirit / this.spiritCharge) - this.getSpiritAvailableUnits();
    }

    public boolean canReserveSpirit(int newSpiritReserved) {
        return this.spirit + this.spiritReserved >= newSpiritReserved && newSpiritReserved <= this.spiritMax;
    }

    public void setSpiritReserved(int spiritReserved) {
        this.spiritReserved = spiritReserved;
    }

    public boolean consumeSpirit(int cost) {
        if (this.spirit < cost) {
            return false;
        }
        this.spirit -= cost;
        return true;
    }

    public void addSpiritReserved(int spiritReserved) {
        this.spiritReserved += spiritReserved;
    }

    public int getSummonFocusCharge() {
        return this.summonFocusCharge;
    }

    public int getSummonFocusMax() {
        return this.summonFocusMax;
    }

    public int getSummonFocus() {
        return this.summonFocus;
    }

    public int getSummonFocusMaxUnits() {
        return this.summonFocusCharge <= 0 ? 0 : Math.round((float) this.summonFocusMax / this.summonFocusCharge);
    }

    public int getSummonFocusAvailableUnits() {
        return this.summonFocusCharge <= 0 ? 0 : (int) Math.floor((double) this.summonFocus / this.summonFocusCharge);
    }

    public float getSummonFocusFillRatio() {
        if (this.summonFocusCharge <= 0) {
            return 0;
        }
        return ((float) this.summonFocus / this.summonFocusCharge) - this.getSummonFocusAvailableUnits();
    }

    public boolean hasSummonFocus(int cost) {
        return this.summonFocus >= cost;
    }

    public boolean consumeSummonFocus(int cost) {
        if (!this.hasSummonFocus(cost)) {
            return false;
        }
        this.summonFocus -= cost;
        return true;
    }

    public int getCreatureStudyCooldown() {
        return this.creatureStudyCooldown;
    }

    public boolean hasCreatureStudyCooldown() {
        return this.creatureStudyCooldown > 0;
    }

    public float getCreatureStudyCooldownSeconds() {
        return (float) this.creatureStudyCooldown / 20;
    }

    public int getCreatureStudyCooldownBarWidth() {
        if (this.creatureStudyCooldownMax <= 0) {
            return 0;
        }
        return this.creatureStudyCooldown / this.creatureStudyCooldownMax;
    }

    public void applyNetworkStats(int spirit, int summonFocus, int creatureStudyCooldown) {
        this.spirit = spirit;
        this.summonFocus = summonFocus;
        this.creatureStudyCooldown = creatureStudyCooldown;
    }

    public int getSelectedSummonSetId() {
        return this.selectedSummonSet;
    }

    public int getSummonSetMax() {
        return this.summonSetMax;
    }

    public boolean isSelectedSummonSet(int summonSetId) {
        return this.selectedSummonSet == summonSetId;
    }

    public boolean hasStaffPortal() {
        return this.staffPortal != null;
    }

    public PortalEntity getStaffPortal() {
        return this.staffPortal;
    }

    public void setStaffPortal(PortalEntity staffPortal) {
        this.staffPortal = staffPortal;
    }

    public void clearStaffPortal() {
        this.staffPortal = null;
    }

    public void clearStaffPortalIf(PortalEntity staffPortal) {
        if (this.staffPortal == staffPortal) {
            this.staffPortal = null;
        }
    }

    public boolean isStaffPortal(PortalEntity staffPortal) {
        return this.staffPortal == staffPortal;
    }

    // ==================================================
    //                      Summoning
    // ==================================================
    public SummonSet getSummonSet(int setID) {
        if (setID <= 0) {
            LMHelperClass.logWarningMessage("Attempted to access set " + setID + " but the minimum ID is 1. Player: " + this.player);
            return this.getSummonSet(1);
        } else if (setID > this.summonSetMax) {
            LMHelperClass.logWarningMessage("Attempted to access set " + setID + " but the maximum set ID is " + this.summonSetMax + ". Player: " + this.player);
            return this.getSummonSet(this.summonSetMax);
        }
        if (!this.summonSets.containsKey(setID)) {
            this.summonSets.put(setID, new SummonSet(this));
        }
        return this.summonSets.get(setID);
    }

    public SummonSet getSelectedSummonSet() {
        //if(this.selectedSummonSet != this.validateSummonSetID(this.selectedSummonSet))
        //this.setSelectedSummonSet(this.selectedSummonSet); // This is a fail safe and shouldn't really happen, it will fix the current set ID if it is invalid, resending packets too.
        return this.getSummonSet(this.selectedSummonSet);
    }

    public void setSelectedSummonSet(int targetSetID) {
        //targetSetID = validateSummonSetID(targetSetID);
        this.selectedSummonSet = targetSetID;
    }

    /**
     * Use to make sure that the target summoning set ID is valid, it will return it if it is or the best next set ID if it isn't.
     **/
    public int validateSummonSetID(int targetSetID) {
        targetSetID = Math.max(Math.min(targetSetID, this.summonSetMax), 1);
        while (!this.getSummonSet(targetSetID).isUseable() && targetSetID > 1 && !"".equals(this.getSummonSet(targetSetID).getSummonType())) {
            targetSetID--;
        }
        return targetSetID;
    }


    // ==================================================
    //                    Beastiary
    // ==================================================

    /**
     * Returns the player's beastiary, will also update the client, access the beastiary variable directly when loading NBT data as the network player is null at first.
     **/
    public Beastiary getBeastiary() {
        return this.beastiary;
    }


    public void loadFamiliars() {
        Map<UUID, PetEntry> playerFamiliars = PlayerFamiliars.INSTANCE.getFamiliarsForPlayer(this.player);
        if (playerFamiliars.isEmpty()) {
            return;
        }

        for (PetEntry petEntry : playerFamiliars.values()) {
            if (this.petManager.hasEntry(petEntry)) {
                PetEntry currentFamiliarEntry = this.petManager.getEntry(petEntry.getPetEntryID());
                currentFamiliarEntry.copy(petEntry);
            }
            else {
                this.petManager.addEntry(petEntry);
                petEntry.clearEntityReference();
            }
        }
        this.sendPetEntriesToPlayer("familiar");
    }

    /**
     * Studies the provided entity for the provided amount of knowledge.
     *
     * @param entity            The entity to study, this is checked to see if it is a valid target.
     * @param experience        The amount of knowledge experience to gain.
     * @param useCooldown       If true, the creature study cooldown will be checked and reset on studying.
     * @param alwaysShowMessage If true, a rank up status message is always displayed.
     * @return True if new knowledge was gained for the entity.
     */
    public boolean studyCreature(LivingEntity entity, int experience, boolean useCooldown, boolean alwaysShowMessage) {
        if (!(entity instanceof BaseCreatureEntity)) {
            if (useCooldown && !this.player.getCommandSenderWorld().isClientSide()) {
                this.sendOverlayMessage(Component.translatable("message.beastiary.unknown"));
            }
            return false;
        }

        if (useCooldown && this.creatureStudyCooldown > 0) {
            if (!this.player.getCommandSenderWorld().isClientSide()) {
                this.sendOverlayMessage(Component.translatable("message.beastiary.study.recharging"));
            }
            return false;
        }

        BaseCreatureEntity creature = (BaseCreatureEntity) entity;
        if (creature.isTamed()) {
            return false;
        }
        experience = creature.scaleKnowledgeExperience(experience);
        CreatureKnowledge newKnowledge = this.beastiary.addCreatureKnowledge(creature, experience);
        if (newKnowledge != null) {
            if (useCooldown) {
                this.creatureStudyCooldown = this.creatureStudyCooldownMax;
            }
            if (!this.player.getCommandSenderWorld().isClientSide()) {
                if (newKnowledge.getMaxExperience() == 0) {
                    this.sendOverlayMessage(Component.translatable("message.beastiary.study.full").append(" ").append(creature.getCreatureTitle()));
                } else if (experience > 0) {
                    boolean showMessage = alwaysShowMessage;
                    if (!showMessage) {
                        float messageThreshold = ((float) newKnowledge.getMaxExperience() * 0.25F);
                        int fromExperience = Math.max(0, newKnowledge.getExperience() - experience);
                        int wrappedExperience = fromExperience % (int) messageThreshold;
                        showMessage = wrappedExperience + experience >= messageThreshold;
                    }
                    if (showMessage) {
                        this.sendOverlayMessage(Component.translatable("message.beastiary.study")
                                .append(" ").append(newKnowledge.getCreatureInfo().getTitle())
                                .append(" " + newKnowledge.getExperience() + "/" + newKnowledge.getMaxExperience() + " (+" + experience + ")"));
                    }
                }
            }
            return true;
        }

        if (useCooldown && !this.player.getCommandSenderWorld().isClientSide()) {
            this.sendOverlayMessage(Component.translatable("message.beastiary.study.full").append(" ").append(creature.getCreatureTitle()));
        }
        return false;
    }


    // ==================================================
    //                      Death
    // ==================================================
    public void onDeath() {

    }


    // ==================================================
    //                    Network Sync
    // ==================================================
    public void sendPetEntriesToPlayer(String entryType) {
        if (this.player.getCommandSenderWorld().isClientSide) return;
        for (PetEntry petEntry : this.petManager.getEntries()) {
            if (entryType.equals(petEntry.getType()) || "".equals(entryType)) {
                MessagePetEntry message = new MessagePetEntry(this, petEntry);
                LycanitesMobs.PACKET_MANAGER.sendToPlayer(message, (ServerPlayer) this.player);
            }
        }
    }

    public void sendPetEntryToPlayer(PetEntry petEntry) {
        if (this.player.getCommandSenderWorld().isClientSide) return;
        MessagePetEntry message = new MessagePetEntry(this, petEntry);
        LycanitesMobs.PACKET_MANAGER.sendToPlayer(message, (ServerPlayer) this.player);
    }

    public void sendPetEntryRemoveToPlayer(PetEntry petEntry) {
        if (this.player.getCommandSenderWorld().isClientSide) return;
        MessagePetEntryRemove message = new MessagePetEntryRemove(this, petEntry);
        LycanitesMobs.PACKET_MANAGER.sendToPlayer(message, (ServerPlayer) this.player);
    }

    public void sendPetEntryToServer(PetEntry petEntry) {
        if (!this.player.getCommandSenderWorld().isClientSide) return;
        MessagePetEntry message = new MessagePetEntry(this, petEntry);
        LycanitesMobs.PACKET_MANAGER.sendToServer(message);
    }

    public void sendPetEntryRemoveRequest(PetEntry petEntry) {
        if (!this.player.getCommandSenderWorld().isClientSide) return;
        petEntry.remove();
        MessagePetEntryRemove message = new MessagePetEntryRemove(this, petEntry);
        LycanitesMobs.PACKET_MANAGER.sendToServer(message);
    }

    public void sendAllSummonSetsToPlayer() {
        if (this.player.getCommandSenderWorld().isClientSide) return;
        for (byte setID = 1; setID <= this.summonSetMax; setID++) {
            MessageSummonSet message = new MessageSummonSet(this, setID);
            LycanitesMobs.PACKET_MANAGER.sendToPlayer(message, (ServerPlayer) this.player);
        }
    }

    public void sendSummonSetToServer(byte setID) {
        if (!this.player.getCommandSenderWorld().isClientSide) return;
        MessageSummonSet message = new MessageSummonSet(this, setID);
        LycanitesMobs.PACKET_MANAGER.sendToServer(message);
    }


    // ==================================================
    //                     Controls
    // ==================================================
    public void updateControlStates(byte controlStates) {
        this.controlStates = controlStates;
    }

    public byte getControlStates() {
        return this.controlStates;
    }

    public boolean isControlActive(CONTROL_ID controlID) {
        return (this.controlStates & controlID.id()) > 0;
    }


    // ==================================================
    //                      Utility
    // ==================================================
    public void sendOverlayMessage(MutableComponent messageComponent) {
        if (this.player.getCommandSenderWorld().isClientSide || !(this.player instanceof ServerPlayer)) {
            return;
        }
        MessageOverlayMessage message = new MessageOverlayMessage(messageComponent);
        LycanitesMobs.PACKET_MANAGER.sendToPlayer(message, (ServerPlayer) this.player);
    }


    // ==================================================
    //                        NBT
    // ==================================================
    // ========== Read ===========

    /**
     * Reads a list of Creature Knowledge from a player's NBTTag.
     **/
    public void readNBT(CompoundTag nbtTagCompound) {
        CompoundTag extTagCompound = nbtTagCompound.getCompound("LycanitesMobsPlayer");

        this.beastiary.readFromNBT(extTagCompound);
        this.petManager.readFromNBT(extTagCompound);

        if (extTagCompound.contains("SummonFocus"))
            this.summonFocus = extTagCompound.getInt("SummonFocus");

        if (extTagCompound.contains("Spirit"))
            this.spirit = extTagCompound.getInt("Spirit");

        if (extTagCompound.contains("CreatureStudyCooldown"))
            this.creatureStudyCooldown = extTagCompound.getInt("CreatureStudyCooldown");

        if (extTagCompound.contains("SelectedSummonSet"))
            this.selectedSummonSet = extTagCompound.getInt("SelectedSummonSet");

        if (extTagCompound.contains("SummonSets")) {
            ListTag nbtSummonSets = extTagCompound.getList("SummonSets", 10);
            for (int setID = 0; setID < this.summonSetMax; setID++) {
                CompoundTag nbtSummonSet = (CompoundTag) nbtSummonSets.get(setID);
                SummonSet summonSet = new SummonSet(this);
                summonSet.read(nbtSummonSet);
                this.summonSets.put(setID + 1, summonSet);
            }
        }

        if (extTagCompound.contains("TimePlayed"))
            this.timePlayed = extTagCompound.getLong("TimePlayed");
    }

    // ========== Write ==========

    /**
     * Writes a list of Creature Knowledge to a player's NBTTag.
     **/
    public void writeNBT(CompoundTag nbtTagCompound) {
        CompoundTag extTagCompound = new CompoundTag();

        this.beastiary.writeToNBT(extTagCompound);
        this.petManager.writeToNBT(extTagCompound);

        extTagCompound.putInt("SummonFocus", this.summonFocus);
        extTagCompound.putInt("Spirit", this.spirit);
        extTagCompound.putInt("CreatureStudyCooldown", this.creatureStudyCooldown);
        extTagCompound.putInt("SelectedSummonSet", this.selectedSummonSet);
        extTagCompound.putLong("TimePlayed", this.timePlayed);

        ListTag nbtSummonSets = new ListTag();
        for (int setID = 0; setID < this.summonSetMax; setID++) {
            CompoundTag nbtSummonSet = new CompoundTag();
            SummonSet summonSet = this.getSummonSet(setID + 1);
            summonSet.write(nbtSummonSet);
            nbtSummonSets.add(nbtSummonSet);
        }
        extTagCompound.put("SummonSets", nbtSummonSets);

        nbtTagCompound.put("LycanitesMobsPlayer", extTagCompound);
    }

    /**
     * Re-sends everything the client needs to rebuild its view of this player. Used after a
     * dimension change, where the client player entity is recreated and would otherwise keep
     * an empty beastiary / summon sets / pet list until relog.
     */
    public void sendFullStateToClient() {
        if (!(this.player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        this.beastiary.sendAllToClient();
        this.sendAllSummonSetsToPlayer();
        LycanitesMobs.PACKET_MANAGER.sendToPlayer(new MessageSummonSetSelection(this), serverPlayer);
        this.sendPetEntriesToPlayer("");
        LycanitesMobs.PACKET_MANAGER.sendToPlayer(new MessagePlayerStats(this), serverPlayer);
    }
}
