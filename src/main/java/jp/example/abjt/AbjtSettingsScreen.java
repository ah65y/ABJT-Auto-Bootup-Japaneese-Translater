package jp.example.abjt;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AbjtSettingsScreen extends Screen {
    private final Screen parent;
    private EditBox url, model;
    private boolean fallback, auto, progress;
    private String provider;
    public AbjtSettingsScreen(Screen parent) {
        super(Component.literal("ABJT 設定")); this.parent = parent;
        provider = AbjtConfig.PROVIDER.get(); fallback = AbjtConfig.GOOGLE_FALLBACK.get();
        auto = AbjtConfig.AUTO_FIRST_RUN.get(); progress = AbjtConfig.SHOW_PROGRESS.get();
    }
    @Override protected void init() {
        int c = width / 2;
        url = field(c - 150, 70, 300, AbjtConfig.OLLAMA_URL.get());
        model = field(c - 150, 118, 300, AbjtConfig.OLLAMA_MODEL.get());
        addRenderableWidget(Button.builder(providerText(), b -> { provider = provider.equals("translategemma") ? "google" : "translategemma"; b.setMessage(providerText()); }).bounds(c - 150, 35, 300, 20).build());
        addRenderableWidget(Button.builder(toggle("Google翻訳へのフォールバック", fallback), b -> { fallback = !fallback; b.setMessage(toggle("Google翻訳へのフォールバック", fallback)); }).bounds(c - 150, 160, 300, 20).build());
        addRenderableWidget(Button.builder(toggle("初回起動時に自動生成", auto), b -> { auto = !auto; b.setMessage(toggle("初回起動時に自動生成", auto)); }).bounds(c - 150, 185, 300, 20).build());
        addRenderableWidget(Button.builder(toggle("進捗ウィンドウを表示", progress), b -> { progress = !progress; b.setMessage(toggle("進捗ウィンドウを表示", progress)); }).bounds(c - 150, 210, 300, 20).build());
        addRenderableWidget(Button.builder(Component.literal("保存"), b -> save()).bounds(c - 104, height - 30, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("キャンセル"), b -> minecraft.setScreen(parent)).bounds(c + 4, height - 30, 100, 20).build());
    }
    private EditBox field(int x,int y,int w,String value) { EditBox b=new EditBox(font,x,y,w,20,Component.empty()); b.setMaxLength(2048); b.setValue(value); addRenderableWidget(b); return b; }
    private Component providerText() { return Component.literal("翻訳エンジン: " + (provider.equals("translategemma") ? "TranslateGemma 12B" : "Google翻訳")); }
    private Component toggle(String name, boolean value) { return Component.literal(name + ": " + (value ? "有効" : "無効")); }
    private void save() {
        AbjtConfig.PROVIDER.set(provider); AbjtConfig.OLLAMA_URL.set(url.getValue().trim());
        AbjtConfig.OLLAMA_MODEL.set(model.getValue().trim()); AbjtConfig.GOOGLE_FALLBACK.set(fallback);
        AbjtConfig.AUTO_FIRST_RUN.set(auto); AbjtConfig.SHOW_PROGRESS.set(progress);
        Abjt.saveConfig();
        if (minecraft != null) { minecraft.options.save(); minecraft.setScreen(parent); }
    }
    @Override public void render(GuiGraphics g,int mx,int my,float pt) {
        renderBackground(g); int c=width/2;
        g.drawCenteredString(font,title,c,12,0xFFFFFF);
        g.drawString(font,"Ollama URL (localhost)",c-150,57,0xDDDDDD,false);
        g.drawString(font,"モデル名 (初期値: translategemma:12b)",c-150,105,0xDDDDDD,false);
        g.drawCenteredString(font,"TranslateGemmaは応答が完了するまで待機してから次の文章へ進みます。",c,245,0xDDDDDD);
        super.render(g,mx,my,pt);
    }
}
