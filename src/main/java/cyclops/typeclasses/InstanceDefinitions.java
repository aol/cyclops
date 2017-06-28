package cyclops.typeclasses;

import com.aol.cyclops2.hkt.Higher;
import cyclops.companion.Monoids;
import cyclops.companion.Optionals;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import cyclops.monads.Witness;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by johnmcclean on 28/06/2017.
 */
public interface InstanceDefinitions<W> {
 
    public  <T,R>Functor<W> functor();

    public  <T> Pure<W> unit();

    public  <T,R> Applicative<W> applicative();

    public  <T,R> Monad<W> monad();

    public  <T,R> Maybe<MonadZero<W>> monadZero();

    public  <T> Maybe<MonadPlus<W>> monadPlus();

    public  <T> Maybe<MonadPlus<W>> monadPlus(Monoid<Higher<W,T>> m);

    public  <C2,T> Traverse<W> traverse();
    public  <T> Foldable<W> foldable();
    public  <T> Maybe<Comonad<W>> comonad();
}
