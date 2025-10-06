package edu.itz.proyectofinal.sintaxis;

import edu.itz.proyectofinal.control.Control;
import edu.itz.proyectofinal.lexemas.Lexema;
import edu.itz.proyectofinal.vistas.Ventana;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JTextArea;

public class AnaSintaxis {
    private static ArrayList<Lexema> lexes;
    private static int pos = 0;
    private static JTextArea salida;
    private static int errores = 0;
    private static final int MAX_ERRORES = 100;
    
    // Tabla de transiciones (nuestra tabla de 1s)
    private static final int[][] TABLA_SEGUIDORES = {
        // PR,  D, ID,  M,  C,  A, AM,  P,  T,  N,OPB,OPA,OPL,OPN,OPR
        {   1,  1,  1,  1,  1,  0,  1,  1,  0,  0,  0,  0,  0,  0,  0}, // PR
        {   1,  1,  1,  1,  1,  1,  1,  0,  1,  1,  0,  1,  0,  0,  0}, // D
        {   1,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  0,  0,  0}, // ID
        {   0,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0}, // M
        {   0,  1,  0,  0,  0,  0,  1,  0,  0,  0,  0,  0,  0,  0,  0}, // C
        {   0,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0}, // A
        {   0,  1,  0,  0,  0,  0,  1,  0,  0,  0,  0,  1,  0,  0,  1}, // AM
        {   0,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0}, // P
        {   0,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0}, // T
        {   0,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  0,  0,  1}, // N
        {   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0}, // OPB
        {   1,  1,  0,  0,  1,  0,  1,  0,  0,  1,  0,  1,  0,  0,  0}, // OPA
        {   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0}, // OPL
        {   0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0}, // OPN
        {   0,  0,  0,  0,  0,  0,  1,  0,  0,  1,  0,  0,  0,  0,  0}  // OPR
    };
    
    // Índices para los tipos de token
    private static final int PR = 0, D = 1, ID = 2, M = 3, C = 4, A = 5, 
                           AM = 6, P = 7, T = 8, N = 9, OPB = 10, OPA = 11, 
                           OPL = 12, OPN = 13, OPR = 14;

    public static void programa(Ventana ventana) {
        salida = ventana.getTxtSalida();
        salida.append(""); // No limpiar, solo asegurar que esté listo
        lexes = Control.getTokensAnalizados();
        pos = 0;
        errores = 0;
        
        if (lexes.isEmpty()) {
            error("No hay tokens para analizar", 1);
            return;
        }
        
        salida.append("\n=== ANÁLISIS SINTÁCTICO ===\n");
        salida.append("Iniciando análisis sintáctico...\n");
        
        // Análisis basado en la tabla de tran
        analizarSecuenciaTokens();
        
        // Verificaciones específicas para Java
        verificarEstructuraJava();
        
        if (errores == 0) {
            salida.append("\n✓ Análisis sintáctico completado SIN ERRORES.\n");
        } else {
            salida.append("\n✗ Análisis completado con " + errores + " errores.\n");
        }
    }
    
    private static void analizarSecuenciaTokens() {
        if (lexes.size() < 2) {
            error("Secuencia de tokens demasiado corta para análisis", 1);
            return;
        }
        
        salida.append("\n--- VERIFICANDO SECUENCIAS DE TOKENS ---\n");
        
        // Lista para evitar errores duplicados en la misma posición
        Set<Integer> lineasConError = new HashSet<>();
        
        // Verificar secuencia token por token usando la tabla
        for (int i = 0; i < lexes.size() - 1; i++) {
            Lexema tokenActual = lexes.get(i);
            Lexema tokenSiguiente = lexes.get(i + 1);
            
            
            if (lineasConError.contains(tokenActual.getLinea())) {
                continue;
            }
            
            int tipoActual = obtenerIndiceTipo(tokenActual.getTipoToken());
            int tipoSiguiente = obtenerIndiceTipo(tokenSiguiente.getTipoToken());
            
            if (tipoActual == -1) {
                error("Tipo de token desconocido: '" + tokenActual.getTipoToken() + "'", tokenActual.getLinea());
                lineasConError.add(tokenActual.getLinea());
                continue;
            }
            
            if (tipoSiguiente == -1) {
                error("Tipo de token desconocido: '" + tokenSiguiente.getTipoToken() + "'", tokenSiguiente.getLinea());
                lineasConError.add(tokenSiguiente.getLinea());
                continue;
            }
            
            // verif en la tabla si el token siguiente puede ir después del actual
            if (TABLA_SEGUIDORES[tipoActual][tipoSiguiente] == 0) {
                // Solo si no es una combinación válida de delimitadores
                if (!esCombinacionValidaEspecial(tokenActual, tokenSiguiente)) {
                    error("Secuencia no permitida: '" + tokenActual.getElemento() + "' (" + 
                          tokenActual.getTipoToken() + ") no puede ir seguido de '" + 
                          tokenSiguiente.getElemento() + "' (" + tokenSiguiente.getTipoToken() + ")", 
                          tokenActual.getLinea());
                    lineasConError.add(tokenActual.getLinea());
                }
            }
        }
    }
    
