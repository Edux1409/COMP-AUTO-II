/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.itz.proyectofinal.lexemas;


public class Lexema {
    private String elemento;
    private String tipoToken;  // PR, D, Id, M, C, A, AM, P, T, N, OPB, OPA, OPL, OPN, OPR
    private int token;
    private int linea;

    public Lexema(String elemento, String tipoToken, int token, int linea) {
        this.elemento = elemento;
        this.tipoToken = tipoToken;
        this.token = token;
        this.linea = linea;
    }

    public String getElemento() {
        return elemento;
    }

    public String getTipoToken() {
        return tipoToken;
    }

    public int getToken() {
        return token;
    }

    public int getLinea() {
        return linea;
    }

    public void setElemento(String elemento) {
        this.elemento = elemento;
    }

    public void setTipoToken(String tipoToken) {
        this.tipoToken = tipoToken;
    }

    public void setToken(int token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "Lexema{" + "elemento=" + elemento + ", tipoToken=" + tipoToken + 
               ", token=" + token + ", linea=" + linea + '}';
    }
}