package cyclops.data;


import com.oath.cyclops.types.traversable.IterableX;
import cyclops.companion.Reducers;
import cyclops.control.Option;
import cyclops.data.basetests.BaseImmutableListTest;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;

public class LazySeqTest extends BaseImmutableListTest {

    @Override
    protected <T> LazySeq<T> fromStream(Stream<T> s) {
        return LazySeq.fromStream(s);
    }

    @Override
    public <T> LazySeq<T> empty() {
        return LazySeq.empty();
    }

    @Override
    public <T> LazySeq<T> of(T... values) {
        return LazySeq.of(values);
    }

    int count =0;
    @Before
    public void setup(){
      super.setup();
      count =0;
    }



  @Test
  public void testLazy(){
    of(1,2,3,4).map(i->count++);
    assertThat(count,equalTo(1));
    count =0;
    of(1,2,3,4).flatMap(i->{
      System.out.println("here!");
      return LazySeq.of(count++);
    });
    assertThat(count,equalTo(1));
    count =0;
    of(1,2,3,4).concatMap(i->LazySeq.of(count++));
    assertThat(count,equalTo(1));
    count =0;
    of(1,2,3,4).filter(i->{
      count++;
      return i>0;
    });
    assertThat(count,equalTo(1));

    count =0;
    of(1,2,3,4).zip(of(1,2,3,4),(a,b)->{
      count++;
      return a;
    });
    assertThat(count,equalTo(1));



  }

  // TODO(johnmcclean): Stop ignoring this test
  @Ignore
  @Test
  public void testNoMapCalledBeforeFetchingAndOnlyOnceForEachInput() throws Exception {
    @SuppressWarnings("unchecked")
    Function<Integer, Integer> mockFunction = Mockito.mock(Function.class);
    Mockito.doAnswer(invocation -> {
      int arg = (Integer) invocation.getArguments()[0];
      return arg * 2;
    }).when(mockFunction).apply(anyInt());

    LazySeq<Integer> result = LazySeq.of(1, 2, 3, 4, 5)
      .map(mockFunction);

    InOrder inOrder = Mockito.inOrder(mockFunction);
    inOrder.verifyNoMoreInteractions();

    // Invoke twice to assert function called only once
    assertThat(result, hasItems(2, 4, 6, 8, 10));
    assertThat(result, hasItems(2, 4, 6, 8, 10));

    inOrder.verify(mockFunction).apply(1);
    inOrder.verify(mockFunction).apply(2);
    inOrder.verify(mockFunction).apply(3);
    inOrder.verify(mockFunction).apply(4);
    inOrder.verify(mockFunction).apply(5);
    inOrder.verifyNoMoreInteractions();
  }

