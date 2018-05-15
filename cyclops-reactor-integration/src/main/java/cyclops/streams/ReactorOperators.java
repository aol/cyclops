package cyclops.streams;


import cyclops.companion.reactor.Fluxs;
import cyclops.companion.reactor.Monos;

import cyclops.control.Future;
import cyclops.reactive.FluxReactiveSeq;
import cyclops.reactive.ReactiveSeq;

import cyclops.reactive.Spouts;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.function.Function;

/*
 * Extensions for leveraging Reactor operators with cyclops-react ReactiveSeq instances and vice versa
 *
 * <pre>
 * {@code
 *   ReactiveSeq.of(1,2,3)
                .to(flux(f->f.buffer(publisher))
                .map(i->i*2);

   }
 * </pre>
 *
 */
public class ReactorOperators {


    public static <T,R> Function<ReactiveSeq<T>,ReactiveSeq<R>> flux(final Function<? super Flux<? super T>,? extends Flux<? extends R>> fn){
        return s-> FluxReactiveSeq.<R>reactiveSeq(Fluxs.narrow(fn.apply(Fluxs.fluxFrom(s))));
    }
    public static <T,R> Function<Flux<T>,Flux<R>> seq(final Function<? super ReactiveSeq<? super T>,? extends ReactiveSeq<? extends R>> fn){
        return s-> Flux.from(fn.apply(Spouts.from(s)));
    }


    public static <T,R> Function<Future<T>,Future<R>> mono(final Function<? super Mono<? super T>,? extends Mono<? extends R>> fn){

            return s-> Future.fromPublisher(Monos.narrow(fn.apply(Mono.from(s))));
    }
    public static <T,R> Function<Mono<T>,Mono<R>> future(final Function<? super Future<? super T>,? extends Future<? extends R>> fn){
            return s-> Mono.from(fn.apply(Future.fromPublisher(s)));
    }

}
