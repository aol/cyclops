package cyclops.control.validated;

import com.oath.cyclops.types.OrElseValue;
import cyclops.control.AbstractOrElseValueTest;
import cyclops.control.Option;
import cyclops.control.Validated;
import cyclops.data.NonEmptyList;

public class ValidatedOrElseValueTest extends AbstractOrElseValueTest {
    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> of(int value) {
        return (OrElseValue)Validated.valid(value);
    }


    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty2() {
        return (OrElseValue) Validated.invalid(NonEmptyList.of("error"));
    }

    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty3() {
        return (OrElseValue) Validated.invalid(NonEmptyList.of("error"));
    }

    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty4() {
        return (OrElseValue)Validated.invalid(NonEmptyList.of("error"));
    }

    @Override
    public OrElseValue<Integer, OrElseValue<Integer, ?>> empty1() {
        return (OrElseValue)Validated.invalid(NonEmptyList.of("error"));
    }

    @Override
    public boolean isLazy() {
        return false;
    }
}
