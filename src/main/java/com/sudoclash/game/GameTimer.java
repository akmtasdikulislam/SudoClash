package com.sudoclash.game;

public class GameTimer implements Runnable {
    private final int seconds;
    private final TimerListener listener;

    private Thread thread;
    private long deadline;
    private volatile boolean running;

    public GameTimer(int seconds, TimerListener listener) {
        this.seconds = seconds;
        this.listener = listener;
    }

    public synchronized void start() {
        if (thread != null) {
            return;
        }
        deadline = System.currentTimeMillis() + seconds * 1000L;
        running = true;
        thread = new Thread(this, "GameTimer");
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void subtractSeconds(int amount) {
        deadline -= amount * 1000L;
    }

    public synchronized int getRemainingSeconds() {
        if (thread == null) {
            return seconds;
        }
        long left = deadline - System.currentTimeMillis();
        if (left <= 0) {
            return 0;
        }
        // Round up, so 0.4 seconds left still shows 1.
        return (int) ((left + 999) / 1000);
    }

    public void stop() {
        running = false;
        Thread timerThread;
        synchronized (this) {
            timerThread = thread;
        }
        if (timerThread != null) {
            timerThread.interrupt();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public static String format(int totalSeconds) {
        int safe = Math.max(0, totalSeconds);
        return String.format("%02d:%02d", safe / 60, safe % 60);
    }

    @Override
    public void run() {
        int lastSecond = -1;
        while (running && !Thread.currentThread().isInterrupted()) {
            int remaining = getRemainingSeconds();
            if (remaining != lastSecond) {
                lastSecond = remaining;
                listener.onTick(remaining);
            }
            if (remaining <= 0) {
                if (running) {
                    running = false;
                    listener.onTimeUp();
                }
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
