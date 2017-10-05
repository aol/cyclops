package cyclops.data;


import com.aol.cyclops2.matching.Deconstruct;
import com.aol.cyclops2.matching.Deconstruct.Deconstruct2;
import com.aol.cyclops2.matching.Sealed1Or;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.jooq.lambda.tuple.Tuple2;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.jooq.lambda.tuple.Tuple.tuple;

//https://apocalisp.wordpress.com/2008/10/23/heterogeneous-lists-and-the-limits-of-the-java-type-system/
//inspired / influenced by Functional Java's HList
public interface HList<T1 extends HList<T1>> extends Sealed1Or<HList<T1>> {

    public static <T, HL extends HList<HL>> HCons<T, HL> cons(final T value, final HL l) {
        return new HCons<>(value, l);
    }
    public static <T> HCons<T, HNil> of(final T value) {
       return new HCons<>(value, empty());
    }
    public static HList<HNil> empty() {
        return HNil.Instance;
    }


   <TB> HCons<TB, T1> prepend(TB value);


    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode(of={"head,tail"})
    public static class HCons<T1, T2 extends HList<T2>> implements Deconstruct2<T1,HList<T2>>, HList<HCons<T1,T2>> {

        public final T1 head;
        public final HList<T2> tail;

        @Override
        public Tuple2<T1, HList<T2>> unapply() {
            return tuple(head,tail);
        }

        @Override
        public <R> R fold(Function<? super HList<HCons<T1, T2>>, ? extends R> fn1, Supplier<? extends R> s) {
            return fn1.apply(this);
        }

        @Override
        public <TB> HCons<TB, HCons<T1, T2>> prepend(TB value) {
            return cons(value,this);
        }
    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static  class HNil implements HList<HNil> {

        final static HNil Instance = new HNil();

        @Override
        public <R> R fold(Function<? super HList<HNil>, ? extends R> fn1, Supplier<? extends R> s) {
            return s.get();
        }

        @Override
        public <TB> HCons<TB, HNil> prepend(TB value) {
            return cons(value,this);
        }
    }

}
