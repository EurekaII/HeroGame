package io.github.HeroGame.entities.items;

public class Item {
    private String id;
    private String nameKey; // For I18NBundle
    private String descriptionKey; // For I18NBundle
    // Add other properties like type, weight, value, icon, effects etc.

    public Item(String id, String nameKey, String descriptionKey) {
        this.id = id;
        this.nameKey = nameKey;
        this.descriptionKey = descriptionKey;
    }

    public String getId() {
        return id;
    }

    public String getNameKey() {
        return nameKey;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }
    // Add getters and setters
}
