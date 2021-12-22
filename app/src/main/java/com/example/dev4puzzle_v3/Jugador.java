package com.example.dev4puzzle_v3;

public class Jugador {

    String uid;
    String nombreJugador;
    String tiempoPartida;
    long tiempoLongPartida;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }

    public String getTiempoPartida() {
        return tiempoPartida;
    }

    public void setTiempoPartida(String tiempoPartida) {
        this.tiempoPartida = tiempoPartida;
    }

    public long getTiempoLongPartida() {
        return tiempoLongPartida;
    }

    public void setTiempoLongPartida(long tiempoLongPartida) {
        this.tiempoLongPartida = tiempoLongPartida;
    }

    public String toString() {

        return nombreJugador + " "+ tiempoPartida;
    }

}
