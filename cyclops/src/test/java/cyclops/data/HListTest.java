package cyclops.data;

import org.junit.Test;

/**
 * Created by johnmcclean on 06/09/2017.
 */
public class HListTest {
    @Test
    public void simple(){
        HList.HCons<Integer, HList.HCons<String, HList.HNil>> list = HList.cons(1, HList.of("hello"));
    }

}