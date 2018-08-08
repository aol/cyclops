package cyclops.data.talk;

import cyclops.data.ImmutableList;
import cyclops.data.Seq;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImmutableTest {
    int CORE_USER = 0;
    int context =-1;
    @Test
    public ImmutableList<Integer> unmod(){

        cyclops.data.ImmutableList<Integer> userIds = findUserIds(context);
        ImmutableList<Integer> newList = userIds.plus(10)
                                             .plus(20);
        doSomething(newList);
        return userIds;


    }

    public void doSomething(ImmutableList<Integer> list){
        updateActiveUsers(list.plus(CORE_USER));
    }

    private void updateActiveUsers(ImmutableList<Integer> list) {
    }
    private ImmutableList<Integer> findUserIds(int context) {
        return Seq.of(1);
    }
}
