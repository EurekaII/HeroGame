package io.github.HeroGame.entities.stats;

import com.badlogic.gdx.utils.ObjectMap;
import io.github.HeroGame.utils.JsonSerializable; // Assuming you might want this for GSON

/**
 * Holds all statistics for a game unit.
 */
public class Stats implements JsonSerializable {
    private int strength;
    private int dexterity;
    private int intelligence;
    private int endurance;
    private int wisdom;
    private int luck;
    private int spirituality; // 0-100

    public Stats() {
        // Default stats or load from configuration
        this.strength = 5;
        this.dexterity = 5;
        this.intelligence = 5;
        this.endurance = 5;
        this.wisdom = 5;
        this.luck = 5;
        this.spirituality = 5; // Base spirituality
    }

    // Getters
    public int getStrength() { return strength; }
    public int getDexterity() { return dexterity; }
    public int getIntelligence() { return intelligence; }
    public int getEndurance() { return endurance; }
    public int getWisdom() { return wisdom; }
    public int getLuck() { return luck; }
    public int getSpirituality() { return spirituality; }

    // Setters - primarily for loading, direct modification via addPoints
    public void setStrength(int strength) { this.strength = strength; }
    public void setDexterity(int dexterity) { this.dexterity = dexterity; }
    public void setIntelligence(int intelligence) { this.intelligence = intelligence; }
    public void setEndurance(int endurance) { this.endurance = endurance; }
    public void setWisdom(int wisdom) { this.wisdom = wisdom; }
    public void setLuck(int luck) { this.luck = luck; }
    public void setSpirituality(int spirituality) { this.spirituality = Math.max(0, Math.min(100, spirituality)); }


    /**
     * Gets the value of a specific stat.
     * @param type The type of stat to get.
     * @return The value of the stat.
     */
    public int getStat(StatType type) {
        switch (type) {
            case STRENGTH: return strength;
            case DEXTERITY: return dexterity;
            case INTELLIGENCE: return intelligence;
            case ENDURANCE: return endurance;
            case WISDOM: return wisdom;
            case LUCK: return luck;
            case SPIRITUALITY: return spirituality;
            default: throw new IllegalArgumentException("Unknown StatType: " + type);
        }
    }

    /**
     * Adds points to a specific stat.
     * For spirituality, use gainSpirituality/loseSpirituality on Unit.
     * @param type The type of stat to modify.
     * @param points The number of points to add (can be negative).
     */
    public void addPoints(StatType type, int points) {
        if (type == StatType.SPIRITUALITY) {
            System.err.println("Spirituality should be changed via Unit.gainSpirituality/loseSpirituality methods.");
            return; // Or throw an exception
        }
        switch (type) {
            case STRENGTH: strength += points; break;
            case DEXTERITY: dexterity += points; break;
            case INTELLIGENCE: intelligence += points; break;
            case ENDURANCE: endurance += points; break;
            case WISDOM: wisdom += points; break;
            case LUCK: luck += points; break;
        }
        // Add min/max caps if necessary, e.g., stats cannot go below 1.
    }

    // For GSON serialization, if needed
    @Override
    public String toJson() {
        // Basic GSON usage would serialize this object directly.
        // This method is a placeholder if custom serialization logic is needed.
        ObjectMap<String, Object> data = new ObjectMap<>();
        data.put("strength", strength);
        data.put("dexterity", dexterity);
        data.put("intelligence", intelligence);
        data.put("endurance", endurance);
        data.put("wisdom", wisdom);
        data.put("luck", luck);
        data.put("spirituality", spirituality);
        // Using a proper JSON library like GSON is recommended over manual construction.
        return new com.google.gson.Gson().toJson(data);
    }

    @Override
    public void fromJson(String json) {
        // Basic GSON usage would deserialize this object directly.
        com.google.gson.Gson gson = new com.google.gson.Gson();
        Stats loadedStats = gson.fromJson(json, Stats.class);
        if (loadedStats != null) { // Check if deserialization was successful
            this.strength = loadedStats.strength;
            this.dexterity = loadedStats.dexterity;
            this.intelligence = loadedStats.intelligence;
            this.endurance = loadedStats.endurance;
            this.wisdom = loadedStats.wisdom;
            this.luck = loadedStats.luck;
            this.spirituality = loadedStats.spirituality;
        } else {
            System.err.println("Failed to deserialize Stats from JSON: " + json);
            // Initialize with default values or handle error appropriately
        }
    }
}
