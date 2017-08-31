package mr_krab.randomchest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;

@SuppressWarnings("deprecation")
public class Utils {
	private Plugin plugin;
	private Random rnd;
	private HashMap<Chest, Long[]> refillDelay;
	private ConcurrentHashMap<Chest, Long[]> respawnDelay;
	private HashMap<Player, String> selectType;
	private BukkitTask task;
	private YamlConfiguration chests;
	private boolean debug;

	public Utils(Plugin p) {
		plugin = p;
		rnd = new Random();
		refillDelay = new HashMap<Chest, Long[]>();
		respawnDelay = new ConcurrentHashMap<Chest, Long[]>();
		selectType = new HashMap<Player, String>();
		chests = getDB("chestDB.yml");
		debug = false;
	}

	public void reloadChestDB() {
		chests = getDB("chestDB.yml");
	}

	// not work
	public Block getBlockByKey(String key) {
		String[] split = key.split(",");
		return Bukkit.getWorld(split[0]).getBlockAt(str2int(split[1], 0),
				str2int(split[2], 0), str2int(split[3], 0));
	}

	public void restoreAllChests() {
		for (String key : chests.getConfigurationSection("chest")
				.getKeys(false)) {
			getBlockByKey(key).setType(Material.CHEST);
		}
	}

	public void addChest(Player player, Block block) {
		String chestKey = "chest." + block.getWorld().getName() + ","
				+ block.getX() + "," + block.getY() + "," + block.getZ();
		chests.set(chestKey, getSelectedType(player));
		saveDB("chestDB.yml", chests);
	}

	public void removeChest(Player player, Block block) {
		String chestKey = "chest." + block.getWorld().getName() + ","
				+ block.getX() + "," + block.getY() + "," + block.getZ();
		chests.set(chestKey, null);
		respawnDelay.remove((Chest) block.getState());
		saveDB("chestDB.yml", chests);
	}

	public String getChestType(Block block) {
		String chestKey = "chest." + block.getWorld().getName() + ","
				+ block.getX() + "," + block.getY() + "," + block.getZ();
		if (chests.contains(chestKey)) {
			return chests.getString(chestKey);
		}
		return null;
	}

	public void selectType(Player player, String type) {
		selectType.put(player, type);
	}

	public String getSelectedType(Player player) {
		return selectType.get(player);
	}

	public boolean haveSelectedType(Player player) {
		return selectType.containsKey(player);
	}

	public void removeSelectedType(Player player) {
		selectType.remove(player);
	}

	public void addRefillDelay(Chest chest, long delay) {
		Long l[] = { System.currentTimeMillis(), delay };
		refillDelay.put(chest, l);
	}

	public boolean canRefill(Chest chest) {
		if (refillDelay.containsKey(chest)) {
			Long delay[] = refillDelay.get(chest);
			if (System.currentTimeMillis() - delay[0] > delay[1]) {
				refillDelay.remove(chest);
				return true;
			}
		} else {
			return true;
		}
		return false;
	}

	public void addRespawnDelay(Chest chest, long delay) {
		Long l[] = { System.currentTimeMillis(), delay };
		respawnDelay.put(chest, l);
	}

	public int getRespawnDelay(String type) {
		List<Integer> respawnDelay = plugin.getConfig().getIntegerList(
				"chestset." + type.toLowerCase() + ".respawn");
		return respawnDelay != null ? respawnDelay.get(random(0,
				respawnDelay.size() - 1)) : 30;
	}

	public boolean canBreak(String type) {
		return plugin.getConfig().getBoolean(
				"chestset." + type.toLowerCase() + ".break", false);
	}

	public void forceChestsRespawn() {
		for (Chest chest : respawnDelay.keySet()) {
			respawnDelay.remove(chest);
			chest.getBlock().setType(Material.CHEST);
			chest.getBlock().setData(chest.getData().getData());
		}
	}