    private static boolean esCombinacionValidaEspecial(Lexema actual, Lexema siguiente) {
        // Algunas combinaciones que son válidas en Java a pesar de la tabla, aqui referimos a que la tabla puede perfeccionarse
        String elemActual = actual.getElemento();
        String elemSiguiente = siguiente.getElemento();
        String tipoActual = actual.getTipoToken();
        String tipoSiguiente = siguiente.getTipoToken();
        
        // Casos especiales válidos:
        
        // 1. ID seguido de OPR (==) en: "flag == true" - Esto es VÁLIDO en comparaciones
        if (tipoActual.equals("Id") && tipoSiguiente.equals("OPR")) {
            return true; // "flag == true" es válido
        }
        
        // 2. AM seguido de OPR (==) en: "flag == true" - También válido
        if (tipoActual.equals("AM") && tipoSiguiente.equals("OPR")) {
            return true; // "flag == true" es válido
        }
        
        // 3. Combinaciones de delimitadores para arrays y parámetros
        if (elemActual.equals("[") && elemSiguiente.equals("]")) return true;
        if (elemActual.equals("(") && elemSiguiente.equals(")")) return true;
        if (elemActual.equals("{") && elemSiguiente.equals("}")) return true;
        
        // 4. Punto para acceso a miembros: "System.out"
        if (elemActual.equals(".") && (tipoSiguiente.equals("Id") || tipoSiguiente.equals("A") || tipoSiguiente.equals("M"))) {
            return true;
        }
        
        return false;
    }
    
    private static void verificarEstructuraJava() {
        salida.append("\n--- VERIFICANDO ESTRUCTURA JAVA ---\n");
        
        // Verificar que comience con package o import o class
        verificarInicioPrograma();
        
        // Buscar estructura básica de clase Java
        buscarClaseJava();
        
        // Verificaciones específicas de errores comunes
        verificarErroresComunes();
    }
    
    private static void verificarInicioPrograma() {
        if (lexes.size() > 0) {
            Lexema primerToken = lexes.get(0);
            boolean inicioValido = false;
            
            // verificar si el primer token es "package" 
            if (primerToken.getElemento().equals("package") && primerToken.getTipoToken().equals("PR")) {
                inicioValido = true;
                salida.append("✓ Inicio con package\n");
            } 
            // si el segundo token es "package" (puede haber comentarios antes)
            else if (lexes.size() > 1 && lexes.get(1).getElemento().equals("package") && lexes.get(1).getTipoToken().equals("PR")) {
                inicioValido = true;
                salida.append("✓ Inicio con package\n");
            }
            else if (primerToken.getElemento().equals("import") && primerToken.getTipoToken().equals("PR")) {
                inicioValido = true;
                salida.append("✓ Inicio con import\n");
            } else if (primerToken.getElemento().equals("class") && primerToken.getTipoToken().equals("PR")) {
                inicioValido = true;
                salida.append("✓ Inicio con class\n");
            } else if (primerToken.getElemento().equals("public") && primerToken.getTipoToken().equals("PR")) {
                // Verificar si es "public class"
                if (lexes.size() > 1 && lexes.get(1).getElemento().equals("class") && lexes.get(1).getTipoToken().equals("PR")) {
                    inicioValido = true;
                    salida.append("✓ Inicio con public class\n");
                }
            }
            
            if (!inicioValido) {
                error("El programa debe comenzar con package, import o class", primerToken.getLinea());
            }
        }
    }
    
