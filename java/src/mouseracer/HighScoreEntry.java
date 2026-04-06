package mouseracer;

public class HighScoreEntry {
    public final String name;
    public final String mode;
    public final int totalClicks;
    public final float seconds;
    public final float avgCps;
    public final float maxCps;
    public final String date;

    public HighScoreEntry(String name, String mode, int totalClicks, float seconds, float avgCps, float maxCps, String date) {
        this.name = name;
        this.mode = mode;
        this.totalClicks = totalClicks;
        this.seconds = seconds;
        this.avgCps = avgCps;
        this.maxCps = maxCps;
        this.date = date;
    }
}
