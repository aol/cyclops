package cyclops.streams;

import cyclops.reactive.ReactiveSeq;

public class PullReactiveSeqTest extends AbstractReactiveSeqTest {

    @Override
    public ReactiveSeq<Integer> of(Integer... values) {
        return ReactiveSeq.of(values);
    }

    @Override
    public ReactiveSeq<Integer> empty() {
        return ReactiveSeq.empty();
    }

}
