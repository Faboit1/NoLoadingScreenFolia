package dev.cheesesmp.noloadingscreen;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

/**
 * Tracks which players are mid-transfer between two same-environment worlds.
 *
 * <p>Entries are written from region threads (the teleport event) and read from
 * Netty's event loop (the packet listener), so the backing map is concurrent.
 * Players are keyed by UUID rather than by {@code Player} instance: holding the
 * entity itself keeps a disconnected player's whole object graph alive, and on
 * Folia the instance you compare against may not be the one you stored.</p>
 *
 * <p>A flag is a one-shot with a deadline. It is consumed by the first respawn
 * packet that follows the teleport, and expires on its own if that packet never
 * arrives — otherwise a player who teleported once would keep swallowing every
 * later respawn, including the one sent after they die.</p>
 */
public final class PlayerManager {

    /** How long a pending world change stays armed before it is ignored. */
    private static final long DEFAULT_EXPIRY_MILLIS = 5_000L;

    private final Map<UUID, Long> changingWorlds = new ConcurrentHashMap<>();
    private final long expiryMillis;

    public PlayerManager() {
        this(DEFAULT_EXPIRY_MILLIS);
    }

    PlayerManager(long expiryMillis) {
        this.expiryMillis = expiryMillis;
    }

    /**
     * Arms the flag for a player who is about to change worlds.
     */
    public void addChangingWorldPlayer(@NotNull UUID playerId) {
        changingWorlds.put(playerId, System.currentTimeMillis() + expiryMillis);
    }

    /**
     * Atomically clears the flag and reports whether it was still valid, so the
     * respawn packet that triggered it is the only one suppressed.
     *
     * @return true if this player had an unexpired world change pending
     */
    public boolean consumeChangingWorldPlayer(@NotNull UUID playerId) {
        Long deadline = changingWorlds.remove(playerId);
        return deadline != null && System.currentTimeMillis() < deadline;
    }

    public void removeChangingWorldPlayer(@NotNull UUID playerId) {
        changingWorlds.remove(playerId);
    }

    public void clear() {
        changingWorlds.clear();
    }
}
