package cyclops.typeclasses;

import com.oath.cyclops.hkt.Higher;
import cyclops.control.Option;
import cyclops.instances.control.OptionInstances;
import cyclops.typeclasses.monad.MonadZero;
import org.junit.Test;

import static cyclops.control.Option.some;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public abstract  class BaseDoGuardTest<W> extends BaseDoTest<W>{

    abstract Higher<W,Integer> none();
    @Test
    public void doGuardSome(){
        assertThat(this.forEach()
            .__(bound())
            .guard(zero(),i->i>5)
            .yield(i->i+1),equalTo(some(11)));
    }
    @Test
    public void doGuardNone(){
        assertThat(this.forEach()
            .__(bound())
            .guard(zero(),i->i<5)
            .yield(i->i+1),equalTo(none()));
    }
    @Test
    public void doGuardSome1(){
        assertThat(this.forEach()
            .__(some(10))
            .__(some(5))
            .guard(zero(),(a,b)->a+b>14)
            .yield((a,b)->a+b),equalTo(some(15)));
    }
    @Test
    public void doGuardNone1(){
        assertThat(this.forEach()
            .__(some(10))
            .__(some(5))
            .guard(zero(),(a,b)->a+b<15)
            .yield((a,b)->a+b),equalTo(Option.none()));
    }

    @Test
    public void doGuardSome2(){
        assertThat(this.forEach()
                .__(some(10))
                .__(some(5))
                .__(some(2))
                .guard(zero(),(a,b,c)->a+b+c>16)
                .yield((a,b,c)->a+b+c),
            equalTo(some(17)));
    }
    @Test
    public void doGuardNone2(){
        assertThat(this.forEach()
            .__(some(10))
            .__(some(5))
            .__(some(2))
            .guard(zero(),(a,b,c)->a+b+c<17)
            .yield((a,b,c)->a+b+c),equalTo(Option.none()));
    }
    @Test
    public void doGuardSome3(){
        assertThat(forEach()
                .__(some(10))
                .__(some(5))
                .__(some(2))
                .__(some(1))
                .guard(zero(),(a,b,c,d)->a+b+c+d>17)
                .yield((a,b,c,d)->a+b+c+d),
            equalTo(some(18)));
    }
    @Test
    public void doGuardNone3(){
        assertThat(forEach()
            .__(some(10))
            .__(some(5))
            .__(some(2))
            .__(some(1))
            .guard(zero(),(a,b,c,d)->a+b+c+d<17)
            .yield((a,b,c,d)->a+b+c+d),equalTo(Option.none()));
    }


}
