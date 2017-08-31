package mr_krab.randomchest;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdExecutor implements CommandExecutor {

	Plugin plugin;
	Utils utils;

	public CmdExecutor(Plugin p) {
		plugin = p;
		utils = p.getUtils();
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (cmd.getName().equals("chest") && args.length >= 1) {
			if (args[0].equalsIgnoreCase("reload")) {
				plugin.reloadConfig();
				utils.reloadChestDB();
				plugin.loc.init();
				sender.sendMessage(plugin.loc.getString("Prefix") + plugin.loc.getString("Reload"));
				return true;
			} else if (args[0].equalsIgnoreCase("random")) {
				sender.sendMessage(plugin.loc.getString("Prefix") + ChatColor.GRAY + utils.random(1, 100));
				return true;
			} else if (args[0].equalsIgnoreCase("select") && args.length >= 2) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (plugin.getConfig().contains("chestset." + args[1])) {
						int tool = plugin.getConfig().getInt("select-tool");
						utils.selectType(player, args[1]);
						sender.sendMessage(plugin.loc.getString("Prefix") + plugin.loc.getString("Selected").replace("%type", args[1]).replace("%item", Material.getMaterial(tool).name()));
					} else {
						sender.sendMessage(plugin.loc.getString("Prefix") + plugin.loc.getString("TypeNotFound").replace("%type", args[1]));
					}
					return true;
				}
			} else if (args[0].equalsIgnoreCase("unselect")) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					utils.removeSelectedType(player);
					sender.sendMessage(plugin.loc.getString("Prefix") + plugin.loc.getString("Unselect"));
					return true;
				}
			} else if (args[0].equalsIgnoreCase("restore")) {
				if (sender instanceof Player) {
					utils.restoreAllChests();
					sender.sendMessage(plugin.loc.getString("Prefix") + plugin.loc.getString("Restore"));
					return true;
				}
			}
		} else {
			if (sender instanceof Player) {
				// Player player = (Player) sender;
				// wrong comand
			} else {
				sender.sendMessage(plugin.loc.getString("Prefix") + plugin.loc.getString("OnlyGame"));
			}
			return true;
		}
		return false;
	}
}
