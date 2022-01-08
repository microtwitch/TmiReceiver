package de.com.fdm.db.services;

import de.com.fdm.db.data.Channel;
import de.com.fdm.db.data.Consumer;
import de.com.fdm.db.repositories.ChannelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ChannelService {
    Logger logger = LoggerFactory.getLogger(ChannelService.class);

    @Autowired
    private ChannelRepository channelRepository;

    public Set<Channel> getAll() {
        Set<Channel> channels = new HashSet<>();

        for (Channel channel : this.channelRepository.findAll()) {
           channels.add(channel);
        }

        return channels;
    }

    public Set<Consumer> findByChannel(String channelName) {
        Channel channel = this.channelRepository.findByName(channelName);
        if (channel == null) {
            logger.info("No consumers associated with channel #{}", channelName);
            return new HashSet<>();
        }

        return channel.getConsumers();
    }

    public void delete(Channel channel) {
        this.channelRepository.delete(channel);
    }
}
