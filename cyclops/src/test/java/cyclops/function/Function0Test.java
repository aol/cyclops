package cyclops.function;

import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.control.Try;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class Function0Test {

    Function0<Integer> some = ()->10;
    Function0<Integer> none = ()->null;
    Function0<Integer> ex = ()->{
        throw new RuntimeException();
    };

    @Test
    public void lift(){
        assertThat(some.lift().get(),equalTo(Option.some(10)));
        assertThat(none.lift().get(),equalTo(Option.none()));
    }
    @Test
    public void liftTry(){
        assertThat(some.liftTry().get(),equalTo(Try.success(10)));
        assertThat(ex.liftTry().get().isFailure(),equalTo(true));
        assertThat(none.liftTry().get().isFailure(),equalTo(false));
    }
    @Test
    public void lazyLift(){
        assertThat(some.lazyLift().get(),equalTo(Maybe.just(10)));
        assertThat(none.lazyLift().get(),equalTo(Maybe.nothing()));
    }
}
