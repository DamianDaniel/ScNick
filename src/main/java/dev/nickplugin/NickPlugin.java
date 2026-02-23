package dev.nickplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class NickPlugin extends JavaPlugin {

    private NickManager nickManager;


    @Override
    public void onEnable() {
        nickManager = new NickManager(this);

        // Commands
        NickCommand cmd = new NickCommand(this);
        getCommand("nick").setExecutor(cmd);
        getCommand("nick").setTabCompleter(cmd);

        // Events
        getServer().getPluginManager().registerEvents(new NickListener(this), this);

        // Handle /reload — re-apply nicks to any already-online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            String nick = nickManager.getNick(player.getUniqueId());
            if (nick != null) {
                applyNick(player, nick);
            }
        }

        getLogger().info("NickPlugin enabled successfully.");
    }

    @Override
    public void onDisable() {
        getLogger().info("NickPlugin disabled.");
    }


    public NickManager getNickManager() {
        return nickManager;
    }


    public void applyNick(Player player, String nick) {
        Component nickComp = Component.text(nick);
        player.displayName(nickComp);
        player.playerListName(nickComp);
    }

    public void resetNick(Player player) {
        Component realName = Component.text(player.getName());
        player.displayName(realName);
        player.playerListName(realName);
    }
}