	public void startChestsRespawn() {
		task = plugin.getServer().getScheduler()
				.runTaskTimer(plugin, new RespawnTask(respawnDelay), 20L, 20L);
	}

	public BukkitTask getTask() {
		return task;
	}

	@SuppressWarnings({ "unchecked" })
	public void fill(Chest chest, String type) {
		int min = plugin.getConfig().getInt("chestset." + type + ".min", 1);
		int max = plugin.getConfig().getInt("chestset." + type + ".max", 4);
		String customName = plugin.getConfig().getString(
				"chestset." + type + ".customname", null);

		// Loot that we will randomize
		List<?> items = plugin.getConfig().getList(
				"chestset." + type + ".items");

		// Open the chest
		Inventory inv = chest.getInventory();
		if (customName != null) {
			// CraftInventory craftInv=(CraftInventory) inv;
			// net.minecraft.server.v1_5_R2.IInventory iinv = (IInventory)
			// craftInv;
			// inv = Bukkit.createInventory(null, inv.getSize(), customName);
		}
		// Clear the chest
		inv.clear();

		// How many things will be in chest
		int count = random(min, max);

		// Fill things up
		for (int i = 0; i < count; i++) {
			// Random item
			Map<?, ?> item = (Map<?, ?>) items.get(random(0, items.size() - 1));
			int id = (Integer) (item.get("id") != null ? item.get("id") : 0);
			int data = (Integer) (item.get("data") != null ? item.get("data")
					: 0);
			List<Integer> durability = (List<Integer>) (item.get("durability") != null ? item
					.get("durability") : new ArrayList<Integer>());
			boolean randomEnchant = (Boolean) (item.get("random-enchant") != null ? item
					.get("random-enchant") : false);
			List<?> enchantments = (List<?>) (item.get("enchantments") != null ? item
					.get("enchantments") : new ArrayList<Object>());
			String potionType = (String) (item.get("potion-type") != null ? item
					.get("potion-type") : PotionType.WATER.toString());
			int potionLevel = (Integer) (item.get("potion-level") != null ? item
					.get("potion-level") : 1);
			boolean potionSplash = (Boolean) (item.get("potion-splash") != null ? item
					.get("potion-splash") : false);
			String skull = (String) (item.get("skull") != null ? item
					.get("skull") : null);
			String name = (String) (item.get("name") != null ? item.get("name")
					: null);
			List<String> lore = (List<String>) (item.get("lore") != null ? item
					.get("lore") : new ArrayList<Object>());
			int amount = (Integer) (item.get("amount") != null ? item
					.get("amount") : 1);

			if (id > 0) {
				// create item
				ItemStack stack = new ItemStack(id, amount);
				stack.setDurability((short) data);

				// If the potion
				if (stack.getType().equals(Material.POTION)) {
					Potion potion = new Potion(PotionType.WATER);
					PotionType pType = PotionType.valueOf(potionType);
					if (pType != null) {
						potion.setType(pType);
					}
					if (potion.getType().equals(PotionType.WATER)) {
						potion.setSplash(false);
					} else {
						potionLevel = potionLevel > 0 ? potionLevel : 1;
						potionLevel = potionLevel > potion.getType()
								.getMaxLevel() ? potion.getType().getMaxLevel()
								: potionLevel;
						potion.setSplash(potionSplash);
						potion.setLevel(potionLevel);
					}
					stack = potion.toItemStack(amount);
				} else {
					// randomly enchant
					if (randomEnchant) {
						// enchant Bow
						if (isBow(stack)) {
							randomEnchantBow(stack);
						} else
						// Enchant Sword
						if (isSword(stack)) {
							randomEnchantSword(stack);
						} else
						// Enchant Helmet
						if (isHelmet(stack)) {
							randomEnchantHelmet(stack);
						} else
						// Enchant Boots
						if (isBoots(stack)) {
							randomEnchantBoots(stack);
						} else
						// Enchant Armor
						if (isArmor(stack)) {
							randomEnchantArmor(stack);
						}
					} else {
						for (Object e : enchantments) {
							Map<?, ?> enchant = (Map<?, ?>) e;
							String enchantname = (String) (enchant.get("name") != null ? enchant
									.get("name") : null);
							int enchantlvl = (Integer) (enchant.get("level") != null ? enchant
									.get("level") : 1);
							Enchantment ench = Enchantment
									.getByName(enchantname);
							if (ench != null) {
								enchantlvl = enchantlvl < ench.getStartLevel() ? ench
										.getStartLevel() : enchantlvl;
								enchantlvl = enchantlvl > ench.getMaxLevel() ? ench
										.getMaxLevel() : enchantlvl;
								stack.addUnsafeEnchantment(ench, enchantlvl);
							}
						}
					}

					// Diapason durability for item
					if (durability.size() == 2) {
						randomDiapasonDurability(stack, durability.get(0),
								durability.get(1));
					}
				}

				ItemMeta itemMeta = stack.getItemMeta();
				// rename item
				if (name != null) {
					itemMeta.setDisplayName(name);
					;
				}

				// Lore item
				if (lore != null) {
					itemMeta.setLore(lore);
				}

				// skull player
				if (skull != null) {
					stack.setDurability((short) 3);
					((SkullMeta) itemMeta).setOwner(skull);
				}
				stack.setItemMeta(itemMeta);

				// add item in chest
				// inv.addItem(stack);
				addItemToRandomSlot(inv, stack);
			}
		}
	}

