package io.github.HeroGame.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import io.github.HeroGame.entities.Player;
import io.github.HeroGame.fsm.State;

/**
 * Stan rąbania drewna dla gracza.
 */
public class PlayerWoodChopState implements State<Player> {
    private float actionDuration;
    @Override
    public void enter(Player player) {
        player.setVelocity(0, 0);
        player.setStateTime(0f);
        player.setCurrentTool(Player.ToolType.AXE);

        String animationKey = player.getCurrentAnimationKey();
        Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> animation = player.getAnimations().get(animationKey);
        if (animation != null) {
            actionDuration = 1.0f; // Podobnie jak w Miningu
        } else {
            actionDuration = 1.0f;
            System.err.println("Wood chopping animation not found for key: " + animationKey);
        }
        System.out.println(player.getName() + " started WOOD CHOPPING.");
    }

    @Override
    public void update(Player player, float deltaTime) {
        if (player.getStateTime() >= actionDuration) {
            System.out.println(player.getName() + " gathered some wood (placeholder).");
            player.getStateMachine().changeState("IDLE");
        }
    }

    @Override
    public void exit(Player player) {
        System.out.println(player.getName() + " finished WOOD CHOPPING.");
    }
}
