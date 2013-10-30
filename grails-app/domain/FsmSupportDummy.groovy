/**
 * Domain class to held some test of the FsmSupport
 *
 */
class FsmSupportDummy {

    def name
    def status = 'initiala'
    String mood = 'ToBeOverridenInFsmDef'
    def cheers

    def amount  // A number > 0 will allow the status flow to start

    static belongsTo = [
                        ]

    static constraints = {
        status()
        mood()
    }

    static fsm_def = [
        mood : [
            none : { flow ->
                flow.on ('up') {
                    from('none').when({
                            status == 'running'  // Depends on second flow!!
                        }).to('high')
                    from('low').to('high').act({
                    	changeName(name.toUpperCase())
                    })
                    from('high').to('high')
                    from('none').to('none') // Order is CRITICAL!!
                }
                flow.on('down') {
                    from('none').when({
                            status == 'running'
                        }).to('low')
                    from('low').to('low')
                    from('high').to('low').act({
                    	changeName(name.toLowerCase())
                    })
                    from('none').to('none') // Order is CRITICAL!!
                }

            }
        ],
        status : [
            initial : { flow ->
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
                flow.on('hello') {
                    from('stopped').to('stopped').act({
                        cheers = 'helloooou'
                    })
                }
                flow.on('goodbye') {
                    from('stopped').to('stopped').act({
                        cheers = 'goodbyeee'
                    })
                }
            }
        ]
    ]

    /*
     * Sample method to call from conditions!!
     */
    def isSomethingPending() {
        return true
    }

    def changeName(n) {
    	name = n
    }

}
