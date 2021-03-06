package com.yuuko.core.modules.interaction.commands;

import com.yuuko.core.modules.Command;
import com.yuuko.core.modules.interaction.InteractionModule;
import com.yuuko.core.utilities.MessageHandler;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Random;

public class SleepCommand extends Command {
    private static final String[] interactionImage = new String[]{
            "https://i.imgur.com/W5SEYT6.gif",
            "https://i.imgur.com/7AOboGB.gif",
            "https://i.imgur.com/u559Fgp.gif",
            "https://i.imgur.com/qWm5FfT.gif",
            "https://i.imgur.com/1wDyaRE.gif"
    };

    public SleepCommand() {
        super("sleep", InteractionModule.class, 0, new String[]{"-sleep"}, null);
    }

    @Override
    public void executeCommand(MessageReceivedEvent e, String[] command) {
        EmbedBuilder embed = new EmbedBuilder().setDescription("**" + e.getMember().getEffectiveName() + "** goes to sleep.").setImage(interactionImage[new Random().nextInt(interactionImage.length -1)]);
        MessageHandler.sendMessage(e, embed.build());
    }

}
