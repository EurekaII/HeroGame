package io.github.HeroGame.events;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import java.util.function.Consumer;

/**
 * Prosty system zarządzania zdarzeniami oparty na mechanizmie publikuj/subskrybuj.
 * Umożliwia luźne powiązanie komponentów gry.
 */
public class EventManager {
    private static EventManager instance;
    private final ObjectMap<Class<?>, Array<Consumer<?>>> listeners;

    private EventManager() {
        listeners = new ObjectMap<>();
    }

    /**
     * Zwraca instancję singletona EventManager.
     * @return Instancja EventManager.
     */
    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    /**
     * Subskrybuje określony typ zdarzenia.
     * Gdy zdarzenie tego typu zostanie opublikowane, podany listener zostanie wywołany.
     *
     * @param eventType Klasa zdarzenia do subskrypcji.
     * @param listener Consumer, który zostanie wykonany po opublikowaniu zdarzenia.
     * @param <T> Typ zdarzenia.
     */
    public <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        // Sprawdź, czy klucz (eventType) już istnieje. Jeśli nie, dodaj nową Array.
        if (!listeners.containsKey(eventType)) {
            listeners.put(eventType, new Array<>());
        }

        @SuppressWarnings("unchecked") // Bezpieczne, ponieważ sprawdzamy typ przy publikacji
        Array<Consumer<?>> eventListeners = listeners.get(eventType);

        // Dodaj listenera tylko jeśli jeszcze nie istnieje na liście
        if (!eventListeners.contains((Consumer<?>) listener, true)) { // true dla identity comparison
            eventListeners.add(listener);
        }
    }

    /**
     * Odsubskrybowuje listener od określonego typu zdarzenia.
     *
     * @param eventType Klasa zdarzenia.
     * @param listener Consumer do usunięcia.
     * @param <T> Typ zdarzenia.
     */
    public <T> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        if (listeners.containsKey(eventType)) {
            @SuppressWarnings("unchecked")
            Array<Consumer<?>> eventListeners = listeners.get(eventType);
            eventListeners.removeValue((Consumer<?>) listener, true);
            if (eventListeners.isEmpty()) {
                listeners.remove(eventType);
            }
        }
    }

    /**
     * Publikuje zdarzenie do wszystkich subskrybentów.
     *
     * @param event Obiekt zdarzenia do opublikowania.
     */
    @SuppressWarnings("unchecked")
    public void publish(Object event) {
        if (event == null) {
            System.err.println("Attempted to publish a null event.");
            return;
        }
        Class<?> eventType = event.getClass();
        if (listeners.containsKey(eventType)) {
            // Tworzymy kopię, aby uniknąć ConcurrentModificationException, jeśli listener modyfikuje listę
            Array<Consumer<?>> consumers = new Array<>(listeners.get(eventType));
            for (Consumer<?> consumer : consumers) {
                try {
                    // Bezpieczne rzutowanie, ponieważ subskrypcja gwarantuje zgodność typów
                    ((Consumer<Object>) consumer).accept(event);
                } catch (Exception e) {
                    System.err.println("Error executing event listener for " + eventType.getSimpleName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Czyści wszystkich listenerów. Przydatne np. przy zamykaniu gry.
     */
    public void clearAllListeners() {
        listeners.clear();
    }
}
