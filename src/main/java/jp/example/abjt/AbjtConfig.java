package jp.example.abjt;

import net.minecraftforge.common.ForgeConfigSpec;

public final class AbjtConfig {
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.ConfigValue<String> PROVIDER;
    public static final ForgeConfigSpec.ConfigValue<String> OLLAMA_URL;
    public static final ForgeConfigSpec.ConfigValue<String> OLLAMA_MODEL;
    public static final ForgeConfigSpec.BooleanValue GOOGLE_FALLBACK;
    public static final ForgeConfigSpec.BooleanValue AUTO_FIRST_RUN;
    public static final ForgeConfigSpec.BooleanValue SHOW_PROGRESS;
    public static final ForgeConfigSpec SPEC;
    static {
        B.push("translation");
        PROVIDER = B.comment("Default provider: translategemma or google").define("provider", "translategemma",
                s -> s instanceof String && (s.equals("translategemma") || s.equals("google")));
        OLLAMA_URL = B.define("ollamaUrl", "http://127.0.0.1:11434");
        OLLAMA_MODEL = B.define("ollamaModel", "translategemma:12b");
        GOOGLE_FALLBACK = B.define("googleFallback", true);
        AUTO_FIRST_RUN = B.define("autoGenerateOnFirstLaunch", true);
        SHOW_PROGRESS = B.define("showProgressWindow", true);
        B.pop();
        SPEC = B.build();
    }
    private AbjtConfig() {}
}
