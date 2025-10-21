/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.itz.proyectofinal.semantico;

import edu.itz.proyectofinal.control.Control;
import edu.itz.proyectofinal.lexemas.Lexema;
import edu.itz.proyectofinal.vistas.Ventana;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTextArea;


public class Semantico {

    private static ArrayList<Lexema> lexes;
    private static JTextArea salida; // Debe inicializarse al inicio
    private static int errores = 0;
    private static final int MAX_ERRORES = 100;
   
    private static final TablaSimbolos TS = new TablaSimbolos();

    /**
     * Inicia el análisis semántico del código fuente.
     */
    public static void programa(Ventana ventana) {
        salida = ventana.getTxtSalida(); // Inicializa el JTextArea de salida
        salida.append("\n\n=== ANÁLISIS SEMÁNTICO ===\n");
        lexes = Control.getTokensAnalizados();
        errores = 0;
        TS.limpiar(); // Aseguramos que la tabla esté vacía al inicio

        if (lexes.isEmpty()) {
            error("No hay tokens para análisis semántico.", 1);
            return;
        }

        // 1. Llenar la Tabla de Símbolos (variables/constantes) y Detectar Etiquetas
        recolectarSimbolosYEtiquetas();
        
        // 2. Detectar Sobrecarga de Operadores
        detectarSobrecargaOperadores();

        // 3. Verificaciones de uso (ejemplo: variable declarada vs usada)
        verificarUsoSimbolos();

        if (errores == 0) {
            salida.append("\n✓ Análisis semántico completado SIN ERRORES.\n");
            TS.mostrar(salida);
        } else {
            salida.append("\n✗ Análisis completado con " + errores + " errores semánticos.\n");
        }
    }
 
    private static void recolectarSimbolosYEtiquetas() {
        salida.append("\n--- Recolección de Símbolos y Etiquetas ---\n");
        
        for (int i = 0; i < lexes.size(); i++) {
            Lexema actual = lexes.get(i);
            
            // Lógica para detectar declaración de variables (simplificada: Tipo ID ;)
            // Un token es un tipo de dato si es PR y es un tipo primitivo conocido
            if (actual.getTipoToken().equals("PR") && esTipoDato(actual.getElemento())) {
                if (i + 1 < lexes.size() && lexes.get(i + 1).getTipoToken().equals("Id")) {
                    Lexema id = lexes.get(i + 1);
                    String tipo = actual.getElemento();
                    String valor = null; 

                    // Buscamos si hay asignación inmediata
                    int j = i + 2;
                    if (j < lexes.size() && lexes.get(j).getElemento().equals("=")) {
                         if (j + 1 < lexes.size() && !lexes.get(j + 1).getElemento().equals(";")) {
                            valor = lexes.get(j + 1).getElemento();
                         }
                    }
                    
                    TS.agregarSimbolo(id.getElemento(), tipo, valor, "Variable", id.getLinea());
                }
            } 
            
            // Lógica para detección de etiquetas (ID seguido de :)
            else if (actual.getTipoToken().equals("Id") && i + 1 < lexes.size() && 
                     lexes.get(i + 1).getElemento().equals(":") && lexes.get(i + 1).getTipoToken().equals("D")) {
                
                // Las etiquetas deben estar en la tabla de símbolos
                TS.agregarSimbolo(actual.getElemento(), "Etiqueta", null, "Etiqueta", actual.getLinea());
                salida.append("✓ Etiqueta detectada: " + actual.getElemento() + " en línea " + actual.getLinea() + "\n");
            }
        }
    }
    
    // -------------------------------------------------------------------------
    // REQUERIMIENTO: Operadores en sobrecarga
    // -------------------------------------------------------------------------
    private static void detectarSobrecargaOperadores() {
        salida.append("\n--- Detección de Sobrecarga de Operadores ---\n");
        
        // En Java, el '+' es el principal operador sobrecargado (suma vs. concatenación)
        for (int i = 0; i < lexes.size(); i++) {
            Lexema actual = lexes.get(i);
            
            if (actual.getElemento().equals("+") && actual.getTipoToken().equals("OPB")) {
                boolean esConcatenacion = false;
                
                // Comprobación simple: Si el token anterior o siguiente es una constante de cadena ("...")
                if (i > 0 && esConstanteString(lexes.get(i - 1))) {
                    esConcatenacion = true;
                } else if (i + 1 < lexes.size() && esConstanteString(lexes.get(i + 1))) {
                    esConcatenacion = true;
                }
                
                if (esConcatenacion) {
                    salida.append("⚠ Operador '+' usado en posible **sobrecarga** (concatenación de String) en línea " + actual.getLinea() + "\n");
                }
            }
        }
    }
    
