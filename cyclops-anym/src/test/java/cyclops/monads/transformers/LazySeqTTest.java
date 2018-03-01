package cyclops.monads.transformers;

import cyclops.data.LazySeq;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class LazySeqTTest {

	String result = null;


	@Test
	public void filterFail(){
		LazySeqT<Witness.optional,Integer> streamT = LazySeqT.ofList(AnyM.fromOptional(Optional.of(LazySeq.of(10))));
		assertThat(streamT.filter(num->num<10).unwrap().<Optional<LazySeq<String>>>unwrap()
						.get(),  equalTo(LazySeq.of()));

        AnyM<Witness.optional, LazySeq<Integer>> anyM = streamT.unwrap();
        Optional<LazySeq<Integer>> opt = Witness.optional(anyM);


	}
	@Test
	public void filterSuccess(){
		LazySeqT<Witness.optional,Integer> streamT = LazySeqT.ofList(AnyM.fromOptional(Optional.of(LazySeq.of(10))));
		assertThat(streamT.filter(num->num==10).unwrap().<Optional<LazySeq<String>>>unwrap()
						.get(),  equalTo(LazySeq.of(10)));
	}
	@Test
	public void peek() {
		result = null;
		LazySeqT<Witness.optional,Integer> streamT = LazySeqT.ofList(AnyM.fromOptional(Optional.of(LazySeq.of(10))));

		streamT.peek(num->result = "hello world"+num)
				.unwrap().<Optional<LazySeq<String>>>unwrap().get().get(0);
		assertThat(result,  equalTo("hello world10"));
	}
	@Test
	public void map() {
		LazySeqT<Witness.optional,Integer> streamT = LazySeqT.ofList(AnyM.fromOptional(Optional.of(LazySeq.of(10))));
		assertThat(streamT.map(num->"hello world"+num)
						.unwrap().<Optional<LazySeq<String>>>unwrap()
						.get(),  equalTo(LazySeq.of("hello world10")));
	}


}
