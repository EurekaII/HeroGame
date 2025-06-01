package io.github.HeroGame.entities.unit;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import io.github.HeroGame.entities.Race;
import io.github.HeroGame.entities.equipment.Weapon;
import io.github.HeroGame.entities.equipment.WeaponType;
import io.github.HeroGame.entities.skills.Skill;
import io.github.HeroGame.entities.stats.DamageType;
import io.github.HeroGame.entities.stats.Resistances;
import io.github.HeroGame.entities.stats.StatType;
import io.github.HeroGame.entities.stats.Stats;
import io.github.HeroGame.events.EventManager;
import io.github.HeroGame.events.SpiritualityChangedEvent;
import io.github.HeroGame.fsm.StateMachine;
import io.github.HeroGame.utils.JsonSerializable; // Placeholder

/**
 * Abstrakcyjna klasa bazowa dla wszystkich jednostek w grze (Gracz, NPC, Przeciwnicy).
 * Definiuje wspólne atrybuty i zachowania.
 */
public abstract class Unit implements JsonSerializable {
    protected String id; // Unikalny identyfikator jednostki
    protected String name; // Nazwa jednostki (może być kluczem lokalizacyjnym)
    protected Vector2 position;
    protected Vector2 velocity;
    protected int health;
    protected int maxHealth;
    protected int mana;
    protected int maxMana;
    protected Stats stats;
    protected Race race;
    protected StateMachine<Unit> stateMachine;
    protected Resistances resistances;
    protected int defense; // Podstawowa obrona, redukuje obrażenia fizyczne
    protected float blockChance; // Szansa na blok (0-1), aktywna z tarczą
    protected int gold;
    protected int experience; // Tylko dla gracza, ale może być tu dla uproszczenia
    protected int level;
    protected ObjectMap<WeaponType, Float> weaponProficiencies; // Biegłość w typach broni (0.0f - 100.0f)
    protected Weapon equippedWeapon; // Aktualnie wyposażona broń
    // protected Array<Item> equippedItems; // Ekwipunek (pancerz, akcesoria) - do dodania później

    protected TextureRegion currentFrame; // Aktualna klatka animacji/sprite jednostki

    protected static final float MAX_PROFICIENCY = 100.0f;
    protected static final float MEDITATION_RATE = 1.0f; // Punktów duchowości na sekundę medytacji
    protected static final float OVERLOAD_STAT_GAIN_RATE = 0.01f; // Punktów siły/wytrzymałości na sekundę przeciążenia
    protected static final float CRAFT_STAT_GAIN_AMOUNT = 0.05f; // Punktów zręczności/inteligencji za akcję craftu


    /**
     * Konstruktor jednostki.
     * @param id Unikalny ID.
     * @param name Nazwa jednostki.
     * @param race Rasa jednostki.
     * @param initialPosition Pozycja startowa.
     */
    public Unit(String id, String name, Race race, Vector2 initialPosition) {
        this.id = id;
        this.name = name;
        this.race = race;
        this.position = new Vector2(initialPosition);
        this.velocity = new Vector2();
        this.stats = new Stats(); // Inicjalizacja domyślnymi statystykami
        this.resistances = new Resistances();
        this.stateMachine = new StateMachine<>(this);
        this.weaponProficiencies = new ObjectMap<>();
        for (WeaponType type : WeaponType.values()) {
            weaponProficiencies.put(type, 0.0f); // Inicjalizacja wszystkich biegłości na 0
        }

        // Domyślne wartości, mogą być nadpisane przez podklasy lub dane z zapisu
        this.level = 1;
        this.maxHealth = calculateMaxHealth();
        this.health = this.maxHealth;
        this.maxMana = calculateMaxMana();
        this.mana = this.maxMana;
        this.defense = 0;
        this.blockChance = 0.0f;
        this.gold = 0;
        this.experience = 0;
        this.equippedWeapon = null; // Domyślnie brak broni

        // TODO: Załadować teksturę/animację na podstawie rasy/typu jednostki
        // this.currentFrame = ... ;
    }

