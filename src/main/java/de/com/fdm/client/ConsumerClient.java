package de.com.fdm.client;

import de.com.fdm.grpc.lib.ConsumerGrpc;
import de.com.fdm.grpc.lib.TwitchMessage;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ConsumerClient {
    private final ConsumerGrpc.ConsumerStub asyncStub;

    public ConsumerClient(String target) {
        this(ManagedChannelBuilder.forTarget(target).usePlaintext());
    }

    private ConsumerClient(ManagedChannelBuilder<?> channelBuilder) {
        ManagedChannel channel = channelBuilder.build();
        this.asyncStub = ConsumerGrpc.newStub(channel);
    }

    public void sendMessage(TwitchMessage msg) {
        this.asyncStub.consume(msg, new EmptyCallback());
    }
}
