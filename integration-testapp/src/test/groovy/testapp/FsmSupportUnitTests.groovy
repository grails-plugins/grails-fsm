package testapp

import grails.plugin.fsm.FsmUtils
import grails.testing.gorm.DataTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import static testapp.Mood.*
import static testapp.Status.*

@ConfineMetaClassChanges(FsmSupportDummy)
class FsmSupportUnitTests extends Specification implements DataTest {

    void setupSpec() {
        mockDomains(FsmSupportDummy)
    }

    void setup() {
        FsmUtils.mockFsm(FsmSupportDummy)
    }

    void cleanup() {
        FsmSupportDummy.metaClass = null
    }

    void testBasicFsm() {
        expect:
        new FsmSupportDummy().mood == NONE
    }

    void testErrors() {
        given:
        def samp1 = new FsmSupportDummy()

        when:
        samp1.fire('nonflow', LAUNCH)

        then:
        Exception e = thrown()
        e.message == "Can't fire on flow 'nonflow' which is not defined in 'class testapp.FsmSupportDummy'"


        when:
        samp1.fire('mood', NON_EVENT)

        then:
        AssertionError error = thrown()
        error.message.contains("Invalid event 'NON_EVENT'")
    }

    void testComplexFsm() {
        given:
        Enum currentState

        when:
        def samp1 = new FsmSupportDummy()

        then:
        INITIAL == samp1.fire('status', LAUNCH) // Amount == 0 => Won't launch

        when: "Change amount so it will allow starting"
        samp1.amount = 3

        then: "Not running, Mood will not change!!"
        NONE == samp1.fire_mood(UP)
        NONE == samp1.fire('mood', UP)
        NONE == samp1.fire('mood', UP)

        when:
        samp1.fire('status', LAUNCH)

        then:
        RUNNING == samp1.status

        when: "go high!!"
        currentState = samp1.fire('mood', UP)

        then:
        HIGH == currentState

        when:
        samp1.fire('status', STOPPED)

        then:
        STOPPED == samp1.status

        when:
        samp1.fire('status', CONTINUE)

        then:
        STOPPED != samp1.status
        RUNNING == samp1.status  // Property delegeated in the domain class!

        when:
        currentState = samp1.fire('mood', UP)

        then:
        HIGH == currentState

        when:
        currentState = samp1.fire('mood', DOWN)

        then:
        LOW == currentState

        when:
        FsmSupportDummy samp2 = new FsmSupportDummy()

        then:
        NONE == samp2.fire('mood', DOWN)
        NONE == samp2.fire('mood', UP)

        when:
        samp2.amount = 1
        samp2.fire('status', LAUNCH)

        then:
        RUNNING == samp2.status

        when:
        currentState = samp2.fire('mood', DOWN)

        then:
        LOW == currentState

        when:
        currentState = samp2.fire('mood', UP)

        then:
        HIGH == currentState

        when:
        currentState = samp2.fire('mood', UP)

        then:
        HIGH == currentState

        when:
        currentState = samp2.fire('mood', DOWN)

        then:
        LOW == currentState
    }

/* This one fails telling me that there are no transitions from a 'loaded' state, although I explicitly set the state to be 'validated' instead of 'loaded' */

    void testFireEventsFromExistingStateWithErrors() {
        when:
        FsmSupportDummy foo = new FsmSupportDummy()
        foo.mood = HIGH
        foo.status = RUNNING
        foo.fire('mood', DOWN)

        then:
        foo.mood == LOW
    }

    void testDifferentEventsThrowDifferentWorkflows() {
        when:
        FsmSupportDummy foo = new FsmSupportDummy()
        foo.status = STOPPED
        foo.fire('status', HELLO)

        then:
        foo.cheers == 'helloooou'

        when:
        foo.fire('status', GOOD_BYE)

        then:
        foo.cheers == 'goodbyeee'
    }

    void testIsFireable() {
        when:
        FsmSupportDummy foo = new FsmSupportDummy()
        foo.status = STOPPED

        then:
        [CONTINUE, FINIHED, HELLO, GOOD_BYE].each {
            foo.fireable('status', it) == true
        }

        [LAUNCH, STOPPED].each {
            foo.fireable('status', it) == false
        }
    }
}

