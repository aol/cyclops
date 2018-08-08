package cyclops.data.base;

import cyclops.data.base.BAMT.ArrayIterator2D;
import cyclops.data.base.BAMT.ArrayIterator3D;
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

public class ArrayIterator3DTest {

    ArrayIterator3D<Integer> hasEmpty;
    ArrayIterator3D<Integer> simple;
    ArrayIterator3D<Integer> simpleNulls;
    private Tuple2<List<Integer>, Object[][][]> t2;

    @Before
    public void setup(){

        //System.out.println(Arrays.deepToString(simpleNulls()));
        t2 = complexArray();
        hasEmpty = new ArrayIterator3D<Integer>(t2._2());
        simple = new ArrayIterator3D<>(simple());
        simpleNulls = new ArrayIterator3D<>(simpleNulls());
    //    endsEmpty = new ArrayIterator2D<Integer>(new Object[][]{new Object[]{1,2,3},new Object[]{},new Object[]{4,5,6},new Object[]{},new Object[]{},new Object[]{}});
    }
    @Test
    public void complex(){
        assertThat(ReactiveSeq.fromIterable(hasEmpty).toList(),equalTo(t2._1()));
    }

    @Test
    public void dynamicTests(){
        for(int a=0;a<30;a++){
            for(int b=0;b<30;b++){
                for(int c=0;c<30;c++){
                    for(int mod=1;mod<10;mod++){
                        Tuple2<List<Integer>, Object[][][]> t = complexArrayDynamic(a, b, c, mod);
                        System.out.println(Arrays.deepToString(t._2()));
                        ArrayIterator3D<Integer> dyn = new ArrayIterator3D<Integer>(t._2());
                        assertThat(ReactiveSeq.fromIterable(dyn).toList(),equalTo(t._1()));
                    }
                }
            }
        }

    }
    @Test
    public void testSimple(){
        assertThat(ReactiveSeq.fromIterable(simple).toList(),equalTo(Arrays.asList(1,2,3,2,3,4,3,4,5)));
    }
    @Test
    public void testSimpleNulls(){
        assertThat(ReactiveSeq.fromIterable(simpleNulls).toList(),equalTo(Arrays.asList(1,2,3,null,null,null,null,null,2,3,4,null,null,null,null,null,3,4,5,null,null,null,null,null)));
    }
    public Object[][][] simple(){
        Object[][][] array = new Object[3][3][3];
        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                if(j%2==0){
                    array[i][j]=new Object[]{};
                }else {
                    for (int k = 0; k < 3; k++) {

                        array[i][j][k] = i + j + k;

                    }
                }
            }
        }
        return array;
    }
    public Object[][][] simpleNulls(){
        Object[][][] array = new Object[3][4][4];
        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                if(j%2==0){
                    array[i][j]=new Object[]{};
                }else {
                    for (int k = 0; k < 3; k++) {

                        array[i][j][k] = i + j + k;

                    }
                }
            }
        }
        return array;
    }

    public Tuple2<List<Integer>,Object[][][]> complexArray(){
        List<Integer> list = new ArrayList<>();
        Object[][][] array = new Object[10][10][10];
        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++){
                if(j%2==0){
                    array[i][j]=new Object[]{};
                }else {
                    for (int k = 0; k < 10; k++) {

                        array[i][j][k] = i + j + k;
                        list.add(i+j+k);

                    }
                }
            }
        }
        return Tuple.tuple(list,array);
    }
    public Tuple2<List<Integer>,Object[][][]> complexArrayDynamic(int a, int b, int c, int mod){
        List<Integer> list = new ArrayList<>();
        Object[][][] array = new Object[a][b][c];
        for(int i=0;i<a;i++){
            for(int j=0;j<b;j++){
                if(j%mod==0){
                    array[i][j]=new Object[]{};
                }else {
                    for (int k = 0; k < c; k++) {

                        array[i][j][k] = i + j + k;
                        list.add(i+j+k);

                    }
                }
            }
        }
        return Tuple.tuple(list,array);
    }

}
