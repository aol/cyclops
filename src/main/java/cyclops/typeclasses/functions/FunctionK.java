package cyclops.typeclasses.functions;

import com.aol.cyclops2.hkt.Higher;
import cyclops.collections.mutable.ListX;
import cyclops.companion.Optionals;
import cyclops.control.Maybe;
import cyclops.function.Fn1;
import cyclops.monads.Witness;
import cyclops.monads.Witness.list;
import cyclops.monads.Witness.maybe;
import cyclops.monads.Witness.optional;
import cyclops.monads.Witness.reactiveSeq;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.NaturalTransformation;
import lombok.AllArgsConstructor;

import java.util.function.Function;


@AllArgsConstructor
public class FunctionK<W1,W2,T> implements Fn1<Higher<W1,T>,Higher<W2,T>> {
    Fn1<Higher<W1,T>,Higher<W2,T>> fn1;
    InstanceDefinitions<W2> def2;
    @Override
    public Higher<W2, T> apply(Higher<W1, T> a){
        return fn1.apply(a);
    }

    public InstanceDefinitions<W2> definitions(){
        return def2;
    }
    public NaturalTransformation<W1,W2> asNaturalTransformationUnsafe(){
        return new NaturalTransformation<W1, W2>() {
            @Override
            public <T2> Higher<W2, T2> apply(Higher<W1, T2> a) {
                return FunctionK.this.apply((Higher)a);
            }
        };
    }

    public static <W1,W2,T> FunctionK<W1,W2,T> of(Fn1<Higher<W1,T>,Higher<W2,T>> fn1,InstanceDefinitions<W2> def2){
        return new FunctionK<>(fn1,def2);
    }

    public static <W1,T> FunctionK<W1,W1,T> identity(InstanceDefinitions<W1> defs) {
        return of(i->i,defs);
    }

    static <T> FunctionK<reactiveSeq,maybe,T> streamMaybe(){
        return of(i -> ReactiveSeq.narrowK(i).headAndTail().headMaybe(), Maybe.Instances.definitions());
    }
    static <T> FunctionK<reactiveSeq,optional,T> streamOptionals(){
        return of(i -> Optionals.OptionalKind.widen(ReactiveSeq.narrowK(i).headAndTail().headOptional()),Optionals.Instances.definitions());
    }
    static <T> FunctionK<list,reactiveSeq,T> listStream(){
        return of(i -> ListX.narrowK(i).stream(),ReactiveSeq.Instances.definitions());
    }
    static <T> FunctionK<list,maybe,T> listMaybe(){
        return of(i -> ListX.narrowK(i).headAndTail().headMaybe(), Maybe.Instances.definitions());
    }
    static <T> FunctionK<list,optional,T> listOptional(){
        return of(i -> Optionals.OptionalKind.widen(ListX.narrowK(i).headAndTail().headOptional()),Optionals.Instances.definitions());
    }
}
