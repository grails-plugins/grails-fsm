package grails.plugin.fsm
/**
 *
 * DSL for defining Finite State Machines.
 *
 * Derived work of:
 *   http://www.bytemycode.com/snippets/snippet/640/
 *
 * New Features:
 *   - It now will allow 'when' clauses, acepting a closure whose evaluation
 *     to true/false will drive the transition being usable or not.
 *   - Logically, it nows accept multiple transitions from the same state/event.
 *   - It will get a target object in the constructor, and will provide this object
 *     with 'firexxxx' methods so the FSM behaviour is implemented in the entity
 *     (this has now been moved into the Grails plugin, so no longer in this class)
 *
 * See FsmSupportTests.groovy for use samples.
 *
 * TODO: Add 'DO' clause so some code might be launched when a transition is triggered
 * TODO: Add 'wildcards (*)' to 'from' so we can generalize better!
 * TODO: Allow an option to allow "wildcards" or not?
 * TODO: Define a way of unifying the event models, so one event can move several
 * flows defined in the entity
 * TODO: Why DON'T fireXXXX methods work with Grails' entities? Forced to use fire(flow, event)!!
 * @author jorge
 *
 */
class FsmSupport {
    /*
     * A map or maps to store events and from states of each
     * transition. The last element is another map with 'to' states
     * and its attached condition, to be evaluated
     *
     *
     *
        transitions = [
            event1 : [
                from1 : [
                    to1 : cond1,  // "conds" are closures!!
                    to2 : cond2,  // order is not guaranteed
                    to3 : cond3   // first to be evaluated 'true' will win ;)
                ],
                from2 : [
                    to1 : cond1,
                    to2 : cond2
                ]
                from3 : [
                    to1 : {true}  // default closure condition 'true'
                ]
            ]
        ]
     */
    Map<String, Map<String, Map<String, Closure>>> transitions = [:]
    Map actions = [:]

    String initialState
    String currentState
    String property
    def target

    /**
     * Will hold relationships to any object & property any FsmSupport
     * has been tied to.
     */
    static Map relationships = [:]

    FsmSupport(targetObject, String targetProperty, String a_initialState = null, String a_currentState = null) {
        /**
         * If no initialState has been provided we'll try to get it
         * from the object-property itself!!
         */
        if (!a_initialState) {
            a_initialState = targetObject.getProperty(targetProperty)
        }
        initialState = a_initialState
        if (a_currentState) {
            currentState = a_currentState
        } else {
            currentState = initialState
        }
        target = targetObject
        property = targetProperty
        String prop = property.toLowerCase()

        if (!relationships[targetObject]) {
            relationships[targetObject] = [:]
        }
        relationships[targetObject][prop] = this
        targetObject[property] = currentState
    }

    Grammar record() {
        Grammar.newInstance(this)
    }

    FsmSupport registerTransition(Grammar a_grammar) {
        assert a_grammar.isValid(), "Invalid transition (${a_grammar})"

        String event = a_grammar.event
        String fromState = a_grammar.fromState
        String toState = a_grammar.toState

        if (!transitions[event])
            transitions[event] = [:]

        final Map transition = transitions[event]

// TODO: How to assert non-duplications now that they are conditioned?
//        assert cond == null && !transition[fromState], "Duplicate fromState ${fromState} for transition ${a_grammar}"

        Closure cond = a_grammar.cond != null ? a_grammar.cond : { true }
        Map conditionedStates = transition[fromState] != null ? transition[fromState] : [:]
        conditionedStates[toState] = cond
        transition[fromState] = conditionedStates
        this
    }

    FsmSupport addActionToTransition(a_grammar) {
        String fromState = a_grammar.fromState
        String toState = a_grammar.toState
        String key = "${a_grammar.event}-${fromState}"
        if (!actions[key]) {
            actions[key] = [:]
        }
        actions[key][toState] = a_grammar.action
        this

    }

    FsmSupport reset() {
        currentState = initialState
        this
    }

    String fire(String a_event) {
        assert currentState, "Invalid current state '${currentState}': pass into constructor"
        assert transitions.containsKey(a_event), "Invalid event '${a_event}', should be one of ${transitions.keySet()}"

        String fromState = currentState
        String nextState

        Map transition = transitions[a_event]

        transition[currentState].each { String to, Closure cond ->
            cond.setDelegate(target)
            if (!nextState && cond()) {
                nextState = to
                // Notify the object if it's interested in the transition event
                if (target.metaClass.respondsTo(target, "onFsmTransition"))
                    target.onFsmTransition(fromState, to)
            }
        }
        assert nextState, "There is no transition from '${currentState}' to any other state. \nFSM: ${this.transitions}"
        currentState = nextState

        // Update the target property of the target object
        target[property] = currentState

        // Update the object if contains "save"
        if (target.metaClass.respondsTo(target, "save")) {
            target.save()
        }

        // Trigger the action defined if any
        String key = "${a_event}-${fromState}"
        if (actions[key] && actions[key][currentState]) {
            Closure act = actions[key][currentState]
            act.setDelegate(target)
            act()
        }

        currentState
    }

    Boolean isFireable(a_event) {
        def transition = transitions[a_event]
        transition[currentState].any { to, Closure cond ->
            cond.setDelegate(target)
            cond()
        }
    }

    Boolean isState(a_state) {
        currentState == a_state
    }
}



