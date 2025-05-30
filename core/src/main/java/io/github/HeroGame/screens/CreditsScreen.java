package io.github.HeroGame.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Logger;
import io.github.HeroGame.MyGame;

public class CreditsScreen extends BaseScreen {

    private final Logger log = new Logger(CreditsScreen.class.getSimpleName(), Logger.DEBUG);
    private Table mainTable;
    private ScrollPane scrollPane;
    private Table contentTable; // Tabela wewnątrz ScrollPane

    public CreditsScreen(final MyGame game) {
        super(game);
    }

    @Override
    public void show() {
        super.show(); // Ustawia InputProcessor
        log.debug("CreditsScreen show called.");
        rebuildUI();
    }

    @Override
    protected void rebuildUI() {
        log.debug("Rebuilding Credits UI...");
        stage.clear(); // Wyczyść poprzednich aktorów

        Skin skin = game.getSkin(); // Pobierz skin
        I18NBundle i18nBundle = game.getI18nBundle(); // Pobierz aktualny bundle

        mainTable = new Table(skin);
        mainTable.setFillParent(true);
        mainTable.pad(20f);
        // mainTable.setDebug(true); // Pomocne przy układaniu

        // Tytuł
        Label titleLabel = new Label(i18nBundle.get("creditsTitle"), skin, "default-label"); // Użyj stylu "default-label"
        mainTable.add(titleLabel).expandX().center().padBottom(30).row();

        // Tabela na treść, która będzie przewijana
        contentTable = new Table(skin);
        contentTable.top().left(); // Wyrównaj do góry i lewej wewnątrz scrollpane
        // contentTable.setDebug(true);

        // --- Dodawanie treści ---
        addCreditEntry(i18nBundle.get("fontUsed"), skin, i18nBundle, true); // Nagłówek
        addCreditEntry(i18nBundle.get("fontName"), skin, i18nBundle, false);
        addCreditEntry(i18nBundle.get("fontInfo"), skin, i18nBundle, false);

        // Możesz dodać więcej wpisów tutaj w przyszłości
        // addCreditEntry("Kolejny wpis nagłówkowy", skin, i18nBundle, true);
        // addCreditEntry("Treść dla kolejnego wpisu", skin, i18nBundle, false);


        // ScrollPane
        // Upewnij się, że masz styl "default-scrollpane" zdefiniowany w MyGame.finishLoading()
        scrollPane = new ScrollPane(contentTable, skin, "default-scrollpane");
        scrollPane.setFadeScrollBars(false); // Opcjonalnie: paski zawsze widoczne
        scrollPane.setScrollingDisabled(true, false); // Wyłącz przewijanie poziome, włącz pionowe

        // Dodaj ScrollPane do głównej tabeli, pozwalając mu się rozciągnąć
        mainTable.add(scrollPane).expand().fill().padBottom(20).row();


        // Przycisk Powrotu
        TextButton backButton = new TextButton(i18nBundle.get("back"), skin, "default-textbutton"); // Użyj stylu "default-textbutton"
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                log.info("Back button clicked from CreditsScreen.");
                game.setScreen(new MainMenuScreen(game)); // Wróć do menu głównego
            }
        });

        mainTable.add(backButton).width(200).padTop(20).center();

        stage.addActor(mainTable);
        log.debug("Credits UI rebuilt and added to stage.");
    }

    /**
     * Pomocnicza metoda do dodawania wpisu do tabeli z treścią.
     */
    private void addCreditEntry(String text, Skin skin, I18NBundle i18nBundle, boolean isHeader) {
        Label label = new Label(text, skin, isHeader ? "default-label" : "default-label"); // Można by stworzyć osobny styl dla nagłówków
        if (isHeader) {
            label.setFontScale(1.2f); // Lekko powiększ nagłówki
            contentTable.add(label).left().padTop(15).padBottom(5).row();
        } else {
            label.setWrap(true); // Zezwól na zawijanie tekstu
            contentTable.add(label).width(stage.getWidth() * 0.8f).left().padBottom(5).row(); // Ogranicz szerokość dla zawijania
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta); // BaseScreen obsługuje stage.act() i stage.draw()
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height); // BaseScreen aktualizuje viewport
        // Jeśli UI ma się dynamicznie dostosowywać, np. szerokość etykiet w scrollPane
        if (contentTable != null && stage != null) {
            // Przebuduj UI, aby dostosować szerokość etykiet do nowego rozmiaru sceny
            // lub zaktualizuj tylko szerokości komórek
            for(Cell cell : contentTable.getCells()){
                if(cell.getActor() instanceof Label){
                    // Przykład aktualizacji szerokości, można dostosować
                    cell.width(stage.getWidth() * 0.8f);
                }
            }
            contentTable.invalidateHierarchy();
        }
        log.debug("CreditsScreen resized.");
    }

    @Override
    public void dispose() {
        log.debug("Disposing CreditsScreen...");
        super.dispose(); // BaseScreen usuwa stage
    }
}
