package cyclops.typeclasses;


import com.aol.cyclops2.data.collections.extensions.IndexedSequenceX;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collectionx.immutable.LinkedListX;
import cyclops.collectionx.mutable.ListX;
import cyclops.control.Option;
import cyclops.function.Function1;
import cyclops.reactive.ReactiveSeq;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.function.Supplier;

public interface Enumeration<E> {

    Option<E> toEnum(int e);

    int fromEnum(E a);

    default E toEnumOrElse(int e, E value){
        return toEnum(e).orElse(value);
    }
    default E toEnumOrElseGet(int e, Supplier<? extends E> value){
        return toEnum(e).orElse(value.get());
    }



    default E succOrElse(E e,E value){
        return toEnumOrElse(fromEnum(e)+1,value);
    }
    default E predOrElse(E e, E value){
        return toEnumOrElse(fromEnum(e)-1,value);
    }
    default E succOrElseGet(E e,Supplier<? extends E> value){
        return toEnumOrElseGet(fromEnum(e)+1,value);
    }
    default E predOrElseGet(E e, Supplier<? extends E> value){
        return toEnumOrElseGet(fromEnum(e)-1,value);
    }

    default Option<E> succ(E e){
        return toEnum(fromEnum(e)+1);
    }
    default Option<E> pred(E e){
        return toEnum(fromEnum(e)-1);
    }

    static <E extends Enum<E>> Enumeration<E> enums(E... values){
        return new EnumerationByEnum<E>(values);
    }
    static <E extends Enum<E>> Enumeration<E> enums(Class<E> c){
        return new EnumerationByEnum<E>(c.getEnumConstants());
    }

    static Enumeration<Integer> ints(){
          return   new Enumeration<Integer>() {
        @Override
        public Option<Integer> toEnum(int e) {
            return Option.some(e);
        }

        @Override
        public int fromEnum(Integer a) {
            return a;
        }
    };
    }
    static <E> Enumeration<E> enums(IndexedSequenceX<E> seq){
        return new EnumerationByIndexed<E>(seq);
    }

    static <E> Enumeration<E> enumsList(List<E> seq){
        return new EnumerationByIndexed<E>(ListX.fromIterable(seq));
    }
    @AllArgsConstructor
    static class EnumerationByEnum<E extends Enum<E>> implements  Enumeration<E>{
        private final E[] values;
        final Function1<E,Integer> memo = this::calcFromEnum;
        public Option<E> toEnum(int a){

            return a>-1 && a< values.length ? Option.just(values[a]) :  Option.none();
        }
        public E toEnumOrElse(int a,E e){

            return a>-1 && a< values.length ? values[a] :  e;
        }
        public E toEnumOrElseGet(int a,Supplier<? extends E> e){

            return a>-1 && a< values.length ? values[a] :  e.get();
        }


        public Function1<E,Integer> fromEnumMemoized(){
            Function1<E,Integer> fn = this::fromEnum;
            return fn.memoize();
        }

        @Override
        public int fromEnum(E e) {
            return memo.apply(e);
        }
        public int calcFromEnum(E e) {
            for(int i=0;i<values.length;i++){
                if(values[i]==e){
                    return i;
                }
            }
            return -1;

        }
    }
    @AllArgsConstructor
    static class EnumerationByIndexed<E> implements Enumeration<E>{

        private final IndexedSequenceX<E> seq;
        final Function1<E,Integer> memo = this::calcFromEnum;
        @Override
        public Option<E> toEnum(int e) {
            return seq.get(e);
        }

        @Override
        public E toEnumOrElse(int e, E value) {
            return seq.getOrElse(e,value);
        }
        @Override
        public E toEnumOrElseGet(int e, Supplier<? extends E> value) {
            return seq.getOrElseGet(e,value);
        }


        @Override
        public int fromEnum(E a) {
            return  memo.apply(a);
        }
        public int calcFromEnum(E e) {
            for(int i=0;i<seq.size();i++){
                if(seq.get(i)==e){
                    return i;
                }
            }
            return -1;

        }
    }
    default  ReactiveSeq<E> streamTo(E e,E end){
        return ReactiveSeq.range(fromEnum(e),fromEnum(end)).map(this::toEnum)
                .takeWhile(Option::isPresent)
                .filter(Option::isPresent).flatMap(Option::stream);
    }

    default  ReactiveSeq<E> streamThenTo(E e,E next,E end){
        int start=  fromEnum(e);
        int step = fromEnum(next)-start;
        return ReactiveSeq.range(start,step,fromEnum(end)).map(this::toEnum)
                .takeWhile(Option::isPresent)
                .filter(Option::isPresent).flatMap(Option::stream);
    }

    default  ReactiveSeq<E> stream(E e){
        return ReactiveSeq.range(fromEnum(e),Integer.MAX_VALUE).map(this::toEnum)
                .takeWhile(Option::isPresent)
                    .filter(Option::isPresent).flatMap(Option::stream);
    }

    default ListX<E> list(E e){
        return stream(e)
                .to().listX(Evaluation.LAZY);
    }
    default LinkedListX<E> linkedList(E e){
        return stream(e)
                .to().linkedListX(Evaluation.LAZY);
    }
}
