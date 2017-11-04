package cyclops.monads;

import com.oath.cyclops.data.collections.extensions.IndexedSequenceX;
import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.types.stream.HeadAndTail;
import cyclops.async.Future;
import cyclops.async.LazyReact;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.SetX;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.monads.Witness.reactiveSeq;
import cyclops.monads.transformers.ListT;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cyclops.reactive.ReactiveSeq.of;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class StreamTest {
  @Test
  public void anyMTest(){
    List<Integer> list = AnyM.fromStream(Spouts.of(1,2,3,4,5,6)).filter(i->i>3).stream().toList();

    assertThat(list,equalTo(Arrays.asList(4,5,6)));
  }
  int peek = 0;
  @Test
  public void testPeek() {
    peek = 0 ;
    AnyM.fromStream(Stream.of(asList(1,3)))
      .flatMap(c->AnyM.fromStream(c.stream()))
      .stream()
      .map(i->i*2)
      .peek(i-> peek=i)
      .collect(Collectors.toList());
    assertThat(peek,equalTo(6));
  }
  @Test
  public void testMap() {
    List<Integer> list = AnyM.fromStream(Stream.of(asList(1,3)))
      .flatMap(c->AnyM.fromStream(c.stream()))
      .stream()
      .map(i->i*2)
      .peek(System.out::println)
      .collect(Collectors.toList());
    assertThat(Arrays.asList(2,6),equalTo(list));
  }
  @Test
  public void flatMapCompletableFuture(){
    assertThat(Spouts.of(1,2,3).flatMapI(i-> AnyM.fromArray(i+2))
        .collect(Collectors.toList()),
      equalTo(Arrays.asList(3,4,5)));
  }
  @Test
  public void anyMIteration(){
    Iterator<Integer> it = AnyM.fromStream(ReactiveSeq.of(1,2,3))
      .combine((a, b)->a.equals(b), Semigroups.intSum)
      .iterator();
    List<Integer> list = new ArrayList<>();
    while(it.hasNext()){
      list.add(it.next());
    }

    assertThat(list,equalTo(ListX.of(1,2,3)));
  }
  @Test
  public void combineNoOrderAnyM(){
    assertThat(AnyM.fromStream(ReactiveSeq.of(1,2,3))
      .combine((a, b)->a.equals(b), Semigroups.intSum)
      .toListX(),equalTo(ListX.of(1,2,3)));

  }

  @Test
  public void groupedT(){

    ListT<reactiveSeq,Integer> nestedList = AnyM.fromStream(ReactiveSeq.of(1,2,3,4,5,6,7,8,9,10))
      .groupedT(2)
      .map(i->i*2);

    ListX<ListX<String>> listOfLists = nestedList.map(i->"nest:"+i)
      .toListOfLists();
    System.out.println(listOfLists);

    //[[nest:2, nest:4], [nest:6, nest:8], [nest:10, nest:12], [nest:14, nest:16], [nest:18, nest:20]]



  }

  @Test
  public void listT(){


    ListT<Witness.set,Integer> nestedList = ListT.fromSet(SetX.of(ListX.of(11,22),ListX.of(100,200)));

    ListT<Witness.set,Integer> doubled = nestedList.map(i->i*2);
    System.out.println(doubled);

    //ListTSeq[AnyMSeq[[[22, 44], [200, 400]]]]

  }
  @Test
  public void comonad(){
    AnyM.fromOptional(Optional.of(1))
      .coflatMap(v->v.isPresent()?v.toOptional().get() : 10);

  }
  @Test
  public void listTExample(){


    ReactiveSeq.of(10,20,30)
      .sliding(2,1)
      .map(list->list.map(i->i*2)
        .map(this::loadData))
      .forEach(list->System.out.println("next list " + list));

    ReactiveSeq.of(10,20,30,40,50)
      .slidingT(2,1)  //create a sliding view, returns a List Transformer
      .map(i->i*2)  //we now have a Stream of Lists, but still operate on each individual integer
      .map(this::loadData)
      .unwrap()
      .forEach(list->System.out.println("next list " + list));


  }
  @Test
  public void anyMTest(){
    List<Integer> list = LazyReact.sequentialBuilder().of(1,2,3,4,5,6)
      .anyM().filter(i->i>3).stream().toList();

    assertThat(list,equalTo(Arrays.asList(4,5,6)));
  }
  @Test
  public void nestedComps(){


    ListT<Witness.list,Integer> xxs = ListT.fromList(ListX.of(ListX.of(1,3,5,2,3,1,2,4,5),
      ListX.of(1,2,3,4,5,6,7,8,9),
      ListX.of(1,2,4,2,1,6,3,1,3,2,3,6)));

    ListX<IndexedSequenceX<Integer>> list = xxs.filter(i -> i % 2 == 0)
      .unwrapTo(Witness::list);


    //ListX[[2,2,4],[2,4,6,8],[2,4,2,6,2,6]]

  }
  @Test
  public void testFutureFlatMapIterable() {
    Future<Integer> just = Future.of(CompletableFuture.completedFuture(10));

    Future<Integer> f = just.flatMapI(i -> Arrays.asList(i, 20, 30));
    assertThat(f.orElse(-10), equalTo(10));

    f = just.flatMapI(i -> AnyM.fromStream(ReactiveSeq.of(20, i, 30)));
    assertThat(f.orElse(-50), equalTo(20));
  }
  @Test
  public void headTailReplay(){

    ReactiveSeq<String> helloWorld = AnyM.streamOf("hello","world","last").stream();
    HeadAndTail<String> headAndTail = helloWorld.headAndTail();
    String head = headAndTail.head();
    assertThat(head,equalTo("hello"));

    ReactiveSeq<String> tail =  headAndTail.tail();
    assertThat(tail.headAndTail().head(),equalTo("world"));

  }
  @Test
  public void zipOptional(){
    Stream<List<Integer>> zipped = AnyMs.zipAnyM(Stream.of(1,2,3)
      ,AnyM.fromArray(2),
      (a,b) -> Arrays.asList(a,b));


    List<Integer> zip = zipped.collect(Collectors.toList()).get(0);
    assertThat(zip.get(0),equalTo(1));
    assertThat(zip.get(1),equalTo(2));

  }
  @Test
  public void zipOptionalSequence(){
    Stream<List<Integer>> zipped = AnyMs.zipAnyM(Stream.of(1,2,3)
      ,AnyM.fromArray(2),
      (a,b) -> Arrays.asList(a,b));


    List<Integer> zip = zipped.collect(Collectors.toList()).get(0);
    assertThat(zip.get(0),equalTo(1));
    assertThat(zip.get(1),equalTo(2));

  }
  @Test
  public void lazy(){


    ListX<VectorX<String>> list =     ListX.of(1,2,3,5,6,7,8)
      .map(i->i*2)
      .filter(i->i<4)
      .sliding(2)
      .map(vec -> vec.map(i->"value is " + i));


    ListX.of(1,2,3,5,6,7,8)
      .map(i->i*2)
      .filter(i->i<4)
      .slidingT(2)
      .map(i->"value is " + i)
      .unwrap()
      .to(Witness::reactiveSeq);

  }

}
