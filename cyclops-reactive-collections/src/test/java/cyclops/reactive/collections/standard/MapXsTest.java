package cyclops.reactive.collections.standard;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import cyclops.data.tuple.Tuple;
import cyclops.reactive.collections.mutable.*;
import cyclops.reactive.companion.MapXs;
import org.junit.Test;

public class MapXsTest {

    @Test
    public void toStringTest(){
        assertThat(MapXs.of(1,3).toString(),equalTo("MapX[{1=3}]"));
    }
    @Test
    public void toListX(){
        MapX.empty().forEach(1l,System.out::println);
        MapX<String,Integer> maps = MapXs.of("a",1,"b",2);
        ListX<String> strs = maps.toListX(t->""+t._1()+t._2());
        assertThat(strs,equalTo(ListX.of("a1","b2")));
    }
    @Test
    public void toSetX(){
        MapX<String,Integer> maps = MapXs.of("a",1,"b",2);
        SetX<String> strs = maps.toSetX(t->""+t._1()+t._2());
        assertThat(strs,equalTo(SetX.of("a1","b2")));
    }
    @Test
    public void toSortedSetX(){
        MapX<String,Integer> maps = MapXs.of("a",1,"b",2);
        SortedSetX<String> strs = maps.toSortedSetX(t->""+t._1()+t._2());
        assertThat(strs,equalTo(SortedSetX.of("a1","b2")));
    }
    @Test
    public void toQueueX(){
        MapX<String,Integer> maps = MapXs.of("a",1,"b",2);
        QueueX<String> strs = maps.toQueueX(t->""+t._1()+t._2());
        assertThat(strs.toList(),equalTo(QueueX.of("a1","b2").toList()));
    }
    @Test
    public void toDequeX(){
        MapX<String,Integer> maps = MapXs.of("a",1,"b",2);
        DequeX<String> strs = maps.toDequeX(t->""+t._1()+t._2());
        assertThat(strs.toList(),equalTo(DequeX.of("a1","b2").toList()));
    }
    @Test
    public void onEmpty(){
        assertThat(MapX.empty().onEmpty(Tuple.tuple("hello",10)).get("hello"),equalTo(10));
    }
    @Test
    public void onEmptyGet(){
        assertThat(MapX.empty().onEmptyGet(()->Tuple.tuple("hello",10)).get("hello"),equalTo(10));
    }

    @Test
    public void onEmptySwitch(){

        assertThat(MapX.<String,Integer>empty().onEmptySwitch(()->MapX.fromMap(MapXs.of("hello",10))).get("hello"),equalTo(10));
    }
    @Test
    public void testOf() {
        assertThat(MapXs.of(),equalTo(new HashMap()));
    }

    @Test
    public void testOfKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("key",1);
        assertThat(MapXs.of("key",1),equalTo(map));
    }

    @Test
    public void testOfKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        assertThat(MapXs.of("1",1,"2",2),equalTo(map));
    }

    @Test
    public void testOfKVKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        assertThat(MapXs.of("1",1,"2",2,"3",3),equalTo(map));
    }

    @Test
    public void testOfKVKVKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        assertThat(MapXs.of("1",1,"2",2,"3",3,"4",4),equalTo(map));
    }

    @Test
    public void testFrom() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        assertThat(MapXs.from(map).build(),equalTo(map));
    }

    @Test
    public void testMapKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        assertThat(MapXs.map("1",1).put("2", 2).build(),equalTo(map));
    }
    @Test
    public void testMapKVPutAll() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        map.put("5",5);
        map.put("6",6);
        Map<String,Integer> map2 = new HashMap<>();
        map2.put("1",1);
        map2.put("2",2);
        map2.put("3",3);
        map2.put("4",4);
        map2.put("5",5);
        map2.put("6",6);
        assertThat(MapXs.map("1",1).putAll(map2).build(),equalTo(map));
    }

    @Test
    public void testMapKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        assertThat(MapXs.map("1",1,"2", 2).put("3", 3,"4",4).build(),equalTo(map));
    }

    @Test
    public void testMapKVKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        map.put("5",5);
        map.put("6",6);
        assertThat(MapXs.map("1",1,"2", 2,"3", 3).put("4",4,"5",5,"6",6).build(),equalTo(map));
    }

    @Test
    public void testMapKVKVKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        map.put("5",5);
        map.put("6",6);
        map.put("7",7);
        map.put("8",8);
        assertThat(MapXs.map("1",1,"2", 2,"3", 3,"4",4).put("5",5,"6",6,"7",7,"8",8).build(),equalTo(map));
    }

}
