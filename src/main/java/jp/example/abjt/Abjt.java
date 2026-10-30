package jp.example.abjt;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Abjt.MODID)
public final class Abjt {
    public static final String MODID = "abjt";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static ModConfig clientConfig;

    public Abjt() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AbjtConfig.SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigLoading);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AbjtClient::init);
    }

    private void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == AbjtConfig.SPEC) clientConfig = event.getConfig();
    }

    public static void saveConfig() {
        if (clientConfig != null) clientConfig.save();
    }
}
