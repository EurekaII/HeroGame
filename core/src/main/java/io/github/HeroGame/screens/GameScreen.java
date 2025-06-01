package io.github.HeroGame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.HeroGame.MyGame;
import io.github.HeroGame.entities.Player; // Import Player
import io.github.HeroGame.save.GameData;
import io.github.HeroGame.save.GameSaveSystem;
import io.github.HeroGame.world.NeighborCombination;
import io.github.HeroGame.world.TileType;

public class GameScreen extends BaseScreen implements Disposable, InputProcessor {

    private static final Logger log = new Logger(GameScreen.class.getSimpleName(), Logger.DEBUG);

    private static final int TILE_SIZE = 32;
    private int mapWidthTiles;
    private int mapHeightTiles;
    private GameSaveSystem saveSystem;

    private OrthographicCamera worldCamera;
    private Viewport worldViewport;

    private TileType[][] worldGridData;

    private ObjectMap<String, TextureRegion> individualTileTextures;
    private ObjectMap<NeighborCombination, TextureRegion> tileLookupMap;

    private TiledMap tiledMapSource;
    private Skin skin;

    private boolean isPaused;
    private Table pauseTable;

    private boolean paintModeActive;
    private TileType currentPaintTileType;

    private Player player; // Instancja gracza


    public GameScreen(final MyGame game, TiledMap tiledMapSource) {
        super(game);
        log.debug("GameScreen constructor called for NEW GAME (TiledMap).");

        this.tiledMapSource = tiledMapSource;
        initializeWorldGridFromTiledMap();

        loadIndividualTileTextures();
        worldCamera = new OrthographicCamera();
        // Dostosuj viewport, aby lepiej pasował do widoku postaci, np. mniejszy obszar
        // worldViewport = new FitViewport(800, 480, worldCamera); // Przykładowy rozmiar
        worldViewport = new FitViewport(mapWidthTiles * TILE_SIZE, mapHeightTiles * TILE_SIZE, worldCamera); // Na razie zostawmy pełny
        worldViewport.apply(true);


        populateTileLookup();

        this.skin = game.getSkin();
        this.saveSystem = game.getGameSaveSystem();

        // Inicjalizacja gracza
        // Pozycja startowa gracza - np. środek mapy lub zdefiniowany punkt startowy
        Vector2 playerStartPos = new Vector2(mapWidthTiles * TILE_SIZE / 2f, mapHeightTiles * TILE_SIZE / 2f);
        this.player = new Player(playerStartPos);
        log.info("Player initialized at: " + playerStartPos);


        isPaused = false;
        paintModeActive = false;
        currentPaintTileType = TileType.GRASS;

        // Ustawienie kamery na gracza
        worldCamera.position.set(player.getPosition().x, player.getPosition().y, 0);
        worldCamera.update();
    }

    public GameScreen(final MyGame game, GameData loadedGameData) {
        super(game);
        log.debug("GameScreen constructor called for LOADED GAME (GameData).");

        if (loadedGameData == null || loadedGameData.getWorldGridData() == null || loadedGameData.getWorldGridData().length == 0 || loadedGameData.getWorldGridData()[0].length == 0) {
            log.error("Loaded GameData is invalid or empty. Creating a default map.");
            this.mapWidthTiles = 30;
            this.mapHeightTiles = 20;
            this.worldGridData = new TileType[mapWidthTiles][mapHeightTiles];
            for (int x = 0; x < mapWidthTiles; x++) {
                for (int y = 0; y < mapHeightTiles; y++) {
                    this.worldGridData[x][y] = TileType.DIRT;
                }
            }
        } else {
            this.worldGridData = loadedGameData.getWorldGridData();
            this.mapWidthTiles = loadedGameData.getMapWidthTiles();
            this.mapHeightTiles = loadedGameData.getMapHeightTiles();
            log.info("Loaded world grid with dimensions: " + mapWidthTiles + "x" + mapHeightTiles);
        }

        this.tiledMapSource = null;
        loadIndividualTileTextures();
        worldCamera = new OrthographicCamera();
        worldViewport = new FitViewport(mapWidthTiles * TILE_SIZE, mapHeightTiles * TILE_SIZE, worldCamera);
        worldViewport.apply(true);

        populateTileLookup();

        this.skin = game.getSkin();
        this.saveSystem = game.getGameSaveSystem();

        // TODO: Wczytaj stan gracza z loadedGameData
        Vector2 playerStartPos = new Vector2(mapWidthTiles * TILE_SIZE / 2f, mapHeightTiles * TILE_SIZE / 2f); // Domyślna pozycja
        // if (loadedGameData != null && loadedGameData.getPlayerData() != null) {
        //     this.player = new Player(new Vector2(0,0)); // Tymczasowa pozycja, zostanie nadpisana
        //     this.player.fromJson(loadedGameData.getPlayerData()); // Załóżmy, że GameData ma getPlayerData()
        //     playerStartPos.set(player.getPosition());
        //     log.info("Player loaded from save data at: " + player.getPosition());
        // } else {
        this.player = new Player(playerStartPos);
        log.info("Player initialized at: " + playerStartPos + " (no save data for player or new game).");
        // }


        isPaused = false;
        paintModeActive = false;
        currentPaintTileType = TileType.GRASS;

        worldCamera.position.set(player.getPosition().x, player.getPosition().y, 0);
        worldCamera.update();
    }

