package jp.example.abjt;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(Abjt.MODID)
public final class Abjt {
    public static final String MODID = "abjt";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static KeyMapping regenerateKey;
    private static boolean checkedFirstLaunch;
    private static ModConfig clientConfig;

    public Abjt() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AbjtConfig.SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeys);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigLoading);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::onScreenInit);
    }
    private void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == AbjtConfig.SPEC) clientConfig = event.getConfig();
    }
    public static void saveConfig() {
        if (clientConfig != null) clientConfig.save();
    }
    private void registerKeys(RegisterKeyMappingsEvent event) {
        regenerateKey = new KeyMapping("key.abjt.regenerate", InputConstants.Type.KEYSYM, InputConstants.KEY_F8, "key.categories.abjt");
        event.register(regenerateKey);
    }
    private void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen)) return;
        int x = event.getScreen().width - 110;
        event.addListener(Button.builder(net.minecraft.network.chat.Component.literal("ABJT 設定"), b ->
                Minecraft.getInstance().setScreen(new AbjtSettingsScreen(event.getScreen())))
                .bounds(x, 8, 100, 20).build());
        event.addListener(Button.builder(net.minecraft.network.chat.Component.literal("翻訳を生成"), b ->
                startGeneration(event.getScreen())).bounds(x, 32, 100, 20).build());
        if (!checkedFirstLaunch) {
            checkedFirstLaunch = true;
            Path marker = Minecraft.getInstance().gameDirectory.toPath().resolve("config/abjt_first_run_done");
            if (AbjtConfig.AUTO_FIRST_RUN.get() && !Files.exists(marker)) {
                try { Files.createDirectories(marker.getParent()); Files.writeString(marker, "done"); }
                catch (Exception e) { LOGGER.warn("Could not write first-run marker", e); }
                startGeneration(event.getScreen());
            }
        }
    }
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || regenerateKey == null) return;
        while (regenerateKey.consumeClick()) {
            startGeneration(Minecraft.getInstance().screen);
        }
    }
    private void startGeneration(net.minecraft.client.gui.screens.Screen parent) {
        if (TranslationProgress.running) return;
        ModLanguageScanner.generate();
        if (AbjtConfig.SHOW_PROGRESS.get()) Minecraft.getInstance().setScreen(new AbjtProgressScreen(parent));
    }
}
