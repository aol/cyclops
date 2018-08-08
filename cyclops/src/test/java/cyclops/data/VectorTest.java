package cyclops.data;

import cyclops.control.Maybe;
import cyclops.data.base.BAMT;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.control.Option;
import cyclops.data.basetests.BaseImmutableListTest;
import org.hamcrest.MatcherAssert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;



public class VectorTest extends BaseImmutableListTest {
    @Override
    protected <T> Vector<T> fromStream(Stream<T> s) {
        return Vector.fromStream(s);
    }

    @Test
    public void dropSize(){
        System.out.println(of(1,2,3).drop(2));
        System.out.println(of(1,2,3).drop(2).size());
        assertThat(of(1,2,3).drop(2).size(),equalTo(1));
    }

    @Test
    public void map3(){
        Vector.range(0, 10000).map(i->i*2);
    }
    @Test
    public void types(){
        System.out.println(of(1,2,3).splitAt(2)._1().getClass());
        System.out.println(of(1,2,3).splitAt(2)._1());
        System.out.println(of(1,2,3).splitAt(2)._2().getClass());
        System.out.println(of(1,2,3).splitAt(2)._2());

        System.out.println(Tuple.tuple(of(1,2),of(3))._1().getClass());
        System.out.println(Tuple.tuple(of(1,2),of(3))._1());
        System.out.println(Tuple.tuple(of(1,2),of(3))._2().getClass());
        assertThat(of(1,2,3).splitAt(2)._1() ,equalTo(of(1,2)));
        ImmutableList<Integer> three = of(1,2,3).splitAt(2)._2();
        three.equals(of(3));
        assertThat(of(1,2,3).splitAt(2)._2() ,equalTo(of(3)));
        assertThat(of(1,2,3).splitAt(2) ,equalTo(Tuple.tuple(of(1,2),of(3))));
    }
    @Test
    public void equalsAndHash(){
        LinkedList<Integer> l = new LinkedList<>();
        ArrayList<Integer> al = new ArrayList<>();

        l.add(1);
        al.add(1);
        assertThat(l,equalTo(al));
        assertThat(l.hashCode(),equalTo(al.hashCode()));


        assertThat(Vector.of(1,2),equalTo(Seq.of(1,2)));
        assertThat(Vector.of(1,2).hashCode(),equalTo(Seq.of(1,2).hashCode()));

        assertThat(Vector.of(1,2),equalTo(LazySeq.of(1,2)));
        assertThat(Vector.of(1,2).hashCode(),equalTo(LazySeq.of(1,2).hashCode()));

        assertThat(Vector.of(1,2),equalTo(IntMap.of(1,2)));
        assertThat(Vector.of(1,2).hashCode(),equalTo(IntMap.of(1,2).hashCode()));
    }

    @Override
    public <T> Vector<T> empty() {
        return Vector.empty();
    }

    @Override
    public <T> Vector<T> of(T... values) {
        return Vector.of(values);
    }

    @Override
    public Vector<Integer> range(int start, int end) {
        return Vector.range(start,end);
    }

    @Override
    public Vector<Long> rangeLong(long start, long end) {
        return Vector.rangeLong(start,end);
    }

