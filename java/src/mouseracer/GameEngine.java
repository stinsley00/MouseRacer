package mouseracer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GameEngine {
    public enum GameMode {
        SPRINT, ENDURANCE, MARATHON
    }

    public interface GameListener {
        void onTick(float seconds, int totalClicks, float currentCps);
        void onClick(int totalClicks);
        void onGameOver(String taunt, int totalClicks, float seconds, float avgCps, float maxCps);
    }

    private GameMode mode;
    private final AtomicInteger clicks = new AtomicInteger(0);
    private final AtomicInteger totalClicks = new AtomicInteger(0);
    private float seconds;
    private float startSeconds;
    private float maxCps;
    private float firstClick;
    private float adjFirstClick;
    private volatile boolean isRunning;
    private ScheduledExecutorService scheduler;
    private final GameListener listener;

    public GameEngine(GameListener listener) {
        this.listener = listener;
    }

    public void start(GameMode mode) {
        if (isRunning) stop();
        
        this.mode = mode;
        this.clicks.set(0);
        this.totalClicks.set(0);
        this.maxCps = 0;
        this.firstClick = 0;
        this.isRunning = true;
        switch (mode) {
            case SPRINT:
                this.seconds = 10;
                break;
            case ENDURANCE:
            case MARATHON:
                this.seconds = 0;
                break;
        }
        this.startSeconds = this.seconds;

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            switch (this.mode) {
                case SPRINT:
                    handleSprintTick();
                    break;
                case ENDURANCE:
                    handleEnduranceTick();
                    break;
                case MARATHON:
                    handleMarathonTick();
                    break;
            }
        }, 1, 1, TimeUnit.SECONDS);

        listener.onTick(seconds, totalClicks.get(), 0);
    }

    public void registerClick() {
        if (isRunning) {
            int currentTotal = totalClicks.incrementAndGet();
            clicks.incrementAndGet();
            listener.onClick(currentTotal);
        }
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    private synchronized void handleSprintTick() {
        seconds--;
        
        int currentClicks = clicks.getAndSet(0);
        float currentCps = currentClicks;
        if (currentCps > maxCps) maxCps = currentCps;
        listener.onTick(seconds, totalClicks.get(), currentCps);

        if (seconds <= 0) {
            gameOver();
        }
    }

    private synchronized void handleEnduranceTick() {
        seconds++;
        int currentClicks = clicks.getAndSet(0);
        float currentCps = currentClicks;
        if (currentCps > maxCps) maxCps = currentCps;
        listener.onTick(seconds, totalClicks.get(), currentCps);

        if (seconds >= 100) {
            gameOver();
        }
    }

    private synchronized void handleMarathonTick() {
        seconds++;
        int currentClicks = clicks.getAndSet(0);
        float currentCps = currentClicks;
        
        if (firstClick == 0 && currentCps > 0) {
            firstClick = currentCps;
            adjFirstClick = firstClick * 0.75f;
        }

        if (currentCps > firstClick * 1.05f) {
            firstClick = currentCps;
            adjFirstClick = firstClick * 0.75f;
        }

        if (firstClick > 0 && currentCps <= adjFirstClick) {
            gameOver();
            return;
        }

        if (currentCps > maxCps) maxCps = currentCps;
        listener.onTick(seconds, totalClicks.get(), currentCps);
    }

    private void gameOver() {
        stop();
        float elapsed = (mode == GameMode.SPRINT) ? startSeconds - seconds : seconds;
        float avgCps = elapsed > 0 ? (float) totalClicks.get() / elapsed : 0;
        listener.onGameOver(getTaunt(), totalClicks.get(), elapsed, avgCps, maxCps);
    }

    private String getTaunt() {
        int total = totalClicks.get();
        if (total > 350) return "You Might Be Playing too much MouseRacer!";
        if (total > 251) return "All time high score! NOT!";
        if (total > 201) return "Unstoppable!";
        if (total > 151) return "That call stack has your name written all over it!";
        if (total > 121) return "You Have Taken The Lead";
        if (total > 101) return "you have achieved hyperclick!";
        if (total > 50) return "You make Chuck Norris Proud!";
        return "you might consider a different hobby";
    }
}
