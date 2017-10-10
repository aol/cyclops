package cyclops.data;

import cyclops.control.Option;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class VectorTest {

    @Test
    public void testVector(){
        Vector<Integer> ints = Vector.<Integer>empty().plus(1);
        assertThat(ints.get(0),equalTo(Option.some(1)));
    }
    @Test
    public void testVector100(){
        Vector<Integer> ints = Vector.<Integer>empty();
        for(int i=0;i<1025;i++){
            ints = ints.plus(i);
        }

        assertThat(ints.get(0),equalTo(Option.some(0)));
        assertThat(ints.get(900),equalTo(Option.some(900)));
    }

    @Test
    public void last(){
        Object[] array = {"hello","world"};
        assertThat(BAMT.ArrayUtils.last(array),equalTo("world"));
    }
    @Test
    public void test3Pow(){
        Vector<Integer> ints = Vector.<Integer>empty();

        int p  = Double.valueOf(Math.pow(2,15)).intValue();
        for(int i=0;i<p;i++){
            System.out.println(i);
            ints = ints.plus(i);
        }
        for(int i=0;i<p;i++){
            assertThat(ints.get(i),equalTo(Option.some(i)));
        }


        final Vector<Integer> finalRef = ints;
        ints.stream().forEach(next-> {
                    assertThat(finalRef.get(next), equalTo(Option.some(next)));
                }
        );
    }

    @Test
    public void test3PowSet(){
        Vector<Integer> ints = Vector.<Integer>empty();

        int p  = Double.valueOf(Math.pow(2,15)).intValue();
        for(int i=0;i<p;i++){

            ints = ints.plus(i);
        }
        for(int i=0;i<p;i++){
            System.out.println(i);
            ints = ints.set(i,i*2);
        }
        for(int i=0;i<p;i++){
            assertThat(ints.get(i),equalTo(Option.some(i*2)));
        }

    }
    @Test
    public void test4Pow(){
        Vector<Integer> ints = Vector.<Integer>empty();

        int p  = Double.valueOf(Math.pow(2,20)).intValue();
        for(int i=0;i<p;i++){
            ints = ints.plus(i);
        }
        for(int i=0;i<p;i++){
            assertThat(ints.get(i),equalTo(Option.some(i)));
        }

        final Vector<Integer> finalRef = ints;
        ints.stream().forEach(next-> {
                    assertThat(finalRef.get(next), equalTo(Option.some(next)));
                }
        );
    }

    @Test
    public void test4PowSet(){
        Vector<Integer> ints = Vector.<Integer>empty();

        int p  = Double.valueOf(Math.pow(2,20)).intValue();
        for(int i=0;i<p;i++){


            ints = ints.plus(i);
        }
        for(int i=0;i<p;i++){
            ints = ints.set(i,i*2);
        }
        for(int i=0;i<p;i++){
            assertThat(ints.get(i),equalTo(Option.some(i*2)));
        }

    }
    @Test
    public void test5Pow(){
        Vector<Integer> ints = Vector.<Integer>empty();

        int p  = Double.valueOf(Math.pow(2,25)).intValue();
        for(int i=0;i<p;i++){
            ints = ints.plus(i);
        }
        for(int i=0;i<p;i++){
            assertThat(ints.get(i),equalTo(Option.some(i)));
        }

        final Vector<Integer> finalRef = ints;
        ints.stream().forEach(next-> {
                    assertThat(finalRef.get(next), equalTo(Option.some(next)));
                }
        );
    }

    @Test
    public void test5PowSet(){
        Vector<Integer> ints = Vector.<Integer>empty();

        int p  = Double.valueOf(Math.pow(2,25)).intValue();
        for(int i=0;i<p;i++){


            ints = ints.plus(i);
        }
        for(int i=0;i<p;i++){
            ints = ints.set(i,i*2);
        }
        for(int i=0;i<p;i++){
            assertThat(ints.get(i),equalTo(Option.some(i*2)));
        }

    }
    @Test @Ignore
    public void test6Pow(){
        Vector<Integer> ints = Vector.<Integer>empty();

        int p  = Double.valueOf(Math.pow(2,30)).intValue();
        for(int i=0;i<p;i++){
            ints = ints.plus(i);
        }
        for(int i=0;i<p;i++){
            assertThat(ints.get(i),equalTo(Option.some(i)));
        }

        final Vector<Integer> finalRef = ints;
        ints.stream().forEach(next-> {
                    assertThat(finalRef.get(next), equalTo(Option.some(next)));
                }
        );
    }

    @Test @Ignore
    public void test6PowSet(){
        Vector<Integer> ints = Vector.<Integer>empty();

        int p  = Double.valueOf(Math.pow(2,30)).intValue()/2;
        for(int i=0;i<p;i++){


            ints = ints.plus(i);
        }
        for(int i=0;i<p;i++){
            ints = ints.set(i,i*2);
        }
        for(int i=0;i<p;i++){
            assertThat(ints.get(i),equalTo(Option.some(i*2)));
        }

    }


}