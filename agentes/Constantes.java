/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ujaen.ssmmaa.agentes;

/**
 *
 * @author USUARIO
 */
public class Constantes {

    public enum OrdenComanda {
        ENTRANTE, PRINCIPAL, POSTRE
    }

    public static class Plato {

        private String nombre;
        private OrdenComanda orden;
        private double precio;

        public Plato(String nombre, OrdenComanda orden, double precio) {
            this.nombre = nombre;
            this.orden = orden;
            this.precio = precio;
        }

        // Getters y setters
        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public OrdenComanda getOrden() {
            return orden;
        }

        public void setOrden(OrdenComanda orden) {
            this.orden = orden;
        }

        public double getPrecio() {
            return precio;
        }

        public void setPrecio(double precio) {
            this.precio = precio;
        }
    }
}
