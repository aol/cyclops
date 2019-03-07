package cyclops.streams;

import cyclops.data.Vector;
import cyclops.reactive.FluxReactiveSeq;
import cyclops.reactive.ReactiveSeq;
import org.junit.Ignore;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class FluxReactiveSeqTest extends AbstractReactiveSeqTest {
    @Override
    public ReactiveSeq<Integer> of(Integer... values) {
        return FluxReactiveSeq.of(values);
    }

    @Override
    public ReactiveSeq<Integer> empty() {
        return FluxReactiveSeq.empty();
    }

    @Override @Ignore
    public void onErrorList() {
        super.onErrorList();
    }

    @Override @Ignore
    public void onErrorIterator() {
        super.onErrorIterator();
    }

    public void onError(){
        AtomicInteger count = new AtomicInteger(0);
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Integer>> result = new AtomicReference<>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);



        of(1, 2, 3).<Integer>map(i -> {
            throw new RuntimeException();
        })
            .onError(e -> count.incrementAndGet())
            .forEach(n -> {
                result.updateAndGet(v->v.plus(n));
                data.set(true);
            }, e -> {
                error.set(e);
            }, () -> {
                complete.set(true);
            });

        while(error.get()==null){
            LockSupport.parkNanos(10l);
        }
        assertThat(data.get(), equalTo(false));
        assertThat(complete.get(), equalTo(false));
        assertThat(error.get(), instanceOf(RuntimeException.class));
        assertThat(result.get(),equalTo(Vector.empty()));




        assertThat(count.get(),equalTo(1));

    }

    @Override @Ignore
    public void onErrorIncremental() throws InterruptedException {

    }

    @Override @Ignore
    public void onErrorEmptyList() {

    }

    @Override @Ignore
    public void onErrorEmptyIterator() {

    }

    @Override @Ignore
    public void onErrorEmpty() {
    }

    @Override @Ignore
    public void onErrorEmptyIncremental() {

    }
}
