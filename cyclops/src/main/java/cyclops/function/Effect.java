package cyclops.function;

import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.control.Future;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


@FunctionalInterface
public interface Effect extends Runnable{


    default Effect memoize(){
        return ()->Memoize.memoizeRunnable(this)
                          .run();
    }
    default Thread runAsync(){
        Thread t = new Thread(this);
        t.start();
        return t;
    }

    default Future<Void> future(Executor ex){
        return Future.of(CompletableFuture.runAsync(this,ex));
    }
    default Effect async(Executor ex){
        return ()->future(ex);
    }
    default Effect asyncAndBlock(Executor ex){
        return ()->future(ex).getFuture().join();
    }
    default void run(){
        try {
            runChecked();
        } catch (Exception e) {
            throw ExceptionSoftener.throwSoftenedException(e);
        }
    }

    default Callable<Void> callable(){
        return ()-> {
            run();
            return null;
        };
    }

    void runChecked() throws Exception;

    default Effect onError(Consumer<Throwable> onError){
        return ()->{
            try{
                runChecked();
            }catch(Throwable e){
                onError.accept(e);
            }
        };
    }

    default Effect andThen(Runnable r){
        return ()-> {
            run();
            r.run();
        };
    }
    default Effect andThenChecked(Callable<Void> r){
        return ()-> {
            runChecked();
            r.call();
        };
    }

    default <R> Function0<R> supply(R r){
        return ()->{
            run();
            return r;
        };
    }
    default <R> R fold(Supplier<R> success, Function<Throwable,? extends R> failure){
        try {
            run();
        }catch(Throwable t){
            return failure.apply(t);
        }
        return success.get();
    }
    default <R> Function0<R> supplyOr(R success, R failure){
        return ()->{
            try {
                run();
            }catch(Throwable t){
                return failure;
            }
            return success;
        };
    }

    default Effect flatMap(Supplier<Runnable> r){
      return ()->{
          run();
          r.get().run();
      };
    }

    default Effect noop(){
        return ()->{};
    }

    default Effect cycle(){
      return  ()->{
            while(true)
                run();

        };
    }
    default Effect cycleAsync(Executor ex){
        return  ()->{
            while(true)
                async(ex).run();

        };
    }
    default Effect cycleAsync(long times,Executor ex){
        return  ()->{
            for(long i=0;i<times;i++)
                async(ex).run();

        };
    }
    default Effect cycleForever(Consumer<Throwable> onError){
        return  ()-> {
            while (true){
                try {
                    run();
                } catch (Throwable t) {
                    onError.accept(t);
                }
            }

        };
    }
    default Effect cycle(long times){
        return  ()->{
           for(long i=0;i<times;i++)
                run();

        };
    }
    default Effect cycleWhile(Supplier<Boolean> pred){
        return  ()->{
            while(pred.get())
                run();

        };
    }
    default Effect cycleUntil(Supplier<Boolean> pred){
        return cycleWhile(()->!pred.get());
    }
    default Effect cycleUntilException(Consumer<Throwable> onError){
        return ()->{
            try{
                while(true) {
                    run();
                }
            }catch(Throwable t){
                onError.accept(t);
            }
        };

    }
    default FluentFunctions.FluentRunnable fluentRunnable(){
        return FluentFunctions.ofRunnable(this);
    }

    default Effect retry() {
        return retry(7, 2, TimeUnit.SECONDS);
    }

    /**
     * @param retries
     *            Number of retries
     * @param delay
     *            Delay in TimeUnits
     * @param timeUnit
     *            TimeUnit to use for delay
     */
    default  Effect retry( final int retries, final long delay, final TimeUnit timeUnit) {
      return () -> {
            final long[] sleep = { timeUnit.toMillis(delay) };
            Throwable exception = null;
            for (int count = retries; count >=0; count--) {
                try {
                    run();
                } catch (final Throwable e) {
                    exception = e;
                    ExceptionSoftener.softenRunnable(() -> Thread.sleep(sleep[0]))
                            .run();
                    sleep[0] = sleep[0] * 2;
                }
            }
            ExceptionSoftener.throwSoftenedException(exception);

        };

    }



    default Effect compose(Runnable r){
        return ()->{
            r.run();
            run();
        };
    }
    default Effect composeChecked(Callable<Void> r){
        return ()->{
            r.call();
            runChecked();
        };
    }


    default <R1, R4> Effect forEach4(Supplier<Effect> value2,
                                     Supplier<Effect> value3,
                                     Supplier<Effect> value4,
                                     Effect yieldingFunction) {

        return this.flatMap(()->  value2.get().flatMap(()->value3.get().flatMap(()->value4.get().andThen(yieldingFunction))));

    }
    default <R1, R4> Effect forEach3(Supplier<Effect> value2,
                                     Supplier<Effect> value3,
                                     Effect yieldingFunction) {

        return this.flatMap(()->  value2.get().flatMap(()->value3.get().andThen(yieldingFunction)));

    }

    default <R1, R4> Effect forEach2(Supplier<Effect> value2,
                                           Effect yieldingFunction) {

        return this.flatMap(()->  value2.get().andThen(yieldingFunction));

    }

}
