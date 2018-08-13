package cyclops.data.base;

import cyclops.data.base.BAMT.Six;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SixTest {

    @Test
    public void map(){
        Six<Integer> two = create(2,2,2, 2,2,2);
        Six<Integer> mapped = two.map(i->i*100);
        System.out.println(Arrays.deepToString(two.array));
        System.out.println(Arrays.deepToString(mapped.array));
        assertThat(ReactiveSeq.fromIterator(mapped.iterator()).seq(),equalTo(ReactiveSeq.range(0,64).map(i->i*100).seq()));

    }
    @Test
    public void dynamic(){
        for(int i=0;i<15;i++) {
            Six<Integer> two = create(i, i,i,i,i,i);
            Six<Integer> mapped = two.map(n -> n * 100);
          //  System.out.println(Arrays.deepToString(two.array));
            System.out.println(Arrays.deepToString(mapped.array));
            assertThat(mapped.stream().toList(),
                equalTo(ReactiveSeq.iterate(0,n->n+100).take(i*i*i*i*i*i).toList()));
        }

    }

    public Six<Integer> create(int x, int y, int a, int b, int c,int d){
        Object[][][][][][] array = new Object[x][y][a][b][c][d];
        int count =0;
        for(int i=0;i<x;i++){
            for(int j=0;j<y;j++){
                for(int k=0;k<a;k++) {
                    for(int l=0;l<b;l++) {
                        for(int m=0;m<c;m++) {
                            for(int n=0;n<d;n++) {
                                array[i][j][k][l][m][n] = count++;
                            }
                        }
                    }
                }
            }
        }
        return new Six<>(array);
    }
}
