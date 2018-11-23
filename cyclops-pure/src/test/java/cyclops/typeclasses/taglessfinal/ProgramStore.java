package cyclops.typeclasses.taglessfinal;

import com.oath.cyclops.hkt.Higher;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.typeclasses.Do;
import cyclops.typeclasses.monad.Monad;
import cyclops.typeclasses.taglessfinal.Cases.Account;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProgramStore<W> {

    private final Monad<W> monad;
    private final AccountAlgebra2<W> accountService;


    public Higher<W, Tuple2<Option<Account>,Option<Account>>> transfer(Account to, Account from, double amount){

        return Do.forEach(monad)
                 .__(()->accountService.debit(from,amount))
                 .__(newFrom-> accountService.credit(to,amount))
                 .yield(Tuple::tuple);
    }
}
