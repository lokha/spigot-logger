package ua.lokha.spigotloggerforbungee.paperspigotclasses;


import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.command.ConsoleCommandSender;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.apache.logging.log4j.Logger;
import org.jline.reader.*;
import org.jline.reader.LineReader.Option;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import ua.lokha.spigotloggerforbungee.CommandCompleter;
import ua.lokha.spigotloggerforbungee.SpigotLoggerForBungeePlugin;
import ua.lokha.spigotloggerforbungee.utils.Try;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * this class from paperspigot: com.destroystokyo.paper.console.TerminalHandler
 */
public class TerminalHandler {
    private static Field LineReaderImpl_reading = Try.unchecked(() -> {
        final Field reading = LineReaderImpl.class.getDeclaredField("reading");
        reading.setAccessible(true);
        return reading;
    });

    private TerminalHandler() {
    }

    public static boolean handleCommands(BungeeCord bungeeCord, SpigotLoggerForBungeePlugin plugin, Logger logger) {
        Terminal terminal = TerminalConsoleAppender.getTerminal();
        if (terminal == null) {
            return false;
        } else {
            LineReader reader = LineReaderBuilder.builder().appName("BungeeCord").terminal(terminal)
                    .completer(new CommandCompleter(bungeeCord))
                    .build();
            reader.unsetOpt(Option.INSERT_TAB);
            TerminalConsoleAppender.setReader(reader);

            try {
                while (plugin.isRunning()) {
                    try {
                        String line;
                        try {
                            if (reader instanceof LineReaderImpl) {
                                LineReaderImpl_reading.set(reader, false);
                            }
                            line = reader.readLine("> ");
                            if (reader instanceof LineReaderImpl) {
                                LineReaderImpl_reading.set(reader, true);
                            }
                        } catch (EndOfFileException var9) {
                            continue;
                        }

                        if (line == null) {
                            break;
                        }

                        line = line.trim();
                        if (!line.isEmpty()) {
                            plugin.dispatchCommand(line);
                        }
                    } catch (UserInterruptException e) { // юзер хочет выключить программу
                        throw e;
                    } catch (Exception e) {
                        logger.error("Exception handling console input", e);
                    }
                }
            } catch (UserInterruptException var10) {
                bungeeCord.stop(var10.getClass().getName() + ": " + var10.getMessage());
            } finally {
                TerminalConsoleAppender.setReader(null);
            }

            return true;
        }
    }
}

