package cyclops.collections.standard;

import com.aol.cyclops2.data.collections.extensions.IndexedSequenceX;
import cyclops.Converters;
import cyclops.collections.mutable.ListX;
import cyclops.monads.Witness;
import cyclops.monads.Witness.list;
import cyclops.monads.transformers.ListT;
import org.junit.Test;

/**
 * Created by johnmcclean on 19/07/2017.
 */
public class ListXExamples {

    @Test
    public void nestedComps(){


        ListT<list,Integer> xxs = ListT.fromList(ListX.of(ListX.of(1,3,5,2,3,1,2,4,5),
                                                                    ListX.of(1,2,3,4,5,6,7,8,9),
                                                                    ListX.of(1,2,4,2,1,6,3,1,3,2,3,6)));

        ListX<IndexedSequenceX<Integer>> list = xxs.filter(i -> i % 2 == 0)
                                                   .unwrapTo(Witness::list);


        //ListX[[2,2,4],[2,4,6,8],[2,4,2,6,2,6]]

    }

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
                                        .append(5,6,7,8,9,10);


        ListX<Integer> alsoOneToTen = ListX.of(1,2,3,4)
                                           .plusAll(ListX.of(5,6,7,8,9,10));



        alsoOneToTen.printOut();

    }
}
