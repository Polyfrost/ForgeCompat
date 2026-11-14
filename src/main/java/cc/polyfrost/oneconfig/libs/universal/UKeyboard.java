package cc.polyfrost.oneconfig.libs.universal;

import org.lwjgl.input.Keyboard;

public final class UKeyboard {
    public static final int KEY_NONE = Keyboard.KEY_NONE;
    public static final int KEY_ESCAPE = Keyboard.KEY_ESCAPE;
    public static final int KEY_A = Keyboard.KEY_A, KEY_B = Keyboard.KEY_B, KEY_C = Keyboard.KEY_C, KEY_D = Keyboard.KEY_D, KEY_E = Keyboard.KEY_E,
            KEY_F = Keyboard.KEY_F, KEY_G = Keyboard.KEY_G, KEY_H = Keyboard.KEY_H, KEY_I = Keyboard.KEY_I, KEY_J = Keyboard.KEY_J, KEY_K = Keyboard.KEY_K,
            KEY_L = Keyboard.KEY_L, KEY_M = Keyboard.KEY_M, KEY_N = Keyboard.KEY_N, KEY_O = Keyboard.KEY_O, KEY_P = Keyboard.KEY_P, KEY_Q = Keyboard.KEY_Q,
            KEY_R = Keyboard.KEY_R, KEY_S = Keyboard.KEY_S, KEY_T = Keyboard.KEY_T, KEY_U = Keyboard.KEY_U, KEY_V = Keyboard.KEY_V, KEY_W = Keyboard.KEY_W,
            KEY_X = Keyboard.KEY_X, KEY_Y = Keyboard.KEY_Y, KEY_Z = Keyboard.KEY_Z;
    public static final int KEY_LSHIFT = Keyboard.KEY_LSHIFT, KEY_RSHIFT = Keyboard.KEY_RSHIFT, KEY_LCONTROL = Keyboard.KEY_LCONTROL, KEY_RCONTROL = Keyboard.KEY_RCONTROL,
            KEY_LMENU = Keyboard.KEY_LMENU, KEY_RMENU = Keyboard.KEY_RMENU, KEY_ENTER = Keyboard.KEY_RETURN, KEY_TAB = Keyboard.KEY_TAB, KEY_SPACE = Keyboard.KEY_SPACE,
            KEY_BACKSPACE = Keyboard.KEY_BACK, KEY_DELETE = Keyboard.KEY_DELETE, KEY_UP = Keyboard.KEY_UP, KEY_DOWN = Keyboard.KEY_DOWN, KEY_LEFT = Keyboard.KEY_LEFT,
            KEY_RIGHT = Keyboard.KEY_RIGHT;

    private UKeyboard() {
    }

    public static boolean isKeyDown(int key) {
        return key > 0 && Keyboard.isKeyDown(key);
    }

    public static boolean isCtrlKeyDown() {
        return Keyboard.isKeyDown(KEY_LCONTROL) || Keyboard.isKeyDown(KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LMETA) || Keyboard.isKeyDown(Keyboard.KEY_RMETA);
    }

    public static boolean isShiftKeyDown() {
        return Keyboard.isKeyDown(KEY_LSHIFT) || Keyboard.isKeyDown(KEY_RSHIFT);
    }

    public static boolean isAltKeyDown() {
        return Keyboard.isKeyDown(KEY_LMENU) || Keyboard.isKeyDown(KEY_RMENU);
    }
}
