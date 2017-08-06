package cyclops.typeclasses;

import org.junit.Test;

import static cyclops.typeclasses.EnumerationTest.Days.Monday;
import static cyclops.typeclasses.EnumerationTest.Days.Thursday;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 29/07/2017.
 */
public class EnumerationTest {

    public enum Days { Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday}

    @Test
    public void monday(){

        assertThat(Enumeration.enums(Days.values())
                             .stream(Monday)
                             .join(" "),equalTo("Monday Tuesday Wednesday Thursday Friday Saturday Sunday"));

        assertThat(Enumeration.enums(Days.values())
                .stream(Thursday)
                .join(" "),equalTo("Thursday Friday Saturday Sunday"));
    }
}