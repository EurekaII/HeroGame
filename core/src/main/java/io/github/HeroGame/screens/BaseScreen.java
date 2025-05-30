package io.github.HeroGame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.HeroGame.MyGame;

import java.util.Objects;

/**
 * Abstract base class for game screens, providing common setup and utilities.
 */
public abstract class BaseScreen implements Screen, Disposable {

    protected final MyGame game;
    protected final SpriteBatch batch; // May not be needed if only using Stage
    protected final Stage stage;
    protected final Viewport viewport; // Each screen manages its own viewport
    private final Logger log;


    public BaseScreen(final MyGame game) {
        this.game = Objects.requireNonNull(game, "Game instance cannot be null");
        this.batch = game.getBatch();


        this.log = new Logger(this.getClass().getSimpleName(), Logger.DEBUG);
        this.viewport = new FitViewport(1920, 1080);
        ;// Stage nie potrzebuje koniecznie Skin do samego stworzenia
        this.stage = new Stage(this.viewport, this.batch);

        log.debug("BaseScreen initialized (without skin field).");
    }

    /**
     * Called when the screen becomes the current screen for a {@link com.badlogic.gdx.Game}.
     * Sets the InputProcessor to the stage.
     */
    @Override
    public void show() {
        log.debug("Showing screen...");
        // VERY IMPORTANT: Set the input processor to the stage when the screen is shown
        Gdx.input.setInputProcessor(stage);
        log.debug("Input processor set to Stage.");
    }

    /**
     * Called when the screen should render itself.
     * Clears the screen (optional, game loop might handle), updates and draws the stage.
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        // Screen clearing is handled in MyGame.render() loop

        // Update the stage actors
        stage.act(Math.min(delta, 1 / 30f)); // Cap delta to prevent jumps

        // Draw the stage
        viewport.apply(); // Apply viewport changes
        stage.draw();
    }

    /**
     * Called when the game window is resized. Updates the viewport and stage.
     * @param width the new width in pixels
     * @param height the new height in pixels
     * @see com.badlogic.gdx.ApplicationListener#resize(int, int)
     */
    @Override
    public void resize(int width, int height) {
        log.debug("Resizing screen to: " + width + "x" + height);
        // Update the viewport dimensions
        viewport.update(width, height, true); // true to center camera
        // Stage's camera is managed by the viewport, no need to update stage camera directly
    }

    @Override
    public void pause() {
        log.debug("Screen paused.");
        // Optional: Handle game pausing logic specific to this screen
    }

    @Override
    public void resume() {
        log.debug("Screen resumed.");
        // Optional: Handle game resuming logic specific to this screen
    }

    /**
     * Called when this screen is no longer the current screen for a {@link com.badlogic.gdx.Game}.
     * Clears the InputProcessor.
     */
    @Override
    public void hide() {
        log.debug("Hiding screen...");
        // Clear the input processor when the screen is hidden
        // Important if other screens or game logic need input focus
        if (Gdx.input.getInputProcessor() == stage) {
            Gdx.input.setInputProcessor(null);
            log.debug("Input processor cleared.");
        }
    }

    /**
     * Called when this screen should release all resources.
     * Disposes the stage. Subclasses should override to dispose additional resources
     * and call super.dispose().
     */
    @Override
    public void dispose() {
        log.debug("Disposing screen...");
        if (stage != null) {
            stage.dispose();
            log.debug("Stage disposed.");
        }
    }

    /**
     * Called by MyGame when the language changes to update the screen's bundle reference
     * and potentially rebuild UI elements.
     * @param newBundle The newly loaded I18NBundle.
     */
    public void updateLocale(I18NBundle newBundle) {
        // ---> Nie przechowujemy już bundle w BaseScreen <---
        // this.i18nBundle = Objects.requireNonNull(newBundle, "New bundle cannot be null");
        log.debug("Locale update requested. Rebuilding UI...");
        rebuildUI(); // Wywołuje metodę w podklasie, która pobierze nowy bundle z game
    }

    /**
     * Abstract method to be implemented by subclasses.
     * This method should contain the logic to recreate or update UI elements
     * when the language changes or other major state changes occur that require UI refresh.
     */
    protected abstract void rebuildUI();

}
