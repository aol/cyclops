package cyclops.typeclasses.taglessfinal;

import com.oath.cyclops.hkt.Higher;
import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.typeclasses.taglessfinal.Cases.Account;

public interface AccountAlgebra<W> {

    Higher<W,Account> debit(Account account, double amount);
    Higher<W, Account> credit(Account account, double amount);

}
