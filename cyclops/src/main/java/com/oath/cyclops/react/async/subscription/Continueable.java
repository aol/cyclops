package com.oath.cyclops.react.async.subscription;

import cyclops.async.adapters.Queue;

public interface Continueable {

    public void closeQueueIfFinished(Queue queue);

    public void addQueue(Queue queue);

    public void registerSkip(long skip);

    public void registerLimit(long limit);

    public void closeAll(Queue q);

    public boolean closed();

    public void closeQueueIfFinishedStateless(Queue queue);

    public void closeAll();

    public long timeLimit();

    public void registerTimeLimit(long nanos);
}