    /**
     * Oblicza maksymalne zdrowie na podstawie statystyk (np. wytrzymałości).
     * @return Maksymalne zdrowie.
     */
    protected int calculateMaxHealth() {
        // Przykładowa formuła: 100 + Endurance * 10 + Level * 5
        return 50 + stats.getStat(StatType.ENDURANCE) * 10 + level * 5;
    }

    /**
     * Oblicza maksymalną manę na podstawie statystyk (np. inteligencji/mądrości).
     * @return Maksymalna mana.
     */
    protected int calculateMaxMana() {
        // Przykładowa formuła: 50 + Intelligence * 5 + Wisdom * 5 + Level * 3
        return 30 + stats.getStat(StatType.INTELLIGENCE) * 5 + stats.getStat(StatType.WISDOM) * 5 + level * 3;
    }

    /**
     * Metoda aktualizująca logikę jednostki.
     * Powinna być wywoływana w każdej klatce.
     * @param deltaTime Czas od ostatniej klatki.
     */
    public void update(float deltaTime) {
        // Aktualizacja maszyny stanów
        stateMachine.update(deltaTime);

        // Aktualizacja pozycji na podstawie prędkości
        position.add(velocity.x * deltaTime, velocity.y * deltaTime);

        // Tutaj można dodać logikę regeneracji zdrowia/many, efektów statusu itp.
        // np. regenerateHealth(deltaTime);
        // np. regenerateMana(deltaTime);
    }

    /**
     * Metoda renderująca jednostkę.
     * @param batch SpriteBatch używany do rysowania.
     */
    public abstract void render(SpriteBatch batch);
    // Przykład implementacji w podklasie:
    // if (currentFrame != null) {
    //     batch.draw(currentFrame, position.x, position.y);
    // }


    /**
     * Zmniejsza duchowość jednostki.
     * @param amount Ilość punktów duchowości do odjęcia.
     */
    public void loseSpirituality(int amount) {
        if (amount <= 0) return;
        int oldSpirituality = stats.getSpirituality();
        stats.setSpirituality(Math.max(0, oldSpirituality - amount));
        int newSpirituality = stats.getSpirituality();
        if (oldSpirituality != newSpirituality) {
            EventManager.getInstance().publish(new SpiritualityChangedEvent(this, oldSpirituality, newSpirituality, -amount));
            System.out.println(name + " lost " + amount + " spirituality. Current: " + newSpirituality);
        }
    }

    /**
     * Zwiększa duchowość jednostki.
     * @param amount Ilość punktów duchowości do dodania.
     */
    public void gainSpirituality(int amount) {
        if (amount <= 0) return;
        int oldSpirituality = stats.getSpirituality();
        stats.setSpirituality(Math.min(100, oldSpirituality + amount));
        int newSpirituality = stats.getSpirituality();
        if (oldSpirituality != newSpirituality) {
            EventManager.getInstance().publish(new SpiritualityChangedEvent(this, oldSpirituality, newSpirituality, amount));
            System.out.println(name + " gained " + amount + " spirituality. Current: " + newSpirituality);
        }
    }