    private void initializeWorldGridFromTiledMap() {
        if (tiledMapSource == null) {
            log.error("CRITICAL: TiledMap source is null. Cannot initialize world grid from Tiled map. Using default empty map.");
            mapWidthTiles = 30;
            mapHeightTiles = 20;
            worldGridData = new TileType[mapWidthTiles][mapHeightTiles];
            for (int x = 0; x < mapWidthTiles; x++) {
                for (int y = 0; y < mapHeightTiles; y++) {
                    worldGridData[x][y] = TileType.DIRT;
                }
            }
            return;
        }

        TiledMapTileLayer worldGridLayer = (TiledMapTileLayer) tiledMapSource.getLayers().get("WorldGridLayer");

        if (worldGridLayer == null) {
            log.error("CRITICAL: TiledMap layer 'WorldGridLayer' not found! Using default empty map.");
            mapWidthTiles = 30;
            mapHeightTiles = 20;
            worldGridData = new TileType[mapWidthTiles][mapHeightTiles];
            for (int x = 0; x < mapWidthTiles; x++) {
                for (int y = 0; y < mapHeightTiles; y++) {
                    worldGridData[x][y] = TileType.DIRT;
                }
            }
            return;
        }

        mapWidthTiles = worldGridLayer.getWidth();
        mapHeightTiles = worldGridLayer.getHeight();
        worldGridData = new TileType[mapWidthTiles][mapHeightTiles];

        for (int x = 0; x < mapWidthTiles; x++) {
            for (int y = 0; y < mapHeightTiles; y++) {
                TiledMapTileLayer.Cell cell = worldGridLayer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    MapProperties properties = cell.getTile().getProperties();
                    String typeString = properties.get("tileTypeString", String.class);
                    if (typeString != null) {
                        try {
                            worldGridData[x][y] = TileType.valueOf(typeString.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            log.error("Unknown TileType in Tiled map at " + x + "," + y + ": " + typeString + ". Defaulting to NONE.");
                            worldGridData[x][y] = TileType.NONE;
                        }
                    } else {
                        log.error("Tile at " + x + "," + y + " has no 'tileTypeString' property. Defaulting to NONE.");
                        worldGridData[x][y] = TileType.NONE;
                    }
                } else {
                    worldGridData[x][y] = TileType.NONE;
                }
            }
        }
        log.info("World Grid loaded from Tiled map. Dimensions: " + mapWidthTiles + "x" + mapHeightTiles);
    }


