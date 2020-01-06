package cyclops.control.future.futureOverwriteIssue;

import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.control.Either;
import cyclops.control.LazyEither;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.util.concurrent.Executor;

@AllArgsConstructor
@Getter
public abstract class DataFileMetadata {

    private final long customerId;
    private final String type;
    private final Either<IOException,String> contents = null;//LazyEither.later(this::loadContents);

    protected abstract Either<IOException,String> loadContents();


    public LazyEither<IOException,String> loadAsync(Executor ex){


        LazyEither.CompletableEither<String, String> res = LazyEither.<String>either();
        ex.execute(()->loadContents().fold(e->
                res.completeExceptionally(e),
                c->res.complete(c)));

        return res.mapLeft(this::ioOrThrow);
    }

    private IOException ioOrThrow(Throwable t){
        if(t instanceof IOException){
            return (IOException)t;
        }
        throw ExceptionSoftener.throwSoftenedException(t);
    }




}
