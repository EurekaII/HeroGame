# HeroGame

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and an `ApplicationAdapter` extension that draws libGDX logo.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.
- `server`: A separate application without access to the `core` module.
- `shared`: A common module shared by `core` and `server` platforms.

## Architektura gry

### System jednostek
Gra wykorzystuje skalowalny system jednostek z następującą hierarchią:
- `Entity` - Bazowa klasa dla wszystkich obiektów w grze
- `BaseCreature` - Bazowa klasa dla wszystkich żywych stworzeń
- Rodziny stworzeń (np. `BaseSlime`, `BaseSkeleton`) dziedziczą po `BaseCreature`
- Konkretne warianty (np. `GreenSlime`, `GreenSlimeElite`, `KingSlime`) dziedziczą po swojej klasie rodziny

### System maszyny stanów (FSM)
Wszystkie jednostki sterowane przez AI używają systemu FSM znajdującego się w `io.github.HeroGame.ai.fsm`:
- `State<T>` - Interfejs dla wszystkich stanów
- `StateMachine<T>` - Zarządza przejściami między stanami
- Stany znajdują się w `io.github.HeroGame.ai.states`

### Mechanika Duchowości
Gra posiada unikalny system Duchowości:
- Duchowość gracza (0-100) wpływa na zachowanie NPC
- Wysoka duchowość (>75) może sprawić, że wrogie NPC staną się neutralne
- Atak na neutralne NPC zmniejsza duchowość gracza o 25

### System umiejętności
Jednostki mogą implementować interfejs `IAbilityUser` aby używać umiejętności:
- Umiejętności są współdzielone między graczem a przeciwnikami
- Każda umiejętność ma czas odnowienia, koszt many i zasięg
- Umiejętności znajdują się w `io.github.HeroGame.abilities`

### Przykład integracji z istniejącym kodem:

