/******************************************************
 *   UNIVERSIDAD POLITECNICA DEL ESTADO DE MORELOS    *
 *                                                    *
 *          CUATRIMESTRE: 6      GRUPO: A             *
 *  TIPO DE EVIDENCIA: INTEGRADORA                    *
 *  MATERIA: INGENIERIA DE SOFTWARE                   *
 *  PROFESORA: MIGUEL HIDALGO REYES                 *
 *                                                    *
 *              INTEGRANTES DEL EQUIPO:               *
 *         NOMBRE                      MATRICULA      *
 *                                                    *
 * ESQUIVEL MARTINEZ ESTEBAN          EMEO151863      *
 * NOLASCO ORTIZ ELIACIM              NOEO150589      *
 * VALDEPENA RIVERA FRANCISCO JAVIER  VRFO150252      *
 ******************************************************/

package Modelo;

import java.util.Date;

public class Usuario {
    private String nombre;
    private String aPaterno;
    private String aMaterno;
    private String nickname;
    private String password;
    private String email;
    private String tipo;
    private boolean activo;

    public Usuario() {
        this.nombre = "";
        this.aPaterno = "";
        this.aMaterno = "";
        this.nickname = "";
        this.password = "";
        this.email = "";
        this.tipo = "usuario";
        this.activo = true;
    }

    public Usuario(String nombre, String aPaterno, String aMaterno, String nickname, String password, String email) {
        this.nombre = nombre;
        this.aPaterno = aPaterno;
        this.aMaterno = aMaterno;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.tipo = "usuario";
        this.activo = true;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getaPaterno() {
        return aPaterno;
    }

    public void setaPaterno(String aPaterno) {
        this.aPaterno = aPaterno;
    }

    public String getaMaterno() {
        return aMaterno;
    }

    public void setaMaterno(String aMaterno) {
        this.aMaterno = aMaterno;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean getActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
