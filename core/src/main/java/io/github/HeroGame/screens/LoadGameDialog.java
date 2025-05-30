package io.github.HeroGame.screens;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Logger;
import io.github.HeroGame.MyGame;
import io.github.HeroGame.save.GameData;
import io.github.HeroGame.save.GameSaveSystem;

public class LoadGameDialog extends Dialog {
    private final MyGame game;
    private final GameSaveSystem gameSaveSystem;
    private final LoadGameCallback callback;
    private final Logger log = new Logger(LoadGameDialog.class.getSimpleName(), Logger.DEBUG);

    private List<FileHandle> saveFileList;
    private TextButton loadButton;
    private TextButton deleteButton;

    public interface LoadGameCallback {
        void onLoad(GameData loadedGameData);
        void onBack();
        void onError(String message);
    }

    public LoadGameDialog(MyGame game, GameSaveSystem saveSystem, LoadGameCallback callback) {
        super("", game.getSkin());
        this.game = game;
        this.gameSaveSystem = saveSystem;
        this.callback = callback;
        buildUI();
        updateSaveList();
    }

    private void buildUI() {
        Table contentTable = getContentTable();
        contentTable.pad(20f);

        Skin skin = game.getSkin();
        I18NBundle bundle = game.getI18nBundle();

        final TextButton.TextButtonStyle defaultTextButtonStyle = skin.get("default-textbutton", TextButton.TextButtonStyle.class);
        final Label.LabelStyle defaultLabelStyle = skin.get("default-label", Label.LabelStyle.class);

        Label titleLabel = new Label(bundle.get("loadGame"), defaultLabelStyle);
        contentTable.add(titleLabel).colspan(3).center().padBottom(40).row();

        saveFileList = new List<>(skin, "default-list");
        saveFileList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                FileHandle selected = saveFileList.getSelected();
                loadButton.setDisabled(selected == null);
                deleteButton.setDisabled(selected == null || GameSaveSystem.isAutosave(selected));
            }
        });

        ScrollPane scrollPane = new ScrollPane(saveFileList, skin, "default-scrollpane");
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        contentTable.add(scrollPane).colspan(3).grow().height(200).padBottom(20).row();

        loadButton = new TextButton(bundle.get("load"), skin, "default-textbutton");
        deleteButton = new TextButton(bundle.get("delete"), skin, "default-textbutton");
        TextButton backButton = new TextButton(bundle.get("back"), skin, "default-textbutton");

        loadButton.setDisabled(true);
        deleteButton.setDisabled(true);

        Table buttonTable = new Table(skin);
        buttonTable.add(loadButton).width(150).pad(5);
        buttonTable.add(deleteButton).width(150).pad(5);
        buttonTable.add(backButton).width(150).pad(5).row();
        contentTable.add(buttonTable).colspan(3).center().padTop(20).row();

        loadButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                FileHandle selected = saveFileList.getSelected();
                if (selected != null) {
                    GameData loadedData = gameSaveSystem.loadGame(selected.name());
                    if (loadedData != null) {
                        callback.onLoad(loadedData);
                        hide();
                    } else {
                        callback.onError(bundle.get("save_load_error_generic"));
                    }
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

                    Dialog confirmDialog = new Dialog(bundle.get("confirm_delete_title"), skin) {
                        @Override
                        protected void result(Object object) {
                            if (object != null && (Boolean) object) {
                                if (gameSaveSystem.deleteSaveFile(selected.name())) {
                                    log.info("Successfully deleted: " + selected.name());
                                    updateSaveList();
                                    callback.onError(bundle.get("save_deleted_success"));
                                } else {
                                    callback.onError(bundle.get("save_delete_error"));
                                }
                            }
                        }
                    };
                    confirmDialog.text(bundle.get("confirm_delete_text").replace("{fileName}", selected.name()),
                        defaultLabelStyle);
                    confirmDialog.button(bundle.get("yes"), true, defaultTextButtonStyle);
                    confirmDialog.button(bundle.get("no"), false, defaultTextButtonStyle);
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
        Array<FileHandle> allSaveFiles = new Array<>();
        for (FileHandle file : gameSaveSystem.listSaveFiles()) {
            allSaveFiles.add(file);
        }
        saveFileList.setItems(allSaveFiles);
        saveFileList.getSelection().clear();
        loadButton.setDisabled(true);
    }
}
