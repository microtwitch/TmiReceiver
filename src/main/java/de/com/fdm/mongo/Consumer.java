package de.com.fdm.mongo;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class Consumer {
    private ObjectId _id;
    private List<String> channels;
    private String callback;

    public Consumer() {}

    public Consumer(String channel, String callback) {
        this.channels = new ArrayList<>();
        this.channels.add(channel);
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

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public void addChannel(String channel) {
        this.channels.add(channel);
    }
}
