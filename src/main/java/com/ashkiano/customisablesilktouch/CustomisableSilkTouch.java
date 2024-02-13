package com.ashkiano.customisablesilktouch;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

//TODO přidat cusomiableSilktouch.break.<type> permise
//TODO přidat do configu seznam bloků co nepůjdou vykopat i když normálně jdou
//TODO přidat configurovatelnost materiálu krumpáče, který je potřeba na vykopání
//TODO přidat možnost upravovat silk touch i na jiných nástrojích než krumpáči
public class CustomisableSilkTouch extends JavaPlugin implements Listener {

    private List<String> silkTouchBlocks;
    private String silkTouchPermission;
    private boolean showDonateMessage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        getServer().getPluginManager().registerEvents(this, this);

        Metrics metrics = new Metrics(this, 19020);

        if (showDonateMessage) {
            this.getLogger().info("Thank you for using the CustomisableSilkTouch plugin! If you enjoy using this plugin, please consider making a donation to support the development. You can donate at: https://donate.ashkiano.com");
        }

        checkForUpdates();
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        silkTouchBlocks = config.getStringList("silk_touch_blocks");
        silkTouchPermission = config.getString("silk_touch_permission", "customisablesilktouch.use");
        showDonateMessage = config.getBoolean("ShowDonateMessage", true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material blockType = event.getBlock().getType();

        if (!silkTouchBlocks.contains(blockType.name())) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.hasPermission(silkTouchPermission)) {
            return;
        }

        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();

        if (tool.getType() != Material.DIAMOND_PICKAXE && tool.getType() != Material.NETHERITE_PICKAXE) {
            return;
        }

        if (!tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
            return;
        }

        event.setDropItems(false);

        if (blockType == Material.SPAWNER) {
            CreatureSpawner spawner = (CreatureSpawner) event.getBlock().getState();
            ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
            ItemMeta meta = spawnerItem.getItemMeta();
            meta.setDisplayName(spawner.getSpawnedType().name() + "_SPAWNER");
            spawnerItem.setItemMeta(meta);
            player.getWorld().dropItemNaturally(event.getBlock().getLocation(), spawnerItem);
        } else {
            player.getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(blockType));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        ItemMeta meta = item.getItemMeta();

        if (item.getType() == Material.SPAWNER && meta.hasDisplayName()) {
            String displayName = meta.getDisplayName();
            EntityType type = EntityType.valueOf(displayName.replace("_SPAWNER", ""));

            Block block = event.getBlockPlaced();
            BlockState blockState = block.getState();

            if (blockState instanceof CreatureSpawner) {
                CreatureSpawner spawner = (CreatureSpawner) blockState;
                spawner.setSpawnedType(type);
                spawner.update();
            }
        }
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