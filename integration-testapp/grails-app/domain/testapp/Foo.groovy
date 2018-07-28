package testapp

import static testapp.Event.*
import static testapp.Status.*

class Foo {

    Status status

    static fsm_def = [
            status: [
                    (LOADED): { flow ->
                        flow.on(EV_1) {
                            from(LOADED).when({ hasAttrErrors() }).to(IN_ERROR)
                            from(LOADED).when({ !hasAttrErrors() }).to(VALIDATED)
                        }
                        flow.on(EV_2) {
                            from(IN_ERROR).when({ !hasAttrErrors() }).to(VALIDATED)
                        }
                        flow.on(EV_3) {
                            from(VALIDATED).when({ !hasAttrErrors() }).to(DONE)
                        }
                    }
            ]]
}
