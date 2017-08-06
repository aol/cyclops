package com.aol.cyclops2.internal.stream;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.stream.Streamable;
import com.aol.cyclops2.data.collections.extensions.CollectionX;

public class SeqUtils {
    /**
     * Reverse a Stream
     * 
     * <pre>
     * {@code 
     * assertThat(StreamUtils.reverse(Stream.of(1,2,3)).collect(CyclopsCollectors.toList())
    			,equalTo(Arrays.asList(3,2,1)));
     * }
     * </pre>
     * 
     * @param stream Stream to reverse
     * @return Reversed reactiveStream
     */
    public static <U> Stream<U> reverse(final Stream<U> stream) {
        return reversedStream(stream.collect(Collectors.toList()));
    }

    /**
     * Create a reversed Stream from a List
     * <pre>
     * {@code 
     * StreamUtils.reversedStream(asList(1,2,3))
    			.map(i->i*100)
    			.forEach(System.out::println);
    	
    	
    	assertThat(StreamUtils.reversedStream(Arrays.asList(1,2,3)).collect(CyclopsCollectors.toList())
    			,equalTo(Arrays.asList(3,2,1)));
     * 
     * }
     * </pre>
     * 
     * @param list List to create a reversed Stream from
     * @return Reversed Stream
     */
    public static <U> Stream<U> reversedStream(final List<U> list) {
        return new ReversedIterator<>(
                                      list).stream();
    }

    /**
     * Create a Stream that finitely cycles the provided Streamable, provided number of times
     * 
     * <pre>
     * {@code 
     * assertThat(StreamUtils.cycle(3,Streamable.of(1,2,2))
    							.collect(CyclopsCollectors.toList()),
    								equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
     * }
     * </pre>
     * @param s Streamable to cycle
     * @return New cycling reactiveStream
     */
    public static <U> Stream<U> cycle(final int times, final Streamable<U> s) {
        return Stream.iterate(s.stream(), s1 -> s.stream())
                     .limit(times)
                     .flatMap(Function.identity());
    }

    /**
      * Projects an immutable toX of this reactiveStream. Initial iteration over the toX is not thread safe
      * (can't be performed by multiple threads concurrently) subsequent iterations are.
      *
      * @return An immutable toX of this reactiveStream.
      */
    public static final <A> CollectionX<A> toLazyCollection(final Stream<A> stream) {
        return toLazyCollection(stream.iterator());
    }

    public static final <A> CollectionX<A> toLazyCollection(final Iterator<A> iterator) {
        return toLazyCollection(iterator, false);
    }

    /**
     * Lazily constructs a Collection from specified Stream. Collections iterator may be safely used
     * concurrently by multiple threads.
    */
    public static final <A> CollectionX<A> toConcurrentLazyCollection(final Stream<A> stream) {
        return toConcurrentLazyCollection(stream.iterator());
    }

    public static final <A> CollectionX<A> toConcurrentLazyCollection(final Iterator<A> iterator) {
        return toLazyCollection(iterator, true);
    }

    private static final <A> CollectionX<A> toLazyCollection(final Iterator<A> iterator, final boolean concurrent) {
        return CollectionX.fromCollection(createLazyCollection(iterator, concurrent));

    }

    private static final <A> Collection<A> createLazyCollection(final Iterator<A> iterator, final boolean concurrent) {
        return new AbstractCollection<A>() {

            @Override
            public boolean equals(final Object o) {
                if (o == null)
                    return false;
                if (!(o instanceof Collection))
                    return false;
                final Collection<A> c = (Collection) o;
                final Iterator<A> it1 = iterator();
                final Iterator<A> it2 = c.iterator();
                while (it1.hasNext()) {
                    if (!it2.hasNext())
                        return false;
                    if (!Objects.equals(it1.next(), it2.next()))
                        return false;
                }
                if (it2.hasNext())
                    return false;
                return true;
            }

            @Override
            public int hashCode() {
                final Iterator<A> it1 = iterator();
                final List<A> arrayList = new ArrayList<>();
                while (it1.hasNext()) {
                    arrayList.add(it1.next());
                }
                return Objects.hashCode(arrayList.toArray());
            }

            List<A> data = new ArrayList<>();

            volatile boolean complete = false;

            Object lock = new Object();
            ReentrantLock rlock = new ReentrantLock();

            @Override
            public Iterator<A> iterator() {
                if (complete)
                    return data.iterator();
                return new Iterator<A>() {
                    int current = -1;

                    @Override
                    public boolean hasNext() {

                        if (concurrent) {

                            rlock.lock();
                        }
                        try {

                            if (current == data.size() - 1 && !complete) {
                                final boolean result = iterator.hasNext();
                                complete = !result;


                                return result;
                            }
                            if (current + 1 < data.size()) {

                                return true;
                            }
                            return false;
                        } finally {
                            if (concurrent)
                                rlock.unlock();
                        }
                    }

                    @Override
                    public A next() {

                        if (concurrent) {

                            rlock.lock();
                        }
                        try {
                            if (current < data.size() && !complete) {
                                if (iterator.hasNext())
                                    data.add(iterator.next());

                                return data.get(++current);
                            }
                            current++;
                            return data.get(current);
                        } finally {

                            if (concurrent)
                                rlock.unlock();
                        }

                    }

                };

            }

            @Override
            public int size() {
                if (complete)
                    return data.size();
                final Iterator it = iterator();
                while (it.hasNext())
                    it.next();

                return data.size();
            }
        };
    }
}
