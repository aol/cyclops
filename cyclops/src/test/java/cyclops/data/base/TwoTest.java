package cyclops.data.base;

import cyclops.data.base.BAMT.Two;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TwoTest {

    @Test
    public void map(){
        Two<Integer> two = create(2,2);
        Two<Integer> mapped = two.map(i->i*100);
        System.out.println(Arrays.deepToString(two.array));
        System.out.println(Arrays.deepToString(mapped.array));
        assertThat(ReactiveSeq.fromIterator(mapped.iterator()).toList(),equalTo(Arrays.asList(0,100,200,300)));

    }
    @Test
    public void dynamic(){
        for(int i=0;i<100;i++) {
            Two<Integer> two = create(i, i);
            Two<Integer> mapped = two.map(n -> n * 100);
            System.out.println(Arrays.deepToString(two.array));
            System.out.println(Arrays.deepToString(mapped.array));
            assertThat(ReactiveSeq.fromIterator(mapped.iterator()).toList(),
                equalTo(ReactiveSeq.iterate(0,n->n+100).take(i*i).toList()));
        }

    }

    public Two<Integer> create(int x, int y){
        Object[][] array = new Object[x][y];
        int count =0;
        for(int i=0;i<x;i++){
            for(int j=0;j<y;j++){
                array[i][j]=count++;
            }
        }
        return new Two<>(array);
    }
}
