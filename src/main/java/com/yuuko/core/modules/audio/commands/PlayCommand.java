package com.yuuko.core.modules.audio.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.yuuko.core.Configuration;
import com.yuuko.core.modules.Command;
import com.yuuko.core.modules.audio.AudioModule;
import com.yuuko.core.modules.audio.handlers.AudioManagerManager;
import com.yuuko.core.modules.audio.handlers.GuildAudioManager;
import com.yuuko.core.modules.audio.handlers.YouTubeSearchHandler;
import com.yuuko.core.utilities.MessageHandler;
import com.yuuko.core.utilities.TextUtility;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

import static net.dv8tion.jda.core.audio.hooks.ConnectionStatus.CONNECTED;

public class PlayCommand extends Command {

    public PlayCommand() {
        super("play", AudioModule.class, 0, new String[]{"-play", "-play [url]", "-play [term]"}, null);
    }

    @Override
    public void executeCommand(MessageReceivedEvent e, String[] command) {
        GuildAudioManager manager = AudioManagerManager.getGuildAudioManager(e.getGuild().getId());

        if(e.getGuild().getAudioManager().getConnectionStatus() != CONNECTED) {
            e.getGuild().getAudioManager().openAudioConnection(e.getMember().getVoiceState().getChannel());
        }

        if(command.length == 1) {
            if(manager.player.isPaused()) {
                EmbedBuilder embed = new EmbedBuilder().setTitle("Resuming").setDescription("The player has been resumed.");
                MessageHandler.sendMessage(e, embed.build());
                manager.player.setPaused(false);
                new CurrentCommand();
            }

        } else {
            manager.player.setPaused(false);

            if(command[1].startsWith("https://") || command[1].startsWith("http://")) {
                loadAndPlay(manager, e.getChannel(), command[1], e);

            } else {
                String trackId = YouTubeSearchHandler.search(command[1]);

                if(trackId == null || trackId.equals("")) {
                    EmbedBuilder embed = new EmbedBuilder().setTitle("Those search parameters failed to return a result.");
                    MessageHandler.sendMessage(e, embed.build());
                } else {
                    loadAndPlay(manager, e.getChannel(), trackId, e);
                }
            }
        }

    }

    /**
     * Loads a track from a given url and plays it if possible, else adds it to the queue.
     * @param manager ; GuildAudioManager.
     * @param channel ; MessageChannel.
     * @param url ; TrackUrl.
     */
    private void loadAndPlay(GuildAudioManager manager, final MessageChannel channel, String url, MessageReceivedEvent e) {
        final String trackUrl;

        if(url.startsWith("<") && url.endsWith(">")) {
            trackUrl = url.substring(1, url.length() - 1);
        } else {
            trackUrl = url;
        }

        AudioManagerManager.getPlayerManager().loadItemOrdered(manager, trackUrl, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                try {
                    manager.scheduler.queue(track);
                    track.setUserData(e);

                    String[] uri = track.getInfo().uri.split("=");
                    String imageUrl = (uri.length > 1) ? "https://img.youtube.com/vi/" + uri[1] + "/1.jpg" : "https://i.imgur.com/bCNQlm6.jpg";

                    EmbedBuilder embed = new EmbedBuilder()
                            .setAuthor(e.getMember().getEffectiveName() + " added to the queue!", null, e.getAuthor().getAvatarUrl())
                            .setTitle(track.getInfo().title, trackUrl)
                            .setThumbnail(imageUrl)
                            .addField("Duration", TextUtility.getTimestamp(track.getDuration()), true)
                            .addField("Channel", track.getInfo().author, true)
                            .addField("Position in queue", manager.scheduler.queue.size() + "", false)
                            .setFooter(Configuration.VERSION, e.getGuild().getMemberById(Configuration.BOT_ID).getUser().getAvatarUrl());

                    MessageHandler.sendMessage(channel, embed.build());
                } catch(Exception ex) {
                    MessageHandler.sendException(ex, "public void trackLoaded(AudioTrack track) [PlayCommand]");
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                try {
                    EmbedBuilder embed = new EmbedBuilder().setTitle("Adding **" + playlist.getTracks().size() + "** tracks to queue from playlist: **" + playlist.getName() + "**");
                    MessageHandler.sendMessage(channel, embed.build());

                    List<AudioTrack> tracks = playlist.getTracks();
                    for(AudioTrack track: tracks) {
                        track.setUserData(e);
                        manager.scheduler.queue(track);
                    }

                    new CurrentCommand().executeCommand(e, null);

                } catch(Exception ex) {
                    MessageHandler.sendException(ex, "public void playlistLoaded(AudioPlaylist playlist) [PlayCommand]");
                }
            }

            @Override
            public void noMatches() {
                EmbedBuilder embed = new EmbedBuilder().setTitle("No matches found using that parameter.");
                MessageHandler.sendMessage(channel, embed.build());
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                EmbedBuilder embed = new EmbedBuilder().setTitle("Loading failed: " + exception.getMessage());
                MessageHandler.sendMessage(channel, embed.build());
            }
        });
    }
}
