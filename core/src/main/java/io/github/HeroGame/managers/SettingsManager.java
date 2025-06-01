package io.github.HeroGame.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Logger;

import java.util.Locale;
import java.util.Objects;

public final class SettingsManager {

    private static final Logger log = new Logger(SettingsManager.class.getSimpleName(), Logger.DEBUG);
    private static final String PREFERENCES_NAME = "HeroGameSettings";

    // Preference Keys
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_RESOLUTION_WIDTH = "resolutionWidth";
    private static final String KEY_RESOLUTION_HEIGHT = "resolutionHeight";

    // Default Values
    public static final String DEFAULT_LANGUAGE = Locale.ENGLISH.getLanguage(); // "en"
    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 720;

    private final Preferences prefs;

    private static final String KEY_FULLSCREEN = "fullscreen";
    private static final boolean DEFAULT_FULLSCREEN = false;

    // Supported resolutions (Add more as needed)
    public record Resolution(int width, int height) {
        @Override
        public String toString() {
            return width + " x " + height;
        }
    }
    // Store resolutions for easy access/iteration in options
    private static final Resolution[] SUPPORTED_RESOLUTIONS = {
        new Resolution(800, 600),
        new Resolution(1280, 720),
        new Resolution(1920, 1080),
        new Resolution(2560, 1440)
        // Add more supported resolutions here
    };

    public SettingsManager() {
        try {
            this.prefs = Gdx.app.getPreferences(PREFERENCES_NAME);
            log.debug("Preferences loaded successfully: " + PREFERENCES_NAME);
        } catch (GdxRuntimeException e) {
            log.error("Failed to load preferences: " + PREFERENCES_NAME, e);
            // Consider fallback or error handling if preferences are critical
            throw e; // Re-throw for critical failure
        }
    }

