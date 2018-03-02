package cyclops.monads.transformers;

import com.oath.cyclops.data.collections.extensions.IndexedSequenceX;
import cyclops.data.Vector;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.reactive.companion.Converters;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class VectorTTest {

	String result = null;


	@Test
	public void filterFail(){
		VectorT<Witness.optional,Integer> streamT = VectorT.ofList(AnyM.fromOptional(Optional.of(Vector.of(10))));
		assertThat(streamT.filter(num->num<10).unwrap().<Optional<Vector<String>>>unwrap()
						.get(),  equalTo(Vector.of()));

        AnyM<Witness.optional, Vector<Integer>> anyM = streamT.unwrap();
        Optional<Vector<Integer>> opt = Witness.optional(anyM);


	}
	@Test
	public void filterSuccess(){
		VectorT<Witness.optional,Integer> streamT = VectorT.ofList(AnyM.fromOptional(Optional.of(Vector.of(10))));
		assertThat(streamT.filter(num->num==10).unwrap().<Optional<Vector<String>>>unwrap()
						.get(),  equalTo(Vector.of(10)));
	}
	@Test
	public void peek() {
		result = null;
		VectorT<Witness.optional,Integer> streamT = VectorT.ofList(AnyM.fromOptional(Optional.of(Vector.of(10))));

		streamT.peek(num->result = "hello world"+num)
				.unwrap().<Optional<Vector<String>>>unwrap().get().get(0);
		assertThat(result,  equalTo("hello world10"));
	}
	@Test
	public void map() {
		VectorT<Witness.optional,Integer> streamT = VectorT.ofList(AnyM.fromOptional(Optional.of(Vector.of(10))));
		assertThat(streamT.map(num->"hello world"+num)
						.unwrap().<Optional<Vector<String>>>unwrap()
						.get(),  equalTo(Vector.of("hello world10")));
	}


}
