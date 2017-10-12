package cyclops.data.base;

import cyclops.control.Option;
import cyclops.reactive.ReactiveSeq;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

import static cyclops.data.base.BAMT.ArrayUtils.last;
import static cyclops.data.base.BAMT.Two.two;

public class BAMT<T> {

    public interface NestedArray<T>{
        static final int BITS_IN_INDEX = 5;
        static final int SIZE = (int) StrictMath.pow(2, BITS_IN_INDEX);

        public NestedArray<T> append(ActiveTail<T> tail);

        static int bitpos(int hash, int shift){
            return 1 << mask(hash, shift);
        }
        static int mask(int hash, int shift){
            return (hash >>> shift) & (SIZE-1);
        }
        static int mask(int hash){
            return (hash) & (SIZE-1);
        }

        default  <R> R match(Function<? super Zero<T>, ? extends R> zeroFn, Function<? super PopulatedArray<T>, ? extends R> popFn){
            return this instanceof Zero ?  zeroFn.apply((Zero<T>)this) : popFn.apply((PopulatedArray<T>)this);
        }

        ReactiveSeq<T> stream();

    }
    public interface PopulatedArray<T> extends NestedArray<T>{
        public T getOrElseGet(int pos, Supplier<T> alt);
        public T getOrElse(int pos, T alt);
        public Option<T> get(int pos);
        public T[] getNestedArrayAt(int pos);
        public PopulatedArray<T> set(int pos, T value);

    }

    static class ArrayUtils{

        public static <T> T[] append(T[] array, T value) {
            T[] newArray = (T[])new Object[array.length + 1];
            System.arraycopy(array, 0, newArray, 0, array.length);
            newArray[array.length] = value;
            return newArray;
        }
        public static <T> T[] drop(T[] array,int num) {
            T[] newArray = (T[])new Object[array.length -num];
            System.arraycopy(array, 0, newArray, 0, array.length-num);

            return newArray;
        }
        public static Object[] append2(Object[][] array, Object value) {
            Object[] newArray = new Object[array.length + 1];
            System.arraycopy(array, 0, newArray, 0, array.length);
            newArray[array.length] = value;
            return newArray;
        }
        public static <T> T last(T[] array){
            return array[array.length-1];
        }
    }

    @AllArgsConstructor
    public static class ActiveTail<T> implements PopulatedArray<T>{
        private final int bitShiftDepth =0;
        private final T[] array;

        public <T> ActiveTail<T> tail(T[] array){
            return new ActiveTail<>(array);
        }
        public static <T> ActiveTail<T> tail(T value){
            return new ActiveTail<>((T[])new Object[]{value});
        }
        public static <T> ActiveTail<T> emptyTail(){
            return new ActiveTail<>((T[])new Object[0]);
        }

        public ActiveTail<T> append(T t) {
            if(array.length<32){
                return  tail(ArrayUtils.append(array,t));
            }
            return this;
        }
        public ActiveTail<T> drop(int num){
            T[] newArray = (T[])new Object[array.length-num];
            System.arraycopy(array, num, newArray, 0, newArray.length);

            return tail(newArray);
        }
        public ActiveTail<T> dropRight(int num){
            return tail(ArrayUtils.drop(array,num));
        }
        public ActiveTail<T> takeRight(int num){
            T[] newArray = (T[])new Object[Math.max(num,array.length)];
            System.arraycopy(array, array.length-newArray.length, newArray, 0, num);

            return tail(newArray);
        }

        @Override
        public Option<T> get(int pos) {
            int indx = pos & 0x01f;
            if(indx<array.length)
                return Option.of((T)array[indx]);
            return Option.none();
        }
        @Override
        public T getOrElse(int pos, T alt) {
            int indx = pos & 0x01f;
            if(indx<array.length)
                return (T)array[indx];
            return alt;
        }
        @Override
        public T getOrElseGet(int pos, Supplier<T> alt) {
            int indx = pos & 0x01f;
            if(indx<array.length)
                return (T)array[indx];
            return alt.get();
        }

