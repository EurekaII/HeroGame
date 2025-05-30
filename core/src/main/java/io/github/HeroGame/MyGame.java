package io.github.HeroGame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.I18NBundleLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.HeroGame.managers.SettingsManager;
import io.github.HeroGame.save.GameSaveSystem; // Dodano import
import io.github.HeroGame.screens.BaseScreen;
import io.github.HeroGame.screens.LoadingScreen;
import io.github.HeroGame.screens.MainMenuScreen;

import java.util.Locale;
import java.util.Objects;

public final class MyGame extends Game {

    private static final Logger log = new Logger(MyGame.class.getSimpleName(), Logger.DEBUG);

    private final AssetManager assetManager;
    private SpriteBatch batch;
    private Skin skin;
    private SettingsManager settingsManager;
    private I18NBundle i18nBundle;
    private Cursor customCursor;
    private GameSaveSystem gameSaveSystem; // NOWE POLE: Centralna instancja GameSaveSystem

    public static final String FONT_ALKHEMIKAL = "assets/fonts/Alkhemikal.ttf";
    public static final String FONT_ALKHEMIKAL_NAME = "alkhemikal.ttf";
    public static final String BUNDLE_PATH = "assets/i18n/strings";
    public static final String SKIN_PATH = "assets/ui/uiskin.json"; // Już nie używane w obecnym kodzie, ale zostawiamy dla kontekstu
    public static final String ATLAS_PATH = "assets/ui/uiskin.atlas"; // Już nie używane w obecnym kodzie, ale zostawiamy dla kontekstu

    public static final String WORLD_GRID_TMX_PATH = "assets/maps/my_world_grid_map.tmx";

    final String POLISH_CHARACTERS = "ąćęłńóśźżĄĆĘŁŃÓŚŹŻ";

    public MyGame() {
        this.assetManager = new AssetManager();
    }


    @Override
    public void create() {
        Gdx.app.setLogLevel(Logger.ERROR); // Ustaw poziom logowania dla całej aplikacji
        log.debug("MyGame create() method started.");
        log.info("Creating game...");

        try {
            this.settingsManager = new SettingsManager(); // Inicjalizacja SettingsManager
        } catch (GdxRuntimeException e) {
            log.error("CRITICAL: Failed to initialize SettingsManager!", e);
            Gdx.app.exit(); // Wyjście z aplikacji w przypadku krytycznego błędu
            return;
        }

        // Inicjalizacja GameSaveSystem na wczesnym etapie, gdy dostępne są już podstawowe usługi (MyGame)
        this.gameSaveSystem = new GameSaveSystem(this);

        settingsManager.applyDisplayMode(); // Zastosuj ustawienia wyświetlania
        batch = new SpriteBatch(); // Inicjalizacja SpriteBatch
        configureAssetManager(); // Konfiguracja AssetManagera
        log.debug("Setting LoadingScreen...");
        setScreen(new LoadingScreen(this)); // Ustaw początkowy ekran ładowania
    }