  // TODO(johnmcclean): Stop ignoring this test
  @Ignore
  @Test
  public void testFirstMapCalledBeforeFetchingOthersAfterAndAllMapsCalledOnlyOnce() throws Exception {
    @SuppressWarnings("unchecked")
    Function<Integer, Integer> mockFunction = Mockito.mock(Function.class);
    Mockito.doAnswer(invocation -> {
      int arg = (Integer) invocation.getArguments()[0];
      return arg * 2;
    }).when(mockFunction).apply(anyInt());

    LazySeq<Integer> result = LazySeq.of(1, 2, 3, 4, 5)
      .map(mockFunction);

    InOrder inOrder = Mockito.inOrder(mockFunction);
    inOrder.verify(mockFunction).apply(1);
    inOrder.verifyNoMoreInteractions();

    // Invoke twice to assert function called only once
    assertThat(result, hasItems(2, 4, 6, 8, 10));
    assertThat(result, hasItems(2, 4, 6, 8, 10));

    inOrder.verify(mockFunction).apply(2);
    inOrder.verify(mockFunction).apply(3);
    inOrder.verify(mockFunction).apply(4);
    inOrder.verify(mockFunction).apply(5);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void mapLarge(){
    LazySeq.range(0,100_000_000).map(i->count++);
    assertThat(count,equalTo(1));
  }

    @Test
    public void split(){
        assertThat(of(1,2,3,2,3,2).split(i->i==2),equalTo(of(of(1),of(3),of(3))));
        assertThat(of(2,2,2,2,1,2,3,2,3,2).split(i->i==2),equalTo(of(of(1),of(3),of(3))));
        assertThat(of(1,10,2,3,10,2,3,10,2).split(i->i==2),equalTo(of(of(1,10),of(3,10),of(3,10))));
    }

    @Test
    public void splitLarge(){
        fromStream(ReactiveSeq.range(0,100_000)).split(i->i==2).printOut();
        fromStream(ReactiveSeq.range(0,15_000).intersperse(0)).split(i->i==0).printOut();
    }

    @Test
    public void testScanRightSumMonoid() {
        assertThat(of("a", "ab", "abc").peek(System.out::println)
                .map(str -> str.length())
                .peek(System.out::println)
                .scanRight(Reducers.toTotalInt()).toList(), is(asList(6,5,3,0)));

    }
    @Test
    public void retainAllStream(){
        /**
        System.out.println(of(1,2,3,4,5));
        System.out.println(of(1,2,3,4,5).retainAllS(Stream.of(1,2,3)));

        System.out.println(ReactiveSeq.fromIterator(of(1,2,3,4,5).retainAllS(Stream.of(1,2,3)).iterator()).toListX());
         **/
      //  assertThat(Arrays.asList(1,2,3),hasItems(1,2,3));

        ImmutableList<Integer> l = of(1, 2, 3, 4, 5).retainStream(Stream.of(1, 2, 3));
        for(Integer n : l)
            System.out.println("n is " +n);

        l.stream().forEach(System.out::println);

        System.out.println(l.stream().join(",","[","]"));
        l.stream().forEach(System.out::println);
        assertThat(l,hasItems(1,2,3));
    }


    @Test
    public void fromStreamTest(){
        ImmutableList<Integer> l = of(1,2,3,4,5).retainStream(Stream.of(1, 2, 3));


        for(Integer n : l) {
           // System.out.println("n is " + n);
        }

         assertThat(fromStream(Stream.of(1,2,3)),equalTo(of(1,2,3)));
    }

    @Override
    public LazySeq<Integer> range(int start, int end) {
        return LazySeq.range(start,end);
    }

    @Override
    public LazySeq<Long> rangeLong(long start, long end) {
        return LazySeq.rangeLong(start,end);
    }

    @Override
    public <T> ImmutableList<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return LazySeq.iterate(seed,fn,times);
    }

    @Override
    public <T> LazySeq<T> generate(int times, Supplier<T> fn) {
        return LazySeq.generate(fn,times);
    }

    @Override
    public <U, T> Seq<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return Seq.unfold(seed,unfolder);
    }
    @Test
    public void prependAllTest(){
        assertThat(LazySeq.of(1,2,3).prependAll(LazySeq.of(4,5,6)),equalTo(LazySeq.of(4,5,6,1,2,3)));
    }

    @Test
    public void scanRight(){
        LazySeq.of(1,2,3).scanRight(0,(a, b)->a+b).printOut();
    }



    @Test
    public void plusAll(){
        IterableX<Integer> vec = this.<Integer>empty().plusAll(Arrays.asList(1)).plusAll(Arrays.asList(2)).plusAll(Arrays.asList(5));

        MatcherAssert.assertThat(vec,equalTo(of(5,2,1)));
    }
    @Test
    public void plus(){
        IterableX<Integer> vec = this.<Integer>empty().plus(1).plus(2).plus(5);

        Assert.assertThat(vec,equalTo(Vector.of(5,2,1)));
    }

    @Test
    public void toStringTest(){
        assertThat(of().toString(),equalTo("[]"));
        assertThat(of(1).toString(),equalTo("{1...}"));
    }


}
