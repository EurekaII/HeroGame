package io.github.HeroGame.fsm;


import io.github.HeroGame.entities.unit.Unit;

/**
 * Interfejs reprezentujący pojedynczy stan w maszynie stanów.
 * Każdy stan definiuje zachowanie jednostki (Unit), gdy się w nim znajduje.
 *
 * @param <T> Typ jednostki, dla której ten stan jest przeznaczony (musi dziedziczyć po Unit).
 */
public interface State<T extends Unit> {

    /**
     * Wywoływana, gdy jednostka wchodzi w ten stan.
     * Służy do inicjalizacji zasobów lub logiki specyficznej dla stanu.
     *
     * @param entity Jednostka, która wchodzi w ten stan.
     */
    void enter(T entity);

    /**
     * Wywoływana w każdej klatce (lub w regularnych odstępach czasu), gdy jednostka jest w tym stanie.
     * Implementuje główną logikę zachowania stanu.
     *
     * @param entity Jednostka, która jest w tym stanie.
     * @param deltaTime Czas, który upłynął od ostatniej aktualizacji (w sekundach).
     */
    void update(T entity, float deltaTime);

    /**
     * Wywoływana, gdy jednostka opuszcza ten stan.
     * Służy do zwolnienia zasobów lub wykonania logiki czyszczącej.
     *
     * @param entity Jednostka, która opuszcza ten stan.
     */
    void exit(T entity);
}
