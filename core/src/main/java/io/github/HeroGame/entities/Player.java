package io.github.HeroGame.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Disposable;

import io.github.HeroGame.entities.items.Item;
import io.github.HeroGame.entities.stats.StatType;
import io.github.HeroGame.entities.unit.Unit;
import io.github.HeroGame.fsm.State;
import io.github.HeroGame.states.PlayerIdleState; // Założenie stworzenia tego stanu
import io.github.HeroGame.states.PlayerWalkState; // Założenie stworzenia tego stanu
import io.github.HeroGame.states.PlayerAttackState; // Nowy stan
import io.github.HeroGame.states.PlayerMiningState; // Nowy stan
import io.github.HeroGame.states.PlayerWoodChopState; // Nowy stan
import io.github.HeroGame.states.PlayerHurtState;   // Nowy stan
import io.github.HeroGame.states.PlayerDeathState;  // Nowy stan
import io.github.HeroGame.states.PlayerDiggingState; // Nowy stan
import io.github.HeroGame.states.PlayerBowShotState; // Nowy stan
// Importuj inne potrzebne stany gracza, np. AttackState, MeditateState etc.

/**
 * Reprezentuje postać gracza w grze.
 * Dziedziczy po Unit i dodaje specyficzne dla gracza mechaniki jak ekwipunek,
 * punkty umiejętności i zdobywanie poziomów.
 */


public class Player extends Unit implements Disposable {
    private Array<Item> inventory;
    private int experienceToNextLevel;
    private int skillPoints;

    private ObjectMap<String, Animation<TextureRegion>> animations;
    private float stateTime;
    private ObjectMap<String, Texture> loadedSheets;

    private static final int SKILL_POINTS_PER_LEVEL = 5;
    private static final String DEFAULT_PLAYER_ID = "player";
    private static final String DEFAULT_PLAYER_NAME = "Hero";
    private static final String SPRITES_BASE_PATH = "assets/sprites/player/";

    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 42;

    public enum ToolType { NONE, SWORD, TORCH, AXE, PICKAXE, BOW }
    private ToolType currentTool = ToolType.NONE;

    public enum FacingDirection {
        NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST,
        UP, DOWN, LEFT, RIGHT
    }
    private FacingDirection currentFacingDirection;

    public Player(Vector2 initialPosition) {
        super(DEFAULT_PLAYER_ID, DEFAULT_PLAYER_NAME, Race.HUMAN, initialPosition);
        this.inventory = new Array<>();
        this.skillPoints = 0;
        this.experienceToNextLevel = calculateExpToNextLevel(this.level);
        this.animations = new ObjectMap<>();
        this.loadedSheets = new ObjectMap<>();
        this.stateTime = 0f;
        this.currentFacingDirection = FacingDirection.SOUTH;

        try {
            Pixmap pixmap = new Pixmap(FRAME_WIDTH, FRAME_HEIGHT, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.MAGENTA);
            pixmap.fill();
            this.currentFrame = new TextureRegion(new Texture(pixmap));
            pixmap.dispose();
            System.out.println("Player initial currentFrame set to MAGENTA placeholder (" + FRAME_WIDTH + "x" + FRAME_HEIGHT + ").");
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to create initial placeholder pixmap for Player: " + e.getMessage());
        }

        loadAnimations();
        addStatesToStateMachine();

        String defaultStateName = getDefaultStateName();
        FacingDirection initialDirection = FacingDirection.SOUTH;
        String initialAnimKey = getAnimationKey(defaultStateName, initialDirection, ToolType.NONE);

        if (this.stateMachine.hasState(defaultStateName) && animations.containsKey(initialAnimKey)) {
            this.stateMachine.changeState(defaultStateName);
            this.currentFacingDirection = initialDirection;
            this.currentFrame = animations.get(initialAnimKey).getKeyFrame(0);
            System.out.println("Player initial state set to " + defaultStateName + ", direction " + initialDirection + ", animKey " + initialAnimKey);
        } else {
            System.err.println("Initial setup failed: Default state '" + defaultStateName + "' or initial animation key '" + initialAnimKey + "' not found. Searched for key: " + initialAnimKey);
            if (this.stateMachine.hasState("IDLE")) {
                this.stateMachine.changeState("IDLE");
                System.err.println("Fallback to IDLE state, but initial animation might be incorrect.");
            } else {
                System.err.println("CRITICAL: Fallback state 'IDLE' also not found for Player.");
            }
        }
    }

