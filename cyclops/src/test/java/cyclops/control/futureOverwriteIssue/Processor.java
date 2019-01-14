package cyclops.control.futureOverwriteIssue;


import cyclops.control.Either;
import cyclops.control.LazyEither;
import cyclops.control.LazyEither3;
import cyclops.control.LazyEither4;
import cyclops.control.LazyEither5;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.NonEmptyList;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class Processor {

    public static enum Error {
        NO_PERMISSIONS,LOADING_FAILED
    }
    private final AuthorizationService3 authService = new AuthorizationService3();
    private static final Executor exec = Executors.newFixedThreadPool(10);

    User user = new User(Seq.empty());
    @Test
    public void testFailure() throws MalformedURLException {

        for(int i=0;i<100;i++) {
            System.out.println("*********** " + i);
            URLDataFileMetadata failingURL = new URLDataFileMetadata(10l, "url", new URL("http://oath23232.com"));

            Processor proc = new Processor();
            Either<Error, Vector<String>> x = proc.processUsersFiles(user, NonEmptyList.of(failingURL));

            System.out.println(x);

            assertThat(x, equalTo(Either.left(Error.LOADING_FAILED)));
        }

    }
    @Test
    public void testSuccess() throws MalformedURLException {
        Processor proc = new Processor();
        SuccessURLDataFileMetadata url = new SuccessURLDataFileMetadata(10l, "url", new URL("https://www.rte.ie/"));

        Either<Error, Vector<String>> x = proc.processUsersFiles(user, NonEmptyList.of(url));

        System.out.println(x);

        assertThat(x,equalTo(Either.right(Vector.of("contents here"))));

    }
    @Test
    public void testSleeping() throws MalformedURLException {
        Processor proc = new Processor();
        SleepingURLDataFileMetadata slowUrl = new SleepingURLDataFileMetadata(10l, "url", new URL("https://www.rte.ie/"));

        long start = System.currentTimeMillis();
        Either<Error, Vector<String>> x = proc.processUsersFiles(user, NonEmptyList.of(slowUrl));

        System.out.println(System.currentTimeMillis() + " Blocked? ");
        System.out.println(System.currentTimeMillis() + " No... ");
        long blocked = System.currentTimeMillis();

        System.out.println(x + "Completed at  " + System.currentTimeMillis());
        long completed = System.currentTimeMillis();
        assertThat(blocked-start,lessThan(500l));
        assertThat(completed-start,greaterThan(500l));
            assertThat(x,equalTo(Either.right(Vector.of("success"))));

    }

    @Test
    public void testSleepingFilterSwap() throws MalformedURLException {
        Processor proc = new Processor();
        SleepingURLDataFileMetadata slowUrl = new SleepingURLDataFileMetadata(10l, "url", new URL("https://www.rte.ie/"));

        long start = System.currentTimeMillis();
        Option<Error> x = proc.processUsersFiles(user, NonEmptyList.of(slowUrl))
            .swap().filter(i -> true);

        System.out.println(System.currentTimeMillis() + " Blocked? ");
        System.out.println(System.currentTimeMillis() + " No... ");
        long blocked = System.currentTimeMillis();

        System.out.println(x + "Completed at  " + System.currentTimeMillis());
        long completed = System.currentTimeMillis();
        assertThat(blocked-start,lessThan(500l));
        assertThat(completed-start,greaterThan(500l));
        assertThat(x,equalTo(Option.none()));

    }
    @Test
    public void testSleepingFilterSwap3() throws MalformedURLException {
        Processor proc = new Processor();
        SleepingURLDataFileMetadata slowUrl = new SleepingURLDataFileMetadata(10l, "url", new URL("https://www.rte.ie/"));

        long start = System.currentTimeMillis();
        Maybe<Throwable> x = LazyEither3.fromPublisher(proc.processUsersFiles(user, NonEmptyList.of(slowUrl)))
            .swap1().filter(i -> true);

        System.out.println(System.currentTimeMillis() + " Blocked? ");
        System.out.println(System.currentTimeMillis() + " No... ");
        long blocked = System.currentTimeMillis();

        System.out.println(x + "Completed at  " + System.currentTimeMillis());
        long completed = System.currentTimeMillis();
        assertThat(blocked-start,lessThan(500l));
        assertThat(completed-start,greaterThan(500l));
        assertThat(x,equalTo(Option.none()));

    }
    @Test
    public void testSleepingFilterSwap4() throws MalformedURLException {
        Processor proc = new Processor();
        SleepingURLDataFileMetadata slowUrl = new SleepingURLDataFileMetadata(10l, "url", new URL("https://www.rte.ie/"));

        long start = System.currentTimeMillis();
        Maybe<Throwable> x = LazyEither4.fromPublisher(proc.processUsersFiles(user, NonEmptyList.of(slowUrl)))
            .swap1().filter(i -> true);

        System.out.println(System.currentTimeMillis() + " Blocked? ");
        System.out.println(System.currentTimeMillis() + " No... ");
        long blocked = System.currentTimeMillis();

        System.out.println(x + "Completed at  " + System.currentTimeMillis());
        long completed = System.currentTimeMillis();
        assertThat(blocked-start,lessThan(500l));
        assertThat(completed-start,greaterThan(500l));
        assertThat(x,equalTo(Option.none()));

    }
    @Test
    public void testSleepingFilterSwap5() throws MalformedURLException {
        Processor proc = new Processor();
        SleepingURLDataFileMetadata slowUrl = new SleepingURLDataFileMetadata(10l, "url", new URL("https://www.rte.ie/"));

        long start = System.currentTimeMillis();
        Maybe<Throwable> x = LazyEither5.fromPublisher(proc.processUsersFiles(user, NonEmptyList.of(slowUrl)))
            .swap1().filter(i -> true);

        System.out.println(System.currentTimeMillis() + " Blocked? ");
        System.out.println(System.currentTimeMillis() + " No... ");
        long blocked = System.currentTimeMillis();

        System.out.println(x + "Completed at  " + System.currentTimeMillis());
        long completed = System.currentTimeMillis();
        assertThat(blocked-start,lessThan(500l));
        assertThat(completed-start,greaterThan(500l));
        assertThat(x,equalTo(Option.none()));

    }

    @Test
    public void testSleepingStream() throws MalformedURLException {
        Processor proc = new Processor();
        SleepingURLDataFileMetadata slowUrl = new SleepingURLDataFileMetadata(10l, "url", new URL("https://www.rte.ie/"));

        AtomicLong completed = new AtomicLong();
        AtomicBoolean done = new AtomicBoolean(false);
        long start = System.currentTimeMillis();
        System.out.println("Started " + start);
        ReactiveSeq<Vector<String>> x = proc.processUsersFiles(user, NonEmptyList.of(slowUrl))
                                                            .stream();

        x.forEach(System.out::println,System.out::println,()->{
            completed.set(System.currentTimeMillis());
            done.set(true);

        });
        System.out.println(System.currentTimeMillis() + " Blocked? ");
        System.out.println(System.currentTimeMillis() + " No... ");
        long blocked = System.currentTimeMillis();
        while(!done.get()){

        }

        System.out.println(x + "Completed at  " + completed.get());
       // long completed = System.currentTimeMillis();
        assertThat(blocked-start,lessThan(500l));
        assertThat(completed.get()-start,greaterThan(500l));


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
       /**
        return Spouts.from(Flux.from(files.stream())
                    .flatMap(file->asyncWithRetry(1,file,exec),10))
                    .reduceAll(Vector.<String>empty(), Vector::appendAll)
                    .findFirstOrError()
                    .mapLeft(t-> Error.LOADING_FAILED);
        **/
        return files.stream()
                              .mergeMap(10,file->asyncWithRetry(1,file,exec))
                              .reduceAll(Vector.<String>empty(), Vector::appendAll)
                              .findFirstOrError()
                              .mapLeft(t-> Error.LOADING_FAILED);

    }
}
