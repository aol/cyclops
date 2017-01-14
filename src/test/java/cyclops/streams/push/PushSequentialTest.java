package cyclops.streams.push;

import com.aol.cyclops2.streams.BaseSequentialTest;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;

/**
 * Created by johnmcclean on 14/01/2017.
 */
public class PushSequentialTest extends BaseSequentialTest {
    <U> ReactiveSeq<U> of(U... array){
        return Spouts.of(array);
    }
}
