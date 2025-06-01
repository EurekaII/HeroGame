package io.github.HeroGame.states;

import com.badlogic.gdx.graphics.g2d.Animation;
import io.github.HeroGame.entities.Player;
import io.github.HeroGame.fsm.State;

/**
 * Stan ataku dla gracza.
 */
public class PlayerAttackState implements State<Player> {
    private float attackDuration; // Czas trwania animacji ataku

    @Override
    public void enter(Player player) {
        player.setVelocity(0, 0); // Gracz nie może się ruszać podczas ataku
        player.setStateTime(0f); // Resetuj czas animacji

        // Ustaw narzędzie na SWORD jeśli gracz nie ma nic specjalnego
        // To powinno być zarządzane przez system ekwipunku
        if(player.getCurrentTool() == Player.ToolType.NONE || player.getCurrentTool() == Player.ToolType.TORCH) {
            player.setCurrentTool(Player.ToolType.SWORD);
        }

        String animationKey = player.getCurrentAnimationKey(); // Powinien dać np. ATTACK_SWORD_DOWN
        Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> attackAnimation = player.getAnimations().get(animationKey);

        if (attackAnimation != null) {
            attackDuration = attackAnimation.getAnimationDuration();
        } else {
            attackDuration = 0.5f; // Domyślny czas trwania, jeśli animacja nie istnieje
            System.err.println("Attack animation not found for key: " + animationKey);
        }
        System.out.println(player.getName() + " is ATTACKING with " + player.getCurrentTool());

        // TODO: Logika wykrywania trafień, zadawania obrażeń
        // To może być zrobione przez sprawdzanie kolizji w określonej klatce animacji
        // lub przez stworzenie "hitboxa" na czas ataku.
    }

    @Override
    public void update(Player player, float deltaTime) {
        if (player.getStateTime() >= attackDuration) {
            player.getStateMachine().changeState("IDLE"); // Wróć do stanu bezczynności po zakończeniu ataku
        }
    }

    @Override
    public void exit(Player player) {
        // player.setCurrentTool(Player.ToolType.NONE); // Opcjonalnie: schowaj broń po ataku
        System.out.println(player.getName() + " finished ATTACKING.");
    }
}