    private void loadIndividualTileTextures() {
        individualTileTextures = new ObjectMap<>();
        String basePath = "assets/tiles/"; // Upewnij się, że ta ścieżka jest poprawna

        String[] tileNames = {
            "grass_gggg.png", "grass_dggg.png", "grass_gdgg.png", "grass_ggdg.png", "grass_gggd.png",
            "grass_ddgg.png", "grass_ggdd.png", "grass_dgdg.png", "grass_gddg.png",
            "grass_dg_gd.png", "grass_gd_dg.png",
            "grass_dddg.png", "grass_ddgd.png", "grass_dgdd.png", "grass_gddd.png",
            "grass_dddd.png",
            "dirt_base.png"
        };

        for (String name : tileNames) {
            try {
                Texture texture = new Texture(Gdx.files.internal(basePath + name));
                individualTileTextures.put(name.replace(".png", ""), new TextureRegion(texture));
                log.debug("Loaded individual tile: " + name);
            } catch (Exception e) {
                log.error("Failed to load individual tile: " + basePath + name, e);
            }
        }
    }


    private void populateTileLookup() {
        tileLookupMap = new ObjectMap<>();

        java.util.function.Function<String, TextureRegion> getTileRegion = (name) -> {
            TextureRegion region = individualTileTextures.get(name);
            if (region == null) {
                log.error("TextureRegion for '" + name + "' not found in individualTileTextures map!");
                // Fallback to a default texture if available, or handle error
                TextureRegion fallback = individualTileTextures.get("dirt_base"); // Przykładowy fallback
                if (fallback == null) {
                    log.error("Fallback texture 'dirt_base' also not found!");
                }
                return fallback;
            }
            return region;
        };

        tileLookupMap.put(new NeighborCombination(TileType.DIRT, TileType.DIRT, TileType.DIRT, TileType.DIRT, TileType.DIRT), getTileRegion.apply("dirt_base"));

        tileLookupMap.put(new NeighborCombination(TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS), getTileRegion.apply("grass_gggg"));
        tileLookupMap.put(new NeighborCombination(TileType.DIRT, TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS), getTileRegion.apply("grass_dggg"));
        tileLookupMap.put(new NeighborCombination(TileType.GRASS, TileType.DIRT, TileType.GRASS, TileType.GRASS, TileType.GRASS), getTileRegion.apply("grass_gdgg"));
        tileLookupMap.put(new NeighborCombination(TileType.GRASS, TileType.GRASS, TileType.DIRT, TileType.GRASS, TileType.GRASS), getTileRegion.apply("grass_ggdg"));
        tileLookupMap.put(new NeighborCombination(TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.DIRT, TileType.GRASS), getTileRegion.apply("grass_gggd"));
        tileLookupMap.put(new NeighborCombination(TileType.DIRT, TileType.DIRT, TileType.GRASS, TileType.GRASS, TileType.GRASS), getTileRegion.apply("grass_ddgg"));
        tileLookupMap.put(new NeighborCombination(TileType.GRASS, TileType.GRASS, TileType.DIRT, TileType.DIRT, TileType.GRASS), getTileRegion.apply("grass_ggdd"));
        tileLookupMap.put(new NeighborCombination(TileType.DIRT, TileType.GRASS, TileType.DIRT, TileType.GRASS, TileType.GRASS), getTileRegion.apply("grass_dgdg"));
        tileLookupMap.put(new NeighborCombination(TileType.GRASS, TileType.DIRT, TileType.GRASS, TileType.DIRT, TileType.GRASS), getTileRegion.apply("grass_gddg"));
        tileLookupMap.put(new NeighborCombination(TileType.DIRT, TileType.GRASS, TileType.GRASS, TileType.DIRT, TileType.GRASS), getTileRegion.apply("grass_dg_gd"));
        tileLookupMap.put(new NeighborCombination(TileType.GRASS, TileType.DIRT, TileType.DIRT, TileType.GRASS, TileType.GRASS), getTileRegion.apply("grass_gd_dg"));
        tileLookupMap.put(new NeighborCombination(TileType.DIRT, TileType.DIRT, TileType.DIRT, TileType.GRASS, TileType.GRASS), getTileRegion.apply("grass_dddg"));
        tileLookupMap.put(new NeighborCombination(TileType.DIRT, TileType.DIRT, TileType.GRASS, TileType.DIRT, TileType.GRASS), getTileRegion.apply("grass_ddgd"));
        tileLookupMap.put(new NeighborCombination(TileType.DIRT, TileType.GRASS, TileType.DIRT, TileType.DIRT, TileType.GRASS), getTileRegion.apply("grass_dgdd"));
        tileLookupMap.put(new NeighborCombination(TileType.GRASS, TileType.DIRT, TileType.DIRT, TileType.DIRT, TileType.GRASS), getTileRegion.apply("grass_gddd"));
        tileLookupMap.put(new NeighborCombination(TileType.DIRT, TileType.DIRT, TileType.DIRT, TileType.DIRT, TileType.GRASS), getTileRegion.apply("grass_dddd"));
    }

