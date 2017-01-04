package cyclops.higherkindedtypes;

import com.aol.cyclops2.hkt.Higher;

        import java.util.Optional;
import java.util.function.*;


import cyclops.Monoids;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
        import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

/**
 * Simulates Higher Kinded Types for Optional's
 *
 * OptionalKind is a Optional and a Higher Kinded Type (OptionalKind.µ,T)
 *
 * @author johnmcclean
 *
 * @param <T> Data type stored within the Optional
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptionalKind<T> implements Higher<OptionalKind.µ, T> {
    private final Optional<T> boxed;

    /**
     * Witness type
     *
     * @author johnmcclean
     *
     */
    public static class µ {
    }
    /**
     * @return An HKT encoded empty Optional
     */
    public static <T> OptionalKind<T> empty() {
        return widen(Optional.empty());
    }
    /**
     * @param value Value to embed in an Optional
     * @return An HKT encoded Optional
     */
    public static <T> OptionalKind<T> of(T value) {
        return widen(Optional.of(value));
    }
    /**
     * Convert a Optional to a simulated HigherKindedType that captures Optional nature
     * and Optional element data type separately. Recover via @see OptionalKind#narrow
     *
     * If the supplied Optional implements OptionalKind it is returned already, otherwise it
     * is wrapped into a Optional implementation that does implement OptionalKind
     *
     * @param Optional Optional to widen to a OptionalKind
     * @return OptionalKind encoding HKT info about Optionals
     */
    public static <T> OptionalKind<T> widen(final Optional<T> Optional) {

        return new OptionalKind<T>(Optional);
    }
    /**
     * Convert the raw Higher Kinded Type for OptionalKind types into the OptionalKind type definition class
     *
     * @param future HKT encoded list into a OptionalKind
     * @return OptionalKind
     */
    public static <T> OptionalKind<T> narrowK(final Higher<OptionalKind.µ, T> future) {
        return (OptionalKind<T>)future;
    }
    /**
     * Convert the HigherKindedType definition for a Optional into
     *
     * @param Optional Type Constructor to convert back into narrowed type
     * @return Optional from Higher Kinded Type
     */
    public static <T> Optional<T> narrow(final Higher<OptionalKind.µ, T> Optional) {
        //has to be an OptionalKind as only OptionalKind can implement Higher<OptionalKind.µ, T>
        return ((OptionalKind<T>)Optional).boxed;

    }
    public boolean isPresent() {
        return boxed.isPresent();
    }
    public T get() {
        return boxed.get();
    }
    public void ifPresent(Consumer<? super T> consumer) {
        boxed.ifPresent(consumer);
    }
    public Optional<T> filter(Predicate<? super T> predicate) {
        return boxed.filter(predicate);
    }
    public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        return boxed.map(mapper);
    }
    public <U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
        return boxed.flatMap(mapper);
    }
    public T orElse(T other) {
        return boxed.orElse(other);
    }
    public T orElseGet(Supplier<? extends T> other) {
        return boxed.orElseGet(other);
    }
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        return boxed.orElseThrow(exceptionSupplier);
    }
    public boolean equals(Object obj) {
        return boxed.equals(obj);
    }
    public int hashCode() {
        return boxed.hashCode();
    }
    public String toString() {
        return boxed.toString();
    }


    /**
     * Companion class for creating Type Class instances for working with Optionals
     * @author johnmcclean
     *
     */
    @UtilityClass
    public static class OptionalInstances {


        /**
         *
         * Transform a list, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  OptionalKind<Integer> list = Optionals.functor().map(i->i*2, OptionalKind.widen(Arrays.asOptional(1,2,3));
         *
         *  //[2,4,6]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with Optionals
         * <pre>
         * {@code
         *   OptionalKind<Integer> list = Optionals.unit()
        .unit("hello")
        .then(h->Optionals.functor().map((String v) ->v.length(), h))
        .convert(OptionalKind::narrowK);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Optionals
         */
        public static <T,R>Functor<µ> functor(){
            BiFunction<OptionalKind<T>,Function<? super T, ? extends R>,OptionalKind<R>> map = OptionalInstances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * OptionalKind<String> list = Optionals.unit()
        .unit("hello")
        .convert(OptionalKind::narrowK);

        //Arrays.asOptional("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Optionals
         */
        public static <T> Pure<µ> unit(){
            return General.<OptionalKind.µ,T>unit(OptionalInstances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.OptionalKind.widen;
         * import static com.aol.cyclops.util.function.Lambda.l1;
         * import static java.util.Arrays.asOptional;
         *
        Optionals.zippingApplicative()
        .ap(widen(asOptional(l1(this::multiplyByTwo))),widen(asOptional(1,2,3)));
         *
         * //[2,4,6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * OptionalKind<Function<Integer,Integer>> listFn =Optionals.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(OptionalKind::narrowK);

        OptionalKind<Integer> list = Optionals.unit()
        .unit("hello")
        .then(h->Optionals.functor().map((String v) ->v.length(), h))
        .then(h->Optionals.applicative().ap(listFn, h))
        .convert(OptionalKind::narrowK);

        //Arrays.asOptional("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Optionals
         */
        public static <T,R> Applicative<OptionalKind.µ> applicative(){
            BiFunction<OptionalKind< Function<T, R>>,OptionalKind<T>,OptionalKind<R>> ap = OptionalInstances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.OptionalKind.widen;
         * OptionalKind<Integer> list  = Optionals.monad()
        .flatMap(i->widen(OptionalX.range(0,i)), widen(Arrays.asOptional(1,2,3)))
        .convert(OptionalKind::narrowK);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    OptionalKind<Integer> list = Optionals.unit()
        .unit("hello")
        .then(h->Optionals.monad().flatMap((String v) ->Optionals.unit().unit(v.length()), h))
        .convert(OptionalKind::narrowK);

        //Arrays.asOptional("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Optionals
         */
        public static <T,R> Monad<µ> monad(){

            BiFunction<Higher<OptionalKind.µ,T>,Function<? super T, ? extends Higher<OptionalKind.µ,R>>,Higher<OptionalKind.µ,R>> flatMap = OptionalInstances::flatMap;
            return General.monad(applicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  OptionalKind<String> list = Optionals.unit()
        .unit("hello")
        .then(h->Optionals.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(OptionalKind::narrowK);

        //Arrays.asOptional("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<µ> monadZero(){

            return General.monadZero(monad(), OptionalKind.empty());
        }
        /**
         * <pre>
         * {@code
         *  OptionalKind<Integer> list = Optionals.<Integer>monadPlus()
        .plus(OptionalKind.widen(Arrays.asOptional()), OptionalKind.widen(Arrays.asOptional(10)))
        .convert(OptionalKind::narrowK);
        //Arrays.asOptional(10))
         *
         * }
         * </pre>
         * @return Type class for combining Optionals by concatenation
         */
        public static <T> MonadPlus<OptionalKind.µ> monadPlus(){
            Monoid<Optional<T>> mn = Monoids.firstPresentOptional();
            Monoid<OptionalKind<T>> m = Monoid.of(OptionalKind.widen(mn.zero()), (f, g)-> OptionalKind.widen(
                    mn.apply(OptionalKind.narrow(f), OptionalKind.narrow(g))));

            Monoid<Higher<OptionalKind.µ,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<OptionalKind<Integer>> m = Monoid.of(OptionalKind.widen(Arrays.asOptional()), (a,b)->a.isEmpty() ? b : a);
        OptionalKind<Integer> list = Optionals.<Integer>monadPlus(m)
        .plus(OptionalKind.widen(Arrays.asOptional(5)), OptionalKind.widen(Arrays.asOptional(10)))
        .convert(OptionalKind::narrowK);
        //Arrays.asOptional(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining Optionals
         * @return Type class for combining Optionals
         */
        public static <T> MonadPlus<µ> monadPlus(Monoid<OptionalKind<T>> m){
            Monoid<Higher<OptionalKind.µ,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<µ> traverse(){

            return General.traverseByTraverse(applicative(), OptionalInstances::traverseA);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = Optionals.foldable()
        .foldLeft(0, (a,b)->a+b, OptionalKind.widen(Arrays.asOptional(1,2,3,4)));

        //10
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T> Foldable<µ> foldable(){
            BiFunction<Monoid<T>,Higher<OptionalKind.µ,T>,T> foldRightFn =  (m, l)-> OptionalKind.narrow(l).orElse(m.zero());
            BiFunction<Monoid<T>,Higher<µ,T>,T> foldLeftFn = (m, l)-> OptionalKind.narrow(l).orElse(m.zero());
            return General.foldable(foldRightFn, foldLeftFn);
        }
        public static <T> Comonad<µ> comonad(){
            Function<? super Higher<OptionalKind.µ, T>, ? extends T> extractFn = maybe -> maybe.convert(OptionalKind::narrow).get();
            return General.comonad(functor(), unit(), extractFn);
        }

        private <T> OptionalKind<T> of(T value){
            return OptionalKind.widen(Optional.of(value));
        }
        private static <T,R> OptionalKind<R> ap(OptionalKind<Function< T, R>> lt, OptionalKind<T> list){
            return OptionalKind.widen(Maybe.fromOptionalKind(lt).combine(Maybe.fromOptionalKind(list), (a, b)->a.apply(b)).toOptional());

        }
        private static <T,R> Higher<OptionalKind.µ,R> flatMap(Higher<OptionalKind.µ,T> lt, Function<? super T, ? extends  Higher<OptionalKind.µ,R>> fn){
            return OptionalKind.widen(OptionalKind.narrow(lt).flatMap(fn.andThen(OptionalKind::narrow)));
        }
        private static <T,R> OptionalKind<R> map(OptionalKind<T> lt, Function<? super T, ? extends R> fn){
            return OptionalKind.widen(OptionalKind.narrow(lt).map(fn));
        }


        private static <C2,T,R> Higher<C2, Higher<OptionalKind.µ, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,
                                                                                Higher<OptionalKind.µ, T> ds){
            Optional<T> opt = OptionalKind.narrow(ds);
            return opt.isPresent() ?   applicative.map(OptionalKind::of, fn.apply(opt.get())) :
                    applicative.unit(OptionalKind.empty());
        }

    }

}