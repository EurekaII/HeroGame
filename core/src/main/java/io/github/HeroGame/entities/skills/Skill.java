package io.github.HeroGame.entities.skills;

import io.github.HeroGame.entities.unit.Unit;

public class Skill {
    private String id;
    private String nameKey;
    private String descriptionKey;
    private int manaCost;
    // Add other properties: cooldown, target type, effects, damage, range, etc.

    public Skill(String id, String nameKey, String descriptionKey, int manaCost) {
        this.id = id;
        this.nameKey = nameKey;
        this.descriptionKey = descriptionKey;
        this.manaCost = manaCost;
    }

    public String getId() { return id; }
    public String getNameKey() { return nameKey; }
    public String getDescriptionKey() { return descriptionKey; }
    public int getManaCost() { return manaCost; }

    public boolean canUse(Unit user) {
        return user.getMana() >= manaCost;
    }

    public void execute(Unit user, Unit target) {
        // Placeholder for skill execution logic
        // This would involve applying effects, damage, etc.
        System.out.println(user.getName() + " uses " + nameKey + (target != null ? " on " + target.getName() : ""));
        user.setMana(user.getMana() - manaCost);
    }
}