	public void addItemToRandomSlot(Inventory inv, ItemStack item) {
		int index = random(0, inv.getSize() - 1);
		if (inv.getItem(index) != null) {
			addItemToRandomSlot(inv, item);
		} else {
			inv.setItem(index, item);
		}
	}

	// Diapason durability for armor or weapon
	public void randomDiapasonDurability(ItemStack item, int minPercent,
			int maxPercent) {

		double maxD = item.getType().getMaxDurability();

		int min = (int) (maxD / 100 * minPercent);
		int max = (int) (maxD / 100 * maxPercent);
		if (maxD != 0) {
			item.setDurability((short) random(min, max));
		}
	}

	// Enchant armor
	public void randomEnchantArmor(ItemStack item) {
		Enchantment[] enchantment = { Enchantment.PROTECTION_ENVIRONMENTAL,
				Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_EXPLOSIONS,
				Enchantment.PROTECTION_PROJECTILE };
		int count = random(1, enchantment.length);
		for (int i = 0; i < count; i++) {
			int rnd = random(0, enchantment.length - 1);
			int level = random(enchantment[rnd].getStartLevel(),
					enchantment[rnd].getMaxLevel());
			item.addUnsafeEnchantment(enchantment[rnd], level);
		}
	}

	// Enchant boots
	public void randomEnchantBoots(ItemStack item) {
		Enchantment[] enchantment = { Enchantment.PROTECTION_FALL };
		int count = random(1, enchantment.length);
		for (int i = 0; i < count; i++) {
			int rnd = random(0, enchantment.length - 1);
			int level = random(enchantment[rnd].getStartLevel(),
					enchantment[rnd].getMaxLevel());
			item.addUnsafeEnchantment(enchantment[rnd], level);
		}
	}

	// Enchant sword
	public void randomEnchantSword(ItemStack item) {
		Enchantment[] enchantment = { Enchantment.DAMAGE_ALL,
				Enchantment.DAMAGE_UNDEAD, Enchantment.DAMAGE_ARTHROPODS,
				Enchantment.KNOCKBACK, Enchantment.FIRE_ASPECT };
		int count = random(1, enchantment.length);
		for (int i = 0; i < count; i++) {
			int rnd = random(0, enchantment.length - 1);
			int level = random(enchantment[rnd].getStartLevel(),
					enchantment[rnd].getMaxLevel());
			item.addUnsafeEnchantment(enchantment[rnd], level);
		}
	}

