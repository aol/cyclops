package cyclops.data;

import cyclops.collections.tuple.Tuple;
import cyclops.control.Option;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 01/09/2017.
 */
public class BankersQueueTest {

    @Test
    public void enqueue(){
        BankersQueue<Integer> q = BankersQueue.cons(1);

        assertThat(q.dequeue(-1),equalTo(Tuple.tuple(1,q.tail())));




    }
    @Test
    public void enqueue2(){
        BankersQueue<Integer> q = BankersQueue.cons(1).enqueue(10);


        assertThat(q.dequeue(-1),equalTo(Tuple.tuple(10,q.tail())));
        BankersQueue<Integer> q2  = q.dequeue(-1)._2();

        assertThat(q2.dequeue(-1),equalTo(Tuple.tuple(1,q2.tail())));

    }

    @Test
    public void get(){
        BankersQueue<Integer> q = BankersQueue.of(1,2,3);

        assertThat(q.get(0),equalTo(Option.some(1)));
        assertThat(q.get(1),equalTo(Option.some(2)));
        assertThat(q.get(2),equalTo(Option.some(3)));
        assertThat(q.get(3).isPresent(),equalTo(false));
        assertThat(q.get(-1).isPresent(),equalTo(false));
    }
}
