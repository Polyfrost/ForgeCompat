package net.minecraftforge.fml.common;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class EnhancedRuntimeException extends RuntimeException {
    public EnhancedRuntimeException() {
    }

    public EnhancedRuntimeException(String message) {
        super(message);
    }

    public EnhancedRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public EnhancedRuntimeException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length > 2 && stack[2].getClassName().startsWith("org.apache.logging.log4j.")) {
            StringWriter buf = new StringWriter();
            String msg = super.getMessage();
            if (msg != null) buf.append(msg);
            buf.append('\n');
            printStackTrace(new WrappedPrintStream() {
                @Override
                public void println(String line) {
                    buf.append(line).append('\n');
                }
            });
            return buf.toString();
        }
        return super.getMessage();
    }

    @Override
    public void printStackTrace(PrintWriter writer) {
        printStackTrace(new WrappedPrintStream() {
            @Override
            public void println(String line) {
                writer.println(line);
            }
        });
        super.printStackTrace(writer);
    }

    @Override
    public void printStackTrace(PrintStream stream) {
        printStackTrace(new WrappedPrintStream() {
            @Override
            public void println(String line) {
                stream.println(line);
            }
        });
        super.printStackTrace(stream);
    }

    protected abstract void printStackTrace(WrappedPrintStream stream);

    public abstract static class WrappedPrintStream {
        public abstract void println(String line);
    }
}
