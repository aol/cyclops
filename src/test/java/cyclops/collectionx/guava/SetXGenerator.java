package cyclops.collectionx.guava;

import java.util.Set;

import cyclops.collectionx.mutable.SetX;
import com.google.common.collect.testing.TestStringSetGenerator;

public class SetXGenerator extends  TestStringSetGenerator {

    
    @Override
    public Set<String> create(String... elements) {
       return SetX.of(elements);
    }

}
