package cyclops.free;

import com.aol.cyclops.hkt.Higher;
import cyclops.function.Fn1;
import cyclops.function.Fn2;
import cyclops.typeclasses.functor.Functor;

import java.util.function.Function;

//CharToy from https://github.com/aol/cyclops/blob/v4.0.1/cyclops-free-monad/src/main/java/com/aol/cyclops/monad/Free.java
abstract class CharToy<A> implements Higher<CharToy.µ, A> {
    static public final class Unit {
        private Unit(){}

        public static final Unit unit = new Unit();
    }

    public abstract <Z> Z fold(Fn2<Character, A, Z> output, Fn1<A, Z> bell, Z done);

    public static Free<CharToy.µ, Unit> output(final char a){
        return Free.liftF(new CharOutput<>(a, Unit.unit), functor);
    }
    public static Free<CharToy.µ, Unit> bell(){
        return Free.liftF(new CharBell<Unit>(Unit.unit), functor);
    }
    public static Free<CharToy.µ, Unit> done(){
        return Free.liftF(new CharDone<Unit>(), functor);
    }
    public static <A> Free<CharToy.µ, A> pointed(final A a){
        return Free.done(a);
    }
    public abstract <B> CharToy<B> map(Fn1<A, B> f);
    private CharToy(){}

    public static class µ {
    }

    public static final Functor<CharToy.µ> functor =
            new Functor<CharToy.µ>() {
                @Override
                public <X, Y> Higher<CharToy.µ, Y> map(Function<? super X,? extends Y> f, Higher<CharToy.µ, X> fa) {
                    return ((CharToy<X>)fa).map(a->f.apply(a));
                }
            };

    private static final class CharOutput<A> extends CharToy<A>{
        private final char a;
        private final A next;
        private CharOutput(final char a, final A next) {
            this.a = a;
            this.next = next;
        }

        @Override
        public <Z> Z fold(final Fn2<Character, A, Z> output, final Fn1<A, Z> bell, final Z done) {
            return output.apply(a, next);
        }

        @Override
        public <B> CharToy<B> map(final Fn1<A, B> f) {
            return new CharOutput<>(a, f.apply(next));
        }
    }

    private static final class CharBell<A> extends CharToy<A> {
        private final A next;
        private CharBell(final A next) {
            this.next = next;
        }

        @Override
        public <Z> Z fold(final Fn2<Character, A, Z> output, final Fn1<A, Z> bell, Z done) {
            return bell.apply(next);
        }

        @Override
        public <B> CharToy<B> map(final Fn1<A, B> f) {
            return new CharBell<>(f.apply(next));
        }
    }

    private static final class CharDone<A> extends CharToy<A> {
        @Override
        public <Z> Z fold(final Fn2<Character, A, Z> output, final Fn1<A, Z> bell, final Z done) {
            return done;
        }

        @Override
        public <B> CharToy<B> map(final Fn1<A, B> f) {
            return new CharDone<>();
        }
    }
}
