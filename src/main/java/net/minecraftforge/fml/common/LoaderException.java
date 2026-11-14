package net.minecraftforge.fml.common;

public class LoaderException extends RuntimeException {
    public LoaderException(Throwable cause) {
        super(cause);
    }

    public LoaderException(String message) {
        super(message);
    }

    public LoaderException() {
    }
}
