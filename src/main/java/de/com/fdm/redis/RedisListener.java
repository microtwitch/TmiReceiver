package de.com.fdm.redis;

import de.com.fdm.tmi.Reader;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class RedisListener {
    private static final Logger LOG = LoggerFactory.getLogger(RedisListener.class);
    private final RedissonClient client;
    private final Reader reader;

    @Autowired
    public RedisListener(Reader reader) throws IOException {
        this.reader = reader;

        Config config = Config.fromYAML(new File("src/main/resources/redisson_config.yaml"));
        this.client = Redisson.create(config);
    }

    public void setUpCallback() {
        RTopic topic = client.getTopic("tmiReceiver");
        topic.addListener(String.class, ((redisChannel, channel) -> {
            LOG.info("Received channel request: #{}", channel);
            reader.joinChannel(channel);
        }));
    }
}
