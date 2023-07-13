# PRÁCTICA JUEGO
> Ana Jiménez Navarrete

> Miguel Ángel López Reyes

El juego a desarrollar es una modificación del Mouse Run. Se utilizará la ontología que se describe en el fichero [README_ONTOLOGIA](README_ONTOLOGIA.md). Para el diseño del juego se tendrán en cuenta los siguientes agentes especializados con sus tareas principales.
***
## AgenteMonitor

Este agente se encargará de coordinar la realización de todos los juegos posibles y la presentación de los resultados de esos juegos. Además será el encargado de crear en la plataforma algunos agentes especializados. Para ello deberá recibir como parámetro un archivo de configuración que tendrá que tener las siguientes secciones:

- Una lista de AgenteRaton y AgenteLaberinto que permiten demostrar el funcionamiento del juego. La línea que representa un agente es la siguiente: `nombre_agente:localizacion_clase_agente`
- Una lista de juegos que deberán completarse. Cada juego será detallado en una línea compuesta por los parámetros necesarios para su comienzo:  `DificultadLaberinto ModoJuego DificultadJuego`

Sus principales tareas se describen a continuación.

**1. Localizar a `AgenteRaton` y `AgenteLaberinto`**

Esta tarea será resuelta con el servicio de páginas amarillas proporcionado por la plataforma de agentes JADE. El `AgenteMonitor` estará subscrito en las páginas amarillas y cuando algún agente se regisre o abandone del servicico, el `AgenteMonitor` será notificado. Respectivamente, este almacenará el `AID` del agente, siempre y cuando su servicio sea el buscado. O bien, lo eliminará de sus registros en el caso del abandono de la plataforma por parte de algún agente. Los servicios disponibles son:
- `JUGADOR`: correspondiente a los AgenteRaton que jugarán las partidas.
- `ORGANIZADOR`: representan los AgenteLaberinto que organizarán las partidas.

Su implementación constará de una tarea llamada `TareaSubscripcionDF` que hereda de la clase `DFSubcribe`. Además, serán implementados los métodos abstractos y constructor requeridos. Será responsabilidad del `AgenteMonitor` almacenar y/o eliminar un agente de sus registros internos. Por lo cual se hará uso de una interfaz cuyos métodos, `addAgent(.) y removeAgent(.)` ,serán sobrecargados por el `AgenteMonitor`.
```
interface SubscripcionDF {
    addAgent(AID agente, NombreServicio servicio);
    removeAgent(AID agente, NombreServicio servicio);
}
...
class TareaSubscripcionDF extends DFSubscriber {
    SubscripcionDF agente

    void onRegister(.) {
        ...
        agente.addAgent( aid, servicio)
        ...
    }
    void onDeregister(.) {
        ...
        agente.removeAgent( aid )
        ...
    }
}
```

**2. Organizar los jugadores que formará parte de un juego**

En primer lugar, debemos de saber el tipo de juego en cuestión para así determinar el número de jugadores implicados. En nuestro caso un juego tiene asociado un `ModoJuego`, `DificultadJuego` y `DificultadLaberinto`, lo cual implica que el juego puede ser:
- `ModoJuego`
    - `BUSQUEDA`: el objetivo es conseguir los quesos
    - `TORNEO`: todos los ratones se enfrentan para conseguir el mayor número de quesos en las partidas que componen el juego.
    - `ELIMINATORIA`: no todos los ratones compiten en todas las partidas. A medida que los ratones ganan las partidas siguen adelante en la competición, si pierden son eliminados. En la última partida se enfrentan los dos mejores ratones para decidir el ganador.
- `DificultadJuego`: determina el número de ratones que componen cada juego.
    - `BUSQUEDA` : para el caso especial donde solo participa un ratón.
    - `FACIL` : entre 2 y 4.
    - `NORMAL` : entre 4 y 8.
    - `DIFICIL` : entre 8 y 12.
- `DificultadLaberinto`: mantiene unos intervalos para determinar el número de quesos y dimensiones del laberinto.
    - `FACIL` : entre 1 y 3 quesos y entre 5 y 10 unidades del laberinto.
    - `NORMAL` : entre 2 y 4 quesos y entre 10 y 20 unidades del laberinto.
    - `DIFICIL` : entre 4 y 8 quesos y entre 30 y 40 unidades del laberinto.

