package cyclops.function;

import cyclops.companion.Monoids;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;


public class MonoidTest {
    @Test
    public void visit() throws Exception {

      int res = Monoids.intSum.visit((fn,z)-> {
            if(z==0){
                return fn.apply(1,2);
            }
            else
                return fn.apply(10,20);
        });

      assertThat(res,equalTo(3));


    }

}