package gg.essential.api.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class Multithreading {
    public static final Multithreading INSTANCE = new Multithreading();
    private static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(4, task -> {
        Thread thread = new Thread(task, "Essential API Thread");
        thread.setDaemon(true);
        return thread;
    });

    public static void runAsync(Runnable task) {
        POOL.execute(task);
    }

    public static ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return POOL.schedule(task, delay, unit);
    }

    public ScheduledFuture<?> schedule(Runnable task, long delay, long period, TimeUnit unit) {
        return POOL.scheduleAtFixedRate(task, delay, period, unit);
    }

    public Future<?> submit(Runnable task) {
        return POOL.submit(task);
    }
}
