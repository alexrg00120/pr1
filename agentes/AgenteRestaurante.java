/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import es.ujaen.ssmmaa.gui.AgenteRestauranteJFrame;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AgenteRestaurante extends Agent {

    private AgenteRestauranteJFrame myGui;
    private int capacidad; //capacidad de usuarios que puede antender hasta finalizar su servicio
    private int servicios;//numero de servicios que podrá dar antes de finalizar 

    protected void setup() {
        myGui = new AgenteRestauranteJFrame(this);
        myGui.setVisible(true);
        myGui.presentarSalida("Se inicia la ejecución de " + this.getName() + "\n");

        // Registro en las Páginas Amarillas
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("ServicioRestaurante");
        sd.setName("AgenteRestaurante");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // inicializar capacidad y servicios
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            String[] parametros = ((String) args[0]).split(":");
            capacidad = Integer.parseInt(parametros[0]);
            servicios = Integer.parseInt(parametros[1]);
        }

        // Comportamiento del agente
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                // Esperar la solicitud de servicio de un AgenteCliente
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage mensaje = myAgent.receive(mt);
                if (mensaje != null) {

                    // Procesar la solicitud
                    String contenido = mensaje.getContent();
                    //Si hay disponibilidad de espacio se debe atender 
                    if (contenido.startsWith("Necesito un servicio: ") && servicios > 0) {
                        // Si hay capacidad para dar un servicio, enviar un mensaje FIPA-REQUEST al AgenteCocina para preparar un plato
                        String nombrePlato = contenido.substring("Necesito un servicio: ".length());
                        ACLMessage solitudCocina = new ACLMessage(ACLMessage.REQUEST);
                        solitudCocina.addReceiver(new AID("AgenteCocina", AID.ISLOCALNAME));
                        solitudCocina.setContent("Preparar plato");
                        send(solitudCocina);
                        // Mostrar el nombre del plato
                        myGui.presentarSalida("Solicitud recibida de: " + mensaje.getSender().getName());
                        myGui.presentarSalida("Plato solicitado: " + nombrePlato + "\n");

                        // Esperar la respuesta del AgenteCocina
                        //Aquí el ACL.INFORM no funciona bien !!!!!HAY QUE ARREGLARLO
                        mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                                MessageTemplate.MatchSender(new AID("AgenteCocina", AID.ISLOCALNAME)));
                        ACLMessage respuestaCocina = blockingReceive(mt);
                        if (respuestaCocina != null) {
                            // Procesar la respuesta
                            myGui.presentarSalida("Plato Preparado\n");
                            contenido = respuestaCocina.getContent();
                            if ("Plato preparado".equals(contenido)) {
                                // Si el plato ha sido preparado, enviar un mensaje FIPA-INFORM al AgenteCliente para indicar que el servicio ha sido completado
                                ACLMessage inform = mensaje.createReply();
                                inform.setPerformative(ACLMessage.INFORM);
                                inform.setContent("Servicio completado");
                                send(inform);

                                // Disminuir la cantidad de servicios disponibles
                                servicios--;
                                myGui.presentarSalida("Servicio completado");
                            }
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

        System.out.println("AgenteRestaurante: Terminando.");
    }

}
