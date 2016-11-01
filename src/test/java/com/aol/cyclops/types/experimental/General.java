package com.aol.cyclops.types.experimental;

import static com.aol.cyclops.util.function.Lambda.λ;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.derive4j.hkt.Higher;
import org.junit.Test;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.experimental.TypeClasses._Functor;
import com.aol.cyclops.types.higherkindedtypes.type.constructors.ListType;
import com.aol.cyclops.types.higherkindedtypes.type.constructors.MaybeType;
import com.aol.cyclops.types.higherkindedtypes.type.constructors.OptionalType;
import com.aol.cyclops.types.higherkindedtypes.type.constructors.StreamType;
import com.aol.cyclops.util.function.Lambda;

public class General {

    private int mult3(int x){
        return x*3;
    }
    private int add2(int x){
        return x*2;
    }
    @Test
    public void functor(){
        _Functor<ListType.µ> f = TypeClasses.General
                .<ListType.µ,List<?>>functor(ListType::narrow,(list,fn)->ListX.fromIterable(list).map(fn));
       
        Higher<ListType.µ,Integer> mapped1 = f.map(a->a+1, ListType.widen(Arrays.asList(1,2,3)));
        
        System.out.println(mapped1);
        
        ListType.widen(Arrays.asList(1,2,3))
                .apply_(f::map, λ(this::mult3))
                .convert(ListType::narrow);
        
        List<Integer> mapped2 = f.map(a->a+1, ListType.widen(Arrays.asList(1,2,3)))
                                 .apply_(f::map,λ(this::mult3))
                                 .apply_(f::map,λ(this::add2))
                                 .convert(ListType::narrow);
        
        System.out.println(mapped2);
                
        ListX<Integer> mapped = f.map(a->a+1, ListType.widen(Arrays.asList(1,2,3)))
                                 .apply_(f::map, Lambda.<Integer,Integer>λ(a->a+1))
                                 .apply_(f::map, Lambda.<Integer,Integer>λ(a->a*3))
                                 .convert(ListType::narrow);
        
        
        System.out.println(mapped);
        
        System.out.println(f.map(a->a+1, ListX.of(1,2,3)));
    }
    @Test
    public void functorListMaybe(){
        _Functor<ListType.µ> f = TypeClasses.General
                .<ListType.µ,ListX<?>>functor(ListType::narrow,(list,fn)->ListType.widen(list.map(fn)));
        
        Higher<ListType.µ,Maybe<Integer>> mapped2 = f.map(a->Maybe.<Integer>just(a+1), ListType.widen(Arrays.asList(1,2,3)));
        
        List<Maybe<Integer>> list = mapped2.convert(ListType::narrow);
        Higher<ListType.µ,Higher<MaybeType.µ,Integer>> mapped3 = f.map(a->MaybeType.<Integer>just(a+1), ListType.widen(Arrays.asList(1,2,3)));
       
        
      
    }
    @Test
    public void functorOptionalList(){
        _Functor<OptionalType.µ> f = TypeClasses.General
                .<OptionalType.µ,Optional<?>>functor(OptionalType::narrow,(maybe,fn)->OptionalType.widen(maybe.map(fn)));
        
        Higher<OptionalType.µ,List<Integer>> mapped2 = f.map(a->ListX.<Integer>of(a+1), OptionalType.widen(Optional.of(1)));
        
        Optional<List<Integer>> list = mapped2.convert(OptionalType::narrow);
        Higher<OptionalType.µ,Higher<ListType.µ,Integer>> mapped3 = f.map(a->ListX.<Integer>of(a+1),OptionalType.widen(Optional.of(3)));
        
       
       
        
      
    }
    
    @Test
    public void functorMaybeList(){
        _Functor<MaybeType.µ> f = TypeClasses.General
                .<MaybeType.µ,Maybe<?>>functor(MaybeType::narrow,(maybe,fn)->MaybeType.widen(maybe.map(fn)));
        
        Higher<MaybeType.µ,List<Integer>> mapped2 = f.map(a->ListX.<Integer>of(a+1), MaybeType.just(1));
        
        Maybe<List<Integer>> list = mapped2.convert(MaybeType::narrow);
        Higher<MaybeType.µ,Higher<ListType.µ,Integer>> mapped3 = f.map(a->ListX.<Integer>of(a+1),MaybeType.just(3));
        
       
       
        
      
    }
    @Test
    public void functorStreamMaybe(){
        _Functor<StreamType.µ> f = TypeClasses.General
                .<StreamType.µ,Stream<?>>functor(StreamType::narrow,(stream,fn)->StreamType.widen(stream.map(fn)));
        
        Higher<StreamType.µ,Maybe<Integer>> mapped2 = f.map(a->Maybe.<Integer>just(a+1), StreamType.widen(Stream.of(1,2,3)));
        
        Stream<Maybe<Integer>> list = mapped2.convert(StreamType::narrow);
        Higher<StreamType.µ,MaybeType<Integer>> mapped3 = f.map(a->MaybeType.<Integer>just(a+1), StreamType.widen(Stream.of(1,2,3)));
        Stream<MaybeType<Integer>> mapped4 =  mapped3.convert(StreamType::narrow);
        Stream<Maybe<Integer>> mapped6 = mapped4.map(m->m.convert(MaybeType::narrow));
        
        Higher<StreamType.µ,Higher<ListType.µ,Integer>> mappedX = f.map(a->ListX.<Integer>of(a+1), StreamType.widen(Stream.of(1,2,3)));
        
        Stream<List<Integer>> mapped8 = mappedX.convert(StreamType::cast);
       // Stream<List<Integer>> mapped8 =  mappedX.convert(StreamType::narrow);
        
        Stream<Maybe<Integer>> mapped5 = f.map(a->Maybe.<Integer>just(a+1), StreamType.widen(Stream.of(1,2,3)))
                                          .convert(StreamType::narrow);
        
     //   Higher<StreamType.µ,Integer>  stream = mapped3.apply(Lambda.λ(a->a.convert(Maybe.MaybeType::narrow).get()),f::mapInv);
        
        Higher<StreamType.µ,Integer> stream2 = f.map(Lambda.λ(a->a.get()), mapped3);
        
                                        // .convert(StreamType::narrow);
        
      
    }
    
}
