/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.itz.proyectofinal.semantico;

import edu.itz.proyectofinal.control.Control;
import edu.itz.proyectofinal.lexemas.Lexema;
import edu.itz.proyectofinal.vistas.Ventana;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;
import javax.swing.JTextArea;

public class AnaSemantico {
    private static JTextArea salida;
    private static ArrayList<Lexema> lexes;
    private static ArrayList<String[]> tablaSimbolos;
    
    // Variables para almacenar los datos de las pilas
    private static ArrayList<String[]> pilaRIDData = new ArrayList<>();
    private static ArrayList<String[]> pilaIDRData = new ArrayList<>();
    private static String resultadoAnalisis = "";
    private static boolean hayExpresion = false;

    /**
     * Realiza el análisis semántico buscando la primera expresión de asignación.
     * @param ventana La ventana de la interfaz para mostrar la salida.
     */
    public static void programa(Ventana ventana) {
        salida = ventana.getTxtSalida();
        lexes = Control.getTokensAnalizados();
        tablaSimbolos = Control.getTablaSimbolos();
        
        // Limpiar datos anteriores
        pilaRIDData.clear();
        pilaIDRData.clear();
        hayExpresion = false;
        
        salida.append("\n\n=== ANÁLISIS SEMÁNTICO ===\n");
        
        // 1. Buscar la primera expresión de asignación
        int inicioExpresion = -1;
        int finExpresion = -1;
        Lexema variableResultado = null;

        // Buscamos la secuencia: ID -> OPA(=)
        for (int i = 0; i < lexes.size() - 1; i++) {
            Lexema actual = lexes.get(i);
            Lexema siguiente = lexes.get(i + 1);

            // Criterio para una asignación: ID seguido de =
            if (actual.getTipoToken().equals("Id") && siguiente.getElemento().equals("=")) {
                variableResultado = actual; 
                inicioExpresion = i + 2; 

                // Buscamos el final de la expresión (el primer ';' después del '=')
                for (int j = inicioExpresion; j < lexes.size(); j++) {
                    if (lexes.get(j).getElemento().equals(";")) {
                        finExpresion = j - 1; 
                        break;
                    }
                }
                
                if (finExpresion != -1 && inicioExpresion <= finExpresion) {
                    break;
                } else {
                    inicioExpresion = -1;
                    finExpresion = -1;
                    variableResultado = null;
                }
            }
        }

        // 2. Ejecutar el análisis semántico si se encontró una expresión
        if (inicioExpresion != -1 && finExpresion != -1) {
            salida.append("✓ Primera expresión de asignación encontrada en línea " + 
                          variableResultado.getLinea() + ": **" + 
                          variableResultado.getElemento() + " = ...**\n");
            hayExpresion = true;
            simularPilasRID_IDR(variableResultado, inicioExpresion, finExpresion);
        } else {
            // 3. Caso sin expresión algebraica
            salida.append("\n⭐ No se encontraron expresiones algebraicas de asignación (ID = ...).\n");
            salida.append("✓ Análisis Semántico: **CORRECTO**.\n");
            resultadoAnalisis = "No se encontraron expresiones algebraicas de asignación.\nAnálisis Semántico: CORRECTO";
        }
    }

