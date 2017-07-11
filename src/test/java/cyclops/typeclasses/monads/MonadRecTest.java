package cyclops.typeclasses.monads;

import cyclops.async.Future;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.SetX;
import cyclops.companion.Optionals;
import cyclops.companion.Optionals.OptionalKind;
import cyclops.control.Maybe;
import cyclops.control.Xor;
import cyclops.monads.Witness;
import cyclops.monads.Witness.*;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.monad.MonadRec;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class MonadRecTest {

    @Test
    public void listTest(){
        MonadRec<list> mr = ListX.Instances.monadRec();
        ListX<Integer> l = mr.tailRec(0, i -> i < 100_000 ? ListX.of(Xor.secondary(i + 1)) : ListX.of(Xor.primary(i + 1)))
                .convert(ListX::narrowK);
        assertThat(l,equalTo(ListX.of(100_001)));
    }

    @Test
    public void setTest(){
        MonadRec<set> mr = SetX.Instances.monadRec();
        SetX<Integer> l = mr.tailRec(0, i -> i < 100_000 ? SetX.of(Xor.secondary(i + 1)) : SetX.of(Xor.primary(i + 1)))
                .convert(SetX::narrowK);
        assertThat(l,equalTo(SetX.of(100_001)));
    }
    @Test
    public void ReactiveSeqTest(){
        MonadRec<reactiveSeq> mr = ReactiveSeq.Instances.monadRec();
        ReactiveSeq<Integer> l = mr.tailRec(0, i -> i < 100_000 ? ReactiveSeq.of(Xor.secondary(i + 1)) : ReactiveSeq.of(Xor.primary(i + 1)))
                .convert(ReactiveSeq::narrowK);
        assertThat(l.to().listX(),equalTo(ReactiveSeq.of(100_001).to().listX()));
    }

    @Test
    public void maybeTest(){
        MonadRec<maybe> mr = Maybe.Instances.monadRec();
        Maybe<Integer> l = mr.tailRec(0, i -> i < 100_000 ? Maybe.just(Xor.secondary(i + 1)) : Maybe.just(Xor.primary(i + 1)))
                .convert(Maybe::narrowK);
        assertThat(l,equalTo(Maybe.just(100_001)));
    }
    @Test
    public void futureTest(){
        MonadRec<future> mr = Future.Instances.monadRec();
        Future<Integer> l = mr.tailRec(0, i -> i < 100_000 ? Future.ofResult(Xor.secondary(i + 1)) : Future.ofResult(Xor.primary(i + 1)))
                .convert(Future::narrowK);
        assertThat(l.get(),equalTo(Future.ofResult(100_001).get()));
    }
    @Test
    public void optionalTest(){
        MonadRec<optional> mr = Optionals.Instances.monadRec();
        Optional<Integer> l = mr.tailRec(0, i -> i < 100_000 ? OptionalKind.of(Xor.secondary(i + 1)) : OptionalKind.of(Xor.primary(i + 1)))
                .convert(OptionalKind::narrowK);
        assertThat(l,equalTo(Optional.of(100_001)));
    }

}
