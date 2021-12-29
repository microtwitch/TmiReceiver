package de.com.fdm.tmi;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.com.fdm.client.ClientManager;
import de.com.fdm.db.data.Channel;
import de.com.fdm.db.data.Consumer;
import de.com.fdm.db.services.ChannelService;
import de.com.fdm.db.services.ConsumerService;
import de.com.fdm.grpc.receiver.lib.TwitchMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class Reader {
    private final TwitchClient client;
    private final ClientManager clientManager;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private ChannelService channelService;

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

        Set<Consumer> consumers = this.channelService.findByChannel(event.getChannel().getName());

        for (Consumer consumer : consumers) {
            this.clientManager.sendMessage(msg, consumer.getCallback());
        }
    }

    public void joinChannels(Set<Channel> channels) {
        for (Channel channel : channels) {
            if (this.client.getChat().getChannels().contains(channel.getName())) {
                continue;
            }
            this.client.getChat().joinChannel(channel.getName());
        }
    }
}
