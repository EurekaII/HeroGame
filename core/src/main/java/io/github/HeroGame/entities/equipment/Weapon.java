package io.github.HeroGame.entities.equipment;

import io.github.HeroGame.entities.items.Item;
import io.github.HeroGame.entities.stats.DamageType;

/**
 * Represents a weapon item.
 */
public class Weapon extends Item {
    private WeaponType weaponType;
    private int baseDamage;
    private DamageType damageType;
    // Add other weapon-specific properties: range, attack speed, critical chance/multiplier, special effects

    public Weapon(String id, String nameKey, String descriptionKey, WeaponType weaponType, int baseDamage, DamageType damageType) {
        super(id, nameKey, descriptionKey);
        this.weaponType = weaponType;
        this.baseDamage = baseDamage;
        this.damageType = damageType;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public DamageType getDamageType() {
        return damageType;
    }
}
