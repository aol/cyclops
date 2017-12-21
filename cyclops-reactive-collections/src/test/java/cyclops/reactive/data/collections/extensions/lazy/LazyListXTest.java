package cyclops.reactive.data.collections.extensions.lazy;

import cyclops.reactive.collections.mutable.ListX;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class LazyListXTest {

    public class Animal {
        protected String name;

        public Animal(String name) {
            this.name = name;
        }

        public String getType() {
            return "Animal";
        }

        public String getName() {
            return name;
        }
    }

    public class Cat extends Animal {
        public Cat(String name) {
            super(name);
        }

        @Override
        public String getType() {
            return "Cat";
        }
    }

    @Test
    public void testIntercalate() {
        LazyListX<Animal> lazyListX = (LazyListX<Animal>) ListX.of(new Animal("A"), new Animal("B"), new Animal("C"));
        List<List<Cat>> listOfLists = Arrays.asList(new List[]{
                Arrays.asList(new Cat[]{new Cat("1"), new Cat("2"), new Cat("3")}),
                Arrays.asList(new Cat[]{new Cat("4"), new Cat("5"), new Cat("6")}),
                Arrays.asList(new Cat[]{new Cat("7"), new Cat("8"), new Cat("9")})
        });
        ListX<Animal> intercalated = lazyListX.intercalate(listOfLists);
        assertThat(intercalated.size(), equalTo(15));
        assertThat(intercalated.get(0).getName(), equalTo("1"));
        assertThat(intercalated.get(1).getName(), equalTo("2"));
        assertThat(intercalated.get(2).getName(), equalTo("3"));
        assertThat(intercalated.get(3).getName(), equalTo("A"));
        assertThat(intercalated.get(4).getName(), equalTo("B"));
        assertThat(intercalated.get(5).getName(), equalTo("C"));
        assertThat(intercalated.get(6).getName(), equalTo("4"));
        assertThat(intercalated.get(7).getName(), equalTo("5"));
        assertThat(intercalated.get(8).getName(), equalTo("6"));
        assertThat(intercalated.get(9).getName(), equalTo("A"));
        assertThat(intercalated.get(10).getName(), equalTo("B"));
        assertThat(intercalated.get(11).getName(), equalTo("C"));
        assertThat(intercalated.get(12).getName(), equalTo("7"));
        assertThat(intercalated.get(13).getName(), equalTo("8"));
        assertThat(intercalated.get(14).getName(), equalTo("9"));
    }

    @Test
    public void testIntercalate2() {
        ListX needle = ListX.of(-1, -1, -1);
        ListX listOfLists = ListX.of(new ListX[]{
                ListX.of(1l,2l,3l),
                ListX.of(4l,5l,6l),
                ListX.of(7l,8l,9l)
        });
        ListX intercalated = needle.intercalate(listOfLists);
        assertThat(intercalated.size(), equalTo(15));
        assertThat(intercalated.get(0), equalTo(1l));
        assertThat(intercalated.get(1), equalTo(2l));
        assertThat(intercalated.get(2), equalTo(3l));
        assertThat(intercalated.get(3), equalTo(-1));
        assertThat(intercalated.get(4), equalTo(-1));
        assertThat(intercalated.get(5), equalTo(-1));
        assertThat(intercalated.get(6), equalTo(4l));
        assertThat(intercalated.get(7), equalTo(5l));
        assertThat(intercalated.get(8), equalTo(6l));
        assertThat(intercalated.get(9), equalTo(-1));
        assertThat(intercalated.get(10), equalTo(-1));
        assertThat(intercalated.get(11), equalTo(-1));
        assertThat(intercalated.get(12), equalTo(7l));
        assertThat(intercalated.get(13), equalTo(8l));
        assertThat(intercalated.get(14), equalTo(9l));
    }

    @Test
    public void testIntercalate3() {
        ListX<Number> listOfNumber = ListX.of(-1, -1, -1);
        ListX<List<Long>> listOfLongList = ListX.of(new ListX[]{
                ListX.of(1l,2l,3l),
                ListX.of(4l,5l,6l),
                ListX.of(7l,8l,9l)
        });
        ListX<Number> intercalated = listOfNumber.intercalate(listOfLongList);
        assertThat(intercalated.size(), equalTo(15));
        assertThat(intercalated.get(0), equalTo(1l));
        assertThat(intercalated.get(1), equalTo(2l));
        assertThat(intercalated.get(2), equalTo(3l));
        assertThat(intercalated.get(3), equalTo(-1));
        assertThat(intercalated.get(4), equalTo(-1));
        assertThat(intercalated.get(5), equalTo(-1));
        assertThat(intercalated.get(6), equalTo(4l));
        assertThat(intercalated.get(7), equalTo(5l));
        assertThat(intercalated.get(8), equalTo(6l));
        assertThat(intercalated.get(9), equalTo(-1));
        assertThat(intercalated.get(10), equalTo(-1));
        assertThat(intercalated.get(11), equalTo(-1));
        assertThat(intercalated.get(12), equalTo(7l));
        assertThat(intercalated.get(13), equalTo(8l));
        assertThat(intercalated.get(14), equalTo(9l));
    }



    @Test
    public void testIntercalate5() {
        ListX<Number> listOfNumber = ListX.of(-1, -1, -1);
        ListX<List<Long>> listOfLongList = ListX.empty();
        ListX<Number> intercalated = listOfNumber.intercalate(listOfLongList);
        assertThat(intercalated.size(), equalTo(3));
        assertThat(intercalated.get(0), equalTo(-1));
        assertThat(intercalated.get(1), equalTo(-1));
        assertThat(intercalated.get(2), equalTo(-1));
    }
}
