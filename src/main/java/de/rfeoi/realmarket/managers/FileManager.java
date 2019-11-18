package de.rfeoi.realmarket.managers;

import com.google.gson.Gson;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileManager implements IManger {

    private Configuration configuration;
    private Gson gson;
    private final File DATA_FOLDER;

    public FileManager(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        this.configuration = plugin.getConfig();
        gson = new Gson();
        DATA_FOLDER = new File("plugins/" + plugin.getName() + "/");
        if (!DATA_FOLDER.exists()) {
            DATA_FOLDER.mkdirs();
        }
    }


    ConfigurationSection getConfig(IManger manager) {
        return configuration.getConfigurationSection(manager.getManagerName().toLowerCase());
    }


    private String getFileFor(IManger manager, String subType) {
        return DATA_FOLDER.getAbsolutePath() + manager.getManagerName().toUpperCase() + "_" + subType + ".data";
    }

    Object getSavedData(IManger manager, String subType, Class type) {
        try {
            String pathToFile = getFileFor(manager, subType);
            if (!new File(pathToFile).exists()) return null;
            String content = new String(Files.readAllBytes(Paths.get(pathToFile)));
            return gson.fromJson(content, type);
        } catch (IOException e) {
            return null;
        }
    }

    boolean saveSavedData(IManger manager, String subType, Class type, Object content) {
        try {
            String pathToFile = getFileFor(manager, subType);
            String jsonContent = gson.toJson(content, type);
            Files.write(Paths.get(pathToFile), jsonContent.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    @Override
    public String getManagerName() {
        return "File";
    }
}
