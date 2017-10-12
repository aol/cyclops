package cyclops.function;

import cyclops.control.anym.AnyM;
import cyclops.control.anym.WitnessType;
import cyclops.control.anym.function.AnyMFunction1;

import java.util.function.UnaryOperator;


public interface UnaryAnyMFunction<W extends WitnessType<W>,T> extends UnaryOperator<AnyM<W,T>>, AnyMFunction1<W,T,T> {
}