        @Override
        public T[] getNestedArrayAt(int pos) {
            return array;
        }

        @Override
        public ActiveTail<T> set(int pos, T value) {
            T[] updatedNodes =  Arrays.copyOf(array, array.length);
            updatedNodes[pos] = value;
            return new ActiveTail<>(updatedNodes);
        }

        public int size(){
            return array.length;
        }
        @Override
        public NestedArray<T> append(ActiveTail<T> tail) {
            return tail;
        }

        @Override
        public ReactiveSeq<T> stream() {
            return ReactiveSeq.of(array);
        }
    }

    public static class Zero<T> implements NestedArray<T>{
        @Override
        public NestedArray<T> append(ActiveTail<T> tail) {
            return new One(tail.array);
        }

        @Override
        public ReactiveSeq<T> stream() {
            return ReactiveSeq.empty();
        }
    }

    @AllArgsConstructor
    public static class One<T> implements PopulatedArray<T> {

        private final int bitShiftDepth =0;
        private final T[] array;

        public static <T> One<T> one(T[] array){
            return new One<>(array);
        }
        public static <T> One<T> one(T value){
            return new One<>((T[])new Object[]{value});
        }

        public NestedArray<T> append(ActiveTail<T> t) {
            return two(new  Object[][]{array,t.array});
        }

        @Override
        public ReactiveSeq<T> stream() {
            return ReactiveSeq.of(array);
        }

        @Override
        public Option<T> get(int pos) {
            int indx = pos & 0x01f;
            if(indx<array.length)
                return Option.of((T)array[indx]);
            return Option.none();
        }
        @Override
        public T getOrElse(int pos, T alt) {
            int indx = pos & 0x01f;
            if(indx<array.length)
                return (T)array[indx];
            return alt;
        }
        @Override
        public T getOrElseGet(int pos, Supplier<T> alt) {
            int indx = pos & 0x01f;
            if(indx<array.length)
                return (T)array[indx];
            return alt.get();
        }

        @Override
        public T[] getNestedArrayAt(int pos) {
            return array;
        }

        @Override
        public PopulatedArray<T> set(int pos, T t) {
            Object[] updatedNodes = Arrays.copyOf(array, array.length);
            updatedNodes[pos] = t;
            return new One(updatedNodes);
        }


    }
    @AllArgsConstructor
    public static class Two<T> implements PopulatedArray<T>{
        public static final int bitShiftDepth =5;
        private final Object[][] array;

        public static <T> Two<T> two(Object[][] array){
            return new Two<T>(array);
        }

        @Override
        public PopulatedArray<T> set(int pos, T t) {
            Object[][] updatedNodes = Arrays.copyOf(array, array.length);
            int indx = NestedArray.mask(pos,bitShiftDepth);
            Object[] e = updatedNodes[indx];
            Object[] newNode = Arrays.copyOf(e,e.length);
            updatedNodes[indx] = newNode;
            newNode[NestedArray.mask(pos)]=t;
            return two(updatedNodes);

        }

        @Override
        public Option<T> get(int pos) {
            T[] local = getNestedArrayAt(pos);
            int resolved = NestedArray.bitpos(pos,bitShiftDepth);
            int indx = pos & 0x01f;
            if(indx<local.length){
                return Option.of(local[indx]);
            }
            return Option.none();
        }
        @Override
        public T getOrElse(int pos, T alt) {
            T[] local = getNestedArrayAt(pos);
            int resolved = NestedArray.bitpos(pos,bitShiftDepth);
            int indx = pos & 0x01f;
            if(indx<local.length){
                return local[indx];
            }
            return alt;
        }
        @Override
        public T getOrElseGet(int pos, Supplier<T> alt) {
            T[] local = getNestedArrayAt(pos);
            int resolved = NestedArray.bitpos(pos,bitShiftDepth);
            int indx = pos & 0x01f;
            if(indx<local.length){
                return local[indx];
            }
            return alt.get();
        }

