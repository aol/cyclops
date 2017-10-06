package cyclops.streams.push.async;

import com.aol.cyclops2.react.lazy.sequence.CollectableTest;
import com.aol.cyclops2.types.foldable.Folds;
import cyclops.stream.Spouts;


public class AsyncCollectableTest extends CollectableTest {


    public <T> Folds<T> of(T... values){

        return Spouts.<T>async(s->{
            Thread t = new Thread(()-> {
                for (T next : values) {
                    s.onNext(next);
                }
                s.onComplete();
            });
            t.start();
        });
    }

}
