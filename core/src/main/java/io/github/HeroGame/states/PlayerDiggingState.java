package io.github.HeroGame.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import io.github.HeroGame.entities.Player;
import io.github.HeroGame.fsm.State;

/**
 * Stan kopania łopatą dla gracza.
 */
public class PlayerDiggingState implements State<Player> {
    private float actionDuration;
    @Override
    public void enter(Player player) {
        player.setVelocity(0, 0);
        player.setStateTime(0f);
        // player.setCurrentTool(Player.ToolType.SHOVEL); // Jeśli masz osobny typ narzędzia

        String animationKey = player.getCurrentAnimationKey();
        Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> animation = player.getAnimations().get(animationKey);
        if (animation != null) {
            actionDuration = animation.getAnimationDuration(); // Jeśli PlayMode.NORMAL
        } else {
            actionDuration = 0.8f;
            System.err.println("Digging animation not found for key: " + animationKey);
        }
        System.out.println(player.getName() + " started DIGGING.");
    }

    @Override
    public void update(Player player, float deltaTime) {
        if (player.getStateTime() >= actionDuration) {
            System.out.println(player.getName() + " dug something (placeholder).");
            player.getStateMachine().changeState("IDLE");
        }
    }

    @Override
    public void exit(Player player) {
        System.out.println(player.getName() + " finished DIGGING.");
    }
}
