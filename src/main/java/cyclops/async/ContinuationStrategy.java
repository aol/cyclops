package cyclops.async;

import com.aol.cyclops2.types.futurestream.Continuation;

public interface ContinuationStrategy {

    public void addContinuation(Continuation c);

    public void handleContinuation();
}
