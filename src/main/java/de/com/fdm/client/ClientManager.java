package de.com.fdm.client;

import de.com.fdm.grpc.receiver.lib.TwitchMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ClientManager {
    private final HashMap<String, ConsumerClient> clients;

    @Autowired
    private EmptyCallback emptyCallback;

    public ClientManager() {
        this.clients = new HashMap<>();
    }

    public void sendMessage(TwitchMessage msg, String target) {
        if (!this.clients.containsKey(target)) {
            this.clients.put(target, new ConsumerClient(target, emptyCallback));
        }

        ConsumerClient client = this.clients.get(target);
        client.sendMessage(msg);
    }
}