    private void configureAssetManager() {
        log.debug("Configuring AssetManager loaders...");
        InternalFileHandleResolver resolver = new InternalFileHandleResolver();
        assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
        assetManager.setLoader(I18NBundle.class, new I18NBundleLoader(resolver));
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(resolver)); // Loader dla TiledMap
        log.debug("AssetManager configured.");
    }


    /**
     * Wywoływane przez LoadingScreen, gdy niezbędne zasoby (czcionka, skin, bundle) zostaną załadowane.
     */
    public void finishLoading() {
        log.info("Core assets loaded. Initializing Skin and Bundle.");
        Pixmap cursorPixmap = null;
        Pixmap pixmap = null;
        try {
            this.i18nBundle = assetManager.get(MyGame.BUNDLE_PATH, I18NBundle.class);
            log.debug("Loading custom cursor...");
            if (this.i18nBundle != null) {
                try {
                    cursorPixmap = new Pixmap(Gdx.files.internal("assets/textures/kursor32.png"));
                    int xHotspot = 0;
                    int yHotspot = 0;
                    customCursor = Gdx.graphics.newCursor(cursorPixmap, xHotspot, yHotspot);
                    Gdx.graphics.setCursor(customCursor);
                    log.info("Custom cursor set successfully.");
                } catch (Exception e) {
                    log.error("Failed to load or set custom cursor.", e);
                }

                // Sprawdź, czy język z ustawień pasuje do wczytanego bundle'a, jeśli nie, przeładuj
                String preferredLang = settingsManager.getLanguage();
                if (!preferredLang.equals(this.i18nBundle.getLocale().getLanguage()) && ! (this.i18nBundle.getLocale().getLanguage().isEmpty() && preferredLang.equals("en"))) {
                    log.error("MyGame.finishLoading: Preferred language (" + preferredLang + ") differs from initially loaded bundle language ("+this.i18nBundle.getLocale().getLanguage()+"). Reloading bundle.");
                    reloadI18nBundle();
                }

            } else {
                log.error("MyGame.finishLoading: CRITICAL - i18nBundle from AssetManager is NULL! Creating fallback.");
                this.i18nBundle = I18NBundle.createBundle(Gdx.files.internal(MyGame.BUNDLE_PATH), Locale.ENGLISH, "UTF-8");
            }

            log.info("MyGame.finishLoading: Final initial i18nBundle locale: " + this.i18nBundle.getLocale());
            skin = new Skin();

            // --- Bazowe białe i kolorowe tekstury ---
            pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE); pixmap.fill();
            skin.add("white", new Texture(pixmap)); // Ogólna biała tekstura
            pixmap.setColor(0.3f, 0.3f, 0.8f, 1f); pixmap.fill();
            skin.add("selectbox_background", new Texture(pixmap)); // Tło dla SelectBox/Dialogów
            pixmap.setColor(0.1f, 0.1f, 0.6f, 1f); pixmap.fill();
            skin.add("list_selection", new Texture(pixmap)); // Wybór na liście
            pixmap.setColor(0.5f, 0.5f, 1.0f, 1f); pixmap.fill();
            skin.add("scrollbar_knob", new Texture(pixmap)); // Suwak scrollbara


            // --- Czcionka ---
            // Czcionka już załadowana przez AssetManager w LoadingScreen, pobierz ją tutaj
            if (assetManager.isLoaded(FONT_ALKHEMIKAL_NAME, BitmapFont.class)) {
                skin.add(FONT_ALKHEMIKAL_NAME, assetManager.get(FONT_ALKHEMIKAL_NAME, BitmapFont.class), BitmapFont.class);
                log.debug("Font " + FONT_ALKHEMIKAL_NAME + " was loaded via AssetManager.");
            } else {
                log.error("Font " + FONT_ALKHEMIKAL_NAME + " was NOT loaded by AssetManager! Attempting fallback.");
                // Fallback: Wygeneruj czcionkę tutaj, jeśli AssetManager zawiódł
                FreeTypeFontGenerator generator = null;
                try {
                    generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_ALKHEMIKAL));
                    FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
                    param.size = 32;
                    param.borderWidth = 1;
                    param.color = Color.WHITE;
                    param.borderColor = Color.BLACK;
                    param.characters = FreeTypeFontGenerator.DEFAULT_CHARS + POLISH_CHARACTERS;
                    BitmapFont fallbackFont = generator.generateFont(param);
                    skin.add(FONT_ALKHEMIKAL_NAME, fallbackFont, BitmapFont.class);
                    log.error("Using fallback font for " + FONT_ALKHEMIKAL_NAME);
                } catch (Exception fontEx) {
                    log.error("CRITICAL: Fallback font generation also failed!", fontEx);
                    // Ostateczny fallback: Użyj domyślnej czcionki LibGDX (może wyglądać źle)
                    skin.add(FONT_ALKHEMIKAL_NAME, new BitmapFont(), BitmapFont.class);
                    log.error("Using default LibGDX font as ultimate fallback.");
                } finally {
                    if (generator != null) generator.dispose();
                }
            }

            // --- TextButtonStyle ---
            com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle textButtonStyle = new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle();
            textButtonStyle.font = skin.getFont(FONT_ALKHEMIKAL_NAME);
            textButtonStyle.up = skin.newDrawable("white", new Color(0.1f, 0.1f, 0.3f, 1f));
            textButtonStyle.down = skin.newDrawable("white", new Color(0.05f, 0.05f, 0.25f, 1f));
            textButtonStyle.over = skin.newDrawable("white", new Color(0.3f, 0.4f, 0.8f, 1f));
            skin.add("default-textbutton", textButtonStyle);


            // --- LabelStyle ---
            com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle labelStyle = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle();
            labelStyle.font = skin.getFont(FONT_ALKHEMIKAL_NAME);
            skin.add("default-label", labelStyle);

            // --- CheckBoxStyle ---
            com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle checkBoxStyle = new com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle();
            checkBoxStyle.font = skin.getFont(FONT_ALKHEMIKAL_NAME);
            checkBoxStyle.fontColor = Color.WHITE;
            int checkboxSize = 32;
            Pixmap checkboxPixmap = new Pixmap(checkboxSize, checkboxSize, Pixmap.Format.RGBA8888);
            checkboxPixmap.setColor(Color.GRAY); checkboxPixmap.fillRectangle(0, 0, checkboxSize, checkboxSize);
            checkboxPixmap.setColor(Color.LIGHT_GRAY); checkboxPixmap.drawRectangle(0, 0, checkboxSize, checkboxSize);
            skin.add("checkbox_off", new Texture(checkboxPixmap));
            checkboxPixmap.setColor(Color.LIME); checkboxPixmap.fillRectangle(0, 0, checkboxSize, checkboxSize);
            checkboxPixmap.setColor(Color.BLACK);
            checkboxPixmap.drawLine(checkboxSize / 4, checkboxSize / 2, checkboxSize / 2, checkboxSize * 3/4);
            checkboxPixmap.drawLine(checkboxSize / 2, checkboxSize * 3/4, checkboxSize * 3/4, checkboxSize / 4);
            checkboxPixmap.setColor(Color.WHITE); checkboxPixmap.drawRectangle(0, 0, checkboxSize, checkboxSize);
            skin.add("checkbox_on", new Texture(checkboxPixmap));
            checkBoxStyle.checkboxOff = skin.getDrawable("checkbox_off");
            checkBoxStyle.checkboxOn = skin.getDrawable("checkbox_on");
            skin.add("default-checkbox", checkBoxStyle);
            checkboxPixmap.dispose();

            // --- TextFieldStyle ---
            com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle textFieldStyle = new com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle();
            textFieldStyle.font = skin.getFont(FONT_ALKHEMIKAL_NAME);
            textFieldStyle.fontColor = Color.WHITE;
            textFieldStyle.background = skin.newDrawable("white", new Color(0.1f, 0.1f, 0.2f, 1f)); // ciemnoniebieskie tło
            textFieldStyle.cursor = skin.newDrawable("white", Color.WHITE); // biały kursor
            textFieldStyle.selection = skin.newDrawable("white", new Color(0.2f, 0.2f, 0.5f, 0.5f)); // półprzezroczysta niebieska selekcja
            skin.add("default-textfield", textFieldStyle); // Zarejestruj pod nazwą "default-textfield"

            // Dodanie stylu WindowStyle dla dialogów
            Window.WindowStyle windowStyle = new Window.WindowStyle();
            windowStyle.background = skin.getDrawable("selectbox_background"); // Użyj tego samego tła co selectbox
            windowStyle.titleFont = skin.getFont(FONT_ALKHEMIKAL_NAME);
            windowStyle.titleFontColor = Color.WHITE;
            skin.add("default", windowStyle);

            // SelectBox Styles
            ListStyle listStyle = new ListStyle();
            listStyle.font = skin.getFont(FONT_ALKHEMIKAL_NAME);
            listStyle.fontColorSelected = Color.BLACK;
            listStyle.fontColorUnselected = Color.WHITE;
            listStyle.selection = skin.getDrawable("list_selection");
            skin.add("default-list", listStyle);
            ScrollPaneStyle scrollPaneStyle = new ScrollPaneStyle();
            scrollPaneStyle.background = skin.getDrawable("selectbox_background");
            scrollPaneStyle.vScroll = skin.newDrawable("white", Color.DARK_GRAY);
            scrollPaneStyle.vScrollKnob = skin.getDrawable("scrollbar_knob");
            skin.add("default-scrollpane", scrollPaneStyle);
            SelectBoxStyle selectBoxStyle = new SelectBoxStyle();
            selectBoxStyle.font = skin.getFont(FONT_ALKHEMIKAL_NAME);
            selectBoxStyle.fontColor = Color.WHITE;
            selectBoxStyle.background = skin.getDrawable("selectbox_background");
            selectBoxStyle.scrollStyle = skin.get("default-scrollpane", ScrollPaneStyle.class);
            selectBoxStyle.listStyle = skin.get("default-list", ListStyle.class);
            skin.add("default-selectbox", selectBoxStyle);

            log.info("Skin, I18NBundle and Cursor initialized.");
            log.debug("Transitioning to MainMenuScreen.");
            setScreen(new MainMenuScreen(this)); // Przejdź do MainMenuScreen
        } catch (Exception e) {
            log.error("CRITICAL: Failed to initialize Skin or I18NBundle after loading!", e);
            Gdx.app.exit();
        } finally {
            if (pixmap != null) {
                pixmap.dispose();
                log.debug("Disposed reusable Pixmap.");
            }
            if (cursorPixmap != null) {
                cursorPixmap.dispose();
                log.debug("Disposed cursor Pixmap.");
            }
        }
    }


    @Override
    public void render() {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f); // Czyść ekran
        super.render(); // Wywołaj renderowanie dla aktywnego ekranu
    }

    @Override
    public void resize(int width, int height) {
        log.debug("Game resized to: " + width + "x" + height);
        if (getScreen() != null) {
            getScreen().resize(width, height); // Przekaż resize do aktywnego ekranu
        }
    }

    @Override
    public void dispose() {
        log.info("Disposing game resources...");
        if (getScreen() != null) {
            getScreen().dispose(); // Zwolnij zasoby aktywnego ekranu
        }
        if (batch != null) {
            batch.dispose();
            log.debug("SpriteBatch disposed.");
        }
        if (skin != null) {
            skin.dispose();
            log.debug("Skin disposed.");
        }
        assetManager.dispose(); // AssetManager zwolni wszystkie załadowane zasoby
        log.debug("AssetManager disposed.");

        if (customCursor != null) {
            customCursor.dispose();
            log.debug("Custom cursor disposed.");
        }
        log.info("Game disposed.");
    }


    // --- Metody pomocnicze ---

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public Skin getSkin() {
        if (skin == null) {
            log.error("Attempted to get Skin before it was initialized!");
            throw new IllegalStateException("Skin not initialized. Ensure LoadingScreen ran successfully.");
        }
        return skin;
    }

    public SettingsManager getSettingsManager() {
        if (settingsManager == null) {
            log.error("Attempted to get SettingsManager before it was initialized in create()!");
            throw new IllegalStateException("SettingsManager not initialized yet.");
        }
        return settingsManager;
    }

    public I18NBundle getI18nBundle() {
        if (i18nBundle == null) {
            log.error("Attempted to get I18NBundle before it was loaded! Trying to load default synchronously as fallback.");
            try {
                // To jest fallback, powinien być używany tylko w krytycznych sytuacjach
                return I18NBundle.createBundle(Gdx.files.internal(BUNDLE_PATH), Locale.ENGLISH);
            } catch (Exception e) {
                log.error("Failed to load fallback bundle.", e);
                throw new IllegalStateException("I18NBundle not loaded and fallback failed.");
            }
        }
        return i18nBundle;
    }

    // Nowa metoda do udostępniania instancji GameSaveSystem
    public GameSaveSystem getGameSaveSystem() {
        if (gameSaveSystem == null) {
            log.error("Attempted to get GameSaveSystem before it was initialized in create()!");
            throw new IllegalStateException("GameSaveSystem not initialized yet.");
        }
        return gameSaveSystem;
    }

    /**
     * Przeładowuje I18NBundle w oparciu o aktualne ustawienia języka.
     * Powinna być wywołana po zmianie języka w ustawieniach.
     * Wymaga, aby ekran mógł potencjalnie przebudować elementy UI przy użyciu nowego bundle'a.
     */
    public void reloadI18nBundle() {
        Locale targetLocale = settingsManager.getCurrentLocale();
        log.error(">>>> MyGame.reloadI18nBundle() CALLED! <<<<");
        log.error(">>>> MyGame.reloadI18nBundle() CALLED! StackTrace:", new Throwable("StackTrace for reloadI18nBundle"));
        log.info("Reloading I18NBundle for target locale: " + targetLocale.toString());

        // Odładowanie poprzedniego bundle'a z AssetManagera, jeśli był załadowany
        if (assetManager.isLoaded(MyGame.BUNDLE_PATH)) {
            log.debug("Unloading previous bundle from AssetManager: " + MyGame.BUNDLE_PATH);
            assetManager.unload(MyGame.BUNDLE_PATH);
            // Czekaj na zakończenie odładowywania, aby uniknąć problemów
            int retries = 0;
            while(assetManager.isLoaded(MyGame.BUNDLE_PATH) && retries < 100) {
                assetManager.update(); // Przetwarzaj kolejkę AssetManagera
                try { Thread.sleep(10); } catch (InterruptedException e) {Thread.currentThread().interrupt();}
                retries++;
            }
            if (assetManager.isLoaded(MyGame.BUNDLE_PATH)) {
                log.error("PROBLEM: Bundle (" + MyGame.BUNDLE_PATH + ") still loaded after unload() and retries!");
            } else {
                log.debug("Bundle (" + MyGame.BUNDLE_PATH + ") successfully unloaded after " + retries + " retries.");
            }
        } else {
            log.debug("No previous bundle was loaded in AssetManager for path: " + MyGame.BUNDLE_PATH);
        }

        // Załadowanie nowego bundle'a
        I18NBundleLoader.I18NBundleParameter param = new I18NBundleLoader.I18NBundleParameter(targetLocale, "UTF-8");
        log.debug("Queueing load for new bundle with path: " + MyGame.BUNDLE_PATH + ", locale: " + param.locale + ", encoding: " + param.encoding);
        assetManager.load(MyGame.BUNDLE_PATH, I18NBundle.class, param);

        // Zakończ ładowanie assetu synchronicznie
        log.debug("Calling assetManager.finishLoadingAsset(BUNDLE_PATH) to load synchronously...");
        assetManager.finishLoadingAsset(MyGame.BUNDLE_PATH);
        log.debug("assetManager.finishLoadingAsset(BUNDLE_PATH) completed.");

        if (assetManager.isLoaded(MyGame.BUNDLE_PATH, I18NBundle.class)) {
            this.i18nBundle = assetManager.get(MyGame.BUNDLE_PATH, I18NBundle.class);
            log.info("I18NBundle reloaded VIA ASSETMANAGER. New bundle actual locale: " + this.i18nBundle.getLocale());
        } else {
            log.error("CRITICAL: Failed to load new bundle from AssetManager after finishLoadingAsset in reload! Attempting direct synchronous creation as fallback.");
            // Fallback: Spróbuj utworzyć bundle synchronicznie bezpośrednio, jeśli AssetManager zawiódł
            try {
                this.i18nBundle = I18NBundle.createBundle(Gdx.files.internal(BUNDLE_PATH), targetLocale, "UTF-8");
                log.error("Loaded I18NBundle via direct synchronous creation as a fallback. Actual locale: " + this.i18nBundle.getLocale());
            } catch (GdxRuntimeException e) {
                log.error("CRITICAL: Fallback direct bundle creation also failed! UI texts may be missing.", e);
                // W przypadku całkowitej awarii, gra może nadal działać, ale bez poprawnych tekstów
            }
        }

        Screen currentScreen = getScreen();
        if (currentScreen instanceof BaseScreen baseScreen) {
            log.debug("Calling updateLocale on current screen: " + currentScreen.getClass().getSimpleName());
            baseScreen.updateLocale(this.i18nBundle);
        } else if (currentScreen != null) {
            log.info("Current screen does not implement locale update method. UI text might be stale.");
        } else {
            log.error("Current screen is null, cannot update locale.");
        }
    }


    /**
     * Zmienia bieżący ekran, upewniając się, że poprzedni ekran zostanie zwolniony, jeśli nie jest null.
     * @param screen Nowy ekran do ustawienia. Nie może być null.
     */
    @Override
    public void setScreen(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        Screen oldScreen = getScreen();
        super.setScreen(screen);
        log.debug("Switched screen from " + (oldScreen != null ? oldScreen.getClass().getSimpleName() : "null") + " to " + screen.getClass().getSimpleName());

        if (oldScreen != null) {
            log.debug("Disposing previous screen: " + oldScreen.getClass().getSimpleName());
            oldScreen.dispose();
        }
    }
}
