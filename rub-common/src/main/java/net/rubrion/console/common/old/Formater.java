package net.rubrion.console.common.old;

import java.util.logging.LogRecord;

public interface Formater {

    String format(LogRecord record, LogRecord before);

}
