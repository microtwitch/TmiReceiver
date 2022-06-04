package de.com.fdm.tmi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageActionEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class Reader {
    private static final Logger LOG = LoggerFactory.getLogger(Reader.class);
    private final TwitchChat twitchChat;
    private final RedissonClient redissonClient;
    private final ObjectMapper mapper;

    @Autowired
    public Reader() throws IOException {
        this.mapper = new ObjectMapper();

        Config config = Config.fromYAML(new File("src/main/resources/redisson_config.yaml"));
        this.redissonClient = Redisson.create(config);

        twitchChat = TwitchChatBuilder.builder().build();
        twitchChat.getEventManager().onEvent(ChannelMessageEvent.class, this::handleChannelMessage);
        twitchChat.getEventManager().onEvent(ChannelMessageActionEvent.class, this::handleMeMessage);
    }

    private void handleChannelMessage(ChannelMessageEvent event) {
        TwitchMessageDto twitchMessageDto = TwitchMessageDto.fromEvent(event);

        RTopic topic = redissonClient.getTopic("tmiReceiver." + event.getChannel().getName());
        try {
            topic.publish(mapper.writeValueAsString(twitchMessageDto));
        } catch (JsonProcessingException e) {
            LOG.error(e.getMessage());
        }
    }

    private void handleMeMessage(ChannelMessageActionEvent event) {
        ChannelMessageEvent messageEvent = new ChannelMessageEvent(
                event.getChannel(),
                event.getMessageEvent(),
                event.getUser(),
                event.getMessage(),
                event.getPermissions()
        );
        this.handleChannelMessage(messageEvent);
    }

    public void joinChannel(String channel) {
        twitchChat.joinChannel(channel);
    }
}