La información anterior la tenemos que tener en cuenta a la hora de recopilar el número de jugadores. Cuando tengamos los jugadores deseados disponibles, podremos comenzar a organizar el juego. En definitiva, la comunicación con los `AgenteRaton` se realizará a través de un protocolo de tipo *Propose*. Se ha seleccionado este protocolo porque el `AgenteRaton` puede tomar la decisión de aceptar o no jugar el juego atendiendo a su estado actual. El diagrama que muestra dicha comunicación es [ProponerJuego](./img/ProponerJuego.png). Este protocolo contiene dos tareas para la parte del iniciador y otra para el participante:
- `TareaProponerJuegoIniciador`: tarea que se encarga de recoger las respuesta a la propuesta por parte del participante. Si la respuesta es positiva se guarda el jugador e incrementa un contados.
    ```
    class TareaProponerJuegoIniciador extends ProposeInitiator{
        void handleAcceptProposal(ACLMessage accept){
            print("El raton "+accept.getSender()+" ha aceptado el juego");
            agente.incrementarNumJugadores(accept.getSender());
        }
    
        void handleRejectProposal(ACLMessage reject){
            print("El raton "+reject.getSender()+" rechazado el juego por "+reject.getContent());
        }
    }
    ```
 - `TareaProponerJuegoParticipante`: la decisión de aceptar o rechazar la propuesta recaer en el `AgenteRaton`. Además, si la ontologia no la entiene se manda un mensaje de error.
   ```
    class TareaProponerJuegoParticipante extends ProposeResponder{
        ACLMessage prepareResponse(ACLMessage propose){
            ACLMessage msg = propose.createReply();
            Justificacion justf;
            IncidenciaJuego incidencia;
            JuegoAceptado aceptado;

            if ( ontologiaDesconocida() )
                msg.setPermormative( NOT_UNDERSTOOD )
                justf.setDetalle(ONTOLOGIA_DESCONOCIDA);
            else
                juego = extraerPropuesta( propose );
                if ( agente.puedeAceptar( juego ) )
                    msg.setPermormative( ACCEPT_PROPOSAL )
                    aceptado.setjuego( juego )
                    aceptado.setAgenteJuego( jugador )
                else
                    msg.setPermormative( REJECT_PROPOSAL )
                    justf.setJuego( juego );
                    justf.setDetalle( motivo );   
            if ( incidencia )
                msg.setPermormative( NOT_UNDERSTOOD )
                incidencia.setDetalle(ERROR_CONTENIDO_MENSAJE);
           
            if (msg.getPerformative() == ACCEPT_PROPOSAL)
                manager.fillContent(msg, aceptado);
            else if (msg.getPerformative() == REJECT_PROPOSAL)
                manager.fillContent(msg, justf);
            else if ( justf != null )
                manager.fillContent(msg, justf);       
            else 
                manager.fillContent(msg, incidencia); 
            return msg;              
        }
    }
    ```

**3. Localizar a un `AgenteLaberinto` que complete la realización de un juego**

Cuando el `AgenteMonitor` ha localizado jugadores que están dispuestos a participar en un juego hay que localizar a un `AgenteLaberinto` que se encargue de generar las partidas necesarias para completar ese juego. El esquema de comunicación es  [CompletarJuego](./img/CompletarJuego.png). También, se utiliza el protocolo *Propose* porque el `AgenteLaberinto` va a decidir si organizar o no el juego. Por lo tanto las tareas creadas para lograr dicha comunicación son:
- `TareaOrganizarJuegoIniciador`: responsable de manipular los mensajes de aceptación o rechazo de cada uno de los juegos propuestos a `AgenteLaberinto`.
    ```
    class TareaOrganizarJuegoIniciador extends ProposeInitiator{
        void handleAcceptProposal(ACLMessage accept){
            print("El laberinto "+accept.getSender()+" aceptado el juego");
            agente.addLaberinto( accept.getSender() );
            agente.iniciarSubscripcion( accept.getSender() )
        }
    
        void handleRejectProposal(ACLMessage reject){
            print("El laberinto "+reject.getSender()+" rechaza el juego por "+reject.getContent());
        }
    }
    ```
