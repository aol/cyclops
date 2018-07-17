package cyclops.function;

import cyclops.control.Future;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.control.Try;
import org.junit.Test;

import java.util.concurrent.ForkJoinPool;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class Function3Test {

    Function3<Integer,Integer,Integer,Integer> some = (a,b,c)->a+b+c;
    Function3<Integer,Integer,Integer,Integer> none = (a,b,c)->null;
    Function3<Integer,Integer,Integer,Integer> ex = (a,b,c)->{
        throw new RuntimeException();
    };

    @Test
    public void lift(){
        assertThat(some.lift3().apply(3,3,4),equalTo(Option.some(10)));
        assertThat(none.lift3().apply(3,3,4),equalTo(Option.none()));
    }
    @Test
    public void liftEx(){
        assertThat(some.lift3(ForkJoinPool.commonPool()).apply(3,3,4).orElse(-1),equalTo(Future.ofResult(10).orElse(-1)));
        assertThat(none.lift3(ForkJoinPool.commonPool()).apply(3,3,4).orElse(-1),equalTo(null));
    }
    @Test
    public void liftTry(){
        assertThat(some.liftTry3().apply(3,3,4),equalTo(Try.success(10)));
        assertThat(ex.liftTry3().apply(3,3,4).isFailure(),equalTo(true));
        assertThat(none.liftTry3().apply(3,3,4).isFailure(),equalTo(false));
    }
    @Test
    public void lazyLift(){
        assertThat(some.lazyLift3().apply(3,3,4),equalTo(Maybe.just(10)));
        assertThat(none.lazyLift3().apply(3,3,4),equalTo(Maybe.nothing()));
    }
}