        @Override
        public T[] getNestedArrayAt(int pos) {
            int indx = NestedArray.mask(pos, bitShiftDepth);
            if(indx<array.length)
                return  (T[])array[indx];
            return (T[])new Object[0];

        }


        @Override
        public NestedArray<T> append(ActiveTail<T> tail) {
            if(array.length<32){
                Object[][] updatedNodes = Arrays.copyOf(array, array.length+1,Object[][].class);
                updatedNodes[array.length]=tail.array;
                return two(updatedNodes);
            }
            return Three.three(new Object[][][]{array,new Object[][]{tail.array}});


        }

        @Override
        public ReactiveSeq<T> stream() {
            return ReactiveSeq.iterate(0, i->i+1)
                                .take(array.length)
                                .map(indx->array[indx])
                                .flatMap(a-> ReactiveSeq.of((T[])a));
        }
    }
    @AllArgsConstructor
    public static class Three<T> implements PopulatedArray<T>{
        public static final int bitShiftDepth = 10;
        private final Object[][][] array;

        public static <T> Three<T> three(Object[][][] array){
            return new Three<T>(array);
        }

        @Override
        public PopulatedArray<T> set(int pos, T t) {
            Object[][][] updatedNodes = Arrays.copyOf(array, array.length);
            int indx = NestedArray.mask(pos,bitShiftDepth);
            Object[][] e = updatedNodes[indx];
            Object[][] newNode = Arrays.copyOf(e,e.length);
            updatedNodes[indx] = newNode;
            int indx2 = NestedArray.mask(pos, Two.bitShiftDepth);
            Object[] f = e[indx2];
            Object[] newNode2 = Arrays.copyOf(f,f.length);
            newNode[indx2]=f;
            f[NestedArray.mask(pos)]=t;
            return three(updatedNodes);

        }

        @Override
        public Option<T> get(int pos) {
            T[] local = getNestedArrayAt(pos);
            int resolved = NestedArray.bitpos(pos,bitShiftDepth);
            int indx = pos & 0x01f;
            if(indx<local.length){
                return Option.of(local[indx]);
            }
            return Option.none();
        }
        @Override
        public T getOrElse(int pos, T alt) {
            T[] local = getNestedArrayAt(pos);
            int resolved = NestedArray.bitpos(pos,bitShiftDepth);
            int indx = pos & 0x01f;
            if(indx<local.length){
                return local[indx];
            }
            return alt;
        }
        @Override
        public T getOrElseGet(int pos, Supplier<T> alt) {
            T[] local = getNestedArrayAt(pos);
            int resolved = NestedArray.bitpos(pos,bitShiftDepth);
            int indx = pos & 0x01f;
            if(indx<local.length){
                return local[indx];
            }
            return alt.get();
        }

        @Override
        public T[] getNestedArrayAt(int pos) {
            int indx = NestedArray.mask(pos, bitShiftDepth);
            if(indx<array.length){
               Object[][] twoArray = array[indx];
               int indx2 = NestedArray.mask(pos, Two.bitShiftDepth);
               if(indx2<twoArray.length){
                   return  (T[])twoArray[indx2];
               }
            }

            return (T[])new Object[0];

        }


        @Override
        public NestedArray<T> append(ActiveTail<T> tail) {
            if(last(array).length<32){
                Object[][][] updatedNodes = Arrays.copyOf(array, array.length,Object[][][].class);
                updatedNodes[updatedNodes.length-1]=Arrays.copyOf(last(updatedNodes), last(updatedNodes).length+1,Object[][].class);
                last(updatedNodes)[last(array).length] = tail.array;
                return three(updatedNodes);
            }
            if(array.length<32){
                Object[][][] updatedNodes = Arrays.copyOf(array, array.length+1,Object[][][].class);
                updatedNodes[array.length] = new Object[][]{tail.array};
                return three(updatedNodes);

            }
            return Four.four(new Object[][][][]{array,new Object[][][]{new Object[][]{tail.array}}});

        }

