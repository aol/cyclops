package cyclops.data;

import org.junit.Test;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class BQTest {
    @Test
    public void fromStream(){
        System.out.println(BankersQueue.of(1,2,3));
        System.out.println(BankersQueue.fromStream(Stream.of(1,2,3)));
        BankersQueue.fromStream(Stream.of(1,2,3)).iterator();
        assertThat(BankersQueue.fromStream(Stream.of(1,2,3)),equalTo(BankersQueue.of(1,2,3)));
    }
}
