import grails.test.*;
import grails.plugin.fsm.*;

class FsmSupportUnitTests extends GrailsUnitTestCase {
    static transactional = false

    protected void setUp() {
    	FsmUtils.mockFsm(FsmSupportDummy)
        super.setUp()
    }

    protected void tearDown() {
    	FsmSupportDummy.metaClass = null
        super.tearDown()
    }

    void testBasicFsm() {
        def test = new FsmSupportDummy()
        assert test.mood == 'none'
    }

    void testErrors() {
        def samp1 = new FsmSupportDummy()
        try {
            samp1.fire('nonflow', 'launch')
        } catch (Exception e) {
            assert e.message == "Can't fire on flow 'nonflow' which is not defined in 'class FsmSupportDummy'"
        }
        try {
            samp1.fire('mood', 'nonevent')
        } catch (AssertionError e) {
            assert e.message.contains("Invalid event 'nonevent'")
        }
        
    }
    
    void testComplexFsm() {
        // Take a look at SampleDomainClass to understand what's supposed to happen here
        def samp1 = new FsmSupportDummy()

        //assert !samp1.beenSaved     // Not saved initially

        assert 'initial' == samp1.fire('status', 'launch') // Amount == 0 => Won't launch

        //assert samp1.beenSaved  // Status has changed, 'save()' SHOULD have been called

        samp1.amount = 3  // Change amount so it will allow starting

            assert 'none' == samp1.fire_mood('up')  // Not running, Mood will not change!!
            assert 'none' == samp1.fire('mood', 'up')  // Not running, Mood will not change!!
            assert 'none' == samp1.fire('mood', 'up')  // Not running, Mood will not change!!

        assert 'running' == samp1.fire('status', 'launch')  // TODO: Delegate method call!!

            assert 'high' == samp1.fire('mood', 'up')  // status==running, so let's go high!!

        assert 'stopped' == samp1.fire('status', 'stop')
        assert 'stopped' != samp1.fire('status', 'continue')
        assert 'running' == samp1.status  // Property delegeated in the domain class!

        assert 'high' == samp1.fire('mood', 'up')
        assert 'low' == samp1.fire('mood', 'down')

        def samp2 = new FsmSupportDummy()
        assert 'none' == samp2.fire('mood', 'down')
        assert 'none' == samp2.fire('mood', 'up')
            samp2.amount = 1
            samp2.fire('status', 'launch')
        assert 'running' == samp2.status
        assert 'low' == samp2.fire('mood', 'down')
        assert 'high' == samp2.fire('mood', 'up')
        assert 'high' == samp2.fire('mood', 'up')
        assert 'low' == samp2.fire('mood', 'down')
    }

/* This one fails telling me that there are no transitions from a 'loaded' state, although I explicitly set the state to be 'validated' instead of 'loaded' */
    void testFireEventsFromExistingStateWithErrors() {
        FsmSupportDummy foo = new FsmSupportDummy()
        foo.mood = 'high'
        foo.status = 'running'
        println("MOOD: ${foo.mood}")
        foo.fire('mood', 'down')
        println("MOOD: ${foo.mood}")
        assert foo.mood == 'low'
    }

    void testDifferentEventsThrowDifferentWorkflows() {
        FsmSupportDummy foo = new FsmSupportDummy()
        foo.status = 'stopped'
        foo.fire('status', 'hello')
        assert foo.cheers == 'helloooou'
        foo.fire('status', 'goodbye')
        assert foo.cheers == 'goodbyeee'
    }

    void testIsFireable() {
        FsmSupportDummy foo = new FsmSupportDummy()
        foo.status = 'stopped'
        ['continue', 'finish', 'hello', 'goodbye'].each {
            assert foo.fireable('status', it) == true
        }
        ['launch', 'stop'].each {
            assert foo.fireable('status', it) == false
        }
    }
}

