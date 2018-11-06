package cyclops.control.futureOverwriteIssue;

import cyclops.control.Either;
import lombok.Getter;

import java.io.IOException;
import java.net.URL;


@Getter
public class SleepingURLDataFileMetadata extends DataFileMetadata {

    private final URL url;

    public SleepingURLDataFileMetadata(long customerId, String type, URL url) {
        super(customerId, type);
        this.url = url;
    }

    @Override
    public Either<IOException,String> loadContents() {
        System.out.println("Current thread " + Thread.currentThread().getId());
        try {
            Thread.sleep(10000000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Either.right("success");
    }

}
