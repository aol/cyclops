package cyclops.typeclasses.monad;

import com.oath.cyclops.hkt.Higher;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.Function;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ComposedTraverse<W1, W2> {
    private final Traverse<W1> traverse1;
    private final Traverse<W2> traverse2;
    private final Applicative<W2> ap;

    public static <W1,W2> ComposedTraverse<W1,W2> of(Traverse<W1> t1, Traverse<W2> t2, Applicative<W2> ap){
        return new ComposedTraverse<>(t1,t2,ap);
    }

    public <H, A, B> Higher<H,Higher<W1,Higher<W2,B>>> traverse(Applicative<H> applicative, Function<? super A,? extends Higher<H,B>> fn, Higher<W1, Higher<W2, A>> ds){
        Higher<H, Higher<W1, Higher<W2, B>>> v = traverse1.traverseA(applicative, ga -> traverse2.traverseA(applicative, fn, ga),ds);
        return applicative.map_(v,i->i);
    }


}