        @Override
        public ReactiveSeq<T> stream() {
            return ReactiveSeq.iterate(0, i->i+1)
                              .take(array.length)
                              .map(indx->array[indx])
                              .flatMap(a->{
                                  return ReactiveSeq.iterate(0, i->i+1)
                                                    .take(a.length)
                                                    .map(indx->a[indx])
                                                    .flatMap(a2-> ReactiveSeq.of((T[])a2));
                              });
        }
    }

    @AllArgsConstructor
    public static class Four<T> implements PopulatedArray<T>{
        public static final int bitShiftDepth = 15;
        private final Object[][][][] array;

        public static <T> Four<T> four(Object[][][][] array){
            return new Four<T>(array);
        }

        @Override
        public PopulatedArray<T> set(int pos, T t) {
            Object[][][][] n1 = Arrays.copyOf(array, array.length);
            int indx = NestedArray.mask(pos,bitShiftDepth);
            Object[][][] n2 = n1[indx];
            Object[][][] newNode = Arrays.copyOf(n2,n2.length);
            n1[indx] = newNode;
            int indx2 = NestedArray.mask(pos, Three.bitShiftDepth);
            Object[][] n3 = n2[indx2];
            Object[][] newNode2 = Arrays.copyOf(n3,n3.length);
            int indx3 = NestedArray.mask(pos, Two.bitShiftDepth);
            Object[] n4 = n3[indx3];
            Object[] newNode3 = Arrays.copyOf(n3,n3.length);
            newNode2[indx3]=n3;
            n4[NestedArray.mask(pos)]=t;
            return four(n1);

        }
        @Override
        public Option<T> get(int pos) {
            T[] local = getNestedArrayAt(pos);
            int resolved = NestedArray.bitpos(pos,bitShiftDepth);
            int indx = pos & 0x01f;
            if(indx<local.length){
                return Option.of(local[indx]);
            }
            return Option.none();
        }
        @Override
        public T getOrElse(int pos, T alt) {
            T[] local = getNestedArrayAt(pos);
            int resolved = NestedArray.bitpos(pos,bitShiftDepth);
            int indx = pos & 0x01f;
            if(indx<local.length){
                return local[indx];
            }
            return alt;
        }
        @Override
        public T getOrElseGet(int pos, Supplier<T> alt) {
            T[] local = getNestedArrayAt(pos);
            int resolved = NestedArray.bitpos(pos,bitShiftDepth);
            int indx = pos & 0x01f;
            if(indx<local.length){
                return local[indx];
            }
            return alt.get();
        }


        @Override
        public T[] getNestedArrayAt(int pos) {


            int indx = NestedArray.mask(pos, bitShiftDepth);
            if(indx<array.length){
                Object[][][] twoArray = array[indx];
                int indx2 = NestedArray.mask(pos, Three.bitShiftDepth);
                if(indx2<twoArray.length){
                    int indx3 = NestedArray.mask(pos, Two.bitShiftDepth);
                    Object[][] threeArray = twoArray[indx2];
                    if(indx3<threeArray.length){
                        return (T[])threeArray[indx3];
                    }
                }
            }

            return (T[])new Object[0];


        }


