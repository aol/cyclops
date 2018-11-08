package cyclops.control.futureOverwriteIssue;

import cyclops.control.Either;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;


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
