package com.lycanitesmobs.core.data.info.creature;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.nbt.CompoundTag;

public class ExtraMobBehaviour {
	// ========== Mob ==========
	/** The INSTANCE of the mob this extra behaviour belongs to. **/
	protected BaseCreatureEntity host;
	
	// ========== Stats ==========
    protected double multiplierHealth = 1.0D;
	protected double multiplierDefense = 1.0D;
	protected double multiplierArmor = 1.0D;
	protected double multiplierSpeed = 1.0D;
	protected double multiplierDamage = 1.0D;
	protected double multiplierHaste = 1.0D;
	protected double multiplierEffect = 1.0D;
	protected double multiplierPierce = 1.0D;

    protected int boostHealth = 0;
	protected int boostDefense = 0;
	protected int boostArmor = 0;
	protected int boostSpeed = 0;
	protected int boostDamage = 0;
	protected int boostHaste = 0;
	protected int boostEffect = 0;
	protected int boostPierce = 0;
	
	// ========== Overrides ==========
	protected boolean aggressiveOverride = false;
	protected boolean flightOverride = false;
	protected boolean swimmingOverride = false;
	protected boolean waterBreathingOverride = false;
	protected boolean fireImmunityOverride = false;
	protected boolean stealthOverride = false;
	protected boolean itemPickupOverride = false;
	protected int inventorySizeOverride = 0;
	protected double itemDropMultiplierOverride = 1;
	
	// ========== AI ==========
	protected boolean aiAttackPlayers = false;
	protected boolean aiDefendAnimals = false;
	
	
    // ==================================================
    //                     Constructor
    // ==================================================
	public ExtraMobBehaviour(BaseCreatureEntity host) {
		this.host = host;
	}
	
	
	public BaseCreatureEntity host() {
		return this.host;
	}

	public double multiplierHealth() {
		return this.multiplierHealth;
	}

	public double multiplierDefense() {
		return this.multiplierDefense;
	}

	public double multiplierArmor() {
		return this.multiplierArmor;
	}

	public double multiplierSpeed() {
		return this.multiplierSpeed;
	}

	public double multiplierDamage() {
		return this.multiplierDamage;
	}

	public double multiplierHaste() {
		return this.multiplierHaste;
	}

	public double multiplierEffect() {
		return this.multiplierEffect;
	}

	public double multiplierPierce() {
		return this.multiplierPierce;
	}

	public int boostHealth() {
		return this.boostHealth;
	}

	public int boostDefense() {
		return this.boostDefense;
	}

	public int boostArmor() {
		return this.boostArmor;
	}

	public int boostSpeed() {
		return this.boostSpeed;
	}

	public int boostDamage() {
		return this.boostDamage;
	}

	public int boostHaste() {
		return this.boostHaste;
	}

	public int boostEffect() {
		return this.boostEffect;
	}

	public int boostPierce() {
		return this.boostPierce;
	}

	public boolean aggressiveOverride() {
		return this.aggressiveOverride;
	}

	public boolean flightOverride() {
		return this.flightOverride;
	}

	public boolean swimmingOverride() {
		return this.swimmingOverride;
	}

	public boolean waterBreathingOverride() {
		return this.waterBreathingOverride;
	}

	public boolean fireImmunityOverride() {
		return this.fireImmunityOverride;
	}

	public boolean stealthOverride() {
		return this.stealthOverride;
	}

	public boolean itemPickupOverride() {
		return this.itemPickupOverride;
	}

	public int inventorySizeOverride() {
		return this.inventorySizeOverride;
	}

	public double itemDropMultiplierOverride() {
		return this.itemDropMultiplierOverride;
	}

	public boolean aiAttackPlayers() {
		return this.aiAttackPlayers;
	}

	public boolean aiDefendAnimals() {
		return this.aiDefendAnimals;
	}

