package jp.example.abjt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class TranslationEngine {
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public static String translate(String source) throws Exception {
        if (source == null || source.isBlank() || source.matches(".*[ぁ-んァ-ヶ一-龯].*")) return source;
        if ("google".equalsIgnoreCase(AbjtConfig.PROVIDER.get())) {
            try { return google(source); }
            catch (Exception e) { if (!AbjtConfig.GOOGLE_FALLBACK.get()) throw e; return gemma(source); }
        }
        try { return gemma(source); }
        catch (Exception e) {
            if (!AbjtConfig.GOOGLE_FALLBACK.get()) throw e;
            return google(source);
        }
    }

    private static String gemma(String source) throws Exception {
        String base = AbjtConfig.OLLAMA_URL.get().trim().replaceAll("/+$", "");
        URI baseUri = URI.create(base);
        String host = baseUri.getHost();
        if (!"http".equalsIgnoreCase(baseUri.getScheme()) || host == null ||
                !(host.equals("127.0.0.1") || host.equalsIgnoreCase("localhost") || host.equals("::1")))
            throw new IllegalArgumentException("プライバシー保護のためOllamaはlocalhost接続のみ許可されています");
        JsonObject body = new JsonObject();
        body.addProperty("model", AbjtConfig.OLLAMA_MODEL.get().trim());
        body.addProperty("stream", false);
        body.addProperty("prompt", "Translate the following Minecraft mod language string from English to natural Japanese. Output only the translation. Preserve formatting codes, placeholders (%s, %1$d, {name}), numbers, and technical identifiers exactly.\n\n" + source);
        JsonObject options = new JsonObject();
        options.addProperty("temperature", 0);
        body.add("options", options);
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/api/generate"))
                .timeout(Duration.ofMinutes(10)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8)).build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) throw new IllegalStateException("Ollama HTTP " + response.statusCode() + ": " + response.body());
        String result = JsonParser.parseString(response.body()).getAsJsonObject().get("response").getAsString().trim();
        if (result.isEmpty()) throw new IllegalStateException("TranslateGemma returned an empty result");
        return result;
    }

    private static String google(String source) throws Exception {
        String q = URLEncoder.encode(source, StandardCharsets.UTF_8);
        URI uri = URI.create("https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=ja&dt=t&q=" + q);
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(30))
                .header("User-Agent", "ABJT/0.1").GET().build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) throw new IllegalStateException("Google Translate HTTP " + response.statusCode());
        JsonArray chunks = JsonParser.parseString(response.body()).getAsJsonArray().get(0).getAsJsonArray();
        StringBuilder out = new StringBuilder();
        chunks.forEach(c -> { if (c.isJsonArray() && !c.getAsJsonArray().get(0).isJsonNull()) out.append(c.getAsJsonArray().get(0).getAsString()); });
        return out.toString();
    }
    private TranslationEngine() {}
}
