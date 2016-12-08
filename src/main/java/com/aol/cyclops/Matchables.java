package com.aol.cyclops;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.AsMatchable;
import com.aol.cyclops.control.Matchable.MTuple1;
import com.aol.cyclops.control.Matchable.MTuple2;
import com.aol.cyclops.control.Matchable.MTuple3;
import com.aol.cyclops.control.Matchable.MTuple4;
import com.aol.cyclops.control.Matchable.MTuple5;
import com.aol.cyclops.control.Matchable.MXor;
import com.aol.cyclops.control.Matchable.MatchableIterable;
import com.aol.cyclops.control.Matchable.MatchableObject;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.control.monads.transformers.CompletableFutureT;
import com.aol.cyclops.control.monads.transformers.EvalT;
import com.aol.cyclops.control.monads.transformers.FutureT;
import com.aol.cyclops.control.monads.transformers.FutureWT;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.control.monads.transformers.MaybeT;
import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.control.monads.transformers.ReaderT;
import com.aol.cyclops.control.monads.transformers.SetT;
import com.aol.cyclops.control.monads.transformers.StreamT;
import com.aol.cyclops.control.monads.transformers.StreamableT;
import com.aol.cyclops.control.monads.transformers.TryT;
import com.aol.cyclops.control.monads.transformers.XorT;
import com.aol.cyclops.control.monads.transformers.seq.CompletableFutureTSeq;
import com.aol.cyclops.control.monads.transformers.seq.EvalTSeq;
import com.aol.cyclops.control.monads.transformers.seq.MaybeTSeq;
import com.aol.cyclops.control.monads.transformers.seq.OptionalTSeq;
import com.aol.cyclops.control.monads.transformers.seq.ReaderTSeq;
import com.aol.cyclops.control.monads.transformers.seq.SetTSeq;
import com.aol.cyclops.control.monads.transformers.seq.StreamTSeq;
import com.aol.cyclops.control.monads.transformers.seq.StreamableTSeq;
import com.aol.cyclops.control.monads.transformers.seq.TryTSeq;
import com.aol.cyclops.control.monads.transformers.seq.XorTSeq;
import com.aol.cyclops.control.monads.transformers.values.CompletableFutureTValue;
import com.aol.cyclops.control.monads.transformers.values.EvalTValue;
import com.aol.cyclops.control.monads.transformers.values.FutureWTValue;
import com.aol.cyclops.control.monads.transformers.values.ListTValue;
import com.aol.cyclops.control.monads.transformers.values.MaybeTValue;
import com.aol.cyclops.control.monads.transformers.values.OptionalTValue;
import com.aol.cyclops.control.monads.transformers.values.ReaderTValue;
import com.aol.cyclops.control.monads.transformers.values.SetTValue;
import com.aol.cyclops.control.monads.transformers.values.StreamTValue;
import com.aol.cyclops.control.monads.transformers.values.StreamableTValue;
import com.aol.cyclops.control.monads.transformers.values.TryTValue;
import com.aol.cyclops.control.monads.transformers.values.XorTValue;
import com.aol.cyclops.data.async.Adapter;
import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.Topic;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Decomposable;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.stream.HeadAndTail;
import com.aol.cyclops.util.ExceptionSoftener;

/**
 * This class contains static methods for Structural Pattern matching
 * 
 * @author johnmcclean
 *
 */
public class Matchables {

   
    /**
     * Match on a single value 
     * 
     * <pre>
     * {@code 
     * import static com.aol.cyclops.util.function.Predicates.greaterThan;
     * import static com.aol.cyclops.control.Matchable.when;
     * 
     * Matchables.match(100)
                 .matches(c->c.is(when(greaterThan(50)), ()->"large"), ()->"small");
       
       //Eval["large"]
     * }
     * </pre>
     * @param v Value to match on
     * 
     * @return Structural Pattern matching API for that value (encompasing presence and absence / null)
     */
    public static <T1> MTuple1<T1> match(final T1 v) {
        return () -> Tuple.tuple(v);
    }

