package cyclops.data;

import com.oath.cyclops.hkt.Higher;


import com.oath.cyclops.hkt.DataWitness.seq;
import com.oath.cyclops.hkt.DataWitness.reactiveSeq;
import com.oath.cyclops.hkt.DataWitness.vector;
import cyclops.function.NaturalTransformation;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;


public class NaturalTransformationTest {

    NaturalTransformation<reactiveSeq,seq> streamToList = new NaturalTransformation<reactiveSeq, seq>() {
        @Override
        public <T> Higher<seq, T> apply(Higher<reactiveSeq, T> a) {
            return a.convert(ReactiveSeq::narrowK).toSeq();
        }
    };
    NaturalTransformation<seq,vector> listToVector= new NaturalTransformation<seq, vector>() {
        @Override
        public <T> Higher<vector, T> apply(Higher<seq, T> a) {
            return a.convert(Seq::narrowK).to().vector();
        }
    };

    @Test
    public void streamToList(){
        assertThat(streamToList.apply(ReactiveSeq.of(1,2,3)),equalTo(Vector.of(1,2,3)));
    }
    @Test
    public void streamToListAndThenToVectorX(){
        assertThat(streamToList.andThen(listToVector).apply(ReactiveSeq.of(1,2,3)),equalTo(Vector.of(1,2,3)));
    }
    @Test
    public void compose(){
        assertThat(listToVector.compose(streamToList).apply(ReactiveSeq.of(1,2,3)),equalTo(Vector.of(1,2,3)));
    }
}
