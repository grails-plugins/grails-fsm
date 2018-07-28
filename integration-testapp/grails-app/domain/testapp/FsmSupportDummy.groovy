package testapp

import static testapp.Mood.*
import static testapp.Status.*

/**
 * Domain class to held some test of the FsmSupport
 *
 */
class FsmSupportDummy {

    String name
    Status status = INITIAL
    Mood mood = ToBeOverridenInFsmDef
    String cheers

    def amount  // A number > 0 will allow the status flow to start

    static fsm_def = [
            mood  : [
                    (NONE): { flow ->
                        flow.on(UP) {
                            from(NONE).when({
                                status == RUNNING  // Depends on second flow!!
                            }).to(HIGH)
                            from(LOW).to(HIGH).act({
                                changeName(name.toUpperCase())
                            })
                            from(HIGH).to(HIGH)
                            from(NONE).to(NONE) // Order is CRITICAL!!
                        }
                        flow.on(DOWN) {
                            from(NONE).when({
                                status == RUNNING
                            }).to(LOW)
                            from(LOW).to(HIGH)
                            from(HIGH).to(LOW).act({
                                changeName(name.toLowerCase())
                            })
                            from(NONE).to(NONE) // Order is CRITICAL!!
                        }

                    }
            ],
            status: [
                    (INITIAL): { flow ->
                        flow.on(LAUNCH) {
                            from(INITIAL).when({
                                isSomethingPending() && amount > 0
                            }).to(RUNNING)
                            from(INITIAL).to(INITIAL)
                        }
                        flow.on(STOPPED) {
                            from(RUNNING).to(STOPPED)
                        }
                        flow.on(CONTINUE) {
                            from(STOPPED).to(RUNNING)
                        }
                        flow.on(FINIHED) {
                            from(STOPPED).to(FINIHED)
                            from(RUNNING).to(FINIHED)
                        }
                        flow.on(HELLO) {
                            from(STOPPED).to(STOPPED).act({
                                cheers = 'helloooou'
                            })
                        }
                        flow.on(GOOD_BYE) {
                            from(STOPPED).to(STOPPED).act({
                                cheers = 'goodbyeee'
                            })
                        }
                    }
            ]
    ]

    /*
     * Sample method to call from conditions!!
     */

    Boolean isSomethingPending() {
        return true
    }

    String changeName(String n) {
        name = n
    }

}
