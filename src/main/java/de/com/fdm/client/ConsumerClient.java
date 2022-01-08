package de.com.fdm.client;

import de.com.fdm.grpc.receiver.lib.ConsumerGrpc;
import de.com.fdm.grpc.receiver.lib.TwitchMessage;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ConsumerClient {
    private final ConsumerGrpc.ConsumerStub asyncStub;
    private EmptyCallback emptyCallback;

    public ConsumerClient(String target, EmptyCallback emptyCallback) {
        this(ManagedChannelBuilder.forTarget(target).usePlaintext());
        this.emptyCallback = emptyCallback;
    }

    private ConsumerClient(ManagedChannelBuilder<?> channelBuilder) {
        ManagedChannel channel = channelBuilder.build();
        this.asyncStub = ConsumerGrpc.newStub(channel);
    }

    public void sendMessage(TwitchMessage msg) {
        this.asyncStub.consume(msg, emptyCallback);
    }
}
