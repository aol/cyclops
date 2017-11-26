package cyclops.data;

import com.oath.cyclops.types.persistent.PersistentMap;
import org.hamcrest.MatcherAssert;
import org.junit.Test;


import java.util.ArrayList;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class HashMapTest {

    @Test
    public void plusSize(){
        assertThat(TrieMap.empty().put("hello","world").size(),equalTo(1));
    }

    @Test
    public void add10000(){
        //19742
        long start = System.currentTimeMillis();
        HashMap<Integer,Integer> v = HashMap.empty();
        for(int i=0;i<100_000_00;i++){
            v =v.put(i,i);
        }
        System.out.println(System.currentTimeMillis()-start);
        System.out.println(v.size());
    }
    @Test
    public void read100_000_00(){
        //6247
        HashMap<Integer,Integer> v = HashMap.empty();
        for(int i=0;i<100_000_00;i++){
            v =v.put(i,i);
        }
        ArrayList<Integer> al = new ArrayList(v.size());
        long start = System.currentTimeMillis();
        for(int i=0;i<100_000_00;i++){
            al.add(v.getOrElse(i,null));
        }

        System.out.println(System.currentTimeMillis()-start);
        System.out.println(v.size());
    }
    @Test
    public void read100_000_00PC(){
        //2181

        PersistentMap<Integer,Integer> v = HashMap.empty();
        for(int i=0;i<100_000_00;i++){
            v =v.put(i,i);
        }
        ArrayList<Integer> al = new ArrayList(v.size());
        long start = System.currentTimeMillis();
        for(int i=0;i<100_000_00;i++){
            al.add(v.getOrElse(i,null));
        }

        System.out.println(System.currentTimeMillis()-start);
        System.out.println(v.size());
    }

    @Test
    public void add10000PCol(){
        //26792
        long start = System.currentTimeMillis();
        PersistentMap<Integer,Integer> v = HashMap.empty();
        for(int i=0;i<100_000_00;i++){
            v =v.put(i,i);
        }
        System.out.println(System.currentTimeMillis()-start);
        System.out.println(v.size());
    }

  @Test
  public void removeMissingKey(){
    MatcherAssert.assertThat(HashMap.of(1,"a",2,"b").removeAll(0),equalTo(HashMap.of(1,"a",2,"b")));
  }
}
