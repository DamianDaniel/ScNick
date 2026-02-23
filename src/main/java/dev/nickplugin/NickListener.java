package dev.nickplugin;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;


public class NickListener implements Listener {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final NickPlugin plugin;

    public NickListener(NickPlugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        NickManager mgr = plugin.getNickManager();


        if (mgr.hasNick(uuid)) {
            plugin.applyNick(player, mgr.getNick(uuid));
        } else {
            plugin.resetNick(player);
        }

        Component joinMsg = event.joinMessage();
        if (joinMsg != null) {
            event.joinMessage(replaceAllNicks(joinMsg));
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        Component quitMsg = event.quitMessage();
        if (quitMsg != null) {
            event.quitMessage(replaceAllNicks(quitMsg));
        }
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        NickManager mgr = plugin.getNickManager();

        if (mgr.hasNick(sender.getUniqueId())) {
            String nick = mgr.getNick(sender.getUniqueId());
            sender.displayName(Component.text(nick));
        }

        // 2. Replace any real-name mentions in the message body
        Component message = event.message();
        message = replaceAllNicks(message);
        event.message(message);
    }



    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {
        Component deathMsg = event.deathMessage();
        if (deathMsg == null) return;
        event.deathMessage(replaceAllNicks(deathMsg));
    }


    private Component replaceAllNicks(Component component) {
        NickManager mgr = plugin.getNickManager();
        for (Player online : Bukkit.getOnlinePlayers()) {
            String nick = mgr.getNick(online.getUniqueId());
            if (nick == null) continue;
            String realName = online.getName();
            if (realName.equals(nick)) continue;

            component = component.replaceText(
                    TextReplacementConfig.builder()
                            .matchLiteral(realName)
                            .replacement(Component.text(nick))
                            .build()
            );
        }
        return component;
    }
}
