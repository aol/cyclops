package cyclops.typeclasses;

import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.Eq;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.typeclasses.functor.ContravariantFunctor;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.functor.ProFunctor;
import cyclops.typeclasses.monad.*;

/**
 * Created by johnmcclean on 28/06/2017.
 */
public interface InstanceDefinitions<W> {



    default Eq<W> eq(){
        return new Eq<W>(){};
    }

    <T,R>Functor<W> functor();
    <T> Pure<W> unit();
    <T,R> Applicative<W> applicative();
    <T,R> Monad<W> monad();
    <T,R> Option<MonadZero<W>> monadZero();
    <T> Option<MonadPlus<W>> monadPlus();
    <T> MonadRec<W> monadRec();
    <T> Foldable<W> foldable();
    <C2,T> Traverse<W> traverse();


    default <T> Option<ContravariantFunctor<W>> contravariantFunctor(){
        return Maybe.nothing();
    }
    default <T> Option<ProFunctor<W>> profunctor(){
        return Maybe.nothing();
    }
    default <T,E> Option<ApplicativeError<W, E>> applicativeError(){
        return Maybe.nothing();
    }


    <T> Option<MonadPlus<W>> monadPlus(MonoidK<W> m);



    <T> Option<Comonad<W>> comonad();

    default  <T> Option<Unfoldable<W>> unfoldable(){
        return Maybe.nothing();
    }



    default Show<W> show(){
        return new Show<W>(){};
    }

}
