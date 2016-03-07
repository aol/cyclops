package com.aol.cyclops;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.MapX;
import com.aol.cyclops.data.collections.extensions.standard.MapXs;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ClojureOrJava8 {

    @AllArgsConstructor
    @Getter
    static class Person{
        int age;
    }
    
    
    public MapX<Integer, List<Person>> cyclopsJava8(ListX<Person> people){
        
      return people.groupBy(Person::getAge);
        
            
    }
    
    public Map<Integer, List<Person>> plainJava8(List<Person> people){
        return people.stream()
              .collect(Collectors.groupingBy(Person::getAge));   
    }
    
    
    public void transformMap(){
        
       MapX<String,String> x = MapX.fromMap(MapXs.of("hello","1"));
       
       MapX<String,Integer> y = x.map(Integer::parseInt);
       
       MapX<String,Integer> y2 = MapX.fromMap(MapXs.of("hello","1"))
                                    .map(Integer::parseInt);
        
    }
    public void transformMapJava8(){
        
        Map<String,String> x = MapXs.of("hello","1");
        
        Map<String,Integer> y = x.entrySet()
                                 .stream()
                                 .collect(Collectors.toMap(e->e.getKey(), e->Integer.parseInt(e.getValue())));
         
     }
    @Test
    public void listToString(){
        assertThat(listToString(ListX.of("a","b","c")),equalTo("a b c "));
    }
    
    public String listToString(ListX<String> list){
        
        return list.visit(((x,xs)->x+" "+listToString(xs.toListX())),()->"");
    
    }
    
}

