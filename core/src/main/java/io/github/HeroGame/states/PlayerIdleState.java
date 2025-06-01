package io.github.HeroGame.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.HeroGame.entities.Player;
import io.github.HeroGame.fsm.State;

/**
 * Stan bezczynności dla gracza.
 */
public class PlayerIdleState implements State<Player> {
    @Override
    public void enter(Player player) {
        player.setVelocity(0, 0);
        player.setStateTime(0f); // Resetuj czas animacji dla płynnego przejścia
        // System.out.println(player.getName() + " is now IDLE.");
    }

    @Override
    public void update(Player player, float deltaTime) {
        // Sprawdzenie inputu do zmiany stanu
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.A) ||
            Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            player.getStateMachine().changeState("WALK");
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) { // Atak
            player.getStateMachine().changeState("ATTACK");
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) { // Interakcja / Akcja (np. kopanie, rąbanie)
            // Tutaj można dodać logikę sprawdzania, co jest przed graczem
            // i przechodzić do odpowiedniego stanu (MINE, WOODCHOP, DIG)
            // Na razie uproszczone:
            // if (canMine()) player.getStateMachine().changeState("MINE");
            // else if (canChop()) player.getStateMachine().changeState("WOODCHOP");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) { // Strzał z łuku (jeśli wyposażony)
            if(player.getCurrentTool() == Player.ToolType.BOW) {
                player.getStateMachine().changeState("BOW_SHOT");
                return;
            }
        }
        // Można dodać obsługę innych klawiszy dla medytacji, ekwipunku itp.
    }

    @Override
    public void exit(Player player) {
        // System.out.println(player.getName() + " is no longer IDLE.");
    }
}
