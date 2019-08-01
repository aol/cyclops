package cyclops.control.future.futureOverwriteIssue;

import cyclops.control.Either;
import lombok.Getter;

import java.io.IOException;
import java.net.URL;


@Getter
public class SuccessURLDataFileMetadata extends DataFileMetadata {

    private final URL url;

    public SuccessURLDataFileMetadata(long customerId, String type, URL url) {
        super(customerId, type);
        this.url = url;
    }

    @Override
    public Either<IOException,String> loadContents() {
       return Either.right("contents here");
    }

}
