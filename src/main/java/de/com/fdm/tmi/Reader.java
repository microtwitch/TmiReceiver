package de.com.fdm.tmi;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageActionEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.com.fdm.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

@Component
public class Reader {
    private static final Logger LOG = LoggerFactory.getLogger(Reader.class);
    // if this many messages don't get picked up by a client, the channel gets parted
    private final int inactivityThreshold;
    private final TwitchChat twitchChat;
    private final RedisService redisService;

    private final HashMap<String, Queue<Long>> channelUsageHistory;

    @Autowired
    public Reader(RedisService redisService, @Value("${inactivityThreshold}") int inactivityThreshold) {
        this.redisService = redisService;
        this.inactivityThreshold = inactivityThreshold;
        this.channelUsageHistory = new HashMap<>();

        twitchChat = TwitchChatBuilder.builder().build();
        twitchChat.getEventManager().onEvent(ChannelMessageEvent.class, this::handleMessage);
        twitchChat.getEventManager().onEvent(ChannelMessageActionEvent.class, this::handleActionMessage);
    }

    private void handleMessage(ChannelMessageEvent event) {
        TwitchMessageDto twitchMessageDto = TwitchMessageDto.fromEvent(event);
        String channel = twitchMessageDto.channel();

        if (!isChannelUsed(channel)) {
            leaveChannel(channel);
            LOG.info("Left channel #{} due to inactivity.", channel);
            return;
        }

        long numClients = redisService.publishMessage(twitchMessageDto);

        if (channelUsageHistory.get(channel).size() == inactivityThreshold) {
            channelUsageHistory.get(channel).remove();
        }

        channelUsageHistory.get(channel).add(numClients);
    }

    private boolean isChannelUsed(String channel) {
        if (channelUsageHistory.get(channel).size() != inactivityThreshold) {
            return true;
        }

        for (Long entry : channelUsageHistory.get(channel)) {
            if (entry != 0) {
                return true;
            }
        }
        return false;
    }

    private void handleActionMessage(ChannelMessageActionEvent event) {
        ChannelMessageEvent messageEvent = new ChannelMessageEvent(
                event.getChannel(),
                event.getMessageEvent(),
                event.getUser(),
                event.getMessage(),
                event.getPermissions()
        );
        this.handleMessage(messageEvent);
    }

    // twitch4j handles joining the same chat multiple times
    // the join gets ignored in that case!
    public void joinChannel(String channel) {
        channelUsageHistory.put(channel, new ArrayBlockingQueue<>(inactivityThreshold));
        redisService.addChannel(channel);
        twitchChat.joinChannel(channel);

        LOG.info("Joined channel #{}.", channel);
    }

    private void leaveChannel(String channel) {
        redisService.removeChannel(channel);
        twitchChat.leaveChannel(channel);
    }

    public void joinSavedChannels() {
        Set<String> channels = redisService.getChannels();

        for (String channel : channels) {
            joinChannel(channel);
        }
    }
}
