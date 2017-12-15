package cyclops.typeclasses;

import cyclops.control.Future;
import com.oath.cyclops.hkt.DataWitness.future;
import cyclops.typeclasses.monad.Monad;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 11/08/2017.
 */
public class KleisliTest {
    Monad<future> monad = Future.FutureInstances.monad();
    interface DAO {
        default Future<String> load(long id){
            return null;
        }
        default Future<Boolean> save(long id,String data){
            return null;
        }
    }
    public Kleisli<future,DAO,Long> findNextId(){
        return Kleisli.of(monad,dao->Future.ofResult(10l));
    }
    public Kleisli<future,DAO,String> loadName(long id){
        return Kleisli.of(monad,dao->Future.ofResult("hello"));
    }
    public Kleisli<future,DAO,Boolean> updateName(long id, String name){

        return Kleisli.of(monad,dao->Future.ofResult(true));
    }
    public Boolean logIfFail(long id, String name, boolean success){
        return "hello".equals(name);
    }
    @Test
    public void forTest(){

        Kleisli<future, DAO, Boolean> findUpdate = findNextId().forEachK3(this::loadName,
                                                                            (id, name) -> updateName(id, name),
                                                                            (id, name, success) -> logIfFail(id, name, success));

        assertThat(findUpdate.apply(new DAO(){}).convert(Future::narrowK).orElse(false),equalTo(true));

    }
}
