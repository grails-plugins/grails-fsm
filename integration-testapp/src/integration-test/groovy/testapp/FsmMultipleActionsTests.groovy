package testapp

import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

import static testapp.Status.*

/**
 * Cases for several actions attached to different transitions from
 * same state with different when conditions attached.
 *
 */
@Integration
@Rollback
class FsmMultipleActionsTests extends Specification {

    void testFirstAction() {
        when:
        FsmMultipleActions foo = new FsmMultipleActions()
        foo.hasErrors = false

        then:
        !foo.action1Called
        !foo.action2Called
        foo.status == LOADED

        when:
        foo.fire_status(VALIDATED)

        then:
        foo.status == VALIDATED
        !foo.action1Called
        foo.action2Called

    }

    void testSecondAction() {
        when:
        FsmMultipleActions foo = new FsmMultipleActions()
        foo.hasErrors = true

        then:
        !foo.action1Called
        !foo.action2Called
        foo.status == LOADED

        when:
        foo.fire_status(VALIDATED)

        then:
        foo.status == IN_ERROR
        foo.action1Called
        !foo.action2Called

    }
}