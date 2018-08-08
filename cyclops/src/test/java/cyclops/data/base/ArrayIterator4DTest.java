package cyclops.data.base;

import cyclops.data.base.BAMT.ArrayIterator3D;
import cyclops.data.base.BAMT.ArrayIterator4D;
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

public class ArrayIterator4DTest {

    ArrayIterator4D<Integer> hasEmpty;
    ArrayIterator4D<Integer> simple;
    ArrayIterator4D<Integer> simpleNulls;
    private Tuple2<List<Integer>, Object[][][][]> complexT2;
    private Tuple2<List<Integer>, Object[][][][]> simpleT2;
    private Tuple2<List<Integer>, Object[][][][]> simpleNullsT2;

    @Before
    public void setup(){

        //System.out.println(Arrays.deepToString(simpleNulls()));
        complexT2 = complexArray();
        simpleT2= simple();
        simpleNullsT2 = simpleNulls();
        hasEmpty = new ArrayIterator4D<Integer>(complexT2._2());
        simple = new ArrayIterator4D<>(simpleT2._2());
        simpleNulls = new ArrayIterator4D<>(simpleNullsT2._2());
    }
    @Test
    public void complex(){
       assertThat(ReactiveSeq.fromIterable(hasEmpty).toList(),equalTo(complexT2._1()));
    }

    @Test
    public void dynamicTests(){
        for(int a=0;a<20;a++){
            for(int b=0;b<20;b++){
                for(int c=0;c<20;c++){
                    for(int d=0;d<20;d++) {
                        for (int mod = 1; mod < 5; mod++) {
                            Tuple2<List<Integer>, Object[][][][]> t = complexArrayDynamic(a, b, c,d, mod);
                            System.out.println(Arrays.deepToString(t._2()));
                            ArrayIterator4D<Integer> dyn = new ArrayIterator4D<Integer>(t._2());
                            assertThat(ReactiveSeq.fromIterable(dyn).toList(), equalTo(t._1()));
                        }
                    }
                }
            }
        }

    }
    @Test
    public void testSimple(){
        assertThat(ReactiveSeq.fromIterable(simple).toList(),equalTo(simpleT2._1()));
    }
    @Test
    public void testSimpleNulls(){
        assertThat(ReactiveSeq.fromIterable(simpleNulls).toList(),equalTo(simpleNullsT2._1()));
    }
    public Tuple2<List<Integer>,Object[][][][]> simple(){
        List<Integer> list = new ArrayList<>();
        Object[][][][] array = new Object[3][3][3][3];
        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                for(int k=0;k<3;k++) {
                    if (k % 2 == 0) {
                        array[i][j][k] = new Object[]{};
                    } else {
                        for (int l = 0; l < 3; l++) {

                            array[i][j][k][l] = i + j + k +l;
                            list.add(i + j + k+l);
                        }
                    }
                }
            }
        }
        return Tuple.tuple(list,array);
    }
    public Tuple2<List<Integer>,Object[][][][]> simpleNulls(){
        Object[][][][] array = new Object[3][3][4][4];
        List<Integer> list = new ArrayList<>();
        for(int a=0;a<3;a++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (j % 2 == 0) {
                        array[a][i][j] = new Object[]{};
                    } else {
                        for (int k = 0; k < 3; k++) {

                            array[a][i][j][k] = i + j + k;
                            list.add(i + j + k);

                        }
                        list.add(null);
                    }


                }
                for (int nl = 0; nl < 4; nl++) {
                    list.add(null);
                }
            }
        }
        return Tuple.tuple(list,array);
    }

    public Tuple2<List<Integer>,Object[][][][]> complexArray(){
        List<Integer> list = new ArrayList<>();
        Object[][][][] array = new Object[10][10][10][10];
        for(int a=0;a<10;a++) {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (j % 2 == 0) {
                        array[a][i][j] = new Object[]{};
                    } else {
                        for (int k = 0; k < 10; k++) {

                            array[a][i][j][k] = i + j + k;
                            list.add(i + j + k);

                        }
                    }
                }
            }
        }
        return Tuple.tuple(list,array);
    }
    public Tuple2<List<Integer>,Object[][][][]> complexArrayDynamic(int a, int b, int c, int d,int mod){
        List<Integer> list = new ArrayList<>();
        Object[][][][] array = new Object[d][a][b][c];
        for(int a1 = 0;a1<d;a1++) {
            for (int i = 0; i < a; i++) {
                for (int j = 0; j < b; j++) {
                    if (j % mod == 0) {
                        array[a1][i][j] = new Object[]{};
                    } else {
                        for (int k = 0; k < c; k++) {

                            array[a1][i][j][k] = i + j + k;
                            list.add(i + j + k);

                        }
                    }
                }
            }
        }
        return Tuple.tuple(list,array);
    }

}
