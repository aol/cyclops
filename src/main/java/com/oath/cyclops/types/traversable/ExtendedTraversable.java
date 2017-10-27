package com.oath.cyclops.types.traversable;

import cyclops.reactive.ReactiveSeq;

/**
 * Represents a finite traversable type
 *
 *
 *
 * @author johnmcclean
 *
 * @param <T> Data type of elements in this ExtendedTraversable
 */
public interface ExtendedTraversable<T> extends Traversable<T> {

    /**
     * Generate the permutations based on values in the ExtendedTraversable.
     *
     *
     * @return Permutations from this ExtendedTraversable
     */
    default ExtendedTraversable<ReactiveSeq<T>> permutations() {
        return stream().permutations();
    }

    /**
     *  Generate the combinations based on values in the ExtendedTraversable.
     *
     * <pre>
     * {@code
     *   ExtendedTraversable<Integer> reactiveStream = ReactiveSeq.of(1,2,3);
     *   reactiveStream.combinations(2)
     *
     *   //ReactiveSeq[ReactiveSeq[1,2],ReactiveSeq[1,3],ReactiveSeq[2,3]]
     * }
     * </pre>
     *
     *
     * @param size
     *            of combinations
     * @return All combinations of the elements in this ExtendedTraversable of the specified
     *         size
     */
    default ExtendedTraversable<ReactiveSeq<T>> combinations(final int size) {
        return stream().combinations(size);
    }

    /**
     * Generate the combinations based on values in the ExtendedTraversable.
     *
     * <pre>
     * {@code
     *   ReactiveSeq.of(1,2,3).combinations()
     *
     *   //ReactiveSeq[ReactiveSeq[],ReactiveSeq[1],ReactiveSeq[2],ReactiveSeq[3].ReactiveSeq[1,2],ReactiveSeq[1,3],ReactiveSeq[2,3]
     *   			,ReactiveSeq[1,2,3]]
     * }
     * </pre>
     *
     *
     * @return All combinations of the elements in this ExtendedTraversable
     */
    default ExtendedTraversable<ReactiveSeq<T>> combinations() {
        return stream().combinations();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#reactiveStream()
     */
    @Override
    default ReactiveSeq<T> stream() {

        return Traversable.super.stream();
    }

}
