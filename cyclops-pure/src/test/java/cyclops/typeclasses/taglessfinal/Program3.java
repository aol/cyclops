package cyclops.typeclasses.taglessfinal;

import com.oath.cyclops.hkt.Higher;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.function.NaturalTransformation;
import cyclops.typeclasses.Do;
import cyclops.typeclasses.monad.Monad;
import cyclops.typeclasses.taglessfinal.Cases.Account;
import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor
public class Program3<W,W2> {

    private final Monad<W> monad;
    private final AccountAlgebra<W> accountService;
    private final LogAlgebra<W2> logService;
    private final NaturalTransformation<W2,W> nt;

    public Higher<W, Tuple2<Account,Account>> transfer(Account to, Account from, double amount){

        return  Do.forEach(monad)
                    .__(()->accountService.debit(from,amount))
                    .__(this::logBalance)
                    ._1(newFrom-> accountService.credit(to,amount))
                    ._3(this::logBalance)
                    .yield_13(Tuple::tuple);

    }
    private Higher<W, Void> logBalance(Account a) {
        return  logService.info("Account balance " + a.getBalance())
                          .convert(nt.asFunction());
    }
}
