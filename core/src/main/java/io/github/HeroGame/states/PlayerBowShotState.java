package io.github.HeroGame.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import io.github.HeroGame.entities.Player;
import io.github.HeroGame.fsm.State;

/**
 * Stan strzału z łuku dla gracza.
 */
public class PlayerBowShotState implements State<Player> {
    private float actionDuration;
    @Override
    public void enter(Player player) {
        player.setVelocity(0, 0);
        player.setStateTime(0f);
        player.setCurrentTool(Player.ToolType.BOW);

        String animationKey = player.getCurrentAnimationKey(); // ATTACK_BOW_DIRECTION
        Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> animation = player.getAnimations().get(animationKey);
        if (animation != null) {
            actionDuration = animation.getAnimationDuration();
        } else {
            actionDuration = 1.0f;
            System.err.println("Bow shot animation not found for key: " + animationKey);
        }
        System.out.println(player.getName() + " is SHOOTING BOW.");
        // TODO: Stworzenie i wystrzelenie pocisku (strzały)
    }

    @Override
    public void update(Player player, float deltaTime) {
        if (player.getStateTime() >= actionDuration) {
            player.getStateMachine().changeState("IDLE");
        }
    }

    @Override
    public void exit(Player player) {
        System.out.println(player.getName() + " finished SHOOTING BOW.");
    }
}