    /**
     * Simula la creación y muestra de las pilas RID e IDR en formato de tabla.
     */
    private static void simularPilasRID_IDR(Lexema varResultado, int inicio, int fin) {
        // PILA RID (Identificadores/Constantes de DERECHA a Izquierda)
        Stack<String> pilaRID = new Stack<>();
        // PILA IDR (Tipos de Izquierda a DERECHA)
        Stack<String> pilaIDR = new Stack<>();

        // Rellenar las pilas
        for (int i = inicio; i <= fin; i++) {
            Lexema lex = lexes.get(i);
            String tipo = lex.getTipoToken();

            // Solo nos interesan Identificadores (Id), Números/Constantes (N/C) 
            // y excluimos operadores y delimitadores
            if (tipo.equals("Id") || tipo.equals("N") || tipo.equals("C") || tipo.equals("T")) {
                pilaRID.push(lex.getElemento()); 
                pilaIDR.push(obtenerTipoReal(lex)); 
            }
        }
        
        // La Pila IDR (Tipos) se invierte para que los tipos sigan el orden de evaluación de la expresión (Izquierda a Derecha)
        Stack<String> pilaIDR_ordenada = new Stack<>();
        while (!pilaIDR.isEmpty()) {
            pilaIDR_ordenada.push(pilaIDR.pop());
        }
        
        // Para mostrar la Pila RID correctamente en una tabla, la invertimos temporalmente
        ArrayList<String> listaRID_inversa = new ArrayList<>(pilaRID);
        Collections.reverse(listaRID_inversa);
        
        // El tipo de la variable resultado desde la tabla de símbolos
        String tipoResultado = obtenerTipoReal(varResultado);

        // --- Almacenar datos para las tablas ---
        pilaRIDData.clear();
        pilaIDRData.clear();
        
        // Almacenar Pila RID
        for (int i = 0; i < listaRID_inversa.size(); i++) {
            String[] fila = {String.valueOf(i + 1), listaRID_inversa.get(i)};
            pilaRIDData.add(fila);
        }
        
        // Almacenar Pila IDR
        ArrayList<String> listaIDR = new ArrayList<>(pilaIDR_ordenada);
        for (int i = 0; i < listaIDR.size(); i++) {
            String[] fila = {String.valueOf(i + 1), convertirTipoCompleto(listaIDR.get(i))};
            pilaIDRData.add(fila);
        }

        // --- Mostrar en salida de texto ---
        salida.append("\n--- VERIFICACIÓN DE TIPOS Y PILAS ---\n");
        salida.append("  Variable Resultado: **" + varResultado.getElemento() + "** de tipo: **" + 
                     convertirTipoCompleto(tipoResultado) + "**\n");
        
        // Tabla 1: PILA RID (Identificadores/Constantes)
        salida.append("\n### PILA RID (Identificadores y Constantes)\n");
        salida.append("Posición\tElemento\n");
        salida.append("--------\t--------\n");
        for (String[] fila : pilaRIDData) {
            salida.append(String.format("%-8s\t%s\n", fila[0], fila[1]));
        }

        // Tabla 2: PILA IDR (Tipos de Elementos)
        salida.append("\n### PILA IDR (Tipos de Elementos)\n");
        salida.append("Posición\tTipo\n");
        salida.append("--------\t--------\n");
        for (String[] fila : pilaIDRData) {
            salida.append(String.format("%-8s\t%s\n", fila[0], fila[1]));
        }
        
        // Verificación de compatibilidad de tipos
        verificarCompatibilidad(tipoResultado, pilaIDR_ordenada, varResultado.getElemento());
    }
    
    /**
     * Obtiene el tipo real de un lexema consultando la tabla de símbolos
     */
    private static String obtenerTipoReal(Lexema lex) {
        String tipoToken = lex.getTipoToken();
        String elemento = lex.getElemento();
        
        // Si es un número
        if (tipoToken.equals("N")) {
            if (elemento.contains(".")) {
                return "D"; // double
            } else {
                return "I"; // int
            }
        }
        
        // Si es una constante (cadena o carácter)
        if (tipoToken.equals("C")) {
            if (elemento.startsWith("\"") && elemento.endsWith("\"")) {
                return "S"; // String
            } else if (elemento.startsWith("'") && elemento.endsWith("'")) {
                return "C"; // char
            } else if (elemento.equals("true") || elemento.equals("false")) {
                return "B"; // boolean
            }
            return "S"; // por defecto String
        }
        
        // Si es texto (T)
        if (tipoToken.equals("T")) {
            return "S"; // String
        }
        
        // Si es un identificador, consultar la tabla de símbolos
        if (tipoToken.equals("Id")) {
            // Buscar en la tabla de símbolos
            for (String[] simbolo : tablaSimbolos) {
                if (simbolo[0].equals(elemento)) {
                    return simbolo[1]; // Retorna el tipo (I, D, S, C, B, etc.)
                }
            }
            
            // Si no está en la tabla de símbolos, usar inferencia por nombre
            return inferirTipoPorNombre(elemento);
        }
        
        return "S"; // Por defecto String
    }
    
    /**
     * Inferir tipo por nombre de variable (solo como respaldo)
     */
    private static String inferirTipoPorNombre(String nombre) {
        nombre = nombre.toLowerCase();
        
        if (nombre.startsWith("i") || nombre.contains("entero") || nombre.contains("contador") || 
            nombre.contains("num") || nombre.contains("index")) {
            return "I"; // int
        }
        if (nombre.startsWith("f") || nombre.contains("doble") || nombre.contains("decimal") || 
            nombre.contains("flotante") || nombre.contains("real")) {
            return "D"; // double
        }
        if (nombre.startsWith("b") || nombre.contains("bandera") || nombre.contains("flag") || 
            nombre.contains("es") || nombre.contains("tiene")) {
            return "B"; // boolean
        }
        if (nombre.startsWith("c") || nombre.contains("caracter") || nombre.contains("letra") || 
            nombre.contains("char")) {
            return "C"; // char
        }
        if (nombre.startsWith("s") || nombre.contains("cadena") || nombre.contains("texto") || 
            nombre.contains("mensaje") || nombre.contains("str")) {
            return "S"; // String
        }
        
        return "S"; // tipo por defecto String
    }
    
