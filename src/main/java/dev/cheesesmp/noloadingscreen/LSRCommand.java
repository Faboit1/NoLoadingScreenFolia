package dev.cheesesmp.noloadingscreen;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles {@code /noloadingscreen reload}.
 *
 * <p>Messages are plain strings rather than Adventure components on purpose:
 * the shaded PacketEvents brings its own relocated Adventure, and anything in
 * this jar that touched {@code net.kyori} would be rewritten to that copy and
 * no longer match the server's own.</p>
 */
public final class LSRCommand implements CommandExecutor, TabCompleter {

    private final NoLoadingScreen plugin;

    public LSRCommand(NoLoadingScreen plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.getSettings().reload();
            // The original sent the literal text "&bLoadingScreenRemover ..."
            // because legacy codes are not translated on the way out.
            sender.sendMessage(ChatColor.AQUA + "NoLoadingScreen configuration reloaded.");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " reload");
        return true;
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        return args.length == 1 ? List.of("reload") : List.of();
    }
}
