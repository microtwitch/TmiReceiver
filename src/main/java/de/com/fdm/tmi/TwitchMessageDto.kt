package de.com.fdm.tmi

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent

data class TwitchMessageDto(
    val channel: String,
    val message: String,
    val userId: String,
    val rawMsg: String,
) {
    constructor(event: ChannelMessageEvent) : this(
        event.channel.name,
        event.message,
        event.user.id,
        event.messageEvent.rawMessage
    )
}
