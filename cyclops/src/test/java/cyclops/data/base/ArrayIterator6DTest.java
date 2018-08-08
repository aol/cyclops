package cyclops.data.base;

import cyclops.data.base.BAMT.ArrayIterator6D;
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
import static org.hamcrest.Matchers.greaterThan;

public class ArrayIterator6DTest {

    ArrayIterator6D<Integer> hasEmpty;
    ArrayIterator6D<Integer> simple;
    ArrayIterator6D<Integer> simpleNulls;
    private Tuple2<List<Integer>, Object[][][][][][]> complexT2;
    private Tuple2<List<Integer>, Object[][][][][][]> simpleT2;
    private Tuple2<List<Integer>, Object[][][][][][]> simpleNullsT2;

    @Before
    public void setup(){

        //System.out.println(Arrays.deepToString(simpleNulls()));
        complexT2 = complexArray();
        simpleT2= simple();
        simpleNullsT2 = simpleNulls();
        hasEmpty = new ArrayIterator6D<Integer>(complexT2._2());
        simple = new ArrayIterator6D<>(simpleT2._2());
        simpleNulls = new ArrayIterator6D<>(simpleNullsT2._2());
    }
    @Test
    public void complex(){
       assertThat(ReactiveSeq.fromIterable(hasEmpty).toList(),equalTo(complexT2._1()));
    }

    @Test
    public void dynamicTests(){
        for(int a=0;a<5;a++){
            for(int b=0;b<5;b++){
                for(int c=0;c<5;c++){
                    for(int d=0;d<5;d++) {
                        for(int e=0;e<5;e++) {
                            for(int f=0;f<5;f++) {
                                for (int mod = 1; mod < 5; mod++) {
                                    Tuple2<List<Integer>, Object[][][][][][]> t = complexArrayDynamic(a, b, c, d, e,f, mod);
                                    System.out.println(Arrays.deepToString(t._2()));
                                    ArrayIterator6D<Integer> dyn = new ArrayIterator6D<Integer>(t._2());
                                    assertThat(ReactiveSeq.fromIterable(dyn).toList(), equalTo(t._1()));
                                }
                            }
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
        assertThat(simpleNullsT2._1().size(),greaterThan(0));
        assertThat(ReactiveSeq.fromIterable(simpleNulls).toList(),equalTo(simpleNullsT2._1()));
    }
    public Tuple2<List<Integer>,Object[][][][][][]> simple(){
        List<Integer> list = new ArrayList<>();
        Object[][][][][][] array = new Object[3][3][3][3][3][3];
        for(int b=0;b<3;b++) {
            for (int a = 0; a < 3; a++) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        for (int k = 0; k < 3; k++) {
                            if (k % 2 == 0) {
                                array[b][a][i][j][k] = new Object[]{};
                            } else {
                                for (int l = 0; l < 3; l++) {

                                    array[b][a][i][j][k][l] = i + j + k + l;
                                    list.add(i + j + k + l);
                                }
                            }
                        }
                    }
                }
            }
        }
        return Tuple.tuple(list,array);
    }
    public Tuple2<List<Integer>,Object[][][][][][]> simpleNulls(){
        Object[][][][][][] array = new Object[3][3][3][3][4][4];
        List<Integer> list = new ArrayList<>();
        for(int c=0;c<3;c++) {
            for (int b = 0; b < 3; b++) {
                for (int a = 0; a < 3; a++) {
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            if (j % 2 == 0) {
                                array[c][b][a][i][j] = new Object[]{};
                            } else {
                                for (int k = 0; k < 3; k++) {

                                    array[c][b][a][i][j][k] = i + j + k;
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
            }
        }
        return Tuple.tuple(list,array);
    }

    public Tuple2<List<Integer>,Object[][][][][][]> complexArray(){
        List<Integer> list = new ArrayList<>();
        Object[][][][][][] array = new Object[10][10][10][10][10][10];
        for(int c=0;c<10;c++) {
            for (int b = 0; b < 10; b++) {
                for (int a = 0; a < 10; a++) {
                    for (int i = 0; i < 10; i++) {
                        for (int j = 0; j < 10; j++) {
                            if (j % 2 == 0) {
                                array[c][b][a][i][j] = new Object[]{};
                            } else {
                                for (int k = 0; k < 10; k++) {

                                    array[c][b][a][i][j][k] = i + j + k;
                                    list.add(i + j + k);

                                }
                            }
                        }
                    }
                }
            }
        }
        return Tuple.tuple(list,array);
    }
    public Tuple2<List<Integer>,Object[][][][][][]> complexArrayDynamic(int a, int b, int c, int d,int e,int f,int mod){
        List<Integer> list = new ArrayList<>();
        Object[][][][][][] array = new Object[f][e][d][a][b][c];
        for(int c1 = 0;c1<f;c1++) {
            for (int b1 = 0; b1 < e; b1++) {
                for (int a1 = 0; a1 < d; a1++) {
                    for (int i = 0; i < a; i++) {
                        for (int j = 0; j < b; j++) {
                            if (j % mod == 0) {
                                array[c1][b1][a1][i][j] = new Object[]{};
                            } else {
                                for (int k = 0; k < c; k++) {

                                    array[c1][b1][a1][i][j][k] = i + j + k;
                                    list.add(i + j + k);

                                }
                            }
                        }
                    }
                }
            }
        }
        return Tuple.tuple(list,array);
    }

}
