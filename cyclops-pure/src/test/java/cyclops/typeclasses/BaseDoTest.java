package cyclops.typeclasses;

import com.oath.cyclops.hkt.Higher;
import cyclops.instances.control.OptionInstances;
import cyclops.typeclasses.monad.MonadZero;
import org.junit.Test;

import static cyclops.control.Option.some;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public abstract  class BaseDoTest<W> {

    abstract Do<W> forEach();

    abstract MonadZero<W> zero();

    abstract Higher<W,Integer> bound();

    abstract Higher<W,Integer> some(int in);

    @Test
    public void doBound(){
        assertThat(forEach()
            .__(bound())
            .yield(i->i+1),equalTo(some(11)));
    }
    @Test
    public void doUnbound(){
        assertThat(forEach()
            .__(10)
            .yield(i->i+1),equalTo(some(11)));
    }
    @Test
    public void doLazy(){
        assertThat(forEach()
            .__(10)
            .yield(i->i+1),equalTo(some(11)));
    }

    @Test
    public void doBound1(){
        assertThat(forEach()
            .__(some(10))
            .__(some(5))
            .yield((a,b)->a+b),equalTo(some(15)));
    }
    @Test
    public void doUnbound1(){
        assertThat(forEach()
            .__(10)
            .__(5)
            .yield((a,b)->a+b),equalTo(some(15)));
    }
    @Test
    public void doLazy1(){
        assertThat(forEach()
            .__(10)
            .__(i->some(i/2))
            .yield((a,b)->a+b),equalTo(some(15)));
    }

    @Test
    public void doBounded2(){
        assertThat(forEach()
            .__(some(10))
            .__(some(5))
            .__(some(2))
            .yield((a,b,c)->a+b+c),equalTo(some(17)));
    }
    @Test
    public void doUnbound2(){
        assertThat(forEach()
            .__(10)
            .__(5)
            .__(2)
            .yield((a,b,c)->a+b+c),equalTo(some(17)));
    }

    @Test
    public void doLazy2(){
        assertThat(forEach()
            .__(10)
            .__(i->some(i/2))
            .__((a,b)->some(2))
            .yield((a,b,c)->a+b+c),equalTo(some(17)));
    }
    @Test
    public void doLazyA2(){
        assertThat(forEach()
            .__(10)
            .__(i->some(i/2))
            ._1(a->some(a/5))
            .yield((a,b,c)->a+b+c),equalTo(some(17)));
    }
    @Test
    public void doLazyB2(){
        assertThat(forEach()
            .__(10)
            .__(i->some(i/2))
            ._2(b->some(b-3))
            .yield((a,b,c)->a+b+c),equalTo(some(17)));
    }


}
