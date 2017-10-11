package cyclops.control.lazy;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.Value;
import cyclops.control.Xor;
import cyclops.function.Function0;
import cyclops.function.Function3;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;
import cyclops.collections.tuple.Tuple3;
import cyclops.stream.ReactiveSeq;

import static cyclops.collections.tuple.Tuple.tuple;

/**
 * simple Trampoline implementation : inspired by excellent TotallyLazy Java 8 impl 
 * and Mario Fusco presentation
 * 
 * Allows Stack Free Recursion
 * 
 * <pre>
 * {@code 
 * @Test
    public void trampolineTest(){
        
        assertThat(loop(500000,10).result(),equalTo(446198426));
        
    }
    Trampoline<Integer> loop(int times,int sum){
        
        if(times==0)
            return Trampoline.done(sum);
        else
            return Trampoline.more(()->loop(times-1,sum+times));
    }
 * 
 * }
 * </pre>
 * 
 * And co-routines can be implemented simply via zipping trampolines
 * 
    <pre>
 {@code
 Trampoline<Integer> looping = loop(500000,5);
 Trampoline<Integer> looping2 = loop2(500000,5);
 System.out.println(looping.zip(looping2).get());

 }
    </pre>

 Where loop and loop2 are implemented recursively using Trampoline with additional print logic


 <pre>
 {@code
 Trampoline<Integer> loop(int times,int sum){
    System.out.println("Loop-A " + times + " : " + sum);
    if(times==0)
         return Trampoline.done(sum);
    else
        return Trampoline.more(()->loop(times-1,sum+times));
    }
 }

 </pre>

 Results in interleaved execution visible from the console
 <pre>
...
 Loop-B 21414 : 216908016
 Loop-A 21413 : 216929430
 Loop-B 21413 : 216929430
 Loop-A 21412 : 216950843
...

 </pre>
 * 
 * @author johnmcclean
 *
 * @param <T> Return type
 */
@FunctionalInterface
public interface Trampoline<T> extends Value<T>, Function0<T>,To<Trampoline<T>> {

    default <R> R visit(Function<? super Trampoline<T>,? extends R> more, Function<? super T, ? extends R> done){
        return complete() ? done.apply(get()) : more.apply(this.bounce());
    }

    default  <B> Trampoline<Tuple2<T,B>> zip(Trampoline<B> b){
        return zip(b,(x,y)->Tuple.tuple(x,y));

    }
    default  <B,R> Trampoline<R> zip(Trampoline<B> b,BiFunction<? super T,? super B,? extends R> zipper){

        Xor<Trampoline<T>,T> first = resume();
        Xor<Trampoline<B>,B> second = b.resume();

        if(first.isSecondary() && second.isSecondary()) {
            return Trampoline.more(()->first.secondaryOrElse(null).zip(second.secondaryOrElse(null),zipper));
        }
        if(first.isPrimary() && second.isPrimary()){
            return Trampoline.done(zipper.apply(first.orElse(null),second.orElse(null)));
        }
        if(first.isSecondary() && second.isPrimary()){
            return Trampoline.more(()->first.secondaryOrElse(null).zip(b,zipper));
        }
        if(first.isPrimary() && second.isSecondary()){
            return Trampoline.more(()->this.zip(second.secondaryOrElse(null),zipper));
        }
        //unreachable
        return null;

    }
    default  <B,C> Trampoline<Tuple3<T,B,C>> zip(Trampoline<B> b, Trampoline<C> c){
        return zip(b,c,(x,y,z)->Tuple.tuple(x,y,z));

    }
    default  <B,C,R> Trampoline<R> zip(Trampoline<B> b, Trampoline<C> c, Function3<? super T, ? super B, ? super C,? extends R> fn){

        Xor<Trampoline<T>,T> first = resume();
        Xor<Trampoline<B>,B> second = b.resume();
        Xor<Trampoline<C>,C> third = c.resume();

        if(first.isSecondary() && second.isSecondary() && third.isSecondary()) {
            return Trampoline.more(()->first.secondaryOrElse(null).zip(second.secondaryOrElse(null),third.secondaryOrElse(null),fn));
        }
        if(first.isPrimary() && second.isPrimary() && third.isPrimary()){
            return Trampoline.done(fn.apply(first.orElse(null),second.orElse(null),third.orElse(null)));
        }

        if(first.isSecondary() && second.isPrimary() && third.isPrimary()){
            return Trampoline.more(()->first.secondaryOrElse(null).zip(b,c,fn));
        }
        if(first.isPrimary() && second.isSecondary() && third.isPrimary()){
            return Trampoline.more(()->this.zip(second.secondaryOrElse(null),c,fn));
        }
        if(first.isPrimary() && second.isPrimary() && third.isSecondary()){
            return Trampoline.more(()->this.zip(b,third.secondaryOrElse(null),fn));
        }


        if(first.isPrimary() && second.isSecondary() && third.isSecondary()){
            return Trampoline.more(()->this.zip(second.secondaryOrElse(null),third.secondaryOrElse(null),fn));
        }
        if(first.isSecondary() && second.isPrimary() && third.isSecondary()){
            return Trampoline.more(()->first.secondaryOrElse(null).zip(b,third.secondaryOrElse(null),fn));
        }
        if(first.isSecondary() && second.isSecondary() && third.isPrimary()){
            return Trampoline.more(()->first.secondaryOrElse(null).zip(second.secondaryOrElse(null),c,fn));
        }
        //unreachable
        return null;
    }

    default Xor<Trampoline<T>,T> resume(){
        return this.visit(Xor::secondary,Xor::primary);
    }



    /**
     * @return next stage in Trampolining
     */
    default Trampoline<T> bounce() {
        return this;
    }

    /**
     * @return The result of Trampoline execution
     */
    default T result() {
        return get();
    }

    /* (non-Javadoc)
     * @see java.util.function.Supplier#get()
     */
    @Override
    T get();


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#iterator()
     */
    @Override
    default Iterator<T> iterator() {
        return Arrays.asList(result())
                     .iterator();
    }

    @Override
    default ReactiveSeq<T> stream() {
        return Function0.super.stream();
    }

    /**
     * @return true if complete
     * 
     */
    default boolean complete() {
        return true;
    }

    /**
     * Created a completed Trampoline
     * 
     * @param result Completed result
     * @return Completed Trampoline
     */
    public static <T> Trampoline<T> done(final T result) {
        return () -> result;
    }

    /**
     * Create a Trampoline that has more work to do
     * 
     * @param trampoline Next stage in Trampoline
     * @return Trampoline with more work
     */
    public static <T> Trampoline<T> more(final Trampoline<Trampoline<T>> trampoline) {
        return new Trampoline<T>() {


            @Override
            public boolean complete() {
                return false;
            }

            @Override
            public Trampoline<T> bounce() {
                return trampoline.result();
            }

            @Override
            public T get() {
                return trampoline(this);
            }

            T trampoline(final Trampoline<T> trampoline) {

                return Stream.iterate(trampoline, Trampoline::bounce)
                             .filter(Trampoline::complete)
                             .findFirst()
                             .get()
                             .result();

            }
        };
    }

    @Override
    default <R> R visit(Function<? super T, ? extends R> present, Supplier<? extends R> absent){
        return present.apply(get());
    }
}
