package cyclops.typeclasses.monad;


import com.aol.cyclops2.hkt.Higher;
import cyclops.control.Eval;
import cyclops.control.Try;
import cyclops.control.Xor;
import cyclops.control.lazy.Either;
import cyclops.function.Fn0;

import java.util.function.Function;

public interface ApplicativeError<W,E> extends Applicative<W>{

    <T> Higher<W,T> raiseError(E e);
    <T> Higher<W,T> handleErrorWith(Function<? super E,? extends Higher<W,? extends T>> fn, Higher<W,T> ds);
    default <T> Higher<W,T> handleError(Function<? super E,? extends T> fn, Higher<W,T> ds){
        return handleErrorWith(fn.andThen(t->unit(t)),ds);
    }
    default <T> Higher<W,T> tryCatch(Fn0<T> eval, Function<? super Throwable,? extends E> mapper){
       return tryCatchEval(eval.toEval(),mapper);
    }
    default <T> Higher<W,T> tryCatchEval(Eval<T> eval, Function<? super Throwable,? extends E> mapper){
        try{
            return unit(eval.get());
        }catch(Throwable t){
            return raiseError(mapper.apply(t));
        }
    }
    default <T,X extends Throwable> Higher<W,T> fromXor(Xor<E,T> t){
        return t.visit(this::raiseError,a->unit(a));
    }
     default <T> Higher<W, Either<E, T>> recover(Higher<W, T> ds){
        return handleErrorWith(l->unit(Either.left(l)),map(r->Either.right(r),ds));
     }

}
