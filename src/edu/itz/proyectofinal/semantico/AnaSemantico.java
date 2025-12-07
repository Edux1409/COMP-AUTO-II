package edu.itz.proyectofinal.semantico;

import edu.itz.proyectofinal.control.Control;
import edu.itz.proyectofinal.lexemas.Lexema;
import edu.itz.proyectofinal.vistas.Ventana;
import java.util.ArrayList;
import javax.swing.JTextArea;

public class AnaSemantico {
    private static JTextArea salida;
    private static ArrayList<Lexema> lexes;

    public static void programa(Ventana ventana) {
        salida = ventana.getTxtSalida();
        lexes = Control.getTokensAnalizados();
        
        salida.append("\n\n=== 🔍 ANÁLISIS SEMÁNTICO ===\n");
        
        int inicioExpresion = -1;
        int finExpresion = -1;
        Lexema variableResultado = null;

        for (int i = 0; i < lexes.size() - 1; i++) {
            Lexema actual = lexes.get(i);
            Lexema siguiente = lexes.get(i + 1);

            if (actual.getTipoToken().equals("Id") && siguiente.getElemento().equals("=")) {
                variableResultado = actual; 
                inicioExpresion = i + 2; 

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

        if (inicioExpresion != -1 && finExpresion != -1) {
            salida.append("✓ Primera expresión de asignación encontrada en línea " + 
                          variableResultado.getLinea() + ": **" + 
                          variableResultado.getElemento() + " = ...**\n");
            
            analizarExpresionCompleta(variableResultado, inicioExpresion, finExpresion);
        } else {
            salida.append("\n⭐ No se encontraron expresiones algebraicas de asignación (ID = ...).\n");
            salida.append("✅ Análisis Semántico: **CORRECTO**.\n");
        }
    }

    /**
     * Analiza la expresión completa
     */
    private static void analizarExpresionCompleta(Lexema varResultado, int inicio, int fin) {
        // Extraer tokens de la expresión
        ArrayList<Lexema> expresionTokens = new ArrayList<>();
        for (int i = inicio; i <= fin; i++) {
            expresionTokens.add(lexes.get(i));
        }
        
        // Mostrar expresión
        salida.append("\n--- EXPRESIÓN ANALIZADA ---\n");
        StringBuilder expresionStr = new StringBuilder();
        for (Lexema lex : expresionTokens) {
            expresionStr.append(lex.getElemento()).append(" ");
        }
        salida.append(varResultado.getElemento() + " = " + expresionStr.toString().trim() + "\n");
        
        // Analizar estructura de la expresión
        String expresion = expresionStr.toString().trim();
        
        // -------------------------------------------------------------
        // GENERAR TABLAS PARA DIFERENTES TIPOS DE EXPRESIONES
        // -------------------------------------------------------------
        
        // Caso 1: Expresión simple (ej: "5", "x", "3.14")
        if (expresionTokens.size() == 1) {
            generarTablasSimple(varResultado.getElemento(), expresionTokens.get(0).getElemento());
        }
        // Caso 2: Expresión con un operador (ej: "x + y")
        else if (expresionTokens.size() == 3 && esOperador(expresionTokens.get(1))) {
            generarTablasUnOperador(varResultado.getElemento(), 
                                   expresionTokens.get(0).getElemento(),
                                   expresionTokens.get(1).getElemento(),
                                   expresionTokens.get(2).getElemento());
        }
        // Caso 3: Expresión con dos operadores (ej: "x + y * 5")
        else if (expresionTokens.size() >= 3) {
            // Buscar estructura de operadores
            ArrayList<String> operandos = new ArrayList<>();
            ArrayList<String> operadores = new ArrayList<>();
            
            for (Lexema lex : expresionTokens) {
                if (esOperador(lex)) {
                    operadores.add(lex.getElemento());
                } else {
                    operandos.add(lex.getElemento());
                }
            }
            
            if (operadores.size() == 2 && operandos.size() == 3) {
                generarTablasDosOperadores(varResultado.getElemento(),
                                          operandos.get(0), operandos.get(1), operandos.get(2),
                                          operadores.get(0), operadores.get(1), expresion);
            } else {
                generarTablasGeneral(varResultado.getElemento(), expresionTokens, expresion);
            }
        } else {
            generarTablasGeneral(varResultado.getElemento(), expresionTokens, expresion);
        }
        
        // Verificación de tipos
        ArrayList<String> listaTipos = new ArrayList<>();
        for (int i = inicio; i <= fin; i++) {
            Lexema lex = lexes.get(i);
            if (lex.getTipoToken().equals("Id") || lex.getTipoToken().equals("N") || 
                lex.getTipoToken().equals("C")) {
                listaTipos.add(simularTipo(lex));
            }
        }
        
        verificarIncompatibilidad(simularTipo(varResultado), listaTipos);
    }
    
    /**
     * Genera tablas para expresión simple: variable = valor
     */
    private static void generarTablasSimple(String variable, String valor) {
        salida.append("\n### PILA RID (RAÍZ - IZQUIERDA - DERECHA)\n");
        salida.append(String.format("%-8s\t%-12s\t%-12s\t%s\n", "Pos", "RAÍZ", "IZQUIERDA", "DERECHA"));
        salida.append(String.format("%-8s\t%-12s\t%-12s\t%s\n", "---", "----", "---------", "---------"));
        salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 1, "=", variable, valor));
        
        salida.append("\n### PILA IDR (IZQUIERDA - DERECHA - RAÍZ)\n");
        salida.append(String.format("%-8s\t%-12s\t%-12s\t%s\n", "Pos", "IZQUIERDA", "DERECHA", "RAÍZ"));
        salida.append(String.format("%-8s\t%-12s\t%-12s\t%s\n", "---", "---------", "---------", "----"));
        salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 1, variable, valor, "="));
    }
    
    /**
     * Genera tablas para expresión con un operador: variable = A op B
     */
    private static void generarTablasUnOperador(String variable, String op1, String operador, String op2) {
        // PILA RID
        salida.append("\n### PILA RID (RAÍZ - IZQUIERDA - DERECHA)\n");
        salida.append(String.format("%-8s\t%-12s\t%-12s\t%s\n", "Pos", "RAÍZ", "IZQUIERDA", "DERECHA"));
        salida.append(String.format("%-8s\t%-12s\t%-12s\t%s\n", "---", "----", "---------", "---------"));
        salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 1, "=", variable, op1 + " " + operador + " " + op2));
        salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 2, operador, op1, op2));
        
        // PILA IDR
        salida.append("\n### PILA IDR (IZQUIERDA - DERECHA - RAÍZ)\n");
        salida.append(String.format("%-8s\t%-12s\t%-12s\t%s\n", "Pos", "IZQUIERDA", "DERECHA", "RAÍZ"));
        salida.append(String.format("%-8s\t%-12s\t%-12s\t%s\n", "---", "---------", "---------", "----"));
        salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 1, variable, op1 + " " + operador + " " + op2, "="));
        salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 2, op1, op2, operador));
    }
    
    /**
     * Genera tablas para expresión con dos operadores: variable = A op1 B op2 C
     * Ejemplo: resultado = x + y * 5
     */
    private static void generarTablasDosOperadores(String variable, String A, String B, String C, 
                                                  String op1, String op2, String expresionCompleta) {
        // Determinar precedencia (op2 tiene mayor precedencia si es * o /)
        boolean op2Primero = op2.equals("*") || op2.equals("/") || op2.equals("^");
        
        // PILA RID
        salida.append("\n### PILA RID (RAÍZ - IZQUIERDA - DERECHA)\n");
        salida.append(String.format("%-8s\t%-12s\t%-12s\t%s\n", "Pos", "RAÍZ", "IZQUIERDA", "DERECHA"));
        salida.append(String.format("%-8s\t%-12s\t%-12s\t%s\n", "---", "----", "---------", "---------"));
        
        if (op2Primero) {
            // y * 5 primero, luego x + resultado
            salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 1, "=", variable, expresionCompleta));
            salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 2, op2, B, C));
            salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 3, op1, A, "(" + B + op2 + C + ")"));
        } else {
            // x + y primero, luego resultado * 5
            salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 1, "=", variable, expresionCompleta));
            salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 2, op1, A, B));
            salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 3, op2, "(" + A + op1 + B + ")", C));
        }
        
        // PILA IDR
        salida.append("\n### PILA IDR (IZQUIERDA - DERECHA - RAÍZ)\n");
        salida.append(String.format("%-8s\t%-12s\t%-12s\t%s\n", "Pos", "IZQUIERDA", "DERECHA", "RAÍZ"));
        salida.append(String.format("%-8s\t%-12s\t%-12s\t%s\n", "---", "---------", "---------", "----"));
        
        if (op2Primero) {
            salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 1, variable, expresionCompleta, "="));
            salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 2, B, C, op2));
            salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 3, A, "(" + B + op2 + C + ")", op1));
        } else {
            salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 1, variable, expresionCompleta, "="));
            salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 2, A, B, op1));
            salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 3, "(" + A + op1 + B + ")", C, op2));
        }
        
        // SE ELIMINÓ EL ÁRBOL JERÁRQUICO DE AQUÍ
    }
    
    /**
     * Genera tablas para expresión general
     */
    private static void generarTablasGeneral(String variable, ArrayList<Lexema> tokens, String expresion) {
        // PILA RID - Siempre empieza con =
        salida.append("\n### PILA RID (RAÍZ - IZQUIERDA - DERECHA)\n");
        salida.append(String.format("%-8s\t%-12s\t%-12s\t%s\n", "Pos", "RAÍZ", "IZQUIERDA", "DERECHA"));
        salida.append(String.format("%-8s\t%-12s\t%-12s\t%s\n", "---", "----", "---------", "---------"));
        
        int pos = 1;
        salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", pos++, "=", variable, expresion));
        
        // Agregar operadores encontrados
        for (Lexema lex : tokens) {
            if (esOperador(lex)) {
                salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 
                    pos++, lex.getElemento(), "---", "---"));
            }
        }
        
        // PILA IDR
        salida.append("\n### PILA IDR (IZQUIERDA - DERECHA - RAÍZ)\n");
        salida.append(String.format("%-8s\t%-12s\t%-12s\t%s\n", "Pos", "IZQUIERDA", "DERECHA", "RAÍZ"));
        salida.append(String.format("%-8s\t%-12s\t%-12s\t%s\n", "---", "---------", "---------", "----"));
        
        pos = 1;
        salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", pos++, variable, expresion, "="));
        
        for (Lexema lex : tokens) {
            if (esOperador(lex)) {
                salida.append(String.format("%-8d\t%-12s\t%-12s\t%s\n", 
                    pos++, "---", "---", lex.getElemento()));
            }
        }
    }
    
    /**
     * Verifica si un lexema es un operador
     */
    private static boolean esOperador(Lexema lex) {
        String tipo = lex.getTipoToken();
        String elemento = lex.getElemento();
        return tipo.equals("OPA") || tipo.equals("OPM") || 
               elemento.equals("+") || elemento.equals("-") || 
               elemento.equals("*") || elemento.equals("/") || 
               elemento.equals("^") || elemento.equals("=");
    }
    
    /**
     * Simula la obtención de un tipo de dato
     */
    private static String simularTipo(Lexema lex) {
        String elemento = lex.getElemento().toLowerCase();
        
        if (lex.getTipoToken().equals("N")) {
            if (elemento.contains(".")) return "double";
            if (elemento.matches("\\d+")) return "int";
        }
        if (lex.getTipoToken().equals("C")) {
            return "String";
        }
        if (lex.getTipoToken().equals("Id")) {
            if (elemento.startsWith("i") || elemento.contains("contador") || elemento.contains("entero")) return "int";
            if (elemento.startsWith("f") || elemento.contains("decimal") || elemento.contains("flotante")) return "double";
            if (elemento.startsWith("b") || elemento.contains("flag") || elemento.contains("booleano")) return "boolean";
            if (elemento.contains("char") || elemento.contains("letra")) return "char"; 
            if (elemento.contains("cadena")) return "String";
            return "Object";
        }
        return "desconocido";
    }
    
    /**
     * Verifica incompatibilidad de tipos
     */
    private static void verificarIncompatibilidad(String tipoResultado, ArrayList<String> listaTiposExpresion) {
        boolean incompatible = false;
        String razon = "No se encontraron incompatibilidades obvias. Análisis Semántico correcto.";
        
        if (listaTiposExpresion.isEmpty()) {
            incompatible = false;
        } else {
            for (String tipoExp : listaTiposExpresion) {
                if (tipoResultado.equals("int") && tipoExp.equals("double")) {
                    incompatible = true;
                    razon = "Se está intentando asignar un valor de tipo **'double'** a una variable de tipo **'int'** (Pérdida de precisión).";
                    break;
                }
                if (tipoResultado.equals("boolean") && !tipoExp.equals("boolean")) {
                    incompatible = true;
                    razon = "Se está intentando asignar un valor no booleano a una variable de tipo **'boolean'**.";
                    break;
                }
                if (tipoResultado.equals("int") && tipoExp.equals("String")) {
                    incompatible = true;
                    razon = "Se está intentando asignar un valor de tipo **'String'** a una variable de tipo **'int'**.";
                    break;
                }
                if (tipoResultado.equals("double") && tipoExp.equals("String")) {
                    incompatible = true;
                    razon = "Se está intentando asignar un valor de tipo **'String'** a una variable de tipo **'double'**.";
                    break;
                }
            }
        }
        
        salida.append("\n--- RESULTADO DEL ANÁLISIS DE TIPOS ---\n");
        
        if (incompatible) {
            salida.append("❌ ¡ERROR SEMÁNTICO! **POSIBLE INCOMPATIBILIDAD DE VARIABLE CON EL RESULTADO.**\n");
            salida.append("Razón: " + razon + "\n");
        } else {
            salida.append("✅ Análisis Semántico Correcto.\n");
            salida.append("Razón: " + razon + "\n");
        }
    }
}