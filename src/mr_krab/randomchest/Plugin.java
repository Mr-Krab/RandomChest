package mr_krab.randomchest;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {
	public Locale loc = new Locale(this); {
	loc.init();
	}
	private Utils utils;

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		if (utils != null) {
			utils.forceChestsRespawn();
		}
	}

	@Override
	public void onEnable() {
		// create the configuration if there is no
		saveDefaultConfig();

		// Cookies
		utils = new Utils(this);

		// Register commands
		CmdExecutor cmde = new CmdExecutor(this);
		String[] comands = { "chest" };
		for (String cmd : comands) {
			getCommand(cmd).setExecutor(cmde);
		}

		// Register events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(this), this);

		// scheduler respawn chests
		utils.startChestsRespawn();
	}


	public Utils getUtils() {
		return utils;
	}
}
