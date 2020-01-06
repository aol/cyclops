package cyclops.control.option;

import com.oath.cyclops.types.OrElseValue;
import cyclops.control.AbstractOrElseValueTest;
import cyclops.control.Option;

public class OptionOrElseValueTest extends AbstractOrElseValueTest {
    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> of(int value) {
        return (OrElseValue)Option.<Integer>some(value);
    }


    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty2() {
        return (OrElseValue)Option.<Integer>none();
    }

    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty3() {
        return (OrElseValue)Option.<Integer>none();
    }

    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty4() {
        return (OrElseValue)Option.<Integer>none();
    }

    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty1() {
        return (OrElseValue)Option.<Integer>none();
    }

    @Override
    public boolean isLazy() {
        return false;
    }
}
