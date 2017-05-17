package cyclops.streams.push.async;

import com.aol.cyclops2.react.lazy.sequence.CollectableTest;
import cyclops.stream.Spouts;
import org.jooq.lambda.Collectable;

public class AsyncCollectableTest extends CollectableTest {


    public <T> Collectable<T> of(T... values){

        return Spouts.<T>async(s->{
            Thread t = new Thread(()-> {
                for (T next : values) {
                    s.onNext(next);
                }
                s.onComplete();
            });
            t.start();
        }).statisticalOperations();
    }

}
