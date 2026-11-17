package net.minecraftforge.fml.common;

import java.util.concurrent.Callable;

public interface ICrashCallable extends Callable<String> {
    String getLabel();
}
