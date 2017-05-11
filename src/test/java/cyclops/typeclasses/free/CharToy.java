package cyclops.typeclasses.free;

import com.aol.cyclops2.hkt.Higher;
import cyclops.control.either.Either3;
import cyclops.function.Fn1;
import cyclops.function.Fn2;
import cyclops.typeclasses.functor.Functor;

import java.util.function.Function;

//CharToy from https://github.com/xuwei-k/free-monad-java
abstract class CharToy<A> implements Higher<CharToy.µ, A> {
    public static class µ {
    }

    public abstract Either3<CharOutput<A>,CharBell<A>,CharDone<A>> match();


    public static <T> CharToy<T> narrowK(Higher<CharToy.µ, T> wide){
        return (CharToy<T>)wide;
    }


    public static Free<CharToy.µ, String> output(final char a){
        return Free.liftF(new CharOutput<>(a, null), functor);
    }
    public static Free<CharToy.µ, Void> bell(){
        return Free.liftF(new CharBell<Void>(null), functor);
    }
    public static Free<CharToy.µ, Void> done(){
        return Free.liftF(new CharDone<Void>(), functor);
    }
    public static <A> Free<CharToy.µ, A> pointed(final A a){
        return Free.done(a);
    }

    public abstract <B> CharToy<B> map(Fn1<A, B> f);
    private CharToy(){}


    public static final Functor<CharToy.µ> functor =
         new Functor<CharToy.µ>() {
                @Override
                public <X, Y> Higher<CharToy.µ, Y> map(Function<? super X,? extends Y> f, Higher<CharToy.µ, X> fa) {
                    return narrowK(fa).map(a->f.apply(a));
                }
            };

    static final class CharOutput<A> extends CharToy<A>{
        private final char a;
        private final A next;
        private CharOutput(final char a, final A next) {
            this.a = a;
            this.next = next;
        }

        @Override
        public Either3<CharOutput<A>, CharBell<A>, CharDone<A>> match() {
            return Either3.left1(this);
        }


        public <Z> Z visit(final Fn2<Character, A, Z> output) {
            return output.apply(a, next);
        }

        @Override
        public <B> CharToy<B> map(final Fn1<A, B> f) {
            return new CharOutput<>(a, f.apply(next));
        }
    }

   static final class CharBell<A> extends CharToy<A> {
        private final A next;
        private CharBell(final A next) {
            this.next = next;
        }

        @Override
        public Either3<CharOutput<A>, CharBell<A>, CharDone<A>> match() {
            return Either3.left2(this);
        }


        public <Z> Z visit(final Fn1<A, Z> bell) {
            return bell.apply(next);
        }

        @Override
        public <B> CharToy<B> map(final Fn1<A, B> f) {
            return new CharBell<>(f.apply(next));
        }
    }

     static final class CharDone<A> extends CharToy<A> {
        @Override
        public Either3<CharOutput<A>, CharBell<A>, CharDone<A>> match() {
            return Either3.right(this);
        }


        public <Z> Z visit(final Z done) {
            return done;
        }

        @Override
        public <B> CharToy<B> map(final Fn1<A, B> f) {
            return new CharDone<>();
        }
    }
}
