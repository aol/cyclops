package cyclops.control;

import com.oath.cyclops.types.OrElseValue;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public abstract class AbstractOrElseValueTest {

    public abstract OrElseValue<Integer,OrElseValue<Integer,?>> of(int value);
    public abstract OrElseValue<Integer,OrElseValue<Integer,?>> empty();

    @Test
    public void onEmptySwitch_switchesOnEmpty(){
       assertThat(empty().recoverWith(()->of(1)),equalTo(of(1)));
    }
    @Test
    public void onEmptySwitch_doesntswitchesWhenNotEmpty(){
        assertThat(of(1).recoverWith(()->of(2)),equalTo(of(1)));
    }
}
