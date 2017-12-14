package cyclops.stream.pushable;

import java.util.stream.Stream;

import com.oath.cyclops.async.adapters.Queue;

/**
 * A more concrete Tuple2 impl
 * _1 is Queue&lt;T&gt;
 * _2 is Stream&lt;T&gt;
 *
 * @author johnmcclean
 *
 * @param <T> data type
 */
public class PushableStream<T> extends AbstractPushableStream<T, Queue<T>, Stream<T>> {

    public PushableStream(final Queue<T> v1, final Stream<T> v2) {
        super(v1, v2);
    }

    private static final long serialVersionUID = 1L;

}