    /**
     * Match on a two values 
     * <pre>
     * {@code 
     * import static com.aol.cyclops.util.function.Predicates.greaterThan;
     * import static com.aol.cyclops.control.Matchable.when;
     * 
        Matchables.match2(100,2)
                  .matches(c->c.is(when(Predicates.greaterThan(50),Predicates.lessThan(10)), ()->"large and small"), 
                  ()->"not large and small");
       //Eval["large and small"]
     * }
     * </pre>
     * @param t1 1st Value to match on
     * @param t2 2nd Value to match on
     * @return  Structural Pattern matching API for those values
     */
    public static <T1, T2> MTuple2<T1, T2> match2(final T1 t1, final T2 t2) {
        return () -> Tuple.tuple(t1, t2);
    }

    /**
     * Match on three values 
     * <pre>
     * {@code 
     * 
     * Matchables.match3(100,2,1000)
                .matches(c->c.is(when(Predicates.greaterThan(50),Predicates.lessThan(10),Predicates.greaterThan(500)), ()->"large and small and huge"),
                         ()->"not large and small")
                         
        //"large and small and huge"
     * 
     * }
     * </pre>
     * 
     * @param t1 1st Value to match on
     * @param t2 2nd Value to match on
     * @param t3 3rd Value to match on
     * @return Structural Pattern matching API for those values
     */
    public static <T1, T2, T3> MTuple3<T1, T2, T3> match3(final T1 t1, final T2 t2, final T3 t3) {
        return () -> Tuple.tuple(t1, t2, t3);
    }

    /**
     * Match on four values
     * 
     * <pre>
     * {@code
     * 
     * Matchables.match4(100,2,1000,1)
                .matches(c->c.is(when(Predicates.greaterThan(50),
                                       Predicates.lessThan(10),
                                       Predicates.greaterThan(500),
                                       Predicates.lessThan(2)), ()->"large and small and huge and tiny"), ()->"not large and small").get();
       
       //"large and small and huge and tiny"
     * }
     * @param t1 1st Value to match on
     * @param t2 2nd Value to match on
     * @param t3 3rd Value to match on
     * @param t4 4th Value to match on
     * @return Structural Pattern matching API for those values
     */
    public static <T1, T2, T3, T4> MTuple4<T1, T2, T3, T4> match4(final T1 t1, final T2 t2, final T3 t3, final T4 t4) {
        return () -> Tuple.tuple(t1, t2, t3, t4);
    }

    /**
     *  Match on a Supplier 
     *  
     * <pre>
     * {@code
     * import static com.aol.cyclops.util.function.Predicates.greaterThan;
     * import static com.aol.cyclops.control.Matchable.when;
     * 
     * Matchables.supplier(() -> 100)
                                 .matches(c->c.is(when(greaterThan(50)), ()->"large"), ()->"small")
                                 .get()
                                  
      //large   
     * }
     * </pre>         
     *                 
     * @param s1 Supplier to generate a value to match on
     * @return  Structural Pattern matching API for Supplier (encompasing presence and absence / null)
     */
    public static <T1> MTuple1<T1> supplier(final Supplier<T1> s1) {

        return () -> Tuple.tuple(s1.get());
    }

    /**
     *  Match on a tuple
     *  
     * <pre>
     * {@code
     * import static com.aol.cyclops.util.function.Predicates.greaterThan;
     * import static com.aol.cyclops.control.Matchable.when;
     * 
     * Matchables.tuple1(Tuple.tuple(100))
                                 .matches(c->c.is(when(greaterThan(50)), ()->"large"), ()->"small")
                                 .get()
                                  
      //large   
     * }
     * </pre>  
     * 
     * @param t2 Tuple1 to match on
     * @return Structural Pattern matching API for Tuple
     */ 
    public static <T1> MTuple1<T1> tuple1(final Tuple1<T1> t2) {
        return () -> t2;
    }

    public static <T1, T2> MTuple2<T1, T2> supplier2(final Supplier<T1> s1, final Supplier<T2> s2) {
        return () -> Tuple.tuple(s1.get(), s2.get());
    }

    public static <T1, T2> MTuple2<T1, T2> tuple2(final Tuple2<T1, T2> t2) {
        return () -> t2;
    }

