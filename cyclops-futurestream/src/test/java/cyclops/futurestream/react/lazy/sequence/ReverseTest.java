package cyclops.futurestream.react.lazy.sequence;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import cyclops.futurestream.react.lazy.DuplicationTest;
import org.junit.Test;

import cyclops.futurestream.LazyReact;

public class ReverseTest {
	@Test
	public void limitRange() throws InterruptedException{

		assertThat(new LazyReact().range(0,Integer.MAX_VALUE)
				 .limit(100)
				 .count(),equalTo(100L));
	}
	@Test
	public void limitList() throws InterruptedException{

		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(new LazyReact().fromIterable(list)
				 .limit(100)
				 .peek(System.out::println)
				 .count(),equalTo(100L));

	}
	@Test
	public void limitArray() throws InterruptedException{

		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(DuplicationTest.of(list.toArray())
				 .limit(100)
				 .count(),equalTo(100L));

	}
	@Test
	public void skipArray() throws InterruptedException{

		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(DuplicationTest.of(list.toArray())
				 .skip(100)
				 .count(),equalTo(900L));

	}
	@Test
	public void skipRange() throws InterruptedException{

		assertThat(new LazyReact().range(0,1000)
				 .skip(100)
				 .count(),equalTo(900L));
	}
	@Test
	public void skipList() throws InterruptedException{

		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(new LazyReact().fromIterable(list)
				 .skip(100)
				 .count(),equalTo(900L));

	}


}
