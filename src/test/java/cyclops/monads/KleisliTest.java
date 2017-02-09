package cyclops.monads;

import cyclops.collections.ListX;
import cyclops.monads.Witness.stream;
import cyclops.monads.Witness.reactiveSeq;
import cyclops.stream.ReactiveSeq;
import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cyclops.monads.Kleisli.kleisli;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 06/02/2017.
 */
public class KleisliTest {

    @Test
    public void local(){
        Kleisli<stream, Integer, Integer> k1 = t -> AnyM.fromArray(t);
        assertThat(ListX.of(3),equalTo(k1.local(i->i+1).apply(2).to(Witness::stream).collect(Collectors.toList())));
    }

    @Test
    public void flatMap(){
        Kleisli<stream, Integer, Integer> k1 = t -> AnyM.fromArray(1);

        assertThat(ListX.of(11),equalTo(k1.flatMap(i-> t->AnyM.fromArray(i+t))
                .apply(10)
                .collect(Collectors.toList())));
    }
    @Test
    public void example(){
        Kleisli<reactiveSeq, Integer, Integer> k1 = t -> ReactiveSeq.iterate(0,i->i<t, i->i+1)
                                                                            .anyM();

        assertThat(ListX.iterate(10,10,i->i+1),equalTo(k1.flatMap(i-> t-> ReactiveSeq.of(t+i)
                                      .anyM())
                .apply(10)
                .collect(Collectors.toList())));
    }
    @Test
    public void flatMapA(){
        Kleisli<stream, Integer, Integer> k1 =  t -> AnyM.fromArray(1);

        k1.flatMapA(i->AnyM.fromArray(i+10))
                .apply(10)
                .forEach(System.out::println);
    }
    @Test
    public void forTest(){
        Kleisli<stream, Integer, Integer> k = t -> AnyM.fromStream(Stream.of(t));

        k.forEach4(r-> t->AnyM.fromStream(Stream.of(t)),
                                        (Integer r,Integer r1)->t->AnyM.fromArray(r,r1,t),
                                        (r,r1,r2)->t->AnyM.fromArray(r),
                                        (r,r1,r2,r3)-> r+r1);
    }
}