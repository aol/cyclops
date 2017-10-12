package cyclops.companion;

import com.aol.cyclops2.types.traversable.IterableX;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.reactive.ReactiveSeq;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.function.BiFunction;
import java.util.function.Function;

@UtilityClass
public class Iterables {

    public static <T> IterableX<T> fromIterable(Iterable<T> t) {
        if (t instanceof IterableX)
            return (IterableX<T>) t;
        return () -> t.iterator();
    }

    @AllArgsConstructor
    public static class IterableComprehension<T> {
        private final Iterable<T> it;
        public <R1, R2, R3,R> IterableX<R> forEach4(final Function<? super T, ? extends Iterable<R1>> iterable1,
                                                     final BiFunction<? super T,? super R1, ? extends Iterable<R2>> iterable2,
                                                     final Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3,
                                                     final Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
            return Iterables.forEach4(it,iterable1,iterable2,iterable3,yieldingFunction);

        }


        public <R1, R2, R3, R> IterableX<R> forEach4( final Function<? super T, ? extends Iterable<R1>> iterable1,
                                                               final BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2,
                                                               final Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3,
                                                               final Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                               final Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
            return Iterables.forEach4(it,iterable1,iterable2,iterable3,filterFunction,yieldingFunction);
        }


        public  <R1, R2, R> IterableX<R> forEach3( final Function<? super T, ? extends Iterable<R1>> iterable1,
                                                           final BiFunction<? super T,? super R1, ? extends Iterable<R2>> iterable2,
                                                           final Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
            return Iterables.forEach3(it,iterable1,iterable2,yieldingFunction);
        }



        public  <R1, R2, R> IterableX<R> forEach3( final Function<? super T, ? extends Iterable<R1>> iterable1,
                                                           final BiFunction<? super T,? super R1, ? extends Iterable<R2>> iterable2,
                                                           final Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                           final Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
            return Iterables.forEach3(it,iterable1,iterable2,filterFunction,yieldingFunction);
        }


        public  <R1, R> IterableX<R> forEach2( final Function<? super T,? extends Iterable<R1>> iterable1,
                                                       final BiFunction<? super T,? super R1, ? extends R> yieldingFunction) {

            return Iterables.forEach2(it,iterable1,yieldingFunction);
        }


        public <R1, R> IterableX<R> forEach2( final Function<? super T, ? extends Iterable<R1>> iterable1,
                                                       final BiFunction<? super T,? super R1,  Boolean> filterFunction,
                                                       final BiFunction<? super T,? super R1, ? extends R> yieldingFunction) {
            return Iterables.forEach2(it,iterable1,filterFunction,yieldingFunction);

        }

    }

    public static <T,R1, R2, R3,R> IterableX<R> forEach4(Iterable<T> host, final Function<? super T, ? extends Iterable<R1>> iterable1,
                                                         final BiFunction<? super T,? super R1, ? extends Iterable<R2>> iterable2,
                                                         final Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3,
                                                         final Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return fromIterable(host).concatMap(in -> {

            ReactiveSeq<R1> a = ReactiveSeq.fromIterable(iterable1.apply(in));
            return a.concatMap(ina -> {
                ReactiveSeq<R2> b = ReactiveSeq.fromIterable(iterable2.apply(in, ina));
                return b.concatMap(inb -> {
                    ReactiveSeq<R3> c = ReactiveSeq.fromIterable(iterable3.apply(in, ina, inb));
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }

    public static <T,R1, R2, R3, R> IterableX<R> forEach4( Iterable<T> host,final Function<? super T, ? extends Iterable<R1>> iterable1,
                                                           final BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2,
                                                           final Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3,
                                                           final Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                           final Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return fromIterable(host).concatMap(in -> {

            ReactiveSeq<R1> a = ReactiveSeq.fromIterable(iterable1.apply(in));
            return a.flatMap(ina -> {
                ReactiveSeq<R2> b = ReactiveSeq.fromIterable(iterable2.apply(in, ina));
                return b.flatMap(inb -> {
                    ReactiveSeq<R3> c = ReactiveSeq.fromIterable(iterable3.apply(in, ina, inb));
                    return c.filter(in2 -> filterFunction.apply(in, ina, inb, in2))
                            .map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }


    public static <T,R1, R2, R> IterableX<R> forEach3( Iterable<T> host,final Function<? super T, ? extends Iterable<R1>> iterable1,
                                                       final BiFunction<? super T,? super R1, ? extends Iterable<R2>> iterable2,
                                                       final Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return fromIterable(host).concatMap(in -> {

            Iterable<R1> a = iterable1.apply(in);
            return ReactiveSeq.fromIterable(a)
                    .flatMap(ina -> {
                        ReactiveSeq<R2> b = ReactiveSeq.fromIterable(iterable2.apply(in, ina));
                        return b.map(in2 -> yieldingFunction.apply(in, ina, in2));
                    });

        });
    }



    public static <T,R1, R2, R> IterableX<R> forEach3( Iterable<T> host,final Function<? super T, ? extends Iterable<R1>> iterable1,
                                                       final BiFunction<? super T,? super R1, ? extends Iterable<R2>> iterable2,
                                                       final Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                       final Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return fromIterable(host).concatMap(in -> {

            Iterable<R1> a = iterable1.apply(in);
            return ReactiveSeq.fromIterable(a)
                    .flatMap(ina -> {
                        ReactiveSeq<R2> b = ReactiveSeq.fromIterable(iterable2.apply(in, ina));
                        return b.filter(in2 -> filterFunction.apply(in, ina, in2))
                                .map(in2 -> yieldingFunction.apply(in, ina, in2));
                    });

        });
    }


    public static <T,R1, R> IterableX<R> forEach2( Iterable<T> host,final Function<? super T,? extends Iterable<R1>> iterable1,
                                            final BiFunction<? super T,? super R1, ? extends R> yieldingFunction) {

        return fromIterable(host).concatMap(in-> {

            Iterable<? extends R1> b = iterable1.apply(in);
            return ReactiveSeq.fromIterable(b)
                    .map(in2->yieldingFunction.apply(in, in2));
        });
    }


    public static <T,R1, R> IterableX<R> forEach2( Iterable<T> host,final Function<? super T, ? extends Iterable<R1>> iterable1,
                                            final BiFunction<? super T,? super R1,  Boolean> filterFunction,
                                            final BiFunction<? super T,? super R1, ? extends R> yieldingFunction) {
        return fromIterable(host).concatMap(in-> {

            Iterable<? extends R1> b = iterable1.apply(in);
            return ReactiveSeq.fromIterable(b)
                    .filter(in2-> filterFunction.apply(in,in2))
                    .map(in2->yieldingFunction.apply(in, in2));
        });

    }

}
