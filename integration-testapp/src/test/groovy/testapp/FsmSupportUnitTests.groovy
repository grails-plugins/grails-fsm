package testapp

import grails.plugin.fsm.FsmUtils
import grails.testing.gorm.DataTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

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
        new FsmSupportDummy().mood == 'none'
    }

    void testErrors() {
        given:
        def samp1 = new FsmSupportDummy()

        when:
        samp1.fire('nonflow', 'launch')

        then:
        Exception e = thrown()
        e.message == "Can't fire on flow 'nonflow' which is not defined in 'class testapp.FsmSupportDummy'"


        when:
        samp1.fire('mood', 'nonevent')

        then:
        AssertionError error = thrown()
        error.message.contains("Invalid event 'nonevent'")
    }

    void testComplexFsm() {
        given:
        String currentState

        when:
        def samp1 = new FsmSupportDummy()

        then:
        'initial' == samp1.fire('status', 'launch') // Amount == 0 => Won't launch

        when: "Change amount so it will allow starting"
        samp1.amount = 3

        then: "Not running, Mood will not change!!"
        'none' == samp1.fire_mood('up')
        'none' == samp1.fire('mood', 'up')
        'none' == samp1.fire('mood', 'up')

        when:
        samp1.fire('status', 'launch')

        then:
        'running' == samp1.status

        when: "go high!!"
        currentState = samp1.fire('mood', 'up')

        then:
        'high' == currentState

        when:
        samp1.fire('status', 'stop')

        then:
        'stopped' == samp1.status

        when:
        samp1.fire('status', 'continue')

        then:
        'stopped' != samp1.status
        'running' == samp1.status  // Property delegeated in the domain class!

        when:
        currentState = samp1.fire('mood', 'up')

        then:
        'high' == currentState

        when:
        currentState = samp1.fire('mood', 'down')

        then:
        'low' == currentState

        when:
        def samp2 = new FsmSupportDummy()

        then:
        'none' == samp2.fire('mood', 'down')
        'none' == samp2.fire('mood', 'up')

        when:
        samp2.amount = 1
        samp2.fire('status', 'launch')

        then:
        'running' == samp2.status

        when:
        currentState = samp2.fire('mood', 'down')

        then:
        'low' == currentState

        when:
        currentState = samp2.fire('mood', 'up')

        then:
        'high' == currentState

        when:
        currentState = samp2.fire('mood', 'up')

        then:
        'high' == currentState

        when:
        currentState = samp2.fire('mood', 'down')

        then:
        'low' == currentState
    }

/* This one fails telling me that there are no transitions from a 'loaded' state, although I explicitly set the state to be 'validated' instead of 'loaded' */

    void testFireEventsFromExistingStateWithErrors() {
        when:
        FsmSupportDummy foo = new FsmSupportDummy()
        foo.mood = 'high'
        foo.status = 'running'
        foo.fire('mood', 'down')

        then:
        foo.mood == 'low'
    }

    void testDifferentEventsThrowDifferentWorkflows() {
        when:
        FsmSupportDummy foo = new FsmSupportDummy()
        foo.status = 'stopped'
        foo.fire('status', 'hello')

        then:
        foo.cheers == 'helloooou'

        when:
        foo.fire('status', 'goodbye')

        then:
        foo.cheers == 'goodbyeee'
    }

    void testIsFireable() {
        when:
        FsmSupportDummy foo = new FsmSupportDummy()
        foo.status = 'stopped'

        then:
        ['continue', 'finish', 'hello', 'goodbye'].each {
            foo.fireable('status', it) == true
        }

        ['launch', 'stop'].each {
            foo.fireable('status', it) == false
        }
    }
}

