package cyclops.typeclasses;

import com.oath.cyclops.hkt.DataWitness.option;
import cyclops.control.Option;
import cyclops.data.Seq;
import cyclops.function.Lambda;
import cyclops.instances.control.OptionInstances;
import cyclops.instances.data.SeqInstances;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static cyclops.control.Option.some;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class Do7Test {

    @Test
    public void doOption7(){
        assertThat(Do.forEach(OptionInstances::monad)
                     .__(some(10))
                     .__(some(5))
                     .__(some(2))
                     .__(some(1))
                     .__(some(100))
                     .__(some(1000))
                     .__(some(10000))
                     .__(some(100000))
                     .yield((a,b,c,d,e,f,g,h)->a+b+c+d+e+f+g+h)
                    .fold(Option::narrowK),equalTo(some(111118)));
    }
    @Test
    public void doOptionUnbound7(){
        assertThat(Do.forEach(OptionInstances::monad)
                     ._of(10)
                     ._of(5)
                     ._of(2)
                     ._of(1)
                     ._of(100)
                     .__(some(1000))
                     .__(some(10000))
                     .__(some(100000))
                     .yield((a,b,c,d,e,f,g,h)->a+b+c+d+e+f+g+h)
                     .fold(Option::narrowK),
            equalTo(some(111118)));
    }

    @Test
    public void doOptionLazy7(){
        assertThat(Do.forEach(OptionInstances::monad)
            ._of(10)
            .__(i->some(i/2))
            .__((a,b)->some(a-b-3))
            .__((a,b,c)->some(a-c-b-2))
            .__((a,b,c,d)->some(a+b+c+d+82))
            .__((a,b,c,d,e)->some(1000))
            .__((a,b,c,d,e,g)->some(10000))
            .__((a,b,c,d,e,g,h)->some(100000))
            .yield((a,b,c,d,e,f,g,h)->a+b+c+d+e+f+g+h)
            .fold(Option::narrowK),equalTo(some(111118)));
    }


    @Test
    public void doOptionGuardSome3(){
        assertThat(Do.forEach(OptionInstances::monad)
                    .__(some(10))
                    .__(some(5))
                    .__(some(2))
                    .__(some(1))
                    .__(some(100))
                    .__(some(1000))
                    .__(some(10000))
                    .__(some(100000))
                    .guard(OptionInstances.monadZero(),(a,b,c,d,e,f,g,h)->a+b+c+d+e+f+g+h>117)
                    .yield((a,b,c,d,e,f,g,h)->a+b+c+d+e+f+g+h)
                    .fold(Option::narrowK),
            equalTo(some(111118)));
    }
    @Test
    public void doOptionGuardNone3(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(some(10))
            .__(some(5))
            .__(some(2))
            .__(some(1))
            .__(some(100))
            .__(some(1000))
            .__(some(10000))
            .__(some(100000))
            .guard(OptionInstances.monadZero(),(a,b,c,d,e,f,g,h)->a+b+c+d+e+f+g+h<117)
            .yield((a,b,c,d,e,f,g,h)->a+b+c+d+e+f+g+h)
            .fold(Option::narrowK),equalTo(Option.none()));
    }
    @Test
    public void doOptionShow(){
        String s = Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(20)
            ._of(100)
            ._of(1000)
            ._of(10000)
            ._of(100000)
            ._of(1000000)
            .show(new Show<option>(){})
            .yield((a,b,c,d,e,f,g)->a+b+c+d+e+f+g)
            .fold(Option::narrowK).orElse(null);
        assertThat(s,equalTo("111130Some[10000]"));
    }
    @Test
    public void doOptionShowDefault(){
        String s = Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(20)
            ._of(200)
            ._of(1000)
            ._of(10000)
            ._of(100000)
            ._of(1000000)
            ._show(new Show<option>() {})
            .yield((a,b,c,d,e,f,g,st)->st+a+b+c+d+e+f+g).fold(Option::narrowK).orElse(null);
        assertThat(s,equalTo("Some[1000]10202001000100001000001000000"));
    }

    @Test
    public void doOptionMap1(){
        Option<Integer> eleven =   Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(100)
            ._of(1000)
            ._of(10000)
            ._of(100000)
            ._of(1000000)
            ._of(10000000)
            .map(i->i+1)
            .fold(Option::narrowK);

        assertThat(eleven,equalTo(some(10000001)));

    }
    @Test
    public void doOptionPeek1(){
        AtomicInteger ai = new AtomicInteger(-1);
        Option<Integer> eleven =   Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(100)
            ._of(1000)
            ._of(10000)
            ._of(100000)
            ._of(1000000)
            ._of(10000000)
            .peek(i->{
                ai.set(i);
            })
            .fold(Option::narrowK);

        assertThat(ai.get(),equalTo(10000000));

    }
    @Test
    public void doOptionFlatten (){

        Option<Integer> res =   Do.forEach(OptionInstances.monad())
            ._of(10)
            ._of(100)
            ._of(1000)
            ._of(10000)
            ._of(100000)
            ._of(1000000)
            ._of(10000000)
            ._flatten(some(some(10)))
            .yield((a,b,c,d,e,f,g,h)->a+b+c+d+e+f+g+h)
            .fold(Option::narrowK);

        assertThat(res,equalTo(some(11111120)));

    }



    @Test
    public void doSeqAp(){
        Seq<Integer> seq = Do.forEach(SeqInstances::monad)
            ._of(10)
            ._of(20)
            ._of(1000)
            ._of(10000)
            ._of(100000)
            ._of(1000000)
            ._of(10000000)
            .ap(Seq.of(Lambda.λ((Integer i) -> i + 1)))
            .fold(Seq::narrowK);


        assertThat(seq,equalTo(Seq.of(10000001)));

    }

}
