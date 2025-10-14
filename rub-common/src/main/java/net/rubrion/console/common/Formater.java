package net.rubrion.console.common;

import java.util.logging.LogRecord;

public interface Formater {

    String format(LogRecord record, LogRecord before);

}
