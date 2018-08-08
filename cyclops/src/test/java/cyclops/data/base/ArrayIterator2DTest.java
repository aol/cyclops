package cyclops.data.base;

import cyclops.data.base.BAMT.ArrayIterator2D;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ArrayIterator2DTest {

    ArrayIterator2D<Integer> hasEmpty;
    ArrayIterator2D<Integer> endsEmpty;
    ArrayIterator2D<Integer> simpleNulls;
    @Before
    public void setup(){
        hasEmpty = new ArrayIterator2D<Integer>(new Object[][]{new Object[]{1,2,3},new Object[]{},new Object[]{4,5,6},new Object[]{1}});
        endsEmpty = new ArrayIterator2D<Integer>(new Object[][]{new Object[]{1,2,3},new Object[]{},new Object[]{4,5,6},new Object[]{},new Object[]{},new Object[]{}});
        simpleNulls = new ArrayIterator2D<Integer>(simpleNulls());
    }
    @Test
    public void hasEmpty(){
        assertThat(ReactiveSeq.fromIterable(hasEmpty).toList(),equalTo(Arrays.asList(1,2,3,4,5,6,1)));
    }
    @Test
    public void endsEmpty(){
        assertThat(ReactiveSeq.fromIterable(endsEmpty).toList(),equalTo(Arrays.asList(1,2,3,4,5,6)));

    }
    @Test
    public void testSimpleNulls(){
        System.out.println(Arrays.deepToString(simpleNulls()));
        System.out.println(ReactiveSeq.fromIterable(simpleNulls).toList());
        assertThat(ReactiveSeq.fromIterable(simpleNulls).toList(),equalTo(Arrays.asList(1,2,3,null)));
    }
    public Object[][] simpleNulls(){
        Object[][] array = new Object[3][4];
        for(int i=0;i<3;i++) {
            if (i % 2 == 0) {
                array[i] = new Object[]{};
            } else {
                for (int j = 0; j < 3; j++) {


                    array[i][j] = i + j;


                }
            }

        }
        return array;
    }
    @Test
    public void dynamicTests(){
        for(int a=0;a<30;a++){
            for(int b=0;b<30;b++){
                for(int c=0;c<30;c++){
                    for(int mod=1;mod<10;mod++){
                        Tuple2<List<Integer>, Object[][]> t = complexArrayDynamic(a, b, mod);
                        System.out.println(Arrays.deepToString(t._2()));
                        ArrayIterator2D<Integer> dyn = new ArrayIterator2D<Integer>(t._2());
                        assertThat(ReactiveSeq.fromIterable(dyn).toList(),equalTo(t._1()));
                    }
                }
            }
        }

    }
    public Tuple2<List<Integer>,Object[][]> complexArrayDynamic(int a, int b, int mod){
        List<Integer> list = new ArrayList<>();
        Object[][] array = new Object[a][b];
        for(int i=0;i<a;i++){
            if(i%mod==0){
                array[i]=new Object[]{};
            }else {
                for (int j = 0; j < b; j++) {


                    array[i][j] = i + j;
                    list.add(i + j);


                }
            }
        }
        return Tuple.tuple(list,array);
    }
}
