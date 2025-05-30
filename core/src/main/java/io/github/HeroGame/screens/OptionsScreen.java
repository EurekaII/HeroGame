package io.github.HeroGame.screens;

import com.badlogic.gdx.Screen; // Dodano import
import com.badlogic.gdx.utils.Logger;
import io.github.HeroGame.MyGame;
import io.github.HeroGame.managers.SettingsManager;

public class OptionsScreen extends BaseScreen {

    private final Logger log = new Logger(OptionsScreen.class.getSimpleName(), Logger.DEBUG);
    private final SettingsManager settingsManager;
    // OptionsUI optionsUI; // Ta linia nie jest już potrzebna, bo OptionsDialog jest używany bezpośrednio

    private Screen previousScreen; // Referencja do poprzedniego ekranu

    // Konstruktor przyjmujący poprzedni ekran
    public OptionsScreen(final MyGame game, Screen previousScreen) {
        super(game);
        this.settingsManager = game.getSettingsManager();
        this.previousScreen = previousScreen;
    }

    @Override
    public void show() {
        super.show();
        log.debug("OptionsScreen show called.");
        rebuildUI(); // Upewnij się, że UI jest zawsze aktualizowane po pokazaniu
    }

    @Override
    protected void rebuildUI() {
        log.debug("Rebuilding Options UI (showing OptionsDialog)...");
        stage.clear(); // Wyczyść stage przed pokazaniem dialogu

        OptionsDialog dialog = new OptionsDialog(game, settingsManager, new OptionsDialog.OptionsCallback() {
            @Override
            public void onApply() {
                log.info("Options applied, returning to previous screen.");
                // Po zastosowaniu opcji, wróć do poprzedniego ekranu
                game.setScreen(previousScreen != null ? previousScreen : new MainMenuScreen(game));
            }

            @Override
            public void onBack() {
                log.info("Options back button clicked, returning to previous screen.");
                // Po naciśnięciu przycisku Wstecz, wróć do poprzedniego ekranu
                game.setScreen(previousScreen != null ? previousScreen : new MainMenuScreen(game));
            }
        });
        dialog.show(stage); // Pokaż dialog na stage'u tego ekranu
    }

    @Override
    public void render(float delta) {
        super.render(delta); // Renderuj stage, który zawiera dialog
    }
    @Override
    public void resize(int width, int height) { super.resize(width, height); }
    @Override
    public void dispose() { super.dispose(); }
}
