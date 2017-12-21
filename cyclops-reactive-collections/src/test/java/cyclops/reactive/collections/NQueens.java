package cyclops.reactive.collections;

import cyclops.reactive.collections.immutable.LinkedListX;
import cyclops.reactive.collections.immutable.VectorX;
import org.junit.Test;

public class NQueens {

    private final int num=8;
    @Test
    public void run(){
        show(placeQueens(num));
    }

    public LinkedListX<LinkedListX<Integer>> placeQueens(int k){
        if (k == 0)
            return LinkedListX.of(LinkedListX.empty());
        else{
            return placeQueens(k - 1).forEach2(
                         queens -> LinkedListX.range(1, num),
                         (queens, column) -> isSafe(column, queens,1),
                         (queens, column) -> queens.plus(column));

        }

    }


    public Boolean isSafe(int column, LinkedListX<Integer> queens, int delta){
       return  queens.visit((c, rest)-> c != column &&
                                           Math.abs(c - column) != delta &&
                                           isSafe(column, rest.to().linkedListX(), delta + 1) ,
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