    /**
     * Jednostka otrzymuje obrażenia.
     * Uwzględnia odporności, obronę i blok.
     * @param rawDamage Początkowa wartość obrażeń.
     * @param damageType Typ obrażeń.
     * @param attacker Jednostka atakująca (może być null).
     */
    public void takeDamage(int rawDamage, DamageType damageType, Unit attacker) {
        if (health <= 0) return; // Już martwy

        float resistanceMultiplier = 1.0f - resistances.getResistance(damageType);
        int damageAfterResistance = (int) (rawDamage * resistanceMultiplier);

        int finalDamage = damageAfterResistance;
        // Uwzględnienie obrony (np. tylko dla PHYSICAL)
        if (damageType == DamageType.PHYSICAL) {
            finalDamage = Math.max(0, damageAfterResistance - defense);
        }

        // Szansa na blok (jeśli ma tarczę i jest to atak fizyczny)
        // if (equippedShield != null && damageType == DamageType.PHYSICAL && Math.random() < blockChance) {
        //     System.out.println(name + " blocked the attack!");
        //     // Można dodać efekt dźwiękowy/wizualny bloku
        //     return; // Brak obrażeń po bloku (lub zredukowane)
        // }

        health = Math.max(0, health - finalDamage);
        System.out.println(name + " took " + finalDamage + " " + damageType.getLocalizationKey() + " damage. Health: " + health + "/" + maxHealth);

        if (health <= 0) {
            die(attacker);
        } else {
            // Można opublikować event OnDamagedEvent
            // EventManager.getInstance().publish(new UnitDamagedEvent(this, attacker, finalDamage, damageType));
        }
    }

    /**
     * Logika śmierci jednostki.
     * @param killer Jednostka, która zadała śmiertelny cios (może być null).
     */
    protected void die(Unit killer) {
        System.out.println(name + " has died.");
        // TODO: Opublikuj event śmierci (np. UnitDiedEvent)
        // TODO: Logika upuszczania przedmiotów, doświadczenia dla zabójcy itp.
        // TODO: Zmiana stanu na "DeadState" lub usunięcie jednostki z gry
        stateMachine.changeState("DEAD"); // Założenie, że istnieje taki stan
    }

    /**
     * Używa umiejętności.
     * @param skill Umiejętność do użycia.
     * @param target Cel umiejętności (może być null).
     */
    public void useSkill(Skill skill, Unit target) {
        if (skill.canUse(this)) {
            skill.execute(this, target);
            // TODO: Opublikuj event użycia umiejętności
        } else {
            System.out.println(name + " cannot use " + skill.getNameKey() + " (not enough mana or cooldown).");
        }
    }

    /**
     * Rozpoczyna medytację. Zwiększa duchowość w czasie.
     * Ta metoda powinna być wywoływana w pętli update, gdy jednostka aktywnie medytuje.
     * @param deltaTime Czas od ostatniej klatki.
     */
    public void meditate(float deltaTime) {
        // Zwiększanie duchowości o MEDITATION_RATE na sekundę
        float spiritualityGain = MEDITATION_RATE * deltaTime;
        // Używamy tymczasowej zmiennej, aby uniknąć wielokrotnego wywoływania getSpirituality()
        int currentSpirituality = stats.getSpirituality();
        int newSpirituality = Math.min(100, (int) (currentSpirituality + spiritualityGain));

        if (newSpirituality > currentSpirituality) {
            gainSpirituality(newSpirituality - currentSpirituality); // Użyj gainSpirituality do publikacji eventu
        }
        // Można dodać warunek, że medytacja wymaga określonego stanu (np. "MeditatingState")
        // System.out.println(name + " is meditating. Spirituality: " + stats.getSpirituality());
    }

    /**
     * Zwiększa biegłość w danym typie broni.
     * @param weaponType Typ broni.
     * @param amount Ilość punktów biegłości do dodania.
     */
    public void addProficiency(WeaponType weaponType, float amount) {
        if (amount <= 0) return;
        float currentProficiency = weaponProficiencies.get(weaponType, 0.0f);
        float newProficiency = Math.min(MAX_PROFICIENCY, currentProficiency + amount);
        weaponProficiencies.put(weaponType, newProficiency);
        // System.out.println(name + " proficiency in " + weaponType + " increased to " + newProficiency);
    }

