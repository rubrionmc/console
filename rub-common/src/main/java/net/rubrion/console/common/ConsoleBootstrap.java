package net.rubrion.console.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ConsoleBootstrap {

    private static final PrintStream OUT = new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);

    private final Handler handler;
    private final Appender appender;
    private LogRecord last;

    public ConsoleBootstrap() {
        this(ConsoleBootstrap::printToOut, new LogFormatter());
    }

    private static void printToOut(String line) {
        OUT.println(line);
    }

    public ConsoleBootstrap(Consumer<String> sender, Formater formatter) {
        this.appender = new AbstractAppender("RubConsole", null, null, true, null) {
            @Override
            public void append(LogEvent event) {
                LogRecord record = new LogRecord(java.util.logging.Level.INFO, event.getMessage().getFormattedMessage());
                record.setLoggerName(event.getLoggerName());
                record.setInstant(Instant.ofEpochMilli(event.getInstant().getEpochMillisecond()));
                String formatted = formatter.format(record, last);
                sender.accept(formatted);
                // TODO: impl eventbus
                last = record;
            }
        };
        this.appender.start();

        this.handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                String formatted = formatter.format(record, last);
                sender.accept(formatted);
                // TODO: impl eventbus
                last = record;
            }

            @Override
            public void flush() {}

            @Override
            public void close() throws SecurityException {
                throw new SecurityException("You cannot close this handler.");
            }
        };
    }

    public void init() {
        java.util.logging.Logger julRoot = java.util.logging.Logger.getLogger("");
        for (Handler h : julRoot.getHandlers()) julRoot.removeHandler(h);
        julRoot.addHandler(handler);

        org.apache.logging.log4j.core.Logger l4jRoot = ((LoggerContext) LogManager.getContext(false)).getRootLogger();
        for (Appender a : l4jRoot.getAppenders().values()) l4jRoot.removeAppender(a);
        l4jRoot.addAppender(appender);
    }
}

