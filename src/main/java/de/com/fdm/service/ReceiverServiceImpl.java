package de.com.fdm.service;

import de.com.fdm.grpc.lib.Empty;
import de.com.fdm.grpc.lib.ReceiverGrpc;
import de.com.fdm.grpc.lib.Registration;
import de.com.fdm.mongo.Consumer;
import de.com.fdm.mongo.ConsumerRepository;
import de.com.fdm.tmi.Reader;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;


@GrpcService
public class ReceiverServiceImpl extends ReceiverGrpc.ReceiverImplBase {

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private Reader reader;

    @Override
    public void register(Registration request, StreamObserver<Empty> responseObserver) {
        Empty response = Empty.newBuilder().build();
        Consumer existingConsumer = this.consumerRepository.findByCallback(request.getCallback());

        if (existingConsumer == null) {
            this.consumerRepository.save(new Consumer(request.getChannel(), request.getCallback()));
            this.reader.joinChannel(request.getChannel());

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        existingConsumer.addChannel(request.getChannel());
        this.consumerRepository.save(existingConsumer);
        this.reader.joinChannel(request.getChannel());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
