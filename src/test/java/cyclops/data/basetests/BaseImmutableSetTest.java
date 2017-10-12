package cyclops.data.basetests;

import cyclops.collectionx.AbstractIterableXTest;
import cyclops.companion.Monoids;
import cyclops.data.HashSet;
import cyclops.data.ImmutableSet;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public abstract class BaseImmutableSetTest extends AbstractIterableXTest {

    protected abstract <T> ImmutableSet<T> fromStream(Stream<T> s);

    @Override
    public abstract <T> ImmutableSet<T> of(T... values);

    @Test
    public void testMapA(){
        assertThat(of(1,2,3).map(i->i*2),equalTo(HashSet.of(2,4,6)));
        assertThat(this.<Integer>empty().map(i->i*2),equalTo(HashSet.empty()));
    }
    @Test
    public void testFlatMapA(){
        assertThat(of(1,2,3).flatMap(i-> of(i*2)),equalTo(HashSet.of(2,4,6)));
        assertThat(this.<Integer>empty().concatMap(i-> of(i*2)),equalTo(HashSet.empty()));
    }

    @Test
    public void testFoldRightA(){
        assertThat(fromStream(ReactiveSeq.range(0,100_000)).foldRight(Monoids.intSum),equalTo(704982704));
    }
    @Test
    public void forEach2() {

        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), (a , b) -> a + b).toList().size(),
                equalTo(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).size()));
    }

    @Test
    public void forEach2Filter() {

        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), (a , b) -> a > 2 && b < 8,
                (a ,b) -> a + b).toList().size(), equalTo(Arrays.asList(3, 4, 5, 6, 7, 8, 9, 10).size()));
    }
}