        @Override
        public NestedArray<T> append(ActiveTail<T> tail) {
            if(last(last(array)).length<32){
                Object[][][][] updatedNodes = Arrays.copyOf(array, array.length,Object[][][][].class);
                updatedNodes[updatedNodes.length-1]=Arrays.copyOf(last(updatedNodes), last(updatedNodes).length,Object[][][].class);
                last(updatedNodes)[last(updatedNodes).length-1]=Arrays.copyOf(last(last(updatedNodes)), last(last(updatedNodes)).length+1,Object[][].class);
                last(last(updatedNodes))[last(last(array)).length] = tail.array;
                return four(updatedNodes);

            }
            if(last(array).length<32){
                Object[][][][] updatedNodes = Arrays.copyOf(array, array.length,Object[][][][].class);
                updatedNodes[updatedNodes.length-1]=Arrays.copyOf(last(updatedNodes), last(updatedNodes).length+1,Object[][][].class);
                last(updatedNodes)[last(array).length] = new Object[][]{tail.array};
                return four(updatedNodes);
            }
            if(array.length<32){
                Object[][][][] updatedNodes = Arrays.copyOf(array, array.length+1,Object[][][][].class);
                updatedNodes[array.length] = new Object[][][]{new Object[][]{tail.array}};
                return four(updatedNodes);



            }
            return Five.five(new Object[][][][][]{array,new Object[][][][]{new Object[][][]{new Object[][]{tail.array}}}});
        }

        @Override
        public ReactiveSeq<T> stream() {
            return ReactiveSeq.iterate(0, i->i+1)
                    .take(array.length)
                    .map(indx->array[indx])
                    .flatMap(a->{
                        return ReactiveSeq.iterate(0, i->i+1)
                                .take(a.length)
                                .map(indx->a[indx])
                                .flatMap(a2->{
                                    return ReactiveSeq.iterate(0, i->i+1)
                                            .take(a2.length)
                                            .map(indx->a2[indx])
                                            .flatMap(a3-> ReactiveSeq.of((T[])a3));
                                });
                    });
        }
    }
    @AllArgsConstructor
    public static class Five<T> implements PopulatedArray<T>{
        public static final int bitShiftDepth = 20;
        private final Object[][][][][] array;

        public static <T> Five<T> five(Object[][][][][] array){
            return new Five<T>(array);
        }

        @Override
        public PopulatedArray<T> set(int pos, T t) {
            Object[][][][][] n1 = Arrays.copyOf(array, array.length);
            int indx = NestedArray.mask(pos,bitShiftDepth);
            Object[][][][] n2 = n1[indx];
            Object[][][][] newNode = Arrays.copyOf(n2,n2.length);
            n1[indx] = newNode;
            int indx2 = NestedArray.mask(pos, Four.bitShiftDepth);
            Object[][][] n3 = n2[indx2];
            Object[][][] newNode2 = Arrays.copyOf(n3,n3.length);
            int indx3 = NestedArray.mask(pos, Three.bitShiftDepth);
            Object[][] n4 = n3[indx3];
            Object[][] newNode3 = Arrays.copyOf(n3,n3.length);
            int indx4 = NestedArray.mask(pos, Two.bitShiftDepth);
            Object[] n5 = n4[indx4];
            Object[] newNode4 = Arrays.copyOf(n4,n4.length);
            newNode3[indx4]=n4;
            n5[NestedArray.mask(pos)]=t;
            return five(n1);

        }

        @Override
        public Option<T> get(int pos) {
            T[] local = getNestedArrayAt(pos);
            int resolved = NestedArray.bitpos(pos,bitShiftDepth);
            int indx = pos & 0x01f;
            if(indx<local.length){
                return Option.of(local[indx]);
            }
            return Option.none();
        }
        @Override
        public T getOrElse(int pos, T alt) {
            T[] local = getNestedArrayAt(pos);
            int resolved = NestedArray.bitpos(pos,bitShiftDepth);
            int indx = pos & 0x01f;
            if(indx<local.length){
                return local[indx];
            }
            return alt;
        }
        @Override
        public T getOrElseGet(int pos, Supplier<T> alt) {
            T[] local = getNestedArrayAt(pos);
            int resolved = NestedArray.bitpos(pos,bitShiftDepth);
            int indx = pos & 0x01f;
            if(indx<local.length){
                return local[indx];
            }
            return alt.get();
        }
        @Override
        public T[] getNestedArrayAt(int pos) {


            int indx = NestedArray.mask(pos, bitShiftDepth);
            if(indx<array.length){
                Object[][][][] twoArray = array[indx];
                int indx2 = NestedArray.mask(pos, Four.bitShiftDepth);
                if(indx2<twoArray.length){
                    int indx3 = NestedArray.mask(pos, Three.bitShiftDepth);
                    Object[][][] threeArray = twoArray[indx2];
                    if(indx3<threeArray.length){
                        int indx4 = NestedArray.mask(pos, Two.bitShiftDepth);
                        Object[][] fourArray = threeArray[indx3];
                        if(indx4<fourArray.length) {
                            return (T[]) fourArray[indx4];
                        }
                    }
                }
            }

            return (T[])new Object[0];


        }


