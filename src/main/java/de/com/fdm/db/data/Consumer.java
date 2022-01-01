package de.com.fdm.db.data;


import com.google.protobuf.ProtocolStringList;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "consumer")
public class Consumer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consumer_id")
    private Long id;

    private String callback;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "consumer_channel",
            joinColumns = @JoinColumn(name = "consumer_id"),
            inverseJoinColumns = @JoinColumn(name = "channel_id")
    )
    private Set<Channel> channels;

    public void addChannels(ProtocolStringList channels) {
        for (String channel : channels) {
            this.channels.add(new Channel(channel));
        }
    }

    public void removeChannels(ProtocolStringList channels) {
        for (String channel : channels) {
            this.channels.removeIf(c -> c.getName().equals(channel));
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public Set<Channel> getChannels() {
        return channels;
    }

    public void setChannels(Set<Channel> channels) {
        this.channels = channels;
    }
}
