package cyclops.typeclasses.instances;

import com.aol.cyclops.hkt.Higher;
import cyclops.collections.ListX;
import cyclops.function.Monoid;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.Unit;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.experimental.UtilityClass;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Companion class for creating Type Class instances for working with Lists
 * @author johnmcclean
 *
 */
@UtilityClass
public class ListInstances {

    public static void main(String[] args){
        ListX<Integer> small = ListX.of(1,2,3);
        ListX<Integer> list = ListInstances.functor()
                                     .map(i->i*2, small)
                                    // .then_(functor()::map, Lambda.<Integer,Integer>l1(i->i*3))
                                     .then(h-> functor().map((Integer i)->""+i,h))
                                     .then(h-> monad().flatMap(s->ListX.of(1), h))
                                     .convert(ListInstances::narrowK);
          ListX<Integer> string = list.convert(ListInstances::narrowK);
                    
        System.out.println(ListInstances.functor().map(i->i*2, small));
    }
    /**
     * 
     * Transform a list, mulitplying every element by 2
     * 
     * <pre>
     * {@code 
     *  ListX<Integer> list = Lists.functor().map(i->i*2, ListX.widen(Arrays.asList(1,2,3));
     *  
     *  //[2,4,6]
     *  
     * 
     * }
     * </pre>
     * 
     * An example fluent api working with Lists
     * <pre>
     * {@code 
     *   ListX<Integer> list = Lists.unit()
                                       .unit("hello")
                                       .then(h->Lists.functor().map((String v) ->v.length(), h))
                                       .convert(ListX::narrowK);
     * 
     * }
     * </pre>
     * 
     * 
     * @return A functor for Lists
     */
    public static <T,R>Functor<ListX.µ> functor(){
        BiFunction<ListX<T>,Function<? super T, ? extends R>,ListX<R>> map = ListInstances::map;
        return General.functor(map);
    }
    /**
     * <pre>
     * {@code 
     * ListX<String> list = Lists.unit()
                                     .unit("hello")
                                     .convert(ListX::narrowK);
        
        //Arrays.asList("hello"))
     * 
     * }
     * </pre>
     * 
     * 
     * @return A factory for Lists
     */
    public static <T> Unit<ListX.µ> unit(){
        return General.<ListX.µ,T>unit(ListInstances::of);
    }
    /**
     * 
     * <pre>
     * {@code 
     * import static com.aol.cyclops.hkt.jdk.ListX.widen;
     * import static com.aol.cyclops.util.function.Lambda.l1;
     * import static java.util.Arrays.asList;
     * 
       Lists.zippingApplicative()
            .ap(widen(asList(l1(this::multiplyByTwo))),widen(asList(1,2,3)));
     * 
     * //[2,4,6]
     * }
     * </pre>
     * 
     * 
     * Example fluent API
     * <pre>
     * {@code 
     * ListX<Function<Integer,Integer>> listFn =Lists.unit()
     *                                                  .unit(Lambda.l1((Integer i) ->i*2))
     *                                                  .convert(ListX::narrowK);
        
        ListX<Integer> list = Lists.unit()
                                      .unit("hello")
                                      .then(h->Lists.functor().map((String v) ->v.length(), h))
                                      .then(h->Lists.zippingApplicative().ap(listFn, h))
                                      .convert(ListX::narrowK);
        
        //Arrays.asList("hello".length()*2))
     * 
     * }
     * </pre>
     * 
     * 
     * @return A zipper for Lists
     */
    public static <T,R> Applicative<ListX.µ> zippingApplicative(){
        BiFunction<ListX< Function<T, R>>,ListX<T>,ListX<R>> ap = ListInstances::ap;
        return General.applicative(functor(), unit(), ap);
    }
    /**
     * 
     * <pre>
     * {@code 
     * import static com.aol.cyclops.hkt.jdk.ListX.widen;
     * ListX<Integer> list  = Lists.monad()
                                      .flatMap(i->widen(ListX.range(0,i)), widen(Arrays.asList(1,2,3)))
                                      .convert(ListX::narrowK);
     * }
     * </pre>
     * 
     * Example fluent API
     * <pre>
     * {@code 
     *    ListX<Integer> list = Lists.unit()
                                        .unit("hello")
                                        .then(h->Lists.monad().flatMap((String v) ->Lists.unit().unit(v.length()), h))
                                        .convert(ListX::narrowK);
        
        //Arrays.asList("hello".length())
     * 
     * }
     * </pre>
     * 
     * @return Type class with monad functions for Lists
     */
    public static <T,R> Monad<ListX.µ> monad(){
  
        BiFunction<Higher<ListX.µ,T>,Function<? super T, ? extends Higher<ListX.µ,R>>,Higher<ListX.µ,R>> flatMap = ListInstances::flatMap;
        return General.monad(zippingApplicative(), flatMap);
    }
    /**
     * 
     * <pre>
     * {@code 
     *  ListX<String> list = Lists.unit()
                                     .unit("hello")
                                     .then(h->Lists.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(ListX::narrowK);
        
       //Arrays.asList("hello"));
     * 
     * }
     * </pre>
     * 
     * 
     * @return A filterable monad (with default value)
     */
    public static <T,R> MonadZero<ListX.µ> monadZero(){
        
        return General.monadZero(monad(), ListX.empty());
    }
    /**
     * <pre>
     * {@code 
     *  ListX<Integer> list = Lists.<Integer>monadPlus()
                                      .plus(ListX.widen(Arrays.asList()), ListX.widen(Arrays.asList(10)))
                                      .convert(ListX::narrowK);
        //Arrays.asList(10))
     * 
     * }
     * </pre>
     * @return Type class for combining Lists by concatenation
     */
    public static <T> MonadPlus<ListX.µ> monadPlus(){
        Monoid<ListX<T>> m = Monoid.of(ListX.empty(), ListInstances::concat);
        Monoid<Higher<ListX.µ,T>> m2= (Monoid)m;
        return General.monadPlus(monadZero(),m2);
    }
    /**
     * 
     * <pre>
     * {@code 
     *  Monoid<ListX<Integer>> m = Monoid.of(ListX.widen(Arrays.asList()), (a,b)->a.isEmpty() ? b : a);
        ListX<Integer> list = Lists.<Integer>monadPlus(m)
                                      .plus(ListX.widen(Arrays.asList(5)), ListX.widen(Arrays.asList(10)))
                                      .convert(ListX::narrowK);
        //Arrays.asList(5))
     * 
     * }
     * </pre>
     * 
     * @param m Monoid to use for combining Lists
     * @return Type class for combining Lists
     */
    public static <T> MonadPlus<ListX.µ> monadPlus(Monoid<ListX<T>> m){
        Monoid<Higher<ListX.µ,T>> m2= (Monoid)m;
        return General.monadPlus(monadZero(),m2);
    }
 
