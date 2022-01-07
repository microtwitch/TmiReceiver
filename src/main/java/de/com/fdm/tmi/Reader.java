package de.com.fdm.tmi;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
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
    private final TwitchClient client;
    private final ClientManager clientManager;
    private final Counter msgCounter;

    @Autowired
    private ChannelService channelService;

    public Reader(MeterRegistry registry) {
        this.client = TwitchClientBuilder.builder().withEnableChat(true).build();
        this.client.getEventManager().onEvent(ChannelMessageEvent.class, this::handleChannelMessage);
        this.clientManager = new ClientManager();

        Gauge.builder("reader.channels", this::getChannelCount).strongReference(true).register(registry);
        this.msgCounter = Counter.builder("reader.messages").register(registry);
    }

    private int getChannelCount() {
        return this.client.getChat().getChannels().size();
    }

    private void handleChannelMessage(ChannelMessageEvent event) {
        TwitchMessage msg = TwitchMessage.newBuilder()
                .setChannel(event.getChannel().getName())
                .setUserName(event.getUser().getName())
                .setUserId(event.getUser().getId())
                .setText(event.getMessage()).build();

        Set<Consumer> consumers = this.channelService.findByChannel(event.getChannel().getName());

        for (Consumer consumer : consumers) {
            this.clientManager.sendMessage(msg, consumer.getCallback());
        }

        msgCounter.increment();
    }

    public void joinChannels(Set<Channel> channels) {
        for (Channel channel : channels) {
            if (this.client.getChat().getChannels().contains(channel.getName())) {
                continue;
            }
            this.client.getChat().joinChannel(channel.getName());
        }
    }

    public void partChannel(String channel) {
        this.client.getChat().leaveChannel(channel);
    }
}
