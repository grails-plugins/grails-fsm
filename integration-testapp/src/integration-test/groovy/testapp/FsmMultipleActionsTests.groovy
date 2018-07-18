package testapp

import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

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
        def foo = new FsmMultipleActions()
        foo.hasErrors = false

        then:
        !foo.action1Called
        !foo.action2Called
        foo.status == 'loaded'

        when:
        foo.fire_status('validate')

        then:
        foo.status == 'validated'
        !foo.action1Called
        foo.action2Called

    }

    void testSecondAction() {
        when:
        def foo = new FsmMultipleActions()
        foo.hasErrors = true

        then:
        !foo.action1Called
        !foo.action2Called
        foo.status == 'loaded'

        when:
        foo.fire_status('validate')

        then:
        foo.status == 'in_error'
        foo.action1Called
        !foo.action2Called

    }
}