package cyclops.typeclasses.functions;

import com.oath.cyclops.hkt.Higher;
import cyclops.async.Future;
import cyclops.collections.immutable.*;
import cyclops.collections.mutable.*;
import cyclops.companion.CompletableFutures;
import cyclops.companion.Optionals.OptionalKind;
import cyclops.companion.Streams;
import cyclops.control.Ior;
import cyclops.control.Maybe;
import cyclops.control.Try;
import cyclops.control.Either;
import com.oath.cyclops.hkt.DataWitness.*;
import com.oath.cyclops.hkt.DataWitness.list;
import com.oath.cyclops.hkt.DataWitness.optional;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;


public interface SemigroupKs{



    public static SemigroupK<optional> optionalPresent() {
        return new SemigroupK<optional>() {
          @Override
          public <T> Higher<optional, T> apply(Higher<optional, T> a, Higher<optional, T> b) {
            return  OptionalKind.narrowK(a).isPresent() ? a : b;
          }
        };
    }
    public static SemigroupK<list> listXConcat() {
      return new SemigroupK<list>() {

        @Override
        public <T> Higher<list, T> apply(Higher<list, T> a, Higher<list, T> b) {
          return ListX.narrowK(a).plusAll(ListX.narrowK(b));
        }
      };

    }


    static SemigroupK<set> setXConcat() {
      return new SemigroupK<set>() {
        @Override
        public <T> Higher<set, T> apply(Higher<set, T> a, Higher<set, T> b) {
          return SetX.narrowK(a).plusAll(SetX.narrowK(b));
        }
      };

    }



    static SemigroupK<queue> queueXConcat() {
      return new SemigroupK<queue>() {

        @Override
        public <T> Higher<queue, T> apply(Higher<queue, T> a, Higher<queue, T> b) {
          return QueueX.narrowK(a).plusAll(QueueX.narrowK(b));
        }
      };
    }

    /**
     * @return A combiner for DequeX (concatenates two DequeX into a single DequeX)
     */
    static SemigroupK<deque> dequeXConcat() {
      return new SemigroupK<deque>() {

        @Override
        public <T> Higher<deque, T> apply(Higher<deque, T> a, Higher<deque, T> b) {
          return DequeX.narrowK(a).plusAll(DequeX.narrowK(b));
        }
      };

    }

    /**
     * @return A combiner for LinkedListX (concatenates two LinkedListX into a single LinkedListX)
     */
    static SemigroupK<linkedListX> linkedListXConcat() {
      return new SemigroupK<linkedListX>() {

        @Override
        public <T> Higher<linkedListX, T> apply(Higher<linkedListX, T> a, Higher<linkedListX, T> b) {
          return LinkedListX.narrowK(a).plusAll(LinkedListX.narrowK(b));
        }
      };

    }

    /**
     * @return A combiner for VectorX (concatenates two VectorX into a single VectorX)
     */
    static SemigroupK<vectorX> vectorXConcat() {
      return new SemigroupK<vectorX>() {

        @Override
        public <T> Higher<vectorX, T> apply(Higher<vectorX, T> a, Higher<vectorX, T> b) {
          return VectorX.narrowK(a).plusAll(VectorX.narrowK(b));
        }
      };
    }


    static SemigroupK<persistentQueueX> persistentQueueXConcat() {
      return new SemigroupK<persistentQueueX>() {

        @Override
        public <T> Higher<persistentQueueX, T> apply(Higher<persistentQueueX, T> a, Higher<persistentQueueX, T> b) {
          return PersistentQueueX.narrowK(a).plusAll(PersistentQueueX.narrowK(b));
        }
      };
    }
  static SemigroupK<persistentSetX> persistentSetXConcat() {
    return new SemigroupK<persistentSetX>() {

      @Override
      public <T> Higher<persistentSetX, T> apply(Higher<persistentSetX, T> a, Higher<persistentSetX, T> b) {
        return PersistentSetX.narrowK(a).plusAll(PersistentSetX.narrowK(b));
      }
    };
  }


    /**
     * @return Combination of two ReactiveSeq Streams b is appended to a
     */
    static SemigroupK<reactiveSeq> combineReactiveSeq() {
      return new SemigroupK<reactiveSeq>() {

        @Override
        public <T> Higher<reactiveSeq, T> apply(Higher<reactiveSeq, T> a, Higher<reactiveSeq, T> b) {
          return  ReactiveSeq.narrowK(a).appendS(ReactiveSeq.narrowK(b));
        }
      };

    }

    static SemigroupK<reactiveSeq> firstNonEmptyReactiveSeq() {
      return new SemigroupK<reactiveSeq>() {

        @Override
        public <T> Higher<reactiveSeq, T> apply(Higher<reactiveSeq, T> a, Higher<reactiveSeq, T> b) {
          return  ReactiveSeq.narrowK(a).onEmptySwitch(()->ReactiveSeq.narrowK(b));
        }
      };

    }
    static SemigroupK<reactiveSeq> ambReactiveSeq() {
      return new SemigroupK<reactiveSeq>() {

        @Override
        public <T> Higher<reactiveSeq, T> apply(Higher<reactiveSeq, T> a, Higher<reactiveSeq, T> b) {
          return Spouts.amb(ReactiveSeq.narrowK(a),ReactiveSeq.narrowK(b));
        }
      };

    }