    @Override
    public void show() {
        super.show();
        // InputMultiplexer pozwala na obsługę inputu przez wiele obiektów (np. stage dla UI i GameScreen dla świata gry)
        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputMultiplexer(stage, this));
        rebuildUI();
    }

    @Override
    public void render(float delta) {

        if (!isPaused) {
            // Aktualizacja logiki gracza
            if (player != null) {
                player.update(delta);
            }
            // handleInput(delta); // Sterowanie kamerą, jeśli potrzebne niezależnie od gracza
            updateCamera(); // Kamera podąża za graczem
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        worldViewport.apply();
        batch.setProjectionMatrix(worldCamera.combined);

        batch.begin();

        // Renderowanie mapy (kafelków)
        for (int x = 0; x < mapWidthTiles; x++) {
            for (int y = 0; y < mapHeightTiles; y++) {
                TextureRegion baseTile = tileLookupMap.get(new NeighborCombination(TileType.DIRT, TileType.DIRT, TileType.DIRT, TileType.DIRT, TileType.DIRT));
                if (baseTile != null) {
                    batch.draw(baseTile, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
        for (int dx = 0; dx <= mapWidthTiles; dx++) {
            for (int dy = 0; dy <= mapHeightTiles; dy++) {
                TileType bl = getTileTypeForDualGrid(dx, dy);
                TileType br = getTileTypeForDualGrid(dx + 1, dy);
                TileType tl = getTileTypeForDualGrid(dx, dy + 1);
                TileType tr = getTileTypeForDualGrid(dx + 1, dy + 1);

                if (bl == TileType.GRASS || br == TileType.GRASS || tl == TileType.GRASS || tr == TileType.GRASS) {
                    NeighborCombination grassCombination = new NeighborCombination(bl, br, tl, tr, TileType.GRASS);
                    TextureRegion tileGraphic = tileLookupMap.get(grassCombination);

                    if (tileGraphic != null) {
                        batch.draw(tileGraphic, dx * TILE_SIZE, dy * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }

        // Renderowanie gracza
        if (player != null) {
            player.render(batch);
        }
        // TODO: Renderowanie innych jednostek (NPC, Enemy)

        batch.end();

        if (isPaused || hasVisibleDialogs()) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            stage.getViewport().apply();
            batch.setProjectionMatrix(stage.getCamera().combined);
            batch.begin();
            batch.setColor(new Color(0f, 0f, 0f, 0.7f));
            // Poprawka: użyj wymiarów viewportu sceny
            batch.draw(
                game.getSkin().getRegion("white"),
                0, 0,
                stage.getViewport().getWorldWidth(),
                stage.getViewport().getWorldHeight()
            );
            batch.setColor(Color.WHITE);
            batch.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        stage.act(delta);
        stage.draw();
    }

    private void updateCamera() {
        if (player != null) {
            // Kamera płynnie podąża za graczem
            float lerp = 0.1f; // Współczynnik interpolacji dla płynnego ruchu kamery
            worldCamera.position.x += (player.getPosition().x - worldCamera.position.x) * lerp;
            worldCamera.position.y += (player.getPosition().y - worldCamera.position.y) * lerp;

            // Ograniczenie kamery do granic mapy
            float camHalfWidth = worldCamera.viewportWidth * worldCamera.zoom / 2;
            float camHalfHeight = worldCamera.viewportHeight * worldCamera.zoom / 2;

            worldCamera.position.x = Math.max(camHalfWidth, worldCamera.position.x);
            worldCamera.position.x = Math.min(mapWidthTiles * TILE_SIZE - camHalfWidth, worldCamera.position.x);
            worldCamera.position.y = Math.max(camHalfHeight, worldCamera.position.y);
            worldCamera.position.y = Math.min(mapHeightTiles * TILE_SIZE - camHalfHeight, worldCamera.position.y);

            worldCamera.update();
        }
    }


    private TileType getTileTypeForDualGrid(int x, int y) {
        if (x >= 0 && x < mapWidthTiles && y >= 0 && y < mapHeightTiles) {
            return worldGridData[x][y];
        }
        return TileType.DIRT; // Domyślny typ poza granicami dla dual grid
    }

    private void processTileChange(int screenX, int screenY, TileType targetType) {
        Vector3 worldCoords = worldCamera.unproject(new Vector3(screenX, screenY, 0));
        int tileX = (int) Math.floor(worldCoords.x / TILE_SIZE);
        int tileY = (int) Math.floor(worldCoords.y / TILE_SIZE);

        if (tileX >= 0 && tileX < mapWidthTiles && tileY >= 0 && tileY < mapHeightTiles) {
            if (worldGridData[tileX][tileY] != targetType) {
                worldGridData[tileX][tileY] = targetType;
                log.info("Changed tile " + tileX + "," + tileY + " to " + targetType);
            }
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        // Przekazanie inputu do stanów gracza jest obsługiwane przez Gdx.input.isKeyPressed w stanach
        // Tutaj obsługujemy globalne akcje jak pauza, tryb malowania
        if (keycode == Input.Keys.ESCAPE) {
            togglePause();
            return true;
        }
        if (isPaused) return false; // Nie przetwarzaj innych klawiszy, gdy gra jest zapauzowana

        if (keycode == Input.Keys.T) {
            paintModeActive = !paintModeActive;
            Gdx.app.log("GameScreen", "Paint mode: " + (paintModeActive ? "ACTIVE" : "INACTIVE"));
            return true;
        }
        if (paintModeActive) { // Klawisze do malowania tylko w trybie malowania
            if (keycode == Input.Keys.NUM_1) {
                currentPaintTileType = TileType.GRASS;
                Gdx.app.log("GameScreen", "Current paint tile type: GRASS");
                return true;
            }
            if (keycode == Input.Keys.NUM_2) {
                currentPaintTileType = TileType.DIRT;
                Gdx.app.log("GameScreen", "Current paint tile type: DIRT");
                return true;
            }
        }
        // Inne klawisze (np. otwieranie ekwipunku, mapy) mogą być tutaj
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!isPaused && paintModeActive) {
            if (button == Input.Buttons.LEFT) {
                processTileChange(screenX, screenY, currentPaintTileType);
                return true;
            } else if (button == Input.Buttons.RIGHT) {
                processTileChange(screenX, screenY, TileType.DIRT); // Prawy przycisk zawsze maluje ziemią
                return true;
            }
        }
        // TODO: Obsługa kliknięć dla interakcji ze światem/NPC (jeśli nie jest to paintMode)
        return false; // Pozwól stage'owi obsłużyć kliknięcia, jeśli to nie było malowanie
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!isPaused && paintModeActive) {
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                processTileChange(screenX, screenY, currentPaintTileType);
                return true;
            } else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
                processTileChange(screenX, screenY, TileType.DIRT);
                return true;
            }
        }
        return false;
    }

    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (!isPaused) {
            float zoomAmount = amountY * 0.1f; // amountY jest -1 lub 1
            worldCamera.zoom += zoomAmount;
            worldCamera.zoom = Math.max(0.5f, Math.min(2.0f, worldCamera.zoom)); // Ograniczenie zoomu
            // worldCamera.update(); // update jest już w updateCamera()
            log.debug("Camera zoom: " + worldCamera.zoom);
            return true;
        }
        return false;
    }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }

    // Metoda handleInput została zastąpiona przez logikę w stanach gracza i updateCamera()
    // private void handleInput(float delta) { ... }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height); // Ważne dla stage
        worldViewport.update(width, height, true); // true oznacza centrowanie kamery
        log.debug("GameScreen resized to: " + width + "x" + height + ". WorldViewport updated.");
    }

    @Override
    public void dispose() {
        log.debug("Disposing GameScreen...");
        if (tiledMapSource != null) {
            tiledMapSource.dispose();
        }
        for (TextureRegion region : individualTileTextures.values()) {
            if (region != null && region.getTexture() != null) { // Sprawdzenie nulla dla regionu
                region.getTexture().dispose();
            }
        }
        individualTileTextures.clear();
        log.debug("Individual tile textures disposed.");

        if (player != null) {
            // Jeśli Player ma zasoby do zwolnienia (np. tekstury ładowane tylko przez niego)
            // player.dispose();
        }

        if (pauseTable != null) {
            pauseTable.remove(); // Usuń ze stage'a, aby uniknąć problemów
        }

        super.dispose(); // Ważne dla stage
    }

    @Override
    protected void rebuildUI() {
        log.debug("Rebuilding GameScreen UI (pause menu)...");
        stage.clear(); // Wyczyść stary stage
        createPauseMenu(); // Stwórz menu pauzy
        if (isPaused && pauseTable != null) { // Dodaj menu pauzy tylko jeśli gra jest zapauzowana
            stage.addActor(pauseTable);
        }
    }

    private void createPauseMenu() {
        pauseTable = new Table(game.getSkin());
        pauseTable.setFillParent(true);
        // pauseTable.setDebug(true); // Do debugowania układu

        I18NBundle bundle = game.getI18nBundle();

        Label titleLabel = new Label(bundle.get("pauseMenuTitle"), game.getSkin(), "default-label"); // Użyj odpowiedniego stylu
        pauseTable.add(titleLabel).padBottom(50).colspan(2).center().row();

        float buttonWidth = 350f;
        float buttonPad = 15f;

        TextButton resumeButton = new TextButton(bundle.get("resumeGame"), game.getSkin(), "default");
        TextButton saveButton = new TextButton(bundle.get("saveGame"), game.getSkin(), "default");
        TextButton loadButton = new TextButton(bundle.get("loadGame"), game.getSkin(), "default");
        TextButton optionsButton = new TextButton(bundle.get("options"), game.getSkin(), "default");
        TextButton exitToMainMenuButton = new TextButton(bundle.get("exitToMainMenu"), game.getSkin(), "default");
        TextButton exitToDesktopButton = new TextButton(bundle.get("exitGame"), game.getSkin(), "default");

        pauseTable.add(resumeButton).width(buttonWidth).pad(buttonPad).colspan(2).center().row();
        pauseTable.add(saveButton).width(buttonWidth).pad(buttonPad).colspan(2).center().row();
        pauseTable.add(loadButton).width(buttonWidth).pad(buttonPad).colspan(2).center().row();
        pauseTable.add(optionsButton).width(buttonWidth).pad(buttonPad).colspan(2).center().row();
        pauseTable.add(exitToMainMenuButton).width(buttonWidth).pad(buttonPad).colspan(2).center().row();
        pauseTable.add(exitToDesktopButton).width(buttonWidth).pad(buttonPad).colspan(2).center().row();


        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                log.info("Resume button clicked.");
                togglePause();
            }
        });

        saveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                log.info("Save Game button clicked. Showing SaveGameDialog.");
                // TODO: Zaktualizuj GameData o stan gracza
                GameData currentData = new GameData(worldGridData, mapWidthTiles, mapHeightTiles /*, player.toJson() */);
                SaveGameDialog saveDialog = new SaveGameDialog(game, saveSystem, currentData, new SaveGameDialog.SaveGameCallback() {
                    @Override
                    public void onSaveSuccess(String message) {
                        log.info("Save operation successful: " + message);
                        showTemporaryMessageWithUnpause(message, "save_success_title");
                    }

                    @Override
                    public void onBack() {
                        log.info("Save dialog back button clicked.");
                    }

                    @Override
                    public void onError(String message) {
                        showErrorDialog(message);
                    }
                }) {

                    protected void confirmOverwrite(String fileName, Runnable saveAction) {
                        ((GameScreen) game.getScreen()).showOverwriteConfirmation(fileName, saveAction);
                    }
                };
                saveDialog.show(stage);
            }
        });

        loadButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                log.info("Load Game button clicked. Showing LoadGameDialog.");
                LoadGameDialog loadDialog = new LoadGameDialog(game, saveSystem, new LoadGameDialog.LoadGameCallback() {
                    @Override
                    public void onLoad(GameData loadedGameData) {
                        if (loadedGameData != null) {
                            log.info("Game loaded successfully. Replacing current GameScreen.");
                            game.setScreen(new GameScreen(game, loadedGameData)); // Przekaż wczytane dane
                        } else {
                            log.error("Failed to load game: loadedData is null.");
                            showErrorDialog(bundle.get("save_load_error_generic"));
                        }
                    }

                    @Override
                    public void onBack() {
                        log.info("Load dialog back button clicked.");
                    }

                    @Override
                    public void onError(String message) {
                        showErrorDialog(message);
                    }
                });
                loadDialog.show(stage);
            }
        });

        optionsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                log.info("Options button clicked from Pause Menu. Showing OptionsDialog.");
                OptionsDialog dialog = new OptionsDialog(game, game.getSettingsManager(), new OptionsDialog.OptionsCallback() {
                    @Override
                    public void onApply() {
                        log.info("Options applied. UI might rebuild if language changed.");
                        // rebuildUI(); // Odkomentuj, jeśli zmiana opcji wymaga przebudowy UI natychmiast
                    }

                    @Override
                    public void onBack() {
                        log.info("Options dialog back button clicked.");
                    }
                });
                dialog.show(stage);
            }
        });

        exitToMainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                log.info("Exit to Main Menu button clicked.");
                game.setScreen(new MainMenuScreen(game)); // Załóżmy, że MainMenuScreen istnieje
            }
        });

        exitToDesktopButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                log.info("Exit to Desktop button clicked. Exiting application.");
                Gdx.app.exit();
            }
        });
    }

    private boolean hasVisibleDialogs() {
        for (Actor actor : stage.getActors()) {
            if (actor instanceof Dialog) {
                Dialog dialog = (Dialog) actor;
                // Sprawdź, czy dialog jest częścią sceny i jest widoczny
                if (dialog.getStage() != null && dialog.isVisible()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            if (pauseTable == null) createPauseMenu(); // Stwórz menu, jeśli nie istnieje
            stage.addActor(pauseTable); // Dodaj menu pauzy do sceny
            log.info("Game paused.");
        } else {
            if (pauseTable != null) pauseTable.remove(); // Usuń menu pauzy ze sceny
            log.info("Game resumed.");
        }
        // Upewnij się, że input jest poprawnie ustawiony
        // Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));
    }


    private void setupDialogContent(Dialog dialog, String title, String message, String[] buttonTexts, Runnable[] buttonActions) {
        dialog.getContentTable().clear(); // Wyczyść poprzednią zawartość
        dialog.getButtonTable().clear();  // Wyczyść poprzednie przyciski

        // Ustawienie tytułu dialogu (jeśli skin obsługuje tytuły w ten sposób)
        // dialog.getTitleLabel().setText(title); // Standardowy sposób, jeśli dialog ma tytuł

        // Alternatywnie, dodaj Label jako część contentTable, jeśli styl tego wymaga
        Label titleLabel = new Label(title, game.getSkin(), "default-label"); // Użyj odpowiedniego stylu
        titleLabel.setWrap(true); // Pozwól na zawijanie tekstu
        dialog.getContentTable().add(titleLabel).width(Gdx.graphics.getWidth() * 0.6f).pad(10).row();


        Label messageLabel = new Label(message, game.getSkin(), "default-label"); // Użyj odpowiedniego stylu
        messageLabel.setWrap(true);
        dialog.getContentTable().add(messageLabel).width(Gdx.graphics.getWidth() * 0.6f).pad(10).row();

        for (int i = 0; i < buttonTexts.length; i++) {
            TextButton button = new TextButton(buttonTexts[i], game.getSkin(), "default"); // Użyj odpowiedniego stylu
            final int idx = i; // Potrzebne dla lambdy/klasy anonimowej
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (buttonActions[idx] != null) {
                        buttonActions[idx].run();
                    }
                    dialog.hide(); // Zawsze ukrywaj dialog po kliknięciu przycisku
                }
            });
            dialog.getButtonTable().add(button).width(120).pad(10); // Dostosuj szerokość i padding
        }
        dialog.pack(); // Dopasuj rozmiar dialogu do zawartości
        // Wyśrodkuj dialog na ekranie
        dialog.setPosition(
            Math.round((stage.getWidth() - dialog.getWidth()) / 2),
            Math.round((stage.getHeight() - dialog.getHeight()) / 2)
        );
    }

    private void showTemporaryMessageWithUnpause(String message, String titleBundleKey) {
        I18NBundle i18n = game.getI18nBundle();
        String title = i18n.get(titleBundleKey);

        Dialog dialog = new Dialog("", game.getSkin()) { // Pusty tytuł, bo dodajemy go w setupDialogContent
            @Override
            protected void result(Object object) { // Ta metoda jest wywoływana, gdy dialog jest zamykany (np. przez przycisk)
                this.hide(); // Upewnij się, że dialog jest ukryty
                if (GameScreen.this.isPaused) { // Odpauzuj tylko jeśli była pauza
                    GameScreen.this.isPaused = false;
                    if (GameScreen.this.pauseTable != null) {
                        GameScreen.this.pauseTable.remove();
                    }
                    log.info("Game resumed after message confirmation.");
                    // Gdx.input.setInputProcessor(new InputMultiplexer(stage, GameScreen.this)); // Ustawione w show() i togglePause()
                }
            }
        };

        setupDialogContent(dialog, title, message, new String[]{i18n.get("ok")}, new Runnable[]{null});
        dialog.show(stage);
    }

    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        I18NBundle bundle = game.getI18nBundle();
        Dialog dialog = new Dialog("", skin); // Pusty tytuł
        // Table contentTable = new Table(skin); // Niepotrzebne, setupDialogContent tworzy strukturę

        // Label label = new Label(message, skin);
        // label.setWrap(true);
        // contentTable.add(label).width(320).pad(12);
        // dialog.getContentTable().add(contentTable).width(340).pad(10).row();

        // TextButton yesButton = new TextButton(bundle.get("yes"), skin);
        // TextButton noButton = new TextButton(bundle.get("no"), skin);

        // yesButton.addListener(new ClickListener() {
        //     @Override public void clicked(InputEvent event, float x, float y) {
        //         if (onConfirm != null) onConfirm.run();
        //         dialog.hide();
        //     }
        // });
        // noButton.addListener(new ClickListener() {
        //     @Override public void clicked(InputEvent event, float x, float y) {
        //         dialog.hide();
        //     }
        // });

        // dialog.getButtonTable().add(yesButton).width(110).pad(6);
        // dialog.getButtonTable().add(noButton).width(110).pad(6);
        setupDialogContent(dialog, title, message,
            new String[]{bundle.get("yes"), bundle.get("no")},
            new Runnable[]{onConfirm, null} // null dla "No" oznacza tylko zamknięcie dialogu
        );
        dialog.show(stage);
    }

    private void showErrorDialog(String message) {
        I18NBundle i18n = game.getI18nBundle();
        Dialog errorDialog = new Dialog("", game.getSkin()) {
            @Override
            protected void result(Object object) {
                this.hide();
            }
        };
        setupDialogContent(errorDialog, i18n.get("error_title"), message, new String[]{i18n.get("ok")}, new Runnable[]{null});
        errorDialog.show(stage);
    }

    private void showTemporaryMessage(String message, String titleKey) {
        I18NBundle i18n = game.getI18nBundle();
        String title = i18n.get(titleKey);
        Dialog dialog = new Dialog("", game.getSkin()) {
            @Override
            protected void result(Object object) {
                this.hide();
            }
        };
        setupDialogContent(dialog, title, message, new String[]{i18n.get("ok")}, new Runnable[]{null});
        dialog.show(stage);
    }
    private void showOverwriteConfirmation(String fileName, Runnable onConfirm) {
        I18NBundle i18n = game.getI18nBundle();
        String title = i18n.get("confirm_overwrite_title");
        String message = i18n.format("confirm_overwrite_text", fileName);
        Dialog dialog = new Dialog("", game.getSkin());
        setupDialogContent(dialog, title, message,
            new String[]{i18n.get("yes"), i18n.get("no")},
            new Runnable[]{
                onConfirm,
                null
            }
        );
        dialog.show(stage);
    }
}
