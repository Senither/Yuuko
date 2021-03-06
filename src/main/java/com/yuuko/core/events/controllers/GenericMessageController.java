package com.yuuko.core.events.controllers;

import com.yuuko.core.Cache;
import com.yuuko.core.Configuration;
import com.yuuko.core.database.DatabaseFunctions;
import com.yuuko.core.database.connections.DatabaseConnection;
import com.yuuko.core.metrics.handlers.MetricsManager;
import com.yuuko.core.modules.Command;
import com.yuuko.core.modules.audio.commands.SearchCommand;
import com.yuuko.core.modules.core.settings.SettingExecuteBoolean;
import com.yuuko.core.utilities.MessageHandler;
import com.yuuko.core.utilities.Sanitiser;
import com.yuuko.core.utilities.TextUtility;
import com.yuuko.core.utilities.Utils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.ResultSet;

public class GenericMessageController {

    public GenericMessageController(GenericMessageEvent e) {
        if(e instanceof MessageReceivedEvent) {
            messageReceivedEvent((MessageReceivedEvent)e);
        }
    }

    private void messageReceivedEvent(MessageReceivedEvent e) {
        try {
            // Increment message counter, regardless of it's author.
            MetricsManager.getEventMetrics().MESSAGES_PROCESSED.getAndIncrement();

            // Figure out if the user is a bot or not, so we don't waste any time.
            if(e.getAuthor().isBot()) {
                return;
            }

            String message = e.getMessage().getContentRaw().toLowerCase();

            String prefix = Utils.getServerPrefix(e.getGuild().getId());
            if(message.startsWith(Configuration.GLOBAL_PREFIX) || prefix.equals("")) {
                prefix = Configuration.GLOBAL_PREFIX;
            }

            // Ignores messages that consist of just the prefix or starts with the prefix twice.
            if(message.equalsIgnoreCase(prefix)
                    || message.startsWith(prefix + prefix)
                    || message.equals(Configuration.GLOBAL_PREFIX)
                    || message.startsWith(Configuration.GLOBAL_PREFIX + Configuration.GLOBAL_PREFIX)) {
                return;
            }

            // Used to help calculate execution time of functions.
            long startExecutionNano = System.nanoTime();

            if(message.startsWith(prefix) || message.startsWith(Configuration.GLOBAL_PREFIX)) {
                processMessage(e, startExecutionNano, prefix);
                return;
            }

            if(Sanitiser.isNumber(message) || message.equals("cancel")) {
                processMessage(e, startExecutionNano);
            }

        } catch(NullPointerException ex) {
            // Do nothing, null pointers happen. (Should they though...)
        } catch (Exception ex) {
            MessageHandler.sendException(ex, "private void messageReceivedEvent(MessageReceivedEvent e)");
        }
    }

    /**
     * Deals with commands that start with a prefix.
     * @param e MessageReceivedEvent
     * @param startExecutionNano long
     * @param prefix String
     */
    private void processMessage(MessageReceivedEvent e, long startExecutionNano, String prefix) {
        String[] command = e.getMessage().getContentRaw().substring(prefix.length()).split("\\s+", 2);
        String commandPrefix = e.getMessage().getContentRaw().substring(0, prefix.length());

        try {
            final long executionTime;

            Constructor<?> constructor = null;
            String moduleDbName = "";

            // Iterate through the command list, if the input matches the effective name (includes invocation)
            // Get the command modules constructor from the command class. (Much easier than what I did previously)
            for(Command cmd : Cache.COMMANDS) {
                if((commandPrefix + command[0]).equalsIgnoreCase(cmd.getGlobalName()) || (commandPrefix + command[0]).equalsIgnoreCase(prefix + cmd.getName())) {
                    constructor = cmd.getModule().getConstructor(MessageReceivedEvent.class, String[].class);
                    break;
                }
            }

            final boolean isAllowed = checkBindings(e, moduleDbName, command[0]);

            if(constructor != null && isAllowed) {
                constructor.newInstance(e, command);
                executionTime = (System.nanoTime() - startExecutionNano)/1000000;
                MessageHandler.sendCommand(e, executionTime);
                new DatabaseFunctions().updateCommandsLog(e.getGuild().getId(), command[0].toLowerCase());

                if(new DatabaseFunctions().getServerSetting("commandLogging", e.getGuild().getId()).equalsIgnoreCase("1")) {
                    new SettingExecuteBoolean(null, null, null).executeLogging(e, executionTime);
                }
            }

        } catch (Exception ex) {
            MessageHandler.sendException(ex, "GenericMessageController ~ " + ex.getMessage() + " ~ " +  e.getMessage().getContentRaw());
        }
    }

    /**
     * Deals with non-prefixed commands that are a number between 1-10.
     * @param e MessageReceivedEvent
     * @param startExecutionNano long
     */
    private void processMessage(MessageReceivedEvent e, long startExecutionNano) {
        try {
            if(Cache.audioSearchResults.containsKey(e.getAuthor().getIdLong())) {
                String[] input = e.getMessage().getContentRaw().toLowerCase().split("\\s+", 2);

                // Search function check if regex matches. Used in conjunction with the search input.
                if(input[0].matches("^[0-9]{1,2}$") || input[0].equals("cancel")) {
                    new SearchCommand().executeCommand(e, input[0]);
                }

                if(new DatabaseFunctions().getServerSetting("deleteExecuted", e.getGuild().getId()).equalsIgnoreCase("1")) {
                    e.getMessage().delete().queue();
                }

                if(new DatabaseFunctions().getServerSetting("commandLogging", e.getGuild().getId()).equalsIgnoreCase("1")) {
                    long executionTime = (System.nanoTime() - startExecutionNano)/1000000;
                    Utils.updateLatest(e.getGuild().getName() + " - " + input[0] + " (" + executionTime + "ms)");
                    new SettingExecuteBoolean(null, null, null).executeLogging(e, executionTime);
                }
            }

        } catch (Exception ex) {
            MessageHandler.sendException(ex, "GenericMessageController (Aux) - " + e.getMessage().getContentRaw());
        }
    }

    /**
     * Checks channel bindings to see if commands are allowed to be executed there.
     * @param e MessageReceivedEvent
     * @param moduleDbName String
     * @return boolean
     */
    private boolean checkBindings(MessageReceivedEvent e, String moduleDbName, String command) {
        try {
            StringBuilder boundChannels = new StringBuilder();
            Connection connection = DatabaseConnection.getConnection();
            ResultSet rs = new DatabaseFunctions().getBindingsByModule(connection, e.getGuild().getId(), moduleDbName);

            while(rs.next()) {
                if(rs.getString(2).equals(e.getTextChannel().getId()) && rs.getString(3).toLowerCase().equals(moduleDbName)) {
                    connection.close();
                    return true;
                }
                boundChannels.append(e.getGuild().getTextChannelById(rs.getString(2)).getName()).append(", ");
            }
            connection.close();

            if(boundChannels.toString().equals("")) {
                return true;
            } else {
                TextUtility.removeLastOccurrence(boundChannels, ", ");

                EmbedBuilder embed = new EmbedBuilder().setTitle("The **_" + command + "_** command is bound to " + boundChannels.toString() + ".");
                MessageHandler.sendMessage(e, embed.build());
                return false;
            }

        } catch(Exception ex) {
            MessageHandler.sendException(ex, "Bindings");
            return true;
        }
    }

}
