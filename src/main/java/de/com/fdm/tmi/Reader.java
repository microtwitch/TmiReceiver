package de.com.fdm.tmi;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageActionEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.com.fdm.client.ClientManager;
import de.com.fdm.db.data.Channel;
import de.com.fdm.db.data.Consumer;
import de.com.fdm.db.services.ChannelService;
import de.com.fdm.grpc.receiver.lib.TwitchMessage;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class Reader {
    private final TwitchChat client;
    private final ClientManager clientManager;
    private final Counter msgCounter;

    @Autowired
    private ChannelService channelService;

    public Reader(MeterRegistry registry) {
        client = TwitchChatBuilder.builder().build();
        client.getEventManager().onEvent(ChannelMessageEvent.class, this::handleChannelMessage);
        client.getEventManager().onEvent(ChannelMessageActionEvent.class, this::handleMeMessage);
        clientManager = new ClientManager();

        Gauge.builder("reader.channels", this::getChannelCount).strongReference(true).register(registry);
        msgCounter = Counter.builder("reader.messages").register(registry);
    }

    private int getChannelCount() {
        return client.getChannels().size();
    }

    private void handleChannelMessage(ChannelMessageEvent event) {
        TwitchMessage msg = TwitchMessage.newBuilder()
                .setChannel(event.getChannel().getName())
                .setUserName(event.getUser().getName())
                .setUserId(event.getUser().getId())
                .setText(event.getMessage()).build();

        Set<Consumer> consumers = channelService.findByChannel(event.getChannel().getName());

        for (Consumer consumer : consumers) {
            clientManager.sendMessage(msg, consumer.getCallback());
        }

        msgCounter.increment();
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

    public void joinChannels(Set<Channel> channels) {
        for (Channel channel : channels) {
            if (client.getChannels().contains(channel.getName())) {
                continue;
            }
            client.joinChannel(channel.getName());
        }
    }

    public void partChannel(String channel) {
        client.leaveChannel(channel);
    }
}
