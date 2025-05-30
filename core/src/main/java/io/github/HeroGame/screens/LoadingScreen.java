package io.github.HeroGame.screens;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.I18NBundleLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Logger;
import io.github.HeroGame.MyGame;

import java.util.Locale;

public class LoadingScreen extends BaseScreen {

    private final AssetManager assetManager;
    private final Logger log = new Logger(LoadingScreen.class.getSimpleName(), Logger.DEBUG);
    private boolean assetsLoaded = false;
    final String POLISH_CHARACTERS = "ąćęłńóśźżĄĆĘŁŃÓŚŹŻ";
    final String ALIAS_EN = "i18n/strings_en_bundle";
    final String ALIAS_PL = "i18n/strings_pl_bundle";

    private ProgressBar progressBar;
    private Label loadingLabel;


    public LoadingScreen(MyGame game) {
        super(game);
        this.assetManager = game.getAssetManager();
        log.debug("LoadingScreen initialized.");
    }

    @Override
    public void show() {
        log.info("Loading screen shown. Starting asset loading...");

        FreetypeFontLoader.FreeTypeFontLoaderParameter fontParam = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        fontParam.fontFileName = MyGame.FONT_ALKHEMIKAL;
        fontParam.fontParameters.size = 32;
        fontParam.fontParameters.color = Color.WHITE;
        fontParam.fontParameters.borderColor = Color.BLACK;
        fontParam.fontParameters.borderWidth = 1;
        fontParam.fontParameters.characters = FreeTypeFontGenerator.DEFAULT_CHARS + POLISH_CHARACTERS;

        assetManager.load(MyGame.FONT_ALKHEMIKAL, com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.class);
        assetManager.load(MyGame.FONT_ALKHEMIKAL_NAME, BitmapFont.class, fontParam);

        log.debug("Loading initial I18NBundle with Locale.ENGLISH and UTF-8 encoding.");
        I18NBundleLoader.I18NBundleParameter bundleParam = new I18NBundleLoader.I18NBundleParameter(
            Locale.ENGLISH,
            "UTF-8"
        );
        assetManager.load(MyGame.BUNDLE_PATH, I18NBundle.class, bundleParam);

        // USUNIĘTO: assetManager.load(MyGame.DUAL_GRID_TILES_ATLAS, TextureAtlas.class);
        assetManager.load(MyGame.WORLD_GRID_TMX_PATH, TiledMap.class);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        if (!assetsLoaded) {
            if (assetManager.update()) {
                log.info("Assets loaded!");
                assetsLoaded = true;
                game.finishLoading();
            } else {
                float progress = assetManager.getProgress();
                log.debug("Loading progress: " + String.format("%.2f", progress * 100) + "%");

                if (progressBar != null) {
                    progressBar.setValue(progress);
                }
                if (loadingLabel != null) {
                    loadingLabel.setText("Loading... " + (int)(progress * 100) + "%");
                } else if (assetManager.isLoaded(MyGame.FONT_ALKHEMIKAL_NAME) && loadingLabel == null) {
                    createLoadingUI();
                }
            }
        }
    }

    private void createLoadingUI() {
        log.debug("Font loaded, creating Loading UI elements.");
        try {
            Skin tempSkin = new Skin();
            tempSkin.add("default", assetManager.get(MyGame.FONT_ALKHEMIKAL_NAME, BitmapFont.class));

            Label.LabelStyle lblStyle = new Label.LabelStyle(tempSkin.getFont("default"), Color.WHITE);
            tempSkin.add("default", lblStyle);

            loadingLabel = new Label("Loading... 0%", tempSkin);

            Table table = new Table();
            table.setFillParent(true);
            table.center();
            table.add(loadingLabel).padBottom(20);

            stage.addActor(table);
            log.debug("Loading UI added to stage.");

        } catch (Exception e) {
            log.error("Failed to create loading UI. Font might not be ready?", e);
        }
    }

    @Override
    protected void rebuildUI() {
        if (loadingLabel != null) {
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        log.debug("Disposing LoadingScreen.");
        super.dispose();
    }
}
