package cyclops.reactive;

import cyclops.control.Try;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static cyclops.data.tuple.Tuple.tuple;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HibernateManagedTest {
    SessionFactory factory;
    Session session;
    @Before
    public void setup(){
        factory = mock(SessionFactory.class);
        session = mock(Session.class);
        when(factory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(mock(Transaction.class));
        when(session.createQuery(Mockito.anyString())).thenReturn(mock(Query.class));
    }


    private Try<String,Throwable> deleteFromMyTable(Session s){
        s.createQuery("DELETE FROM myTable")
            .executeUpdate();
        s.flush();
        return Try.success("deleted");
    }

    @Test
    public void hibernate(){

        Try<String, Throwable> res = Managed.of(factory::openSession)
                                            .with(Session::beginTransaction)
                                            .map((session, tx) ->

                                                deleteFromMyTable(session)
                                                        .bipeek(success -> tx.commit(),error -> tx.rollback())


                                            ).foldRun(Try::flatten);

        assertThat(res,equalTo(Try.success("deleted")));

    }

}
