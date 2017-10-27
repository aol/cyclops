package cyclops.control.computations;

import com.oath.cyclops.types.functor.Transformable;
import cyclops.control.Unrestricted;
import cyclops.control.LazyEither3;
import cyclops.function.Function1;
import cyclops.function.Function2;

import java.util.function.Function;

//ToyLanguage from https://github.com/xuwei-k/free-monad-java
abstract class ToyLanguage<A> implements Transformable<A> {

    public abstract LazyEither3<Output<A>,Bell<A>,Done<A>> match();


    public static <T> ToyLanguage<T> narrowK(Transformable<T> wide){
        return (ToyLanguage<T>)wide;
    }

    public final static <T> Function<Transformable<Unrestricted<T>>,ToyLanguage<Unrestricted<T>>> decoder() {
        return c->(ToyLanguage<Unrestricted<T>>)c;
    }
    public static Unrestricted<String> output(final char a){
        return Unrestricted.liftF(new Output<>(a, null));
    }
    public static Unrestricted<Void> bell(){
        return Unrestricted.liftF(new Bell<>(null));
    }
    public static Unrestricted<Void> done(){
        return Unrestricted.liftF(new Done<Void>());
    }
    public static <A> Unrestricted<A> pointed(final A a){
        return Unrestricted.done(a);
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
        public LazyEither3<Output<A>, Bell<A>, Done<A>> match() {
            return LazyEither3.left1(this);
        }


        public <Z> Z visit(final Function2<Character, A, Z> output) {
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
        public LazyEither3<Output<A>, Bell<A>, Done<A>> match() {
            return LazyEither3.left2(this);
        }


        public <Z> Z visit(final Function1<A, Z> bell) {
            return bell.apply(next);
        }

        @Override
        public <B> ToyLanguage<B> map(final Function<? super A,? extends  B> f) {
            return new Bell<>(f.apply(next));
        }
    }

     static final class Done<A> extends ToyLanguage<A> {
        @Override
        public LazyEither3<Output<A>, Bell<A>, Done<A>> match() {
            return LazyEither3.right(this);
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
