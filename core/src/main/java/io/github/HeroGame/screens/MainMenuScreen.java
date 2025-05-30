package io.github.HeroGame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Logger;
import io.github.HeroGame.MyGame; // Dodano import
import io.github.HeroGame.save.GameData;

import java.util.MissingResourceException;
import java.util.Objects;

public class MainMenuScreen extends BaseScreen {

    private final Logger log = new Logger(MainMenuScreen.class.getSimpleName(), Logger.DEBUG);
    private Table mainTable;
    private Texture backgroundTexture;
    private Texture logoTexture;

    private static final String BG_TEXTURE_PATH = "assets/textures/background.png";
    private static final String LOGO_TEXTURE_PATH = "assets/textures/logo_herogame.png";

    public MainMenuScreen(final MyGame game) {
        super(game);
        loadScreenAssets();
    }

    private void loadScreenAssets() {
        log.debug("Queueing MainMenuScreen assets...");
        if (!game.getAssetManager().isLoaded(BG_TEXTURE_PATH)) {
            game.getAssetManager().load(BG_TEXTURE_PATH, Texture.class);
            game.getAssetManager().finishLoadingAsset(BG_TEXTURE_PATH);
            log.debug("Background texture loaded.");
        }
        this.backgroundTexture = game.getAssetManager().get(BG_TEXTURE_PATH, Texture.class);

        if (!game.getAssetManager().isLoaded(LOGO_TEXTURE_PATH)) {
            game.getAssetManager().load(LOGO_TEXTURE_PATH, Texture.class);
            game.getAssetManager().finishLoadingAsset(LOGO_TEXTURE_PATH);
            log.debug("Logo texture loaded: " + LOGO_TEXTURE_PATH);
        }
        this.logoTexture = game.getAssetManager().get(LOGO_TEXTURE_PATH, Texture.class);
    }


    @Override
    public void show() {
        super.show();
        log.debug("MainMenuScreen show called.");
        rebuildUI();
    }

