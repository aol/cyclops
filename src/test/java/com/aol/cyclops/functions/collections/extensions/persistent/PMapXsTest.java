package com.aol.cyclops.functions.collections.extensions.persistent;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.persistent.PMapXs;
public class PMapXsTest {

    @Test
    public void testOf() {
        assertThat(PMapXs.of(),equalTo(new HashMap()));
    }

    @Test
    public void testOfKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("key",1);
        assertThat(PMapXs.of("key",1),equalTo(map));
    }

    @Test
    public void testOfKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        assertThat(PMapXs.of("1",1,"2",2),equalTo(map));
    }

    @Test
    public void testOfKVKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        assertThat(PMapXs.of("1",1,"2",2,"3",3),equalTo(map));
    }

    @Test
    public void testOfKVKVKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        assertThat(PMapXs.of("1",1,"2",2,"3",3,"4",4),equalTo(map));
    }

    @Test
    public void testFrom() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        assertThat(PMapXs.from(map).build(),equalTo(map));
    }

    @Test
    public void testMapKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        assertThat(PMapXs.map("1",1).put("2", 2).build(),equalTo(map));
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
        assertThat(PMapXs.map("1",1).putAll(map2).build(),equalTo(map));
    }

    @Test
    public void testMapKVKV() {
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);
        map.put("4",4);
        assertThat(PMapXs.map("1",1,"2", 2).put("3", 3,"4",4).build(),equalTo(map));
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
        assertThat(PMapXs.map("1",1,"2", 2,"3", 3).put("4",4,"5",5,"6",6).build(),equalTo(map));
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
        assertThat(PMapXs.map("1",1,"2", 2,"3", 3,"4",4).put("5",5,"6",6,"7",7,"8",8).build(),equalTo(map));
    }

}