    public static <TYPE, T1 extends TYPE, T2 extends TYPE, T3 extends TYPE> MTuple3<T1, T2, T3> tuple3(final Tuple3<T1, T2, T3> t3) {
        return () -> t3;
    }

    public static <TYPE, T1 extends TYPE, T2 extends TYPE, T3 extends TYPE> MTuple3<T1, T2, T3> supplier3(final Supplier<T1> s1,
            final Supplier<T2> s2, final Supplier<T3> s3) {
        return () -> Tuple.tuple(s1.get(), s2.get(), s3.get());
    }

    public static <TYPE, T1 extends TYPE, T2 extends TYPE, T3 extends TYPE, T4 extends TYPE> MTuple4<T1, T2, T3, T4> tuple4(
            final Tuple4<T1, T2, T3, T4> t4) {
        return () -> t4;
    }

    public static <TYPE, T1 extends TYPE, T2 extends TYPE, T3 extends TYPE, T4 extends TYPE> MTuple4<T1, T2, T3, T4> supplier4(final Supplier<T1> s1,
            final Supplier<T2> s2, final Supplier<T3> s3, final Supplier<T4> s4) {
        return () -> Tuple.tuple(s1.get(), s2.get(), s3.get(), s4.get());
    }

    public static <TYPE, T1 extends TYPE, T2 extends TYPE, T3 extends TYPE, T4 extends TYPE, T5 extends TYPE> MTuple5<T1, T2, T3, T4, T5> tuple5(
            final Tuple5<T1, T2, T3, T4, T5> t5) {
        return () -> t5;
    }

    public static <TYPE, T1 extends TYPE, T2 extends TYPE, T3 extends TYPE, T4 extends TYPE, T5 extends TYPE> MTuple5<T1, T2, T3, T4, T5> supplier5(
            final Supplier<T1> s1, final Supplier<T2> s2, final Supplier<T3> s3, final Supplier<T4> s4, final Supplier<T5> s5) {
        return () -> Tuple.tuple(s1.get(), s2.get(), s3.get(), s4.get(), s5.get());
    }

    public static <T> MatchableIterable<T> iterable(final Iterable<T> o) {
        return () -> o;
    }

    /**
     * Create a new matchable that will match on the fields of the provided Decomposable
     * 
     * @param o Decomposable to match on it's fields
     * @return new Matchable
     */
    public static <T extends Decomposable> MatchableObject<T> decomposable(final Decomposable o) {
        return AsMatchable.asMatchable(o);
    }

