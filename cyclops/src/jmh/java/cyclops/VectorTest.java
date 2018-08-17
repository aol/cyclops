package cyclops;

import cyclops.reactive.collections.immutable.VectorX;
import cyclops.reactive.collections.mutable.ListX;
import io.vavr.collection.Vector;
import org.junit.Test;
import org.openjdk.jmh.annotations.TearDown;

import java.util.Arrays;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class VectorTest {
    @Test
    public void vectorplay(){
        Vector.of(1,2,3)
            .map(i->{
                System.out.println("a "+ i);
                return i;
            })
            .map(i->i+2)

            .map(i->{
                System.out.println("b " + i);
                return i;
            })
            .filter(i->i<4)
            .map(i->{
                System.out.println("c "+ i);
                return i;
            });

    }

    @Test
    public void multipaths() {

        Vector<Integer> list = Vector.of(1, 2, 3);
        Vector<Integer> by10 = list.map(i -> i * 10);
        Vector<Integer> plus2 = list.map(i -> i + 2);
        Vector<Integer> by10Plus2 = by10.map(i -> i + 2);
        assertThat(by10.toJavaList(), equalTo(Arrays.asList(10, 20, 30)));
        assertThat(plus2.toJavaList(), equalTo(Arrays.asList(3, 4, 5)));
        assertThat(by10Plus2.toJavaList(), equalTo(Arrays.asList(12, 22, 32)));
    }
}
