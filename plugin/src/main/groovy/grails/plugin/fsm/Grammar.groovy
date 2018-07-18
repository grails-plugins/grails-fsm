package grails.plugin.fsm

class Grammar {
    FsmSupport fsm

    String event
    String fromState
    String toState
    Closure cond
    Closure action
    Boolean conditionable  // 'from' != null && 'to' == null

    Grammar(a_fsm) {
        fsm = a_fsm
    }

    Grammar on(String a_event) {
        event = a_event
        toState = fromState = null
        this
    }

    Grammar on(String a_event, Closure a_transitioner) {
        on(a_event)
        a_transitioner.delegate = this
        a_transitioner.call()
        this
    }

    Grammar from(a_fromState) {
        conditionable = true
        cond = null
        fromState = a_fromState
        this
    }

    Grammar to(a_toState) {
        assert a_toState, "Invalid toState: ${a_toState}"

        conditionable = false
        toState = a_toState
        fsm.registerTransition(this)
        this
    }

    Grammar crash() {
        // TODO: Will allow to explicitely crash in a forbidden event!
        // This would imply changing default behaviour so non-declared events
        // not fail and just keep currentState.
        // TODO: Think about it...
        this
    }

    /*
     * *Must* be called after 'from' and before 'to'
     */

    Grammar when(cond_closure) {
        assert conditionable, "'when' must be called after a 'from' and before 'to'"
        cond = cond_closure
        this
    }

    Grammar act(do_closure) {
        assert toState, "'act' can only be called after a 'to' has been defined"
        action = do_closure
        fsm.addActionToTransition(this)
        this
    }

    Boolean isValid() {
        event && fromState && toState
    }

    String toString() {
        "${event}:${fromState}=>${toState}"
    }
}