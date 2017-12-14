package cyclops.companion;

import cyclops.control.Either;
import com.oath.cyclops.async.adapters.Adapter;
import com.oath.cyclops.async.adapters.Queue;
import com.oath.cyclops.async.adapters.Topic;


import java.util.concurrent.BlockingQueue;

/**
 * This class contains static methods for Structural Pattern matching
 *
 * @author johnmcclean
 *
 */
public interface Eithers {




    /**
     * Create a Pattern Matcher on cyclops2-react adapter type (note this will only fold
     * on known types within the cyclops2-react library)
     *
     * <pre>
     * {@code
     *     Adapter<Integer> adapter = QueueFactories.<Integer>unboundedQueue()
     *                                              .build();
     *
     *     String result =   Xors.adapter(adapter)
                                       .visit(queue->"we have a queue",topic->"we have a topic");
     *
     *    //"we have a queue"
     * }
     * </pre>
     *
     * @param adapter Adapter to fold on
     * @return Structural pattern matcher for Adapter types.
     */
    public static <T> Either<Queue<T>, Topic<T>> adapter(final Adapter<T> adapter) {
        return adapter.matches();
    }




    /**
     * Pattern matching on the blocking / non-blocking nature of a Queue
     *
     * <pre>
     * {@code
     *  Eithers.blocking(new ManyToManyConcurrentArrayQueue(10))
                  .visit(c->"blocking", c->"not")
         //"not"


       Eithers.blocking(new LinkedBlockingQueue(10))
                 .visit(c->"blocking", c->"not")
        //"blocking
     *
     * }
     * </pre>
     *
     * @param queue Queue to pattern fold on
     * @return Pattern matchier on the blocking / non-blocking nature of the supplied Queue
     */
    public static <T> Either<BlockingQueue<T>, java.util.Queue<T>> blocking(final java.util.Queue<T> queue) {

        return queue instanceof BlockingQueue ? Either.<BlockingQueue<T>, java.util.Queue<T>>left((BlockingQueue) queue)
                : Either.<BlockingQueue<T>, java.util.Queue<T>>right(queue);
    }
}
