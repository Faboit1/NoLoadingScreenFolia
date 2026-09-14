package dev.cheesesmp.noloadingscreen;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Folia build of LoadingScreenRemover.
 *
 * <p>PacketEvents is shaded and relocated rather than depended on as a separate
 * plugin, so this is a single drop-in jar. The relocation keeps this copy from
 * clashing with a standalone PacketEvents install.</p>
 */
public final class NoLoadingScreen extends JavaPlugin {

    private PlayerManager playerManager;
    private PlayerListener playerListener;
    private Settings settings;

    @Override
    public void onLoad() {
        // PacketEvents has to be built and loaded before the server opens its
        // listener, which is why this cannot wait for onEnable.
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));

        // This copy of PacketEvents is shaded in and cannot be updated on its
        // own, so its update check would only ever print a notice nobody can
        // act on; its metrics would also be filed under the upstream plugin.
        PacketEvents.getAPI().getSettings()
                .checkForUpdates(false)
                .bStats(false);

        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        this.settings = new Settings(this);
        this.playerManager = new PlayerManager();
        this.playerListener = new PlayerListener(this);

        getServer().getPluginManager().registerEvents(playerListener, this);
        PacketEvents.getAPI().getEventManager().registerListener(playerListener);
        PacketEvents.getAPI().init();

        LSRCommand command = new LSRCommand(this);
        getCommand("noloadingscreen").setExecutor(command);
        getCommand("noloadingscreen").setTabCompleter(command);

        getLogger().info("Enabled - suppressing loading screens between same-environment worlds.");
    }

    @Override
    public void onDisable() {
        if (playerManager != null) {
            playerManager.clear();
        }
        if (PacketEvents.getAPI() != null) {
            PacketEvents.getAPI().terminate();
        }
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public Settings getSettings() {
        return settings;
    }
}
