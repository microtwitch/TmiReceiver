package de.com.fdm.db.services;

import de.com.fdm.db.data.Channel;
import de.com.fdm.db.data.Consumer;
import de.com.fdm.db.repositories.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ChannelService {

    @Autowired
    private ChannelRepository channelRepository;

    public Set<Channel> getAll() {
        Set<Channel> channels = new HashSet<>();

        for (Channel channel : this.channelRepository.findAll()) {
           channels.add(channel);
        }

        return channels;
    }

    public Set<Consumer> findByChannel(String channel) {
        return this.channelRepository.findByName(channel).getConsumers();
    }
}
