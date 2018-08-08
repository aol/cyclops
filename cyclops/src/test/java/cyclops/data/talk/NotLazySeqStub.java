package cyclops.data.talk;

import cyclops.control.Eval;
import cyclops.data.LazySeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotLazySeqStub<T>  {

    public final T head;
    public final Supplier<LazySeq<T>> tail;

}
