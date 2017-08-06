package cyclops.function;

import cyclops.companion.Groups;
import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 27/07/2017.
 */
public class GroupTest {

    @Test
    public void stringReverse(){
        String res = Groups.stringConcat.reduceReverse(Stream.of("hello","world","reversed"));
        System.out.println(res);
    }

}