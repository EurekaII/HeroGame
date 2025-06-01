package io.github.HeroGame.entities.stats;

/**
 * Enum for different types of damage.
 */
public enum DamageType {
    PHYSICAL("damage_physical"),
    FIRE("damage_fire"),
    COLD("damage_cold"),
    POISON("damage_poison"),
    MAGIC("damage_magic"),
    HOLY("damage_holy"), // Example of spiritual damage type
    DARK("damage_dark"); // Example of spiritual damage type


    private final String localizationKey;

    DamageType(String localizationKey) {
        this.localizationKey = localizationKey;
    }

    public String getLocalizationKey() {
        return localizationKey;
    }
}
