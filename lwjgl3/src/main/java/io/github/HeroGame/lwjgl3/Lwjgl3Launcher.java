package io.github.HeroGame.lwjgl3;

import io.github.HeroGame.MyGame;
import io.github.HeroGame.managers.SettingsManager; // Potrzebny dostęp do settingsManager *przed* utworzeniem gry
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        // Create configuration BEFORE application instance
        Lwjgl3ApplicationConfiguration configuration = createApplicationConfiguration();
        // Pass configuration to application constructor
        new Lwjgl3Application(new MyGame(), configuration);
    }

    private static Lwjgl3ApplicationConfiguration createApplicationConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Hero Game"); // Initial title, can be updated later from bundle

        // --- Read resolution from Preferences BEFORE setting window mode ---
        // This requires accessing Preferences *outside* the LibGDX application lifecycle.
        // LibGDX provides ways, but it's a bit platform-dependent or requires setup.
        // A simpler approach for desktop is often to just read the preference file directly,
        // but let's try the LibGDX way if possible, though it's usually done *after* Gdx.app exists.

        // *** Standard approach: Let MyGame handle it after init ***
        // MyGame's create() method calls SettingsManager.applyResolution()
        // which reads prefs and calls Gdx.graphics.setWindowedMode().
        // The initial window size might flicker briefly.

        // *** Alternative (More complex): Read prefs beforehand ***
        // This is harder because Gdx.app doesn't exist yet.
        // We *could* create a temporary SettingsManager instance just for reading,
        // relying on Gdx.files potentially being available statically or setting it up manually.
        // Let's stick to the standard approach for now unless flicker becomes an issue.

        // Set initial window size (will be overridden by SettingsManager in MyGame.create)
        SettingsManager.Resolution defaultRes = SettingsManager.getDefaultResolution();
        configuration.setWindowedMode(defaultRes.width(), defaultRes.height());


        // --- Other configurations ---
        configuration.setWindowIcon("logo128.png", "logo64.png", "logo32.png", "logo16.png"); // Ensure these files exist in core/assets or adjust path
        // VSync, FPS limits, etc.
        configuration.useVsync(true); // Generally recommended
        configuration.setForegroundFPS(60); // Cap FPS when active
        // configuration.setIdleFPS(30); // Cap FPS when inactive (optional)

        return configuration;
    }
}
