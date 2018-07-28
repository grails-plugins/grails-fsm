package testapp

import static testapp.Estado.*
import static testapp.EstadoEnvio.*

class Foo2 {
    Estado estado = INICIAL
    EstadoEnvio estadoEnvio = ENVIO_INICIAL

    static fsm_def = [
            estado     : [
                    (INICIAL): { flow ->
                        flow.on(COMANDO) {
                            from(INICIAL).to(FINAL)
                        }
                    }
            ],

            estadoEnvio: [
                    (ENVIO_INICIAL): { flow ->
                        flow.on(COMANDO_ENVIO) {
                            from(ENVIO_INICIAL).to(ENVIO_FINAL)
                        }
                    }
            ]
    ]
}
