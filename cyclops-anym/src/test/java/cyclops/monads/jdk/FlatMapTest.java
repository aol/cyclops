package cyclops.monads.jdk;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.Test;

import cyclops.monads.AnyM;
public class FlatMapTest {


	@Test
	public void flatMap(){
		assertThat(AnyM.fromStream(Stream.of(1,2,3)).flatMap(i->AnyM.fromStream(Stream.of(i))).stream().toList(),equalTo(Arrays.asList(1,2,3)));
	}

	@Test
	public void flatMapToSeq(){

		assertThat(AnyM.fromStream(Stream.of(1,2,3)).flatMap(i-> AnyM.fromStream(Stream.of(i+2))).stream().toList(),equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapSeqToStream(){

		assertThat(AnyM.fromStream(Stream.of(1,2,3)).flatMap(i-> AnyM.fromStream(Stream.of(i+2))).stream().toList(),equalTo(Arrays.asList(3,4,5)));
	}



}
