package io.github.HeroGame.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.HeroGame.entities.Player;
import io.github.HeroGame.fsm.State;

/**
 * Stan poruszania się dla gracza.
 */
public class PlayerWalkState implements State<Player> {
    private static final float PLAYER_SPEED = 100f;

    @Override
    public void enter(Player player) {
        player.setStateTime(0f);
        // System.out.println(player.getName() + " started WALKING.");
    }

    @Override
    public void update(Player player, float deltaTime) {
        float speed = PLAYER_SPEED;
        float velX = 0;
        float velY = 0;
        boolean moving = false;

        Player.FacingDirection newDirection = player.getCurrentFacingDirection();

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            velY = speed;
            newDirection = Player.FacingDirection.UP;
            moving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            velY = -speed;
            newDirection = Player.FacingDirection.DOWN;
            moving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            velX = -speed;
            newDirection = Player.FacingDirection.LEFT;
            moving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            velX = speed;
            newDirection = Player.FacingDirection.RIGHT;
            moving = true;
        }

        // Ustalanie kierunku dla 8-way movement (jeśli są wciśnięte dwa klawisze)
        if (Gdx.input.isKeyPressed(Input.Keys.W) && Gdx.input.isKeyPressed(Input.Keys.D)) newDirection = Player.FacingDirection.NORTHEAST;
        else if (Gdx.input.isKeyPressed(Input.Keys.W) && Gdx.input.isKeyPressed(Input.Keys.A)) newDirection = Player.FacingDirection.NORTHWEST;
        else if (Gdx.input.isKeyPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.D)) newDirection = Player.FacingDirection.SOUTHEAST;
        else if (Gdx.input.isKeyPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.A)) newDirection = Player.FacingDirection.SOUTHWEST;
        // Jeśli tylko jeden kierunek pionowy/poziomy jest wciśnięty, powyższe if-y dla W,S,A,D już go ustawiły.

        player.setCurrentFacingDirection(newDirection);


        if (velX != 0 && velY != 0) {
            float invSqrt2 = 0.7071f;
            velX *= invSqrt2;
            velY *= invSqrt2;
        }
        player.setVelocity(velX, velY);

        if (!moving) {
            player.getStateMachine().changeState("IDLE");
        }
    }

    @Override
    public void exit(Player player) {
        player.setVelocity(0, 0);
        // System.out.println(player.getName() + " stopped WALKING.");
    }
}
