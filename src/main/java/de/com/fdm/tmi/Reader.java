package de.com.fdm.tmi;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.com.fdm.client.ClientManager;
import de.com.fdm.grpc.lib.TwitchMessage;
import de.com.fdm.mongo.Consumer;
import de.com.fdm.mongo.ConsumerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Reader {
    private final TwitchClient client;
    private final ClientManager clientManager;

    @Autowired
    private ConsumerRepository consumerRepository;

    public Reader() {
        this.client = TwitchClientBuilder.builder().withEnableChat(true).build();
        this.client.getEventManager().onEvent(ChannelMessageEvent.class, this::handleChannelMessage);
        this.clientManager = new ClientManager();
    }

    private void handleChannelMessage(ChannelMessageEvent event) {
        TwitchMessage msg = TwitchMessage.newBuilder()
                .setChannel(event.getChannel().getName())
                .setName(event.getUser().getName())
                .setText(event.getMessage()).build();

        List<Consumer> consumers = this.consumerRepository.findByChannels(event.getChannel().getName());

        for (Consumer consumer : consumers) {
            if (consumer.getChannels().contains(msg.getChannel())) {
                this.clientManager.sendMessage(msg, consumer.getCallback());
            }
        }
    }

    public void joinChannel(List<String> channels) {
        for (String channel : channels) {
            if (this.client.getChat().getChannels().contains(channel)) {
                continue;
            }
            this.client.getChat().joinChannel(channel);
        }
    }
}
