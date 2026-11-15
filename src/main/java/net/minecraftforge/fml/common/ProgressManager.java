package net.minecraftforge.fml.common;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProgressManager {
    private static final List<ProgressBar> bars = new CopyOnWriteArrayList<>();

    public static ProgressBar push(String title, int steps) {
        return push(title, steps, false);
    }

    public static ProgressBar push(String title, int steps, boolean timeEachStep) {
        ProgressBar bar = new ProgressBar(title, steps);
        bars.add(bar);
        return bar;
    }

    public static void pop(ProgressBar bar) {
        if (bar.getSteps() != bar.getStep()) throw new IllegalStateException("can't pop unfinished ProgressBar " + bar.getTitle());
        bars.remove(bar);
    }

    public static Iterator<ProgressBar> barIterator() {
        return bars.iterator();
    }

    public static class ProgressBar {
        private final String title;
        private final int steps;
        private volatile int step;
        private volatile String message = "";

        ProgressBar(String title, int steps) {
            this.title = title;
            this.steps = steps;
        }

        public void step(Class<?> classToName, String... extra) {
            step(classToName.getSimpleName() + String.join("", extra));
        }

        public void step(String message) {
            if (step >= steps) throw new IllegalStateException("too much steps for ProgressBar " + title);
            step++;
            this.message = message;
        }

        public String getTitle() {
            return title;
        }

        public int getSteps() {
            return steps;
        }

        public int getStep() {
            return step;
        }

        public String getMessage() {
            return message;
        }

        public void timeEachStep() {
        }
    }
}
