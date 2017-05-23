package cyclops.control;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.Value;

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
