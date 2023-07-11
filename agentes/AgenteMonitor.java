package es.ujaen.ssmmaa.agentes;


import es.ujaen.ssmmaa.gui.AgenteMonitorJFrame;
import jade.core.Agent;
import jade.core.MicroRuntime;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.StaleProxyException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author USUARIO
 */
public class AgenteMonitor extends Agent {

    private String configFilePath;
    private int intervalo;
    private List<String> agentes = new ArrayList<>();
    private List<String[]> parametrosAgentes = new ArrayList<>();
    private int numAgentesCreados = 0;  // Índice del agente actual en la lista de agentes
    private AgenteMonitorJFrame myGui;

    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            configFilePath = (String) args[0];
        }

        // Leer el fichero de configuración
        try (BufferedReader reader = new BufferedReader(new FileReader(configFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] partes = line.split(":");
                    agentes.add(partes[0] + ":" + partes[1]);
                    parametrosAgentes.add(Arrays.copyOfRange(partes, 2, partes.length));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Añadir la tarea TareaCrearAgentes
        addBehaviour(new TareaCrearAgentes(this, intervalo * 1000));

        // Crear y mostrar la GUI
        myGui = new AgenteMonitorJFrame(this);
        myGui.setVisible(true);

    }

    protected void takeDown() {
        // Cerrar la GUI
        myGui.dispose();

        // Imprimir un mensaje de despedida
        System.out.println("Agente " + getAID().getName() + " terminando.");
        MicroRuntime.stopJADE();
    }

    private class TareaCrearAgentes extends TickerBehaviour {

        public TareaCrearAgentes(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {

            if (numAgentesCreados < agentes.size()) {
                String agenteInfo = agentes.get(numAgentesCreados);
                String[] partes = agenteInfo.split(":");
                String nombreAgente = partes[0];
                String claseAgente = partes[1];
                String[] parametros = parametrosAgentes.get(numAgentesCreados);

                try {
                    MicroRuntime.startAgent(nombreAgente, claseAgente, parametros);

                    // Mostrar un mensaje en la GUI
                    myGui.presentarSalida("\nAgente " + nombreAgente + " creado con parámetros " + Arrays.toString(parametros) + ".");

                    numAgentesCreados++;  // Pasar al siguiente agente
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                } catch (Exception ex) {
                    Logger.getLogger(AgenteMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }

}
