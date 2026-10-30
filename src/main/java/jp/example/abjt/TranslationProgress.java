package jp.example.abjt;

public final class TranslationProgress {
    public static volatile boolean running;
    public static volatile String currentMod = "待機中";
    public static volatile int completed;
    public static volatile int total;
    public static volatile int translated;
    public static volatile long startedAt;
    public static volatile String status = "待機中";
    public static int percent() { return total <= 0 ? 0 : Math.min(100, completed * 100 / total); }
    public static String remaining() {
        if (completed <= 0 || total <= completed) return completed >= total && total > 0 ? "0分" : "計算中";
        long elapsed = System.currentTimeMillis() - startedAt;
        long remaining = elapsed * (total - completed) / completed;
        long minutes = Math.max(0, remaining / 60000);
        return (minutes / 60) + "時間" + (minutes % 60) + "分";
    }
    private TranslationProgress() {}
}
