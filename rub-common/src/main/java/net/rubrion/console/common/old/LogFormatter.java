package net.rubrion.console.common.old;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Formatter for log records with color support and line wrapping.
 * Provides compact formatting for consecutive similar log records.
 */
public class LogFormatter implements Formater {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int MAX_LINE_WIDTH = 115;
    private static final int CONSECUTIVE_THRESHOLD_MS = 1000;

    private enum LogLevelConfig {
        SEVERE(Level.SEVERE, "x", LogColors.DARK_RED),
        WARNING(Level.WARNING, "!", LogColors.YELLOW),
        DEFAULT(Level.INFO, "*", LogColors.GRAY);

        final Level level;
        final String icon;
        final String color;

        LogLevelConfig(Level level, String icon, String color) {
            this.level = level;
            this.icon = icon;
            this.color = color;
        }

        static LogLevelConfig fromLevel(Level level) {
            for (LogLevelConfig config : values()) {
                if (config.level == level) {
                    return config;
                }
            }
            return DEFAULT;
        }
    }

    @Override
    public String format(@NotNull LogRecord record, LogRecord previousRecord) {
        LogLevelConfig levelConfig = LogLevelConfig.fromLevel(record.getLevel());
        String loggerName = extractLoggerName(record);
        String timestamp = LocalTime.now().format(TIME_FORMATTER);

        if (shouldUseCompactFormat(record, previousRecord)) {
            return formatCompactMessage(record.getMessage(), levelConfig, timestamp, loggerName);
        } else {
            return formatFullMessage(record.getMessage(), levelConfig, timestamp, loggerName);
        }
    }

    private @NotNull String extractLoggerName(@NotNull LogRecord record) {
        if (record.getLoggerName() == null) {
            return "Unknown";
        }

        String fullName = record.getLoggerName();
        int lastDotIndex = fullName.lastIndexOf('.');
        return lastDotIndex != -1 ? fullName.substring(lastDotIndex + 1) : fullName;
    }

    private boolean shouldUseCompactFormat(@NotNull LogRecord current, LogRecord previous) {
        if (previous == null || previous.getLoggerName() == null) {
            return false;
        }

        boolean sameLogger = previous.getLoggerName().equals(current.getLoggerName());
        boolean sameLevel = previous.getLevel().equals(current.getLevel());
        boolean withinTimeThreshold = Duration.between(previous.getInstant(), current.getInstant())
                .toMillis() < CONSECUTIVE_THRESHOLD_MS;

        return sameLogger && sameLevel && withinTimeThreshold;
    }

    private @NotNull String formatFullMessage(String message, LogLevelConfig levelConfig,
                                              String timestamp, String loggerName) {
        String prefix = buildFullPrefix(levelConfig, timestamp, loggerName);
        int prefixLength = calculatePrefixLength(levelConfig.icon, timestamp, loggerName);

        return wrapMessage(prefix, message, prefixLength, levelConfig.color) + LogColors.RESET;
    }

    private @NotNull String formatCompactMessage(String message, @NotNull LogLevelConfig levelConfig,
                                                 String timestamp, String loggerName) {
        int prefixLength = calculateCompactPrefixLength(levelConfig.icon, timestamp, loggerName);
        String spaces = " ".repeat(Math.max(0, prefixLength));
        String linePrefix = levelConfig.color + "\033[1|" + spaces + LogColors.RESET;

        return wrapMessage(linePrefix, message, prefixLength + 1, levelConfig.color);
    }

    @Contract(pure = true)
    private @NotNull String buildFullPrefix(@NotNull LogLevelConfig levelConfig, String timestamp, String loggerName) {
        return levelConfig.color + "[" + levelConfig.icon + " " + timestamp + "]" + LogColors.RESET
                + " (" + LogColors.LIGHT_GREEN + loggerName + LogColors.RESET + "): " + LogColors.WHITE;
    }

    private int calculatePrefixLength(String icon, String timestamp, @NotNull String loggerName) {
        return ("[" + icon + " " + timestamp + "]").length() + 3 + loggerName.length() + 3;
    }

    private int calculateCompactPrefixLength(String icon, String timestamp, @NotNull String loggerName) {
        return ("\033[1G[" + icon + " " + timestamp + "]").length() + 3 + loggerName.length();
    }

    /**
     * Wraps a message to fit within the maximum line width.
     */
    private @NotNull String wrapMessage(String prefix, @NotNull String message, int prefixLength, String color) {
        int availableWidth = Math.max(1, MAX_LINE_WIDTH - prefixLength);
        String[] explicitLines = message.split("\n", -1);
        StringBuilder result = new StringBuilder();

        String continuationPrefix = createContinuationPrefix(prefixLength, color);
        boolean isFirstLine = true;

        for (String line : explicitLines) {
            if (isFirstLine) {
                appendLine(result, prefix, line, true);
                isFirstLine = false;
            } else {
                appendLine(result, continuationPrefix, line, false);
            }

            if (line.length() > availableWidth) {
                wrapLongLine(result, line, availableWidth, continuationPrefix);
            }
        }

        return result.toString();
    }

    private @NotNull String createContinuationPrefix(int prefixLength, String color) {
        String spaces = " ".repeat(Math.max(0, prefixLength - 2));
        return color + "|" + spaces + LogColors.RESET;
    }

    private void appendLine(StringBuilder result, String prefix, String line, boolean isFirstLine) {
        if (!isFirstLine) {
            result.append("\n");
        }
        result.append(prefix).append(line);
    }

    private void wrapLongLine(StringBuilder result, @NotNull String line, int availableWidth, String continuationPrefix) {
        int start = availableWidth;

        while (start < line.length()) {
            int end = Math.min(start + availableWidth, line.length());
            int breakPosition = findBreakPosition(line, start, end, availableWidth);

            String segment = line.substring(start, breakPosition).trim();
            result.append("\n").append(continuationPrefix).append(segment);

            start = breakPosition;
            if (start < line.length() && line.charAt(start) == ' ') {
                start++;
            }
        }
    }

    private int findBreakPosition(@NotNull String line, int start, int end, int availableWidth) {
        if (end >= line.length()) {
            return end;
        }

        int lastSpace = line.lastIndexOf(' ', end);
        boolean reasonableBreak = lastSpace > start && (end - lastSpace) < availableWidth / 3;

        return reasonableBreak ? lastSpace : end;
    }
}