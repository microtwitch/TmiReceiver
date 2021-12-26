package de.com.fdm.mongo;

import org.bson.types.ObjectId;

import java.util.List;

public class Consumer {
    // important to be able to save Consumer again without creating copies
    // mongodb driver uses id for identification
    private ObjectId _id;
    private final List<String> channels;
    private final String callback;

    public Consumer(List<String> channels, String callback) {
        this.channels = channels;
        this.callback = callback;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public List<String> getChannels() {
        return channels;
    }

    public String getCallback() {
        return callback;
    }

    public void addChannel(List<String> channels) {
        for (String channel : channels) {
            if (this.channels.contains(channel)) {
                continue;
            }
            this.channels.add(channel);
        }
    }
}