    /**
     * Create a matchable that matches on the provided Objects
     * 
     * <pre>
     * {@code 
     * Eval<String> result = Matchables.listOfValues(1,2)
                                        .matches(c->c.has(when(1,3),then("2"))
                                                     .has(when(1,2),then("3")),otherwise("8"));
        
       //Eval["3"];
     * }
     * </pre>
     * 
     * 
     * @param o Objects to match on
     * @return new Matchable
     */
    public static <T> MatchableObject<T> listOfValues(final T... o) {
        return AsMatchable.asMatchable(Arrays.asList(o));
    }

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
    public static <T> MXor<Queue<T>, Topic<T>> adapter(final Adapter<T> adapter) {
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
     */
    public static <T1> MXor<T1, Throwable> future(final CompletableFuture<T1> future) {
        return () -> FutureW.of(future)
                            .toXor()
                            .swap();
    }

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
     */
    public static <T1> MXor<T1, Throwable> future(final FutureW<T1> future) {
        return () -> future.toXor()
                           .swap();
    }

    public static <T1, X extends Throwable> MXor<T1, X> tryMatch(final Try<T1, X> match) {
        return () -> match.toXor()
                          .swap();
    }

    public static <T> Matchable.MatchableOptional<T> maybe(final Maybe<T> opt) {
        return opt;
    }

    public static <T> Matchable.MatchableOptional<T> maybe(final Value<T> opt) {
        return () -> opt.toOptional();
    }

    public static <T> Matchable.MatchableOptional<T> optional(final Optional<T> opt) {
        return Maybe.fromOptional(opt);
    }

    public static <W extends WitnessType,T> MXor<AnyMValue<W,T>, AnyMSeq<W,T>> anyM(final AnyM<W,T> anyM) {
        return () -> anyM instanceof AnyMValue ? Xor.secondary((AnyMValue<W,T>) anyM) : Xor.primary((AnyMSeq<W,T>) anyM);
    }

    public static <T> MXor<MaybeTValue<T>, MaybeTSeq<T>> maybeT(final MaybeT<T> transformer) {
        return () -> transformer instanceof MaybeTValue ? Xor.secondary((MaybeTValue<T>) transformer) : Xor.primary((MaybeTSeq<T>) transformer);
    }

    public static <T> MXor<OptionalTValue<T>, OptionalTSeq<T>> optionalT(final OptionalT<T> transformer) {
        return () -> transformer instanceof OptionalTValue ? Xor.secondary((OptionalTValue<T>) transformer)
                : Xor.primary((OptionalTSeq<T>) transformer);
    }

    public static <T> MXor<EvalTValue<T>, EvalTSeq<T>> evalT(final EvalT<T> transformer) {
        return () -> transformer instanceof EvalTValue ? Xor.secondary((EvalTValue<T>) transformer) : Xor.primary((EvalTSeq<T>) transformer);
    }

    public static <ST, T> MXor<XorTValue<ST, T>, XorTSeq<ST, T>> xorT(final XorT<ST, T> transformer) {
        return () -> transformer instanceof XorTValue ? Xor.secondary((XorTValue<ST, T>) transformer) : Xor.primary((XorTSeq<ST, T>) transformer);
    }

    public static <T, X extends Throwable> MXor<TryTValue<T, X>, TryTSeq<T, X>> tryT(final TryT<T, X> transformer) {
        return () -> transformer instanceof TryTValue ? Xor.secondary((TryTValue<T, X>) transformer) : Xor.primary((TryTSeq<T, X>) transformer);
    }

    public static <T> MXor<FutureWTValue<T>, FutureT<T>> futureWT(final FutureWT<T> transformer) {
        return () -> transformer instanceof FutureWTValue ? Xor.secondary((FutureWTValue<T>) transformer) : Xor.primary((FutureT<T>) transformer);
    }

    public static <T> MXor<CompletableFutureTValue<T>, CompletableFutureTSeq<T>> completableFutureT(final CompletableFutureT<T> transformer) {
        return () -> transformer instanceof CompletableFutureTValue ? Xor.secondary((CompletableFutureTValue<T>) transformer)
                : Xor.primary((CompletableFutureTSeq<T>) transformer);
    }

    public static <T> MXor<ListTValue<T>, ListT<T>> listT(final ListT<T> transformer) {
        return () -> transformer instanceof ListTValue ? Xor.secondary((ListTValue<T>) transformer) : Xor.primary((ListT<T>) transformer);
    }

    public static <T> MXor<SetTValue<T>, SetTSeq<T>> setT(final SetT<T> transformer) {
        return () -> transformer instanceof SetTValue ? Xor.secondary((SetTValue<T>) transformer) : Xor.primary((SetTSeq<T>) transformer);
    }

    public static <T> MXor<StreamTValue<T>, StreamTSeq<T>> streamT(final StreamT<T> transformer) {
        return () -> transformer instanceof StreamTValue ? Xor.secondary((StreamTValue<T>) transformer) : Xor.primary((StreamTSeq<T>) transformer);
    }

    public static <T> MXor<StreamableTValue<T>, StreamableTSeq<T>> streamableT(final StreamableT<T> transformer) {
        return () -> transformer instanceof StreamableTValue ? Xor.secondary((StreamableTValue<T>) transformer)
                : Xor.primary((StreamableTSeq<T>) transformer);
    }

    public static <T, R> MXor<ReaderTValue<T, R>, ReaderTSeq<T, R>> readerT(final ReaderT<T, R> transformer) {
        return () -> transformer instanceof ReaderTValue ? Xor.secondary((ReaderTValue<T, R>) transformer)
                : Xor.primary((ReaderTSeq<T, R>) transformer);
    }

    public static <X extends Throwable> MTuple4<Class, String, Throwable, MatchableIterable<StackTraceElement>> throwable(final X t) {
        return supplier4(() -> t.getClass(), () -> t.getMessage(), () -> t.getCause(), () -> iterable(Arrays.asList(t.getStackTrace())));
    }

    /**
     * Break an URL down into
     * protocol, host, port, path, query
     * 
     * <pre>
     * {@code 
     * Eval<String> url = Matchables.url(new URL("http://www.aol.com/path?q=hello"))
                                     .on$12_45()
                                     .matches(c->c.is(when("http","www.aol.com","/path","q=hello"), then("correct")),otherwise("miss"));
        
        //Eval["correct"]
     * 
     * }
     * </pre>
     * 
     * @param url URL to pattern match on
     * @return Pattern Matcher for URLs
     */
    public static MTuple5<String, String, Integer, String, String> url(final URL url) {
        return supplier5(() -> url.getProtocol(), () -> url.getHost(), () -> url.getPort(), () -> url.getPath(), () -> url.getQuery());
    }

    /**
     * Pattern match on the contents of a BufferedReader
     * <pre>
     * {@code 
     *  String result = Matchables.lines(new BufferedReader(new FileReader(new File(file))))
                                  .on$12___()
                                  .matches(c->c.is(when("hello","world"),then("correct")), otherwise("miss")).get();
        
        //"correct"
     * 
     * }
     * </pre>
     * 
     * 
     * @param in BufferedReader to match on
     * @return
     */
    public static Matchable.AutoCloseableMatchableIterable<String> lines(final BufferedReader in) {

        return new Matchable.AutoCloseableMatchableIterable<>(
                                                              in, () -> in.lines()
                                                                          .iterator());
    }

    public static Matchable.AutoCloseableMatchableIterable<String> lines(final URL url) {

        final BufferedReader in = ExceptionSoftener.softenSupplier(() -> new BufferedReader(
                                                                                            new InputStreamReader(
                                                                                                                  url.openStream())))
                                                   .get();
        return new Matchable.AutoCloseableMatchableIterable<>(
                                                              in, () -> in.lines()
                                                                          .iterator());
    }

    /**
     * Pattern match on the contents of a File
     * <pre>
     * {@code 
     * String result = Matchables.lines(new File(file))
                                 .on$12___()
                                 .matches(c->c.is(when("hello","world"),then("correct")), otherwise("miss")).get();
                                  
       }
       </pre>
     * 
     * @param f File to match against
     * @return Matcher
     */
    public static Matchable.AutoCloseableMatchableIterable<String> lines(final File f) {
        final Stream<String> stream = ExceptionSoftener.softenSupplier(() -> Files.lines(Paths.get(f.getAbsolutePath())))
                                                       .get();
        return new Matchable.AutoCloseableMatchableIterable<>(
                                                              stream, () -> stream.iterator());
    }

    /**
     * Pattern match on the words (space separated) in a character sequence
     * <pre>
     * {@code 
     *  Matchables.words("hello world")
                                  .matches(c->c.has(when("hello","world"), then("correct")), otherwise("miss"))
                                  .get();
     *  //"correct
     * }</pre>
     * 
     * 
     * 
     * @param seq Character Sequence to match against
     * @return Pattern matching for words based on input sequence
     */
    public static MatchableIterable<String> words(final CharSequence seq) {
        return iterable(Arrays.asList(seq.toString()
                                         .split(" ")));
    }
    /**
     * Pattern match on the words  in a character sequence
     * <pre>
     * {@code 
     *  Matchables.words("hello,world",",")
                                  .matches(c->c.has(when("hello","world","boo!"), then("incorrect")), otherwise("miss"))
                                  .get();
     *  //"miss"
     * }</pre>
     * 
     * 
     * 
     * @param seq Character Sequence to match against
     * @param separator Word separator
     * @return Pattern matching for words based on input sequence
     */
    public static MatchableIterable<String> words(final CharSequence seq, String separator) {
        return iterable(Arrays.asList(seq.toString()
                                         .split(separator)));
    }

    /**
     * Pattern match on the characters in a character sequence
     * 
     * <pre>
     * {@code 
     *  String result =   Matchables.chars("hello,world")
                                    .matches(c->c.has(when('h','e','l','l','o'), then("startsWith")), otherwise("miss"))
                                    .get();
      
       //"startsWith"
     * 
     * }
     * </pre>
     * 
     * 
     * @param chars Characters to match on
     * @return MatchableIterable for pattern matching on the characters in the sequence
     */
    public static MatchableIterable<Character> chars(final CharSequence chars) {
        final Iterable<Character> it = () -> chars.chars()
                                                  .boxed()
                                                  .map(i -> Character.toChars(i)[0])
                                                  .iterator();
        return () -> it;
    }

    /**
     * Pattern match on an Xor type
     * 
     * <pre>
     * {@code 
     * 
     *  Xor<Exception, String> xor = Xor.primary("hello world");

        Eval<String> result = Matchables.xor(xor)
                                        .matches(c -> c.is(when(instanceOf(RuntimeException.class)), () -> "runtime"),
                                                 c -> c.is(when(equal("hello world")), () -> "hello back"), () -> "unknown");

       //Eval["hello back"]
     * 
     * }</pre>
     * 
     * @param xor Xor to match on
     * @return Pattern matcher for Xor
     */
    public static <ST, PT> MXor<ST, PT> xor(final Xor<ST, PT> xor) {
        return () -> xor;
    }

    public static <T> MTuple2<Maybe<T>, ListX<T>> headAndTail(final Collection<T> col) {
        final HeadAndTail<T> ht = CollectionX.fromCollection(col)
                                             .headAndTail();
        return supplier2(() -> ht.headMaybe(), () -> ht.tail()
                                                       .toListX());
    }

    public static <K, V> ReactiveSeq<MTuple2<K, V>> keysAndValues(final Map<K, V> map) {
        return ReactiveSeq.fromIterable(map.entrySet())
                          .map(entry -> supplier2(() -> entry.getKey(), () -> entry.getValue()));
    }

    public static MTuple3<Integer, Integer, Integer> dateDDMMYYYY(final Date date) {
        final Date input = new Date();
        final LocalDate local = input.toInstant()
                                     .atZone(ZoneId.systemDefault())
                                     .toLocalDate();
        return localDateDDMMYYYY(local);
    }

    public static MTuple3<Integer, Integer, Integer> dateMMDDYYYY(final Date date) {
        final Date input = new Date();
        final LocalDate local = input.toInstant()
                                     .atZone(ZoneId.systemDefault())
                                     .toLocalDate();
        return localDateMMDDYYYY(local);
    }

    public static MTuple3<Integer, Integer, Integer> localDateDDMMYYYY(final LocalDate date) {
        return supplier3(() -> date.getDayOfMonth(), () -> date.getMonth()
                                                               .getValue(),
                         () -> date.getYear());
    }

    public static MTuple3<Integer, Integer, Integer> localDateMMDDYYYY(final LocalDate date) {
        return supplier3(() -> date.getMonth()
                                   .getValue(),
                         () -> date.getDayOfMonth(), () -> date.getYear());
    }

    /**
     * Structural pattern matching on a Date's hour minutes and seconds component
     * 
     * @param date Date to match on
     * @return Structural pattern matcher for hours / minutes / seconds
     */
    public static MTuple3<Integer, Integer, Integer> dateHMS(final Date date) {
        final Date input = new Date();
        final LocalTime local = input.toInstant()
                                     .atZone(ZoneId.systemDefault())
                                     .toLocalTime();
        return localTimeHMS(local);
    }

    public static MTuple3<Integer, Integer, Integer> localTimeHMS(final LocalTime time) {
        return supplier3(() -> time.getHour(), () -> time.getMinute(), () -> time.getSecond());
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
    public static <T> MXor<BlockingQueue<T>, java.util.Queue<T>> blocking(final java.util.Queue<T> queue) {

        return () -> queue instanceof BlockingQueue ? Xor.<BlockingQueue<T>, java.util.Queue<T>> secondary((BlockingQueue) queue)
                : Xor.<BlockingQueue<T>, java.util.Queue<T>> primary(queue);
    }
}
