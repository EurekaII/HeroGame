package io.github.HeroGame.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label; // Dodano import dla Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton; // Dodano import dla TextButton
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Logger;
import io.github.HeroGame.MyGame;
import io.github.HeroGame.save.GameData;
import io.github.HeroGame.save.GameSaveSystem;

public class LoadScreen extends BaseScreen {

    private final Logger log = new Logger(LoadScreen.class.getSimpleName(), Logger.DEBUG);
    private Screen previousScreen;

    public LoadScreen(final MyGame game, Screen previousScreen) {
        super(game);
        this.previousScreen = previousScreen;
    }

    @Override
    public void show() {
        super.show();
        rebuildUI();
    }

    @Override
    protected void rebuildUI() {
        stage.clear();
        GameSaveSystem saveSystem = game.getGameSaveSystem();

        LoadGameDialog dialog = new LoadGameDialog(game, saveSystem, new LoadGameDialog.LoadGameCallback() {
            @Override
            public void onLoad(GameData loadedGameData) {
                if (loadedGameData != null) {
                    log.info("Loaded save from LoadGameDialog, opening GameScreen...");
                    game.setScreen(new GameScreen(game, loadedGameData));
                } else {
                    log.error("Loaded GameData was null, cannot open GameScreen.");
                    showErrorDialog(game.getI18nBundle().get("save_load_error_generic"));
                }
            }

            @Override
            public void onBack() {
                log.info("Back pressed in LoadGameDialog, returning to previousScreen...");
                game.setScreen(previousScreen != null ? previousScreen : new MainMenuScreen(game));
            }

            @Override
            public void onError(String message) {
                showErrorDialog(message);
            }
        });
        dialog.show(stage);
    }

    // Metoda pomocnicza do wyświetlania dialogów błędów
    private void showErrorDialog(String message) {
        I18NBundle i18n = game.getI18nBundle();
        Dialog errorDialog = new Dialog(i18n.get("error_title"), game.getSkin());
        errorDialog.text(message, game.getSkin().get("default-label", Label.LabelStyle.class));

        // --- KLUCZOWA POPRAWKA DLA PRZYCISKU ---
        // Pobierz obiekt stylu TextButton.TextButtonStyle
        TextButton.TextButtonStyle buttonStyle = game.getSkin().get("default-textbutton", TextButton.TextButtonStyle.class);
        // Użyj metody button() która przyjmuje obiekt stylu
        errorDialog.button(i18n.get("ok"), buttonStyle); // Użyj stylu TextButton.TextButtonStyle

        errorDialog.show(stage);
    }

    @Override public void render(float delta) { super.render(delta); }
    @Override public void resize(int width, int height) { super.resize(width, height); }
    @Override public void dispose() { super.dispose(); }
}
