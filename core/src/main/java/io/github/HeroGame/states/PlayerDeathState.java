package io.github.HeroGame.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import io.github.HeroGame.entities.Player;
import io.github.HeroGame.fsm.State;

/**
 * Stan śmierci gracza.
 */
public class PlayerDeathState implements State<Player> {
    private float deathAnimationDuration;
    private boolean animationFinished = false;

    @Override
    public void enter(Player player) {
        player.setVelocity(0, 0);
        player.setStateTime(0f);
        animationFinished = false;

        String animationKey = player.getCurrentAnimationKey(); // DEATH_DOWN
        Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> animation = player.getAnimations().get(animationKey);
        if (animation != null) {
            deathAnimationDuration = animation.getAnimationDuration();
        } else {
            deathAnimationDuration = 2.0f; // Dłuższy czas, jeśli brak animacji
            System.err.println("Death animation not found for key: " + animationKey);
        }
        System.out.println(player.getName() + " has DIED.");
        // TODO: Opublikuj event PlayerDiedEvent
        // TODO: Zablokuj input gracza
    }

    @Override
    public void update(Player player, float deltaTime) {
        if (!animationFinished && player.getStateTime() >= deathAnimationDuration) {
            animationFinished = true;
            System.out.println(player.getName() + " death animation finished.");
            // Tutaj można wyświetlić ekran "Game Over" lub opcje wczytania gry.
            // Gracz pozostaje w tym stanie.
        }
        // Animacja śmierci powinna być odtwarzana (logika w Player.update())
    }

    @Override
    public void exit(Player player) {
        // Ten stan zwykle nie jest opuszczany, chyba że przez wczytanie gry/respawn
        System.out.println(player.getName() + " is somehow exiting DEATH state (e.g. revive/load).");
    }
}
