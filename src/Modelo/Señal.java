/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo;

/**
 *
 * @author usuario
 */
public class Señal {
    private int id;
    private String nombre;
    private String urlAudio;
    private String urlImage;

    public Señal() {
        this.id = 0;
        this.nombre = "";
        this.urlAudio = "";
        this.urlImage = "";
    }

    public Señal(int id, String nombre, String urlAudio, String urlImage) {
        this.id = id;
        this.nombre = nombre;
        this.urlAudio = urlAudio;
        this.urlImage = urlImage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUrlAudio() {
        return urlAudio;
    }

    public void setUrlAudio(String urlAudio) {
        this.urlAudio = urlAudio;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }
}
