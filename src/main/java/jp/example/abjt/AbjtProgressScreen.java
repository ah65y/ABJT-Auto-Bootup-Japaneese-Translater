package jp.example.abjt;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AbjtProgressScreen extends Screen {
    private final Screen parent;
    public AbjtProgressScreen(Screen parent) { super(Component.literal("ABJT 翻訳進捗")); this.parent = parent; }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int cx = width / 2;
        g.drawCenteredString(font, "ABJT - 日本語翻訳の進捗", cx, height / 2 - 65, 0xFFFFFF);
        g.drawCenteredString(font, TranslationProgress.status, cx, height / 2 - 42, 0xE0E0E0);
        g.drawCenteredString(font, "処理中のMOD: " + TranslationProgress.currentMod, cx, height / 2 - 22, 0xFFFFFF);
        g.fill(cx - 130, height / 2 + 2, cx + 130, height / 2 + 14, 0xFF333333);
        g.fill(cx - 130, height / 2 + 2, cx - 130 + 260 * TranslationProgress.percent() / 100, height / 2 + 14, 0xFF42A5F5);
        g.drawCenteredString(font, TranslationProgress.percent() + "%  (" + TranslationProgress.completed + "/" + TranslationProgress.total + ")", cx, height / 2 + 21, 0xFFFFFF);
        long elapsed = Math.max(1, System.currentTimeMillis() - TranslationProgress.startedAt);
        double speed = TranslationProgress.completed * 1000.0 / elapsed;
        g.drawCenteredString(font, "翻訳済み: " + TranslationProgress.translated + " 件   速度: " + String.format(java.util.Locale.ROOT, "%.2f", speed) + " 件/秒", cx, height / 2 + 40, 0xDDDDDD);
        g.drawCenteredString(font, "残り時間: " + TranslationProgress.remaining(), cx, height / 2 + 58, 0xDDDDDD);
        if (!TranslationProgress.running) g.drawCenteredString(font, "完了しました。ESCで閉じることができます。", cx, height / 2 + 80, 0x80FF80);
        super.render(g, mouseX, mouseY, partialTick);
    }
    @Override public boolean shouldCloseOnEsc() { return true; }
    @Override public void onClose() { if (minecraft != null) minecraft.setScreen(parent); }
}
