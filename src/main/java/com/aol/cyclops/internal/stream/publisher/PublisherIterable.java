package com.aol.cyclops.internal.stream.publisher;


import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;



/**
 * Reproduced with permission -> https://github.com/aol/cyclops-react/issues/395
 * Emits the contents of an Iterable source.
 * 
 * @author smaldini
 * @author akarnokd 
 *
 * @param <T> the value type
 */
public final class PublisherIterable<T>  implements Publisher<T> {
    
    
    static enum EmptySubscription implements Subscription{
        INSTANCE;
        @Override
        public void request(long n) {
          
            
        }

        @Override
        public void cancel() {
          
            
        }
        
    }
    /**
     * Calls onSubscribe on the target Subscriber with the empty instance followed by a call to onComplete.
     *
     * @param s
     */
    public static void complete(Subscriber<?> s) {
        s.onSubscribe(EmptySubscription.INSTANCE);
        s.onComplete();
    }
    public static void error(Subscriber<?> s, Throwable e) {
        s.onSubscribe(EmptySubscription.INSTANCE);
        s.onError(e);
    }
   
    
   
   
    public static long addCap(long a, long b) {
        long u = a + b;
        if (u < 0) {
            return Long.MAX_VALUE;
        }
        return u;
    }

    public static long multiplyCap(long a, long b) {
        long u = a * b;
        if (((a | b) >>> 31) != 0 && (b != 0 && u / b != a)) {
            return Long.MAX_VALUE;
        }
        return u;
    }

    /**
     * Atomically adds the value to the atomic variable, capping the sum at Long.MAX_VALUE
     * and returning the original value.
     * @param <T> the type of the parent class of the field
     * @param updater the field updater
     * @param instance the instance of the field to update
     * @param n the value to add, n > 0, not validated
     * @return the original value before the add
     */
    public static <T> long getAndAddCap(AtomicLongFieldUpdater<T> updater, T instance, long n) {
        for (; ; ) {
            long r = updater.get(instance);
            if (r == Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            long u = addCap(r, n);
            if (updater.compareAndSet(instance, r, u)) {
                return r;
            }
        }
    }
    final Iterable<? extends T> iterable;

    public PublisherIterable(Iterable<? extends T> iterable) {
        this.iterable = Objects.requireNonNull(iterable, "iterable");
    }

    @Override
    public void subscribe(Subscriber<? super T> s) {
        Iterator<? extends T> it;

        try {
            it = iterable.iterator();
        } catch (Throwable e) {
            error(s, e);
            return;
        }

        subscribe(s, it);
    }

  

    /**
     * Common method to take an Iterator as a source of values.
     *
     * @param s
     * @param it
     */
    static <T> void subscribe(Subscriber<? super T> s, Iterator<? extends T> it) {
        if (it == null) {
            error(s, new NullPointerException("The iterator is null"));
            return;
        }

        boolean b;

        try {
            b = it.hasNext();
        } catch (Throwable e) {
            error(s, e);
            return;
        }
        if (!b) {
           complete(s);
            return;
        }

        s.onSubscribe(new IterableSubscription<T>(s, it));
        
    }

    static final class IterableSubscription<T> implements Subscription{

        final Subscriber<? super T> actual;

        final Iterator<? extends T> iterator;

        volatile boolean cancelled;

        volatile long requested;
        @SuppressWarnings("rawtypes")
        static final AtomicLongFieldUpdater<IterableSubscription> REQUESTED =
          AtomicLongFieldUpdater.newUpdater(IterableSubscription.class, "requested");

        int state;
        
        /** Indicates that the iterator's hasNext returned true before but the value is not yet retrieved. */
        static final int STATE_HAS_NEXT_NO_VALUE = 0;
        /** Indicates that there is a value available in current. */
        static final int STATE_HAS_NEXT_HAS_VALUE = 1;
        /** Indicates that there are no more values available. */
        static final int STATE_NO_NEXT = 2;
        /** Indicates that the value has been consumed and a new value should be retrieved. */
        static final int STATE_CALL_HAS_NEXT = 3;
        
        T current;
        
        public IterableSubscription(Subscriber<? super T> actual, Iterator<? extends T> iterator) {
            this.actual = actual;
            this.iterator = iterator;
        }

        @Override
        public void request(long n) {
            if (n < 1) {
                actual.onError( new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0 but it was " + n));
                return;
                
            }
           
            if (getAndAddCap(REQUESTED, this, n) == 0) {
                if (n == Long.MAX_VALUE) {
                    fastPath();
                } else {
                    slowPath(n);
                }
            }
            
        }

        void slowPath(long n) {
            final Iterator<? extends T> a = iterator;
            final Subscriber<? super T> s = actual;

            long e = 0L;

            for (; ; ) {

                while (e != n) {
                    boolean b;

                    try {
                        b = a.hasNext();
                    } catch (Throwable ex) {
                        s.onError(ex);
                        return;
                    }

                    if (cancelled) {
                        return;
                    }

                    if (!b) {
                        s.onComplete();
                        return;
                    }
                    T t;

                    try {
                        t = a.next();
                    } catch (Throwable ex) {
                        s.onError(ex);
                        return;
                    }

                    if (cancelled) {
                        return;
                    }

                    if (t == null) {
                        s.onError(new NullPointerException("The iterator returned a null value"));
                        return;
                    }

                    s.onNext(t);

                    if (cancelled) {
                        return;
                    }

                   

                    e++;
                }

                n = requested;

                if (n == e) {
                    n = REQUESTED.addAndGet(this, -e);
                    if (n == 0L) {
                        return;
                    }
                    e = 0L;
                }
            }
        }

        void fastPath() {
            final Iterator<? extends T> a = iterator;
            final Subscriber<? super T> s = actual;

            for (; ; ) {

                if (cancelled) {
                    return;
                }

                T t;

                try {
                    t = a.next();
                } catch (Exception ex) {
                    s.onError(ex);
                    return;
                }

                if (cancelled) {
                    return;
                }

                if (t == null) {
                    s.onError(new NullPointerException("The iterator returned a null value"));
                    return;
                }

                s.onNext(t);

                if (cancelled) {
                    return;
                }

                boolean b;

                try {
                    b = a.hasNext();
                } catch (Exception ex) {
                    s.onError(ex);
                    return;
                }

                if (cancelled) {
                    return;
                }

                if (!b) {
                    s.onComplete();
                    return;
                }
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
        }

        
    }

  
}