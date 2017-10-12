package cyclops.control.anym.function;

import cyclops.control.anym.AnyM;
import cyclops.control.anym.WitnessType;

import java.util.function.BinaryOperator;


public interface BinaryAnyMFunction<W extends WitnessType<W>,T> extends BinaryOperator<AnyM<W,T>>, AnyMFunction2<W,T,T,T> {
}
