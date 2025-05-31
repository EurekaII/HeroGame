package io.github.HeroGame.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Logger;
import io.github.HeroGame.MyGame;
import io.github.HeroGame.world.TileType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GameSaveSystem {
    private static final String SAVE_DIR = "saves/";
    private static final String AUTOSAVE_PREFIX = "autosave_";
    private static final float AUTOSAVE_INTERVAL = 60; // 1 minuta w sekundach dla testów

    private float timeSinceLastAutosave = 0;
    private final Logger log;
    private final MyGame game;
    private final Gson gson;

    public GameSaveSystem(MyGame game) {
        this.game = Objects.requireNonNull(game, "MyGame instance cannot be null");
        this.log = new Logger(GameSaveSystem.class.getSimpleName(), Logger.DEBUG);

        this.gson = new GsonBuilder().setPrettyPrinting().create();

        FileHandle saveDir = Gdx.files.local(SAVE_DIR);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
            log.info("Created save directory: " + saveDir.path());
        }
    }

    public void update(float delta, GameData currentGameState) {
        timeSinceLastAutosave += delta;
        if (timeSinceLastAutosave >= AUTOSAVE_INTERVAL) {
            autoSave(currentGameState);
            timeSinceLastAutosave = 0;
        }
    }

    public void saveGame(String fileName, GameData dataToSave) {
        Objects.requireNonNull(dataToSave, "GameData to save cannot be null");
        String jsonText = gson.toJson(dataToSave);
        FileHandle file = Gdx.files.local(SAVE_DIR + fileName);
        file.writeString(jsonText, false);
        log.info("Game saved to: " + file.path());
    }

    public GameData loadGame(String fileName) {
        FileHandle file = Gdx.files.local(SAVE_DIR + fileName);
        if (!file.exists()) {
            log.error("Save file not found: " + file.path());
            return null;
        }
        try {
            String jsonText = file.readString();
            GameData loadedData = gson.fromJson(jsonText, GameData.class);
            log.info("Game loaded from: " + file.path());
            return loadedData;
        } catch (Exception e) {
            log.error("Failed to load game from file: " + file.path(), e);
            return null;
        }
    }

    private void autoSave(GameData currentGameState) {
        String autosaveFileName = AUTOSAVE_PREFIX + "latest.json"; // zawsze nadpisuje jeden plik!
        saveGame(autosaveFileName, currentGameState);
        log.info("Autosaved game to: " + autosaveFileName);
    }

    public FileHandle[] listSaveFiles() {
        FileHandle saveDir = Gdx.files.local(SAVE_DIR);
        if (!saveDir.exists()) return new FileHandle[0];
        return saveDir.list((dir, name) -> name.endsWith(".json"));
    }

    public boolean deleteSaveFile(String fileName) {
        FileHandle file = Gdx.files.local(SAVE_DIR + fileName);
        if (file.exists()) {
            if (fileName.startsWith(AUTOSAVE_PREFIX)) {
                log.error("Attempted to delete autosave file: " + file.path() + " - operation denied.");
                return false;
            }
            log.info("Deleting save file: " + file.path());
            return file.delete();
        }
        log.error("Attempted to delete non-existent save file: " + file.path());
        return false;
    }

    public static boolean isAutosave(FileHandle file) {
        return file.name().startsWith(AUTOSAVE_PREFIX);
    }
}
