package cyclops.streams.push.async;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.streams.AbstractReactiveSeqTest;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class AsyncReactiveSeqTest extends AbstractReactiveSeqTest {

    @Override
    public ReactiveSeq<Integer> of(Integer... values) {
        return Spouts.async(s->{
            Thread t = new Thread(()-> {
                for (Integer next : values) {
                    s.onNext(next);
                }
                s.onComplete();
            });
            t.start();
        });
    }

    @Override
    public ReactiveSeq<Integer> empty() {
        return Spouts.async(s->{
            Thread t = new Thread(()-> {
                s.onComplete();
            });
            t.start();
        });
    }

    @Test
    public void onErrorList(){
        AtomicInteger count = new AtomicInteger(0);

        try {
            of(1, 2, 3).map(i -> {
                throw new RuntimeException();
            })
                .onError(e -> count.incrementAndGet())
                .toList();
            fail("exception expected");
        }catch(Exception e){
            e.printStackTrace();
        }


        assertThat(count.get(),equalTo(3));

    }




}
