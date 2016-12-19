package com.aol.cyclops.control;

import java.util.function.Function;

import com.aol.cyclops.types.Functor;
import com.aol.cyclops.util.function.F1;

/**
 * An interface that represents the Reader monad
 * 
 * A technique for functional dependency injection. Functions rather than values
 * are manipulated and dependencies injected into Functions to execute them.
 * 
 * {@see <a href="https://medium.com/@johnmcclean/dependency-injection-using-the-reader-monad-in-java8-9056d9501c75">Dependency injection using the Reader Monad in Java 8</a>}
 * 
 * <pre>
 * {@code 
 * For comprehension with the Reader Monad, using curried syntax :-
 *  
 * import com.aol.cyclops.control.For;
 * 
 * Reader<UserRepository,Integer> res =  For.reader(depth1("bob"))
                                            .reader(a->depth2("bob"))
                                            .reader(a->b->depth3("bob"))
                                            .reader(a->b->c->depth3("bob"))
                                            .reader(a->b->c->d->depth4("bob"))
                                            .reader(a->b->c->d->e->depth5("bob"))
                                            .reader(a->b->c->d->e->f->depth5("bob"))
                                            .reader(a->b->c->d->e->f->g->depth6("bob"))
                                            .yield(a->b->c->d->e->f->g->h->a+b+c+d+e+f+g+h).unwrap();
   res.apply(new UserRepositoryImpl())
   //29
   private Reader<UserRepository,Integer> depth8(String name){
        return repo->depth7(name).apply(repo)+1;
   }
 * 
 * 
 * }
 * </pre>
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> Current input type of Function
 * @param <R> Current return type of Function
 */
public interface Reader<T, R> extends F1<T, R>, Functor<R> {

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#map(java.util.function.Function)
     */
    @Override
    default <R1> Reader<T, R1> map(final Function<? super R, ? extends R1> f2) {
        return FluentFunctions.of(this.andThen(f2));
    }

    /**
     * FlatMap this Reader by applying the prodived function and unnesting to a single Reader
     * 
     * @param f Transformation function to be flattened
     * @return Transformed Reader
     */
    default <R1> Reader<T, R1> flatMap(final Function<? super R, ? extends Reader<T, R1>> f) {
        return FluentFunctions.of(a -> f.apply(apply(a))
                                        .apply(a));
    }

    

}
