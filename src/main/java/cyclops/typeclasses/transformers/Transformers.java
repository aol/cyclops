package cyclops.typeclasses.transformers;


import com.oath.cyclops.hkt.Higher;
import cyclops.companion.Optionals.OptionalKind;
import cyclops.control.*;
import cyclops.control.Maybe;
import cyclops.monads.Witness.*;
import cyclops.typeclasses.Nested;
import cyclops.typeclasses.monad.Monad;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.Function;

public interface Transformers {
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class MaybeTransformer<W1,T> implements Transformer<W1,maybe,T>{
        private final Nested<W1,maybe,T> nested;
        private final Monad<W1> monad1;

        private final  static <W1> TransformerFactory<W1,maybe> factory(){
            return MaybeTransformer::maybeT;
        }
        public static <W1,T> MaybeTransformer<W1,T> maybeT(Nested<W1,maybe,T> nested){
            return new MaybeTransformer<W1,T>(nested,nested.def1.monad());
        }
        @Override
        public <R> Nested<W1, maybe, R> flatMap(Function<? super T, ? extends Nested<W1, maybe, R>> fn) {
            Higher<W1, Higher<maybe, R>> r = monad1.flatMap(m -> Maybe.narrowK(m).visit(t -> fn.apply(t).nested,
                    () -> monad1.unit(Maybe.nothing())),
                    nested.nested);



            return Nested.of(r, nested.def1, nested.def2);



        }

        @Override
        public <R> Nested<W1, maybe, R> flatMapK(Function<? super T, ? extends Higher<W1, Higher<maybe, R>>> fn) {
            return flatMap(fn.andThen(x->Nested.of(x,nested.def1,nested.def2)));
        }


    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class OptionalTransformer<W1,T> implements Transformer<W1,optional,T>{
        private final Nested<W1,optional,T> nested;
        private final Monad<W1> monad1;

        private final  static <W1> TransformerFactory<W1,optional> factory(){
            return OptionalTransformer::optionalT;
        }
        public static <W1,T> OptionalTransformer<W1,T> optionalT(Nested<W1,optional,T> nested){
            return new OptionalTransformer<W1,T>(nested,nested.def1.monad());
        }
        @Override
        public <R> Nested<W1, optional, R> flatMap(Function<? super T, ? extends Nested<W1, optional, R>> fn) {
            Higher<W1, Higher<optional, R>> r = monad1.flatMap(m -> OptionalKind.narrow(m)
                                                                                .map(t -> fn.apply(t).nested)
                                                                                .orElseGet(() -> monad1.unit(OptionalKind.empty())),
                    nested.nested);



            return Nested.of(r, nested.def1, nested.def2);



        }

        @Override
        public <R> Nested<W1, optional, R> flatMapK(Function<? super T, ? extends Higher<W1, Higher<optional, R>>> fn) {
            return flatMap(fn.andThen(x->Nested.of(x,nested.def1,nested.def2)));
        }


    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class XorTransformer<W1,L,R> implements Transformer<W1,Higher<either,L>,R>{
        private final Nested<W1,Higher<either,L>,R> nested;
        private final Monad<W1> monad1;

        private final  static <W1,L> TransformerFactory<W1,Higher<either,L>> factory(){
            return XorTransformer::xorT;
        }
        public static <W1,L,R> XorTransformer<W1,L,R> xorT(Nested<W1,Higher<either,L>,R> nested){
            return new XorTransformer<W1,L,R>(nested,nested.def1.monad());
        }


        @Override
        public <R1> Nested<W1, Higher<either, L>, R1> flatMap(Function<? super R, ? extends Nested<W1, Higher<either, L>, R1>> fn) {
            Higher<W1, Higher<Higher<either, L>, R1>> res = monad1.flatMap(m -> Either.narrowK(m).visit(l -> monad1.unit(Either.left(l)),

                    r -> fn.apply(r).nested),
                    nested.nested);

            return Nested.of(res, nested.def1, nested.def2);
        }

        @Override
        public <R1> Nested<W1, Higher<either, L>, R1> flatMapK(Function<? super R, ? extends Higher<W1, Higher<Higher<either, L>, R1>>> fn) {
            return flatMap(fn.andThen(x->Nested.of(x,nested.def1,nested.def2)));
        }
    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class IdentityTransformer<W1,T> implements Transformer<W1,identity,T>{
        private final Nested<W1,identity,T> nested;
        private final Monad<W1> monad1;

        private final  static <W1> TransformerFactory<W1,identity> factory(){
            return IdentityTransformer::identityT;
        }
        public static <W1,T> IdentityTransformer<W1,T> identityT(Nested<W1,identity,T> nested){
            return new IdentityTransformer<>(nested,nested.def1.monad());
        }
        @Override
        public <R> Nested<W1, identity, R> flatMap(Function<? super T, ? extends Nested<W1, identity, R>> fn) {
            Higher<W1, Higher<identity, R>> r = monad1.flatMap(m -> Identity.narrowK(m).visit(t -> fn.apply(t).nested),
                    nested.nested);

            return Nested.of(r, nested.def1, nested.def2);



        }
        @Override
        public <R> Nested<W1, identity, R> flatMapK(Function<? super T, ? extends Higher<W1, Higher<identity, R>>> fn) {
            return flatMap(fn.andThen(x->Nested.of(x,nested.def1,nested.def2)));
        }

    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class TryTransformer<W1,X extends Throwable,T> implements Transformer<W1,Higher<tryType,X>,T>{
        private final Nested<W1,Higher<tryType,X>,T> nested;
        private final Monad<W1> monad1;

        private final  static <W1,X extends Throwable,T> TransformerFactory<W1,Higher<tryType,X>> factory(){
            return TryTransformer::tryT;
        }
        public static <W1,X extends Throwable,T> TryTransformer<W1,X,T> tryT(Nested<W1,Higher<tryType,X>,T> nested){
            return new TryTransformer<>(nested,nested.def1.monad());
        }


        @Override
        public <R1> Nested<W1, Higher<tryType, X>, R1> flatMap(Function<? super T, ? extends Nested<W1, Higher<tryType, X>, R1>> fn) {
            Higher<W1, Higher<Higher<tryType, X>, R1>> res = monad1.flatMap(m -> Try.narrowK(m).visit(r -> fn.apply(r).nested,l -> monad1.unit(Try.failure(l))),
                    nested.nested);

            return Nested.of(res, nested.def1, nested.def2);
        }

        @Override
        public <R> Nested<W1, Higher<tryType, X>, R> flatMapK(Function<? super T, ? extends Higher<W1, Higher<Higher<tryType, X>, R>>> fn) {
            return flatMap(fn.andThen(x->Nested.of(x,nested.def1,nested.def2)));
        }


    }
}