    private static void verificarUsoSimbolos() {
         salida.append("\n--- Verificación de Uso de Símbolos ---\n");
         
        
         for (int i = 0; i < lexes.size(); i++) {
            Lexema actual = lexes.get(i);

            if (actual.getTipoToken().equals("Id")) {
                String nombre = actual.getElemento();
                
                boolean esDeclaracion = (i > 0 && lexes.get(i - 1).getTipoToken().equals("PR") && esTipoDato(lexes.get(i - 1).getElemento()));
                
                if (!esDeclaracion && !TS.existeSimbolo(nombre)) {
                    error("Variable o identificador no declarado: '" + nombre + "'", actual.getLinea());
                }
            }
         }
    }

    private static boolean esTipoDato(String elemento) {
        return elemento.equals("int") || elemento.equals("String") || 
               elemento.equals("boolean") || elemento.equals("float") || 
               elemento.equals("double") || elemento.equals("char");
    }
    
    private static boolean esConstanteString(Lexema lex) {
        // En tu análisis léxico, las constantes de cadena son probablemente de tipo N y comienzan con comillas
        return lex.getTipoToken().equals("N") && lex.getElemento().startsWith("\"") && lex.getElemento().endsWith("\"");
    }

    public static void error(String msg, int linea) {
        errores++;
        if (salida == null) {
             // En caso extremo de fallo en inicialización
            System.err.println("ERROR SEMÁNTICO (Línea " + linea + "): " + msg); 
            return;
        }
        
        if (errores > MAX_ERRORES) {
            salida.append("\n✗ DEMASIADOS ERRORES. Análisis semántico abortado.\n");
            throw new RuntimeException("Demasiados errores semánticos");
        }
        
        salida.append("ERROR SEMÁNTICO [" + errores + "] en línea " + linea + ": " + msg + "\n");
    }
}


class TablaSimbolos {
    // Almacena: Nombre_del_símbolo -> Información_semántica
    private final Map<String, Simbolo> tabla = new HashMap<>();

    public void agregarSimbolo(String nombre, String tipo, String valor, String categoria, int lineaDeclaracion) {
        if (tabla.containsKey(nombre)) {
            // Se usa el método estático de error de AnaSemantico para reportar
            Semantico.error("Símbolo duplicado '" + nombre + 
                               "' declarado nuevamente. Primera vez en línea " + tabla.get(nombre).getLineaDeclaracion(), 
                               lineaDeclaracion);
            return;
        }
        tabla.put(nombre, new Simbolo(nombre, tipo, valor, categoria, lineaDeclaracion));
    }

    public boolean existeSimbolo(String nombre) {
        return tabla.containsKey(nombre);
    }
    
    public void limpiar() {
        tabla.clear();
    }

    public void mostrar(JTextArea salida) {
        if (tabla.isEmpty()) {
            salida.append("\nTabla de Símbolos: VACÍA\n");
            return;
        }
        salida.append("\n=== TABLA DE SÍMBOLOS ===\n");
        salida.append(String.format("%-15s | %-10s | %-10s | %-15s | %s\n", 
                                    "NOMBRE", "CATEGORÍA", "TIPO", "VALOR INICIAL", "LÍNEA"));
        salida.append("-----------------------------------------------------------------\n");
        tabla.values().forEach(s -> 
            salida.append(String.format("%-15s | %-10s | %-10s | %-15s | %d\n", 
                                        s.getNombre(), s.getCategoria(), s.getTipo(), 
                                        s.getValorInicial() != null ? s.getValorInicial() : "NULL", 
                                        s.getLineaDeclaracion()))
        );
        salida.append("-----------------------------------------------------------------\n");
    }
}

class Simbolo {
    private final String nombre;
    private final String tipo;
    private final String valorInicial;
    private final String categoria; // Variable, Constante, Etiqueta, Método, Clase
    private final int lineaDeclaracion;

    public Simbolo(String nombre, String tipo, String valorInicial, String categoria, int lineaDeclaracion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.valorInicial = valorInicial;
        this.categoria = categoria;
        this.lineaDeclaracion = lineaDeclaracion;
    }

   
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getValorInicial() { return valorInicial; }
    public String getCategoria() { return categoria; }
    public int getLineaDeclaracion() { return lineaDeclaracion; }
}