    static SemigroupK<reactiveSeq> mergeLatestReactiveSeq() {
      return new SemigroupK<reactiveSeq>() {

        @Override
        public <T> Higher<reactiveSeq, T> apply(Higher<reactiveSeq, T> a, Higher<reactiveSeq, T> b) {
          return Spouts.mergeLatest(ReactiveSeq.narrowK(a),ReactiveSeq.narrowK(b));
        }
      };

    }



    /**
     * @return Combination of two Stream's : b is appended to a
     */
    static SemigroupK<stream> combineStream() {
      return new SemigroupK<stream>() {

        @Override
        public <T> Higher<stream, T> apply(Higher<stream, T> a, Higher<stream, T> b) {
          return Streams.StreamKind.widen(Stream.concat(Streams.StreamKind.narrow(a), Streams.StreamKind.narrow(b)));
        }
      };

    }


    /**
     * @return Combine two CompletableFuture's by taking the first present
     */
    static SemigroupK<completableFuture> firstCompleteCompletableFuture() {
      return new SemigroupK<completableFuture>() {

        @Override
        public <T> Higher<completableFuture, T> apply(Higher<completableFuture, T> a, Higher<completableFuture, T> b) {
          CompletableFuture x = CompletableFuture.anyOf(CompletableFutures.CompletableFutureKind.<T>narrowK(a), CompletableFutures.CompletableFutureKind.<T>narrowK(b));
          return CompletableFutures.CompletableFutureKind.widen(x);
        }
      };

    }
    /**
     * @return Combine two Future's by taking the first result
     */
    static  SemigroupK<future> firstCompleteFuture() {
      return new SemigroupK<future>() {

        @Override
        public <T> Higher<future, T> apply(Higher<future, T> a, Higher<future, T> b) {
          return Future.anyOf(Future.narrowK(a),Future.narrowK(b));
        }
      };

    }