    /**
     * @return Type class for traversables with traverse / sequence operations
     */
    public static <C2,T> Traverse<ListX.µ> traverse(){
        BiFunction<Applicative<C2>,ListX<Higher<C2, T>>,Higher<C2, ListX<T>>> sequenceFn = (ap,list) -> {
        
            Higher<C2,ListX<T>> identity = ap.unit(ListX.empty());

            BiFunction<Higher<C2,ListX<T>>,Higher<C2,T>,Higher<C2,ListX<T>>> combineToList =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.add(b); return a;}),acc,next);

            BinaryOperator<Higher<C2,ListX<T>>> combineLists = (a,b)-> ap.apBiFn(ap.unit((l1,l2)-> { l1.addAll(l2); return l1;}),a,b); ;  

            return list.stream()
                      .reduce(identity,
                              combineToList,
                              combineLists);  

   
        };
        BiFunction<Applicative<C2>,Higher<ListX.µ,Higher<C2, T>>,Higher<C2, Higher<ListX.µ,T>>> sequenceNarrow  = 
                                                        (a,b) -> ListInstances.widen2(sequenceFn.apply(a, ListInstances.narrowK(b)));
        return General.traverse(zippingApplicative(), sequenceNarrow);
    }
    
    /**
     * 
     * <pre>
     * {@code 
     * int sum  = Lists.foldable()
                        .foldLeft(0, (a,b)->a+b, ListX.widen(Arrays.asList(1,2,3,4)));
        
        //10
     * 
     * }
     * </pre>
     * 
     * 
     * @return Type class for folding / reduction operations
     */
    public static <T> Foldable<ListX.µ> foldable(){
        BiFunction<Monoid<T>,Higher<ListX.µ,T>,T> foldRightFn =  (m,l)-> ListX.fromIterable(narrow(l)).foldRight(m);
        BiFunction<Monoid<T>,Higher<ListX.µ,T>,T> foldLeftFn = (m,l)-> ListX.fromIterable(narrow(l)).reduce(m);
        return General.foldable(foldRightFn, foldLeftFn);
    }
  
    private static  <T> ListX<T> concat(List<T> l1, List<T> l2){
        return ListX.fromStreamS(Stream.concat(l1.stream(),l2.stream()));
    }
    private <T> ListX<T> of(T value){
        return ListX.of(value);
    }
    private static <T,R> ListX<R> ap(ListX<Function< T, R>> lt,  ListX<T> list){
        return ListX.fromIterable(lt).zip(list,(a,b)->a.apply(b));
    }
    private static <T,R> Higher<ListX.µ,R> flatMap( Higher<ListX.µ,T> lt, Function<? super T, ? extends  Higher<ListX.µ,R>> fn){
        return ListX.fromIterable(ListInstances.narrowK(lt)).flatMap(fn.andThen(ListInstances::narrowK));
    }
    private static <T,R> ListX<R> map(ListX<T> lt, Function<? super T, ? extends R> fn){
        return ListX.fromIterable(lt).map(fn);
    }



    /**
     * Widen a ListType nested inside another HKT encoded type
     *
     * @param flux HTK encoded type containing  a List to widen
     * @return HKT encoded type with a widened List
     */
    public static <C2, T> Higher<C2, Higher<ListX.µ, T>> widen2(Higher<C2, ListX<T>> flux) {
        // a functor could be used (if C2 is a functor / one exists for C2 type)
        // instead of casting
        // cast seems safer as Higher<ListX.µ,T> must be a ListX
        return (Higher) flux;
    }



    /**
     * Convert the raw Higher Kinded Type for ListType types into the ListType type definition class
     *
     * @param future HKT encoded list into a ListType
     * @return ListType
     */
    public static <T> ListX<T> narrowK(final Higher<ListX.µ, T> future) {
        return (ListX<T>) future;
    }

    /**
     * Convert the HigherKindedType definition for a List into
     *
     * @param List Type Constructor to convert back into narrowed type
     * @return List from Higher Kinded Type
     */
    public static <T> ListX<T> narrow(final Higher<ListX.µ, T> completableList) {

        return ((ListX<T>) completableList);//.narrow();

    }
}
