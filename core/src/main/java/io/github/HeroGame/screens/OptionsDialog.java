package io.github.HeroGame.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Logger; // Dodano import
import io.github.HeroGame.MyGame;
import io.github.HeroGame.managers.SettingsManager;

public class OptionsDialog extends Dialog {
    private final MyGame game;
    private final SettingsManager settingsManager;
    private final OptionsCallback callback;
    private SelectBox<LanguageOption> languageSelectBox;
    private SelectBox<SettingsManager.Resolution> resolutionSelectBox;
    private CheckBox fullscreenCheckBox;
    private final Logger log = new Logger(OptionsDialog.class.getSimpleName(), Logger.DEBUG); // Dodano logger

    private static record LanguageOption(String code, String displayName) {
        @Override
        public String toString() { return displayName; }
    }

    public interface OptionsCallback {
        void onApply();
        void onBack();
    }

    public OptionsDialog(MyGame game, SettingsManager settingsManager, OptionsCallback callback) {
        super("", game.getSkin()); // Tytuł może być pusty, jeśli jest dodawany jako Label w contentTable
        this.game = game;
        this.settingsManager = settingsManager;
        this.callback = callback;
        buildUI();
    }

    private void buildUI() {
        Table contentTable = getContentTable(); // To jest wewnętrzna tabela Dialogu
        contentTable.pad(20f);

        Skin skin = game.getSkin();
        I18NBundle bundle = game.getI18nBundle();

        Label titleLabel = new Label(bundle.get("optionsTitle"), skin, "default-label");
        contentTable.add(titleLabel).colspan(2).center().padBottom(40).row();

        Label languageLabel = new Label(bundle.get("language") + ":", skin, "default-label");
        languageSelectBox = new SelectBox<>(skin, "default-selectbox");
        Array<LanguageOption> languageOptions = new Array<>();
        languageOptions.add(new LanguageOption("en", bundle.get("english")));
        languageOptions.add(new LanguageOption("pl", bundle.get("polish")));
        languageOptions.add(new LanguageOption("de", bundle.get("german")));
        languageSelectBox.setItems(languageOptions);
        String currentLang = settingsManager.getLanguage();
        for (LanguageOption opt : languageOptions) {
            if (opt.code().equals(currentLang)) {
                languageSelectBox.setSelected(opt);
                break;
            }
        }
        contentTable.add(languageLabel).right().padRight(10);
        contentTable.add(languageSelectBox).width(250).left().row();

        Label resolutionLabel = new Label(bundle.get("resolution") + ":", skin, "default-label");
        resolutionSelectBox = new SelectBox<>(skin, "default-selectbox");
        resolutionSelectBox.setItems(SettingsManager.getSupportedResolutions());
        resolutionSelectBox.setSelected(settingsManager.getResolution());
        contentTable.add(resolutionLabel).right().padRight(10).padTop(20);
        contentTable.add(resolutionSelectBox).width(250).left().padTop(20).row();

        Label fullscreenLabel = new Label(bundle.get("fullscreen") + ":", skin, "default-label");
        fullscreenCheckBox = new CheckBox("", skin, "default-checkbox");
        fullscreenCheckBox.setChecked(settingsManager.isFullscreen());
        contentTable.add(fullscreenLabel).right().padRight(10).padTop(20);
        contentTable.add(fullscreenCheckBox).left().padTop(20).row();

        TextButton applyButton = new TextButton(bundle.get("apply"), skin, "default");
        TextButton backButton = new TextButton(bundle.get("back"), skin, "default");

        Table buttonTable = new Table(skin);
        buttonTable.add(applyButton).width(150).pad(10);
        buttonTable.add(backButton).width(150).pad(10);
        contentTable.add(buttonTable).colspan(2).center().padTop(50).row();

        applyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                log.debug("OptionsDialog: Apply button clicked.");
                applySettings();
                callback.onApply();
                hide(); // Ukryj dialog po zastosowaniu
            }
        });

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                log.debug("OptionsDialog: Back button clicked.");
                callback.onBack();
                hide(); // Ukryj dialog po powrocie
            }
        });

        // Ustaw tło dla całego dialogu, a nie tylko dla contentTable
        setBackground(skin.getDrawable("selectbox_background"));
    }

    private void applySettings() {
        LanguageOption lang = languageSelectBox.getSelected();
        if (!settingsManager.getLanguage().equals(lang.code())) {
            log.info("Changing language to: " + lang.code());
            settingsManager.setLanguage(lang.code());
            game.reloadI18nBundle(); // To wywoła rebuildUI na ekranie bazowym
        }
        SettingsManager.Resolution res = resolutionSelectBox.getSelected();
        if (!res.equals(settingsManager.getResolution()) || settingsManager.isFullscreen() != fullscreenCheckBox.isChecked()) {
            log.info("Applying display settings: Resolution=" + res + ", Fullscreen=" + fullscreenCheckBox.isChecked());
            settingsManager.setResolution(res);
            settingsManager.setFullscreen(fullscreenCheckBox.isChecked());
            settingsManager.applyDisplayMode();
        }
    }
}