    /**
     * @return Combine two Future's by taking the first successful
     */
    static  SemigroupK<future> firstSuccessfulFuture() {
      return new SemigroupK<future>() {

        @Override
        public <T> Higher<future, T> apply(Higher<future, T> a, Higher<future, T> b) {
          return Future.firstSuccess(Future.narrowK(a),Future.narrowK(b));
        }
      };

    }
    /**
     * @return Combine two Xor's by taking the first right
     */
    static <ST> SemigroupK<Higher<either,ST>> firstRightEither() {
      return new SemigroupK<Higher<either,ST>>() {

        @Override
        public <T> Higher<Higher<either,ST>, T> apply(Higher<Higher<either,ST>, T> a, Higher<Higher<either,ST>, T> b) {
          return Either.narrowK(a).isRight() ? a : b;
        }
      };

    }
    /**
     * @return Combine two Xor's by taking the first left
     */
    static <ST> SemigroupK<Higher<either,ST>> firstLeftEither() {
      return new SemigroupK<Higher<either,ST>>() {

        @Override
        public <T> Higher<Higher<either,ST>, T> apply(Higher<Higher<either,ST>, T> a, Higher<Higher<either,ST>, T> b) {
          return  Either.narrowK(a).isLeft() ? a : b;
        }
      };
    }
    /**
     * @return Combine two Xor's by taking the last right
     */
    static <ST> SemigroupK<Higher<either,ST>> lastRightEither() {
      return new SemigroupK<Higher<either,ST>>() {

        @Override
        public <T> Higher<Higher<either,ST>, T> apply(Higher<Higher<either,ST>, T> a, Higher<Higher<either,ST>, T> b) {
          return Either.narrowK(b).isRight() ? b : a;
        }
      };

    }
    /**
     * @return Combine two Xor's by taking the last left
     */
    static <ST> SemigroupK<Higher<either,ST>> lastLeftEither() {
      return new SemigroupK<Higher<either,ST>>() {

        @Override
        public <T> Higher<Higher<either,ST>, T> apply(Higher<Higher<either,ST>, T> a, Higher<Higher<either,ST>, T> b) {
          return Either.narrowK(b).isLeft() ? b : a;
        }
      };
    }
    /**
     * @return Combine two Try's by taking the first right
     */
    static <X extends Throwable> SemigroupK<Higher<tryType,X>> firstTrySuccess() {
      return new SemigroupK<Higher<tryType,X>>() {

        @Override
        public <T> Higher<Higher<tryType,X>, T> apply(Higher<Higher<tryType,X>, T> a, Higher<Higher<tryType,X>, T> b) {
          return Try.narrowK(a).isSuccess() ? a : b;
        }
      };

    }
    /**
     * @return Combine two Try's by taking the first left
     */
    static <X extends Throwable> SemigroupK<Higher<tryType,X>> firstTryFailure() {
      return new SemigroupK<Higher<tryType,X>>() {

        @Override
        public <T> Higher<Higher<tryType,X>, T> apply(Higher<Higher<tryType,X>, T> a, Higher<Higher<tryType,X>, T> b) {
          return Try.narrowK(a).isFailure() ? a : b;
        }
      };

    }
    /**
     * @return Combine two Tryr's by taking the last right
     */
    static<X extends Throwable> SemigroupK<Higher<tryType,X>> lastTrySuccess() {
      return new SemigroupK<Higher<tryType,X>>() {

        @Override
        public <T> Higher<Higher<tryType,X>, T> apply(Higher<Higher<tryType,X>, T> a, Higher<Higher<tryType,X>, T> b) {
          return Try.narrowK(b).isSuccess() ? b : a;
        }
      };

    }
    /**
     * @return Combine two Try's by taking the last left
     */
    static <X extends Throwable> SemigroupK<Higher<tryType,X>> lastTryFailure() {
      return new SemigroupK<Higher<tryType,X>>() {

        @Override
        public <T> Higher<Higher<tryType,X>, T> apply(Higher<Higher<tryType,X>, T> a, Higher<Higher<tryType,X>, T> b) {
          return Try.narrowK(b).isFailure() ? b : a;
        }
      };

    }
    /**
     * @return Combine two Ior's by taking the first right
     */
    static <ST> SemigroupK<Higher<ior,ST>> firstPrimaryIor() {
      return new SemigroupK<Higher<ior,ST>>() {

        @Override
        public <T> Higher<Higher<ior,ST>, T> apply(Higher<Higher<ior,ST>, T> a, Higher<Higher<ior,ST>, T> b) {
          return Ior.narrowK(a).isRight() ? a : b;
        }
      };

    }
    /**
     * @return Combine two Ior's by taking the first left
     */
    static <ST> SemigroupK<Higher<ior,ST>> firstSecondaryIor() {
      return new SemigroupK<Higher<ior,ST>>() {

        @Override
        public <T> Higher<Higher<ior,ST>, T> apply(Higher<Higher<ior,ST>, T> a, Higher<Higher<ior,ST>, T> b) {
          return Ior.narrowK(a).isLeft() ? a : b;
        }
      };

    }
    /**
     * @return Combine two Ior's by taking the last right
     */
    static <ST> SemigroupK<Higher<ior,ST>> lastPrimaryIor() {
      return new SemigroupK<Higher<ior,ST>>() {

        @Override
        public <T> Higher<Higher<ior,ST>, T> apply(Higher<Higher<ior,ST>, T> a, Higher<Higher<ior,ST>, T> b) {
          return Ior.narrowK(b).isRight() ? b : a;
        }
      };

    }
    /**
     * @return Combine two Ior's by taking the last left
     */
    static <ST> SemigroupK<Higher<ior,ST>> lastSecondaryIor() {
      return new SemigroupK<Higher<ior,ST>>() {

        @Override
        public <T> Higher<Higher<ior,ST>, T> apply(Higher<Higher<ior,ST>, T> a, Higher<Higher<ior,ST>, T> b) {
          return Ior.narrowK(b).isLeft() ? b : a;
        }
      };

    }

    /**
     * @return Combine two Maybe's by taking the first present
     */
    static SemigroupK<maybe> firstPresentMaybe() {
      return new SemigroupK<maybe>() {

        @Override
        public <T> Higher<maybe, T> apply(Higher<maybe, T> a, Higher<maybe, T> b) {
          return Maybe.narrowK(a).isPresent() ? a : b;
        }
      };

    }

    /**
     * @return Combine two optionals by taking the first present
     */
    static SemigroupK<optional> firstPresentOptional() {
      return new SemigroupK<optional>() {

        @Override
        public <T> Higher<optional, T> apply(Higher<optional, T> a, Higher<optional, T> b) {
          return OptionalKind.narrowK(a).isPresent() ? a : b;
        }
      };

    }

    /**
     * @return Combine two Maybes by taking the last present
     */
    static SemigroupK<maybe> lastPresentMaybe() {
      return new SemigroupK<maybe>() {
        @Override
        public <T> Higher<maybe, T> apply(Higher<maybe, T> a, Higher<maybe, T> b) {
          return  Maybe.narrowK(b).isPresent() ? b : a;
        }
      };

    }

    /**
     * @return Combine two optionals by taking the last present
     */
    static SemigroupK<optional> lastPresentOptional() {
      return new SemigroupK<optional>() {
        @Override
        public <T> Higher<optional, T> apply(Higher<optional, T> a, Higher<optional, T> b) {
          return OptionalKind.narrowK(b).isPresent() ? b : a;
        }
      };

    }
}