	// ==================================================
    //                        NBT
    // ==================================================
   	// ========== Read ===========
    /** Called from this host passing a compound storing all the extra behaviour options. **/
    public void read(CompoundTag nbtTagCompound) {
    	// Stat Multipliers:
        if(nbtTagCompound.contains("MultiplierHealth")) {
            this.multiplierHealth = nbtTagCompound.getDouble("MultiplierHealth");
        }
    	if(nbtTagCompound.contains("MultiplierDefense")) {
    		this.multiplierDefense = nbtTagCompound.getDouble("MultiplierDefense");
    	}
		if(nbtTagCompound.contains("MultiplierArmor")) {
			this.multiplierArmor = nbtTagCompound.getDouble("MultiplierArmor");
		}
    	if(nbtTagCompound.contains("MultiplierSpeed")) {
    		this.multiplierSpeed = nbtTagCompound.getDouble("MultiplierSpeed");
    	}
    	if(nbtTagCompound.contains("MultiplierDamage")) {
    		this.multiplierDamage = nbtTagCompound.getDouble("MultiplierDamage");
    	}
    	if(nbtTagCompound.contains("MultiplierHaste")) {
    		this.multiplierHaste = nbtTagCompound.getDouble("MultiplierHaste");
    	}
    	if(nbtTagCompound.contains("MultiplierEffect")) {
    		this.multiplierEffect = nbtTagCompound.getDouble("MultiplierEffect");
    	}
    	if(nbtTagCompound.contains("MultiplierPierce")) {
    		this.multiplierEffect = nbtTagCompound.getDouble("MultiplierPierce");
    	}

    	// Stat Boosts:
        if(nbtTagCompound.contains("BoostHealth")) {
            this.boostHealth = nbtTagCompound.getInt("BoostHealth");
        }
    	if(nbtTagCompound.contains("BoostDefense")) {
    		this.boostDefense = nbtTagCompound.getInt("BoostDefense");
    	}
		if(nbtTagCompound.contains("BoostArmor")) {
			this.boostArmor = nbtTagCompound.getInt("BoostArmor");
		}
    	if(nbtTagCompound.contains("BoostSpeed")) {
    		this.boostSpeed = nbtTagCompound.getInt("BoostSpeed");
    	}
    	if(nbtTagCompound.contains("BoostDamage")) {
    		this.boostDamage = nbtTagCompound.getInt("BoostDamage");
    	}
    	if(nbtTagCompound.contains("BoostHaste")) {
    		this.boostHaste = nbtTagCompound.getInt("BoostHaste");
    	}
    	if(nbtTagCompound.contains("BoostEffect")) {
    		this.boostEffect = nbtTagCompound.getInt("BoostEffect");
    	}
    	if(nbtTagCompound.contains("BoostPierce")) {
    		this.boostEffect = nbtTagCompound.getInt("BoostPierce");
    	}

    	// Overrides:
    	if(nbtTagCompound.contains("AggressiveOverride")) {
    		this.aggressiveOverride = nbtTagCompound.getBoolean("AggressiveOverride");
    	}
    	if(nbtTagCompound.contains("FlightOverride")) {
    		this.flightOverride = nbtTagCompound.getBoolean("FlightOverride");
    	}
    	if(nbtTagCompound.contains("SwimmingOverride")) {
    		this.swimmingOverride = nbtTagCompound.getBoolean("SwimmingOverride");
    	}
    	if(nbtTagCompound.contains("WaterBreathingOverride")) {
    		this.waterBreathingOverride = nbtTagCompound.getBoolean("WaterBreathingOverride");
    	}
    	if(nbtTagCompound.contains("FireImmunityOverride")) {
    		this.fireImmunityOverride = nbtTagCompound.getBoolean("FireImmunityOverride");
    	}
    	if(nbtTagCompound.contains("StealthOverride")) {
    		this.stealthOverride = nbtTagCompound.getBoolean("StealthOverride");
    	}
    	if(nbtTagCompound.contains("ItemPickupOverride")) {
    		this.itemPickupOverride = nbtTagCompound.getBoolean("ItemPickupOverride");
    	}
    	if(nbtTagCompound.contains("InventorySizeOverride")) {
    		this.inventorySizeOverride = nbtTagCompound.getInt("InventorySizeOverride");
    	}
    	if(nbtTagCompound.contains("ItemDropMultiplierOverride")) {
    		this.itemDropMultiplierOverride = nbtTagCompound.getDouble("ItemDropMultiplierOverride");
    	}
    	
    	// AI:
    	if(nbtTagCompound.contains("AIAttackPlayers")) {
    		this.aiAttackPlayers = nbtTagCompound.getBoolean("AIAttackPlayers");
    	}
    	if(nbtTagCompound.contains("AIDefendAnimals")) {
    		this.aiDefendAnimals = nbtTagCompound.getBoolean("AIDefendAnimals");
    	}
		this.host.configureExtraBehaviourGoals(this.aiAttackPlayers, this.aiDefendAnimals);
    }
    
    // ========== Write ==========
    /** Called from this host passing a compound writing all the extra behaviour options. **/
    public void write(CompoundTag nbtTagCompound) {
    	// Stat Multipliers:
        nbtTagCompound.putDouble("MultiplierHealth", this.multiplierHealth);
    	nbtTagCompound.putDouble("MultiplierDefense", this.multiplierDefense);
		nbtTagCompound.putDouble("MultiplierArmor", this.multiplierArmor);
    	nbtTagCompound.putDouble("MultiplierSpeed", this.multiplierSpeed);
    	nbtTagCompound.putDouble("MultiplierDamage", this.multiplierDamage);
    	nbtTagCompound.putDouble("MultiplierHaste", this.multiplierHaste);
    	nbtTagCompound.putDouble("MultiplierEffect", this.multiplierEffect);
    	nbtTagCompound.putDouble("MultiplierPierce", this.multiplierPierce);

    	// Stat Boosts:
        nbtTagCompound.putInt("BoostHealth", this.boostHealth);
    	nbtTagCompound.putInt("BoostDefense", this.boostDefense);
		nbtTagCompound.putInt("BoostArmor", this.boostArmor);
    	nbtTagCompound.putInt("BoostSpeed", this.boostSpeed);
    	nbtTagCompound.putInt("BoostDamage", this.boostDamage);
    	nbtTagCompound.putInt("BoostHaste", this.boostHaste);
    	nbtTagCompound.putInt("BoostEffect", this.boostEffect);
    	nbtTagCompound.putInt("BoostPierce", this.boostPierce);

    	// Overrides:
    	nbtTagCompound.putBoolean("AggressiveOverride", this.aggressiveOverride);
    	nbtTagCompound.putBoolean("FlightOverride", this.flightOverride);
    	nbtTagCompound.putBoolean("SwimmingOverride", this.swimmingOverride);
    	nbtTagCompound.putBoolean("WaterBreathingOverride", this.waterBreathingOverride);
    	nbtTagCompound.putBoolean("FireImmunityOverride", this.fireImmunityOverride);
    	nbtTagCompound.putBoolean("StealthOverride", this.stealthOverride);
    	nbtTagCompound.putBoolean("ItemPickupOverride", this.itemPickupOverride);
    	nbtTagCompound.putInt("InventorySizeOverride", this.inventorySizeOverride);
    	nbtTagCompound.putDouble("ItemDropMultiplierOverride", this.itemDropMultiplierOverride);
    	
    	// AI:
    	nbtTagCompound.putBoolean("AIAttackPlayers", this.aiAttackPlayers);
    	nbtTagCompound.putBoolean("AIDefendAnimals", this.aiDefendAnimals);
    }
}
