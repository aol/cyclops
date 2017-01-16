package cyclops.streams.push;

import com.aol.cyclops2.streams.BaseSequentialTest;
import com.aol.cyclops2.types.stream.reactive.ReactiveSubscriber;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 14/01/2017.
 */
public class PushSequentialTest extends BaseSequentialTest {
    @Override
    protected <U> ReactiveSeq<U> of(U... array){

        return Spouts.of(array);
    }

}
