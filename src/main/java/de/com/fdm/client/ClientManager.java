package de.com.fdm.client;

import de.com.fdm.grpc.lib.TwitchMessage;

import java.util.HashMap;

public class ClientManager {
    private final HashMap<String, ConsumerClient> clients;

    public ClientManager() {
        this.clients = new HashMap<>();
    }

    public void sendMessage(TwitchMessage msg, String callback) {
        if (!this.clients.containsKey(callback)) {
            this.clients.put(callback, new ConsumerClient(callback));
        }

        ConsumerClient client = this.clients.get(callback);

        client.sendMessage(msg);
    }
}
