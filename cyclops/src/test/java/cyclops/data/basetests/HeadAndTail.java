package cyclops.data.basetests;


import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.reactive.ReactiveSeq;
import cyclops.function.Memoize;

import lombok.AllArgsConstructor;

/**
 * A class that represents a lazily constructed Head and Tail from a non-scalar data type
 *
 * @author johnmcclean
 *
 * @param <T> Data type of elements in this Head and Tail
 */
@AllArgsConstructor
public class HeadAndTail<T> {
    private final Supplier<T> head;
    private final Supplier<ReactiveSeq<T>> tail;
    private final Supplier<Boolean> isHead;

    public static <T> HeadAndTail<T> headAndTail(Iterable<T> it){
        return new HeadAndTail<>(it.iterator());
    }
    /**
     * Construct a HeadAndTail from an Iterator
     *
     * @param it Iterator to construct head and tail from
     */
    public HeadAndTail(final Iterator<T> it) {
        isHead = Memoize.memoizeSupplier(() -> it.hasNext());
        head = Memoize.memoizeSupplier(() -> {
            if (isHead.get())
                return it.next();
            throw new NoSuchElementException();
        });
        tail = Memoize.memoizeSupplier(() -> {
            if (isHead.get())
                head.get();
            else
                return ReactiveSeq.empty();
            return ReactiveSeq.fromIterator(it);
        });

    }

    /**
     * @return true if the head is present
     */
    public boolean isHeadPresent() {
        return isHead.get();
    }

    /**
     * @return Head (first) value, will throw an exception if the head is not present
     */
    public T head() {
        return head.get();
    }

    /**
     * @return Optional.zero if the head is not present, otherwise an Optional containing the head
     */
    public Optional<T> headOptional() {
        return isHeadPresent() ? Optional.of(head()) : Optional.empty();

    }

    /**
     * @return Maybe.none if the head is not present, otherwise a Maybe.some containing the first value
     */
    public Maybe<T> headMaybe() {
        return isHeadPresent() ? Maybe.fromEval(Eval.later(head)) : Maybe.nothing();

    }

    /**
     * @return A Stream containing the Head if present
     */
    public ReactiveSeq<T> headStream() {
        return isHeadPresent() ? ReactiveSeq.of(head)
            .map(Supplier::get)
            : ReactiveSeq.empty();
    }

    /**
     * @return The tail
     */
    public ReactiveSeq<T> tail() {
        return tail.get();
    }

}
