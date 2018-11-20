package cyclops.typeclasses;

import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.data.Seq;
import cyclops.instances.control.EitherInstances;
import cyclops.instances.control.OptionInstances;
import cyclops.instances.data.SeqInstances;
import org.junit.Test;

import static cyclops.control.Either.right;
import static cyclops.control.Option.some;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class DoTest {

    @Test
    public void doOption(){
        assertThat(Do.forEach(OptionInstances::monad)
                     .__(some(10))
                     .yield(i->i+1),equalTo(some(11)));
    }
    @Test
    public void doOptionUnbound(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(10)
            .yield(i->i+1),equalTo(some(11)));
    }
    @Test
    public void doOptionLazy(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(10)
            .yield(i->i+1),equalTo(some(11)));
    }
    @Test
    public void doOptionGuardSome(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(some(10))
            .guard(OptionInstances.monadZero(),i->i>5)
            .yield(i->i+1),equalTo(some(11)));
    }
    @Test
    public void doOptionGuardNone(){
        assertThat(Do.forEach(OptionInstances::monad)
            .__(some(10))
            .guard(OptionInstances.monadZero(),i->i<5)
            .yield(i->i+1),equalTo(Option.none()));
    }
    @Test
    public void doSeq(){
        assertThat(Do.forEach(SeqInstances::monad)
            .__(Seq.of(10))
            .yield(i->i+1),equalTo(Seq.of(11)));
    }
    @Test
    public void doSeqUnbound(){
        assertThat(Do.forEach(SeqInstances::monad)
            .__(10)
            .yield(i->i+1),equalTo(Seq.of(11)));
    }
    @Test
    public void doSeqLazy(){
        assertThat(Do.forEach(SeqInstances::monad)
            .__(10)
            .yield(i->i+1),equalTo(Seq.of(11)));
    }

    @Test
    public void doEither(){
        Do.forEach(EitherInstances.<String>monad())
            .__(Either.<String,Integer>right(10))
            .yield(i->i+1);

        assertThat(Do.forEach(EitherInstances::monad)
            .__(right(10))
            .yield(i->i+1),equalTo(right(11)));
    }

}
