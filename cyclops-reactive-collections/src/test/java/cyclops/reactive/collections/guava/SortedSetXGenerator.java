package cyclops.reactive.collections.guava;

import java.util.Set;

import cyclops.reactive.collections.mutable.SortedSetX;
import com.google.common.collect.testing.TestStringSetGenerator;

public class SortedSetXGenerator extends  TestStringSetGenerator {


    @Override
    public Set<String> create(String... elements) {
       return SortedSetX.of(elements);
    }

}
