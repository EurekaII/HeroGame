package io.github.HeroGame.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import io.github.HeroGame.entities.Player;
import io.github.HeroGame.fsm.State;

/**
 * Stan kopania (górnictwa) dla gracza.
 */
public class PlayerMiningState implements State<Player> {
    private float actionDuration;

    @Override
    public void enter(Player player) {
        player.setVelocity(0, 0);
        player.setStateTime(0f);
        player.setCurrentTool(Player.ToolType.PICKAXE); // Załóżmy, że gracz automatycznie używa kilofa

        String animationKey = player.getCurrentAnimationKey(); // Powinien dać MINE_PICKAXE_DOWN
        Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> animation = player.getAnimations().get(animationKey);
        if (animation != null) {
            actionDuration = animation.getAnimationDuration(); // Jeśli animacja jest LOOP, to nie zadziała dobrze
            // Lepiej ustawić stały czas lub na podstawie liczby uderzeń
            actionDuration = 1.0f; // Np. 1 sekunda na jedno "uderzenie" kilofem
        } else {
            actionDuration = 1.0f;
            System.err.println("Mining animation not found for key: " + animationKey);
        }
        System.out.println(player.getName() + " started MINING.");
        // TODO: Logika rozpoczęcia interakcji z rudą/skałą
    }

    @Override
    public void update(Player player, float deltaTime) {
        // Tutaj można dodać logikę postępu kopania, np. po określonym czasie/liczbie "uderzeń"
        // gracz otrzymuje surowce. Animacja może być zapętlona.
        if (player.getStateTime() >= actionDuration) { // Przykładowe zakończenie po jednym cyklu
            System.out.println(player.getName() + " gathered some ore (placeholder).");
            // Można albo zakończyć kopanie, albo kontynuować, jeśli gracz trzyma klawisz
            player.getStateMachine().changeState("IDLE");
        }
    }

    @Override
    public void exit(Player player) {
        // player.setCurrentTool(Player.ToolType.NONE);
        System.out.println(player.getName() + " finished MINING.");
    }
}
