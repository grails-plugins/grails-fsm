package testapp

import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
@Rollback
class FsmSupportIntegrationTests extends Specification {

    void testInitialValues() {
        when:
        def testa = new FsmSupportDummy()

        then:
        testa.status == 'initial'
        testa.mood == testa.getMood()
        testa.mood == 'none'
    }

    void testErrors() {
        when:
        def samp1 = new FsmSupportDummy()
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
        def samp1 = new FsmSupportDummy()
        String currentState

        when:
        samp1.fire('status', 'launch')

        then: "Amount == 0 => Won't launch"
        'initial' == samp1.status

        when:
        samp1.amount = 3  // Change amount so it will allow starting

        then: "Not running, Mood will not change!!"
        'none' == samp1.fire_mood('up')
        'none' == samp1.fire('mood', 'up')
        'none' == samp1.fire('mood', 'up')

        when:
        samp1.fire('status', 'launch')

        then:
        'running' == samp1.status

        when:
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
        'low' == samp2.fire('mood', 'down')
        'high' == samp2.fire('mood', 'up')
        'high' == samp2.fire('mood', 'up')
        'low' == samp2.fire('mood', 'down')
    }

    void testReloadedStates() {
        when:
        def testFoo = new Foo2()
        testFoo.estadoEnvio = 'envioFinal'
        testFoo.save()

        then: "(PRE) Debemos estar en estado inicial, envio envioFinal"
        "inicial" == testFoo.estado
        "envioFinal" == testFoo.estadoEnvio

        when:
        testFoo.fire('estado', 'comando')

        then: "(POST) Debemos estar en estado final"
        "final" == testFoo.estado
    }

}
