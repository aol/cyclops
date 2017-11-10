package com.oath.cyclops.matching;

import static cyclops.matching.Api.Case;
import static cyclops.matching.Api.MatchType;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;

import cyclops.control.Option;
import cyclops.data.Seq;
import lombok.AllArgsConstructor;
import org.junit.Assert;
import org.junit.Test;

public class OptionalMatchingTest {

  @Test
  public void shouldMatchOptionalPresent() {
    Assert.assertEquals((Long) 1L, new Matching.OptionalMatching<>(of("present")).of(new Case.CaseOptional<>(() -> 1L, () -> 2L)));
  }

  @Test
  public void shouldMatchOptionalAbsent() {
    Assert.assertEquals((Long) 2L, new Matching.OptionalMatching<>(empty()).of(new Case.CaseOptional<>(() -> 1L, () -> 2L)));
  }

  @Test
  public void matchOption(){

    int defaultValue =-1;
    Option<Integer> empty = Option.none();
    int res  = MatchType(empty).of(Case(value->value),Case(nil->defaultValue));

    //res is -1

    int res2 = empty.fold(v->v,n->-1);

    //res2 is -1

    System.out.println("res is " + res);
    System.out.println("res2 is " + res2);
  }

  @Test
  public void matchList(){

    Seq<Integer> list = Seq.of(1,2,3);
    int first = MatchType(list).of(Case(nel->nel.head()),Case(empty->-1));

    int first2 = list.fold(s->s.head(),e->-1);

    //first & first2 are 1

   // list.head(); //no such method


  }

  static interface Remote{
    public String data();
    public void save(int i);
  }
  @AllArgsConstructor
  static class NeedsMockingToTestLogic {

    Remote remote;

    public void remoteDataProcessing(){

      remote.save(remote.data().length()*50);


    }

  }
  @AllArgsConstructor
  static class DoesntNeedMockingToTestBusinessLogic {

    Remote remote;

    public String dataLoader(){
      return remote.data();
    }
    public void dataSaver(int data){
      remote.save(data);
    }
    public int remoteStringLength(String data){

      return data.length()*50;


    }

  }
}
