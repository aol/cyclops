package cyclops.control.futureOverwriteIssue;


import cyclops.control.Either;
import cyclops.control.LazyEither;
import cyclops.data.NonEmptyList;
import cyclops.data.Seq;
import cyclops.data.Vector;
import lombok.AllArgsConstructor;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;



public class Processor {

    public static enum Error {
        NO_PERMISSIONS,LOADING_FAILED
    }
    private final AuthorizationService3 authService = new AuthorizationService3();
    private final Executor exec = Executors.newCachedThreadPool();

    @Test
    public void testFailure() throws MalformedURLException {
        URLDataFileMetadata meta = new URLDataFileMetadata(10l, "url", new URL("http://oath23232.com"));
        Either<Error, Vector<String>> x = new Processor().processUsersFiles(new User(Seq.empty()), NonEmptyList.of(meta));

        System.out.println(x);

        assertThat(x,equalTo(Either.left(Error.LOADING_FAILED)));

    }
    @Test
    public void testSuccess() throws MalformedURLException {
        SuccessURLDataFileMetadata meta = new SuccessURLDataFileMetadata(10l, "url", new URL("https://www.rte.ie/"));

        Either<Error, Vector<String>> x = new Processor().processUsersFiles(new User(Seq.empty()), NonEmptyList.of(meta));

        System.out.println(x);

        assertThat(x,equalTo(Either.right(Vector.of("success"))));

    }
    @Test
    public void testSleeping() throws MalformedURLException {
        SleepingURLDataFileMetadata sleeping = new SleepingURLDataFileMetadata(10l, "url", new URL("https://www.rte.ie/"));

        Either<Error, Vector<String>> x = new Processor().processUsersFiles(new User(Seq.empty()), NonEmptyList.of(sleeping));
        System.out.println("Blocked?");
        System.out.println(x);

        assertThat(x,equalTo(Either.right(Vector.of("success"))));

    }

    public Either<Error,Vector<String>> processUsersFiles(User user, NonEmptyList<DataFileMetadata> files){

        return authService.isAuthorized(user, files)
                          .flatMap(this::loadContents);

    }

    public LazyEither<IOException,String> asyncWithRetry(int retries, DataFileMetadata file, Executor e){
        return  file.loadAsync(e)
                .flatMapLeft(error -> retries > 0 ? asyncWithRetry(retries - 1, file, r -> r.run()) : Either.left(error));




    }
    private Either<Error,Vector<String>> loadContents(Vector<DataFileMetadata> files){
        return files.stream()
                    .mergeMap(10,file->asyncWithRetry(1,file,exec))
                    .reduceAll(Vector.<String>empty(), Vector::appendAll)
                    .findFirstOrError()
                    .mapLeft(t-> Error.LOADING_FAILED);
    }
}
