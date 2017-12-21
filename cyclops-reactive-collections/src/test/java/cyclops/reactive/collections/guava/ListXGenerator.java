package cyclops.reactive.collections.guava;

import java.util.List;

import cyclops.reactive.collections.mutable.ListX;
import com.google.common.collect.testing.TestStringListGenerator;

public class ListXGenerator extends  TestStringListGenerator {


    @Override
    public List<String> create(String... elements) {
       return ListX.of(elements);
    }

}
