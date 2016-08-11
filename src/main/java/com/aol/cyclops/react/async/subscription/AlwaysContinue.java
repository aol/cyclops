package com.aol.cyclops.react.async.subscription;

import com.aol.cyclops.data.async.Queue;

public class AlwaysContinue implements Continueable {

    public void closeQueueIfFinished(Queue queue) {

    }

    @Override
    public void addQueue(Queue queue) {

    }

    public void registerSkip(long skip) {

    }

    public void registerLimit(long limit) {

    }

    @Override
    public void closeAll(Queue queue) {

    }

    @Override
    public boolean closed() {

        return false;
    }

    @Override
    public void closeQueueIfFinishedStateless(Queue queue) {

    }

    @Override
    public void closeAll() {

    }

    @Override
    public long timeLimit() {

        return -1;
    }

    @Override
    public void registerTimeLimit(long nanos) {

    }
}
