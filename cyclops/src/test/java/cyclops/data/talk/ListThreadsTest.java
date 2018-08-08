package cyclops.data.talk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ListThreadsTest {
    int CORE_USER = 0;
    Executor IO_THREAD_POOL = Executors.newFixedThreadPool(1);

    public List<Integer> future(){

        List<Integer> userIds = findUserIdsActiveThisMonth();

        cyclops.control.Future.of(()->{
                                        userIds.add(CORE_USER);
                                        updateActiveUsersThisMonth(userIds);
                                        return userIds;
                                        },
                                     IO_THREAD_POOL);

        userIds.addAll(findUserIdsActiveThisYear());
        return userIds;

    }



    private Collection<? extends Integer> findUserIdsActiveThisYear() {
        return null;
    }


    private List<Integer> findUserIdsActiveThisMonth() {
        List<Integer> list = new ArrayList<>();
        list.add(10);
        list.add(20);
        return list;
    }
    private void updateActiveUsersThisMonth(List<Integer> list) {
    }
}