    /**
     * Convierte la abreviatura del tipo a nombre completo para mostrar
     */
    private static String convertirTipoCompleto(String abreviatura) {
        switch (abreviatura) {
            case "I": return "int";
            case "D": return "double";
            case "S": return "String";
            case "C": return "char";
            case "B": return "boolean";
            case "F": return "float";
            case "L": return "long";
            default: return abreviatura; // Si ya está en formato completo
        }
    }
    
    /**
     * Verifica la compatibilidad de tipos entre la variable y la expresión
     */
    private static void verificarCompatibilidad(String tipoResultado, Stack<String> pilaTiposExpresion, String nombreVariable) {
        boolean incompatible = false;
        String razon = "Análisis Semántico correcto.";
        String tipoIncompatible = "";
        
        // Convertir tipo resultado a formato completo para mensajes
        String tipoResultadoCompleto = convertirTipoCompleto(tipoResultado);
        
        // Verificar cada tipo en la expresión
        for (String tipoExpresion : pilaTiposExpresion) {
            String tipoExpresionCompleto = convertirTipoCompleto(tipoExpresion);
            
            if (!sonTiposCompatibles(tipoResultado, tipoExpresion)) {
                incompatible = true;
                tipoIncompatible = tipoExpresionCompleto;
                razon = "INCOMPATIBILIDAD: No se puede asignar un valor de tipo '" + tipoExpresionCompleto + 
                       "' a la variable '" + nombreVariable + "' de tipo '" + tipoResultadoCompleto + "'";
                break;
            }
        }
        
        salida.append("\n--- RESULTADO DEL ANÁLISIS DE TIPOS ---\n");
        
        if (incompatible) {
            salida.append("❌ ¡ERROR SEMÁNTICO! INCOMPATIBILIDAD DE TIPOS DETECTADA.\n");
            salida.append("Razón: **" + razon + "**\n");
            resultadoAnalisis = "❌ ¡ERROR SEMÁNTICO!\n" + razon;
        } else {
            salida.append("✅ Análisis Semántico Correcto.\n");
            salida.append("Razón: **" + razon + "**\n");
            resultadoAnalisis = "✅ Análisis Semántico Correcto.\n" + razon;
        }
    }
    
    /**
     * Determina si dos tipos son compatibles para asignación
     */
    private static boolean sonTiposCompatibles(String tipoDestino, String tipoOrigen) {
        // Mismos tipos siempre son compatibles
        if (tipoDestino.equals(tipoOrigen)) {
            return true;
        }
        
        // Convertir a formato completo para comparaciones
        String destino = convertirTipoCompleto(tipoDestino);
        String origen = convertirTipoCompleto(tipoOrigen);
        
        // Reglas de compatibilidad
        
        // int puede recibir int (ya cubierto arriba), pero no otros tipos
        if (destino.equals("int")) {
            return origen.equals("int"); // int solo es compatible con int
        }
        
        // double puede recibir int y double
        if (destino.equals("double")) {
            return origen.equals("int") || origen.equals("double");
        }
        
        // boolean solo es compatible con boolean
        if (destino.equals("boolean")) {
            return origen.equals("boolean");
        }
        
        // char solo es compatible con char
        if (destino.equals("char")) {
            return origen.equals("char");
        }
        
        // String puede recibir String y char (para caracteres individuales)
        if (destino.equals("String")) {
            return origen.equals("String") || origen.equals("char");
        }
        
        // float puede recibir int y float
        if (destino.equals("float")) {
            return origen.equals("int") || origen.equals("float");
        }
        
        // long puede recibir int y long
        if (destino.equals("long")) {
            return origen.equals("int") || origen.equals("long");
        }
        
        // Para tipos desconocidos, asumir incompatibilidad
        return false;
    }
    
    // Métodos públicos para acceder a los datos de las pilas
    public static ArrayList<String[]> getPilaRIDData() {
        return pilaRIDData;
    }
    
    public static ArrayList<String[]> getPilaIDRData() {
        return pilaIDRData;
    }
    
    public static String getResultadoAnalisis() {
        return resultadoAnalisis;
    }
    
    public static boolean hayExpresionParaMostrar() {
        return hayExpresion;
    }
}