        @Override
        public NestedArray<T> append(ActiveTail<T> tail) {
            if(last(last(last(array))).length<32){
                Object[][][][][] updatedNodes = Arrays.copyOf(array, array.length,Object[][][][][].class);
                updatedNodes[updatedNodes.length-1]=Arrays.copyOf(last(updatedNodes), last(updatedNodes).length,Object[][][][].class);
                last(updatedNodes)[last(updatedNodes).length-1]=Arrays.copyOf(last(last(updatedNodes)), last(last(updatedNodes)).length,Object[][][].class);
                last(last(updatedNodes))[last(last(updatedNodes)).length-1]=Arrays.copyOf(last(last(last(updatedNodes))), last(last(last(updatedNodes))).length+1,Object[][].class);
                last(last(last(updatedNodes)))[last(last(last(array))).length] = tail.array;
                return five(updatedNodes);
            }
            if(last(last(array)).length<32){
                Object[][][][][] updatedNodes = Arrays.copyOf(array, array.length,Object[][][][][].class);
                updatedNodes[updatedNodes.length-1]=Arrays.copyOf(last(updatedNodes), last(updatedNodes).length,Object[][][][].class);
                last(updatedNodes)[last(updatedNodes).length-1]=Arrays.copyOf(last(last(updatedNodes)), last(last(updatedNodes)).length+1,Object[][][].class);
                last(last(updatedNodes))[last(last(array)).length] = new Object[][]{tail.array};
                return five(updatedNodes);

            }
            if(last(array).length<32){
                Object[][][][][] updatedNodes = Arrays.copyOf(array, array.length,Object[][][][][].class);
                updatedNodes[updatedNodes.length-1]=Arrays.copyOf(last(updatedNodes), last(updatedNodes).length+1,Object[][][][].class);
                last(updatedNodes)[last(array).length] = new Object[][][]{new Object[][]{tail.array}};
                return five(updatedNodes);

            }
            if(array.length<32){
                Object[][][][][] updatedNodes = Arrays.copyOf(array, array.length+1,Object[][][][][].class);
                updatedNodes[array.length] = new Object[][][][]{new Object[][][]{new Object[][]{tail.array}}};
                return five(updatedNodes);
            }
            return Six.six(new Object[][][][][][]{array,new Object[][][][][]{new Object[][][][]{new Object[][][]{new Object[][]{tail.array}}}}});
        }

        @Override
        public ReactiveSeq<T> stream() {
            return ReactiveSeq.iterate(0, i->i+1)
                    .take(array.length)
                    .map(indx->array[indx])
                    .flatMap(a->{
                        return ReactiveSeq.iterate(0, i->i+1)
                                .take(a.length)
                                .map(indx->a[indx])
                                .flatMap(a2->{
                                    return ReactiveSeq.iterate(0, i->i+1)
                                            .take(a2.length)
                                            .map(indx->a2[indx])
                                            .flatMap(a3->{
                                                return ReactiveSeq.iterate(0, i->i+1)
                                                .take(a3.length)
                                                .map(indx->a3[indx])
                                                .flatMap(a4-> ReactiveSeq.of((T[])a4));
                                            });
                                });
                    });
        }
    }
    @AllArgsConstructor
    public static class Six<T> implements PopulatedArray<T>{
        public static final int bitShiftDepth = 25;
        private final Object[][][][][][] array;

