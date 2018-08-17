package cyclops.data.base;

import cyclops.data.base.BAMT.Four;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class FourTest {

    @Test
    public void map(){
        Four<Integer> two = create(2,2,2, 2);
        Four<Integer> mapped = two.map(i->i*100);
        System.out.println(Arrays.deepToString(two.array));
        System.out.println(Arrays.deepToString(mapped.array));
        assertThat(ReactiveSeq.fromIterator(mapped.iterator()).toList(),equalTo(Arrays.asList(0,100,200,300,400,500,600,700,800,900,1000,1100,1200,1300,1400,1500)));

    }
    @Test
    public void dynamic(){
        for(int i=0;i<20;i++) {
            Four<Integer> two = create(i, i,i,i);
            Four<Integer> mapped = two.map(n -> n * 100);
            System.out.println(Arrays.deepToString(two.array));
            System.out.println(Arrays.deepToString(mapped.array));
            assertThat(ReactiveSeq.fromIterator(mapped.iterator()).toList(),
                equalTo(ReactiveSeq.iterate(0,n->n+100).take(i*i*i*i).toList()));
        }

    }

    public Four<Integer> create(int x, int y, int a, int b){
        Object[][][][] array = new Object[x][y][a][b];
        int count =0;
        for(int i=0;i<x;i++){
            for(int j=0;j<y;j++){
                for(int k=0;k<a;k++) {
                    for(int l=0;l<b;l++) {
                        array[i][j][k][l] = count++;
                    }
                }
            }
        }
        return new Four<>(array);
    }
}