    private static void buscarClaseJava() {
        boolean encontreClass = false;
        
        for (int i = 0; i < lexes.size(); i++) {
            Lexema token = lexes.get(i);
            if (token.getElemento().equals("class") && token.getTipoToken().equals("PR")) {
                encontreClass = true;
                
                // Verificar estructura después de class
                if (i + 1 < lexes.size()) {
                    Lexema siguiente = lexes.get(i + 1);
                    if (siguiente.getTipoToken().equals("Id") || siguiente.getTipoToken().equals("C")) {
                        salida.append("✓ Declaración de clase válida en línea " + token.getLinea() + "\n");
                    } else {
                        error("Se esperaba identificador de clase después de 'class'", token.getLinea());
                    }
                } else {
                    error("Declaración de clase incompleta", token.getLinea());
                }
                break;
            }
        }
        
        if (!encontreClass) {
            error("No se encontró declaración de clase en el programa", 1);
        }
        
        // Verificar método main
        verificarMetodoMain();
        
        // Verificar cantidad de llaves o bien llaves balanceadas
        verificarLlavesBalanceadas();
    }
    
    private static void verificarMetodoMain() {
        boolean encontreMain = false;
        
        for (int i = 0; i < lexes.size() - 6; i++) {
            if (lexes.get(i).getElemento().equals("public") && lexes.get(i).getTipoToken().equals("PR") &&
                lexes.get(i + 1).getElemento().equals("static") && lexes.get(i + 1).getTipoToken().equals("PR") &&
                lexes.get(i + 2).getElemento().equals("void") && lexes.get(i + 2).getTipoToken().equals("PR") &&
                lexes.get(i + 3).getElemento().equals("main") && lexes.get(i + 3).getTipoToken().equals("M")) {
                
                encontreMain = true;
                salida.append("✓ Método main encontrado en línea " + lexes.get(i).getLinea() + "\n");
                
                // Verificar paréntesis de main
                if (i + 4 < lexes.size() && lexes.get(i + 4).getElemento().equals("(")) {
                    salida.append("✓ Sintaxis de método main correcta\n");
                } else {
                    error("Se esperaba '(' después de 'main'", lexes.get(i + 3).getLinea());
                }
                break;
            }
        }
        
        if (!encontreMain) {
            salida.append("⚠ Advertencia: No se encontró método main (opcional)\n");
        }
    }
    
    private static void verificarLlavesBalanceadas() {
        int balance = 0;
        int maxBalance = 0;
        
        for (Lexema lexema : lexes) {
            if (lexema.getElemento().equals("{")) {
                balance++;
                maxBalance = Math.max(maxBalance, balance);
            } else if (lexema.getElemento().equals("}")) {
                balance--;
                if (balance < 0) {
                    error("Llave de cierre '}' sin llave de apertura correspondiente", lexema.getLinea());
                    balance = 0;
                }
            }
        }
        
        if (balance > 0) {
            error("Faltan " + balance + " llave(s) de cierre '}'", lexes.get(lexes.size() - 1).getLinea());
        } else if (balance == 0) {
            salida.append("✓ Llaves correctamente balanceadas (máxima anidación: " + maxBalance + ")\n");
        }
    }
    