	// Enchant bow
	public void randomEnchantBow(ItemStack item) {
		Enchantment[] enchantment = { Enchantment.ARROW_DAMAGE,
				Enchantment.ARROW_KNOCKBACK, Enchantment.ARROW_FIRE,
				Enchantment.ARROW_INFINITE };
		int count = random(1, enchantment.length);
		for (int i = 0; i < count; i++) {
			int rnd = random(0, enchantment.length - 1);
			int level = random(enchantment[rnd].getStartLevel(),
					enchantment[rnd].getMaxLevel());
			item.addUnsafeEnchantment(enchantment[rnd], level);
		}
	}

	// Enchant helmet
	public void randomEnchantHelmet(ItemStack item) {
		Enchantment[] enchantment = { Enchantment.OXYGEN,
				Enchantment.WATER_WORKER };
		int count = random(1, enchantment.length);
		for (int i = 0; i < count; i++) {
			int rnd = random(0, enchantment.length - 1);
			int level = random(enchantment[rnd].getStartLevel(),
					enchantment[rnd].getMaxLevel());
			item.addUnsafeEnchantment(enchantment[rnd], level);
		}
	}

	public boolean isArmor(ItemStack stack) {
		if (stack.getType().equals(Material.LEATHER_CHESTPLATE)
				|| stack.getType().equals(Material.IRON_CHESTPLATE)
				|| stack.getType().equals(Material.DIAMOND_CHESTPLATE)) {
			return true;
		}
		return false;
	}

	public boolean isBoots(ItemStack stack) {
		if (stack.getType().equals(Material.LEATHER_BOOTS)
				|| stack.getType().equals(Material.IRON_BOOTS)
				|| stack.getType().equals(Material.DIAMOND_BOOTS)) {
			return true;
		}
		return false;
	}

	public boolean isHelmet(ItemStack stack) {
		if (stack.getType().equals(Material.LEATHER_HELMET)
				|| stack.getType().equals(Material.IRON_HELMET)
				|| stack.getType().equals(Material.DIAMOND_HELMET)) {
			return true;
		}
		return false;
	}

	public boolean isSword(ItemStack stack) {
		if (stack.getType().equals(Material.WOOD_SWORD)
				|| stack.getType().equals(Material.STONE_SWORD)
				|| stack.getType().equals(Material.IRON_SWORD)
				|| stack.getType().equals(Material.DIAMOND_SWORD)) {
			return true;
		}
		return false;
	}

	public boolean isBow(ItemStack stack) {
		if (stack.getType().equals(Material.BOW)) {
			return true;
		}
		return false;
	}

	public boolean isSetExist(String set) {
		Set<String> chestset = plugin.getConfig()
				.getConfigurationSection("chestset").getKeys(false);
		if (chestset.contains(set.toLowerCase())) {
			return true;
		}
		return false;
	}

	public YamlConfiguration getDB(String name) {
		File config = new File(plugin.getDataFolder(), name);
		YamlConfiguration ymlcnf;

		if (!config.exists()) {
			ymlcnf = new YamlConfiguration();
			saveDB(name, ymlcnf);
		} else {
			ymlcnf = YamlConfiguration.loadConfiguration(config);
		}
		return ymlcnf;
	}

	public void saveDB(String name, YamlConfiguration ymlcnf) {
		File config = new File(plugin.getDataFolder(), name);
		try {
			ymlcnf.save(config);
		} catch (Exception e) {
		}
	}

	public int str2int(String s, int def) {
		if (s == null) {
			return def;
		}
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	public void debug(String s) {
		if (debug) {
			System.out.println(s);
		}
	}

	public int random(int min, int max) {
		if (min == 0 && max == 0) {
			return 0;
		}
		return rnd.nextInt(max - min + 1) + min;
	}
}