    @Override
    protected void rebuildUI() {
        log.debug("Rebuilding Main Menu UI...");
        stage.clear();

        Skin skin = game.getSkin();
        I18NBundle i18nBundle = game.getI18nBundle();

        log.debug("MainMenuScreen rebuildUI: Locale of current i18nBundle: " + i18nBundle.getLocale());
        log.debug("MainMenuScreen rebuildUI: Test key 'newGame' from bundle: '" + i18nBundle.get("newGame") + "'");

        mainTable = new Table();
        mainTable.setFillParent(true);

        if (backgroundTexture != null) {
            mainTable.setBackground(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
        } else {
            log.error("Background texture not loaded or not set.");
        }

        if (logoTexture != null) {
            Image logoImage = new Image(logoTexture);
            mainTable.add(logoImage).expandX().padTop(50).padBottom(50).row();
        } else {
            log.error("Logo texture not loaded. Falling back to text title.");
            Label titleLabel = new Label(i18nBundle.get("gameTitle"), skin, "default-label");
            titleLabel.setAlignment(Align.center);
            mainTable.add(titleLabel).expandX().padTop(50).padBottom(50).row();
        }

        TextButton newGameButton = createMenuButton("newGame", skin, i18nBundle);
        TextButton loadGameButton = createMenuButton("loadGame", skin, i18nBundle);
        TextButton optionsButton = createMenuButton("options", skin, i18nBundle);
        TextButton creditsButton = createMenuButton("credits", skin, i18nBundle);
        TextButton exitButton = createMenuButton("exit", skin, i18nBundle);

        float buttonWidth = 300f;
        float buttonPad = 3f;

        mainTable.add(newGameButton).width(buttonWidth).pad(buttonPad).row();
        mainTable.add(loadGameButton).width(buttonWidth).pad(buttonPad).row();
        mainTable.add(optionsButton).width(buttonWidth).pad(buttonPad).row();
        mainTable.add(creditsButton).width(buttonWidth).pad(buttonPad).row();
        mainTable.add(exitButton).width(buttonWidth).pad(buttonPad).row();

        newGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                log.info("New Game button clicked. Starting new game with default map.");
                // POPRAWKA: Jawne rzutowanie na TiledMap lub przekazanie klasy do get()
                TiledMap defaultMap = game.getAssetManager().get(MyGame.WORLD_GRID_TMX_PATH, TiledMap.class);
                game.setScreen(new GameScreen(game, defaultMap));
            }
        });

        loadGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                log.info("Load Game button clicked from Main Menu. Showing LoadGameDialog.");
                // Wyświetl LoadGameDialog tak samo jak z menu pauzy, ale callback będzie inny
                LoadGameDialog loadDialog = new LoadGameDialog(game, game.getGameSaveSystem(), new LoadGameDialog.LoadGameCallback() {
                    @Override
                    public void onLoad(GameData loadedGameData) {
                        if (loadedGameData != null) {
                            log.info("Game loaded successfully from Main Menu. Opening GameScreen...");
                            game.setScreen(new GameScreen(game, loadedGameData)); // Otwórz GameScreen z wczytanymi danymi
                        } else {
                            log.error("Failed to load game from Main Menu: loadedData is null.");
                            showErrorDialog(game.getI18nBundle().get("save_load_error_generic"));
                            // Pozostań na MainMenuScreen jeśli ładowanie się nie powiodło
                            rebuildUI(); // Odśwież UI, aby dialog zniknął i główne menu było aktywne
                        }
                    }

                    @Override
                    public void onBack() {
                        log.info("Load dialog back button clicked from Main Menu.");
                        // Po powrocie z dialogu ładowania, pozostań w głównym menu
                        rebuildUI(); // Odśwież UI, aby dialog zniknął i główne menu było aktywne
                    }

                    @Override
                    public void onError(String message) {
                        showErrorDialog(message);
                        // Po błędzie, pozostajemy w głównym menu (dialog błędu zakryje LoadGameDialog)
                        // LoadGameDialog sam się zamknie po onError, więc wystarczy odświeżyć MainMenu
                        rebuildUI();
                    }
                });
                loadDialog.show(stage); // Pokaż dialog na stage'u MainMenuScreen
            }
        });

        optionsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                log.info("Options button clicked.");
                // <<< ZMIENIONO: Przekaż aktualny ekran (MainMenuScreen) >>>
                game.setScreen(new OptionsScreen(game, MainMenuScreen.this));
            }
        });

        creditsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                log.info("Credits button clicked.");
                game.setScreen(new CreditsScreen(game));
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                log.info("Exit button clicked. Exiting application.");
                Gdx.app.exit();
            }
        });

        stage.addActor(mainTable);
        log.debug("Main Menu UI rebuilt and added to stage.");
    }

    private TextButton createMenuButton(String bundleKey, Skin skin, I18NBundle i18nBundle) {
        Objects.requireNonNull(bundleKey, "Bundle key cannot be null");
        Objects.requireNonNull(skin, "Skin cannot be null for createMenuButton");
        Objects.requireNonNull(i18nBundle, "I18NBundle cannot be null for createMenuButton");
        String text;

        try {
            text = i18nBundle.get(bundleKey);
        } catch (MissingResourceException e) {
            text = "[Missing: " + bundleKey + "]";
            log.error("Missing resource key in I18NBundle: '" + bundleKey + "'", e);
        }

        return new TextButton(text, skin, "default");
    }

    private void showErrorDialog(String message) {
        I18NBundle i18n = game.getI18nBundle();
        Dialog errorDialog = new Dialog(i18n.get("error_title"), game.getSkin());
        errorDialog.text(message, game.getSkin().get("default-label", Label.LabelStyle.class));
        TextButton.TextButtonStyle buttonStyle = game.getSkin().get("default", TextButton.TextButtonStyle.class);
        errorDialog.button(i18n.get("ok"), buttonStyle); // <-- TO JEST POPRAWNA LINIJA
        errorDialog.show(stage);
    }


    @Override
    public void render(float delta) {
        super.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        log.debug("MainMenuScreen resized.");
    }

    @Override
    public void dispose() {
        log.debug("Disposing MainMenuScreen...");
        super.dispose();
    }
}
