package cyclops.companion;


import cyclops.control.Option;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class StreamsTest {

    @Test
    public void testSplitAt() {
        assertEquals(Arrays.asList(), Streams.splitAt(Stream.of(),0)._1().collect(Collectors.toList()));
        assertEquals(asList(),  Streams.splitAt(Stream.of(),0)._2().collect(Collectors.toList()));
        assertEquals(Arrays.asList(1), Streams.splitAt(Stream.of(1,2),1)._1().collect(Collectors.toList()));
    }

    @Test
    public void whenSplitAtHeadThenOrderingIsCorrect(){
        assertEquals(asList(), Streams.splitAt(Stream.of(1),1)._2().collect(Collectors.toList()));
        assertEquals(Arrays.asList(1),  Streams.splitAt(Stream.of(1),1)._1().collect(Collectors.toList()));
    }

    @Test
    public void splitAt3(){
        assertThat(ReactiveSeq.of(1,2,3,4,5,6).splitAt(3)._1().toList(),
            equalTo(Streams.splitAt(Stream.of(1, 2, 3, 4, 5, 6),3)._1().collect(Collectors.toList())));
    }
    @Test
    public void duplicate(){
        assertThat(Streams.duplicate(Stream.of(1,2,3))._1().collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
        assertThat(Streams.duplicate(Stream.of())._1().collect(Collectors.toList()),equalTo(Arrays.asList()));
    }
}
