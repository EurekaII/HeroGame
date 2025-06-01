package io.github.HeroGame.entities.stats;

/**
 * Enum for various statistic types.
 * Klucze dla statystyk: stat_strength, stat_dexterity, etc. (w plikach lokalizacyjnych)
 */
public enum StatType {
    STRENGTH("stat_strength"),
    DEXTERITY("stat_dexterity"),
    INTELLIGENCE("stat_intelligence"),
    ENDURANCE("stat_endurance"),
    WISDOM("stat_wisdom"),
    LUCK("stat_luck"),
    SPIRITUALITY("stat_spirituality");

    private final String localizationKey;

    StatType(String localizationKey) {
        this.localizationKey = localizationKey;
    }

    public String getLocalizationKey() {
        return localizationKey;
    }
}
