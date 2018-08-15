package cyclops.data.talk;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UnmodifiableTest {
    int CORE_USER = 0;

    public void unmod(){
        List<Integer> list = new ArrayList<>();
        list.add(10);
        list.add(20);
        doSomething(Collections.unmodifiableList(list));
    }


    public void doSomething(List<Integer> list){
        list.add(CORE_USER);
        updateActiveUsers(list);
    }

    private void updateActiveUsers(List<Integer> list) {
    }
}
