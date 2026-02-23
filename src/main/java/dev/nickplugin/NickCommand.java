package dev.nickplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class NickCommand implements CommandExecutor, TabCompleter {

    private static final int MAX_NICK_LENGTH = 32;
    private final NickPlugin plugin;

    public NickCommand(NickPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("nickplugin.nick")) {
            sender.sendMessage(Component.text("You don't have permission to use /nick.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /nick <player> <nick|reset>", NamedTextColor.RED));
            return true;
        }

        String targetArg = args[0];
        String nickArg   = args[1];

        Player target = Bukkit.getPlayerExact(targetArg);
        if (target == null) {
            sender.sendMessage(Component.text("Player '" + targetArg + "' is not online.", NamedTextColor.RED));
            return true;
        }

        NickManager mgr = plugin.getNickManager();


        if (nickArg.equalsIgnoreCase("reset")) {
            if (!mgr.hasNick(target.getUniqueId())) {
                sender.sendMessage(Component.text(target.getName() + " has no nickname to reset.", NamedTextColor.YELLOW));
                return true;
            }
            String old = mgr.getNick(target.getUniqueId());
            mgr.removeNick(target.getUniqueId());
            plugin.resetNick(target);
            sender.sendMessage(Component.text("Removed nickname '" + old + "' from " + target.getName() + ".", NamedTextColor.GREEN));
            target.sendMessage(Component.text("Your nickname has been removed.", NamedTextColor.YELLOW));
            return true;
        }

        if (nickArg.length() > MAX_NICK_LENGTH) {
            sender.sendMessage(Component.text("Nickname must be " + MAX_NICK_LENGTH + " characters or fewer.", NamedTextColor.RED));
            return true;
        }
        if (!nickArg.matches("[a-zA-Z0-9_ ]+")) {
            sender.sendMessage(Component.text("Nickname may only contain letters, numbers, underscores and spaces.", NamedTextColor.RED));
            return true;
        }


        mgr.setNick(target.getUniqueId(), nickArg);
        plugin.applyNick(target, nickArg);

        sender.sendMessage(Component.text("Set nickname of " + target.getName() + " to '" + nickArg + "'.", NamedTextColor.GREEN));
        if (!sender.equals(target)) {
            target.sendMessage(Component.text("Your nickname has been set to: ", NamedTextColor.YELLOW)
                    .append(Component.text(nickArg, NamedTextColor.GOLD)));
        }
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (!sender.hasPermission("nickplugin.nick")) return result;

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(partial)) {
                    result.add(p.getName());
                }
            }
        } else if (args.length == 2) {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target != null) {
                NickManager mgr = plugin.getNickManager();
                if (mgr.hasNick(target.getUniqueId())) {
                    result.add("reset");
                    result.add(mgr.getNick(target.getUniqueId()));
                } else {
                    result.add(target.getName());
                }
            }
        }
        return result;
    }
}
