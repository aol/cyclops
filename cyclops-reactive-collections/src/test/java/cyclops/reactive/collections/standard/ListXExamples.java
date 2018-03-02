package cyclops.reactive.collections.standard;

import cyclops.reactive.collections.mutable.ListX;
import org.junit.Test;

/**
 * Created by johnmcclean on 19/07/2017.
 */
public class ListXExamples {


    @Test
    public void comprehension(){


        ListX<Integer> first10EvenInts = ListX.range(1,10)
                                              .map(i->i*2);

        ListX<Integer> greaterThan12 = ListX.range(1,10)
                                            .map(i->i*2)
                                            .filter(i->i>12);



        ListX.of(2,5,10)
                .forEach2(i->ListX.of(8,10,11),(x,y)->x*y);

        // ListX[16,20,22,40,50,55,80,100,110]


        ListX.of(2,5,10)
                .forEach2(i->ListX.of(8,10,11),(x,y)->x*y>50,(x,y)->x*y);

        // ListX[55,80,100,110]


        ListX.of(2,5,10)
                .forEach2(i->ListX.of(8,10,11),(x,y)->x*y)
                .filter(p->p>50);

        // ListX[55,80,100,110]





    }
    @Test
    public void append(){

        ListX<Integer> oneToTen = ListX.of(1,2,3,4)
                                        .appendAll(5,6,7,8,9,10);


        ListX<Integer> alsoOneToTen = ListX.of(1,2,3,4)
                                           .plusAll(ListX.of(5,6,7,8,9,10));



        alsoOneToTen.printOut();

    }
}
