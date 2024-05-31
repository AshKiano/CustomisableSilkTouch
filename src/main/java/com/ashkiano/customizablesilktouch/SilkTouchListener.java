package com.ashkiano.customizablesilktouch;

import com.ashkiano.customizablesilktouch.data.SilkTouchData;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class SilkTouchListener implements Listener {

    private final CustomizableSilkTouch plugin;

    public SilkTouchListener(CustomizableSilkTouch plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        SilkTouchData silkTouchData = plugin.getDataByTool(tool.getType());

        if (silkTouchData == null) {
            return;
        }

        Material blockType = event.getBlock().getType();

        if (!silkTouchData.getSilkTouchBlocks().contains(blockType) && !event.getPlayer().hasPermission("customizablesilktouch.break." + blockType.name())) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.hasPermission(plugin.getConfig().getString("Silk-Touch-Permission"))) {
            return;
        }

        if (!tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
            return;
        }

        event.setDropItems(false);

        if (silkTouchData.getBlockedBlocks().containsKey(blockType)) {
            player.getWorld().dropItemNaturally(event.getBlock().getLocation(),
                    new ItemStack(silkTouchData.getBlockedBlocks().get(blockType)));
            return;
        }

        if (blockType == Material.SPAWNER) {
            CreatureSpawner spawnerBlock = (CreatureSpawner) event.getBlock().getState();

            ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
            BlockStateMeta blockStateMeta = (BlockStateMeta) spawnerItem.getItemMeta();
            CreatureSpawner spawnerMeta = (CreatureSpawner) blockStateMeta.getBlockState();
            spawnerMeta.setSpawnedType(spawnerBlock.getSpawnedType());
            blockStateMeta.setBlockState(spawnerMeta);
            spawnerItem.setItemMeta(blockStateMeta);

            player.getWorld().dropItemNaturally(event.getBlock().getLocation(), spawnerItem);
            return;
        }

        player.getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(blockType));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != Material.SPAWNER) {
            return;
        }

        ItemStack item = event.getItemInHand();
        BlockStateMeta blockStateMeta = (BlockStateMeta) item.getItemMeta();
        CreatureSpawner spawnerMeta = (CreatureSpawner) blockStateMeta.getBlockState();

        CreatureSpawner spawnerData = (CreatureSpawner) event.getBlock().getState();
        spawnerData.setSpawnedType(spawnerMeta.getSpawnedType());
        spawnerData.update();
    }

}