        public static <T> Six<T> six(Object[][][][][][] array){
            return new Six<T>(array);
        }

        @Override
        public PopulatedArray<T> set(int pos, T t) {
            Object[][][][][][] n1 = Arrays.copyOf(array, array.length);
            int indx = NestedArray.mask(pos,bitShiftDepth);
            Object[][][][][] n2 = n1[indx];
            Object[][][][][] newNode = Arrays.copyOf(n2,n2.length);
            n1[indx] = newNode;
            int indx2 = NestedArray.mask(pos, Five.bitShiftDepth);
            Object[][][][] n3 = n2[indx2];
            Object[][][][] newNode2 = Arrays.copyOf(n3,n3.length);
            int indx3 = NestedArray.mask(pos, Four.bitShiftDepth);
            Object[][][] n4 = n3[indx3];
            Object[][][] newNode3 = Arrays.copyOf(n3,n3.length);
            int indx4 = NestedArray.mask(pos, Three.bitShiftDepth);
            Object[][] n5 = n4[indx4];
            int indx5 = NestedArray.mask(pos, Two.bitShiftDepth);
            Object[] n6 = n5[indx5];
            Object[] newNode4 = Arrays.copyOf(n4,n4.length);
            newNode4[indx5]=n4;
            n6[NestedArray.mask(pos)]=t;
            return six(n1);

        }

        @Override
        public Option<T> get(int pos) {
            T[] local = getNestedArrayAt(pos);
            int resolved = NestedArray.bitpos(pos,bitShiftDepth);
            int indx = pos & 0x01f;
            if(indx<local.length){
                return Option.of(local[indx]);
            }
            return Option.none();
        }
        @Override
        public T getOrElse(int pos, T alt) {
            T[] local = getNestedArrayAt(pos);
            int resolved = NestedArray.bitpos(pos,bitShiftDepth);
            int indx = pos & 0x01f;
            if(indx<local.length){
                return local[indx];
            }
            return alt;
        }
        @Override
        public T getOrElseGet(int pos, Supplier<T> alt) {
            T[] local = getNestedArrayAt(pos);
            int resolved = NestedArray.bitpos(pos,bitShiftDepth);
            int indx = pos & 0x01f;
            if(indx<local.length){
                return local[indx];
            }
            return alt.get();
        }

        @Override
        public T[] getNestedArrayAt(int pos) {


            int indx = NestedArray.mask(pos, bitShiftDepth);
            if(indx<array.length){
                Object[][][][][] twoArray = array[indx];
                int indx2 = NestedArray.mask(pos, Five.bitShiftDepth);
                if(indx2<twoArray.length){
                    int indx3 = NestedArray.mask(pos, Four.bitShiftDepth);
                    Object[][][][] threeArray = twoArray[indx2];
                    if(indx3<threeArray.length){
                        int indx4 = NestedArray.mask(pos, Three.bitShiftDepth);
                        Object[][][] fourArray = threeArray[indx3];
                        if(indx4<fourArray.length) {
                            int indx5 = NestedArray.mask(pos, Two.bitShiftDepth);
                            Object[][] fiveArray = fourArray[indx4];
                            if(indx5<fiveArray.length)
                                return (T[]) fiveArray[indx5];
                        }
                    }
                }
            }

            return (T[])new Object[0];


        }


