package com.aol.cyclops.streams.anyM;
import static com.aol.cyclops.streams.anyM.StateMachines.newStateMachine;
import static com.aol.cyclops.streams.anyM.StateMachinesTest.State.state;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.aol.cyclops.streams.anyM.StateMachines.StateMachine;
import com.google.common.collect.ArrayTable;

public class StateMachinesTest {

    @Test
    public void demoDFA() {
        ArrayTable<State, Character, Optional<State>> transitiontable = ArrayTable.create(
                asList(state(1), state(2)), asList('a', 'b'));

        transitiontable.put(state(1), 'a', of(state(2)));
        transitiontable.put(state(1), 'b', of(state(1)));
        transitiontable.put(state(2), 'b', of(state(1)));

        StateMachine<State,Optional<State>> dfa = newStateMachine(transitiontable::get, empty());
        Optional<State> finalState = dfa.accept(state(1), "ab");
        assertEquals(state(1), finalState.get());
    }
   
    @Test
    public void demoNFA() {
        ArrayTable<State, Character, List<State>> transitiontable = ArrayTable.create(
                asList(state(1), state(2), state(3)), asList('a', 'b'));

        transitiontable.put(state(1), 'a', asList(state(2), state(3)));
        transitiontable.put(state(2), 'a', asList(state(2)));
        transitiontable.put(state(3), 'b', asList(state(3)));

        StateMachine<State, List<State>> nfa = newStateMachine(transitiontable::get, emptyList());
        List<State> finalStates = nfa.accept(state(1), "ab");
        
        assertEquals(asList(state(3)), finalStates);
    }

   
    
    /** States of a finite state automaton */
    public static class State {
        private final int id; // the identity of the state

        public static State state(int id) {
            return new State(id);
        }

        private State(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "State " + id;
        }

        @Override
        public final int hashCode() {
            return Integer.hashCode(id);
        }

        @Override
        public final boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            State other = (State) obj;
            return id == other.id;
        }
    }
    
}

