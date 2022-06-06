package de.com.fdm.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.com.fdm.tmi.TwitchMessageDto;
import org.redisson.Redisson;
import org.redisson.api.RSet;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@Service
public class RedisService {
    private static final Logger LOG = LoggerFactory.getLogger(RedisService.class);
    private static final String CHANNELS_KEY = "channels";
    private final RedissonClient client;
    private final ObjectMapper mapper;

    @Autowired
    public RedisService() throws IOException {
        Config config = Config.fromYAML(new File("src/main/resources/redisson_config.yaml"));
        this.client = Redisson.create(config);

        this.mapper = new ObjectMapper();
    }

    public Set<String> getChannels() {
        RSet<String> channels = client.getSet(CHANNELS_KEY);
        return channels.readAll();
    }

    public void addChannel(String channel) {
        RSet<String> channels = client.getSet(CHANNELS_KEY);
        channels.add(channel);
    }

    public void removeChannel(String channel) {
        RSet<String> channels = client.getSet(CHANNELS_KEY);
        channels.remove(channel);
    }

    public long publishMessage(TwitchMessageDto twitchMessageDto) {
        RTopic topic = client.getTopic("tmiReceiver." + twitchMessageDto.channel());
        try {
            return topic.publish(mapper.writeValueAsString(twitchMessageDto));
        } catch (JsonProcessingException e) {
            LOG.error(e.getMessage());
        }

        return -1;
    }
}
