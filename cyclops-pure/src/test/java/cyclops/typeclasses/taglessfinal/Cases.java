package cyclops.typeclasses.taglessfinal;

import lombok.ToString;
import lombok.Value;
import lombok.With;

public class Cases {
    @Value @With @ToString
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
