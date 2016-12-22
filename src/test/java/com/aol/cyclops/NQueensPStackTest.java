package com.aol.cyclops;

import org.junit.Test;

import cyclops.collections.immutable.PStackX;
import cyclops.collections.immutable.PVectorX;

import static cyclops.collections.immutable.PStackX.range;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import lombok.val;

public class NQueensPStackTest {
    private final int num=8;
    
    @Test
    public void run(){
        val queens = placeQueens(num);
        assertThat(queens.size(),equalTo(82));
        show(placeQueens(num));
    }
    
    public PStackX<PStackX<Integer>> placeQueens(int k) {
        if (k == 0)
            return PStackX.of(PStackX.empty());
        else {
            return placeQueens(k - 1).forEach2(queens -> range(1, num + 1),
                                               (queens, column) -> isSafe(column, queens, 1),
                                               (queens, column) -> queens.plus(column));
        }
    }
    
    
    public Boolean isSafe(int column, PStackX<Integer> queens, int delta){
       return  queens.visit((c, rest)-> c != column &&
                                           Math.abs(c - column) != delta &&
                                           isSafe(column, rest.toPStackX(), delta + 1) ,
                            ()->true);
    }
           

    
    public void show(PStackX<PStackX<Integer>> solutions){
        solutions.forEach(solution->{
            System.out.println("----Solution----");
            solution.forEach(col->{
                System.out.println(PVectorX.range(0,solution.size())
                                           .map(i->"*")
                                           .with(col-1, "X")
                                           .join(" "));
            });
        });
    }

}
