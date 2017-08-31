package mr_krab.randomchest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Скорее всего, это финальная версия класса. 
 * Окей, что это? Это класс-помошник, который поможет вам разобраться
 * со своими языковыми пакетами. Т.е. вместо использования yaml файлов,
 * которые зависимы от кодировки чтения файла, вы можете спокойно писать
 * свои локали в .properties файл не заботясь об кодировке файла вообще.
 * http://java-properties-editor.com/ - прога для редактирования ;) 
 *
 * Т.к. писался этот класс для использования в плагинах под Bukkit API,
 * тут используются методы из вышеупомянутого api. Но ничего не стоит 
 * переписать/удалить пару моментов под другие приложения.
 *
 * @author Dereku
 */
public class Locale {

    private final HashMap<String, MessageFormat> messageCache = new HashMap<>();
    private final Properties locale = new Properties();
    private final JavaPlugin plugin;
    private File localeFile;

    public Locale(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Инициализация класса. Должно вызываться в первую очередь.
     * В противном случае вы будете получать Key "key" does not exists!
     */
    public void init() {
        this.locale.clear();
        String loc = this.plugin.getConfig().getString("locale", "en_US");
        this.localeFile = new File(this.plugin.getDataFolder(), loc + ".properties");
        
        if (this.saveLocale(loc)) {
            try (FileReader fr = new FileReader(this.localeFile)) {
                this.locale.load(fr);
            } catch (Exception ex) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to load " + loc + " locale!", ex);
            }
        } else {
            try {
                this.locale.load(this.plugin.getResource("en_US.properties"));
            } catch (IOException ex) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to load en_US locale!", ex);
            }
        }
    }

    /**
     * Получение сообщения из конфигурации
     * Пример сообщения: "There is so many players."
     * Пример вызова: getString("key");
     *
     * @param key ключ сообщения
     * @return сообщение, иначе null
     */
    public String getString(final String key) {
        return this.getString(key, false, "");
    }

    /**
     * Получение сообщения с аргументами из конфигурации
     * Пример сообщения: "There is {0} players: {1}."
     * Пример вызова: getString("key", "2", "You, Me");
     *
     * @param key ключ сообщения
     * @param args аргументы сообщения
     * @return сообщение, иначе null
     */
    public String getString(final String key, final String... args) {
        return this.getString(key, false, args);
    }

    /**
     * Получение сообщения из конфигурации с возможностью фильтрации цвета
     * Пример сообщения: "\u00a76There is so many players."
     * Пример вызова: getString("key", false);
     *
     * @param key ключ сообщения
     * @param removeColors если true, то цвета будут убраны
     * @return сообщение, иначе null
     */
    public String getString(final String key, final boolean removeColors) {
        return this.getString(key, removeColors, "");
    }

    /**
     * Получение сообщения с аргументами из конфигурации с возможностью фильтрации цвета
     * Пример сообщения: "\u00a76There is \u00a7c{0} \u00a76players:\u00a7c {1}."
     * Пример вызова: getString("key", false, "2", "You, Me");
     *
     * @param key ключ сообщения
     * @param removeColors если true, то цвета будут убраны
     * @param args аргументы сообщения
     * @return сообщение, иначе null
     */
    public String getString(final String key, final boolean removeColors, final String... args) {
        String out = this.locale.getProperty(key);
        if (out == null) {
            return ChatColor.RED + "Key \"" + key + "\" not found!";
        }

        MessageFormat mf = this.messageCache.get(out);
        if (mf == null) {
            mf = new MessageFormat(out);
            this.messageCache.put(out, mf);
        }
        
        out = mf.format(args);

        if (removeColors) {
            out = ChatColor.stripColor(out);
        }
        
        return out;
    }
    
    private boolean saveLocale(final String name) {
        if (this.localeFile.exists()) {
            return true;
        }
        
        InputStream is = this.plugin.getResource(name + ".properties");
        if (is == null) {
            this.plugin.getLogger().log(Level.WARNING, "Locale \"{0}\" does not exists!\"", name);
            return false;
        }
        
        try {
            FileUtils.copyInputStreamToFile(is, localeFile);
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to save \"" + name + ".properties \"", ex);
            return false;
        }
        return true;
    }
}