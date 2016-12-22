package cyclops.function;

import java.util.function.BinaryOperator;


public interface BinaryFn<T> extends BinaryOperator<T>, Fn2<T,T,T> {
}
