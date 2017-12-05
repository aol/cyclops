package cyclops.futurestream.react.lazy.sequence;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import cyclops.futurestream.react.lazy.DuplicationTest;
import org.junit.Test;

import cyclops.reactive.ReactiveSeq;
import com.oath.cyclops.types.stream.HeadAndTail;

public class HeadTailTest {

	@Test
	public void headTailReplay(){

		ReactiveSeq<String> helloWorld = DuplicationTest.of("hello","world","last");
		HeadAndTail<String> headAndTail = helloWorld.headAndTail();
		 String head = headAndTail.head();
		 assertThat(head,equalTo("hello"));

		ReactiveSeq<String> tail =  headAndTail.tail();
		assertThat(tail.headAndTail().head(),equalTo("world"));

	}

}
