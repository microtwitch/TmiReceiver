package de.com.fdm.client;

import de.com.fdm.grpc.lib.Empty;
import io.grpc.stub.StreamObserver;

public class EmptyCallback implements StreamObserver<Empty> {
    @Override
    public void onNext(Empty value) {
    }

    @Override
    public void onError(Throwable t) {
        System.out.println(t.getMessage());
    }

    @Override
    public void onCompleted() {
    }
}
