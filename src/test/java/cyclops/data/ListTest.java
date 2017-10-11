package cyclops.data;

import cyclops.companion.Monoids;
import cyclops.data.basetests.BaseImmutableListTest;
import cyclops.stream.ReactiveSeq;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class ListTest{

    @Test
    public void testMapA(){
      assertThat(Seq.of(1,2,3).map(i->i*2),equalTo(Seq.of(2,4,6)));
      assertThat(Seq.<Integer>empty().map(i->i*2),equalTo(Seq.empty()));
    }
    @Test
    public void testFlatMapA(){
        assertThat(Seq.of(1,2,3).flatMap(i-> Seq.of(i*2)),equalTo(Seq.of(2,4,6)));
        assertThat(Seq.<Integer>empty().flatMap(i-> Seq.of(i*2)),equalTo(Seq.empty()));
    }

    @Test
    public void testFoldRightA(){
        assertThat(Seq.fromStream(ReactiveSeq.range(0,100_000)).foldRight(Monoids.intSum),equalTo(704982704));
    }
}