    @Override
    public <T> ImmutableList<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return Vector.iterate(seed,fn,times);
    }

    @Override
    public <T> Vector<T> generate(int times, Supplier<T> fn) {
        return Vector.generate(fn,times);
    }

    @Override
    public <U, T> Vector<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return Vector.unfold(seed,unfolder);
    }
    @Test
    public void map2depth(){
        //Vector.range(0, 10000)
        Vector.range(0, 32)
            .map(i -> i * 2)

            .foldLeft((a, b) -> a + b);
    }
    @Test
    public void map3depth(){
        Vector.range(0, 2000)
            .map(i -> i * 2)
            .foldLeft((a, b) -> a + b);
    }
    @Test
    public void map4depth(){
        Vector.range(0, 10000)
            .map(i -> i * 2)
            .foldLeft((a, b) -> a + b);
    }

    @Test
    public void map5depth(){
        Vector.range(0, 5000000)
            .map(i -> i * 2)
            .foldLeft((a, b) -> a + b);
    }
    @Test
    public void npe4D(){
        Vector.range(0, 10000)
     //  Vector.range(0, 32)
        //    .map(i -> i * 2)
            .concatMap(i->Vector.range(0,10))
            .map(i -> i * 2)
         //   .filter(i -> i < 5000)
         //   .map(i -> "hello " + i)
         //   .map(i -> i.length())
            .foldLeft((a, b) -> a + b);
    }
    @Test
    public void npe3D(){
        Vector.range(0, 1000)
            //  Vector.range(0, 32)
            //    .map(i -> i * 2)
            .concatMap(i->Vector.range(0,10))
            .map(i -> i * 2)
            //   .filter(i -> i < 5000)
            //   .map(i -> "hello " + i)
            //   .map(i -> i.length())
            .foldLeft((a, b) -> a + b);
    }
    @Test
    public void map3D(){

        assertThat(Vector.range(0, 10000)

                .map(i -> i * 2),equalTo(Seq.range(0,10000).map(i->i*2)));


    }
    @Test
    public void map2D(){
        for(int i=32;i<1024;i++) {
            System.out.println("I is " + i);
            assertThat(Vector.range(0, i)

                .map(n -> n * 2), equalTo(Seq.range(0, i).map(n -> n * 2)));
        }


    }
    @Test
    public void map1D(){

        for(int i=0;i<32;i++) {
            assertThat(Vector.range(0, i)

                .map(n -> n * 2), equalTo(Seq.range(0, i).map(n -> n * 2)));
        }


    }
    @Test
    public void testVector(){
        Vector<Integer> ints = Vector.<Integer>empty().plus(1);
        assertThat(ints.get(0),equalTo(Option.some(1)));
    }
    @Test
    public void testVector100(){
        Vector<Integer> ints = Vector.<Integer>empty();
        for(int i=0;i<1025;i++){
            ints = ints.plus(i);
        }

        assertThat(ints.get(0),equalTo(Option.some(0)));
        assertThat(ints.get(900),equalTo(Option.some(900)));
    }

    @Test
    public void last(){
        Object[] array = {"hello","world"};
        assertThat(BAMT.ArrayUtils.last(array),equalTo("world"));
    }
    @Test
    public void test3Pow(){
        Vector<Integer> ints = Vector.<Integer>empty();

        int p  = Double.valueOf(Math.pow(2,15)).intValue();
        for(int i=0;i<p;i++){
            System.out.println(i);
            ints = ints.plus(i);
        }
        for(int i=0;i<p;i++){
            assertThat(ints.get(i),equalTo(Option.some(i)));
        }


        final Vector<Integer> finalRef = ints;
        ints.stream().forEach(next-> {
                    assertThat(finalRef.get(next), equalTo(Option.some(next)));
                }
        );
    }


    @Test
    public void test3PowSet(){
        Vector<Integer> ints = Vector.<Integer>empty();

        int p  = Double.valueOf(Math.pow(2,15)).intValue();
        for(int i=0;i<p;i++){

            ints = ints.plus(i);
        }
        for(int i=0;i<p;i++){
            System.out.println(i);
            ints = ints.updateAt(i,i*2);
        }
        for(int i=0;i<p;i++){
            assertThat(ints.get(i),equalTo(Option.some(i*2)));
        }

    }
    @Test @Ignore
    public void test4Pow(){
        Vector<Integer> ints = Vector.<Integer>empty();

        int p  = Double.valueOf(Math.pow(2,20)).intValue();
        for(int i=0;i<p;i++){
            ints = ints.plus(i);
        }
        for(int i=0;i<p;i++){
            assertThat(ints.get(i),equalTo(Option.some(i)));
        }

        final Vector<Integer> finalRef = ints;
        ints.stream().forEach(next-> {
                    assertThat(finalRef.get(next), equalTo(Option.some(next)));
                }
        );
    }

    @Test @Ignore
    public void test4PowSet(){
        Vector<Integer> ints = Vector.<Integer>empty();

        int p  = Double.valueOf(Math.pow(2,20)).intValue();
        for(int i=0;i<p;i++){


            ints = ints.plus(i);
        }
        for(int i=0;i<p;i++){
            ints = ints.updateAt(i,i*2);
        }
        for(int i=0;i<p;i++){
            assertThat(ints.get(i),equalTo(Option.some(i*2)));
        }

    }
    @Test @Ignore
    public void test5Pow(){
        Vector<Integer> ints = Vector.<Integer>empty();

        int p  = Double.valueOf(Math.pow(2,25)).intValue();
        for(int i=0;i<p;i++){
            ints = ints.plus(i);
        }
        for(int i=0;i<p;i++){
            assertThat(ints.get(i),equalTo(Option.some(i)));
        }

        final Vector<Integer> finalRef = ints;
        ints.stream().forEach(next-> {
                    assertThat(finalRef.get(next), equalTo(Option.some(next)));
                }
        );
    }

    @Test @Ignore
    public void test5PowSet(){
        Vector<Integer> ints = Vector.<Integer>empty();

        int p  = Double.valueOf(Math.pow(2,25)).intValue();
        for(int i=0;i<p;i++){


            ints = ints.plus(i);
        }
        for(int i=0;i<p;i++){
            ints = ints.updateAt(i,i*2);
        }
        for(int i=0;i<p;i++){
            assertThat(ints.get(i),equalTo(Option.some(i*2)));
        }

    }
    @Test @Ignore
    public void test6Pow(){
        Vector<Integer> ints = Vector.<Integer>empty();

        int p  = Double.valueOf(Math.pow(2,30)).intValue();
        for(int i=0;i<p;i++){
            ints = ints.plus(i);
        }
        for(int i=0;i<p;i++){
            assertThat(ints.get(i),equalTo(Option.some(i)));
        }

        final Vector<Integer> finalRef = ints;
        ints.stream().forEach(next-> {
                    assertThat(finalRef.get(next), equalTo(Option.some(next)));
                }
        );
    }

    @Test @Ignore
    public void test6PowSet(){
        Vector<Integer> ints = Vector.<Integer>empty();

        int p  = Double.valueOf(Math.pow(2,30)).intValue()/2;
        for(int i=0;i<p;i++){



            ints = ints.plus(i);
            for(int x=0;x<i;x++){
                assertThat(ints.get(x),equalTo(Option.some(x)));
            }

        }
        for(int i=0;i<p;i++){
            ints = ints.updateAt(i,i*2);
        }
        for(int i=0;i<p;i++){
            assertThat(ints.get(i),equalTo(Option.some(i*2)));
        }

    }


    @Test
    public void shift(){
        System.out.println("Shift "+(1 >>> 5) + " and " + ((1 >>> 5) & 31));
        System.out.println("Shift "+(32 >>> 5) + " and " + ((32 >>> 5) & 31));
        System.out.println("Shift "+(64 >>> 5) + " and " + ((64 >>> 5) & 31));
        System.out.println("Shift "+(96 >>> 5) + " and " + ((96 >>> 5) & 31));
        System.out.println("Shift "+(128 >>> 5) + " and " + ((128 >>> 5) & 31));

    }
    @Test
    public void shift2(){
        for(int i=0;i<10000;i++){
            System.out.println("I is " +  i +  "Shift "+(i >>> 5) + " and " + ((i >>> 5) & 31));
        }



    }

}
