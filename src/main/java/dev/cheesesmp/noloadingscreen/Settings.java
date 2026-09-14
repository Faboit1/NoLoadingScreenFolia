package dev.cheesesmp.noloadingscreen;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plugin configuration, backed by Bukkit's own YAML handling.
 *
 * <p>The original plugin shaded a full configuration library plus a runtime
 * snakeyaml-engine download for two integers; the built-in config does the same
 * job with nothing to relocate or resolve at startup.</p>
 */
public final class Settings {

    private final JavaPlugin plugin;

    private int blindTicks;
    private int speedTicks;
    private boolean maskChunkLoad;

    public Settings(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.blindTicks = Math.max(0, config.getInt("blind-ticks", 20));
        this.speedTicks = Math.max(0, config.getInt("speed-ticks", 10));
        this.maskChunkLoad = config.getBoolean("mask-chunk-load", true);
    }

    /** How long the blindness effect lasts, in ticks. 0 disables it. */
    public int blindTicks() {
        return blindTicks;
    }

    /** How long the speed/slowness pair lasts, in ticks. 0 disables them. */
    public int speedTicks() {
        return speedTicks;
    }

    /** Whether to apply potion effects at all while the destination chunks load. */
    public boolean maskChunkLoad() {
        return maskChunkLoad;
    }
}
