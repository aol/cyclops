package cyclops.typeclasses;


import com.aol.cyclops2.data.collections.extensions.IndexedSequenceX;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.mutable.ListX;
import cyclops.control.Maybe;
import cyclops.function.Fn1;
import cyclops.function.Predicates;
import cyclops.stream.ReactiveSeq;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.function.Function;

public interface Enumeration<E> {

    Maybe<E> toEnum(int e);

    int fromEnum(E a);


    default Maybe<E> succ(E e){
        return toEnum(fromEnum(e)+1);
    }
    default Maybe<E> pred(E e){
        return toEnum(fromEnum(e)-1);
    }

    static <E extends Enum<E>> Enumeration<E> enums(E... values){
        return new EnumerationByEnum<E>(values);
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
        final Fn1<E,Integer> memo = this::calcFromEnum;
        public Maybe<E> toEnum(int a){

            return a>-1 && a< values.length ? Maybe.just(values[a]) :  Maybe.none();
        }


        public Fn1<E,Integer> fromEnumMemoized(){
            Fn1<E,Integer> fn = this::fromEnum;
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
        final Fn1<E,Integer> memo = this::calcFromEnum;
        @Override
        public Maybe<E> toEnum(int e) {
            return seq.get(e);
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
                .takeWhile(Maybe::isPresent)
                .filter(Maybe::isPresent).map(Maybe::get);
    }

    default  ReactiveSeq<E> streamThenTo(E e,E next,E end){
        int start=  fromEnum(e);
        int step = fromEnum(next)-start;
        return ReactiveSeq.range(start,step,fromEnum(end)).map(this::toEnum)
                .takeWhile(Maybe::isPresent)
                .filter(Maybe::isPresent).map(Maybe::get);
    }

    default  ReactiveSeq<E> stream(E e){
        return ReactiveSeq.range(fromEnum(e),Integer.MAX_VALUE).map(this::toEnum)
                .takeWhile(Maybe::isPresent)
                    .filter(Maybe::isPresent).map(Maybe::get);
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
