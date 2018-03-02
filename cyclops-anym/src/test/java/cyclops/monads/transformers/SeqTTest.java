package cyclops.monads.transformers;

import cyclops.data.Seq;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class SeqTTest {

	String result = null;


	@Test
	public void filterFail(){
		SeqT<Witness.optional,Integer> streamT = SeqT.ofList(AnyM.fromOptional(Optional.of(Seq.of(10))));
		assertThat(streamT.filter(num->num<10).unwrap().<Optional<Seq<String>>>unwrap()
						.get(),  equalTo(Seq.of()));

        AnyM<Witness.optional, Seq<Integer>> anyM = streamT.unwrap();
        Optional<Seq<Integer>> opt = Witness.optional(anyM);


	}
	@Test
	public void filterSuccess(){
		SeqT<Witness.optional,Integer> streamT = SeqT.ofList(AnyM.fromOptional(Optional.of(Seq.of(10))));
		assertThat(streamT.filter(num->num==10).unwrap().<Optional<Seq<String>>>unwrap()
						.get(),  equalTo(Seq.of(10)));
	}
	@Test
	public void peek() {
		result = null;
		SeqT<Witness.optional,Integer> streamT = SeqT.ofList(AnyM.fromOptional(Optional.of(Seq.of(10))));

		streamT.peek(num->result = "hello world"+num)
				.unwrap().<Optional<Seq<String>>>unwrap().get().get(0);
		assertThat(result,  equalTo("hello world10"));
	}
	@Test
	public void map() {
		SeqT<Witness.optional,Integer> streamT = SeqT.ofList(AnyM.fromOptional(Optional.of(Seq.of(10))));
		assertThat(streamT.map(num->"hello world"+num)
						.unwrap().<Optional<Seq<String>>>unwrap()
						.get(),  equalTo(Seq.of("hello world10")));
	}


}