- `TareaOrganizarJuegoParticipante`: de nuevo, al igual que en la anterior tarea `TareaProponerJuegoParticipante`, esta tarea es responsable de decidir si aceptar o no el juego.
    ```
    class TareaOrganizarJuegoParticipante extends ProposeResponder{
        ACLMessage prepareResponse(ACLMessage propose){
            ACLMessage msg = propose.createReply();
            Justificacion justf;
            IncidenciaJuego incidencia;
            JuegoAceptado aceptado;

            if ( ontologiaDesconocida() )
                msg.setPermormative( NOT_UNDERSTOOD )
                justf.setDetalle(ONTOLOGIA_DESCONOCIDA);
            else
                juego = extraerPropuesta( propose );
                if ( agente.puedeOrganizar( juego ) )
                    msg.setPermormative( ACCEPT_PROPOSAL )
                    aceptado.setjuego( juego )
                    aceptado.setAgenteJuego( jugador )
                else
                    msg.setPermormative( REJECT_PROPOSAL )
                    justf.setJuego( juego );
                    justf.setDetalle( motivo );   
            if ( incidencia )
                msg.setPermormative( NOT_UNDERSTOOD )
                incidencia.setDetalle(ERROR_CONTENIDO_MENSAJE);

            //Enviar
            if (msg.getPerformative() == ACCEPT_PROPOSAL)
                manager.fillContent(msg, aceptado);
            else if (msg.getPerformative() == REJECT_PROPOSAL)
                manager.fillContent(msg, justf);
            else if ( justf != null )
                manager.fillContent(msg, justf);       
            else 
                manager.fillContent(msg, incidencia); 
            return msg;             
        }
    }
    ```

La tarea respondable de recopilar a jugadores será la llamada `TareaOrganizarJugadores`. Será una tarea ejecutada cada cierto intervalo de tiempo por el `AgenteMonitor`. Cuando tenemos juegos sin asignar, calculamos el número de jugadores que tenemos que buscar, determinado por la dificultad del mismo. Iniciamos el protocolo *Propose* a los ratones registrados y cuando estos nos responda positivamente aumentamos el contador que contabiliza el número de jugadores disponibles y guardamos su participación en un juego concreto. Todo ello realizado en el método `proponerJuego(.)`. Finalmente, cuando ya se tengan los agentes necesarios se lanza la organización de un juego al `AgenteLaberinto` para que de comienzo al juego, con el método llamado `lanzarLaberinto(.)`. Suponemos que hasta que no se encuentran los jugadores necesarios para completar un juego no se pasa al siguiente juego. Decir que se ha creado la clase auxiliar `DatosJuego` para almacenar la información recopilada del fichero de configuración acerca de cada juego.

```
class TareaOrganizarJugadores extends TickerBehaviour {
    DatosJuego juegoActual = null;
    int numJugadores = 0;
    int numJugadoresAceptados = 0;

    void onTick(){
        if ( juegos.isEmpty() )
            print("Finalizado la organización de todos los juegos)
            stop()

        if ( juegoActual!=null && juegoActual.laberinto!=null )
            juegoActual=null;

        if ( juegoActual == null && tengoJuegosSinAsignar() )
            juegoActual = juegos.get(0);
            numJugadores = rand( juegoActual.getDificultad().getMinimo(), juegoActual.getDificultad().getMaximo()  )
            numJugadoresAceptados = 0
        
        if ( juegoActual != null && numJugadoresAceptados < numJugadores)
            nuevoJugador = proponerJuego( ratones, numJugadoresAceptados )
            juegoActual.addAgent( nuevoJugador )

        if ( numJugadoresAceptados == numJugadores ) 
            lanzarLaberinto( juegoActual )
    }
}
```


**4. Recoger resultados de los juegos organizados y almacenar su resultado**

Para que el `AgenteMonitor` pueda recibir la información del resultado de los juegos que proponga a diferentes `AgenteLaberinto` será necesario el intercambio de mensajes mostrado en el siguiente diagrama: [InformarJuego](./img/InformarJuego.png). En esta ocasión se utiliza el protocolo *Susbcribe*, se mantienen los `AgenteMonitor` y `AgenteLaberinto` conectados con este protocolo. Siempre que el `AgenteLaberinto` tenga nueva información acerca de algún juego será notificado el `AgenteMonitor`.

