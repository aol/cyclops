package cyclops.monads;

import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 06/02/2017.
 */
public class KleisliTest {

    @Test
    public void flatMap(){
        Kleisli<Witness.stream, Integer, Integer> k1 = Kleisli.kleisli(t -> AnyM.fromArray(1), Witness.stream.INSTANCE);

        k1.flatMap(i->Kleisli.kleisli(t->AnyM.fromArray(i+t),Witness.stream.INSTANCE))
                .apply(10)
                .forEach(System.out::println);
    }
    @Test
    public void flatMapA(){
        Kleisli<Witness.stream, Integer, Integer> k1 = Kleisli.kleisli(t -> AnyM.fromArray(1), Witness.stream.INSTANCE);

        k1.flatMapA(i->AnyM.fromArray(i+10))
                .apply(10)
                .forEach(System.out::println);
    }
    @Test
    public void forTest(){
        Kleisli<Witness.stream, Integer, Integer> k = Kleisli.kleisli(t -> AnyM.fromStream(Stream.of(t)), Witness.stream.INSTANCE);

        k.forEach4(r->Kleisli.kleisli(t->AnyM.fromStream(Stream.of(t)),Witness.stream.INSTANCE),
                                        (Integer r,Integer r1)->t->AnyM.fromArray(r,r1,t),
                                        (r,r1,r2)->t->AnyM.fromArray(r),
                                        (r,r1,r2,r3)-> r+r1);
    }
}