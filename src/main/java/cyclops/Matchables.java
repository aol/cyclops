package cyclops;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.async.Adapter;
import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.Topic;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.anyM.WitnessType;

import java.util.concurrent.BlockingQueue;

/**
 * This class contains static methods for Structural Pattern matching
 * 
 * @author johnmcclean
 *
 */
public class Matchables {

   


    /**
     * Create a Pattern Matcher on cyclops-react adapter type (note this will only match
     * on known types within the cyclops-react library)
     * 
     * <pre>
     * {@code 
     *     Adapter<Integer> adapter = QueueFactories.<Integer>unboundedQueue()
     *                                              .build();
     *                                              
     *     String result =   Matchables.adapter(adapter)
                                       .visit(queue->"we have a queue",topic->"we have a topic");
     *                      
     *    //"we have a queue"                                                  
     * } 
     * </pre>
     * 
     * @param adapter Adapter to match on
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
     *  Eval<Integer> result = Matchables.future(CompletableFuture.completedFuture(10))
                                         .matches(c-> 
                                                     c.is( when(some(10)), then(20)),  //success
                                                      
                                                     c->c.is(when(instanceOf(RuntimeException.class)), then(2)), //failure
                                                      
                                                     otherwise(3) //no match
                                                 );
        
        //Eval[20]
     * 
     * }</pre>
     * 
     * 
     * @param future Future to match on
     * @return Pattern Matcher for CompletableFutures
      USE EITHER HERE
    public static <T1> Xor<T1, Throwable> future(final CompletableFuture<T1> future) {
        return () -> FutureW.of(future)
                            .toXor()
                            .swap();
    }*/

    /**
     * Pattern Match on a FutureW handling success and failure cases differently
     * <pre>
     * {@code 
     *  Eval<Integer> result = Matchables.future(FutureW.ofResult(1))
                                         .matches(c-> c.is( when(some(1)), then(10)), 
                                                  c->c.is(when(instanceOf(RuntimeException.class)), then(2)),
                                                  otherwise(3));
        
        //Eval.now[10]
     * 
     *  Eval<Integer> result = Matchables.future(FutureW.ofError(new RuntimeException()))
                                         .matches(c-> c.is( when(some(10)), then(2)), 
                                                  c->c.is(when(instanceOf(RuntimeException.class)), then(2)),
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
     * @param future Future to match on
     * @return Pattern Matcher for Futures

    public static <T1> MXor<T1, Throwable> future(final FutureW<T1> future) {
        return () -> future.toXor()
                           .swap();
    }
USE EITHER
    public static <T1, X extends Throwable> MXor<T1, X> tryMatch(final Try<T1, X> match) {
        return () -> match.toXor()
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
     *  Matchables.blocking(new ManyToManyConcurrentArrayQueue(10))
                  .visit(c->"blocking", c->"not")
         //"not"
    
   
       Matchables.blocking(new LinkedBlockingQueue(10))
                 .visit(c->"blocking", c->"not")
        //"blocking
     * 
     * }
     * </pre>
     * 
     * @param queue Queue to pattern match on
     * @return Pattern matchier on the blocking / non-blocking nature of the supplied Queue
     */
    public static <T> Xor<BlockingQueue<T>, java.util.Queue<T>> blocking(final java.util.Queue<T> queue) {

        return queue instanceof BlockingQueue ? Xor.<BlockingQueue<T>, java.util.Queue<T>> secondary((BlockingQueue) queue)
                : Xor.<BlockingQueue<T>, java.util.Queue<T>> primary(queue);
    }
}
