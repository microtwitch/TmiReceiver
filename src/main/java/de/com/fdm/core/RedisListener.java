package de.com.fdm.core;

import de.com.fdm.tmi.Reader;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class RedisListener {

    @Autowired
    public RedisListener(Reader reader) throws IOException {
        Config config = Config.fromYAML(new File("src/main/resources/redisson_config.yaml"));
        RedissonClient client = Redisson.create(config);
        RTopic topic = client.getTopic("tmiReceiver");
        topic.addListener(String.class, (channel, twitchChannel) -> reader.joinChannel(twitchChannel));
    }
}
