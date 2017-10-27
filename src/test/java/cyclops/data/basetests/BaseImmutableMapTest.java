package cyclops.data.basetests;

import cyclops.collections.immutable.*;
import cyclops.collections.mutable.ListX;
import cyclops.data.tuple.Tuple;
import cyclops.companion.MapXs;
import cyclops.companion.PersistentMapXs;
import cyclops.control.Option;
import cyclops.data.ImmutableMap;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract  class BaseImmutableMapTest {

    protected abstract <K,V> ImmutableMap<K,V> empty();
    protected abstract  <K,V> ImmutableMap<K,V> of(K k1,V v1);
    protected abstract <K,V> ImmutableMap<K,V> of(K k1,V v1,K k2, V v2);
    protected abstract ImmutableMap<String,Integer> fromMap(Map<String, Integer> hello);

    @Test
    public void toStringTest(){
        assertThat(of(1,3).toString(),equalTo("[{1=3}]"));
    }
    @Test
    public void toPStackX(){
        ImmutableMap<String,Integer> maps = of("a",1,"b",2);
        LinkedListX<String> strs = maps.toLinkedListX(t->""+t._1()+t._2());
        assertThat(strs,equalTo(ListX.of("a1","b2")));
    }
    @Test
    public void toPSetX(){
        ImmutableMap<String,Integer> maps = of("a",1,"b",2);
        PersistentSetX<String> strs = maps.toPersistentSetX(t->""+t._1()+t._2());
        assertThat(strs,equalTo(PersistentSetX.of("a1","b2")));
    }
    @Test
    public void toPOrderedSetX(){
        ImmutableMap<String,Integer> maps = of("a",1,"b",2);
        OrderedSetX<String> strs = maps.toOrderedSetX(t->""+t._1()+t._2());
        assertThat(strs,equalTo(OrderedSetX.of("a1","b2")));
    }
    @Test
    public void toPBagX(){
        ImmutableMap<String,Integer> maps = of("a",1,"b",2);
        BagX<String> strs = maps.toBagX(t->""+t._1()+t._2());
        assertThat(strs,equalTo(BagX.of("a1","b2")));
    }
    @Test
    public void toPQueueX(){
        ImmutableMap<String,Integer> maps = of("a",1,"b",2);
        PersistentQueueX<String> strs = maps.toPersistentQueueX(t->""+t._1()+t._2());
        assertThat(strs.toList(),equalTo(PersistentQueueX.of("a1","b2").toList()));
    }
    @Test
    public void toPVectorX(){
        ImmutableMap<String,Integer> maps = of("a",1,"b",2);
        VectorX<String> strs = maps.toVectorX(t->""+t._1()+t._2());
        assertThat(strs,equalTo(VectorX.of("a1","b2")));
    }

    @Test
    public void onEmpty(){
        assertThat(empty().onEmpty(Tuple.tuple("hello",10)).get("hello"),equalTo(Option.some(10)));
    }
    @Test
    public void onEmptyGet(){
        assertThat(empty().onEmptyGet(()->Tuple.tuple("hello",10)).get("hello"),equalTo(Option.some(10)));
    }
    @Test
    public void onEmptyThrow(){

        assertTrue(empty().onEmptyTry(()->new RuntimeException("hello")).isFailure());
        assertFalse(of(1,2).onEmptyTry(()->new RuntimeException("hello")).isFailure());
    }
    @Test
    public void onEmptySwitch(){

        assertThat(this.<String,Integer>empty().onEmptySwitch(()-> fromMap(MapXs.of("hello",10))).get("hello"),equalTo(Option.some(10)));
    }

    

    @Test
    public void testOf() {
        assertThat(this.empty().javaMap(),equalTo(new java.util.HashMap()));
    }

    @Test
    public void testOfKV() {
        Map<String,Integer> map = new java.util.HashMap<>();
        map.put("key",1);
        assertThat(of("key",1).javaMap(),equalTo(map));
    }

    @Test
    public void testOfKVKV() {
        Map<String,Integer> map = new java.util.HashMap<>();
        map.put("1",1);
        map.put("2",2);
        assertThat(of("1",1,"2",2).javaMap(),equalTo(map));
    }

   

    @Test
    public void testMapKV() {
        Map<String,Integer> map = new java.util.HashMap<>();
        map.put("1",1);
        map.put("2",2);
        assertThat(PersistentMapXs.map("1",1).put("2", 2).build(),equalTo(map));
    }
    @Test
    public void testMapKVPutAll() {
        Map<String,Integer> map = new java.util.HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        map.put("5",5);
        map.put("6",6);
        Map<String,Integer> map2 = new java.util.HashMap<>();
        map2.put("1",1);
        map2.put("2",2);
        map2.put("3",3);
        map2.put("4",4);
        map2.put("5",5);
        map2.put("6",6);
        assertThat(PersistentMapXs.map("1",1).putAll(map2).build(),equalTo(map));
    }

    @Test
    public void testEmpty(){
        assertThat(empty().size(),equalTo(0));
    }
    @Test
    public void test(){
        ImmutableMap<Integer,Integer> map = empty();


        assertThat(map.put(10,10).size(),equalTo(1));

    }

    public <K,V> void putAndCompare(ImmutableMap<K,V> map){
        HashMap<K,V> jmap = new HashMap<>();

        jmap.putAll(map.javaMap());
        assertThat(map.javaMap(),equalTo(jmap));
    }


    @Test
    public void add3Entries(){
        ImmutableMap<Integer,Integer> map = empty();

        for(int i=0;i<3;i++){
            map = map.put(i,i*2);

        }
        assertThat(map.size(),equalTo(3));
        putAndCompare(map);
    }
    @Test
    public void add5Entries(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<5;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(5));
        putAndCompare(map);
    }
    @Test
    public void add10Entries(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<10;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(10));
        putAndCompare(map);
    }
    @Test
    public void add34Entries(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<34;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(34));
        putAndCompare(map);
    }
    @Test
    public void add80Entries(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<80;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(80));
        putAndCompare(map);
    }
    @Test
    public void streamTest(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<500;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.stream().toList().size(),equalTo(500));
    }
    @Test
    public void add500Entries(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<500;i++){
            map = map.put(i,i*2);
        }

        assertThat(map.size(),equalTo(500));
        putAndCompare(map);

    }
    @Test
    public void add50000Entries(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<50000;i++){
            map = map.put(i,i*2);
        }
        assertThat(map.size(),equalTo(50000));
        putAndCompare(map);
    }

    @Test
    public void map(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<80;i++){
            map = map.put(i,i*2);
        }
        ImmutableMap<Integer,Integer> map2 =  map.bimap(k->k*2,v->v*10);
        assertThat(map2.stream().map(t->t._1()).sumInt(i->i),equalTo(map.stream().map(t->t._1()).sumInt(i->i)*2));
        assertThat(map2.stream().map(t->t._2()).sumInt(i->i),equalTo(map.stream().map(t->t._2()).sumInt(i->i)*10));

    }
    @Test
    public void flatMap(){
        ImmutableMap<Integer,Integer> map = empty();
        for(int i=0;i<80;i++){
            map = map.put(i,i*2);
        }
        ImmutableMap<Integer,Integer> map2 =  map.flatMap((k,v)-> of(k*2,v*10));
        assertThat(map2.stream().map(t->t._1()).sumInt(i->i),equalTo(map.stream().map(t->t._1()).sumInt(i->i)*2));
        assertThat(map2.stream().map(t->t._2()).sumInt(i->i),equalTo(map.stream().map(t->t._2()).sumInt(i->i)*10));

    }
}
