package io.github.HeroGame.entities.equipment;

/**
 * Enum for types of weapons.
 * Affects proficiency and combat calculations.
 */
public enum WeaponType {
    SWORD("weapon_type_sword"),
    AXE("weapon_type_axe"),
    BOW("weapon_type_bow"),
    STAFF("weapon_type_staff"),
    DAGGER("weapon_type_dagger"),
    UNARMED("weapon_type_unarmed"); // For fist fighting or natural weapons

    private final String localizationKey;

    WeaponType(String localizationKey) {
        this.localizationKey = localizationKey;
    }

    public String getLocalizationKey() {
        return localizationKey;
    }
}
