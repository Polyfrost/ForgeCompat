package cc.polyfrost.oneconfig.config.core;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

public class OneKeyBind {
    protected final ArrayList<Integer> keyBinds = new ArrayList<>();
    private transient Runnable runnable;

    public OneKeyBind(int... keys) {
        for (int key : keys) keyBinds.add(key);
    }

    public boolean isActive() {
        if (keyBinds.isEmpty()) return false;
        for (int key : keyBinds) if (key <= 0 || !Keyboard.isKeyDown(key)) return false;
        return true;
    }

    public void run() {
        if (runnable != null) runnable.run();
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public ArrayList<Integer> getKeyBinds() {
        return keyBinds;
    }

    public int getSize() {
        return keyBinds.size();
    }

    public void clearKeys() {
        keyBinds.clear();
    }

    public void addKey(int key) {
        keyBinds.add(key);
    }

    public String getDisplay() {
        StringBuilder out = new StringBuilder();
        for (int key : keyBinds) {
            if (out.length() > 0) out.append(" + ");
            out.append(Keyboard.getKeyName(key));
        }
        return out.toString();
    }
}