    // --- Language ---
    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }

    public void setLanguage(String languageCode) {
        Objects.requireNonNull(languageCode, "Language code cannot be null");
        log.debug("setLanguage called with: " + languageCode);
        String oldLanguage = prefs.getString(KEY_LANGUAGE, "NOT_SET");

        // Prosta walidacja - można to zrobić bardziej elegancko, np. z listą/setem obsługiwanych kodów
        if (!languageCode.equals("en") && !languageCode.equals("pl") && !languageCode.equals("de") && !languageCode.equals("id")) { // <<< DODANO "id"
            log.info("Unsupported language code set: " + languageCode + ". Falling back to default (" + DEFAULT_LANGUAGE + ").");
            languageCode = DEFAULT_LANGUAGE;
        }

        prefs.putString(KEY_LANGUAGE, languageCode);
        prefs.flush();
        log.info("Language set in Preferences from '" + oldLanguage + "' to: '" + prefs.getString(KEY_LANGUAGE, "ERROR_READING") + "'");
    }


    public Locale getCurrentLocale() {
        String langCode = getLanguage();
        log.debug("getCurrentLocale: langCode from getLanguage() is '" + langCode + "'");
        Locale locale = new Locale(langCode);
        log.debug("getCurrentLocale: created Locale is '" + locale.toString() + "'");
        return locale;
    }

    // --- Resolution ---
    public Resolution getResolution() {
        int width = prefs.getInteger(KEY_RESOLUTION_WIDTH, DEFAULT_WIDTH);
        int height = prefs.getInteger(KEY_RESOLUTION_HEIGHT, DEFAULT_HEIGHT);
        // Ensure the loaded resolution is actually supported, otherwise return default
        for (Resolution res : SUPPORTED_RESOLUTIONS) {
            if (res.width() == width && res.height() == height) {
                return res;
            }
        }
        log.info("Stored resolution (" + width + "x" + height + ") not supported. Using default resolution: " + getDefaultResolution());
        return getDefaultResolution();
    }

    public void setResolution(int width, int height) {
        boolean supported = false;
        for (Resolution res : SUPPORTED_RESOLUTIONS) {
            if (res.width() == width && res.height() == height) {
                supported = true;
                break;
            }
        }

        if (supported) {
            prefs.putInteger(KEY_RESOLUTION_WIDTH, width);
            prefs.putInteger(KEY_RESOLUTION_HEIGHT, height);
            prefs.flush(); // Save immediately
            log.info("Resolution set to: " + width + "x" + height);
            // Note: Applying the resolution change to the window requires separate logic
        } else {
            log.error("Attempted to set unsupported resolution: " + width + "x" + height);
            // Optionally throw an exception or just log the error
        }
    }

    public void setResolution(Resolution resolution) {
        Objects.requireNonNull(resolution, "Resolution cannot be null");
        setResolution(resolution.width(), resolution.height());
    }

    public static Resolution getDefaultResolution() {
        for (Resolution res : SUPPORTED_RESOLUTIONS) {
            if (res.width() == DEFAULT_WIDTH && res.height() == DEFAULT_HEIGHT) {
                return res;
            }
        }
        // Fallback if default isn't in the list (should not happen with current setup)
        return SUPPORTED_RESOLUTIONS.length > 0 ? SUPPORTED_RESOLUTIONS[0] : new Resolution(640, 480);
    }

    public static Resolution[] getSupportedResolutions() {
        // Return a copy to prevent external modification
        return SUPPORTED_RESOLUTIONS.clone();
    }

    public boolean isFullscreen() {
        return prefs.getBoolean(KEY_FULLSCREEN, DEFAULT_FULLSCREEN);
    }

    public void setFullscreen(boolean fullscreen) {
        prefs.putBoolean(KEY_FULLSCREEN, fullscreen);
        prefs.flush();
        log.info("Fullscreen set to: " + fullscreen);
        // Samo ustawienie preferencji nie zmienia trybu, to musi zrobić applySettings
    }

    // --- Apply Resolution ---
    /**
     * Applies the currently set resolution to the game window.
     * Should be called after setting a new resolution, potentially
     * requiring a screen refresh or specific viewport updates.
     */

    public void applyDisplayMode() {
        Resolution currentRes = getResolution();
        boolean fullscreen = isFullscreen();
        log.debug("Applying display mode: " + currentRes + (fullscreen ? " (Fullscreen)" : " (Windowed)"));

        if (fullscreen) {
            // Znajdź najbliższy obsługiwany tryb pełnoekranowy
            Graphics.DisplayMode displayMode = null;
            for (Graphics.DisplayMode mode : Gdx.graphics.getDisplayModes()) {
                if (mode.width == currentRes.width() && mode.height == currentRes.height()) {
                    displayMode = mode;
                    break;
                }
            }
            // Jeśli nie ma dokładnego dopasowania, można wziąć najlepsze dostępne lub najbliższe
            // Dla uproszczenia, jeśli nie ma, użyjemy obecnego trybu okienkowego
            if (displayMode != null) {
                if (Gdx.graphics.setFullscreenMode(displayMode)) {
                    log.info("Switched to fullscreen mode: " + displayMode.width + "x" + displayMode.height);
                } else {
                    log.error("Failed to switch to fullscreen mode with preferred resolution. Staying windowed.");
                    // Wróć do trybu okienkowego z obecną rozdzielczością jako fallback
                    if (Gdx.graphics.setWindowedMode(currentRes.width(), currentRes.height())) {
                        log.info("Fallback to windowed mode: " + currentRes.width() + "x" + currentRes.height());
                    } else {
                        log.error("CRITICAL: Failed to set any display mode!");
                    }
                }
            } else {
                log.error("Preferred resolution " + currentRes + " not available in fullscreen. Staying windowed.");
                // Upewnij się, że jesteśmy w trybie okienkowym
                if (!Gdx.graphics.isFullscreen()) { // Jeśli już byliśmy fullscreen z inną rozd.
                    if (Gdx.graphics.setWindowedMode(currentRes.width(), currentRes.height())) {
                        log.info("Set to windowed mode: " + currentRes.width() + "x" + currentRes.height());
                    }
                } else { // Jeśli już byliśmy w trybie okienkowym z tą rozdzielczością, nic nie rób
                    log.info("Already in windowed mode with current resolution.");
                }
            }
        } else { // Tryb okienkowy
            if (Gdx.graphics.setWindowedMode(currentRes.width(), currentRes.height())) {
                log.info("Switched to windowed mode: " + currentRes.width() + "x" + currentRes.height());
            } else {
                log.error("Failed to set windowed mode: " + currentRes.width() + "x" + currentRes.height());
            }
        }
    }

    public void applyResolution() {
        Resolution currentRes = getResolution();
        log.debug("Applying resolution: " + currentRes);
        try {
            if (Gdx.graphics.setWindowedMode(currentRes.width(), currentRes.height())) {
                log.info("Window mode set to " + currentRes.width() + "x" + currentRes.height());
                // Viewports using ScreenViewport usually adapt automatically.
                // Other viewports might need manual update: viewport.update(width, height, true);
            } else {
                log.error("Failed to set window mode to " + currentRes.width() + "x" + currentRes.height());
                // Handle failure - perhaps fallback to a default or previous resolution
            }
        } catch (GdxRuntimeException e) {
            log.error("Error applying resolution", e);
            // Handle specific graphics-related exceptions
        }
    }
}
