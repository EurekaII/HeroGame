package io.github.HeroGame.events;

import io.github.HeroGame.entities.unit.Unit;

/**
 * Zdarzenie publikowane, gdy zmienia się poziom duchowości jednostki.
 */
public class SpiritualityChangedEvent {
    private final Unit unit;
    private final int oldSpirituality;
    private final int newSpirituality;
    private final int changeAmount;

    /**
     * Konstruktor zdarzenia zmiany duchowości.
     * @param unit Jednostka, której duchowość się zmieniła.
     * @param oldSpirituality Poprzednia wartość duchowości.
     * @param newSpirituality Nowa wartość duchowości.
     * @param changeAmount Ilość, o którą zmieniła się duchowość (dodatnia lub ujemna).
     */
    public SpiritualityChangedEvent(Unit unit, int oldSpirituality, int newSpirituality, int changeAmount) {
        this.unit = unit;
        this.oldSpirituality = oldSpirituality;
        this.newSpirituality = newSpirituality;
        this.changeAmount = changeAmount;
    }

    public Unit getUnit() {
        return unit;
    }

    public int getOldSpirituality() {
        return oldSpirituality;
    }

    public int getNewSpirituality() {
        return newSpirituality;
    }

    public int getChangeAmount() {
        return changeAmount;
    }
}
