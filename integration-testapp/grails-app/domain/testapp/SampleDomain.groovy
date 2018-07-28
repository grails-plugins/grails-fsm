/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package testapp

import static testapp.Mood.*
import static testapp.Status.*

/**
 * The Domain class to held some test of the FsmSupport
 *
 * @author puneetbehl
 * @since 1.0.0
 */

// tag::example1[]
class SampleDomain {

    String name
    Status status

    /**
     * fsm_def will override this value with 'none' in this example
     * and a Warning will be traced.
     */
    Mood mood = IGNORED

    /**
     * A number > 0 will allow the status flow to start
     */
    Integer amount
    // end::example1[]

    // tag::example1Fsm[]
    static fsm_def = [
            mood  : [ // <1>
                      (NONE): { flow ->
                          flow.on(UP) {
                              from(NONE).when({
                                  status == RUNNING  // Depends on second flow !
                              }).to(HIGH)
                              from(LOW).to(HIGH).act({
                                  doSomethingInteresting()
                              })
                              from(HIGH).to(HIGH)
                              from(NONE).to(NONE) // Order is CRITICAL !
                          }
                          flow.on(DOWN) {
                              from(NONE).when({
                                  status == RUNNING
                              }).to(LOW)
                              from(LOW).to(LOW)
                              from(HIGH).to(LOW)
                              from(NONE).to(NONE) // Order is CRITICAL !
                          }
                      }
            ],
            status: [ // <2>
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
                      }
            ]
    ]
    // end::example1Fsm[]

    // tag::example1[]
    /**
     * Sample method to call from conditions!
     */
    Boolean isSomethingPending() {
        return true
    }

    void doSomethingInteresting() {

    }

}
// end::example1[]