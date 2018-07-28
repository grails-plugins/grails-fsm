package testapp

import static testapp.Status.*

/**
 * Domain class to held some test of the Fsm plugin
 * This testcase adapted from Aleksandar Kochnev's propsed to cover a bug in
 * actions attached to transitions. 
 */
class FsmMultipleActions {

    Status status = LOADED
    Boolean hasErrors
    Boolean action1Called = false
    Boolean action2Called = false

    static fsm_def = [
            status: [
                    (LOADED): { flow ->
                        flow.on(VALIDATED) {
                            from(LOADED).when({ hasErrors }).to(IN_ERROR).act({
                                setAction(1)
                            })
                            from(LOADED).when({ !hasErrors }).to(VALIDATED).act({
                                setAction(2)
                            })
                        }
                    }
            ]
    ]

    def setAction(n) {
        this."action${n}Called" = true
    }

    FsmMultipleActions() {

    }
}
