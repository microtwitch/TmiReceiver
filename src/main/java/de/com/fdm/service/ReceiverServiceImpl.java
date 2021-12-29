package de.com.fdm.service;

import de.com.fdm.db.data.Channel;
import de.com.fdm.db.data.Consumer;
import de.com.fdm.db.services.ConsumerService;
import de.com.fdm.grpc.receiver.lib.Empty;
import de.com.fdm.grpc.receiver.lib.ReceiverGrpc;
import de.com.fdm.grpc.receiver.lib.Registration;
import de.com.fdm.tmi.Reader;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;


@GrpcService
public class ReceiverServiceImpl extends ReceiverGrpc.ReceiverImplBase {

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private Reader reader;

    @Override
    public void register(Registration request, StreamObserver<Empty> responseObserver) {
        Empty response = Empty.newBuilder().build();

        Consumer consumer = new Consumer();
        consumer.setCallback(request.getCallback());

        Set<Channel> channels = new HashSet<>();
        for (String channel : request.getChannelsList()) {
            channels.add(new Channel(channel));
        }
        consumer.setChannels(channels);

        this.consumerService.saveConsumer(consumer);

        this.reader.joinChannels(channels);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
