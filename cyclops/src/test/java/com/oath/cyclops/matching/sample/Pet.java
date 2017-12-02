package com.oath.cyclops.matching.sample;

import com.oath.cyclops.matching.Deconstruct;
import cyclops.data.tuple.Tuple3;
import lombok.AllArgsConstructor;
import lombok.Getter;

public interface Pet extends Deconstruct.Deconstruct3<String, Integer, String> {

  String getName();

  int getAge();

  String getGender();

  @Override
  default Tuple3<String, Integer, String> unapply() {
    return new Tuple3<>(getName(), getAge(), getGender());
  }

  @AllArgsConstructor
  @Getter
  class Dog implements Pet {
    private final String name;
    private final int age;
    private final String gender;
  }

  @AllArgsConstructor
  @Getter
  class Cat implements Pet {
    private final String name;
    private final int age;
    private final String gender;
  }

}
