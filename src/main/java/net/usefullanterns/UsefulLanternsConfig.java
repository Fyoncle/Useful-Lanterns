package net.usefullanterns;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class UsefulLanternsConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("usefullanterns.json");
    // ── Singleton ─────────────────────────────────────────────────────────────
    private static UsefulLanternsConfig instance;
    // ── Config fields ─────────────────────────────────────────────────────────
    // "Flip The Lantern" — false = left side (default), true = right side
    public boolean flipLantern = false;

    public static UsefulLanternsConfig get() {
        if (instance == null) instance = load();
        return instance;
    }

    // ── Load / Save ───────────────────────────────────────────────────────────
    private static UsefulLanternsConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                return GSON.fromJson(reader, UsefulLanternsConfig.class);
            } catch (IOException e) {
                System.err.println("[usefullanterns] Failed to load config, using defaults: " + e.getMessage());
            }
        }
        UsefulLanternsConfig defaults = new UsefulLanternsConfig();
        defaults.save();
        return defaults;
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            System.err.println("[usefullanterns] Failed to save config: " + e.getMessage());
        }
    }
}