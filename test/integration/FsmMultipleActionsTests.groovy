/**
 * Cases for several actions attached to different transitions from
 * same state with different when conditions attached.
 * 
 */
class FsmMultipleActionsTests extends GroovyTestCase {
    static transactional = false
    void testFirstAction() {
        def foo= new FsmMultipleActions()
        foo.hasErrors = false
        assert !foo.action1Called
        assert !foo.action2Called
        assert foo.status =='loaded'        

        foo.fire_status('validate')
        assert foo.status == 'validated'
        assert !foo.action1Called
        assert foo.action2Called

    }

    void testSecondAction() {
    	def foo = new FsmMultipleActions()
    	foo.hasErrors = true
    	assert !foo.action1Called
    	assert !foo.action2Called
    	assert foo.status == 'loaded'

    	foo.fire_status('validate')
    	assert foo.status == 'in_error'
    	assert foo.action1Called
    	assert !foo.action2Called
    		
    }

    void testAnyFromTransition() {
    	
    }
}