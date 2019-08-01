package cyclops.control.future.futureOverwriteIssue;


import cyclops.control.Either;
import cyclops.data.NonEmptyList;
import cyclops.data.Vector;

public class AuthorizationService3 {
    public Either<Processor.Error, Vector<DataFileMetadata>> isAuthorized(User user, NonEmptyList<DataFileMetadata> nel) {
        return Either.right(nel.vector());
    }
}
