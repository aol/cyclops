package cyclops.stream.pushable;

import com.oath.cyclops.async.adapters.Queue;
import cyclops.futurestream.FutureStream;

/**
 * A more concrete Tuple2 impl
 * _1 is Queue&lt;T&gt;
 * _2 is LazyFutureStream&lt;T&gt;
 *
 * @author johnmcclean
 *
 * @param <T> data type
 */
public class PushableFutureStream<T> extends AbstractPushableStream<T, Queue<T>, FutureStream<T>> {

    public PushableFutureStream(final Queue<T> v1, final FutureStream<T> v2) {
        super(v1, v2);

    }

    private static final long serialVersionUID = 1L;

}