    private static void verificarErroresComunes() {
        salida.append("\n--- VERIFICANDO ERRORES COMUNES ---\n");
        
        // Lista para evitar errores duplicados en la misma posición
        Set<Integer> lineasConErrorComun = new HashSet<>();
        
        // Solo verificar errores que NO son detectados por la tabla de seguimiento
        
        // Buscar el error específico: D (;) seguido de OPA (=) - Este NO está en la tabla
        for (int i = 0; i < lexes.size() - 1; i++) {
            Lexema tokenActual = lexes.get(i);
            
            // Si ya reportamos un error común en esta línea, continuar
            if (lineasConErrorComun.contains(tokenActual.getLinea())) {
                continue;
            }
            
            if (tokenActual.getElemento().equals(";") && tokenActual.getTipoToken().equals("D") &&
                lexes.get(i + 1).getElemento().equals("=") && lexes.get(i + 1).getTipoToken().equals("OPA")) {
                error("Punto y coma ';' no puede ir seguido de operador de asignación '='", tokenActual.getLinea());
                lineasConErrorComun.add(tokenActual.getLinea());
            }
        }
        
        //buscar ID seguido de OPR (==) - Este SÍ está en la tabla como 0, pero necesitamos detectarlo específicamente
        for (int i = 0; i < lexes.size() - 1; i++) {
            Lexema tokenActual = lexes.get(i);
            
            if (lineasConErrorComun.contains(tokenActual.getLinea())) {
                continue;
            }
            
            //  ID → OPR (==) que según la tabla es 0 (no permitido)
            if (tokenActual.getTipoToken().equals("Id") && 
                lexes.get(i + 1).getTipoToken().equals("OPR")) {
                error("Identificador '" + tokenActual.getElemento() + "' no puede ir seguido de operador relacional '" + 
                      lexes.get(i + 1).getElemento() + "' - falta operador de asignación '='", tokenActual.getLinea());
                lineasConErrorComun.add(tokenActual.getLinea());
            }
        }
        
        //buscar AM seguido de OPR (==) - Este SÍ está en la tabla como 1 (PERMITIDO), pero queremos detectarlo como error
        for (int i = 0; i < lexes.size() - 1; i++) {
            Lexema tokenActual = lexes.get(i);
            
            if (lineasConErrorComun.contains(tokenActual.getLinea())) {
                continue;
            }
            
            //AM → OPR (==) que según la tabla es 1 (permitido), pero queremos detectarlo como error
            if (tokenActual.getTipoToken().equals("AM") && 
                lexes.get(i + 1).getTipoToken().equals("OPR")) {
                error("Argumento de método '" + tokenActual.getElemento() + "' no puede ir seguido de operador relacional '" + 
                      lexes.get(i + 1).getElemento() + "' - falta operador de asignación '='", tokenActual.getLinea());
                lineasConErrorComun.add(tokenActual.getLinea());
            }
        }
        
        // buscar asignaciones sin valor: "variable = ;" - Este NO está en la tabla
        for (int i = 0; i < lexes.size() - 2; i++) {
            Lexema tokenActual = lexes.get(i);
            
            if (lineasConErrorComun.contains(tokenActual.getLinea())) {
                continue;
            }
            
            if (tokenActual.getElemento().equals("=") && tokenActual.getTipoToken().equals("OPA") &&
                lexes.get(i + 1).getElemento().equals(";") && lexes.get(i + 1).getTipoToken().equals("D")) {
                error("Operador de asignación '=' no puede ir seguido de ';' - falta valor", tokenActual.getLinea());
                lineasConErrorComun.add(tokenActual.getLinea());
            }
        }
    }
    
    private static int obtenerIndiceTipo(String tipoToken) {
        switch (tipoToken) {
            case "PR": return PR;
            case "D": return D;
            case "Id": return ID;
            case "M": return M;
            case "C": return C;
            case "A": return A;
            case "AM": return AM;
            case "P": return P;
            case "T": return T;
            case "N": return N;
            case "OPB": return OPB;
            case "OPA": return OPA;
            case "OPL": return OPL;
            case "OPN": return OPN;
            case "OPR": return OPR;
            default: return -1;
        }
    }
    
    private static void error(String msg, int linea) {
        errores++;
        if (errores > MAX_ERRORES) {
            salida.append("\n✗ DEMASIADOS ERRORES. Análisis abortado.\n");
            throw new RuntimeException("Demasiados errores sintácticos");
        }
        
        salida.append("ERROR SINTÁCTICO [" + errores + "] en línea " + linea + ": " + msg + "\n");
        
        // Mostrar contexto del error
        mostrarContextoError(linea);
    }
    
    private static void mostrarContextoError(int lineaError) {
        StringBuilder contexto = new StringBuilder("    Contexto: ");
        int tokensEnLinea = 0;
        
        for (Lexema lexema : lexes) {
            if (lexema.getLinea() == lineaError) {
                contexto.append(lexema.getElemento()).append(" ");
                tokensEnLinea++;
            }
        }
        
        if (tokensEnLinea > 0) {
            salida.append(contexto.toString());
            salida.append("\n");
        }
    }
    
    // Método adicional para mostrar la tabla de seguimiento (opcional, para depuración)
    public static void mostrarTablaSeguimiento(Ventana ventana) {
        salida = ventana.getTxtSalida();
        salida.append("\n=== TABLA DE SEGUIMIENTO SINTÁCTICO ===\n\n");
        
        String[] encabezados = {"PR", "D", "Id", "M", "C", "A", "AM", "P", "T", "N", "OPB", "OPA", "OPL", "OPN", "OPR"};
        
        salida.append("     ");
        for (String encabezado : encabezados) {
            salida.append(String.format("%-4s", encabezado));
        }
        salida.append("\n");
        
        for (int i = 0; i < TABLA_SEGUIDORES.length; i++) {
            salida.append(String.format("%-4s ", encabezados[i]));
            for (int j = 0; j < TABLA_SEGUIDORES[i].length; j++) {
                salida.append(String.format("%-4d", TABLA_SEGUIDORES[i][j]));
            }
            salida.append("\n");
        }
        
        salida.append("\nLeyenda: 1 = Secuencia permitida, 0 = Secuencia no permitida\n");
    }
}