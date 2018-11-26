package cyclops.typeclasses.taglessfinal;

import com.oath.cyclops.hkt.DataWitness.io;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.reactive.IO;

import cyclops.typeclasses.taglessfinal.Cases.Account;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AccountIO implements AccountAlgebra<io> {
    private final Executor exec = Executors.newFixedThreadPool(1);

    @Override
    public Higher<io, Account> debit(Account account, double amount) {
        return IO.of(()->account,exec)
                 .map(a->a.debit(amount));
    }

    @Override
    public Higher<io, Account> credit(Account account, double amount) {
        return IO.of(()->account,exec)
                 .map(a->a.credit(amount));
    }
}
