package com.aol.cyclops;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.MapX;
import com.aol.cyclops.data.collections.extensions.standard.MapXs;
import com.aol.cyclops.data.collections.extensions.standard.SetX;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ClojureOrJava8 {

    @AllArgsConstructor
    @Getter
    static class Person{
        int age;
    }
    public void cyclopsReactTransformList(){
        
        
        
        ListX<Integer> org = ListX.of(10,20,30).limit(num);
        List<Integer> mapped = org.map(i->i*2);
    
    }
    @Test
    public void comonad(){
        AnyM.fromOptional(Optional.of(1))
            .coflatMap(v->v.isPresent()?v.get() : 10);
        
    }
    @Test
    public void applicative(){
        AnyM.fromOptional(Optional.of(1)).applyFunctions().ap(Semigroups.intMult)
                                         .ap(AnyM.fromOptional(Optional.of(10)));
    }
    
    public void java8TransformList(){
        
        
        List<Integer> org = Arrays.asList(10,20,30);
        List<Integer> mapped = org.stream()
                                    .map(i->i*2)
                                    .collect(Collectors.toList());
        
        
    }
    private String loadData(int d){
        return "";
    }
    
    @Test
    public void listT(){
       
        
        ReactiveSeq.of(10,20,30)
                   .sliding(2,1)
                   .map(list->list.map(i->i*2)
                                  .map(this::loadData))
                   .forEach(list->System.out.println("next list " + list));
        
        ReactiveSeq.of(10,20,30,40,50)
                   .slidingT(2,1)  //create a sliding view, returns a List Transformer
                   .map(i->i*2)  //we now have a Stream of Lists, but still operate on each individual integer
                   .map(this::loadData)
                   .unwrap()
                   .forEach(list->System.out.println("next list " + list));
    
    
    }
    public String processJob(String job){
        return job;
        
    }
    @Test
    public void groupedSet(){
        
        
        
        SetX.of(10,20,30,40,50)
            .grouped(2)
            .printOut();
        
        
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

