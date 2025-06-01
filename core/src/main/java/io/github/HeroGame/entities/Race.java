package io.github.HeroGame.entities;

/**
 * Enum for different races in the game.
 * Each race might have base stat modifiers or unique abilities.
 */
public enum Race {
    HUMAN("race_human"),
    DWARF("race_dwarf"),
    ELF("race_elf"),
    DEMON("race_demon"), // Special interactions with spirituality
    MONSTER("race_monster"); // Generic category for creatures like slimes

    private final String localizationKey;

    Race(String localizationKey) {
        this.localizationKey = localizationKey;
    }

    public String getLocalizationKey() {
        return localizationKey;
    }

    // Potentially add base stat modifiers or abilities here
    // public Stats getBaseStatModifiers() { ... }
}
