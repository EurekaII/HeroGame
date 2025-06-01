package io.github.HeroGame.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import io.github.HeroGame.entities.Player;
import io.github.HeroGame.fsm.State;

/**
 * Stan otrzymywania obrażeń przez gracza.
 */
public class PlayerHurtState implements State<Player> {
    private float hurtDuration;

    @Override
    public void enter(Player player) {
        player.setVelocity(0, 0); // Może lekki odrzut?
        player.setStateTime(0f);

        String animationKey = player.getCurrentAnimationKey();
        Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> animation = player.getAnimations().get(animationKey);
        if (animation != null) {
            hurtDuration = animation.getAnimationDuration();
        } else {
            hurtDuration = 0.3f;
            System.err.println("Hurt animation not found for key: " + animationKey);
        }
        System.out.println(player.getName() + " is HURT.");
        // TODO: Efekt dźwiękowy, wizualny (np. mignięcie na czerwono)
    }

    @Override
    public void update(Player player, float deltaTime) {
        if (player.getStateTime() >= hurtDuration) {
            if (player.isAlive()) {
                player.getStateMachine().changeState("IDLE");
            } else {
                player.getStateMachine().changeState("DEATH"); // Jeśli obrażenia były śmiertelne
            }
        }
    }

    @Override
    public void exit(Player player) {
        // System.out.println(player.getName() + " recovered from HURT state.");
    }
}
