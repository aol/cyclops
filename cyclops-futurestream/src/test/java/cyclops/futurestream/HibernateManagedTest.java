package cyclops.futurestream;

import cyclops.control.Try;
import cyclops.reactive.IO;
import cyclops.reactive.Managed;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

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

        Try<String, Throwable> res = FutureStreamIO.FutureStreamManaged.of(factory::openSession)
                                            .with(Session::beginTransaction)
                                            .map((session, tx) ->{
                                                try {
                                                    verify(session,never()).close();
                                                }catch(Exception e) {
                                                }

                                                return deleteFromMyTable(session)
                                                        .bipeek(success -> tx.commit(),error -> tx.rollback());


    } ).foldRun(Try::flatten);

        assertThat(res,equalTo(Try.success("deleted")));

    }

    @Test
    public void hibernateIO(){

        Try<String, Throwable> res = FutureStreamIO.of(factory)
                                        .checkedBracketWith(SessionFactory::openSession,Session::beginTransaction)
            .mapIO((session, tx) ->{
                try {
                    verify(session,never()).close();
                }catch(Exception e) {
                }

                return deleteFromMyTable(session)
                    .bipeek(success -> tx.commit(),error -> tx.rollback());


            } ).foldRun(Try::flatten);

        assertThat(res,equalTo(Try.success("deleted")));

    }

}
