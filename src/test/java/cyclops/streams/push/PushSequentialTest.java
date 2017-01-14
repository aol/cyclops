package cyclops.streams.push;

import com.aol.cyclops2.streams.BaseSequentialTest;
import com.aol.cyclops2.types.stream.reactive.ReactiveSubscriber;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.junit.Test;

import java.util.stream.Collectors;

/**
 * Created by johnmcclean on 14/01/2017.
 */
public class PushSequentialTest extends BaseSequentialTest {
    @Override
    protected <U> ReactiveSeq<U> of(U... array){

        return Spouts.of(array);
    }


}
