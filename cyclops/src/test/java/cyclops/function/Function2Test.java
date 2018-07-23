package cyclops.function;

import cyclops.control.Future;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.control.Try;
import org.junit.Test;

import java.util.concurrent.ForkJoinPool;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class Function2Test {

    Function2<Integer,Integer,Integer> some = (a,b)->a+b;
    Function2<Integer,Integer,Integer> none = (a,b)->null;
    Function2<Integer,Integer,Integer> ex = (a,b)->{
        throw new RuntimeException();
    };

    @Test
    public void lift(){
        assertThat(some.lift().apply(5,5),equalTo(Option.some(10)));
        assertThat(none.lift().apply(5,5),equalTo(Option.none()));
    }
    @Test
    public void liftEx(){
        assertThat(some.lift(ForkJoinPool.commonPool()).apply(5,5).orElse(-1),equalTo(Future.ofResult(10).orElse(-1)));
        assertThat(none.lift(ForkJoinPool.commonPool()).apply(5,5).orElse(-1),equalTo(null));
    }
    @Test
    public void liftTry(){
        assertThat(some.liftTry().apply(5,5),equalTo(Try.success(10)));
        assertThat(ex.liftTry().apply(5,5).isFailure(),equalTo(true));
        assertThat(none.liftTry().apply(5,5).isFailure(),equalTo(false));
    }
    @Test
    public void lazyLift(){
        assertThat(some.lazyLift().apply(5,5),equalTo(Maybe.just(10)));
        assertThat(none.lazyLift().apply(5,5),equalTo(Maybe.nothing()));
    }
}
