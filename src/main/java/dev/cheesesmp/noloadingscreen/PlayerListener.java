package dev.cheesesmp.noloadingscreen;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Suppresses the "Loading terrain" screen on world-to-world teleports.
 *
 * <p>The client shows that screen because the server sends a respawn packet
 * whenever the player's world changes. When both worlds share an environment
 * the client's dimension state is already correct, so the packet can be dropped
 * and the teleport looks like an ordinary move. Crossing into a different
 * environment genuinely changes dimension state, so those are left alone.</p>
 */
public final class PlayerListener extends PacketListenerAbstract implements Listener {

    private final NoLoadingScreen plugin;

    public PlayerListener(NoLoadingScreen plugin) {
        super(PacketListenerPriority.NORMAL);
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        World fromWorld = from.getWorld();
        World toWorld = to.getWorld();
        if (fromWorld == null || toWorld == null) {
            return;
        }

        UUID playerId = event.getPlayer().getUniqueId();
        boolean sameEnvironmentWorldChange = !fromWorld.equals(toWorld)
                && fromWorld.getEnvironment() == toWorld.getEnvironment();

        if (!sameEnvironmentWorldChange) {
            plugin.getPlayerManager().removeChangingWorldPlayer(playerId);
            return;
        }

        plugin.getPlayerManager().addChangingWorldPlayer(playerId);
        applyMaskingEffects(event);
    }

    /**
     * Hides the frames the client renders before the destination chunks arrive.
     * Without the respawn packet the client keeps drawing, so it would otherwise
     * show the old world's terrain — or empty void — for a moment.
     */
    private void applyMaskingEffects(PlayerTeleportEvent event) {
        Settings settings = plugin.getSettings();
        if (!settings.maskChunkLoad()) {
            return;
        }

        // The teleport event runs on the player's own region thread, so the
        // entity may be modified directly here.
        if (settings.blindTicks() > 0) {
            event.getPlayer().addPotionEffect(
                    new PotionEffect(PotionEffectType.BLINDNESS, settings.blindTicks(), 1, true, false, false));
        }

        if (settings.speedTicks() > 0) {
            // Speed and slowness cancel out into roughly normal movement while
            // still overriding whatever momentum the client had mid-teleport.
            event.getPlayer().addPotionEffect(
                    new PotionEffect(PotionEffectType.SPEED, settings.speedTicks(), 3, true, false, false));
            event.getPlayer().addPotionEffect(
                    new PotionEffect(PotionEffectType.SLOWNESS, settings.speedTicks(), 5, true, false, false));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlayerManager().removeChangingWorldPlayer(event.getPlayer().getUniqueId());
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.RESPAWN) {
            return;
        }

        // Runs on the Netty event loop: read the UUID off the connection rather
        // than resolving a Bukkit Player, which would be an off-region access.
        UUID playerId = event.getUser().getUUID();
        if (playerId == null) {
            return;
        }

        if (plugin.getPlayerManager().consumeChangingWorldPlayer(playerId)) {
            event.setCancelled(true);
        }
    }
}
