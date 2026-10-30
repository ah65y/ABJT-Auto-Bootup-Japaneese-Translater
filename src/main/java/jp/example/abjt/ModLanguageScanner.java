package jp.example.abjt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ModLanguageScanner {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<String,String> CACHE = new ConcurrentHashMap<>();
    private static final Set<String> EXCLUDED = new HashSet<>();
    private static final Path CONFIG_DIR = Minecraft.getInstance().gameDirectory.toPath().resolve("config");
    private static final Path CACHE_FILE = CONFIG_DIR.resolve("abjt_translation_cache.json");
    private static final Path EXCLUDED_FILE = CONFIG_DIR.resolve("abjt_excluded_mods.json");
    private static final Path PACK_DIR = Minecraft.getInstance().gameDirectory.toPath().resolve("resourcepacks/ABJT-Japanese");

    public static void generate() {
        if (TranslationProgress.running) return;
        TranslationProgress.running = true;
        TranslationProgress.startedAt = System.currentTimeMillis();
        TranslationProgress.completed = 0; TranslationProgress.total = 0; TranslationProgress.translated = 0;
        TranslationProgress.status = "MODを検索中...";
        Thread worker = new Thread(ModLanguageScanner::run, "ABJT-Translation-Worker");
        worker.setDaemon(true); worker.start();
    }

    private static void run() {
        try {
            Files.createDirectories(CONFIG_DIR);
            loadJsonMap(CACHE_FILE, CACHE);
            loadExcluded();
            Path mods = Minecraft.getInstance().gameDirectory.toPath().resolve("mods");
            Files.createDirectories(mods);
            List<JarLang> jobs = new ArrayList<>();
            try (var stream = Files.list(mods)) {
                for (Path jar : stream.filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".jar")).toList()) {
                    try (ZipFile zip = new ZipFile(jar.toFile())) {
                        Enumeration<? extends ZipEntry> entries = zip.entries();
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (!name.matches("assets/[^/]+/lang/en_us\\.json")) continue;
                            String modId = name.split("/")[1];
                            String ja = "assets/" + modId + "/lang/ja_jp.json";
                            if (zip.getEntry(ja) != null) {
                                EXCLUDED.add(modId + "|" + jar.getFileName() + "|" + Files.size(jar) + "|" + Files.getLastModifiedTime(jar).toMillis());
                                continue;
                            }
                            String identity = jar.getFileName() + "|" + Files.size(jar) + "|" + Files.getLastModifiedTime(jar).toMillis();
                            if (!EXCLUDED.contains(modId + "|" + identity)) jobs.add(new JarLang(jar, modId, name, identity));
                        }
                    } catch (Exception e) { Abjt.LOGGER.warn("JAR scan failed: {}", jar, e); }
                }
            }
            List<EntryTask> tasks = new ArrayList<>();
            for (JarLang job : jobs) {
                try (ZipFile zip = new ZipFile(job.jar.toFile());
                     InputStreamReader reader = new InputStreamReader(zip.getInputStream(zip.getEntry(job.entry)), StandardCharsets.UTF_8)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    for (Map.Entry<String,JsonElement> e : json.entrySet())
                        if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString())
                            tasks.add(new EntryTask(job, e.getKey(), e.getValue().getAsString()));
                } catch (Exception e) { Abjt.LOGGER.warn("Language JSON parse failed: {}", job.entry, e); }
            }
            TranslationProgress.total = tasks.size();
            deleteTree(PACK_DIR.resolve("assets"));
            Files.createDirectories(PACK_DIR.resolve("assets"));
            Files.writeString(PACK_DIR.resolve("pack.mcmeta"), "{\"pack\":{\"pack_format\":15,\"description\":\"ABJT Japanese translations\"}}", StandardCharsets.UTF_8);
            Map<String,JsonObject> outputs = new LinkedHashMap<>();
            for (EntryTask task : tasks) {
                TranslationProgress.currentMod = task.jar.modId;
                TranslationProgress.status = "翻訳中: " + task.key;
                String cacheKey = task.jar.identity + "|" + task.jar.modId + "|" + task.key + "|" + task.value;
                String translated = CACHE.get(cacheKey);
                if (translated == null) {
                    if (task.value.matches(".*[A-Za-z].*") && !task.value.matches("^[a-z0-9_.-]+:[a-z0-9_./-]+$"))
                        translated = TranslationEngine.translate(task.value);
                    else translated = task.value;
                    CACHE.put(cacheKey, translated);
                }
                outputs.computeIfAbsent(task.jar.modId, k -> new JsonObject()).addProperty(task.key, translated);
                TranslationProgress.completed++;
                if (!translated.equals(task.value)) TranslationProgress.translated++;
                if (TranslationProgress.completed % 20 == 0) saveCache();
            }
            for (Map.Entry<String,JsonObject> out : outputs.entrySet()) {
                Path lang = PACK_DIR.resolve("assets").resolve(out.getKey()).resolve("lang");
                Files.createDirectories(lang);
                try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(lang.resolve("ja_jp.json")), StandardCharsets.UTF_8)) {
                    GSON.toJson(out.getValue(), writer);
                }
            }
            saveCache(); saveExcluded();
            TranslationProgress.status = "リソースパックを適用中...";
            Minecraft.getInstance().execute(() -> {
                try {
                    Minecraft.getInstance().options.resourcePacks.add("file/ABJT-Japanese");
                    Minecraft.getInstance().reloadResourcePacks().whenComplete((v, error) -> {
                        TranslationProgress.status = error == null ? "翻訳完了" : "適用に失敗しました: " + error.getMessage();
                        TranslationProgress.running = false;
                    });
                } catch (Exception e) {
                    TranslationProgress.status = "適用に失敗しました: " + e.getMessage();
                    TranslationProgress.running = false;
                }
            });
        } catch (Exception e) {
            Abjt.LOGGER.error("ABJT translation generation failed", e);
            TranslationProgress.status = "エラー: " + e.getMessage();
            TranslationProgress.running = false;
        }
    }

    private static void deleteTree(Path root) throws Exception {
        if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        }
    }
    private static void loadJsonMap(Path file, Map<String,String> map) {
        try { if (Files.exists(file)) JsonParser.parseString(Files.readString(file)).getAsJsonObject().entrySet().forEach(e -> map.put(e.getKey(), e.getValue().getAsString())); }
        catch (Exception e) { Abjt.LOGGER.warn("Could not load ABJT cache", e); }
    }
    private static void saveCache() {
        try { Files.createDirectories(CONFIG_DIR); Files.writeString(CACHE_FILE, GSON.toJson(CACHE), StandardCharsets.UTF_8); }
        catch (Exception e) { Abjt.LOGGER.warn("Could not save ABJT cache", e); }
    }
    private static void loadExcluded() {
        try { if (Files.exists(EXCLUDED_FILE)) JsonParser.parseString(Files.readString(EXCLUDED_FILE)).getAsJsonArray().forEach(e -> EXCLUDED.add(e.getAsString())); }
        catch (Exception e) { Abjt.LOGGER.warn("Could not load excluded MOD cache", e); }
    }
    private static void saveExcluded() {
        try { Files.writeString(EXCLUDED_FILE, GSON.toJson(EXCLUDED), StandardCharsets.UTF_8); }
        catch (Exception e) { Abjt.LOGGER.warn("Could not save excluded MOD cache", e); }
    }
    private record JarLang(Path jar, String modId, String entry, String identity) {}
    private record EntryTask(JarLang jar, String key, String value) {}
    private ModLanguageScanner() {}
}
