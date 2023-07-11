/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import es.ujaen.ssmmaa.agentes.Constantes.OrdenComanda;
import es.ujaen.ssmmaa.gui.AgenteClienteJFrame;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.List;

public class AgenteCliente extends Agent {

    private AgenteClienteJFrame myGui;

    private ArrayList<Constantes.Plato> servicios = new ArrayList<>();

    protected void setup() {

        myGui = new AgenteClienteJFrame(this);
        myGui.setVisible(true);
        myGui.presentarSalida("Se inicia la ejecución de " + this.getName() + "\n");

        // Registro en las Páginas Amarillas
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("ServicioCliente");
        sd.setName("AgenteCliente");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // inicializar servicios
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            String[] parametros = ((String) args[0]).split(":");
            for (String parametro : parametros) {
                OrdenComanda orden = OrdenComanda.valueOf(parametro);
                String nombrePlato = "Plato " + parametro; // Nombre único para cada plato
                Constantes.Plato plato = new Constantes.Plato(nombrePlato, orden, 10.0);
                servicios.add(plato);

            }
        }

        // Comportamiento del agente
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                // Realizar tareas de la cocina               
                // Crear un mensaje FIPA-REQUEST para solicitar un servicio
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(new AID("AgenteRestaurante", AID.ISLOCALNAME));
                String nombrePlato = servicios.get(0).getNombre(); // Obtener el nombre del primer plato en la lista
                msg.setContent("Necesito un servicio: " + nombrePlato);
                send(msg);

                // Esperar la respuesta del AgenteRestaurante
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage reply = myAgent.receive(mt);
                if (reply != null) {
                    // Procesar la respuesta
                    String contenido = reply.getContent();
                    if ("Servicio completado".equals(contenido)) {
                        // Eliminar el servicio de la lista
                        servicios.remove(0);
                        myGui.presentarSalida("Servicio completado: " + contenido + "\n");
                        if (servicios.isEmpty()) {
                            // Si no hay más servicios, el cliente abandona el restaurante
                            doDelete();
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }

    protected void takeDown() {
        // Desregistro de las Páginas Amarillas
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Cerrar la GUI
        myGui.dispose();

        System.out.println("AgenteCocina: Terminando.");
    }
}