La comunicación será iniciada cuando un `AgenteLaberinto` tenga asignado algún juego para organizar y, por supuesto, este no tenga aún un protocolo de tipo *Subscribe* con el `AgenteMonitor`. las dos tareas que comprenden este protocolo son:
- `TareaInformarJuegoIniciador`: encargada de recoger los mensajes que lleguen del participante, tales como la confirmación de la subcripción, el informe relacionado con los resultados de juegos o las posibles incidencias.
    ```
    class TareaInformarJuegoIniciador extends SubscriptionInitiator{

        void handleAllResponses(Vector responses){
            for( ACLMessage msg : responses)
                switch ( msg.getPerformative() ) 
                    case NOT_UNDERSTOOD:
                        print( "ontología incorrecta")
                    case REFUSE:
                        print( "no se ha creado la subscripción")
                    case FAILURE:
                        print( "error en la cancelación de la subscripción")
                    case AGREE:
                        print( "subscripción confirmada")
                    default: 
                        print( "mensaje desconocido" )
        }
        
        void handleInform(ACLMessage inform) {
            msg = extraerContenido( inform )
            if ( msg == Clasificacionjuego )
                agente.guardar( msg.getContent() )
                mostrar( msg.getContent() )
            else
                print( "Incidencia : " + msg.getContent() )
        }
    }
    ```
- `TareaInformarJuegoParticipante`: las posibles respuesta a una subscripción es su confirmación, rechazo (en nuestro problema no va a pasar) o informar. El tipo de informe puede ser la clasificación de un juego tras su finalización o la incidencia ocurrida que hace la finalización forzosa del juego. Además, la comunicación se termina cuando el `AgenteMonitor` finalice su ejecución, en ese caso se manda un mensaje de cancelación.
    ```
    class TareaInformarJuegoParticipante extends SubscriptionResponder {

        ACLMessage handleSubscription(ACLMessage subscription){
            ACLMessage msg = subscription.createReply();
            if ( !ontologiaConocida() ) 
                msg.setPerformative(NOT_UNDERSTOOD);
                incidencia.setJustificacion(ONTOLOGIA_DESCONOCIDA);
                manager.fillContent(msg, incidencia);
            else 
                informarResultado = extraerContenido( subscription );
                msg.setPerformative(AGREE);
                justif.setMotivo(SUBSCRIPCION_ACEPTADA);
                manager.fillContent(msg, justif);
            return msg;
        }

        ACLMessage handleCancel(ACLMessage cancel){  
            ACLMessage msg = cancel.createReply();
            msg.setPerformative(ACLMessage.INFORM);  
            IncidenciaJuego incidencia;
            incidencia.setJustificacion(Incidencia.SUBSCRIPCION_CANCELADA);
            try{
                mySubscriptionManager.deregister( suscripcion );
            }catch(Exception ex){
                msg.setPerformative(ACLMessage.FAILURE);
                incidencia.setJustificacion(Incidencia.ERROR_CANCELACION);
            }
            
            manager.fillContent(msg, incidencia);
            return msg;
        }
    }
    ```
Es responsablilidad del `AgenteMonitor` organizar las subscripciones. Esto se realizará en una tarea llamada `TareaIniciarSubscripciones`, básicamente se trata de formar el mensaje, el contenido del mismo y registrar la tarea para futuras cancelaciones. Siempre y cuando los laberintos tengan asignado algún juego.
```
class TareaIniciarSubscripcion extends TickerBehaviour{
    void onTick(){
        if ( !laberintos.isEmpty() && laberintos.get(iLab).juegoAsigando)
            crearMensaje()
            crearContenido()
            enviarMensaje()
            registrarTarea()
    }
}
```
***

## AgenteRaton
Es el agente que adopta el rol del jugador representado por un ratón. El jugador debe diseñarse para seguir correctamente las reglas del juego y será responsable de mantener un estado correcto del juego, es decir, no tiene permitido hacer trampas. Este agente actúa de forma autónoma, esto es, no requiere supervisión de un usuario humano. Sus tareas principales serán:

