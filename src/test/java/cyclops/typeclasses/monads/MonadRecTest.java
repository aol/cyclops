package cyclops.typeclasses.monads;

import com.aol.cyclops2.hkt.Higher;
import cyclops.async.Future;
import cyclops.collectionx.mutable.ListX;
import cyclops.collectionx.mutable.SetX;
import cyclops.companion.Optionals;
import cyclops.companion.Optionals.OptionalKind;
import cyclops.control.lazy.Maybe;
import cyclops.control.Reader;
import cyclops.control.Either;
import cyclops.control.anym.Witness.*;
import cyclops.reactive.ReactiveSeq;
import cyclops.typeclasses.monad.MonadRec;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class MonadRecTest {

    @Test
    public void listTest(){

        MonadRec<list> mr = ListX.Instances.monadRec();
        ListX<Integer> l = mr.tailRec(0, i -> i < 100_000 ? ListX.of(Either.left(i + 1)) : ListX.of(Either.right(i + 1)))
                .convert(ListX::narrowK);
        assertThat(l,equalTo(ListX.of(100_001)));
    }

    @Test
    public void setTest(){
        MonadRec<set> mr = SetX.Instances.monadRec();
        SetX<Integer> l = mr.tailRec(0, i -> i < 100_000 ? SetX.of(Either.left(i + 1)) : SetX.of(Either.right(i + 1)))
                .convert(SetX::narrowK);
        assertThat(l,equalTo(SetX.of(100_001)));
    }
    @Test
    public void ReactiveSeqTest(){
        MonadRec<reactiveSeq> mr = ReactiveSeq.Instances.monadRec();
        ReactiveSeq<Integer> l = mr.tailRec(0, i -> i < 100_000 ? ReactiveSeq.of(Either.left(i + 1)) : ReactiveSeq.of(Either.right(i + 1)))
                .convert(ReactiveSeq::narrowK);
        assertThat(l.to().listX(),equalTo(ReactiveSeq.of(100_001).to().listX()));
    }

    @Test
    public void maybeTest(){
        MonadRec<maybe> mr = Maybe.Instances.monadRec();
        Maybe<Integer> l = mr.tailRec(0, i -> i < 100_000 ? Maybe.just(Either.left(i + 1)) : Maybe.just(Either.right(i + 1)))
                .convert(Maybe::narrowK);
        assertThat(l,equalTo(Maybe.just(100_001)));
    }


    @Test
    public void futureTest(){
        MonadRec<future> mr = Future.Instances.monadRec();
        Future<Integer> l = mr.tailRec(0, i -> i < 100_000 ? Future.ofResult(Either.left(i + 1)) : Future.ofResult(Either.right(i + 1)))
                .convert(Future::narrowK);
        assertThat(l.get(),equalTo(Future.ofResult(100_001).get()));
    }
    @Test
    public void optionalTest(){
        MonadRec<optional> mr = Optionals.Instances.monadRec();
        Optional<Integer> l = mr.tailRec(0, i -> i < 100_000 ? OptionalKind.of(Either.left(i + 1)) : OptionalKind.of(Either.right(i + 1)))
                .convert(OptionalKind::narrowK);
        assertThat(l,equalTo(Optional.of(100_001)));
    }

    @Test
    public void readerTest(){
        MonadRec<Higher<reader, Integer>> mr = Reader.Instances.monadRec();

        Reader<Integer, Integer> l = mr.tailRec(0, i -> i < 100_000 ? Reader.of(in -> Either.left(in+i + 1)) : Reader.of(in -> Either.right(in+i + 1)))
                                        .convert(Reader::narrowK);
        assertThat(l.apply(10),equalTo(100_001+11));
    }
}
