package cyclops.typeclasses.monads;

import com.oath.cyclops.hkt.Higher;
import cyclops.control.Future;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.SetX;
import cyclops.companion.Optionals;
import cyclops.companion.Optionals.OptionalKind;
import cyclops.control.Maybe;
import cyclops.control.Reader;
import cyclops.control.Either;
import com.oath.cyclops.hkt.DataWitness.*;
import cyclops.reactive.ReactiveSeq;
import cyclops.typeclasses.monad.MonadRec;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class MonadRecTest {

    @Test
    public void listTest(){

        MonadRec<list> mr = ListXInstances.monadRec();
        ListX<Integer> l = mr.tailRec(0, i -> i < 100_000 ? ListX.of(Either.left(i + 1)) : ListX.of(Either.right(i + 1)))
                .convert(ListX::narrowK);
        assertThat(l,equalTo(ListX.of(100_001)));
    }

    @Test
    public void setTest(){
        MonadRec<set> mr = SetX.SetXInstances.monadRec();
        SetX<Integer> l = mr.tailRec(0, i -> i < 100_000 ? SetX.of(Either.left(i + 1)) : SetX.of(Either.right(i + 1)))
                .convert(SetX::narrowK);
        assertThat(l,equalTo(SetX.of(100_001)));
    }
    @Test
    public void ReactiveSeqTest(){
        MonadRec<reactiveSeq> mr = PublisherInstances.monadRec();
        ReactiveSeq<Integer> l = mr.tailRec(0, i -> i < 100_000 ? ReactiveSeq.of(Either.left(i + 1)) : ReactiveSeq.of(Either.right(i + 1)))
                .convert(ReactiveSeq::narrowK);
        assertThat(l.to().listX(),equalTo(ReactiveSeq.of(100_001).to().listX()));
    }

    @Test
    public void maybeTest(){
        MonadRec<option> mr = MaybeInstances.monadRec();
        Maybe<Integer> l = mr.tailRec(0, i -> i < 100_000 ? Maybe.just(Either.left(i + 1)) : Maybe.just(Either.right(i + 1)))
                .convert(Maybe::narrowK);
        assertThat(l,equalTo(Maybe.just(100_001)));
    }


    @Test
    public void futureTest(){
        MonadRec<future> mr = FutureInstances.monadRec();
        Future<Integer> l = mr.tailRec(0, i -> i < 100_000 ? Future.ofResult(Either.left(i + 1)) : Future.ofResult(Either.right(i + 1)))
                .convert(Future::narrowK);
        assertThat(l.get(),equalTo(Future.ofResult(100_001).get()));
    }
    @Test
    public void optionalTest(){
        MonadRec<optional> mr = OptionalInstances.monadRec();
        Optional<Integer> l = mr.tailRec(0, i -> i < 100_000 ? OptionalKind.of(Either.left(i + 1)) : OptionalKind.of(Either.right(i + 1)))
                .convert(OptionalKind::narrowK);
        assertThat(l,equalTo(Optional.of(100_001)));
    }

    @Test
    public void readerTest(){
        MonadRec<Higher<reader, Integer>> mr = Reader.ReaderInstances.monadRec();

        Reader<Integer, Integer> l = mr.tailRec(0, i -> i < 100_000 ? Reader.of(in -> Either.left(in+i + 1)) : Reader.of(in -> Either.right(in+i + 1)))
                                        .convert(Reader::narrowK);
        assertThat(l.apply(10),equalTo(100_001+11));
    }
}