**1. Aceptar la realización en un juego que le proponga un AgenteMonitor. Al menos debe aceptar jugar 3 juegos simultáneos.**

Los juegos son recibidos del `AgenteMonitor` en la comunicación [ProponerJuego](./img/ProponerJuego.png). En la tarea número 2 del `AgenteMonitor` describo la tarea que corresponde al participante. En ella utilizo un método del `AgenteRaton` llamado `puedoAceptar( juego )` que decide si el `AgenteRaton` puede o no aceptar la propuesta.

```
boolean puedoAceptar( Juego juego ){
    if ( juegos.size()<MAX_JUEGOS )
        juegos.add( juego )
        return true
    return false    
}
```

**2. Realizar los turnos para completar los movimientos dentro de un laberinto que le sean solicitados para una partida perteneciente a uno de sus juegos activos. Debe tener en cuenta los dos modos de juego disponibles: competitivo o búsqueda.**

Para completar la partida hay que ir generando los turnos necesarios hasta obtener un ganador o declarar un empate. Esta comunicación se resuelve con un protocolo *CFP*, [PedirMovimiento](./img/PedirMovimiento.png), donde el `AgentePartida` es el responsable de organizar los turnos del juego. El método que voy detallar a continuación será llamado dentro de la tarea para el participante correspondiente a dicho protocolo. No siempre se va a devolver y nuevo movimiento sino puede pasar que el `AgenteRaton` abandone. Por ese motivo el valor del movimiento devuelto es nulo. 
```
MovimientoEntregado respuestaCFP( PedirMovimiento mov){
    movpartida = mov.getPartida()
    for( Partida p : partidas )
        if ( p.getIdPartida()==movPartida.getPartida() && p.getIdJuego()==movPartida.getIdJuego() )
            MovimientoEntregado nuevoMov;
            nuevoMov.setPartida( p.getPartida() )
            nuevaPos = calcularMovimiento( mov.getPosicion() )
            nuevoMov.setMovimiento( new Movimiento( Accion.MOVIMIENTO, new Posicion(nuevoPos.x, nuevoPos.y) ))
            
            return nuevoMov

    return null;
}
```

***
## AgenteLaberinto
Será el encargado de organizar las partidas, representadas por la resolución de un laberinto. Sus tareas principales son:  

### Variables
`int numJuegos`: Número de juegos simultáneos que se están jugando en un momento  
`ArrayList<OrganizarJuego> juegosAOrganizar`: Juegos esperando a ser inicializados  
`ArrayList<AID> agentesPartidas`: Lista de AgentesPartida que se han inicializado  
`ArrayList<ResultadosPartidas> partidas`: Lista de los resultados recibidos de las partidas.

**Aceptar la organización de un juego propuesto por un `AgenteMonitor`. Al menos debe aceptar organizar 3 juegos simultáneos.**  
Una vez que el `AgenteMonitor` ha encontrado los suficientes `AgenteRatón` para comenzar el juego, el `AgenteMonitor` busca un
`AgenteLaberinto` que pueda organizar los juegos. Todo esto se realiza en la comunicación [OrganizarJuego](./img/OrganizarJuego.png).  En la tarea 3 del `AgenteMonitor` está descrito la tarea que corresponde al `AgenteLaberinto`.   

```
boolean puedoOrganizar( OrganizarJuego juego ){
    if ( numJuegos < MAX_JUEGOS )
        juegosAOrganizar.add( juego )
        return true

    return false    
}
```

**Generar las partidas, representados por los laberintos, necesarias para los juegos que esté organizando. El modo de organizar estas partidas vendrá determinado por el tipo de juego que esté organizando**  
Una vez que el AgenteLaberinto haya aceptado organizar las partidas, este será el encargado de generarlas una a una. Para ello crearemos una tarea cíclica que compruebe si hay partidas para comenzar, y si es el caso, creará un `AgentePartida` por cada una de las partidas a jugar.  

```
class TareaGenerarPartidas extends TickerBehaviour {
    void onTick(){
        if(!juegosAOrganizar.empty()){
            Juego juego = juegosAOrganizar.remove(0);
            if(juego != null) {
                numJuegos++;
                AgentePartida agentePartida = crearAgente(AgentePartida, juego);
                agentesPartidas.add(agentePartida);
            }
        }
    }
}
```

