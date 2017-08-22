package com.aol.cyclops2.data.collections.extensions.lazy;

import cyclops.collections.mutable.ListX;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class LazyListXTest {

    @Test
    public void testIntercalate() {
        LazyListX<Integer> lazyListX = (LazyListX<Integer>) ListX.of(-1, -1, -1);
        List<List<Integer>> listOfLists = Arrays.asList(new List[]{
                Arrays.asList(new Integer[]{1, 2, 3}),
                Arrays.asList(new Integer[]{4, 5, 6}),
                Arrays.asList(new Integer[]{7, 8, 9})
        });
        ListX<Integer> intercalated = lazyListX.intercalate(listOfLists);
        assertThat(intercalated.size(), equalTo(15));
        assertThat(intercalated.get(0), equalTo(1));
        assertThat(intercalated.get(1), equalTo(2));
        assertThat(intercalated.get(2), equalTo(3));
        assertThat(intercalated.get(3), equalTo(-1));
        assertThat(intercalated.get(4), equalTo(-1));
        assertThat(intercalated.get(5), equalTo(-1));
        assertThat(intercalated.get(6), equalTo(4));
        assertThat(intercalated.get(7), equalTo(5));
        assertThat(intercalated.get(8), equalTo(6));
        assertThat(intercalated.get(9), equalTo(-1));
        assertThat(intercalated.get(10), equalTo(-1));
        assertThat(intercalated.get(11), equalTo(-1));
        assertThat(intercalated.get(12), equalTo(7));
        assertThat(intercalated.get(13), equalTo(8));
        assertThat(intercalated.get(14), equalTo(9));
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
}
