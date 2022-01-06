package de.com.fdm.client;


import de.com.fdm.grpc.receiver.lib.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptyCallback implements StreamObserver<Empty> {
    Logger logger = LoggerFactory.getLogger(EmptyCallback.class);

    @Override
    public void onNext(Empty value) {
    }

    @Override
    public void onError(Throwable t) {
        logger.error(t.getMessage());
    }

    @Override
    public void onCompleted() {
    }
}
