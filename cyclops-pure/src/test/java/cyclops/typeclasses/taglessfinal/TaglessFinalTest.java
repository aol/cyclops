package cyclops.typeclasses.taglessfinal;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.io;
import com.oath.cyclops.hkt.Higher;
import cyclops.data.tuple.Tuple2;
import cyclops.instances.reactive.IOInstances;
import cyclops.reactive.IO;
import cyclops.reactive.IOMonad;
import org.junit.Before;
import org.junit.Test;
import static cyclops.typeclasses.taglessfinal.Cases.*;

public class TaglessFinalTest {

    private Cases.Account acc1;
    private Cases.Account acc2;
    Program<io> prog;
    @Before
    public void setup(){
        acc1 = new Cases.Account(10000d,10);
        acc2 = new Cases.Account(0d,11);
        prog = new Program<>(IOInstances.monad(),new AccountIO(),acc1,acc2);
    }

    @Test
    public void programA(){

        IO<Tuple2<Account, Account>> res = prog.transfer(100)
                                                .convert(IO::narrowK);
        res.run().peek(System.out::println);
    }
    @Test
    public void programB(){

        IO<Tuple2<Account, Account>> res = prog.transfer(100,IO::narrowK);
        res.run().peek(System.out::println);
    }
}
