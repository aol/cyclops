package cyclops.monads;

import cyclops.reactive.collections.mutable.ListX;
import cyclops.control.Option;
import cyclops.control.Either;
import cyclops.monads.Witness.stream;
import cyclops.monads.Witness.reactiveSeq;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 06/02/2017.
 */
public class KleisliMTest {
    @Test
    public void firstK(){
        KleisliM<stream, Integer, Integer> k1 = t -> AnyM.fromArray(t);
        assertThat(ListX.of(10),
                equalTo(k1.firstK().apply(Tuple.tuple(10,-1)).stream().map(Tuple2::_1).toList()));
    }

    @Test
    public void secondK(){
        KleisliM<stream, Integer, Integer> k1 = t -> AnyM.fromArray(t);
        assertThat(ListX.of(-1),
                equalTo(k1.secondK().apply(Tuple.tuple(10,-1)).stream().map(Tuple2::_2).toList()));
    }
    @Test
    public void leftK(){
        KleisliM<stream, Integer, Integer> k1 = t -> AnyM.fromArray(t);
        assertThat(ListX.of(Option.some(10)),
                equalTo(k1.leftK(stream.INSTANCE).apply(Either.left(10)).stream().map(Either::getLeft).toList()));
    }
    @Test
    public void rightK(){
        KleisliM<stream, Integer, Integer> k1 = t -> AnyM.fromArray(t);
        assertThat(ListX.of(Option.some(10)),
                equalTo(k1.rightK(stream.INSTANCE).apply(Either.right(10)).stream().map(Either::get).toList()));
    }
    @Test
    public void andThen(){
        KleisliM<stream, Integer, Integer> k1 = t -> AnyM.fromArray(t);
        assertThat("10",equalTo(k1.andThen(s->s.stream()
                                                        .join()).apply(10)));
    }

    @Test
    public void local(){
        KleisliM<stream, Integer, Integer> k1 = t -> AnyM.fromArray(t);
        assertThat(ListX.of(3),equalTo(k1.local(i->i+1).apply(2).to(Witness::stream).collect(Collectors.toList())));
    }

    @Test
    public void flatMap(){
        KleisliM<stream, Integer, Integer> k1 = t -> AnyM.fromArray(1);

        assertThat(ListX.of(11),equalTo(k1.flatMap(i-> t->AnyM.fromArray(i+t))
                .apply(10)
                .collect(Collectors.toList())));
    }
    @Test
    public void example(){
        KleisliM<reactiveSeq, Integer, Integer> k1 = t -> AnyM.fromStream(ReactiveSeq.iterate(0, i->i<t, i->i+1));

        assertThat(ListX.iterate(10,10,i->i+1),equalTo(k1.flatMap(i-> t-> AnyM.fromStream(ReactiveSeq.of(t+i)))
                .apply(10)
                .collect(Collectors.toList())));
    }
    @Test
    public void flatMapA(){
        KleisliM<stream, Integer, Integer> k1 = t -> AnyM.fromArray(1);

        k1.flatMapA(i->AnyM.fromArray(i+10))
                .apply(10)
                .forEach(System.out::println);
    }
    @Test
    public void forTest(){
        KleisliM<stream, Integer, Integer> k = t -> AnyM.fromStream(Stream.of(t));

        k.forEach4(r-> t->AnyM.fromStream(Stream.of(t)),
                                        (Integer r,Integer r1)->t->AnyM.fromArray(r,r1,t),
                                        (r,r1,r2)->t->AnyM.fromArray(r),
                                        (r,r1,r2,r3)-> r+r1);
    }
}
