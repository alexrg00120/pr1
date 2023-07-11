# Práctica a realizar (Opcional)
En esta práctica será una primera toma de contacto con la creación de agentes y una comunicación básica entre ellos para alcanzar los objetivos que tiene cada agente.

La práctica consiste en realizar una simulación con agentes sobre el funcionamiento de un restaurante.

## Definición de Constantes
Para la realización de la práctica hay que definir los enumerados con los elementos que se describen:

- **OrdenComanda**
	- *ENTRANTE*, *PRINCIPAL*, *POSTRE*

- **Plato**
	- *NOMBRE_DEL_PLATO(OrdenComanda,precio)*
		- La cantidad de platos estará a cargo de cada uno de vosotros cuando desarrolléis el proyecto. 

## Formato del fichero de configuración
Para las pruebas de la práctica se tendrá que aportar un fichero de configuración con el siguiente formato:

- Estará compuesto por líneas donde estará el nombre del agente, clase y en la línea siguiente los parámetros de configuración para ese agente. Los parámetros estarán en la descripción del agente.
	- `nombreAgente:es.ujaen.ssmma.agentes.ClaseAgente`
	- `parámetros agente`

- **AgenteMonitor** :
Será el agente que se crea para la prueba de la práctica y su parámetro es el fichero de configuración.
Procesará el fichero de configuración y creará todos los agentes presentes con sus parámetros.

## Agentes para la práctica
Las tareas de los agentes serán diseñadas por cada uno de los alumnos. Los agentes tienen unos parámetros y unos objetivos que deben alcanzar antes de finalizar.

Todos los agentes:
- Deben ser robustos y dar soluciones a las posibles incidencias en la ejecución.
- Deberán mostrar de forma clara su ejecución y se guardarán en un archivo el resultado de la ejecución de los agentes.

- **AgenteRestaurante**
	- Los parámetros del agente será la capacidad de usuarios que puede atender hasta finalizar su servicio. También tendrá definido el número de servicios que podrá dar antes de finalizar.
	- Los objetivos del agente son los siguientes:
		- Atender la petición de entrada que haga un cliente, si hay disponibilidad de espacio deberá atenderlo. 
		- El servicio a los clientes estará ordenado según el orden de la comanda.
		- La preparación del plato será encargado a una cocina que tenga disponibilidad para ese plato.

- **AgenteCocina**
	- Los parámetros del agente será la cantidad de cada plato que podrá preparar antes de finalizar.
	- Los objetivos del agente son los siguientes:
		- Atender solicitudes de los restaurantes para preparar un plato dependiendo de la disponibilidad.
		- Indicar cuando el plato ya ha sido preparado.

- **AgenteCliente**
	- Los parámetros del agente será una lista de servicios que quiere completar. Cada servicio tendrá un plato para el orden de la comanda.
	- Los objetivos del agente son los siguientes:
		- Encontrar el restaurante para un servicio, cuando finalice un servicio se entiende que el cliente ha abandonado el restaurante.




