package cyclops.data;

import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.stream.Stream;

import static cyclops.data.Chain.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ChainTest {
     Chain<Integer> fromStream(Stream<Integer> s) {
        Chain<Integer> res = Chain.empty();
        for(Integer next : ReactiveSeq.fromStream(s)){
            res = res.append(next);
        }
        return res;
    }
    @Test
    public void large(){
            fromStream(ReactiveSeq.range(0,100_000)).forEach(System.out::println);

    }
    @Test
    public void single(){
         Chain<Integer> ten = singleton(10);
         assertThat(ten,equalTo(Seq.of(10)));
        assertThat(ten.size(),equalTo(1));
        assertThat(ten.isEmpty(),equalTo(false));
        assertThat(ten.toString(),equalTo("[10]"));
    }
    @Test
    public void append(){
        Chain<Integer> two = Chain.append(singleton(10),singleton(20));
        two.forEach(System.out::println);
        assertThat(two,equalTo(Seq.of(10,20)));
        assertThat(two.size(),equalTo(2));
        assertThat(two.isEmpty(),equalTo(false));
        assertThat(two.toString(),equalTo("[10, 20]"));
    }
    @Test
    public void wrap(){
        Chain<Integer> three = Chain.wrap(LazySeq.of(10,20,30));
        assertThat(three,equalTo(Seq.of(10,20,30)));
        assertThat(three.size(),equalTo(3));
        assertThat(three.isEmpty(),equalTo(false));
        assertThat(three.toString(),equalTo("[10, 20, 30]"));
    }


    @Test
    public void empty(){
        Chain<Integer> empty = Chain.<Integer>empty();
        assertThat(empty,equalTo(Seq.empty()));
        assertThat(empty.size(),equalTo(0));
        assertThat(empty.isEmpty(),equalTo(true));
        assertThat(empty.toString(),equalTo("[]"));
    }
}
