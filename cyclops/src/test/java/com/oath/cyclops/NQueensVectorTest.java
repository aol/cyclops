package com.oath.cyclops;

import com.oath.cyclops.types.traversable.IterableX;
import cyclops.reactive.collections.immutable.VectorX;
import lombok.val;
import org.junit.Test;

import static cyclops.reactive.collections.immutable.VectorX.range;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class NQueensVectorTest {
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

    public VectorX<VectorX<Integer>> placeQueens(int k) {
        if (k == 0)
            return VectorX.of(VectorX.empty());
        else {
            return placeQueens(k - 1).forEach2(queens -> range(1, num+1 ),
                                               (queens, column) -> isSafe(column, queens, 1),
                                               (queens, column) -> queens.insertAt(0,column));
        }
    }


    public Boolean isSafe(int column, IterableX<Integer> queens, int delta){
       return  queens.visit((c, rest)-> c != column &&
                                           Math.abs(c - column) != delta &&
                                           isSafe(column, rest, delta + 1) ,
                            ()->true);
    }



    public void show(VectorX<VectorX<Integer>> solutions){
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