        @Override
        public NestedArray<T> append(ActiveTail<T> tail) {
            if(last(last(last(last(array)))).length<32){
                Object[][][][][][] updatedNodes = Arrays.copyOf(array, array.length,Object[][][][][][].class);
                updatedNodes[updatedNodes.length-1]=Arrays.copyOf(last(updatedNodes), last(updatedNodes).length,Object[][][][][].class);
                last(updatedNodes)[last(updatedNodes).length-1]=Arrays.copyOf(last(last(updatedNodes)), last(last(updatedNodes)).length,Object[][][][].class);
                last(last(updatedNodes))[last(last(updatedNodes)).length-1]=Arrays.copyOf(last(last(last(updatedNodes))), last(last(last(updatedNodes))).length,Object[][][].class);
                last(last(last(updatedNodes)))[last(last(last(updatedNodes))).length-1]=Arrays.copyOf(last(last(last(last(updatedNodes)))), last(last(last(last(updatedNodes)))).length+1,Object[][].class);
                last(last(last(last(updatedNodes))))[last(last(last(last(array)))).length] = tail.array;
                return six(updatedNodes);
            }
            if(last(last(last(array))).length<32){
                Object[][][][][][] updatedNodes = Arrays.copyOf(array, array.length,Object[][][][][][].class);
                updatedNodes[updatedNodes.length-1]=Arrays.copyOf(last(updatedNodes), last(updatedNodes).length,Object[][][][][].class);
                last(updatedNodes)[last(updatedNodes).length-1]=Arrays.copyOf(last(last(updatedNodes)), last(last(updatedNodes)).length,Object[][][][].class);
                last(last(updatedNodes))[last(last(updatedNodes)).length-1]=Arrays.copyOf(last(last(last(updatedNodes))), last(last(last(updatedNodes))).length+1,Object[][][].class);
                last(last(last(updatedNodes)))[last(last(last(array))).length] = new Object[][]{tail.array};
                return six(updatedNodes);

            }
            if(last(last(array)).length<32){
                Object[][][][][][] updatedNodes = Arrays.copyOf(array, array.length,Object[][][][][][].class);
                updatedNodes[updatedNodes.length-1]=Arrays.copyOf(last(updatedNodes), last(updatedNodes).length,Object[][][][][].class);
                last(updatedNodes)[last(updatedNodes).length-1]=Arrays.copyOf(last(last(updatedNodes)), last(last(updatedNodes)).length+1,Object[][][][].class);
                last(last(updatedNodes))[last(last(array)).length] = new Object[][][]{new Object[][]{tail.array}};
                return six(updatedNodes);

            }
            if(last(array).length<32){
                Object[][][][][][] updatedNodes = Arrays.copyOf(array, array.length,Object[][][][][][].class);
                updatedNodes[updatedNodes.length-1]=Arrays.copyOf(last(updatedNodes), last(updatedNodes).length+1,Object[][][][][].class);
                last(updatedNodes)[last(array).length] = new Object[][][][]{new Object[][][]{new Object[][]{tail.array}}};
                return six(updatedNodes);

            }
            if(array.length<32){
                Object[][][][][][] updatedNodes = Arrays.copyOf(array, array.length+1,Object[][][][][][].class);
                updatedNodes[array.length] = new Object[][][][][]{new Object[][][][]{new Object[][][]{new Object[][]{tail.array}}}};
                return six(updatedNodes);
            }
            return this; //BAMT is full
        }

        @Override
        public ReactiveSeq<T> stream() {
            return ReactiveSeq.iterate(0, i->i+1)
                    .take(array.length)
                    .map(indx->array[indx])
                    .flatMap(a->{
                        return ReactiveSeq.iterate(0, i->i+1)
                                .take(a.length)
                                .map(indx->a[indx])
                                .flatMap(a2->{
                                    return ReactiveSeq.iterate(0, i->i+1)
                                            .take(a2.length)
                                            .map(indx->a2[indx])
                                            .flatMap(a3->{
                                                return ReactiveSeq.iterate(0, i->i+1)
                                                        .take(a3.length)
                                                        .map(indx->a3[indx])
                                                        .flatMap(a4->{
                                                            return ReactiveSeq.iterate(0, i->i+1)
                                                                              .take(a4.length)
                                                                              .map(indx->a4[indx])
                                                                              .flatMap(a5-> ReactiveSeq.of((T[])a5));
                                                        });
                                            });
                                });
                    });
        }
    }
}
