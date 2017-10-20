package com.aol.cyclops2;

import com.aol.cyclops2.types.traversable.IterableX;
import cyclops.collectionx.immutable.LinkedListX;
import org.junit.Test;

import cyclops.collectionx.immutable.VectorX;

import static cyclops.collectionx.immutable.LinkedListX.range;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import lombok.val;

public class NQueensPStackTest {
    private final int num=8;

    @Test
    public void rangeTest(){
        System.out.println(range(1,10));
    }
    @Test
    public void run(){
        val queens = placeQueens(num);
        assertThat(queens.size(),equalTo(92));
        show(placeQueens(num));
    }
    
    public LinkedListX<LinkedListX<Integer>> placeQueens(int k) {
        if (k == 0)
            return LinkedListX.of(LinkedListX.empty());
        else {
            return placeQueens(k - 1).forEach2(queens -> range(1, num+1 ),
                                               (queens, column) -> isSafe(column, queens, 1),
                                               (queens, column) -> queens.plus(column));
        }
    }
    
    
    public Boolean isSafe(int column, IterableX<Integer> queens, int delta){
       return  queens.visit((c, rest)-> c != column &&
                                           Math.abs(c - column) != delta &&
                                           isSafe(column, rest, delta + 1) ,
                            ()->true);
    }
           

    
    public void show(LinkedListX<LinkedListX<Integer>> solutions){
        solutions.forEach(solution->{
            System.out.println("----Solution----");
            solution.forEach(col->{
                System.out.println(VectorX.range(0,solution.size())
                                           .map(i->"*")
                                           .insertAt(col-1, "X")
                                           .join(" "));
            });
        });
    }

}