    private Texture getSheet(String fileName) {
        if (!loadedSheets.containsKey(fileName)) {
            try {
                Texture sheet = new Texture(Gdx.files.internal(SPRITES_BASE_PATH + fileName));
                loadedSheets.put(fileName, sheet);
                System.out.println("  Loaded texture sheet: '" + fileName + "' (Actual Width: " + sheet.getWidth() + ", Actual Height: " + sheet.getHeight() + ")");
                return sheet;
            } catch (Exception e) {
                System.err.println("  EXCEPTION during loading texture sheet: '" + fileName + "' - " + e.getMessage());
                return null;
            }
        }
        return loadedSheets.get(fileName);
    }

    private void loadAnimations() {
        System.out.println("--- Loading Player Animations (Frame: " + FRAME_WIDTH + "x" + FRAME_HEIGHT + ") ---");

        int commonStartX = 4 * 32;
        int commonInitialY = 2 * 42;
        int commonRowSpacing = 6 * FRAME_HEIGHT;
        int horizontalFrameStep = (1 + 8) * FRAME_WIDTH;

        float idleFrameDuration = 0.15f;
        float walkFrameDuration = 0.1f;

        FacingDirection[] directionOrder = {
            FacingDirection.EAST, FacingDirection.NORTH, FacingDirection.WEST, FacingDirection.SOUTH,
            FacingDirection.SOUTHEAST, FacingDirection.NORTHEAST, FacingDirection.NORTHWEST, FacingDirection.SOUTHWEST
        };

        // === WAŻNE: ZDEFINIUJ POPRAWNĄ LICZBĘ KLATEK DLA KAŻDEGO KIERUNKU ===
        // Kolejność w tablicy musi odpowiadać kolejności w `directionOrder`
        int[] idleFramesCounts = {
            8, // EAST (zgodnie z obrazkiem image_272d62.png, rząd 1 ma 10 klatek)
            8, // NORTH (rząd 2 ma 6 klatek)
            8, // WEST  (rząd 3 ma 6 klatek)
            8, // SOUTH (rząd 4 ma 6 klatek)
            8, // SOUTHEAST (załóżmy 8, dostosuj!)
            8, // NORTHEAST (załóżmy 8, dostosuj!)
            8, // NORTHWEST (załóżmy 8, dostosuj!)
            8  // SOUTHWEST (załóżmy 8, dostosuj!)
        };
        // Zrób to samo dla animacji Walk, jeśli liczby klatek są inne niż dla Idle
        int[] walkFramesCounts = { // Załóżmy, że Walk ma taki sam układ klatek jak Idle
            8, 8, 8, 8, 8, 8, 8, 8 // DOSTOSUJ TE WARTOŚCI!
        };


        Texture idleSheet = getSheet("Char Idle.png");
        Texture walkSheet = getSheet("Char Walk.png");

        if (idleSheet != null) {
            System.out.println("Loading Idle animations from Char Idle.png with horizontalFrameStep: " + horizontalFrameStep);
            int currentY_idle = commonInitialY;
            for (int i = 0; i < directionOrder.length; i++) {
                if (i < idleFramesCounts.length) { // Zabezpieczenie
                    loadAnimationSequence("Idle", directionOrder[i], idleSheet, commonStartX, currentY_idle, idleFramesCounts[i], idleFrameDuration, Animation.PlayMode.LOOP, ToolType.NONE, horizontalFrameStep);
                }
                currentY_idle += FRAME_HEIGHT + commonRowSpacing;
            }
        }

        if (walkSheet != null) {
            System.out.println("Loading Walk animations from Char Walk.png with horizontalFrameStep: " + horizontalFrameStep);
            int currentY_walk = commonInitialY;
            for (int i = 0; i < directionOrder.length; i++) {
                if (i < walkFramesCounts.length) { // Zabezpieczenie
                    loadAnimationSequence("Walk", directionOrder[i], walkSheet, commonStartX, currentY_walk, walkFramesCounts[i], walkFrameDuration, Animation.PlayMode.LOOP, ToolType.NONE, horizontalFrameStep);
                }
                currentY_walk += FRAME_HEIGHT + commonRowSpacing;
            }
        }

        int tightHorizontalFrameStep = FRAME_WIDTH;
        Texture attackSheet = getSheet("Char Attack.png");
        if (attackSheet != null) {
            System.out.println("Loading Attack_SWORD animations from Char Attack.png with horizontalFrameStep: " + tightHorizontalFrameStep);
            int attackFrames = 6; float attackFrameDuration = 0.08f; // DOSTOSUJ liczbę klatek dla ataku!
            // Poniżej zakładamy, że arkusz Attack ma prosty układ 4 rzędów od (0,0)
            // Jeśli jest inaczej, musisz podać dokładne startX, startY dla każdego kierunku.
            FacingDirection[] attackDirOrder = {FacingDirection.DOWN, FacingDirection.LEFT, FacingDirection.RIGHT, FacingDirection.UP};
            int[] attackFramesCounts = {6, 6, 6, 6}; // DOSTOSUJ liczbę klatek dla każdego kierunku ataku!
            int currentY_attack = 0; // Załóżmy start od Y=0 dla tego arkusza

            for(int i=0; i<attackDirOrder.length; i++){
                if (i < attackFramesCounts.length) {
                    loadAnimationSequence("Attack_SWORD", attackDirOrder[i], attackSheet, 0, currentY_attack, attackFramesCounts[i], attackFrameDuration, Animation.PlayMode.NORMAL, ToolType.SWORD, tightHorizontalFrameStep);
                    currentY_attack += FRAME_HEIGHT; // Załóżmy brak dodatkowych przerw pionowych w arkuszu Attack
                }
            }
        }
        // TODO: Zrób to samo (załadowanie arkusza i precyzyjne wywołania loadAnimationSequence)
        // dla "Char Attack H.png", "Char Bow Shot.png", "Char Wood Chop.png", "Char Mining.png", "Char Dig.png", "Char Hurt.png", "Char Death.png"
        // Pamiętaj o poprawnym `horizontalFrameStep` dla każdego arkusza.

        System.out.println("--- Finished Loading Player Animations ---");

        String initialAnimKey = getAnimationKey("IDLE", FacingDirection.SOUTH, ToolType.NONE);
        if (animations.containsKey(initialAnimKey)) {
            this.currentFrame = animations.get(initialAnimKey).getKeyFrame(0);
            System.out.println("Player currentFrame set to initial IDLE_SOUTH animation (Key: " + initialAnimKey + ").");
        } else {
            System.err.println("Initial IDLE_SOUTH animation not found (key: " + initialAnimKey + "). Player will use MAGENTA placeholder if available.");
            if (this.currentFrame == null || !(this.currentFrame.getTexture().getTextureData() instanceof com.badlogic.gdx.graphics.glutils.PixmapTextureData) ) {
                Pixmap pixmap = new Pixmap(FRAME_WIDTH, FRAME_HEIGHT, Pixmap.Format.RGBA8888);
                pixmap.setColor(Color.BLUE); pixmap.fill();
                this.currentFrame = new TextureRegion(new Texture(pixmap));
                pixmap.dispose();
                System.err.println("Player currentFrame set to BLUE emergency placeholder.");
            }
        }
    }

