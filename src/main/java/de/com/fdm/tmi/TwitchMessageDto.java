package de.com.fdm.tmi;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

public record TwitchMessageDto(String channel, String message, String userId, String rawMsg) {
    public static TwitchMessageDto fromEvent(ChannelMessageEvent event) {
        return new TwitchMessageDto(
                event.getChannel().getName(),
                event.getMessage(),
                event.getUser().getId(),
                event.getMessageEvent().getRawMessage()
        );
    }
}
