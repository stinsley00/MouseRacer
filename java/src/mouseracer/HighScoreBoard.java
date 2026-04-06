package mouseracer;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HighScoreBoard {

    private final List<HighScoreEntry> entries = new ArrayList<>();
    private final Path filePath;

    public HighScoreBoard() {
        this(resolveScoresPath());
    }

    public HighScoreBoard(Path filePath) {
        this.filePath = filePath;
        load();
    }

    public void addEntry(HighScoreEntry entry) {
        entries.add(entry);
        entries.sort((a, b) -> Integer.compare(b.totalClicks, a.totalClicks));
    }

    public List<HighScoreEntry> topN(String mode, int n) {
        return entries.stream()
            .filter(e -> e.mode.equals(mode))
            .limit(n)
            .collect(Collectors.toList());
    }

    public static HighScoreEntry newEntry(String name, GameEngine.GameMode mode, int totalClicks, float seconds, float avgCps, float maxCps) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        return new HighScoreEntry(name, mode.name(), totalClicks, seconds, avgCps, maxCps, date);
    }

    // --- Simple JSON persistence (no external libraries) ---

    public void save() {
        try {
            Files.createDirectories(filePath.getParent());
            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < entries.size(); i++) {
                HighScoreEntry e = entries.get(i);
                sb.append("  {\n");
                sb.append("    \"name\": ").append(jsonString(e.name)).append(",\n");
                sb.append("    \"mode\": ").append(jsonString(e.mode)).append(",\n");
                sb.append("    \"total_clicks\": ").append(e.totalClicks).append(",\n");
                sb.append("    \"seconds\": ").append(e.seconds).append(",\n");
                sb.append("    \"avg_cps\": ").append(e.avgCps).append(",\n");
                sb.append("    \"max_cps\": ").append(e.maxCps).append(",\n");
                sb.append("    \"date\": ").append(jsonString(e.date)).append("\n");
                sb.append("  }");
                if (i < entries.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("]\n");
            Files.writeString(filePath, sb.toString());
        } catch (IOException ex) {
            System.err.println("Failed to save high scores: " + ex.getMessage());
        }
    }

    private void load() {
        if (!Files.exists(filePath)) return;
        try {
            String content = Files.readString(filePath);
            parseJsonArray(content);
        } catch (Exception ex) {
            System.err.println("Failed to load high scores: " + ex.getMessage());
        }
    }

    private void parseJsonArray(String json) {
        json = json.trim();
        if (!json.startsWith("[") || !json.endsWith("]")) return;
        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return;

        int depth = 0;
        int start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    parseJsonObject(json.substring(start, i + 1));
                    start = -1;
                }
            }
        }
    }

    private void parseJsonObject(String obj) {
        Map<String, String> map = new HashMap<>();
        obj = obj.trim();
        obj = obj.substring(1, obj.length() - 1); // strip { }

        String[] pairs = obj.split(",(?=\\s*\")");
        for (String pair : pairs) {
            int colon = pair.indexOf(':');
            if (colon < 0) continue;
            String key = pair.substring(0, colon).trim().replace("\"", "");
            String val = pair.substring(colon + 1).trim();
            if (val.startsWith("\"") && val.endsWith("\"")) {
                val = val.substring(1, val.length() - 1);
            }
            map.put(key, val);
        }

        try {
            entries.add(new HighScoreEntry(
                map.getOrDefault("name", "Anonymous"),
                map.getOrDefault("mode", "SPRINT"),
                Integer.parseInt(map.getOrDefault("total_clicks", "0")),
                Float.parseFloat(map.getOrDefault("seconds", "0")),
                Float.parseFloat(map.getOrDefault("avg_cps", "0")),
                Float.parseFloat(map.getOrDefault("max_cps", "0")),
                map.getOrDefault("date", "")
            ));
        } catch (NumberFormatException ignored) {}
    }

    private static String jsonString(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static Path resolveScoresPath() {
        String home = System.getProperty("user.home");
        String os = System.getProperty("os.name", "").toLowerCase();
        Path dir;
        if (os.contains("linux")) {
            String xdg = System.getenv("XDG_DATA_HOME");
            dir = (xdg != null && !xdg.isEmpty()) ? Path.of(xdg) : Path.of(home, ".local", "share");
        } else if (os.contains("mac")) {
            dir = Path.of(home, "Library", "Application Support");
        } else {
            String appData = System.getenv("APPDATA");
            dir = (appData != null) ? Path.of(appData) : Path.of(home);
        }
        return dir.resolve("mouse-racer").resolve("high_scores.json");
    }
}
