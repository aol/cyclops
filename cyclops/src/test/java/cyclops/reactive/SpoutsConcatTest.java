package cyclops.reactive;

import cyclops.data.Vector;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SpoutsConcatTest {
    @Test
    public void three() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Integer>> result = new AtomicReference<>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);
        ReactiveSeq<Integer> rs = ReactiveSeq.of(1, 2, 3);

        Spouts.concat(Vector.of(rs, rs, rs)).forEach(n -> {
            data.set(true);
            result.updateAndGet(v->v.plus(n));
        }, e -> {
            error.set(e);
        }, () -> {
            complete.set(true);
        });

        assertThat(data.get(), equalTo(true));
        assertThat(complete.get(), equalTo(true));
        assertThat(error.get(), equalTo(null));
        assertThat(result.get(),equalTo(Vector.of(1, 2, 3, 1, 2, 3, 1, 2, 3)));
    }
    @Test
    public void threeIncremental() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Integer>> result = new AtomicReference<>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);
        ReactiveSeq<Integer> rs = ReactiveSeq.of(1, 2, 3);

        Subscription sub = Spouts.concat(Vector.of(rs, rs, rs)).forEach(0,n -> {
            data.set(true);
            result.updateAndGet(v->v.plus(n));
        }, e -> {
            error.set(e);
        }, () -> {
            complete.set(true);
        });
        assertThat(data.get(), equalTo(false));
        assertThat(complete.get(), equalTo(false));
        assertThat(error.get(), equalTo(null));
        assertThat(result.get(),equalTo(Vector.of()));

        sub.request(1);

        assertThat(data.get(), equalTo(true));
        assertThat(complete.get(), equalTo(false));
        assertThat(error.get(), equalTo(null));
        assertThat(result.get(),equalTo(Vector.of(1)));

        sub.request(4);

        assertThat(data.get(), equalTo(true));
        assertThat(complete.get(), equalTo(false));
        assertThat(error.get(), equalTo(null));
        assertThat(result.get(),equalTo(Vector.of(1,2,3,1,2)));

        sub.request(40000);


        assertThat(data.get(), equalTo(true));
        assertThat(complete.get(), equalTo(true));
        assertThat(error.get(), equalTo(null));
        assertThat(result.get(),equalTo(Vector.of(1, 2, 3, 1, 2, 3, 1, 2, 3)));
    }


    @Test
    public void append(){
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Integer>> result = new AtomicReference<>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);
        Subscription sub =  Spouts.concat(Spouts.of(1),Spouts.of(2,3,4))
            .forEach(0,n -> {
                data.set(true);
                result.updateAndGet(v->v.plus(n));
            }, e -> {
                error.set(e);
            }, () -> {
                complete.set(true);
            });
        assertThat(data.get(), equalTo(false));
        assertThat(complete.get(), equalTo(false));
        assertThat(error.get(), equalTo(null));
        assertThat(result.get(),equalTo(Vector.of()));

        sub.request(1);

        assertThat(data.get(), equalTo(true));
        assertThat(complete.get(), equalTo(false));
        assertThat(error.get(), equalTo(null));
        assertThat(result.get(),equalTo(Vector.of(1)));

        sub.request(4);

        assertThat(data.get(), equalTo(true));
        assertThat(complete.get(), equalTo(true));
        assertThat(error.get(), equalTo(null));
        assertThat(result.get(),equalTo(Vector.of(1,2,3,4)));
    }



    @Test(expected = NullPointerException.class)
    public void testNull(){
       Spouts.concat(Spouts.of(null));
   }

    @Test
    public void longRange() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Integer>> result = new AtomicReference<>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);
        Spouts.concat(Spouts.range(1, 1_000_000_000),Spouts.empty())
            .take(10).forEach(n -> {
            data.set(true);
            result.updateAndGet(v->v.plus(n));
        }, e -> {
            error.set(e);
        }, () -> {
            complete.set(true);
        });

        assertThat(data.get(), equalTo(true));
        assertThat(complete.get(), equalTo(true));
        assertThat(error.get(), equalTo(null));
        assertThat(result.get(),equalTo(Vector.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));

    }
    @Test
    public void nested() {
        ReactiveSeq<String> f = Spouts.concat(Spouts.of("test"), Spouts.of("test2")).appendStream(Spouts.of("test3"));



        StepVerifier.create(f)
                    .expectNext("test", "test2", "test3")
                    .verifyComplete();
    }

    int expected = 0;
    @Test
    public void veryLarge(){
        expected = 0;
        ReactiveSeq<ReactiveSeq<Integer>> largeStreamOfStreams = Spouts.range(0,10_000).map(i->Spouts.range(10_000*i,(10_000*i)+10_000));
        Spouts.concat(largeStreamOfStreams).forEach(i->{
            i.forEach(n->{
                assertThat(n,equalTo(expected++));
            });
        });
        assertThat(expected,equalTo(100000000));
    }


}