    /**
     * Wykonuje akcję "craftingu", która może zwiększyć statystyki.
     * @param statToImprove Statystyka do poprawy (np. DEXTERITY dla alchemii, INTELLIGENCE dla run).
     */
    public void performCraftAction(StatType statToImprove) {
        if (statToImprove == StatType.DEXTERITY || statToImprove == StatType.INTELLIGENCE) {
            // Zwiększanie statystyki o CRAFT_STAT_GAIN_AMOUNT
            // To jest uproszczenie, w rzeczywistości punkty powinny być float i kumulować się
            // A następnie przyznawać pełne punkty statystyk po przekroczeniu progu.
            // Na razie dodajemy bezpośrednio jako int dla uproszczenia.
            stats.addPoints(statToImprove, (int)Math.ceil(CRAFT_STAT_GAIN_AMOUNT)); // Zaokrąglamy w górę
            System.out.println(name + " performed a craft action, improving " + statToImprove.getLocalizationKey());
        }
    }

    /**
     * Symuluje efekt przeciążenia (noszenia zbyt ciężkiego ekwipunku).
     * Zwiększa siłę i wytrzymałość w czasie.
     * @param deltaTime Czas od ostatniej klatki.
     */
    public void simulateOverloadEffect(float deltaTime) {
        // Podobnie jak w craftingu, to jest uproszczenie.
        // Statystyki powinny kumulować się jako float i przyznawać punkty po przekroczeniu progu.
        stats.addPoints(StatType.STRENGTH, (int)Math.ceil(OVERLOAD_STAT_GAIN_RATE * deltaTime));
        stats.addPoints(StatType.ENDURANCE, (int)Math.ceil(OVERLOAD_STAT_GAIN_RATE * deltaTime));
        // System.out.println(name + " is overloaded, gaining strength and endurance.");
    }


