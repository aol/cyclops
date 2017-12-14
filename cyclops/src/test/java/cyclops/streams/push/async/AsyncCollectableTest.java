package cyclops.streams.push.async;

import com.oath.cyclops.types.foldable.Folds;
import cyclops.reactive.Spouts;
import cyclops.streams.CollectableTest;


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
