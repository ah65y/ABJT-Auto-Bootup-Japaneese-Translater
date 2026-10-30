package jp.example.abjt;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AbjtClient {
    private static KeyMapping regenerateKey;
    private static boolean checkedFirstLaunch;
    private static final AbjtClient INSTANCE = new AbjtClient();

    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(INSTANCE::registerKeys);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        MinecraftForge.EVENT_BUS.addListener(INSTANCE::onScreenInit);
    }

    private void registerKeys(RegisterKeyMappingsEvent event) {
        regenerateKey = new KeyMapping("key.abjt.regenerate", InputConstants.Type.KEYSYM,
                InputConstants.KEY_F8, "key.categories.abjt");
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
                catch (Exception e) { Abjt.LOGGER.warn("Could not write first-run marker", e); }
                startGeneration(event.getScreen());
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || regenerateKey == null) return;
        while (regenerateKey.consumeClick()) startGeneration(Minecraft.getInstance().screen);
    }

    private void startGeneration(net.minecraft.client.gui.screens.Screen parent) {
        if (TranslationProgress.running) return;
        ModLanguageScanner.generate();
        if (AbjtConfig.SHOW_PROGRESS.get()) Minecraft.getInstance().setScreen(new AbjtProgressScreen(parent));
    }
    private AbjtClient() {}
}