**Obtener los resultados de las partidas para completar los resultado de todos los juegos que esté organizando en un momento determinado.**  
El obtener los resultados de las partidas se hará mediante un protocolo FIPA-Propose entre el `AgentePartida` y `AgenteLaberinto`.  

Para ello vamos a crear `Informar Partida (FIPA-Propose)`  
![alt text](./img/InformarPartida.png)  

Dado esto podemos obtener una vez que finalice el `AgentePartida` la información en `AgenteLaberinto`  

Los nuevos elementos de la ontología son:  
- `ClasificacionPartida` : Si el juego ha finalizado correctamente se envía la información relativa a la clasificación del juego con los jugadores implicados.
    - `Partida` : representa el juego que ha finalizado.
    - `ListaJugadores` : colección de elementos `Jugador` que han participado en la partida ordenados desde el campeón en adelante.
    - `ListaPuntuacion` : colección con los puntos obtenidos por cada uno de los jugadores de la lista anterior.


- `Confirmación` : Este elemento simboliza que el AgenteLaberinto ha confirmado los datos de la partida recibidos:
	- `Partida` : representa el juego en el que no se desea participar.  

Además de estos nuevos elementos crearemos una nueva función llamada `informarResultadoMonitor` en la que se le informe al `AgenteMonitor` de los resultados de la partida.

## TareaInformarPartidaIniciador
```
class TareaInformarPartidaIniciador extends ProposeInitiator{
    void handleAcceptProposal(ACLMessage accept){
        print("El agentePartida "+accept.getSender()+" ha aceptado los resultados.");

        informarResultadoMonitor(accept)
    }

    void handleRejectProposal(ACLMessage reject){
        print("El agentePartida "+reject.getSender()+ " ha rechazado los resultados");
    }
}
```  

## TareaInformarPartidaParticipante  
```
class TareaInformarPartidaParticipante extends ProposeResponder{

    ACLMessage prepareResponse(ACLMessage propose){
        ACLMessage msg = propose.createReply();
        Justificacion justf;
        Confirmacion confirm;

        if ( ontologiaDesconocida() )
            msg.setPermormative( NOT_UNDERSTOOD )
            justf.setDetalle(ONTOLOGIA_DESCONOCIDA);
        else
            resultadoPartida = extraerPropuesta( propose );
            partidas.add(resultadoPartida)
            if ( resultadoPartida != null )
                msg.setPermormative( ACCEPT_PROPOSAL )
                confirm.setPartida( resultadoPartida.getPartida() )
            else
                msg.setPermormative( REJECT_PROPOSAL )
                justf.setJuego( juego );
                justf.setDetalle( "Los resultados son NULL." );   

        if ( incidencia )
            msg.setPermormative( NOT_UNDERSTOOD )
            incidencia.setDetalle(ERROR_CONTENIDO_MENSAJE);
        
        if (msg.getPerformative() == ACCEPT_PROPOSAL)
            manager.fillContent(msg, confirm);
        else if (msg.getPerformative() == REJECT_PROPOSAL)
            manager.fillContent(msg, justf);
        else if ( justf != null )
            manager.fillContent(msg, justf);       
        else 
            manager.fillContent(msg, incidencia); 
        return msg;              
    }
}
```  

Además en este apartado se puede observar el requisito **Guardar registro de las partidas que componen un juego por si se solicita su reproducción una vez que han finalizado.**

**Informar del resultado del juego al `AgenteMonitor` que le solicitó su organización.**  
Como hemos visto, el `AgenteMonitor` ya tenía una suscribción con el `AgenteLaberinto` por lo que en este apartado simplemente nos ceñiremos en enviar el Inform desde el `AgenteLaberinto` al `AgenteMonitor`.  
Esto lo haremos cuando el AgentePartida informe al AgenteLaberinto de que ha terminado una partida. Esto lo podemos ver un poco más arriba en la `TareaInformarPartidaIniciador`.
***
## AgentePartida  
Esta clase será la encargada de gestionar la propia partida a parte de gestionar la comunicación con los `AgenteRatón`, generar el laberinto y representar visualmente la partida  

