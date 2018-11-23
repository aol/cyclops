package cyclops.typeclasses.taglessfinal;

import lombok.Value;
import lombok.experimental.Wither;

public class Cases {
    @Value @Wither
    public static class Account {
        double balance;
        long id;

        public Account debit(double amount){
            return withBalance(balance-amount);
        }
        public Account credit(double amount){
            return withBalance(balance+amount);
        }
    }





}
