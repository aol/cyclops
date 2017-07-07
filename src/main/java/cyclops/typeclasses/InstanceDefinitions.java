package cyclops.typeclasses;

import com.aol.cyclops2.hkt.Higher;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import cyclops.monads.Witness;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;

/**
 * Created by johnmcclean on 28/06/2017.
 */
public interface InstanceDefinitions<W> {

    default Eq<W> eq(){
        return new Eq<W>(){};
    }
    public  <T,R>Functor<W> functor();

    public  <T> Pure<W> unit();

    public  <T,R> Applicative<W> applicative();

    public  <T,R> Monad<W> monad();

    public  <T,R> Maybe<MonadZero<W>> monadZero();

    public  <T> Maybe<MonadPlus<W>> monadPlus();

    public  <T> MonadRec<W> monadRec();

    public  <T> Maybe<MonadPlus<W>> monadPlus(Monoid<Higher<W,T>> m);

    public  <C2,T> Maybe<Traverse<W>> traverse();
    public  <T> Maybe<Foldable<W>> foldable();
    public  <T> Maybe<Comonad<W>> comonad();
    public  <T> Maybe<Unfoldable<W>> unfoldable();
}
