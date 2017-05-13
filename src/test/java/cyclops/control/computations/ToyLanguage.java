package cyclops.control.computations;

import com.aol.cyclops2.types.Transformable;
import cyclops.control.Computations;
import cyclops.control.either.Either3;
import cyclops.function.Fn1;
import cyclops.function.Fn2;
import cyclops.typeclasses.free.Free;

import java.util.function.Function;

//ToyLanguage from https://github.com/xuwei-k/free-monad-java
abstract class ToyLanguage<A> implements Transformable<A> {

    public abstract Either3<Output<A>,Bell<A>,Done<A>> match();


    public static <T> ToyLanguage<T> narrowK(Transformable<T> wide){
        return (ToyLanguage<T>)wide;
    }

    public final static <T> Function<Transformable<Computations<T>>,ToyLanguage<Computations<T>>> decoder() {
        return c->(ToyLanguage<Computations<T>>)c;
    }
    public static Computations<String> output(final char a){
        return Computations.liftF(new Output<>(a, null));
    }
    public static Computations<Void> bell(){
        return Computations.liftF(new Bell<>(null));
    }
    public static Computations<Void> done(){
        return Computations.liftF(new Done<Void>());
    }
    public static <A> Computations<A> pointed(final A a){
        return Computations.done(a);
    }


    private ToyLanguage(){}




    static final class Output<A> extends ToyLanguage<A> {
        private final char a;
        private final A next;
        private Output(final char a, final A next) {
            this.a = a;
            this.next = next;
        }

        @Override
        public Either3<Output<A>, Bell<A>, Done<A>> match() {
            return Either3.left1(this);
        }


        public <Z> Z visit(final Fn2<Character, A, Z> output) {
            return output.apply(a, next);
        }

        @Override
        public <B> ToyLanguage<B> map(final Function<? super A,? extends  B> f) {
            return new Output<>(a, f.apply(next));
        }
    }

   static final class Bell<A> extends ToyLanguage<A> {
        private final A next;
        private Bell(final A next) {
            this.next = next;
        }

        @Override
        public Either3<Output<A>, Bell<A>, Done<A>> match() {
            return Either3.left2(this);
        }


        public <Z> Z visit(final Fn1<A, Z> bell) {
            return bell.apply(next);
        }

        @Override
        public <B> ToyLanguage<B> map(final Function<? super A,? extends  B> f) {
            return new Bell<>(f.apply(next));
        }
    }

     static final class Done<A> extends ToyLanguage<A> {
        @Override
        public Either3<Output<A>, Bell<A>, Done<A>> match() {
            return Either3.right(this);
        }


        public <Z> Z visit(final Z done) {
            return done;
        }

        @Override
        public <B> ToyLanguage<B> map(final Function<? super A,? extends  B> f) {
            return new Done<>();
        }
    }
}
