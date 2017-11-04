package cyclops.collections.persistent;

import cyclops.collections.immutable.LinkedListX;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

/**
 * Created by johnmcclean on 02/06/2017.
 */
public class LinkedListErrorTest {
    @Test
    public void iterator(){
        for(Integer next : ReactiveSeq.of(1,2,3)
                .dropRight(1)){
            System.out.println(next);
        }

    }
    @Test
    public void dropRight23(){
        LinkedListX.of(1,2,3).dropRight(1)
                .iterator();
        for(Integer next : LinkedListX.of(1,2,3).dropRight(1))
            System.out.println(next);

       // LinkedListX.of(1,2,3).dropRight(1).forEach(System.out::println);
        /**
        System.out.println(LinkedListX.of(1,2,3).dropRight(1).join("-"));
        System.out.println("--");
        System.out.println(ReactiveSeq.fromIterable(LinkedListX.of(1,2,3).dropRight(1)).collect(Collectors.toList()));
        System.out.println("--");
        System.out.println(LinkedListX.of(1,2,3).dropRight(1).collect(Collectors.toList()));
        System.out.println("--");
        LinkedListX.of(1,2,3).dropRight(1).printOut();
        System.out.println("--");
        LinkedListX.of(1,2,3).dropRight(1).forEach(System.out::println);
        System.out.println("Res  = "+LinkedListX.of(1,2,3).dropRight(1).toList());
         **/
    }
}
