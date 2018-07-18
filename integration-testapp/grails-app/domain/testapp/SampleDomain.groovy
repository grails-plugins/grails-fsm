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

/**
 * The Domain class to held some test of the FsmSupport
 *
 * @author puneetbehl
 * @since 1.0.0
 */

// tag::example1[]
class SampleDomain {

    String name
    String status

    /**
     * fsm_def will override this value with 'none' in this example
     * and a Warning will be traced.
     */
    String mood = "ignored"

    /**
     * A number > 0 will allow the status flow to start
     */
    Integer amount
    // end::example1[]

    // tag::example1Fsm[]
    static fsm_def = [
            mood  : [ // <1>
                      none: { flow ->
                          flow.on('up') {
                              from('none').when({
                                  status == 'running'  // Depends on second flow !
                              }).to('high')
                              from('low').to('high').act({
                                  doSomethingInteresting()
                              })
                              from('high').to('high')
                              from('none').to('none') // Order is CRITICAL !
                          }
                          flow.on('down') {
                              from('none').when({
                                  status == 'running'
                              }).to('low')
                              from('low').to('low')
                              from('high').to('low')
                              from('none').to('none') // Order is CRITICAL !
                          }
                      }
            ],
            status: [ // <2>
                      initial: { flow ->
                          flow.on('launch') {
                              from('initial').when({
                                  isSomethingPending() && amount > 0
                              }).to('running')
                              from('initial').to('initial')
                          }
                          flow.on('stop') {
                              from('running').to('stopped')
                          }
                          flow.on('continue') {
                              from('stopped').to('running')
                          }
                          flow.on('finish') {
                              from('stopped').to('finished')
                              from('running').to('finished')
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