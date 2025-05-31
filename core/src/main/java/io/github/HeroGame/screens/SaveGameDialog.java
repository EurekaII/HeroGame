package io.github.HeroGame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Logger;
import io.github.HeroGame.MyGame;
import io.github.HeroGame.save.GameData;
import io.github.HeroGame.save.GameSaveSystem;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveGameDialog extends Dialog {
    private final MyGame game;
    private final GameSaveSystem gameSaveSystem;
    private final SaveGameCallback callback;
    private final GameData currentGameState;
    private final Logger log = new Logger(SaveGameDialog.class.getSimpleName(), Logger.DEBUG);

    private List<FileHandle> saveFileList;
    private Array<FileHandle> userSaveFiles;
    private TextField saveNameField;
    private TextButton saveButton;
    private TextButton deleteButton;

    public interface SaveGameCallback {
        void onSaveSuccess(String message);
        void onBack();
        void onError(String message);
    }

    public SaveGameDialog(MyGame game, GameSaveSystem saveSystem, GameData currentGameState, SaveGameCallback callback) {
        super("", game.getSkin());
        this.game = game;
        this.gameSaveSystem = saveSystem;
        this.currentGameState = currentGameState;
        this.callback = callback;
        buildUI();
        updateSaveList();
    }

    private void buildUI() {
        Table contentTable = getContentTable();
        contentTable.pad(20f);

        Skin skin = game.getSkin();
        I18NBundle bundle = game.getI18nBundle();

        final TextButton.TextButtonStyle defaultTextButtonStyle = skin.get("default", TextButton.TextButtonStyle.class);
        final Label.LabelStyle defaultLabelStyle = skin.get("default-label", Label.LabelStyle.class);

        Label titleLabel = new Label(bundle.get("saveGame"), defaultLabelStyle);
        contentTable.add(titleLabel).colspan(3).center().padBottom(40).row();

        saveFileList = new List<>(skin, "default-list");
        saveFileList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                FileHandle selected = saveFileList.getSelected();
                if (selected != null) {
                    String nameWithoutTimestamp = extractBaseName(selected.name());
                    saveNameField.setText(nameWithoutTimestamp);
                    deleteButton.setDisabled(GameSaveSystem.isAutosave(selected));
                } else {
                    saveNameField.setText("");
                    deleteButton.setDisabled(true);
                }
            }
        });
        ScrollPane scrollPane = new ScrollPane(saveFileList, skin, "default-scrollpane");
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        contentTable.add(scrollPane).colspan(3).grow().height(200).padBottom(20).row();

        Label nameLabel = new Label(bundle.get("save_dialog_prompt"), defaultLabelStyle);
        saveNameField = new TextField("", skin, "default-textfield");
        contentTable.add(nameLabel).right().padRight(10);
        contentTable.add(saveNameField).width(300).left().pad(5).row();

        Table buttonTable = new Table(skin);
        saveButton = new TextButton(bundle.get("save"), skin, "default");
        deleteButton = new TextButton(bundle.get("delete"), skin, "default");
        TextButton backButton = new TextButton(bundle.get("back"), skin, "default");

        deleteButton.setDisabled(true);

        buttonTable.add(saveButton).width(150).pad(5);
        buttonTable.add(deleteButton).width(150).pad(5);
        buttonTable.add(backButton).width(150).pad(5).row();

        contentTable.add(buttonTable).colspan(3).center().padTop(20).row();

        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String enteredName = saveNameField.getText().trim();
                if (enteredName.isEmpty()) {
                    callback.onError(bundle.get("save_name_empty"));
                    return;
                }

                FileHandle selectedFile = saveFileList.getSelected();
                final String finalTargetFileName;
                boolean isOverwrite = false;

                if (selectedFile != null) {
                    String selectedFileBaseName = extractBaseName(selectedFile.name());
                    if (enteredName.equalsIgnoreCase(selectedFileBaseName)) {
                        finalTargetFileName = selectedFile.name();
                        isOverwrite = true;
                    } else {
                        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
                        finalTargetFileName = enteredName + "_" + timestamp + ".json";
                    }
                } else {
                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
                    finalTargetFileName = enteredName + "_" + timestamp + ".json";
                }

                if (isOverwrite) {
                    Dialog confirmDialog = new Dialog("", skin) {
                        @Override
                        protected void result(Object object) {
                            if (object != null && (Boolean) object) {
                                performSave(finalTargetFileName);
                            }
                        }
                    };
                    Label titleLabel = new Label(bundle.get("confirm_overwrite_title"), defaultLabelStyle);
                    titleLabel.setWrap(true);
                    titleLabel.setAlignment(Align.center);
                    confirmDialog.getContentTable().add(titleLabel).width(560).padTop(10).padBottom(10).center().row();

                    // Treść dialogu:
                    Table confirmTable = new Table(skin);
                    Label confirmLabel = new Label(bundle.get("confirm_overwrite_text").replace("{fileName}", selectedFile.name()), defaultLabelStyle);
                    confirmLabel.setWrap(true);
                    confirmLabel.setAlignment(Align.center);
                    confirmTable.add(confirmLabel).width(540).pad(12).center();
                    confirmDialog.getContentTable().add(confirmTable).width(560).pad(10).center().row();

                    // Przyciski:
                    confirmDialog.button(bundle.get("yes"), true, defaultTextButtonStyle);
                    confirmDialog.button(bundle.get("no"), false, defaultTextButtonStyle);

                    // Ustaw szerokość/padding przycisków:
                    Array<Cell> buttonCells = confirmDialog.getButtonTable().getCells();
                    for (Cell cell : buttonCells) {
                        cell.width(120).pad(8);
                    }
                    confirmDialog.show(getStage());
                } else {
                    performSave(finalTargetFileName);
                }
            }
        });

        deleteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                FileHandle selected = saveFileList.getSelected();
                if (selected != null) {
                    if (GameSaveSystem.isAutosave(selected)) {
                        callback.onError(bundle.get("autosave_cannot_delete"));
                        return;
                    }

                    Dialog confirmDialog = new Dialog("", skin) {
                        @Override
                        protected void result(Object object) {
                            if (object != null && (Boolean) object) {
                                if (gameSaveSystem.deleteSaveFile(selected.name())) {
                                    log.info("Successfully deleted: " + selected.name());
                                    updateSaveList();
                                    callback.onSaveSuccess(bundle.get("save_deleted_success"));
                                } else {
                                    callback.onError(bundle.get("save_delete_error"));
                                }
                            }
                        }
                    };
                    Table confirmTable = new Table(skin);
                    Label confirmLabel = new Label(bundle.get("confirm_delete_text").replace("{fileName}", selected.name()), defaultLabelStyle);
                    confirmLabel.setWrap(true);
                    confirmLabel.setAlignment(Align.center);
                    confirmTable.add(confirmLabel).width(540).pad(12).center();
                    confirmDialog.getContentTable().add(confirmTable).width(560).pad(10).row();
                    confirmDialog.button(bundle.get("yes"), true, defaultTextButtonStyle);
                    confirmDialog.button(bundle.get("no"), false, defaultTextButtonStyle);

                    // Ustaw szerokość/padding przycisków:
                    Array<Cell> buttonCells = confirmDialog.getButtonTable().getCells();
                    for (Cell cell : buttonCells) {
                        cell.width(120).pad(8);
                    }
                    confirmDialog.show(getStage());

                } else {
                    callback.onError(bundle.get("save_select_to_delete"));
                }
            }
        });

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.onBack();
                hide();
            }
        });

        setBackground(skin.getDrawable("selectbox_background"));
    }

    private String extractBaseName(String fileName) {
        return fileName.replaceAll("_[0-9]{8}_[0-9]{4}\\.json$", "");
    }

    private void updateSaveList() {
        userSaveFiles = new Array<>();
        for (FileHandle file : gameSaveSystem.listSaveFiles()) {
            if (!GameSaveSystem.isAutosave(file)) {
                userSaveFiles.add(file);
            }
        }
        saveFileList.setItems(userSaveFiles);
        saveFileList.getSelection().clear();
        saveNameField.setText("");
        deleteButton.setDisabled(true);
    }


    private void performSave(String fileName) {
        try {
            gameSaveSystem.saveGame(fileName, currentGameState);
            log.info("Game saved as: " + fileName);
            callback.onSaveSuccess(game.getI18nBundle().get("save_success") + fileName);
            hide();
        } catch (Exception e) {
            log.error("Error saving game: " + e.getMessage(), e);
            callback.onError(game.getI18nBundle().get("save_error") + e.getMessage());
        }
    }
}
