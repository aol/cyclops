package cyclops.streams.push.async;

import com.oath.cyclops.react.lazy.sequence.CollectableTest;
import com.oath.cyclops.types.foldable.Folds;
import cyclops.reactive.Spouts;


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
