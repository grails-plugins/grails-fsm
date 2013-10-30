/**
 * Domain class to held some test of the Fsm plugin
 * This testcase adapted from Aleksandar Kochnev's propsed to cover a bug in
 * actions attached to transitions. 
 */
class FsmMultipleActions {

    def status = 'loaded'
    def hasErrors
    def action1Called = false
    def action2Called = false

    static fsm_def = [
	    status : [
	               loaded : { flow ->
	                   flow.on ('validate') {                    
	                       from('loaded').when({hasErrors}).to('in_error').act({
	                    	   setAction(1)
	                       })
	                       from('loaded').when({!hasErrors}).to('validated').act({
	                    	   setAction(2)
	                       })
	                   }
	               }
	          ]
    ]
    def setAction(n) {
    	this."action${n}Called" = true
    }

    def FsmMultipleActions() {
    	
    }
}