    private void loadAnimationSequence(String baseActionName, FacingDirection direction, Texture sheet,
                                       int startXOnSheet, int startYOnSheet, int framesInSequence,
                                       float frameDuration, Animation.PlayMode playMode, ToolType tool,
                                       int horizontalFrameStep) {
        if (sheet == null) {
            System.err.println("  Skipping animation sequence: Action='" + baseActionName + "', Dir='" + direction + "' because texture sheet is null.");
            return;
        }
        if (framesInSequence <= 0) {
            System.out.println("    Skipping sequence for Action='" + baseActionName + "', Dir='" + direction + "' as framesInSequence is 0 or less.");
            return;
        }
        System.out.println("  Attempting to load sequence: Action='" + baseActionName + "', Dir='" + direction + "', Tool='" + tool +
            "' from sheet at (" + startXOnSheet + "," + startYOnSheet +
            ") for " + framesInSequence + " frames. Horizontal step: " + horizontalFrameStep);
        try {
            Array<TextureRegion> frames = new Array<>();
            for (int i = 0; i < framesInSequence; i++) {
                int currentFrameX = startXOnSheet + (i * horizontalFrameStep);
                if (currentFrameX + FRAME_WIDTH <= sheet.getWidth() && startYOnSheet + FRAME_HEIGHT <= sheet.getHeight()) {
                    frames.add(new TextureRegion(sheet, currentFrameX, startYOnSheet, FRAME_WIDTH, FRAME_HEIGHT));
                } else {
                    System.err.println("    Frame " + i + " for " + baseActionName + "_" + direction +
                        " is out of bounds for texture. Requested: x=" + currentFrameX +
                        ", y=" + startYOnSheet + ". Texture dims: " + sheet.getWidth() + "x" + sheet.getHeight());
                    break;
                }
            }

            if (frames.size == framesInSequence) { // Sprawdź, czy udało się załadować WSZYSTKIE oczekiwane klatki
                String animationKey = getAnimationKey(baseActionName, direction, tool);
                animations.put(animationKey, new Animation<>(frameDuration, frames, playMode));
                System.out.println("    Successfully loaded animation sequence: '" + animationKey + "' with " + frames.size + " frames.");
            } else {
                System.err.println("    Failed to extract expected " + framesInSequence + " frames for sequence: Action='" + baseActionName +
                    "', Dir='" + direction + "', Tool='" + tool +
                    "'. Extracted " + frames.size + " frames.");
            }
        } catch (Exception e) {
            System.err.println("  EXCEPTION during loading animation sequence: Action='" + baseActionName + "', Dir='" + direction +
                "' - " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void loadAnimationSequence(String baseActionName, FacingDirection direction, Texture sheet,
                                       int startXOnSheet, int startYOnSheet, int framesInSequence,
                                       float frameDuration, Animation.PlayMode playMode, int horizontalFrameStep) {
        loadAnimationSequence(baseActionName, direction, sheet, startXOnSheet, startYOnSheet, framesInSequence, frameDuration, playMode, ToolType.NONE, horizontalFrameStep);
    }

    @Override
    public void dispose() {
        System.out.println("Disposing Player resources...");
        for (Texture texture : loadedSheets.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        loadedSheets.clear();
        System.out.println("Player loadedSheets disposed.");
    }

    private String getAnimationKey(String action, FacingDirection direction, ToolType tool) {
        String toolNamePart = "";
        ToolType effectiveTool = tool;

        if (action.equalsIgnoreCase("ATTACK")) {
            effectiveTool = (tool == ToolType.NONE || tool == ToolType.TORCH) ? ToolType.SWORD : tool;
        } else if (action.equalsIgnoreCase("WOODCHOP")) {
            effectiveTool = ToolType.AXE;
        } else if (action.equalsIgnoreCase("MINE")) {
            effectiveTool = ToolType.PICKAXE;
        }

        if (effectiveTool != ToolType.NONE) {
            if (action.equalsIgnoreCase("ATTACK") ||
                action.equalsIgnoreCase("WOODCHOP") ||
                action.equalsIgnoreCase("MINE") ||
                action.equalsIgnoreCase("DIG") ||
                (action.equalsIgnoreCase("IDLE") && effectiveTool != ToolType.NONE) ||
                (action.equalsIgnoreCase("WALK") && effectiveTool != ToolType.NONE) ) {
                toolNamePart = effectiveTool.name();
            }
        }

        FacingDirection mappedDirection = direction;
        boolean isFourDirectionalAction = action.matches("ATTACK.*|WOODCHOP|MINE|DIG|HURT|BOW_SHOT");

        if (action.equalsIgnoreCase("DEATH")) {
            mappedDirection = FacingDirection.DOWN;
        } else if (isFourDirectionalAction) {
            switch (direction) {
                case NORTH: case NORTHEAST: case NORTHWEST: case UP: mappedDirection = FacingDirection.UP; break;
                case EAST: case SOUTHEAST: mappedDirection = FacingDirection.RIGHT; break;
                case WEST: case SOUTHWEST: mappedDirection = FacingDirection.LEFT; break;
                case SOUTH: case DOWN: mappedDirection = FacingDirection.DOWN; break;
                default: mappedDirection = FacingDirection.DOWN;
            }
        } else { // 8-directional actions
            switch (direction) {
                case NORTH: mappedDirection = FacingDirection.NORTH; break;
                case NORTHEAST: mappedDirection = FacingDirection.NORTHEAST; break;
                case EAST: mappedDirection = FacingDirection.EAST; break;
                case SOUTHEAST: mappedDirection = FacingDirection.SOUTHEAST; break;
                case SOUTH: mappedDirection = FacingDirection.SOUTH; break;
                case SOUTHWEST: mappedDirection = FacingDirection.SOUTHWEST; break;
                case WEST: mappedDirection = FacingDirection.WEST; break;
                case NORTHWEST: mappedDirection = FacingDirection.NORTHWEST; break;
                case UP: mappedDirection = FacingDirection.NORTH; break;
                case DOWN: mappedDirection = FacingDirection.SOUTH; break;
                case LEFT: mappedDirection = FacingDirection.WEST; break;
                case RIGHT: mappedDirection = FacingDirection.EAST; break;
            }
        }

        String key = action.toUpperCase();
        if (!toolNamePart.isEmpty()) {
            key += "_" + toolNamePart;
        }
        key += "_" + mappedDirection.name();
        return key;
    }


    @Override
    protected void addStatesToStateMachine() {
        if (this.stateMachine == null) {
            System.err.println("CRITICAL: StateMachine is null in Player.addStatesToStateMachine. Re-initializing.");
            this.stateMachine = new io.github.HeroGame.fsm.StateMachine<>(this);
        }
        stateMachine.addState("IDLE", new PlayerIdleState());
        stateMachine.addState("WALK", new PlayerWalkState());
        stateMachine.addState("ATTACK", new PlayerAttackState());
        stateMachine.addState("MINE", new PlayerMiningState());
        stateMachine.addState("WOODCHOP", new PlayerWoodChopState());
        stateMachine.addState("DIG", new PlayerDiggingState());
        stateMachine.addState("BOW_SHOT", new PlayerBowShotState());
        stateMachine.addState("HURT", new PlayerHurtState());
        stateMachine.addState("DEATH", new PlayerDeathState());
    }

    @Override
    protected String getDefaultStateName() {
        return "IDLE";
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        stateTime += deltaTime;

        if (experience >= experienceToNextLevel && isAlive()) {
            levelUp();
        }

        if (currentFrame == null) {
            System.err.println("CRITICAL: currentFrame is null in Player.update() before animation update. Setting RED placeholder.");
            Pixmap pixmap = new Pixmap(FRAME_WIDTH, FRAME_HEIGHT, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.RED); pixmap.fill();
            this.currentFrame = new TextureRegion(new Texture(pixmap));
            pixmap.dispose();
        }

        if (isAlive()) {
            String animationKey = getCurrentAnimationKey();
            Animation<TextureRegion> currentAnim = animations.get(animationKey);

            if (currentAnim != null) {
                boolean loop = (currentAnim.getPlayMode() == Animation.PlayMode.LOOP ||
                    currentAnim.getPlayMode() == Animation.PlayMode.LOOP_PINGPONG ||
                    currentAnim.getPlayMode() == Animation.PlayMode.LOOP_RANDOM ||
                    currentAnim.getPlayMode() == Animation.PlayMode.LOOP_REVERSED);
                currentFrame = currentAnim.getKeyFrame(stateTime, loop);

                if (currentAnim.getPlayMode() == Animation.PlayMode.NORMAL && currentAnim.isAnimationFinished(stateTime)) {
                    if (!stateMachine.isInState(PlayerDeathState.class)) {
                        State<?> currentState = stateMachine.getCurrentState();
                        boolean isSelfManagedState = currentState instanceof PlayerAttackState ||
                            currentState instanceof PlayerBowShotState ||
                            currentState instanceof PlayerHurtState ||
                            currentState instanceof PlayerDiggingState;

                        if (!isSelfManagedState) {
                            // stateMachine.changeState("IDLE"); // Rozważ, czy to jest potrzebne
                        }
                    }
                }
            } else {
                System.err.println("BLINK DETECTED! Animation key NOT FOUND in Player.update(): '" + animationKey + "'. Current State: " + (stateMachine.getCurrentState() != null ? stateMachine.getCurrentState().getClass().getSimpleName() : "null") + ", Direction: " + currentFacingDirection);
                String idleKey = getAnimationKey("IDLE", currentFacingDirection, ToolType.NONE);
                Animation<TextureRegion> idleFallbackAnim = animations.get(idleKey);
                if (idleFallbackAnim != null) {
                    currentFrame = idleFallbackAnim.getKeyFrame(stateTime, true);
                } else {
                    System.err.println("  IDLE fallback ALSO NOT FOUND for key: '" + idleKey + "'. Magenta placeholder should be shown.");
                }
            }
        } else {
            String deathAnimationKey = getAnimationKey("DEATH", FacingDirection.DOWN, ToolType.NONE);
            Animation<TextureRegion> deathAnim = animations.get(deathAnimationKey);
            if (deathAnim != null) {
                currentFrame = deathAnim.getKeyFrame(stateTime, false);
            }  else if (currentFrame == null) {
                System.err.println("CRITICAL: Missing DEATH animation and currentFrame is null. Setting emergency BLACK placeholder for dead player.");
                Pixmap pixmap = new Pixmap(FRAME_WIDTH, FRAME_HEIGHT, Pixmap.Format.RGBA8888);
                pixmap.setColor(Color.BLACK); pixmap.fill();
                this.currentFrame = new TextureRegion(new Texture(pixmap));
                pixmap.dispose();
            }
        }
    }

    public String getCurrentAnimationKey() {
        if (stateMachine == null) {
            System.err.println("CRITICAL: stateMachine is null in getCurrentAnimationKey!");
            return getAnimationKey("IDLE", FacingDirection.SOUTH, ToolType.NONE);
        }
        if (!isAlive() && stateMachine.isInState(PlayerDeathState.class)) {
            return getAnimationKey("DEATH", FacingDirection.DOWN, ToolType.NONE);
        }

        String stateName = "IDLE";
        State<? extends Unit> currentStateObj = stateMachine.getCurrentState();

        if (currentStateObj == null) {
            return getAnimationKey("IDLE", currentFacingDirection, currentTool);
        }

        if (currentStateObj instanceof PlayerIdleState) stateName = "IDLE";
        else if (currentStateObj instanceof PlayerWalkState) stateName = "WALK";
        else if (currentStateObj instanceof PlayerAttackState) stateName = "ATTACK";
        else if (currentStateObj instanceof PlayerMiningState) stateName = "MINE";
        else if (currentStateObj instanceof PlayerWoodChopState) stateName = "WOODCHOP";
        else if (currentStateObj instanceof PlayerDiggingState) stateName = "DIG";
        else if (currentStateObj instanceof PlayerBowShotState) stateName = "ATTACK_BOW";
        else if (currentStateObj instanceof PlayerHurtState) stateName = "HURT";

        return getAnimationKey(stateName, currentFacingDirection, currentTool);
    }


    @Override
    public void render(SpriteBatch batch) {
        if (currentFrame != null) {
            batch.draw(currentFrame,
                position.x - FRAME_WIDTH / 2f,
                position.y,
                FRAME_WIDTH, FRAME_HEIGHT);
        } else {
            System.err.println("CRITICAL: Player.currentFrame is NULL at render time! Position: " + position.x + "," + position.y);
        }
    }

    public void levelUp() {
        level++;
        skillPoints += SKILL_POINTS_PER_LEVEL;
        experience = experience - experienceToNextLevel;
        experienceToNextLevel = calculateExpToNextLevel(level);
        refreshDependentStats();
        health = maxHealth;
        mana = maxMana;
        System.out.println(name + " reached Level " + level + "! You have " + skillPoints + " skill points.");
    }

    private int calculateExpToNextLevel(int currentLevel) {
        return (int) (Math.pow(currentLevel, 1.5) * 100 + 50 * currentLevel);
    }

    public void addItemToInventory(Item item) {
        inventory.add(item);
        System.out.println(item.getNameKey() + " added to inventory.");
    }

    public void removeItemFromInventory(Item item) {
        if (inventory.removeValue(item, true)) {
            System.out.println(item.getNameKey() + " removed from inventory.");
        }
    }

    public void allocateSkillPoints(StatType stat, int points) {
        if (stat == StatType.SPIRITUALITY) {
            System.out.println("Spirituality cannot be increased with skill points.");
            return;
        }
        if (points <= 0) {
            System.out.println("Points to allocate must be positive.");
            return;
        }
        if (skillPoints >= points) {
            stats.addPoints(stat, points);
            skillPoints -= points;
            refreshDependentStats();
            System.out.println("Allocated " + points + " to " + stat.getLocalizationKey() + ". Skill points remaining: " + skillPoints);
        } else {
            System.out.println("Not enough skill points to allocate " + points + " to " + stat.getLocalizationKey());
        }
    }

    public Array<Item> getInventory() { return inventory; }
    public int getExperienceToNextLevel() { return experienceToNextLevel; }
    public int getSkillPoints() { return skillPoints; }
    public FacingDirection getCurrentFacingDirection() { return currentFacingDirection; }
    public void setCurrentFacingDirection(FacingDirection facingDirection) { this.currentFacingDirection = facingDirection; }
    public ObjectMap<String, Animation<TextureRegion>> getAnimations() { return animations; }
    public float getStateTime() { return stateTime; }
    public void setStateTime(float time) { this.stateTime = time; }

    public ToolType getCurrentTool() { return currentTool; }
    public void setCurrentTool(ToolType tool) { this.currentTool = tool; }


    @Override
    public String toJson() {
        return super.toJson();
    }

    @Override
    public void fromJson(String json) {
        super.fromJson(json);
    }
}
