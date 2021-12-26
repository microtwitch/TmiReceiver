package de.com.fdm.mongo;

import java.util.List;

public class Consumer {
    private final List<String> channels;
    private final String callback;

    public Consumer(List<String> channels, String callback) {
        this.channels = channels;
        this.callback = callback;
    }

    public List<String> getChannels() {
        return channels;
    }

    public String getCallback() {
        return callback;
    }

    public void addChannel(List<String> channels) {
        this.channels.addAll(channels);
    }
}
