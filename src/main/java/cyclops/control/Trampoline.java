package cyclops.control;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.Value;
import cyclops.control.lazy.Either;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import static javafx.scene.input.KeyCode.R;

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
 * And co-routines
 * 
 * <pre>
 * {@code 
 *  List results;
    @Test
    public void coroutine(){
        results = new ArrayList();
        Iterator<String> it = Arrays.asList("hello","world","take").iterator();
        val coroutine = new Trampoline[1];
        coroutine[0] = Trampoline.more( ()-> it.hasNext() ? print(it.next(),coroutine[0]) : Trampoline.done(0));
        withCoroutine(coroutine[0]);
        
        assertThat(results,equalTo(Arrays.asList(0,"hello",1,"world",2,"take",3,4)));
    }
    
    private Trampoline<Integer> print(Object next, Trampoline trampoline) {
        System.out.println(next);
        results.add(next);
        return trampoline;
    }
    public void withCoroutine(Trampoline coroutine){
        
        for(int i=0;i<5;i++){
                print(i,coroutine);
                if(!coroutine.complete())
                    coroutine= coroutine.bounce();
                
        }
        
    }
 * 
 * }
 * </pre>
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> Return type
 */
@FunctionalInterface
public interface Trampoline<T> extends Value<T>, To<Trampoline<T>> {

    default <R> R visit(Function<? super Trampoline<T>,? extends R> more, Function<? super T, ? extends R> done){
        return complete() ? done.apply(get()) : more.apply(this.bounce());
    }

    default  <B> Trampoline<Tuple2<T,B>> zip(Trampoline<B> b){

        Xor<Trampoline<T>,T> first = resume();
        Xor<Trampoline<B>,B> second = b.resume();

        if(first.isSecondary() && second.isSecondary()) {
            return Trampoline.more(()->first.secondaryGet().zip(second.secondaryGet()));
        }
        if(first.isPrimary() && second.isPrimary()){
            return Trampoline.done(Tuple.tuple(first.get(),second.get()));
        }
        if(first.isSecondary() && second.isPrimary()){
            return Trampoline.more(()->first.secondaryGet().zip(b));
        }
        if(first.isPrimary() && second.isSecondary()){
            return Trampoline.more(()->this.zip(second.secondaryGet()));
        }
        return null;
    }
    default  <B,C> Trampoline<Tuple3<T,B,C>> zip(Trampoline<B> b, Trampoline<C> c){

        Xor<Trampoline<T>,T> first = resume();
        Xor<Trampoline<B>,B> second = b.resume();
        Xor<Trampoline<C>,C> third = c.resume();

        if(first.isSecondary() && second.isSecondary() && third.isSecondary()) {
            return Trampoline.more(()->first.secondaryGet().zip(second.secondaryGet(),third.secondaryGet()));
        }
        if(first.isPrimary() && second.isPrimary() && third.isPrimary()){
            return Trampoline.done(Tuple.tuple(first.get(),second.get(),third.get()));
        }

        if(first.isSecondary() && second.isPrimary() && third.isPrimary()){
            return Trampoline.more(()->first.secondaryGet().zip(b,c));
        }
        if(first.isPrimary() && second.isSecondary() && third.isPrimary()){
            return Trampoline.more(()->this.zip(second.secondaryGet(),c));
        }
        if(first.isPrimary() && second.isPrimary() && third.isSecondary()){
            return Trampoline.more(()->this.zip(b,third.secondaryGet()));
        }


        if(first.isPrimary() && second.isSecondary() && third.isSecondary()){
            return Trampoline.more(()->this.zip(second.secondaryGet(),third.secondaryGet()));
        }
        if(first.isSecondary() && second.isPrimary() && third.isSecondary()){
            return Trampoline.more(()->first.secondaryGet().zip(b,third.secondaryGet()));
        }
        if(first.isSecondary() && second.isSecondary() && third.isPrimary()){
            return Trampoline.more(()->first.secondaryGet().zip(second.secondaryGet(),c));
        }
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
     * Create a Trampoline that has more work toNested do
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
}
