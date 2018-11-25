package cyclops.typeclasses.taglessfinal;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.identity;
import com.oath.cyclops.hkt.DataWitness.io;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Identity;
import cyclops.data.tuple.Tuple2;
import cyclops.function.NaturalTransformation;
import cyclops.instances.reactive.IOInstances;
import cyclops.reactive.IO;
import org.junit.Before;
import org.junit.Test;

import static cyclops.typeclasses.taglessfinal.Cases.Account;

public class TaglessFinal3Test {

    private Account acc1;
    private Account acc2;
    Program3<io, identity> prog;
    private NaturalTransformation<identity,io> nt;


    @Before
    public void setup(){
        nt = new NaturalTransformation<identity, io>() {
            @Override
            public <T> Higher<io, T> apply(Higher<identity, T> a) {
                return IO.sync(Identity.narrowK(a));

            }
        };
        acc1 = new Account(10000d,10);
        acc2 = new Account(0d,11);
        prog = new Program3<>(IOInstances.monad(),new AccountIO(),new LogID(),nt,acc2,acc1);
    }


    @Test
    public void programB(){

        IO<Tuple2<Account, Account>> res = prog.transfer(100,IO::narrowK);
        res.run().peek(System.out::println);
    }
}