    // Gettery i Settery
    public String getId() { return id; }
    public String getName() { return name; } // Może zwracać zlokalizowaną nazwę
    public Vector2 getPosition() { return position; }
    public void setPosition(float x, float y) { this.position.set(x, y); }
    public Vector2 getVelocity() { return velocity; }
    public void setVelocity(float x, float y) { this.velocity.set(x,y); }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = Math.max(0, Math.min(health, maxHealth)); }
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = Math.max(1, maxHealth); } // Zdrowie nie może być < 1
    public int getMana() { return mana; }
    public void setMana(int mana) { this.mana = Math.max(0, Math.min(mana, maxMana)); }
    public int getMaxMana() { return maxMana; }
    public void setMaxMana(int maxMana) { this.maxMana = Math.max(0, maxMana); }
    public Stats getStats() { return stats; }
    public Race getRace() { return race; }
    public StateMachine<Unit> getStateMachine() { return stateMachine; }
    public Resistances getResistances() { return resistances; }
    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = Math.max(0, defense); }
    public float getBlockChance() { return blockChance; }
    public void setBlockChance(float blockChance) { this.blockChance = Math.max(0f, Math.min(1f, blockChance)); }
    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = Math.max(0, gold); }
    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = Math.max(0, experience); }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, level); } // Poziom nie może być < 1
    public ObjectMap<WeaponType, Float> getWeaponProficiencies() { return weaponProficiencies; }
    public Weapon getEquippedWeapon() { return equippedWeapon; }
    public void setEquippedWeapon(Weapon weapon) { this.equippedWeapon = weapon; }
    public TextureRegion getCurrentFrame() { return currentFrame; }
    public void setCurrentFrame(TextureRegion frame) { this.currentFrame = frame; }

    /**
     * Sprawdza, czy jednostka żyje.
     * @return True, jeśli zdrowie > 0.
     */
    public boolean isAlive() {
        return health > 0;
    }

    /**
     * Metoda do odświeżania statystyk zależnych od innych (np. maxHealth po zmianie Endurance).
     * Powinna być wywoływana po zmianie statystyk bazowych lub poziomu.
     */
    public void refreshDependentStats() {
        int oldMaxHealth = this.maxHealth;
        this.maxHealth = calculateMaxHealth();
        // Jeśli zdrowie przekracza nowe maxHealth, ustaw je na maxHealth
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        } else if (this.maxHealth > oldMaxHealth) { // Jeśli maxHealth wzrosło, dodaj różnicę do bieżącego zdrowia
            this.health += (this.maxHealth - oldMaxHealth);
            this.health = Math.min(this.health, this.maxHealth); // Upewnij się, że nie przekracza nowego max
        }


        int oldMaxMana = this.maxMana;
        this.maxMana = calculateMaxMana();
        if (this.mana > this.maxMana) {
            this.mana = this.maxMana;
        } else if (this.maxMana > oldMaxMana) {
            this.mana += (this.maxMana - oldMaxMana);
            this.mana = Math.min(this.mana, this.maxMana);
        }
    }


    // Metody związane z zapisem/odczytem gry (implementacja JsonSerializable)
    // Te metody będą bardziej złożone i będą wymagały biblioteki GSON
    @Override
    public String toJson() {
        // TODO: Implementacja serializacji do JSON przy użyciu GSON
        // Należy zserializować wszystkie istotne pola: position, health, stats, etc.
        // StateMachine może wymagać specjalnego traktowania (np. zapis nazwy aktualnego stanu)
        com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
            // .registerTypeAdapter(StateMachine.class, new StateMachineSerializer()) // Przykładowy custom serializer
            .create();
        return gson.toJson(this); // Uproszczenie, GSON może wymagać konfiguracji dla niektórych typów
    }

    @Override
    public void fromJson(String json) {
        // TODO: Implementacja deserializacji z JSON przy użyciu GSON
        com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
            // .registerTypeAdapter(StateMachine.class, new StateMachineDeserializer(this)) // Przykładowy custom deserializer
            .create();
        Unit loadedUnit = gson.fromJson(json, this.getClass()); // Użyj getClass() dla poprawnej deserializacji podklas

        // Kopiowanie pól - GSON może to zrobić automatycznie, ale czasami potrzebna jest ręczna interwencja
        this.id = loadedUnit.id;
        this.name = loadedUnit.name;
        this.position = new Vector2(loadedUnit.position); // Upewnij się, że Vector2 jest poprawnie serializowany/deserializowany
        this.velocity = new Vector2(loadedUnit.velocity);
        this.health = loadedUnit.health;
        this.maxHealth = loadedUnit.maxHealth;
        this.mana = loadedUnit.mana;
        this.maxMana = loadedUnit.maxMana;
        this.stats = loadedUnit.stats; // Zakładając, że Stats jest poprawnie (de)serializowany
        this.race = loadedUnit.race;
        this.resistances = loadedUnit.resistances; // Zakładając, że Resistances jest poprawnie (de)serializowany
        this.defense = loadedUnit.defense;
        this.blockChance = loadedUnit.blockChance;
        this.gold = loadedUnit.gold;
        this.experience = loadedUnit.experience;
        this.level = loadedUnit.level;
        this.weaponProficiencies = new ObjectMap<>(loadedUnit.weaponProficiencies); // Głęboka kopia, jeśli GSON nie robi
        this.equippedWeapon = loadedUnit.equippedWeapon; // Zakładając, że Weapon jest poprawnie (de)serializowany

        // StateMachine wymaga specjalnej obsługi - np. odtworzenie stanu na podstawie zapisanej nazwy
        // this.stateMachine = new StateMachine<>(this);
        // addStatesToStateMachine(); // Dodaj wszystkie możliwe stany
        // if (loadedUnit.getPersistentStateName() != null) { // Załóżmy, że jest metoda do zapisu nazwy stanu
        //    this.stateMachine.changeState(loadedUnit.getPersistentStateName());
        // } else {
        //    this.stateMachine.changeState(getDefaultStateName()); // Ustaw domyślny stan
        // }
        // Na razie pomijamy złożoną (de)serializację StateMachine
    }

    /**
     * Metoda pomocnicza do dodawania wszystkich możliwych stanów do maszyny stanów.
     * Powinna być zaimplementowana w podklasach lub tutaj, jeśli stany są wspólne.
     */
    protected abstract void addStatesToStateMachine();

    /**
     * Zwraca nazwę domyślnego stanu dla tej jednostki.
     */
    protected abstract String getDefaultStateName();

}
