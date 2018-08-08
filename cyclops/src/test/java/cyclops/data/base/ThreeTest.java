package cyclops.data.base;

import cyclops.data.base.BAMT.Three;
import cyclops.data.base.BAMT.Two;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ThreeTest {

    @Test
    public void map(){
        Three<Integer> two = create(2,2,2);
        Three<Integer> mapped = two.map(i->i*100);
        System.out.println(Arrays.deepToString(two.array));
        System.out.println(Arrays.deepToString(mapped.array));
        assertThat(ReactiveSeq.fromIterator(mapped.iterator()).toList(),equalTo(Arrays.asList(0,100,200,300,400,500,600,700)));

    }
    @Test
    public void dynamic(){
        for(int i=0;i<20;i++) {
            Three<Integer> two = create(i, i,i);
            Three<Integer> mapped = two.map(n -> n * 100);
            System.out.println(Arrays.deepToString(two.array));
            System.out.println(Arrays.deepToString(mapped.array));
            assertThat(ReactiveSeq.fromIterator(mapped.iterator()).toList(),
                equalTo(ReactiveSeq.iterate(0,n->n+100).take(i*i*i).toList()));
        }

    }

    public Three<Integer> create(int x, int y, int a){
        Object[][][] array = new Object[x][y][a];
        int count =0;
        for(int i=0;i<x;i++){
            for(int j=0;j<y;j++){
                for(int k=0;k<a;k++) {
                    array[i][j][k] = count++;
                }
            }
        }
        return new Three<>(array);
    }
}