```java
// W GameScreen.java, dodaj inicjalizację przeciwników z FSM:
private void initializeEnemies() {
    // Użyj istniejącej klasy Goblin
    Goblin goblin = new Goblin(300, 400);
    goblin.setTarget(player);
    entities.add(goblin);
    
    // Dodaj nowe Slime'y gdy zostaną zaimplementowane
    GreenSlime slime = new GreenSlime(200, 200);
    slime.setTarget(player);
    entities.add(slime);
}

// W Enemy.java (io.github.HeroGame.entities.enemies), dodaj pole StateMachine:
public abstract class Enemy extends Entity {
    protected StateMachine<Enemy> stateMachine;
    protected Player target;
    
    public Enemy(float x, float y) {
        super(x, y);
        this.stateMachine = new StateMachine<>(this);
    }
}

// W Player.java (io.github.HeroGame.entities), dodaj mechanikę duchowości:
public class Player extends Entity {
    private int spirituality = 50;
    
    public void loseSpirituality(int amount) {
        spirituality = Math.max(0, spirituality - amount);
    }
}

# Dalsze Założenia Projektowe: RPG z Wyborami Moralnymi i Reaktywnym Światem

## Wizja Projektu
Tworzymy dynamiczną grę RPG, w której wybory gracza mają realny wpływ na świat i interakcje z jego mieszkańcami. Gra odchodzi od binarnego podziału na "dobrych" i "złych", oferując głęboki system moralny oparty na statystyce **Duchowości**. Gracz może wybrać ścieżkę pacyfistyczną, unikając walki, lub klasyczną, opartą na walce, z różnymi konsekwencjami dla każdej z nich. Świat reaguje na działania gracza, a NPC (w tym przeciwnicy) zachowują się dynamicznie dzięki systemowi maszyn stanów (FSM) i globalnemu systemowi eventów.

## Kluczowe Mechaniki

### 1. System Duchowości
- **Duchowość** to kluczowa statystyka gracza, określająca jego "moralny kompas".
- Wysoka Duchowość pozwala przeciwnikom przejść w stan **Neutralny**, w którym stają się NPC oferującymi interakcje (dialogi, questy, handel, nauka umiejętności).
- Atak na neutralnego NPC skutkuje utratą Duchowości (np. -25 punktów) i natychmiastowym powrotem NPC do stanu agresywnego.
- Duchowość wpływa na szanse na pozytywne interakcje:
  - Za każde 5 punktów powyżej progu neutralności NPC, szansa na pozytywny event (quest, handel, nauka) rośnie o 2% (z mnożnikiem x2, max. 90%).
- Duchowość może wzrastać przez pokojowe akcje (np. ukończenie "dobrych" questów) lub maleć przez agresywne działania.
- W przyszłości: Duchowość i reputacja mogą wpływać na reakcje frakcji, dostępność usług w miastach i otwieranie/zamykanie ścieżek fabularnych.

### 2. System Maszyn Stanów (FSM) dla NPC/Przeciwników
- Każdy NPC/przeciwnik korzysta z FSM do zarządzania zachowaniami:
  - **WanderState**: NPC porusza się losowo po mapie.
  - **ChaseState**: NPC ściga gracza, jeśli ten jest w zasięgu wykrycia.
  - **AttackState**: NPC atakuje, gdy gracz jest w zasięgu ataku.
  - **NeutralState**: Aktywowany, gdy Duchowość gracza przekracza próg specyficzny dla NPC. NPC staje się nieagresywny i oferuje interakcje.
  - **FleeState** (opcjonalny): NPC ucieka, jeśli Duchowość gracza jest bardzo niska.
- **Histereza**: Wprowadzono opóźnienie (np. 2 sekundy) dla przejścia z NeutralState do stanów agresywnych, aby uniknąć "migotania" zachowań na granicy warunków.
- Różne progi Duchowości dla różnych typów NPC (np. spokojny szkielet: 40, agresywny wojownik: 90).

### 3. Interakcje w Stanie Neutralnym
- Neutralni NPC oferują zróżnicowane interakcje, losowane lub zależne od kontekstu (postępów gracza, pory dnia, globalnych wydarzeń):
  - **Prosty dialog**: Np. "Goblin patrzy na Ciebie z ostrożnością...".
  - **Questy**: Generowane (np. "Zbierz 10 ziół") lub unikalne fabularne (np. szkielet proszący o dostarczenie medalionu do wdowy).
  - **Handel**: Wymiana przedmiotów.
  - **Nauka umiejętności**: NPC może nauczyć gracza pasywnych zdolności.
  - **Żądanie przedmiotu**: Np. potwór prosi o jedzenie w zamian za nagrodę.
- Unikalne questy są jednorazowe dla danego NPC, śledzone przez `UniqueQuestManager`.
- Szansa na pozytywny event (quest, handel, nauka) rośnie z nadwyżką Duchowości.

### 4. System Questów
- **QuestGiver**: Interfejs łączący tablice zadań (np. Gildia Najemników) i NPC oferujących questy. Oba działają podobnie – NPC z questem to "tablica z jednym zadaniem".
- **Generowane questy**: Proste zadania (np. zabij X potworów, zbierz Y przedmiotów, dostarcz paczkę z A do B).
- **Unikalne questy fabularne**: Predefiniowane historie (np. szkielet z medalionem). Zawierają elementy jak przekazanie przedmiotu (np. medalion) i specyficzne cele (np. dostarczenie do NPC).
- **Tablice zadań**: Oferują questy specyficzne dla frakcji (np. Gildia Najemników: zabójstwa, eskorty). Odświeżają się cyklicznie (np. co 5 minut).

### 5. Globalny System Eventów
- System **Publikuj/Subskrybuj** (`EventManager`) umożliwia luźno powiązaną komunikację między systemami gry.
- Kluczowe eventy:
  - `QuestTakenEvent`: Publikowany, gdy gracz akceptuje quest (z tablicy lub NPC).
  - `PlayerSpiritualityChangedEvent`: Informuje o zmianie Duchowości gracza.
  - `EnemyKilledEvent`, `ItemCollectedEvent`: Do śledzenia postępów questów.
- Zapewnia elastyczność i skalowalność przy dodawaniu nowych interakcji i systemów.

### 6. Pacyfistyczna Ścieżka Gry
- Gra jest zaprojektowana tak, by możliwe było przejście bez walki od początku:
  - Wysoka Duchowość pozwala unikać walki przez przełączanie NPC w NeutralState.
  - Alternatywne rozwiązania questów: np. przegonienie potworów zamiast zabijania, użycie perswazji/skradania.
  - Umiejętności niewalczące: perswazja, alchemia, skradanie się, wiedza o świecie.
- Konsekwencje stylu gry:
  - Agresywna gra obniża Duchowość, co może zamykać dostęp do "pacyfistycznych" questów i NPC.
  - Pokojowe podejście otwiera unikalne ścieżki fabularne i buduje reputację z frakcjami.

## Plan Implementacji
1. **Podstawy Systemu FSM**:
   - Zaimplementować `State`, `StateMachine`, i podstawowe stany (`Wander`, `Chase`, `Attack`, `Neutral`).
   - Dodać obsługę progu Duchowości w stanach agresywnych dla przejścia do `NeutralState`.
   - Wprowadzić histerezę w `NeutralState` (opóźnienie 2s dla zmiany stanu).

2. **Rozbudowa Interakcji NPC**:
   - Dodać `ComplexNeutralInteractionType` dla różnych typów interakcji (dialog, quest, handel, nauka, żądanie przedmiotu).
   - Zaimplementować logikę losowania interakcji w `Enemy.randomizeOfferedInteraction`, z uwzględnieniem wpływu Duchowości na szanse pozytywnych eventów.
   - Stworzyć `UniqueQuestManager` do zarządzania unikalnymi questami (np. szkielet z medalionem).

3. **System Questów**:
   - Zaimplementować interfejs `QuestGiver` dla `QuestBoard` i `Enemy`.
   - Stworzyć `QuestGenerator` dla generycznych questów i `UniqueQuestDefinition` dla fabularnych.
   - Zintegrować z `EventManager` dla publikacji `QuestTakenEvent` i innych.

4. **Duchowość i Konsekwencje**:
   - Dodać mechanikę utraty Duchowości w `Enemy.onAttackedBy` dla neutralnych NPC.
   - Rozbudować `Player` o metody `loseSpirituality` i `gainSpirituality`.
   - W przyszłości: zaimplementować wpływ Duchowości na frakcje i świat.

5. **Pacyfistyczna Ścieżka**:
   - Dodać alternatywne rozwiązania questów (np. opcje perswazji, skradania).
   - Rozbudować system umiejętności niewalczących.
   - Wprowadzić prosty system reputacji dla frakcji.

6. **Optymalizacja Grafiki**:
   - Na razie ręczne cięcie sprite’ów i użycie `TextureRegion.split` dla arkuszy.
   - Docelowo: przejście na atlasy tekstur z darmowym TexturePackerem LibGDX lub komercyjnym Texture Packerem, gdy będzie dostępny.

## Następne Kroki
- Zaimplementować podstawy FSM i przetestować z prostymi NPC (np. goblin, szkielet).
- Stworzyć prototyp systemu questów z jednym generowanym i jednym unikalnym questem.
- Dodać interfejs `QuestGiver` i przetestować z `QuestBoard` oraz neutralnym NPC.
- Wprowadzić podstawową obsługę Duchowości i jej wpływu na stany NPC.
- Iteracyjnie dodawać nowe interakcje i testować pacyfistyczną ścieżkę gry.

## Uwagi Techniczne
- **Technologia**: LibGDX (Java) do renderowania i zarządzania grą.
- **Struktura kodu**: Modularna, z wyraźnym podziałem odpowiedzialności (FSM w `Enemy`, questy w `QuestManager`, eventy w `EventManager`).
- **Optymalizacja grafiki**: Na razie ręczne cięcie sprite’ów; docelowo atlasy tekstur dla wydajności.

Projekt ma ogromny potencjał, łącząc elastyczność klasycznego RPG z innowacyjnym systemem moralnym i dynamicznymi interakcjami. Stopniowe wdrażanie pozwoli na iteracyjne testowanie i dopracowanie mechanik.