package com.yuuko.core.modules.utility.commands;

import com.yuuko.core.modules.Command;
import com.yuuko.core.modules.utility.UtilityModule;
import com.yuuko.core.utilities.MessageHandler;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ChannelCommand extends Command {

    public ChannelCommand() {
        super("channel", UtilityModule.class, 3, new String[]{"-channel [action] [type] [name]", "-channel [action] [type] [name] [nsfw]"}, new Permission[]{Permission.MANAGE_CHANNEL});
    }

    @Override
    public void executeCommand(MessageReceivedEvent e, String[] command) {
        String[] commandParameters = command[1].split("\\s+", 3);
        String type = commandParameters[1].toLowerCase();

        // Checks the parameters of the command, if the first param is 'add' follow that flow, else if it's 'del' following that flow instead.
        if(commandParameters[0].equals("add")) {
            if(type.equals("text")) {
                e.getGuild().getController().createTextChannel(commandParameters[1]).setNSFW(commandParameters.length > 2).queue();
            } else if(type.equals("voice")) {
                e.getGuild().getController().createVoiceChannel(commandParameters[1]).queue();
            }

        } else if(commandParameters[0].equals("del")) {
            if(type.equals("text")) {
                if(e.getGuild().getTextChannelsByName(commandParameters[2], true).size() == 0) {
                    EmbedBuilder embed = new EmbedBuilder().setTitle("That text-channel could not be found.");
                    MessageHandler.sendMessage(e, embed.build());
                    return;
                }
                e.getGuild().getTextChannelsByName(commandParameters[2], true).get(0).delete().queue();
            } else if(type.equals("voice") && commandParameters[2].length() == 18 && Long.parseLong(commandParameters[2]) > 0) {
                if(e.getGuild().getVoiceChannelsByName(commandParameters[2], true).size() == 0) {
                    EmbedBuilder embed = new EmbedBuilder().setTitle("That voice-channel could not be found.");
                    MessageHandler.sendMessage(e, embed.build());
                    return;
                }
                e.getGuild().getVoiceChannelsByName(commandParameters[1], true).get(0).delete().queue();
            } else {
                e.getTextChannel().delete().queue();
            }

        } else {
            EmbedBuilder embed = new EmbedBuilder().setTitle("Something went wrong while trying to do that.");
            MessageHandler.sendMessage(e, embed.build());
        }
    }

}
