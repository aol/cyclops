package cyclops.futurestream;

import cyclops.reactive.ReactiveSeq;
import cyclops.streams.AbstractReactiveSeqTest;

public class FutureStreamReactiveSeqTest extends AbstractReactiveSeqTest {
    @Override
    public ReactiveSeq<Integer> of(Integer... values) {
        return LazyReact.sequentialBuilder()
                            .of(values);
    }

    @Override
    public ReactiveSeq<Integer> empty() {
        return FutureStream.builder()
                            .of();
    }
}
