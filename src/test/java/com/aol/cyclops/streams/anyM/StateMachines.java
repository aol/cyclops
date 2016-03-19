package com.aol.cyclops.streams.anyM;

import static com.aol.cyclops.control.Matchable.otherwise;
import static com.aol.cyclops.control.Matchable.then;
import static com.aol.cyclops.control.Matchable.when;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.For;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.util.function.Predicates;

public abstract class StateMachines {

    @FunctionalInterface
    public interface StateMachine<S, M> {
        M accept(S state, CharSequence input);
    }
    
    /**
     * Create a finite state machine with the given transition function. The transition function may be partial. It is
     * extended to a total function by mapping all undefined values to the given default.
     * <p>
     * If we pass in a transition function that has type <code>Optional&lt;State&gt;</code> we will get a deterministic
     * finite state machine, and if we pass in a function that has type <code>List&lt;State&gt;</code> then we will get
     * a non-deterministc one.
     * 
     * @param partialTransitions possibly partial function taking the current state and the next input character and
     *            returning the next state (or states) wrapped in the monad appropriate to the machine's type
     * @param defaultValue function that supplies the value returned for undefined transitions, representing failure
     * @return a finite state machine based on the given transition function
     */
    public static <S, M> StateMachine<S,M> newStateMachine(BiFunction<S, Character, M> partialTransitions, M defaultValue) {
        Objects.requireNonNull(partialTransitions);
        Objects.requireNonNull(defaultValue);
       
        // Make the given function total, and yield a wrapped monad (from an Optional or List, as the case may be)
        BiFunction<S, Character, AnyMSeq<S>> totalTransitions = (s, c) -> 
            anyM(Optional.ofNullable(partialTransitions.apply(s, c)).orElse(defaultValue));

        // Define how to create a new instance of the underlying monad
        Function<S, AnyMSeq<S>> unit = s -> anyM(defaultValue).unit(s);
        
        // Return a traversal function and unwrap the result to the original type that went into the AnyM construction
        return (state,input) -> machine(totalTransitions, unit).accept(state, input).unwrap();
    }
    
    private static <S> AnyMSeq<S> anyM(Object o){
        //somewhat risky action of allowing Optional types into the same AnyM bucket as Lists, if we flatMap optional by List 
        return AnyM.ofSeq(o);
    }

    // walk :: Monad m => s -> (s -> Char -> m s) -> [Char] -> m s
    // walk state advance [] = return state
    // walk state advance (i:inputs) = do
    //                                  next <- advance state i
    //                                  walk next advance inputs
    private static <S> StateMachine<S, AnyMSeq<S>> machine(BiFunction<S, Character, AnyMSeq<S>> transitions, Function<S, AnyMSeq<S>> unit) {
        return (state,input) -> {
            if (input.length() == 0) return unit.apply(state);
           
            return transitions.apply(state, input.charAt(0))
                       .flatMap(next -> machine(transitions,unit).accept(next, input.subSequence(1, input.length())));
        };
    }
}
