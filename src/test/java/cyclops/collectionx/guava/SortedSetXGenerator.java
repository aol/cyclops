package cyclops.collectionx.guava;

import java.util.Set;

import cyclops.collectionx.mutable.SortedSetX;
import com.google.common.collect.testing.TestStringSetGenerator;

public class SortedSetXGenerator extends  TestStringSetGenerator {

    
    @Override
    public Set<String> create(String... elements) {
       return SortedSetX.of(elements);
    }

}
