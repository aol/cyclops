package cyclops.stream;

import java.util.Objects;
import java.util.stream.Stream;

import cyclops.async.LazyReact;
import cyclops.async.Adapter;
import cyclops.async.Queue;
import cyclops.async.QueueFactories;
import cyclops.async.QueueFactory;
import com.aol.cyclops2.util.stream.pushable.MultipleStreamSource;
import com.aol.cyclops2.util.stream.pushable.PushableLazyFutureStream;
import com.aol.cyclops2.util.stream.pushable.PushableReactiveSeq;
import com.aol.cyclops2.util.stream.pushable.PushableStream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Create Java 8 Streams that data can be pushed into
 * 
 * Pushing data into a Java 8 Stream
 * 
 * <pre>
 * {@code 
 * PushableStream<Integer> pushable = StreamSource.ofUnbounded()
                                                        .reactiveStream();
   pushable.getInput()
           .offer(10);
    
    Stream<Integer> reactiveStream = pushable.getStream();
    reactiveStream.forEach(System.out::println);
    
    //print 10
     
    pushable.getInput()
           .offer(20);
           
    //print 20       
       
                
    pushable.getInput()
            .close();            
 * 
 * 
 * }
 * </pre>
 * 
 * 
 * 
 * Pushing data into a FutureStream
 * 
 * <pre>
 * {@code 
 * PushableLazyFutureStream<Integer> pushable = StreamSource.ofUnbounded()
                                                            .futureStream(new LazyReact());
   pushable.getInput()
           .offer(100);
   
   //on another thread
   pushable.getStream()
           .forEach(this:process);
               
                
   //close input into Stream -         
   pushable.getInput().close();             
 * 
 * 
 * }
 * </pre>
 * 
 * Multiple Streams reading the same data across threads
 * 
 * <pre>
 * {@code 
 *   MultipleStreamSource<Integer> multi = StreamSource.ofMultiple();
     multi.getInput()
          .offer(100);
        
    //example on separate threads
    
     //thread 1
    LazyFutureStream<Integer> futureStream = multi.futureStream(new LazyReact());
    futureStream.forEach(System.out::println);
    
    //print 100
    
    //thread 2
    ReactiveSeq<Integer> seq = multi.reactiveSeq();
    seq.forEach(System.out::println);
   
    //print 100
     
    //thread 3
    Stream<Integer> reactiveStream = multi.reactiveStream();
    reactiveStream.forEach(System.out::println);
   
    //print 100    
       
    multi.getInput()
         .offer(200);  
         
    //thread 1 
    //print 200  
    
    //thread 2 
    //print 200 
    
    //thread 3 
    //print 200 
     
    multi.getInput()
         .close();
        
        
 * 
 * 
 * }
 * </pre>
 * 
 * @author johnmcclean
 *
 */

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamSource {

    private final int backPressureAfter;
    private final boolean backPressureOn;

    /**
     * Connect multiple Streams to a Pushable datasource, each Stream will recieve the same
     * data.
     * <pre>
     * {@code 
     * 
 
     *   MultipleStreamSource<Integer> multi = StreamSource.ofMultiple();
         multi.getInput()
              .offer(100);
        
        //example on separate threads
    
         //thread 1
        LazyFutureStream<Integer> futureStream = multi.futureStream(new LazyReact());
        futureStream.forEach(System.out::println);
        
        //print 100
        
        //thread 2
        ReactiveSeq<Integer> seq = multi.reactiveSeq();
        seq.forEach(System.out::println);
       
        //print 100
         
        //thread 3
        Stream<Integer> reactiveStream = multi.reactiveStream();
        reactiveStream.forEach(System.out::println);
       
        //print 100    
           
        multi.getInput()
             .offer(200);  
             
        //thread 1 
        //print 200  
        
        //thread 2 
        //print 200 
        
        //thread 3 
        //print 200 
         
        multi.getInput()
             .close();

     * 
     * }
     * </pre>
     * 
     * @return a builder that will use Topics to allow multiple Streams from the same data
     */
    public static <T> MultipleStreamSource<T> ofMultiple() {
        return new MultipleStreamSource<T>(
                                           StreamSource.ofUnbounded()
                                                       .createQueue());
    }

    /**
     * Connect multiple Streams to a Pushable datasource, each Stream will recieve the same
     * data. In this backpresure is applied by using a LinkedBlockingQueue. @see cyclops2.reactiveStream.StreamSource#ofMultiple(QueueFactory)
     * For more granular management of Adapter based backpressure. Adapters can be backed by non-blocking data structures and different backpressure strategies applied
     * <pre>
     * {@code 
     * 
 
     *   MultipleStreamSource<Integer> multi = StreamSource.ofMultiple(2);
         multi.getInput()
              .offer(100);
        
        //example on separate threads
    
         //thread 1
        LazyFutureStream<Integer> futureStream = multi.futureStream(new LazyReact());
        futureStream.map(this::slowProcess)
                    .forEach(System.out::println);
        
        //print 100
        
        //thread 2
        ReactiveSeq<Integer> seq = multi.reactiveSeq();
        seq.forEach(System.out::println);
       
        //print 100
         
        //thread 3
        Stream<Integer> reactiveStream = multi.reactiveStream();
        reactiveStream.forEach(System.out::println);
       
        //print 100    
           
        multi.getInput()
             .offer(200);  
             
        //thread 1 
        //print 200  
        
        //thread 2 
        //print 200 
        
        //thread 3 
        //print 200 
         
        multi.getInput()
             .offer(300);
        multi.getInput()
             .offer(400); 
        multi.getInput()
             .offer(500);    //blocked as backpressure applied        
        multi.getInput()
             .close();

     * 
     * }
     * </pre>
     * @param backPressureAfter Excess number of emitted records over consumed (by all connected Streams
     * after which backPressure will be applied).
     * @return a builder that will use Topics to allow multiple Streams from the same data
     */
    public static <T> MultipleStreamSource<T> ofMultiple(final int backPressureAfter) {
        return new MultipleStreamSource<T>(
                                           StreamSource.of(backPressureAfter)
                                                       .createQueue());
    }

    /**
     * Construct a StreamSource that supports multiple readers of the same data backed by a Queue created
     * from the supplied QueueFactory
     * 
     * 
     * @see QueueFactories for Factory creation options and various backpressure strategies
     * <pre>
     * {@code 
     *  MultipleStreamSource<Integer> multi = StreamSource
                                                .ofMultiple(QueueFactories.boundedQueue(100));
        LazyFutureStream<Integer> pushable = multi.futureStream(new LazyReact());
        ReactiveSeq<Integer> seq = multi.reactiveSeq();
        multi.getInput().offer(100);
        multi.getInput().close();
        pushable.collect(Collectors.toList()); //[100]
        seq.collect(Collectors.toList()); //[100]     
     * 
     * }
     * </pre>
     * 
     * @param q QueueFactory used to create the Adapter to back the pushable StreamSource
     * @return a builder that will use Topics to allow multiple Streams from the same data
     */
    public static <T> MultipleStreamSource<T> ofMultiple(final QueueFactory<?> q) {
        Objects.requireNonNull(q);
        return new MultipleStreamSource<T>(
                                           StreamSource.of(q)
                                                       .createQueue());
    }

    /**
     * Construct a Pushable StreamSource using the provided QueueFactory as a push mechanism
     * @see QueueFactories for Factory creation options and various backpressure strategies
     * 
     * <pre>
     * {@code 
     * PushableStream<Integer> pushable = StreamSource.of(QueueFactories.boundedQueue(10))
                                                        .reactiveStream();
       pushable.getInput()
               .offer(10);
    
        Stream<Integer> reactiveStream = pushable.getStream();
        reactiveStream.forEach(System.out::println);
    
        //print 10
     
        pushable.getInput()
               .offer(20);
           
        //print 20       
       
                
        pushable.getInput()
                .close();            
        }
     * </pre> 
     * 
     * 
     * 
     * @param q QueueFactory used to create the Adapter to back the pushable StreamSource
     * @return Pushable StreamSource
     */
    public static StreamSource of(final QueueFactory<?> q) {
        Objects.requireNonNull(q);
        return new StreamSource() {
            @SuppressWarnings("unchecked")
            @Override
            <T> Queue<T> createQueue() {
                return (Queue<T>) q.build();

            }
        };
    }

    /**
     * Construct a Pushable StreamSource with no max size. Warning if data producers pushing data to this StreamSource
     * are faster than Data consumers the JVM will eventually run out of memory.
     * <pre>
     * {@code 
     * PushableStream<Integer> pushable = StreamSource.ofUnbounded()
                                                      .reactiveStream();
       pushable.getInput()
               .offer(10);
    
        Stream<Integer> reactiveStream = pushable.getStream();
        reactiveStream.forEach(System.out::println);
    
        //print 10
     
        pushable.getInput()
               .offer(20);
           
        //print 20       
       
                
        pushable.getInput()
                .close();            
        }
     * </pre>
     * 
     * @return Pushable StreamSource
     */
    public static StreamSource ofUnbounded() {
        return new StreamSource();
    }
    /**
     * A builder for pushable Streams that apply backpressure if producing Streams exceed the capacity of consuming Streams.
     * 
     * In this backpresure is applied by using a LinkedBlockingQueue. @see cyclops2.reactiveStream.StreamSource#ofMultiple(QueueFactory)
     * For more granular management of Adapter based backpressure. Adapters can be backed by non-blocking data structures and different backpressure strategies applied
    
       <pre>
       {@code 
           StreamSource source = StreamSource.of(10);
           
           pushable.getInput()
               .offer(10);
           
           //on a separate thread
           source.reactiveSeq()
                 .forEach(System.out::println);
       
       }
       </pre>
    
     * @param backPressureAfter Excess number of emitted records over consumed (by all connected Streams
     * after which backPressure will be applied).
     * @return A builder for Pushable Streams
     */
    public static StreamSource of(final int backPressureAfter) {
        if (backPressureAfter < 1)
            throw new IllegalArgumentException(
                                               "Can't apply back pressure after less than 1 event");
        return new StreamSource(
                                backPressureAfter, true);
    }

    <T> Queue<T> createQueue() {

        Queue q;
        if (!backPressureOn)
            q = QueueFactories.unboundedNonBlockingQueue()
                              .build();
        else
            q = QueueFactories.boundedQueue(backPressureAfter)
                              .build();
        return q;
    }

    private StreamSource() {

        backPressureAfter = Runtime.getRuntime()
                                   .availableProcessors();
        backPressureOn = false;
    }

    /**
     * Create a pushable LazyFutureStream using the supplied ReactPool
     * 
     * <pre>
     * {@code 
     * 
     *  PushableLazyFutureStream<Integer> pushable = StreamSource.ofUnbounded()
                                                                 .futureStream(new LazyReact());
        pushable.getInput().add(100);
        pushable.getInput().close();
        
        
        assertThat(pushable.getStream().collect(Collectors.toList()),
                hasItem(100));
     * 
     * 
     * }</pre>
     * 
     * 
     * 
     * @param s ReactPool to use to create the Stream
     * @return a Tuple2 with a Queue&lt;T&gt; and LazyFutureStream&lt;T&gt; - add data to the Queue
     * to push it to the Stream
     */
    public <T> PushableLazyFutureStream<T> futureStream(final LazyReact s) {

        final Queue<T> q = createQueue();
        return new PushableLazyFutureStream<T>(
                                               q, s.fromStream(q.stream()));

    }

    /**
     * Create a FutureStream. his will call FutureStream#futureStream(Stream) which creates
     * a sequential LazyFutureStream
     *
     * <pre>
     * {@code 
     * 
     *  PushableLazyFutureStream<Integer> pushable = StreamSource.futureStream(QueueFactories.boundedNonBlockingQueue(1000),new LazyReact());
        pushable.getInput().add(100);
        pushable.getInput().close();
        
        
        assertThat(pushable.getStream().collect(Collectors.toList()),
                hasItem(100));
     * 
     * 
     * }</pre> 
     * 
     * 
     * @param adapter Adapter to create a LazyFutureStream from
     * @return A LazyFutureStream that will accept values from the supplied adapter
     */
    public static <T> FutureStream<T> futureStream(final Adapter<T> adapter, final LazyReact react) {


        return react.fromStream(adapter.stream());
    }

    /**
     * Create a pushable JDK 8 Stream
     * 
     * <pre>
     * {@code 
     * PushableStream<Integer> pushable = StreamSource.ofUnbounded()
                                                        .reactiveStream();
        pushable.getInput()
                .add(10);
        pushable.getInput()
                .close();
        
        pushable.getStream().collect(Collectors.toList()) //[10]
               
     * 
     * }
     * </pre>
     * 
     * @return PushableStream that can accept data to push into a Java 8 Stream
     * to push it to the Stream
     */
    public <T> PushableStream<T> stream() {
        final Queue<T> q = createQueue();
        return new PushableStream<T>(
                                     q, q.jdkStream());

    }

    /**
     * Create a pushable {@link PushableReactiveSeq}
     * 
     * <pre>
     * {@code 
     *  PushableReactiveSeq<Integer> pushable = StreamSource.ofUnbounded()
                                                            .reactiveSeq();
        pushable.getInput()
                .add(10);
        
        //on another thread
        pushable.getStream()
                .collect(Collectors.toList()) //[10]
              
     * 
     * }
     * </pre>
     * 
     * 
     * @return PushableStream that can accept data to push into a {@see cyclops2.reactiveStream.ReactiveSeq}
     * to push it to the Stream
     */
    public <T> PushableReactiveSeq<T> reactiveSeq() {
        final Queue<T> q = createQueue();
        return new PushableReactiveSeq<T>(
                                          q, q.stream());
    }

    /**
     * Create a JDK 8 Stream from the supplied Adapter
     * 
     * <pre>
     * {@code 
     *   Queue<Integer> q = QueueFactories.boundedNonBlockingQueue(1000);
     *   Stream<Integer> reactiveStream = StreamSource.reactiveStream(q);
     *   reactiveStream.forEach(System.out::println);
     *   
     *   //on a separate thread
     *   q.offer(10);
     * }
     * </pre>
     * @param adapter Adapter to create a Steam from
     * @return Stream that will accept input from supplied adapter
     */
    public static <T> Stream<T> stream(final Adapter<T> adapter) {

        return adapter.stream();
    }

    /**
     * Create a pushable {@link ReactiveSeq}
     * 
     * <pre>
     * {@code 
     *  Signal<Integer> signal = Signal.queueBackedSignal();
        ReactiveSeq<Integer> pushable = StreamSource.reactiveSeq(signal
                                                    .getDiscrete());
        signal.set(100);
        signal.close();
        
        assertThat(pushable.collect(Collectors.toList()), hasItem(100));
     * }
     * </pre>
     * 
     * @param adapter Adapter to create a Seq from
     * @return A Seq that will accept input from a supplied adapter
     */
    public static <T> ReactiveSeq<T> reactiveSeq(final Adapter<T> adapter) {

        return adapter.stream();
    }

}
