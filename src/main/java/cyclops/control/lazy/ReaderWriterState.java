package cyclops.control.lazy;

import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.hkt.Higher4;
import cyclops.control.Either;
import cyclops.control.anym.Witness.rws;
import cyclops.control.anym.Witness.supplier;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.free.Free;
import cyclops.function.*;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple3;

import java.util.function.BiFunction;
import java.util.function.Function;

import static cyclops.data.tuple.Tuple.tuple;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReaderWriterState<R,W,S,T> implements Higher4<rws,R,W,S,T> {

    private final Monoid<W> monoid;
    private final BiFunction<R,S, Free<supplier,Tuple3<W, S, T>>> runState;

    public Tuple3<W,S,T> run(R r,S s) {
        return Function0.run(runState.apply(r,s));
    }

    public static <T,R1, W, S, R> ReaderWriterState<R1, W, S, R> tailRec(Monoid<W> monoid,T initial, Function<? super T, ? extends  ReaderWriterState<R1, W, S,  ? extends Either<T, R>>> fn) {
        Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R> x = Instances.<R1,W,S> monadRec(monoid).tailRec(initial, fn);
        return narrowK(x);
    }

    public  ReaderWriterState<R,W,S,T> tell(W value) {
        BiFunction<? super R, ? super S, Free<supplier,Tuple3<W,S, T>>> fn =
                (r,s)->runState.apply(r,s).map(t3-> tuple(monoid.apply(t3._1(),value),t3._2(),t3._3()));

        return suspended(fn,monoid);
    }

    public ReaderWriterState<R,W,S,T> ask() {
         return suspended((r,s) -> runState.apply(r,s).map(t3 -> Tuple.<W,S,T>tuple(monoid.zero(),s,t3._3())),monoid);
    }


    public ReaderWriterState<R,W,S,T> local(Function<? super  R,? extends R> fn) {
        BiFunction<? super R, ? super S, Free<supplier,Tuple3<W,S, T>>> runFn = (R r, S s) -> runState.apply(fn.apply(r), s);
        return suspended(runFn,monoid);
    }

    public <R2> ReaderWriterState<R,W,S,R2> map(Function<? super T,? extends R2> mapper) {

        return mapState(t -> tuple(t._1(), t._2(), mapper.apply(t._3())));
    }
    public <R2> ReaderWriterState<R,W,S,R2> mapState(Function<Tuple3<W,S,T>, Tuple3<W,S, R2>> fn) {
        return suspended((r,s) -> runState.apply(r,s).map(t3 -> fn.apply(t3)),monoid);
    }
    private static <R,W,S,T> ReaderWriterState<R,W,S,T> suspended(BiFunction<? super R, ? super S, Free<supplier,Tuple3<W,S, T>>> runF,
                                                                  Monoid<W> monoid) {

        return new ReaderWriterState<R, W, S, T>(monoid,(R r ,S s) -> Function0.suspend(Lambda.λK(()->runF.apply(r,s))));

    }

    public <R2> ReaderWriterState<R,W,S,R2> flatMap(Function<? super T,? extends  ReaderWriterState<R,W,S,R2>> f) {

        return suspended((r,s) -> runState.apply(r, s)
                .flatMap(result -> Free.done(f.apply(result._3())
                                              .run(r, result._2())
                                              .transform((w2,s2,r2)-> tuple(monoid.apply(w2,result._1()),s2,r2)

                ))),monoid);
    }


    public <R1, R2, R3, R4> ReaderWriterState<R,W,S,R4> forEach4(Function<? super T, ReaderWriterState<R,W,S,R1>> value2,
                                                      BiFunction<? super T, ? super R1, ReaderWriterState<R,W,S,R2>> value3,
                                                      Function3<? super T, ? super R1, ? super R2, ReaderWriterState<R,W,S,R3>> value4,
                                                      Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R4> yieldingFunction) {




        return this.flatMap(in -> {
            
            ReaderWriterState<R,W,S,R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                ReaderWriterState<R,W,S,R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {

                    ReaderWriterState<R,W,S,R3> c = value4.apply(in,ina,inb);
                    return c.map(inc->yieldingFunction.apply(in, ina, inb, inc));

                });


            });


        });

    }




    public <R1, R2, R4> ReaderWriterState<R,W,S,R4> forEach3(Function<? super T, ReaderWriterState<R,W,S,R1>> value2,
                                                  BiFunction<? super T, ? super R1, ReaderWriterState<R,W,S,R2>> value3,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            ReaderWriterState<R,W,S,R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                ReaderWriterState<R,W,S,R2> b = value3.apply(in,ina);
                return b.map(in2 -> {
                    return yieldingFunction.apply(in, ina, in2);

                });



            });

        });

    }

    public <R1, R4> ReaderWriterState<R,W,S,R4> forEach2(Function<? super T, ReaderWriterState<R,W,S,R1>> value2,
                                              BiFunction<? super T, ? super R1, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            ReaderWriterState<R,W,S,R1> a = value2.apply(in);
            return a.map(in2 -> {
                return yieldingFunction.apply(in, in2);

            });




        });


    }
    public Active<Higher<Higher<Higher<rws,R>,W>,S>,T> allTypeclasses(R val1, S val2,Monoid<W> monoid){
        return Active.of(this, Instances.definitions(val1,val2,monoid));
    }
    public <W2,R2> Nested<Higher<Higher<Higher<rws, R>, W>, S>, W2, R2>mapM(R val1, S val2,Monoid<W> monoid, Function<? super T,? extends Higher<W2,R2>> fn, InstanceDefinitions<W2> defs){
        InstanceDefinitions<Higher<Higher<Higher<rws,R>, W>, S>> def1 = Instances.definitions(val1,val2,monoid);
        ReaderWriterState<R, W, S, Higher<W2, R2>> r = map(fn);
        Higher<Higher<Higher<Higher<rws,R>,W>,S>,Higher<W2,R2>> hkt = r;

        Nested<Higher<Higher<Higher<rws, R>, W>, S>, W2, R2> res = Nested.of(hkt, def1, defs);
        return res;
    }
    public static <R,W,S,T> ReaderWriterState<R,W,S,T> rws(BiFunction<? super R, ? super S,? extends Tuple3<W,S, T>> runF, Monoid<W> monoid) {

        return new ReaderWriterState<R, W, S, T>(monoid,(r,s) -> Free.done(runF.apply(r,s)));
    }

    public static <R,W,S,T> ReaderWriterState<R,W,S,T> narrowK(Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> hkt){
        return (ReaderWriterState<R,W,S,T>)hkt;
    }
    public static  <R,W,S,T> Kleisli<Higher<Higher<Higher<rws, R>, W>, S>,ReaderWriterState<R,W,S,T>,T> kindKleisli(Monoid<W> m){
        return Kleisli.of(Instances.monad(m), ReaderWriterState::widen);
    }
    public static <R,W,S,T> Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> widen(ReaderWriterState<R,W,S,T> narrow) {
        return narrow;
    }
    public static  <R,W,S,T> Cokleisli<Higher<Higher<Higher<rws, R>, W>, S>,T,ReaderWriterState<R,W,S,T>> kindCokleisli(){
        return Cokleisli.of(ReaderWriterState::narrowK);
    }
    public static class Instances{

        public static <R,W,S> InstanceDefinitions<Higher<Higher<Higher<rws,R>,W>,S>> definitions(R val1, S val2,Monoid<W> monoid){
            return new InstanceDefinitions<Higher<Higher<Higher<rws,R>,W>,S>>() {

                @Override
                public <T, R2> Functor<Higher<Higher<Higher<rws, R>, W>, S>> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<Higher<Higher<Higher<rws, R>, W>, S>> unit() {
                    return Instances.unit(monoid);
                }

                @Override
                public <T, R2> Applicative<Higher<Higher<Higher<rws, R>, W>, S>> applicative() {
                    return Instances.applicative(monoid);
                }

                @Override
                public <T, R2> Monad<Higher<Higher<Higher<rws, R>, W>, S>> monad() {
                    return Instances.monad(monoid);
                }

                @Override
                public <T, R2> Maybe<MonadZero<Higher<Higher<Higher<rws, R>, W>, S>>> monadZero() {
                    return Maybe.nothing();
                }

                @Override
                public <T> Maybe<MonadPlus<Higher<Higher<Higher<rws, R>, W>, S>>> monadPlus() {
                    return Maybe.nothing();
                }

                @Override
                public <T> MonadRec<Higher<Higher<Higher<rws, R>, W>, S>> monadRec() {
                    return Instances.monadRec(monoid);
                }

                @Override
                public <T> Maybe<MonadPlus<Higher<Higher<Higher<rws, R>, W>, S>>> monadPlus(Monoid<Higher<Higher<Higher<Higher<rws, R>, W>, S>, T>> m) {
                    return Maybe.nothing();
                }

                @Override
                public <C2, T> Traverse<Higher<Higher<Higher<rws, R>, W>, S>> traverse() {
                    return Instances.traverse(val1,val2,monoid);
                }

                @Override
                public <T> Foldable<Higher<Higher<Higher<rws, R>, W>, S>> foldable() {
                    return Instances.foldable(val1,val2,monoid);
                }

                @Override
                public <T> Maybe<Comonad<Higher<Higher<Higher<rws, R>, W>, S>>> comonad() {
                    return Maybe.nothing();
                }

                @Override
                public <T> Maybe<Unfoldable<Higher<Higher<Higher<rws, R>, W>, S>>> unfoldable() {
                    return Maybe.nothing();
                }
            }  ;
        }
        public static <R,W,S> Functor<Higher<Higher<Higher<rws,R>,W>,S>> functor(){
            return new  Functor<Higher<Higher<Higher<rws,R>,W>,S>>(){

                @Override
                public <T, R2> Higher<Higher<Higher<Higher<rws, R>, W>, S>, R2> map(Function<? super T, ? extends R2> fn, Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> ds) {
                    ReaderWriterState<R, W, S, T> r = narrowK(ds);
                    return r.map(fn);
                }
            };
        }

        public static <R,W,S>  Pure<Higher<Higher<Higher<rws,R>,W>,S>>unit(Monoid<W> monoid){
            return new Pure<Higher<Higher<Higher<rws,R>,W>,S>>() {

                @Override
                public <T> Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> unit(T value) {
                    return rws((a,s)-> tuple(monoid.zero(),s,value),monoid);
                }
            };
        }

        public static <R,W,S> Applicative<Higher<Higher<Higher<rws,R>,W>,S>> applicative(Monoid<W> monoid){
            return new Applicative<Higher<Higher<Higher<rws,R>,W>,S>>(){
                @Override
                public <T, R2> Higher<Higher<Higher<Higher<rws, R>, W>, S>, R2> ap(Higher<Higher<Higher<Higher<rws, R>, W>, S>, ? extends Function<T, R2>> fn, Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> apply) {
                    ReaderWriterState<R, W, S, ? extends Function<T, R2>> f = narrowK(fn);
                    ReaderWriterState<R, W, S, T> ap = narrowK(apply);
                    ReaderWriterState<R, W, S, R2> mapped = f.flatMap(x -> ap.map(x));
                    return mapped;
                }

                @Override
                public <T, R2> Higher<Higher<Higher<Higher<rws, R>, W>, S>, R2> map(Function<? super T, ? extends R2> fn, Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> ds) {
                    return Instances.<R,W,S>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> unit(T value) {
                    return Instances.<R,W,S>unit(monoid).unit(value);
                }

            };
        }
        public static <R1,W,S> Monad<Higher<Higher<Higher<rws, R1>,W>,S>> monad(Monoid<W> monoid) {
            return new Monad<Higher<Higher<Higher<rws, R1>,W>,S>>() {


                @Override
                public <T, R> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R> flatMap(Function<? super T, ? extends Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R>> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> ds) {
                    ReaderWriterState<R1, W, S, T> r = narrowK(ds);
                    return r.flatMap(fn.andThen(h->narrowK(h)));

                }

                @Override
                public <T, R2> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R2> ap(Higher<Higher<Higher<Higher<rws, R1>, W>, S>, ? extends Function<T, R2>> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> apply) {
                    return Instances.<R1,W,S>applicative(monoid).ap(fn,apply);
                }

                @Override
                public <T, R2> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R2> map(Function<? super T, ? extends R2> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> ds) {
                    return Instances.<R1, W, S>functor().map(fn, ds);
                }

                @Override
                public <T> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> unit(T value) {
                    return Instances.<R1,W,S>unit(monoid).unit(value);
                }

            };

        }
        public static <R1,W,S> Foldable<Higher<Higher<Higher<rws, R1>,W>,S>> foldable(R1 val1, S val2,Monoid<W> monoid) {
            return new Foldable<Higher<Higher<Higher<rws, R1>, W>, S>>() {
                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> ds) {
                    ReaderWriterState<R1, W, S, T> rws =narrowK(ds);
                    return rws.run(val1, val2).transform((a, b, t) -> monoid.foldRight(t));
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> ds) {
                    ReaderWriterState<R1, W, S, T> rws =narrowK(ds);
                    return rws.run(val1, val2).transform((a, b, t) -> monoid.foldLeft(t));
                }

                @Override
                public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> nestedA) {
                    return foldLeft(mb,narrowK(nestedA).<R>map(fn));
                }
            };
        }
        public static <R1,W,S> Traverse<Higher<Higher<Higher<rws, R1>,W>,S>> traverse(R1 val1, S val2,Monoid<W> monoid) {
            return new Traverse<Higher<Higher<Higher<rws, R1>, W>, S>>() {
                @Override
                public <C2, T, R> Higher<C2, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> ds) {
                    ReaderWriterState<R1,W,S,T> rws = narrowK(ds);
                    Higher<C2, R> x = rws.run(val1, val2).transform((a, b, t) -> fn.apply(t));
                    return applicative.map_(x,i-> rws((ra, rb) -> tuple(monoid.zero(), rb, i), monoid));
                }
                @Override
                public <C2, T> Higher<C2, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T>> sequenceA(Applicative<C2> applicative, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, Higher<C2, T>> ds) {
                    return traverseA(applicative,Function.identity(),ds);
                }

                @Override
                public <T, R> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R> ap(Higher<Higher<Higher<Higher<rws, R1>, W>, S>, ? extends Function<T, R>> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> apply) {
                    return Instances.<R1,W,S>applicative(monoid).ap(fn,apply);
                }

                @Override
                public <T> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> unit(T value) {
                    return Instances.<R1,W,S>unit(monoid).unit(value);
                }

                @Override
                public <T, R> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<Higher<rws, R1>, W>, S>, T> ds) {
                    return Instances.<R1, W, S>functor().map(fn, ds);
                }
            };
        }
        public static <R1,W,S> MonadRec<Higher<Higher<Higher<rws, R1>,W>,S>> monadRec(Monoid<W> monoid) {
            return new MonadRec<Higher<Higher<Higher<rws, R1>,W>,S>>() {


                @Override
                public <T, R> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<Higher<Higher<rws, R1>, W>, S>, ? extends Either<T, R>>> fn) {
                    return narrowK(fn.apply(initial)).flatMap( eval ->
                            eval.visit(s->narrowK(tailRec(s,fn)),p->{
                                ReaderWriterState<R1, W, S, R> k = narrowK(Instances.<R1, W, S>unit(monoid).<R>unit(p));
                                return k;
                            }));
                }


            };
        }
    }
}
