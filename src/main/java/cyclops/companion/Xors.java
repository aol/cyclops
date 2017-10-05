package cyclops.companion;

import cyclops.monads.AnyM;
import cyclops.control.Xor;
import cyclops.async.adapters.Adapter;
import cyclops.async.adapters.Queue;
import cyclops.async.adapters.Topic;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import com.aol.cyclops2.types.anyM.AnyMValue;
import cyclops.monads.WitnessType;

import java.util.concurrent.BlockingQueue;

/**
 * This class contains static methods for Structural Pattern matching
 * 
 * @author johnmcclean
 *
 */
public class Xors {

   


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
    public static <T> Xor<Queue<T>, Topic<T>> adapter(final Adapter<T> adapter) {
        return adapter.matches();
    }

    /**
     * Create a Pattern Matcher on CompletableFutures, specify success and failure event paths
     * 
     * <pre>
     * {@code 
     *  Eval<Integer> result = Xors.future(CompletableFuture.completedFuture(10))
                                         .matches(c-> 
                                                     c.is( when(some(10)), transform(20)),  //success
                                                      
                                                     c->c.is(when(instanceOf(RuntimeException.class)), transform(2)), //failure
                                                      
                                                     otherwise(3) //no fold
                                                 );
        
        //Eval[20]
     * 
     * }</pre>
     * 
     * 
     * @param future Future to fold on
     * @return Pattern Matcher for CompletableFutures
      USE EITHER HERE
    public static <T1> Xor<T1, Throwable> future(final CompletableFuture<T1> future) {
        return () -> Future.of(future)
                            .toXor()
                            .swap();
    }*/

    /**
     * Pattern Match on a Future handling success and failure cases differently
     * <pre>
     * {@code 
     *  Eval<Integer> result = Xors.future(Future.ofResult(1))
                                         .matches(c-> c.is( when(some(1)), transform(10)),
                                                  c->c.is(when(instanceOf(RuntimeException.class)), transform(2)),
                                                  otherwise(3));
        
        //Eval.now[10]
     * 
     *  Eval<Integer> result = Xors.future(Future.ofError(new RuntimeException()))
                                         .matches(c-> c.is( when(some(10)), transform(2)),
                                                  c->c.is(when(instanceOf(RuntimeException.class)), transform(2)),
                                                  otherwise(3));
        
       //Eval.now(2)
     * 
     * 
     * }
     * </pre>
     * 
     * 
     * 
     * 
     * @param future Future to fold on
     * @return Pattern Matcher for Futures

    public static <T1> MXor<T1, Throwable> future(final Future<T1> future) {
        return () -> future.toXor()
                           .swap();
    }
USE EITHER
    public static <T1, X extends Throwable> MXor<T1, X> tryMatch(final Try<T1, X> fold) {
        return () -> fold.toXor()
                          .swap();
    }
*/

    public static <W extends WitnessType<W>,T> Xor<AnyMValue<W,T>, AnyMSeq<W,T>> anyM(final AnyM<W,T> anyM) {
        return anyM instanceof AnyMValue ? Xor.secondary((AnyMValue<W,T>) anyM) : Xor.primary((AnyMSeq<W,T>) anyM);
    }






    /**
     * Pattern matching on the blocking / non-blocking nature of a Queue
     * 
     * <pre>
     * {@code 
     *  Xors.blocking(new ManyToManyConcurrentArrayQueue(10))
                  .visit(c->"blocking", c->"not")
         //"not"
    
   
       Xors.blocking(new LinkedBlockingQueue(10))
                 .visit(c->"blocking", c->"not")
        //"blocking
     * 
     * }
     * </pre>
     * 
     * @param queue Queue to pattern fold on
     * @return Pattern matchier on the blocking / non-blocking nature of the supplied Queue
     */
    public static <T> Xor<BlockingQueue<T>, java.util.Queue<T>> blocking(final java.util.Queue<T> queue) {

        return queue instanceof BlockingQueue ? Xor.<BlockingQueue<T>, java.util.Queue<T>> secondary((BlockingQueue) queue)
                : Xor.<BlockingQueue<T>, java.util.Queue<T>> primary(queue);
    }
}
