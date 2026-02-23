package dev.nickplugin;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NickManager {

    private final NickPlugin plugin;
    private final File dataFile;
    private YamlConfiguration yaml;

    private final Map<UUID, String> nicks = new HashMap<>();

    public NickManager(NickPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "nicks.yml");
        load();
    }


    private void load() {
        plugin.getDataFolder().mkdirs();
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create nicks.yml: " + e.getMessage());
            }
        }
        yaml = YamlConfiguration.loadConfiguration(dataFile);
        nicks.clear();
        for (String key : yaml.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String nick = yaml.getString(key);
                if (nick != null && !nick.isBlank()) {
                    nicks.put(uuid, nick);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        plugin.getLogger().info("Loaded " + nicks.size() + " nickname(s) from nicks.yml.");
    }

    private void persist(UUID uuid, String nick) {
        if (nick == null) {
            yaml.set(uuid.toString(), null);
        } else {
            yaml.set(uuid.toString(), nick);
        }
        try {
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save nicks.yml: " + e.getMessage());
        }
    }


    public void setNick(UUID uuid, String nick) {
        nicks.put(uuid, nick);
        persist(uuid, nick);
    }

    public void removeNick(UUID uuid) {
        nicks.remove(uuid);
        persist(uuid, null);
    }

    public String getNick(UUID uuid) {
        return nicks.get(uuid);
    }

    public boolean hasNick(UUID uuid) {
        return nicks.containsKey(uuid);
    }


    public String getEffectiveName(UUID uuid, String realName) {
        String nick = nicks.get(uuid);
        return nick != null ? nick : realName;
    }
}
