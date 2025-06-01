package io.github.HeroGame.fsm;

import com.badlogic.gdx.utils.ObjectMap;
import io.github.HeroGame.entities.unit.Unit; // Założenie, że klasa Unit będzie w tym pakiecie

/**
 * Zarządza stanami dla określonej jednostki (Unit).
 * Pozwala na dodawanie, zmienianie i aktualizowanie stanów.
 *
 * @param <T> Typ jednostki, dla której ta maszyna stanów jest przeznaczona (musi dziedziczyć po Unit).
 */
public class StateMachine<T extends Unit> {

    private T owner; // Jednostka, do której należy ta maszyna stanów
    private State<? extends T> currentState; // Użycie wildcard dla większej elastyczności
    private State<? extends T> previousState; // Użycie wildcard
    private ObjectMap<String, State<? extends T>> states; // Mapa przechowująca stany, użycie wildcard

    /**
     * Konstruktor maszyny stanów.
     * @param owner Jednostka, która będzie zarządzana przez tę maszynę stanów.
     */
    public StateMachine(T owner) {
        this.owner = owner;
        this.currentState = null;
        this.previousState = null;
        this.states = new ObjectMap<>();
    }

    /**
     * Dodaje nowy stan do maszyny stanów.
     * @param name Nazwa stanu (unikalny identyfikator).
     * @param state Obiekt stanu do dodania. Akceptuje State<? extends T>.
     */
    public void addState(String name, State<? extends T> state) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa stanu nie może być pusta.");
        }
        if (state == null) {
            throw new IllegalArgumentException("Stan nie może być null.");
        }
        states.put(name, state);
    }

    /**
     * Sprawdza, czy stan o podanej nazwie jest zarejestrowany w maszynie stanów.
     * @param name Nazwa stanu do sprawdzenia.
     * @return true, jeśli stan istnieje, false w przeciwnym razie.
     */
    public boolean hasState(String name) {
        return states.containsKey(name);
    }

    /**
     * Zmienia aktualny stan maszyny.
     * Jeśli nowy stan istnieje, poprzedni stan jest opuszczany (metoda exit()),
     * a nowy stan jest aktywowany (metoda enter()).
     *
     * @param name Nazwa nowego stanu do aktywacji.
     */
    @SuppressWarnings("unchecked") // Bezpieczne, ponieważ T jest właścicielem, a stany są State<? extends T>
    public void changeState(String name) {
        if (!hasState(name)) { // Użycie nowej metody hasState
            System.err.println("Próba zmiany na nieistniejący stan: " + name + " dla " + owner);
            return;
        }

        State<? extends T> newState = states.get(name);

        if (currentState == newState) {
            return;
        }

        if (currentState != null) {
            // Rzutowanie jest tutaj konieczne i powinno być bezpieczne,
            // ponieważ stan zawsze operuje na typie T (lub jego podtypie),
            // a T jest typem ownera.
            ((State<T>) currentState).exit(owner);
        }

        previousState = currentState;
        currentState = newState;

        if (currentState != null) {
            ((State<T>) currentState).enter(owner);
        } else {
            System.err.println("Nowy stan '" + name + "' jest nullem po pobraniu z mapy, mimo że klucz istnieje. To nie powinno się zdarzyć.");
        }
    }

    /**
     * Aktualizuje logikę bieżącego stanu.
     * Wywołuje metodę update() na aktualnym stanie.
     *
     * @param deltaTime Czas, który upłynął od ostatniej aktualizacji.
     */
    @SuppressWarnings("unchecked") // Podobnie jak w changeState, rzutowanie jest tu bezpieczne.
    public void update(float deltaTime) {
        if (currentState != null) {
            ((State<T>) currentState).update(owner, deltaTime);
        }
    }

    /**
     * Zwraca aktualnie aktywny stan.
     * @return Aktualny stan lub null, jeśli żaden stan nie jest aktywny.
     */
    public State<? extends T> getCurrentState() {
        return currentState;
    }

    /**
     * Zwraca poprzednio aktywny stan.
     * @return Poprzedni stan lub null, jeśli nie było poprzedniego stanu.
     */
    public State<? extends T> getPreviousState() {
        return previousState;
    }

    /**
     * Zwraca właściciela tej maszyny stanów.
     * @return Jednostka (Unit), do której należy ta maszyna FSM.
     */
    public T getOwner() {
        return owner;
    }

    /**
     * Sprawdza, czy bieżący stan jest określonego typu.
     * @param stateClass Klasa stanu do sprawdzenia.
     * @return True, jeśli bieżący stan jest instancją danej klasy, w przeciwnym razie false.
     */
    public boolean isInState(Class<? extends State<? extends T>> stateClass) {
        return currentState != null && stateClass.isInstance(currentState);
    }
}
