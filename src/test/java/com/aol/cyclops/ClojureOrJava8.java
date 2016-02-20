package com.aol.cyclops;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.standard.ListX;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ClojureOrJava8 {

    @AllArgsConstructor
    @Getter
    static class Person{
        int age;
    }
    
    @Test
    public void cyclopsJava8(){
        
        ListX.of(new Person(10))
             .groupBy(Person::getAge);
        
            
    }
    @Test
    public void plainJava8(){
        
        Arrays.asList(new Person(10))
              .stream()
              .collect(Collectors.groupingBy(Person::getAge));
        
        
    }
    
}