### Variables
`Maze laberinto` Es el laberinto creado para la partida  
`int xQueso` X actual del queso  
`int yQueso` Y actual del queso  
`ArrayList<Integer> quesos` Es una lista que guarda los quesos que ha cogido cada ratón  
`int tamLaberinto` Es el tamaño del laberinto  
`int numQuesos` Es el número de quesos para esta partida  
`bool esperandoRaton` Controla si el ratón ha respondido  
`AID agenteLaberinto` Representa el AgenteLaberinto que creó el AgentePartida

**Crear el laberinto con los parámetros de configuración que le ha pasado el AgenteLaberinto.**  
En la inicialización del AgentePartida con los parámetros recibidos para iniciar la partida se puede generar el laberinto, haremos uso de 3 clases auxiliares que reutilizaremos de la práctica de IA, estas clases son las siguiente:  
**`Maze`**: Hace referencia al propio laberinto. Está compuesto por elemenetos de la clase Grid  
**`Grid`**: Hace referencia a cada una de las casillas del laberinto y está compuesto por paredes.  
**`Wall`**: Son las paredes del laberinto  

El constructor de la clase `Maze` se le pasan como argumentos las dimensiones del laberinto. En el constructor de Maze se creará el laberinto llamando al método de Maze **buildMaze()**.  

Tenemos un elemento del vocabulario el cual es `DificultadLaberinto` que define el grado de dificultad respecto al número de quesos y el número de casillas. En caso del número de casillas simplemente se lo pasamos al constructor de Maze dependiendo de la dificultad.

- `DificultadLaberinto`: mantiene unos intervalos para determinar el número de quesos y dimensiones del laberinto.
    - `FACIL` : entre 1 y 3 quesos y entre 5 y 10 unidades del laberinto.
    - `NORMAL` : entre 2 y 4 quesos y entre 10 y 20 unidades del laberinto.
    - `DIFICIL` : entre 4 y 8 quesos y entre 30 y 40 unidades del laberinto.

Además cada vez que un queso sea cogido se llamará a la función `generarQueso` que será la encargada de generar un nuevo queso cuando el último haya sido cogido.

```
void generarNuevoQueso() {
    do {
        int newX = random(0, tamLaberinto)
        int newY = random(0, tamLaberinto)
    } while(newX == xQueso AND newY == yQueso)

    xQueso =  newX
    yQueso = newY

    numQuesos++;
}
```

**Organizar los turnos de juego necesarios para completar una partida. Si se trata de una partida que aún no se ha completado.**  
Para organizar los turnos usaremos una variable entera que se mueva entre 0 y N siendo N el número de ratones, simulando un ciclo usando el operador módulo. Además usaremos una tarea para gestionar los turnos. 

La función `pedirMovimiento` se encarga de crear el CFP al ratón para pedir el movimiento al ratón en cuestión  

```
class TareaOrganizarTurno extends TickerBehaviour {

    int turno = 0;
    int re

    void onTick(){
        if(!esperandoRaton){

            AID raton = ratones.get(turno);
            pedirMovimiento(raton);


            turno++;
            turno = turno % jugadores.size();
        }

    }
}
```

**Visualiza el resultado de la partida para que se pueda seguir de forma visual y con un tiempo apropiado para ello. La visualización puede ser de una partida que en ese momento se está desarrollando o una que ya se ha desarrollado anteriormente.**  
La representación que haremos nos apoyaremos en la visualización de la práctica de Inteligencia Artificial del año pasado la cual es una matriz de imágenes de tamaño tamLaberinto x tamLaberinto en la que dependiendo de la casilla, se mostrará una imagen u otra.

Las casillas dependen de donde tengan los muros por lo que cada casilla con muros separados tendrá una representación diferente.

**Comunicar el resultado de la partida, si es nueva, al AgenteLaberinto que la solicitó.**  
Como está explicado en el apartado anterior, no entraremos mucho en detalles en este apartado ya que está especificado en el apartado del `AgenteLaberinto`. Pero resumiendo, se basa en establecer un protocolo FIPA-Propose al AgenteLaberinto que inició la partida, de esta forma se comunicarán ambos agentes para comunicar la respuesta.
***