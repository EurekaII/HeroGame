package io.github.HeroGame.entities.stats;

import com.badlogic.gdx.utils.ObjectMap;
import io.github.HeroGame.utils.JsonSerializable;

/**
 * Holds all resistance values for a game unit.
 * Values are typically between 0.0 (no resistance) and 1.0 (full immunity).
 */
public class Resistances implements JsonSerializable {
    private float physical;
    private float fire;
    private float cold;
    private float poison;
    private float magic;
    // Add other resistances as needed

    public Resistances() {
        this.physical = 0.0f;
        this.fire = 0.0f;
        this.cold = 0.0f;
        this.poison = 0.0f;
        this.magic = 0.0f;
    }

    // Getters
    public float getPhysical() { return physical; }
    public float getFire() { return fire; }
    public float getCold() { return cold; }
    public float getPoison() { return poison; }
    public float getMagic() { return magic; }

    // Setters for loading or direct modification
    public void setPhysical(float physical) { this.physical = Math.max(0f, Math.min(1f, physical)); }
    public void setFire(float fire) { this.fire = Math.max(0f, Math.min(1f, fire)); }
    public void setCold(float cold) { this.cold = Math.max(0f, Math.min(1f, cold)); }
    public void setPoison(float poison) { this.poison = Math.max(0f, Math.min(1f, poison)); }
    public void setMagic(float magic) { this.magic = Math.max(0f, Math.min(1f, magic)); }


    /**
     * Gets the resistance value for a specific damage type.
     * @param type The type of damage.
     * @return The resistance value (0.0 to 1.0).
     */
    public float getResistance(DamageType type) {
        switch (type) {
            case PHYSICAL: return physical;
            case FIRE: return fire;
            case COLD: return cold;
            case POISON: return poison;
            case MAGIC: return magic;
            // Add cases for HOLY, DARK etc. if they have specific resistances
            default: return 0.0f; // Default to no resistance for unhandled types
        }
    }

    /**
     * Sets the resistance value for a specific damage type.
     * @param type The type of damage.
     * @param value The resistance value (0.0 to 1.0).
     */
    public void setResistance(DamageType type, float value) {
        float clampedValue = Math.max(0f, Math.min(1f, value));
        switch (type) {
            case PHYSICAL: this.physical = clampedValue; break;
            case FIRE: this.fire = clampedValue; break;
            case COLD: this.cold = clampedValue; break;
            case POISON: this.poison = clampedValue; break;
            case MAGIC: this.magic = clampedValue; break;
            default:
                System.err.println("Cannot set resistance for unhandled damage type: " + type);
                break;
        }
    }


    @Override
    public String toJson() {
        ObjectMap<String, Object> data = new ObjectMap<>();
        data.put("physical", physical);
        data.put("fire", fire);
        data.put("cold", cold);
        data.put("poison", poison);
        data.put("magic", magic);
        return new com.google.gson.Gson().toJson(data);
    }

    @Override
    public void fromJson(String json) {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        Resistances loadedResistances = gson.fromJson(json, Resistances.class);
        if (loadedResistances != null) {
            this.physical = loadedResistances.physical;
            this.fire = loadedResistances.fire;
            this.cold = loadedResistances.cold;
            this.poison = loadedResistances.poison;
            this.magic = loadedResistances.magic;
        } else {
            System.err.println("Failed to deserialize Resistances from JSON: " + json);
        }
    }
}
