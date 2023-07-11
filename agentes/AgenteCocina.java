/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

import es.ujaen.ssmmaa.gui.AgenteCocinaJFrame;

// AgenteCocina.java
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
import java.util.HashMap;
import java.util.Map;

public class AgenteCocina extends Agent {

    private AgenteCocinaJFrame myGui;
    private Map<Constantes.Plato, Integer> cantidadPlatos = new HashMap<>(); //cantidad de platos que podrá preparar antes de finalizar 

    protected void setup() {
        myGui = new AgenteCocinaJFrame(this);
        myGui.setVisible(true);
        myGui.presentarSalida("Se inicia la ejecución de " + this.getName() + "\n");

        // Registro en las Páginas Amarillas
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("ServicioCocina");
        sd.setName("AgenteCocina");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Recuperar los argumentos
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            String[] parametros = ((String) args[0]).split(":");
            for (int i = 0; i < parametros.length; i++) {
                int cantidad = Integer.parseInt(parametros[i]);
                Constantes.OrdenComanda orden = Constantes.OrdenComanda.values()[i];
                // Aquí asumimos que todos los platos tienen el mismo nombre y precio.
                Constantes.Plato plato = new Constantes.Plato("Plato", orden, 10.0);
                cantidadPlatos.put(plato, cantidad);
            }
        }

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                // Esperar la solicitud de preparación de un plato del AgenteRestaurante
                MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                        MessageTemplate.MatchSender(new AID("AgenteRestaurante", AID.ISLOCALNAME)));
                ACLMessage msg = blockingReceive(mt);
                if (msg != null) {
                    // Procesar la solicitud
                    String contenido = msg.getContent();
                    //Constantes.Plato plato = buscarPlatoPorNombre(contenido);
                    if (/*plato != null*/"Preparar plato".equals(contenido) /*&& cantidadPlatos.get(plato) > 0*/) {
                        // Si el plato está disponible, prepararlo y disminuir la cantidad

                        //EL ERROR ESTA AQUÍ,                         
                        //cantidadPlatos.put(plato, cantidadPlatos.get(plato) - 1);
                        myGui.presentarSalida("Plato disponible\n");
                        // Enviar un mensaje FIPA-INFORM al AgenteRestaurante para indicar que el plato ha sido preparado
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("Plato preparado");
                        send(reply);
                        myGui.presentarSalida("Plato cocinado\n");

                    }
                } else {
                    block();
                }
            }
        });
    }

    private Constantes.Plato buscarPlatoPorNombre(String nombre) {
        for (Constantes.Plato plato : cantidadPlatos.keySet()) {
            if (plato.getNombre().equals(nombre)) {
                return plato;
            }
        }
        return null;
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
