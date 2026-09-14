package dev.cheesesmp.noloadingscreen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlayerManagerTest {

    @Test
    void armedPlayerIsConsumedExactlyOnce() {
        PlayerManager manager = new PlayerManager();
        UUID player = UUID.randomUUID();

        manager.addChangingWorldPlayer(player);

        assertTrue(manager.consumeChangingWorldPlayer(player),
                "the respawn packet following the teleport should be suppressed");
        assertFalse(manager.consumeChangingWorldPlayer(player),
                "a later respawn, such as the one sent after dying, must go through");
    }

    @Test
    void unarmedPlayerIsNeverConsumed() {
        PlayerManager manager = new PlayerManager();

        assertFalse(manager.consumeChangingWorldPlayer(UUID.randomUUID()));
    }

    @Test
    void armingOnePlayerDoesNotAffectAnother() {
        PlayerManager manager = new PlayerManager();
        UUID teleporting = UUID.randomUUID();
        UUID bystander = UUID.randomUUID();

        manager.addChangingWorldPlayer(teleporting);

        assertFalse(manager.consumeChangingWorldPlayer(bystander));
        assertTrue(manager.consumeChangingWorldPlayer(teleporting));
    }

    @Test
    void removeDisarmsWithoutConsuming() {
        PlayerManager manager = new PlayerManager();
        UUID player = UUID.randomUUID();

        manager.addChangingWorldPlayer(player);
        manager.removeChangingWorldPlayer(player);

        assertFalse(manager.consumeChangingWorldPlayer(player),
                "quitting or a non-qualifying teleport should drop the pending flag");
    }

    @Test
    void clearDisarmsEveryone() {
        PlayerManager manager = new PlayerManager();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        manager.addChangingWorldPlayer(first);
        manager.addChangingWorldPlayer(second);
        manager.clear();

        assertFalse(manager.consumeChangingWorldPlayer(first));
        assertFalse(manager.consumeChangingWorldPlayer(second));
    }

    @Test
    void flagExpiresWhenNoRespawnPacketFollows() throws InterruptedException {
        PlayerManager manager = new PlayerManager(1L);
        UUID player = UUID.randomUUID();

        manager.addChangingWorldPlayer(player);
        Thread.sleep(20L);

        assertFalse(manager.consumeChangingWorldPlayer(player),
                "a flag left behind by a cancelled teleport must not linger");
    }
}
