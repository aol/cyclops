package cyclops.monads.anym;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import cyclops.monads.AnyM;
import cyclops.reactive.ReactiveSeq;
public class StreamTest {
	 public static <U> ReactiveSeq<U> of(U... array){
		 return AnyM.streamOf(array).stream();
	 }

	@Test
	public void testAnyMatch(){
		assertThat(of(1,2,3,4,5).anyMatch(it-> it.equals(3)),equalTo(true));
	}
	@Test
	public void testAllMatch(){
		assertThat(of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
	}
	@Test
	public void testNoneMatch(){
		assertThat(of(1,2,3,4,5).noneMatch(it-> it==5000),equalTo(true));
	}


	@Test
	public void testAnyMatchFalse(){
		assertThat(of(1,2,3,4,5).anyMatch(it-> it.equals(8)),equalTo(false));
	}
	@Test
	public void testAllMatchFalse(){
		assertThat(of(1,2,3,4,5).allMatch(it-> it<0 && it >6),equalTo(false));
	}
	@Test
	public void testFlatMap(){
		assertThat(of( asList("1","10"), asList("2"),asList("3"),asList("4")).flatMapStream( list -> list.stream() ).collect(Collectors.toList()
						),hasItem("10"));
	}

	@Test
	public void testMapReduce(){
		assertThat(of(1,2,3,4,5).map(it -> it*100).reduce( (acc,next) -> acc+next).get(),equalTo(1500));
	}
	@Test
	public void testMapReduceSeed(){
		assertThat(of(1,2,3,4,5).map(it -> it*100).reduce( 50,(acc,next) -> acc+next),equalTo(1550));
	}


	@Test
	public void testMapReduceCombiner(){
		assertThat(of(1,2,3,4,5).map(it -> it*100).reduce( 0,
                (acc, next) -> acc+next,
                Integer::sum),equalTo(1500));
	}
	@Test
	public void testFindFirst(){
		assertThat(Arrays.asList(1,2,3),hasItem(of(1,2,3,4,5).filter(it -> it <3).findFirst().get()));
	}
	@Test
	public void testFindAny(){
		assertThat(Arrays.asList(1,2,3),hasItem(of(1,2,3,4,5).filter(it -> it <3).findAny().get()));
	}
	@Test
	public void testDistinct(){
		assertThat(of(1,1,1,2,1).distinct().collect(Collectors.toList()).size(),equalTo(2));
		assertThat(of(1,1,1,2,1).distinct().collect(Collectors.toList()),hasItem(1));
		assertThat(of(1,1,1,2,1).distinct().collect(Collectors.toList()),hasItem(2));
	}

	@Test
	public void testLimit(){
		assertThat(of(1,2,3,4,5).limit(2).collect(Collectors.toList()).size(),equalTo(2));
	}
	@Test
	public void testSkip(){
		assertThat(of(1,2,3,4,5).skip(2).collect(Collectors.toList()).size(),equalTo(3));
	}
	@Test
    public void testTake(){
        assertThat(of(1,2,3,4,5).take(2).collect(Collectors.toList()).size(),equalTo(2));
    }
    @Test
    public void testDrop(){
        assertThat(of(1,2,3,4,5).drop(2).collect(Collectors.toList()).size(),equalTo(3));
    }
	@Test
	public void testMax(){
		assertThat(of(1,2,3,4,5).maximum((t1, t2) -> t1-t2).orElse(-1),equalTo(5));
	}
	@Test
	public void testMin(){
		assertThat(of(1,2,3,4,5).minimum((t1, t2) -> t1-t2).orElse(-10),equalTo(1));
	}

	@Test
	public void testMapToInt(){
		assertThat(of("1","2","3","4").mapToInt(it -> Integer.valueOf(it)).max().getAsInt(),equalTo(4));

	}

	@Test
	public void mapToLong() {
		assertThat(of("1","2","3","4").mapToLong(it -> Long.valueOf(it)).max().getAsLong(),equalTo(4l));
	}

	@Test
	public void mapToDouble() {
		assertThat(of("1","2","3","4").mapToDouble(it -> Double.valueOf(it)).max().getAsDouble(),equalTo(4d));
	}


	@Test
	public void flatMapToInt() {
		assertThat(of( asList("1","10"), asList("2"),asList("3"),asList("4"))
				.flatMapToInt(list ->list.stream()
						.mapToInt(Integer::valueOf)).max().getAsInt(),equalTo(10));
	}


	@Test
	public void flatMapToLong() {
		assertThat(of( asList("1","10"), asList("2"),asList("3"),asList("4"))
				.flatMapToLong(list ->list.stream().mapToLong(Long::valueOf)).max().getAsLong(),equalTo(10l));

	}


	@Test
	public void flatMapToDouble(){

		assertThat(of( asList("1","10"),
				asList("2"),asList("3"),asList("4"))
				.flatMapToDouble(list ->list.stream()
						.mapToDouble(Double::valueOf))
						.max().getAsDouble(),equalTo(10d));
	}

	@Test
	public void sorted() {
		assertThat(of(1,5,3,4,2).sorted().collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test
	public void sortedComparator() {
		assertThat(of(1,5,3,4,2).sorted((t1,t2) -> t2-t1).collect(Collectors.toList()),equalTo(Arrays.asList(5,4,3,2,1)));
	}
	@Test
	public void forEach() {
		List<Integer> list = new ArrayList<>();
		of(1,5,3,4,2).forEach(it-> list.add(it));
		assertThat(list,hasItem(1));
		assertThat(list,hasItem(2));
		assertThat(list,hasItem(3));
		assertThat(list,hasItem(4));
		assertThat(list,hasItem(5));

	}
	@Test
	public void forEachOrderedx() {
		List<Integer> list = new ArrayList<>();
		of(1,5,3,4,2).forEachOrdered(it-> list.add(it));
		assertThat(list,hasItem(1));
		assertThat(list,hasItem(2));
		assertThat(list,hasItem(3));
		assertThat(list,hasItem(4));
		assertThat(list,hasItem(5));

	}

	@Test
	public void testToArray() {
		assertThat( Arrays.asList(1,2,3,4,5),hasItem(of(1,5,3,4,2).toArray()[0]));
	}
	@Test
	public void testToArrayGenerator() {
		assertThat( Arrays.asList(1,2,3,4,5),hasItem(of(1,5,3,4,2).toArray(it->new Integer[it])[0]));
	}

	@Test
	public void testCount(){
		assertThat(of(1,5,3,4,2).count(),equalTo(5L));
	}

	@Test
	public void collectSBB(){

		List<Integer> list = of(1,2,3,4,5).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		assertThat(list.size(),equalTo(5));
	}
	@Test
	public void collect(){
		assertThat(of(1,2,3,4,5).collect(Collectors.toList()).size(),equalTo(5));
		assertThat(of(1,1,1,2).collect(Collectors.toSet()).size(),equalTo(2));
	}
	@Test
	public void testFilter(){
		assertThat(of(1,1,1,2).filter(it -> it==1).collect(Collectors.toList()).size(),equalTo(3));
	}
	@Test
	public void testMap(){
		assertThat(of(1).map(it->it+100).collect(Collectors.toList()).get(0),equalTo(101));
	}
	Object val;
	@Test
	public void testPeek(){
		val = null;
		of(1).map(it->it+100).peek(it -> val=it).collect(Collectors.toList());
		assertThat(val,equalTo(101));
	}



}
