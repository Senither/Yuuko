package com.yuuko.core.modules.interaction.commands;

import com.yuuko.core.modules.Command;
import com.yuuko.core.modules.interaction.InteractionModule;
import com.yuuko.core.utilities.MessageHandler;
import com.yuuko.core.utilities.MessageUtility;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Random;

public class LaughCommand extends Command {
    private static final String[] interactionImage = new String[]{
            "https://i.imgur.com/SGboaP0.gif",
            "https://i.imgur.com/S0m2mfm.gif",
            "https://i.imgur.com/12T0WK1.gif",
            "https://i.imgur.com/1i53Pu5.gif",
            "https://i.imgur.com/EgOdPmj.gif"
    };

    public LaughCommand() {
        super("laugh", InteractionModule.class, 0, new String[]{"-laugh", "-laugh @user"}, null);
    }

    @Override
    public void executeCommand(MessageReceivedEvent e, String[] command) {
        if(MessageUtility.checkIfUserMentioned(e)) {
            Member target = MessageUtility.getFirstMentionedMember(e);
            if(target != null) {
                EmbedBuilder embed = new EmbedBuilder().setDescription("**" + e.getMember().getEffectiveName() + "** laughs at **" + target.getEffectiveName() + "**.").setImage(interactionImage[new Random().nextInt(interactionImage.length -1)]);
                MessageHandler.sendMessage(e, embed.build());
            }
        } else {
            EmbedBuilder embed = new EmbedBuilder().setDescription("**" + e.getMember().getEffectiveName() + "** laughs.").setImage(interactionImage[new Random().nextInt(interactionImage.length -1)]);
            MessageHandler.sendMessage(e, embed.build());
        }
    }

}
