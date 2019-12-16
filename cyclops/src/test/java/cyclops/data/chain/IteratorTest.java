package cyclops.data.chain;

import cyclops.data.Chain;
import cyclops.data.ImmutableList;
import cyclops.data.Seq;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class IteratorTest {

    @Test
    public void it(){
        System.out.println(ReactiveSeq.fromStream(Stream.of(1,2,3)).iterator().hasNext());
    }
    @Test
    public void removeValue2(){
        Chain<Integer> vec =Chain.wrap(Seq.of(5,2,1));
        ReactiveSeq<Integer> stream = vec.stream().removeFirst(e -> Objects.equals(e, 2));

        System.out.println(stream.toList());
        System.out.println(stream.toList());
        //   System.out.println(vec);
        //   System.out.println("SeqToStre"+vec.stream().seq());
        //  System.out.println("Seq"+vec.stream(). removeFirst(e-> Objects.equals(e,2)).seq());
      //  System.out.println("FS"+Chain.wrap(vec.stream(). removeFirst(e-> Objects.equals(e,2))));
        // assertThat(vec.removeValue(2), CoreMatchers.equalTo(of(5,1)));
    }

    int count =0;
    @Test
    public void testCycleWhileNoOrd() {
        count =0;

        ImmutableList<Integer> ch =  Chain.wrap(asList(1, 2, 3)).cycleWhile(next->count++<6);

        assertThat(ch.toList(),equalTo(asList(1,2,3,1,2,3)));
        count = 0;
        assertThat(ch.toList(),equalTo(asList(1,2,3,1,2,3)));

    }

}
