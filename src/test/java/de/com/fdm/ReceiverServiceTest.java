package de.com.fdm;

import de.com.fdm.client.EmptyCallback;
import de.com.fdm.db.repositories.ConsumerRepository;
import de.com.fdm.grpc.receiver.lib.Empty;
import de.com.fdm.grpc.receiver.lib.Registration;
import de.com.fdm.main.Application;
import de.com.fdm.service.ReceiverServiceImpl;
import io.grpc.Status;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = Application.class)
@DirtiesContext
public class ReceiverServiceTest {

    @Autowired
    private ReceiverServiceImpl receiverService;

    @Autowired
    private ConsumerRepository consumerRepository;

    @MockBean
    private EmptyCallback testCallback;

    @BeforeEach
    private void clearDb() {
        consumerRepository.deleteAll();
    }

    // TODO: this doesn't actually test the behaviour reported in
    //       https://github.com/microtwitch/TmiReceiver/issues/2.
    //       The test doesnt fail if the exception is raised, it has to be manually checked
    //       if it happens!

    @Test
    void testPartFastChat() throws Exception {
        Mockito.doNothing().when(testCallback).onError(Status.UNAVAILABLE.asException());

        Registration registration = Registration.newBuilder()
                .setCallback("localhost:12345")
                .addChannels("tmiloadtesting2")
                .build();

        StreamRecorder<Empty> responseObserver = StreamRecorder.create();
        receiverService.register(registration, responseObserver);

        if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            Assertions.fail("The call did not terminate in time");
        }

        assertNull(responseObserver.getError());
        Thread.sleep(5000);

        receiverService.unsubscribe(registration, responseObserver);
        if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            Assertions.fail("The call did not terminate in time");
        }

        assertNull(responseObserver.getError());
    }
}
