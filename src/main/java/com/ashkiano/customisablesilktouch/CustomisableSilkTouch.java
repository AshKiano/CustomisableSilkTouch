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

import java.util.List;

//TODO přidat cusomiableSilktouch.break.<type> permise
//TODO přidat do configu seznam bloků co nepůjdou vykopat i když normálně jdou
//TODO přidat configurovatelnost materiálu krumpáče, který je potřeba na vykopání
//TODO přidat možnost upravovat silk touch i na jiných nástrojích než krumpáči
public class CustomisableSilkTouch extends JavaPlugin implements Listener {

    private List<String> silkTouchBlocks;
    private String silkTouchPermission;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        getServer().getPluginManager().registerEvents(this, this);

        Metrics metrics = new Metrics(this, 19020);

        System.out.println("Thank you for using the CustomisableSilkTouch plugin! If you enjoy using this plugin, please consider making a donation to support the development. You can donate at: https://paypal.me/josefvyskocil");
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        silkTouchBlocks = config.getStringList("silk_touch_blocks");
        silkTouchPermission = config.getString("silk_touch_permission", "customisablesilktouch.use");
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
}