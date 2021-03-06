package com.yuuko.core.modules.nsfw.commands;

import com.google.gson.JsonObject;
import com.yuuko.core.Cache;
import com.yuuko.core.Configuration;
import com.yuuko.core.modules.Command;
import com.yuuko.core.modules.nsfw.NSFWModule;
import com.yuuko.core.utilities.MessageHandler;
import com.yuuko.core.utilities.json.JsonBuffer;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class NekoCommand extends Command {

    public NekoCommand() {
        super("neko", NSFWModule.class, 0, new String[]{"-neko", "-neko [type]"}, null);
    }

    @Override
    public void executeCommand(MessageReceivedEvent e, String[] command) {
        try {
            JsonObject json = new JsonBuffer("https://nekos.life/api/v2/img/" + ((command.length > 1) ? command[1] : "lewd"), "default", "default", null, null).getAsJsonObject();

            if(!json.has("msg")) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Neko: " + json.get("url").getAsString())
                        .setImage(json.get("url").getAsString())
                        .setFooter(Cache.STANDARD_STRINGS[1] + e.getAuthor().getName(), e.getGuild().getMemberById(Configuration.BOT_ID).getUser().getAvatarUrl());
                MessageHandler.sendMessage(e, embed.build());
            } else {
                EmbedBuilder embed = new EmbedBuilder().setTitle("Invalid Parameter").setDescription("'femdom', 'nekoapi_v3', 'tickle', 'classic', 'ngif', 'erofeet', 'meow', 'erok', 'poke', 'les', 'hololewd', 'lewdk', 'keta', 'feetg', 'nsfw_neko_gif', 'eroyuri', 'kiss', '8ball', 'kuni', 'tits', 'pussy_jpg', 'cum_jpg', 'pussy', 'lewdkemo', 'lizard', 'slap', 'lewd', 'cum', 'cuddle', 'spank', 'smallboobs', 'Random_hentai_gif', 'avatar', 'fox_girl', 'nsfw_avatar', 'hug', 'gecg', 'boobs', 'pat', 'feet', 'smug', 'kemonomimi', 'solog', 'holo', 'wallpaper', 'bj', 'woof', 'yuri', 'trap', 'anal', 'baka', 'blowjob', 'holoero', 'feed', 'neko', 'gasm', 'hentai', 'futanari', 'ero', 'solo', 'waifu', 'pwankg', 'eron', 'erokemo'");
                MessageHandler.sendMessage(e, embed.build());
            }

        } catch(Exception ex) {
            MessageHandler.sendException(ex, e.getMessage().getContentRaw());
        }
    }

}
