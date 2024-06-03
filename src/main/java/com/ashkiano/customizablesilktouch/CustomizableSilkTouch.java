package com.ashkiano.customizablesilktouch;

import com.ashkiano.customizablesilktouch.command.CSTCommand;
import com.ashkiano.customizablesilktouch.data.SilkTouchData;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class CustomizableSilkTouch extends JavaPlugin {

    private final HashMap<String, SilkTouchData> silkTouchDataMap = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reload();

        getServer().getPluginManager().registerEvents(new SilkTouchListener(this), this);

        new Metrics(this, 19020);
        checkForUpdates();

        getCommand("customizablesilktouch").setExecutor(new CSTCommand(this));

        if (!getConfig().getBoolean("Show-Donate-Message")) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            Bukkit.getConsoleSender().sendMessage(
                    ChatColor.GOLD + "Thank you for using the " + getName() + " plugin!",
                    ChatColor.GOLD + "If you enjoy using this plugin!",
                    ChatColor.GOLD + "Please consider making a donation to support the development!",
                    ChatColor.GOLD + "You can donate at: " + ChatColor.GREEN + "https://donate.ashkiano.com"
            );
        }, 20);
    }

    public void reload() {
        silkTouchDataMap.clear();
        reloadConfig();

        ConfigurationSection toolsSection = getConfig().getConfigurationSection("Silk-Touch-Tools");

        if (toolsSection == null) {
            getLogger().severe("Tool configuration section missing!");
            return;
        }

        for (String toolId : toolsSection.getKeys(false)) {
            List<Material> toolMaterials = new ArrayList<>();
            toolsSection.getStringList(toolId + ".Materials").forEach(material -> toolMaterials.add(Material.valueOf(material)));

            List<Material> silkTouchBlocks = new ArrayList<>();
            toolsSection.getStringList(toolId + ".Silk-Touch-Blocks").forEach(material -> silkTouchBlocks.add(Material.valueOf(material)));

            HashMap<Material, Material> blockedBlocks = new HashMap<>();
            toolsSection.getStringList(toolId + ".Blocked-Blocks").forEach(material -> {
                String[] materialData = material.split(":");
                blockedBlocks.put(Material.valueOf(materialData[0]), Material.valueOf(materialData[1]));
            });

            silkTouchDataMap.put(toolId, new SilkTouchData(toolId, toolMaterials, silkTouchBlocks, blockedBlocks));
        }
    }

    public SilkTouchData getDataByTool(Material material) {
        return silkTouchDataMap.values().stream().filter(data -> data.getToolItems().contains(material)).findFirst().orElse(null);
    }

    private void checkForUpdates() {
        try {
            String pluginName = this.getDescription().getName();
            URL url = new URL("https://www.ashkiano.com/version_check.php?plugin=" + pluginName);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String jsonResponse = response.toString();
                JSONObject jsonObject = new JSONObject(jsonResponse);
                if (jsonObject.has("error")) {
                    this.getLogger().warning("Error when checking for updates: " + jsonObject.getString("error"));
                } else {
                    String latestVersion = jsonObject.getString("latest_version");

                    String currentVersion = this.getDescription().getVersion();
                    if (currentVersion.equals(latestVersion)) {
                        this.getLogger().info("This plugin is up to date!");
                    } else {
                        this.getLogger().warning("There is a newer version (" + latestVersion + ") available! Please update!");
                    }
                }
            } else {
                this.getLogger().warning("Failed to check for updates. Response code: " + responseCode);
            }
        } catch (Exception e) {
            this.getLogger().warning("Failed to check for updates. Error: " + e.getMessage());
        }
    }
}