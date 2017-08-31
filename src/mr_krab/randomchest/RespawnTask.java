package mr_krab.randomchest;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;

public class RespawnTask implements Runnable {
	private int taskId;
	private ConcurrentHashMap<Chest, Long[]> respawnDelay;

	public RespawnTask(ConcurrentHashMap<Chest, Long[]> rD) {
		taskId = -1;
		respawnDelay = rD;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		for (Chest chest : respawnDelay.keySet()) {
			Long delay[] = respawnDelay.get(chest);
			if (System.currentTimeMillis() - delay[0] > delay[1]) {
				respawnDelay.remove(chest);
				chest.getBlock().setType(Material.CHEST);
				chest.getBlock().setData(chest.getData().getData());
			}
		}
	}

	public void setTaskID(int id) {
		taskId = id;
	}

	public void cancel() {
		Bukkit.getScheduler().cancelTask(taskId);
	}
}
