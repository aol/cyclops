package cyclops.typeclasses;

import com.oath.cyclops.hkt.Higher;
import cyclops.typeclasses.monad.MonadZero;
import org.junit.Test;

import static cyclops.function.Function2._1;
import static cyclops.function.Function2._2;
import static cyclops.function.Function3.__1;
import static cyclops.function.Function3.__2;
import static cyclops.function.Function3.__3;
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
            ._of(10)
            .yield(i->i+1),equalTo(some(11)));
    }
    @Test
    public void doLazy(){
        assertThat(forEach()
            ._of(10)
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
            ._of(10)
            ._of(5)
            .yield((a,b)->a+b),equalTo(some(15)));
    }
    @Test
    public void doLazy1(){
        assertThat(forEach()
            ._of(10)
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
            ._of(10)
            ._of(5)
            ._of(2)
            .yield((a,b,c)->a+b+c),equalTo(some(17)));
    }

    @Test
    public void doLazy2(){
        assertThat(forEach()
            ._of(10)
            .__(i->some(i/2))
            .__((a,b)->some(2))
            .yield((a,b,c)->a+b+c),equalTo(some(17)));
    }
    @Test
    public void doLazyA2(){
        assertThat(forEach()
            ._of(10)
            .__(i->some(i/2))
            .__(_1(a->some(a/5)))
            .yield((a,b,c)->a+b+c),equalTo(some(17)));
    }
    @Test
    public void doLazyB2(){
        assertThat(forEach()
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .yield((a,b,c)->a+b+c),equalTo(some(17)));
    }

    @Test
    public void doUnbounded3(){
        assertThat(forEach()
            .__(some(10))
            .__(some(5))
            .__(some(2))
            .__(some(1))
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doUnbound3(){
        assertThat(forEach()
            ._of(10)
            ._of(5)
            ._of(2)
            ._of(1)
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }

    @Test
    public void doLazy3(){
        assertThat(forEach()
            ._of(10)
            .__(i->some(i/2))
            .__((a,b)->some(a-b-3))
            .__((a,b,c)->some(a-c-b-2))
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doLazyA3(){
        assertThat(forEach()
            ._of(10)
            .__(i->some(i/2))
            .__(_1(a->some(a/5)))
            .__(__1(a->some(a/10)))
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doLazyB3(){
        assertThat(forEach()
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__2(b->some(b-4)))
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
    @Test
    public void doLazyC3(){
        assertThat(forEach()
            ._of(10)
            .__(i->some(i/2))
            .__(_2(b->some(b-3)))
            .__(__3(c->some(c-1)))
            .yield((a,b,c,d)->a+b+c+d),equalTo(some(18)));
    }
}
