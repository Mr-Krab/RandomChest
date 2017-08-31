package mr_krab.randomchest;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class PlayerListener implements Listener {
	Plugin plugin;
	Utils utils;

	public PlayerListener(Plugin p) {
		plugin = p;
		utils = p.getUtils();
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();
		Block block = event.getClickedBlock();
		int selectTool = plugin.getConfig().getInt("select-tool");
		int removeTool = plugin.getConfig().getInt("remove-tool");
		if (action.equals(Action.RIGHT_CLICK_BLOCK)
				&& block.getType().equals(Material.CHEST)) {
			Chest chest = (Chest) block.getState();
			if (utils.haveSelectedType(player)
					&& player.getItemInHand().getTypeId() == selectTool
					&& player.getGameMode().equals(GameMode.CREATIVE)) {
				utils.addChest(player, block);
				player.sendMessage(plugin.loc.getString("Prefix") + plugin.loc.getString("Add"));
				event.setCancelled(true);
			} else if (player.getItemInHand().getTypeId() == removeTool
					&& player.getGameMode().equals(GameMode.CREATIVE)) {
				utils.removeChest(player, block);
				player.sendMessage(plugin.loc.getString("Prefix") + plugin.loc.getString("Remove"));
				event.setCancelled(true);
			} else {
				String type = utils.getChestType(block);
				if (type != null) {
					if (player.hasPermission("randomchest.use")) {
						if (utils.isSetExist(type)) {
							int respawn = utils.getRespawnDelay(type);

							if (utils.canRefill(chest)) {
								utils.fill(chest, type);
								utils.addRefillDelay(chest, respawn * 1000);
							}
						} else {
							player.sendMessage(plugin.loc.getString("Prefix") + plugin.loc.getString("TypeNotFound").replace("%type", type));
						}
					} else {
						event.setCancelled(true);
						player.sendMessage(plugin.loc.getString("NoPermissions"));
					}
				}
			}
		} else if (action.equals(Action.LEFT_CLICK_BLOCK)
				&& block.getType().equals(Material.CHEST)) {
			Chest chest = (Chest) block.getState();
			String type = utils.getChestType(block);
			if (type != null) {
				if (player.hasPermission("randomchest.use")) {
					if (utils.isSetExist(type)) {
						if (utils.canBreak(type)) {
							int respawn = utils.getRespawnDelay(type);

							if (utils.canRefill(chest)) {
								utils.fill(chest, type);
								utils.addRefillDelay(chest, respawn * 1000);
							}
							utils.addRespawnDelay(chest, respawn * 1000);

							block.setType(Material.AIR);
						}
					}
				} else {
					event.setCancelled(true);
					player.sendMessage(plugin.loc.getString("NoPermissions"));
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		if (block.getType().equals(Material.CHEST)) {
			String type = utils.getChestType(block);
			if (type != null) {
				if (utils.isSetExist(type)) {
					event.setCancelled(!utils.canBreak(type));
				} else {
					player.sendMessage(plugin.loc.getString("Prefix") + plugin.loc.getString("TypeNotFound").replace("%type", type));
				}
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		Inventory inv = event.getInventory();
		Block block = player.getTargetBlock(null, 5);
		if (inv.getType().equals(InventoryType.CHEST)
				&& block.getType().equals(Material.CHEST)) {
			Chest chest = (Chest) block.getState();
			String type = utils.getChestType(block);
			if (type != null) {
				if (player.hasPermission("randomchest.use")) {
					if (utils.isSetExist(type)) {
						if (utils.canBreak(type)) {
							int respawn = utils.getRespawnDelay(type);

							if (utils.canRefill(chest)) {
								utils.fill(chest, type);
								utils.addRefillDelay(chest, respawn * 1000);
							}
							utils.addRespawnDelay(chest, respawn * 1000);

							block.setType(Material.AIR);
						}
					}
				} else {
					player.sendMessage(plugin.loc.getString("NoPermissions"));
				}
			}
		}
	}
}
