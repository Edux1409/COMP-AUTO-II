package edu.itz.proyectofinal.control;

import edu.itz.proyectofinal.lexemas.Lexema;
import edu.itz.proyectofinal.vistas.Ventana;
import edu.itz.proyectofinal.semantico.AnaSemantico;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

public class Control {
    Ventana v;
    private static ArrayList<Lexema> tokensAnalizados = new ArrayList<>();
    private static ArrayList<String[]> tablaSimbolos = new ArrayList<>();
    private int lecturas = 0;
    private int totalTokens = 0;
    private int delimitadores = 0;
    private int caracteresDesechados = 0;
    private Map<String, Integer> contadorTiposTokens = new HashMap<>();
    private ArrayList<String> errores = new ArrayList<>();

    public Control(Ventana v) {
        this.v = v;
        inicializarContadores();
    }

    private void inicializarContadores() {
        contadorTiposTokens.put("PR", 0);
        contadorTiposTokens.put("D", 0);
        contadorTiposTokens.put("Id", 0);
        contadorTiposTokens.put("M", 0);
        contadorTiposTokens.put("C", 0);
        contadorTiposTokens.put("A", 0);
        contadorTiposTokens.put("AM", 0);
        contadorTiposTokens.put("P", 0);
        contadorTiposTokens.put("T", 0);
        contadorTiposTokens.put("N", 0);
        contadorTiposTokens.put("OPB", 0);
        contadorTiposTokens.put("OPA", 0);
        contadorTiposTokens.put("OPL", 0);
        contadorTiposTokens.put("OPN", 0);
        contadorTiposTokens.put("OPR", 0);
        contadorTiposTokens.put("ERROR", 0);
    }

    public void abrirArchivo() {
        limpiar();
        String path = null;
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(v);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            path = fileChooser.getSelectedFile().getAbsolutePath();
            v.getLblArchivo().setText(path);
        }
        if (path == null) {
            JOptionPane.showMessageDialog(v, "NO SELECCIONÓ NINGÚN ARCHIVO");
            return;
        }
        leerArchivo(path);
    }

    public void limpiar() {
        v.getTxtContenido().setText("");
        v.getLblArchivo().setText("");
        v.getTxtSalida().setText("");
        tokensAnalizados.clear();
        tablaSimbolos.clear();
        lecturas = 0;
        totalTokens = 0;
        delimitadores = 0;
        caracteresDesechados = 0;
        errores.clear();
        inicializarContadores();
    }

    public void leerArchivo(String archivo) {
        String texto = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(archivo));
            String linea;
            while ((linea = br.readLine()) != null) {
                texto += linea + "\n";
            }
            br.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        v.getTxtContenido().append(texto + "\n");
    }

    // Palabras reservadas
    private static final Set<String> PALABRAS_RESERVADAS = new HashSet<>();
    static {
        String[] palabras = {
            "abstract", "continue", "default", "do", "boolean", "double", "byte", "else",
            "case", "enum", "catch", "extends", "char", "final", "class", "finally",
            "const", "float", "for", "goto", "if", "implements", "import", "instanceof",
            "int", "interface", "long", "native", "new", "package", "private", "protected",
            "public", "return", "short", "static", "strictfp", "super", "switch",
            "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"
        };
        for (String palabra : palabras) {
            PALABRAS_RESERVADAS.add(palabra);
        }
    }

    // Métodos comunes de Java
    private static final Set<String> METODOS = new HashSet<>();
    static {
        String[] metodos = {
            "println", "print", "nextInt", "nextDouble", "nextLine", "next", "nextFloat",
            "nextLong", "nextShort", "nextByte", "nextBoolean", "equals", "length",
            "charAt", "substring", "indexOf", "toLowerCase", "toUpperCase", "trim",
            "valueOf", "parseInt", "parseDouble", "parseFloat", "parseLong", "parseShort",
            "parseByte", "parseBoolean", "toString", "toCharArray", "getBytes", "split",
            "replace", "replaceAll", "replaceFirst", "matches", "contains", "startsWith",
            "endsWith", "compareTo", "compareToIgnoreCase", "format", "join", "isEmpty",
            "isBlank", "concat", "intern", "hashCode", "getClass", "notify", "notifyAll",
            "wait", "finalize", "clone", "run", "start", "sleep", "join", "interrupt",
            "isAlive", "setPriority", "getPriority", "yield", "setName", "getName",
            "currentThread", "activeCount", "isDaemon", "setDaemon", "checkAccess",
            "countStackFrames", "destroy", "resume", "stop", "suspend", "getState",
            "getThreadGroup", "getAllStackTraces", "getDefaultUncaughtExceptionHandler",
            "setDefaultUncaughtExceptionHandler", "getUncaughtExceptionHandler",
            "setUncaughtExceptionHandler", "getId", "getContextClassLoader",
            "setContextClassLoader", "getStackTrace", "getAllStackTraces", "main"
        };
        for (String metodo : metodos) {
            METODOS.add(metodo);
        }
    }

    // Clases comunes de Java
    private static final Set<String> CLASES = new HashSet<>();
    static {
        String[] clases = {
            "Scanner", "System", "String", "Integer", "Double", "Float", "Long", "Short",
            "Byte", "Boolean", "Character", "Math", "Arrays", "ArrayList", "LinkedList",
            "HashMap", "HashSet", "TreeMap", "TreeSet", "Vector", "Stack", "Queue",
            "PriorityQueue", "Deque", "ArrayDeque", "LinkedHashMap", "LinkedHashSet",
            "Collections", "Objects", "Optional", "StringBuilder", "StringBuffer",
            "LocalDate", "LocalTime", "LocalDateTime", "DateTimeFormatter", "Pattern",
            "Matcher", "BigInteger", "BigDecimal", "Thread", "Runnable", "Callable",
            "Future", "ExecutorService", "Executors", "File", "FileReader", "FileWriter",
            "BufferedReader", "BufferedWriter", "InputStream", "OutputStream",
            "FileInputStream", "FileOutputStream", "ObjectInputStream", "ObjectOutputStream",
            "Serializable", "Comparator", "Comparable", "Iterable", "Iterator",
            "ListIterator", "Enumeration", "Map", "List", "Set", "Collection",
            "Number", "Exception", "RuntimeException", "IOException", "NullPointerException",
            "ArrayIndexOutOfBoundsException", "IllegalArgumentException", "IllegalStateException",
            "ClassCastException", "UnsupportedOperationException", "ArithmeticException",
            "NumberFormatException", "SecurityException", "InterruptedException",
            "CloneNotSupportedException", "NoSuchMethodException", "NoSuchFieldException",
            "InstantiationException", "IllegalAccessException", "InvocationTargetException",
            "Error", "OutOfMemoryError", "StackOverflowError", "VirtualMachineError",
            "AssertionError", "NoClassDefFoundError", "ClassNotFoundException",
            "LinkageError", "ExceptionInInitializerError", "UnsatisfiedLinkError",
            "NoSuchFieldError", "NoSuchMethodError", "AbstractMethodError",
            "IncompatibleClassChangeError", "VerifyError", "ThreadDeath"
        };
        for (String clase : clases) {
            CLASES.add(clase);
        }
    }

    // Atributos comunes
    private static final Set<String> ATRIBUTOS = new HashSet<>();
    static {
        String[] atributos = {
            "out", "in", "err", "length", "MAX_VALUE", "MIN_VALUE", "SIZE", "BYTES",
            "TYPE", "NaN", "POSITIVE_INFINITY", "NEGATIVE_INFINITY", "MAX_EXPONENT",
            "MIN_EXPONENT", "MAX_RADIX", "MIN_RADIX", "DIGITS", "LETTERS", "LOWERCASE_LETTERS",
            "UPPERCASE_LETTERS", "TITLECASE_LETTERS", "MODIFIER_LETTERS", "OTHER_LETTERS",
            "NON_SPACING_MARKS", "ENCLOSING_MARKS", "COMBINING_SPACING_MARKS", "DECIMAL_DIGIT_NUMBERS",
            "LETTER_NUMBERS", "OTHER_NUMBERS", "SPACE_SEPARATORS", "LINE_SEPARATORS",
            "PARAGRAPH_SEPARATORS", "CONTROL", "FORMAT", "PRIVATE_USE", "SURROGATE",
            "DASH_PUNCTUATION", "START_PUNCTUATION", "END_PUNCTUATION", "CONNECTOR_PUNCTUATION",
            "OTHER_PUNCTUATION", "MATH_SYMBOLS", "CURRENCY_SYMBOLS", "MODIFIER_SYMBOLS",
            "OTHER_SYMBOLS", "INITIAL_QUOTE_PUNCTUATION", "FINAL_QUOTE_PUNCTUATION",
            "DIRECTIONALITY", "UNASSIGNED", "UPPERCASE", "LOWERCASE", "TITLECASE",
            "WHITESPACE", "CONTROL", "PUNCTUATION", "HEX_DIGITS", "ALPHABETIC", "IDEAGRAPHIC",
            "LETTER", "ASCII", "ALNUM", "SPACE", "PRINT", "GRAPH", "BLANK", "CNTRL",
            "XDIGIT", "WORD", "MAX_PRIORITY", "MIN_PRIORITY", "NORM_PRIORITY"
        };
        for (String atributo : atributos) {
            ATRIBUTOS.add(atributo);
        }
    }

    // Operadores
    private static final Map<String, String> OPERADORES = new HashMap<>();
    static {
        // Operadores aritméticos
        OPERADORES.put("+", "OPA");
        OPERADORES.put("-", "OPA");
        OPERADORES.put("*", "OPA");
        OPERADORES.put("/", "OPA");
        OPERADORES.put("%", "OPA");
        OPERADORES.put("++", "OPA");
        OPERADORES.put("--", "OPA");
        OPERADORES.put("=", "OPA");
        
        // Operadores relacionales
        OPERADORES.put("==", "OPR");
        OPERADORES.put("!=", "OPR");
        OPERADORES.put("<", "OPR");
        OPERADORES.put(">", "OPR");
        OPERADORES.put("<=", "OPR");
        OPERADORES.put(">=", "OPR");
        
        // Operadores lógicos
        OPERADORES.put("&&", "OPL");
        OPERADORES.put("||", "OPL");
        OPERADORES.put("!", "OPL");
        
        // Operadores a nivel de bits
        OPERADORES.put("&", "OPN");
        OPERADORES.put("|", "OPN");
        OPERADORES.put("^", "OPN");
        OPERADORES.put("~", "OPN");
        OPERADORES.put("<<", "OPN");
        OPERADORES.put(">>", "OPN");
        OPERADORES.put(">>>", "OPN");
        
        // Operadores booleanos
        OPERADORES.put("true", "OPB");
        OPERADORES.put("false", "OPB");
    }

    // Delimitadores (solo los que se muestran como tokens)
    private static final Set<String> DELIMITADORES_VISIBLES = new HashSet<>();
    static {
        String[] delimitadores = {
            "[", "]", "{", "}", "(", ")", ";", ",", ".", "\""
        };
        for (String del : delimitadores) {
            DELIMITADORES_VISIBLES.add(del);
        }
    }

    private int getTokenCode(String tipo) {
        switch (tipo) {
            case "PR": return 50;    // Palabras reservadas
            case "D": return 100;    // Delimitadores
            case "Id": return 150;   // Identificadores
            case "M": return 200;    // Métodos
            case "C": return 250;    // Clases
            case "A": return 300;    // Atributos
            case "AM": return 350;   // Argumentos de métodos
            case "P": return 400;    // Paquetes
            case "T": return 450;    // Texto (cadenas)
            case "N": return 500;    // Números
            case "OPB": return 550;  // Operadores booleanos
            case "OPA": return 600;  // Operadores aritméticos
            case "OPL": return 650;  // Operadores lógicos
            case "OPN": return 700;  // Operadores a nivel de bits
            case "OPR": return 750;  // Operadores relacionales
            case "ERROR": return 999; // Error léxico
            default: return 0;
        }
    }

    // MÉTODOS NUEVOS PARA DETECCIÓN DE ERRORES
    private boolean esCierreComentarioIncorrecto(String texto, int pos) {
        if (pos + 1 < texto.length()) {
            String dosCaracteres = texto.substring(pos, pos + 2);
            return dosCaracteres.equals("*/");
        }
        return false;
    }

    private boolean esPosibleErrorPalabraReservada(String identificador) {
        if (identificador.equals("Java") || identificador.equals("java")) {
            return true;
        }
        
        // Verificar similitudes con palabras reservadas
        for (String palabra : PALABRAS_RESERVADAS) {
            if (identificador.length() >= palabra.length() - 2 && 
                identificador.length() <= palabra.length() + 2) {
                if (identificador.startsWith(palabra.substring(0, Math.min(3, palabra.length()))) ||
                    identificador.endsWith(palabra.substring(Math.max(0, palabra.length() - 3)))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean esArgumentoMetodo(String identificador, String texto, int pos) {
        int parenAbierto = texto.lastIndexOf('(', pos);
        int parenCerrado = texto.indexOf(')', pos);
        
        if (parenAbierto != -1 && parenAbierto < pos && 
            (parenCerrado == -1 || pos < parenCerrado)) {
            return true;
        }
        return false;
    }

    // NUEVO MÉTODO: Detectar paquetes Java (java.util, java.io, etc.)
    private boolean esPaqueteJava(String texto, int pos) {
        if (pos + 4 < texto.length()) {
            String posibleJava = texto.substring(pos, Math.min(pos + 10, texto.length()));
            return posibleJava.startsWith("java.") || posibleJava.startsWith("javax.");
        }
        return false;
    }

    // NUEVO MÉTODO: Extraer paquete completo (hasta ; o delimitador)
    private String extraerPaqueteCompleto(String texto, int pos) {
        int inicio = pos;
        int fin = texto.length();
        
        for (int i = pos; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == ';' || c == '\n' || c == ' ' || c == '\t' || DELIMITADORES_VISIBLES.contains(String.valueOf(c))) {
                fin = i;
                break;
            }
        }
        
        return texto.substring(inicio, fin);
    }

    // VARIABLE GLOBAL para recordar el último tipo de variable encontrado
    private String ultimoTipoVariable = "S"; // Por defecto String
    
    // MÉTODO MEJORADO: Detección de variables de usuario
    private boolean esVariableDeUsuario(String identificador, String texto, int posActual) {
        // 1. Excluir identificadores que no son variables
        String[] exclusiones = {"tiz", "java", "util", "clas", "operaciones", "statci", 
                               "args", "leer", "close", "public", "private", 
                               "protected", "static", "void", "class", "package", "import",
                               "String", "System", "out", "in", "Scanner", "new"};
        
        for (String exclusion : exclusiones) {
            if (identificador.equals(exclusion)) {
                return false;
            }
        }
        
        // 2. Excluir si es posible error en palabra reservada
        if (esPosibleErrorPalabraReservada(identificador)) {
            return false;
        }
        
        // 3. Excluir si está en contexto de import/package/class
        int inicioLinea = texto.lastIndexOf('\n', posActual) + 1;
        if (inicioLinea < 0) inicioLinea = 0;
        int finLinea = texto.indexOf('\n', posActual);
        if (finLinea < 0) finLinea = texto.length();
        String linea = texto.substring(inicioLinea, Math.min(finLinea, texto.length())).trim();
        
        if (linea.startsWith("import ") || linea.startsWith("package ") || 
            linea.contains(" class ")) {
            return false;
        }
        
        // 4. Solo considerar variables con nombres razonables
        if (identificador.length() > 30) return false;
        
        // 5. Excluir si el identificador está en mayúsculas (posible constante)
        if (identificador.equals(identificador.toUpperCase()) && identificador.length() > 1) {
            return false;
        }
        
        // 6. Buscar patrones de declaración de variables - MEJORADO
        int contextoInicio = Math.max(0, posActual - 50);
        String contexto = texto.substring(contextoInicio, Math.min(posActual + identificador.length() + 20, texto.length()));
        
        // Patrones mejorados para detectar declaraciones de variables
        String[] tipos = {"int", "double", "float", "String", "char", "boolean", "long", "byte", "short"};
        for (String tipo : tipos) {
            // Buscar patrones como "int s", "String g", etc.
            if (contexto.contains(tipo + " " + identificador) || 
                contexto.contains(tipo + "\t" + identificador) ||
                contexto.contains(tipo + "\n" + identificador) ||
                contexto.contains(tipo + ";" + identificador)) {
                // Guardar el tipo encontrado para variables posteriores
                ultimoTipoVariable = determinarAbreviaturaTipo(tipo);
                return true;
            }
            
            // Buscar patrones con coma: "int x, r, t;"
            if (contexto.contains(tipo + " ") && contexto.contains("," + identificador)) {
                // Usar el último tipo encontrado para variables después de coma
                return true;
            }
        }
        
        // 7. Si es una sola letra, probablemente sea variable
        if (identificador.length() == 1 && Character.isLetter(identificador.charAt(0))) {
            return true;
        }
        
        // 8. Verificar si está después de un = (asignación)
        int posAnterior = Math.max(0, posActual - 1);
        String textoAnterior = texto.substring(Math.max(0, posAnterior - 10), posAnterior);
        if (textoAnterior.contains("=") && !textoAnterior.contains("==")) {
            return true;
        }
        
        // 9. Verificar si está en una lista de variables separadas por coma
        String contextoComas = texto.substring(Math.max(0, posActual - 10), Math.min(posActual + 10, texto.length()));
        if (contextoComas.contains("," + identificador) || contextoComas.contains(identificador + ",")) {
            // Si está después de una coma, usar el último tipo conocido
            return true;
        }
        
        return false;
    }

    // MÉTODO AUXILIAR: Convertir tipo completo a abreviatura
    private String determinarAbreviaturaTipo(String tipoCompleto) {
        switch (tipoCompleto) {
            case "int": return "I";
            case "double": return "D";
            case "float": return "F";
            case "String": return "S";
            case "char": return "C";
            case "boolean": return "B";
            case "long": return "L";
            case "byte": return "B";
            case "short": return "I";
            default: return "S";
        }
    }

    // MÉTODO MEJORADO: Determinar tipo de variable para tabla de símbolos
    private String determinarTipoVariable(String identificador, String texto, int posActual) {
        // Buscar el tipo de dato antes del identificador
        int inicioBusqueda = Math.max(0, posActual - 50);
        String contexto = texto.substring(inicioBusqueda, Math.min(posActual + 10, texto.length()));
        
        // Primero buscar declaración explícita
        if (contexto.contains("int " + identificador) || contexto.contains("int\t" + identificador) ||
            contexto.contains("int\n" + identificador) || contexto.contains("int;" + identificador)) {
            ultimoTipoVariable = "I";
            return "I";
        } else if (contexto.contains("double " + identificador) || contexto.contains("double\t" + identificador)) {
            ultimoTipoVariable = "D";
            return "D";
        } else if (contexto.contains("float " + identificador) || contexto.contains("float\t" + identificador)) {
            ultimoTipoVariable = "F";
            return "F";
        } else if (contexto.contains("String " + identificador) || contexto.contains("String\t" + identificador)) {
            ultimoTipoVariable = "S";
            return "S";
        } else if (contexto.contains("char " + identificador) || contexto.contains("char\t" + identificador)) {
            ultimoTipoVariable = "C";
            return "C";
        } else if (contexto.contains("boolean " + identificador) || contexto.contains("boolean\t" + identificador)) {
            ultimoTipoVariable = "B";
            return "B";
        } else if (contexto.contains("long " + identificador) || contexto.contains("long\t" + identificador)) {
            ultimoTipoVariable = "L";
            return "L";
        } else if (contexto.contains("byte " + identificador) || contexto.contains("byte\t" + identificador)) {
            ultimoTipoVariable = "B";
            return "B";
        } else if (contexto.contains("short " + identificador) || contexto.contains("short\t" + identificador)) {
            ultimoTipoVariable = "I";
            return "I";
        }
        
        // Si no encuentra declaración explícita, buscar si está después de una coma
        String contextoComas = texto.substring(Math.max(0, posActual - 20), Math.min(posActual + 5, texto.length()));
        if (contextoComas.contains("," + identificador) || 
            (contextoComas.contains(",") && contextoComas.indexOf(identificador) < contextoComas.indexOf(","))) {
            // Usar el último tipo conocido para variables después de coma
            return ultimoTipoVariable;
        }
        
        // Por defecto, usar el último tipo conocido
        return ultimoTipoVariable;
    }

    private int determinarDireccion(String tipo) {
        int tamano;
        switch (tipo) {
            case "C": tamano = 2; break;  // char
            case "S": tamano = 4; break;  // String (referencia)
            case "I": tamano = 4; break;  // int
            case "F": tamano = 4; break;  // float
            case "D": tamano = 8; break;  // double
            case "L": tamano = 8; break;  // long
            case "B": tamano = 1; break;  // byte
            default: tamano = 4; break;
        }
        
        if (tablaSimbolos.isEmpty()) {
            return 1;
        } else {
            String[] ultimo = tablaSimbolos.get(tablaSimbolos.size() - 1);
            int ultimaDir = Integer.parseInt(ultimo[2]);
            String ultimoTipo = ultimo[1];
            int ultimoTamano = determinarTamanoPorTipo(ultimoTipo);
            return ultimaDir + ultimoTamano;
        }
    }

    private int determinarTamanoPorTipo(String tipo) {
        switch (tipo) {
            case "C": return 2;
            case "S": return 4;
            case "I": return 4;
            case "F": return 4;
            case "D": return 8;
            case "L": return 8;
            case "B": return 1;
            default: return 4;
        }
    }

    private void agregarATablaSimbolos(String identificador, String tipo, int direccion, String valor, int linea) {
        for (String[] simbolo : tablaSimbolos) {
            if (simbolo[0].equals(identificador)) {
                return;
            }
        }
        
        String[] nuevoSimbolo = {identificador, tipo, String.valueOf(direccion), valor, String.valueOf(linea)};
        tablaSimbolos.add(nuevoSimbolo);
    }

    public void analizarTexto() {
        tokensAnalizados.clear();
        tablaSimbolos.clear();
        inicializarContadores();
        lecturas = 0;
        totalTokens = 0;
        delimitadores = 0;
        caracteresDesechados = 0;
        errores.clear();
        ultimoTipoVariable = "S"; // Reiniciar el último tipo

        String texto = v.getTxtContenido().getText();
        JTextArea salida = v.getTxtSalida();
        salida.setText("");

        int pos = 0;
        int lineaActual = 1;
        boolean enComentarioBloque = false;
        int inicioComentarioLinea = 1;

        while (pos < texto.length()) {
            lecturas++;
            char currentChar = texto.charAt(pos);

            // Manejo de saltos de línea
            if (currentChar == '\n') {
                lineaActual++;
                pos++;
                continue;
            }

            // Saltar espacios en blanco (NO se muestran como tokens)
            if (Character.isWhitespace(currentChar)) {
                if (currentChar == ' ' || currentChar == '\t') {
                    caracteresDesechados++;
                }
                pos++;
                continue;
            }

            // CORRECCIÓN: Manejo de comentarios DEBE IR ANTES de la detección de cierre incorrecto
            if (enComentarioBloque) {
                if (pos + 1 < texto.length() && texto.substring(pos, pos + 2).equals("*/")) {
                    enComentarioBloque = false;
                    pos += 2;
                    caracteresDesechados += 2;
                    continue;
                }
                
                if (pos >= texto.length() - 1) {
                    errores.add("ERROR: Comentario de bloque no cerrado. Iniciado en línea " + inicioComentarioLinea);
                    break;
                }
                
                pos++;
                caracteresDesechados++;
                continue;
            }

            // 1. Detectar cierre de comentario incorrecto (ERROR) - DESPUÉS de manejar comentarios abiertos
            if (esCierreComentarioIncorrecto(texto, pos)) {
                errores.add("ERROR LÉXICO: Cierre de comentario '*/' sin comentario abierto en línea " + lineaActual);
                tokensAnalizados.add(new Lexema("*/", "ERROR", 999, lineaActual));
                contadorTiposTokens.put("ERROR", contadorTiposTokens.get("ERROR") + 1);
                salida.append("*/" + "\tERROR\t999\tLínea: " + lineaActual + "\n");
                totalTokens++;
                pos += 2;
                continue;
            }

            if (pos + 1 < texto.length() && texto.substring(pos, pos + 2).equals("//")) {
                while (pos < texto.length() && texto.charAt(pos) != '\n') {
                    pos++;
                    caracteresDesechados++;
                }
                continue;
            }

            // CORRECCIÓN: Detección de comentarios de bloque /*comment*/
            if (pos + 1 < texto.length() && texto.substring(pos, pos + 2).equals("/*")) {
                enComentarioBloque = true;
                inicioComentarioLinea = lineaActual;
                pos += 2;
                caracteresDesechados += 2;
                continue;
            }

            String restante = texto.substring(pos);

            // 2. Paquetes (import statements)
            if (restante.startsWith("import ")) {
                tokensAnalizados.add(new Lexema("import", "PR", 50, lineaActual));
                contadorTiposTokens.put("PR", contadorTiposTokens.get("PR") + 1);
                salida.append("import\tPR\t50\tLínea: " + lineaActual + "\n");
                totalTokens++;
                pos += 6;
                
                while (pos < texto.length() && Character.isWhitespace(texto.charAt(pos))) {
                    caracteresDesechados++;
                    pos++;
                }
                
                int endImport = texto.indexOf(';', pos);
                if (endImport == -1) {
                    errores.add("ERROR: Import no terminado con ; en línea " + lineaActual);
                    endImport = texto.indexOf('\n', pos);
                    if (endImport == -1) endImport = texto.length() - 1;
                }
                
                String paquete = texto.substring(pos, endImport).trim();
                
                if (!paquete.matches("[a-zA-Z_][a-zA-Z0-9_.*]+")) {
                    errores.add("ERROR LÉXICO: Formato de paquete incorrecto: '" + paquete + "' en línea " + lineaActual);
                }
                
                tokensAnalizados.add(new Lexema(paquete, "P", 400, lineaActual));
                contadorTiposTokens.put("P", contadorTiposTokens.get("P") + 1);
                salida.append(paquete + "\tP\t400\tLínea: " + lineaActual + "\n");
                totalTokens++;
                
                if (endImport < texto.length() && texto.charAt(endImport) == ';') {
                    tokensAnalizados.add(new Lexema(";", "D", 100, lineaActual));
                    contadorTiposTokens.put("D", contadorTiposTokens.get("D") + 1);
                    salida.append(";\tD\t100\tLínea: " + lineaActual + "\n");
                    totalTokens++;
                    delimitadores++;
                    pos = endImport + 1;
                } else {
                    pos = endImport;
                }
                continue;
            }

            // 3. Paquetes Java (java.util, java.io, etc.)
            if (esPaqueteJava(texto, pos)) {
                String paquete = extraerPaqueteCompleto(texto, pos);
                tokensAnalizados.add(new Lexema(paquete, "P", 400, lineaActual));
                contadorTiposTokens.put("P", contadorTiposTokens.get("P") + 1);
                salida.append(paquete + "\tP\t400\tLínea: " + lineaActual + "\n");
                totalTokens++;
                pos += paquete.length();
                continue;
            }

            // 4. Cadenas de texto (entre comillas)
            if (currentChar == '"') {
                tokensAnalizados.add(new Lexema("\"", "D", 100, lineaActual));
                contadorTiposTokens.put("D", contadorTiposTokens.get("D") + 1);
                salida.append("\"\tD\t100\tLínea: " + lineaActual + "\n");
                totalTokens++;
                delimitadores++;
                pos++;

                int endQuote = texto.indexOf('"', pos);
                if (endQuote == -1) {
                    errores.add("ERROR: Comilla no cerrada en línea " + lineaActual);
                    break;
                }

                String contenido = texto.substring(pos, endQuote);
                if (!contenido.isEmpty()) {
                    tokensAnalizados.add(new Lexema(contenido, "T", 450, lineaActual));
                    contadorTiposTokens.put("T", contadorTiposTokens.get("T") + 1);
                    salida.append(contenido + "\tT\t450\tLínea: " + lineaActual + "\n");
                    totalTokens++;
                }

                tokensAnalizados.add(new Lexema("\"", "D", 100, lineaActual));
                contadorTiposTokens.put("D", contadorTiposTokens.get("D") + 1);
                salida.append("\"\tD\t100\tLínea: " + lineaActual + "\n");
                totalTokens++;
                delimitadores++;

                pos = endQuote + 1;
                continue;
            }
            
            // 5. Números
            if (Character.isDigit(currentChar)) {
                Pattern numeroPattern = Pattern.compile("^\\d+(\\.\\d+)?([eE][+-]?\\d+)?");
                Matcher matcher = numeroPattern.matcher(restante);
                if (matcher.find()) {
                    String numero = matcher.group();
                    tokensAnalizados.add(new Lexema(numero, "N", 500, lineaActual));
                    contadorTiposTokens.put("N", contadorTiposTokens.get("N") + 1);
                    salida.append(numero + "\tN\t500\tLínea: " + lineaActual + "\n");
                    totalTokens++;
                    pos += numero.length();
                    continue;
                }
            }

            // 6. Operadores de 3 caracteres
            if (pos + 2 < texto.length()) {
                String tresCaracteres = texto.substring(pos, pos + 3);
                if (OPERADORES.containsKey(tresCaracteres)) {
                    String tipo = OPERADORES.get(tresCaracteres);
                    int tokenCode = getTokenCode(tipo);
                    tokensAnalizados.add(new Lexema(tresCaracteres, tipo, tokenCode, lineaActual));
                    contadorTiposTokens.put(tipo, contadorTiposTokens.get(tipo) + 1);
                    salida.append(tresCaracteres + "\t" + tipo + "\t" + tokenCode + "\tLínea: " + lineaActual + "\n");
                    totalTokens++;
                    pos += 3;
                    continue;
                }
            }

            // 7. Operadores de 2 caracteres
            if (pos + 1 < texto.length()) {
                String dosCaracteres = texto.substring(pos, pos + 2);
                if (OPERADORES.containsKey(dosCaracteres)) {
                    String tipo = OPERADORES.get(dosCaracteres);
                    int tokenCode = getTokenCode(tipo);
                    tokensAnalizados.add(new Lexema(dosCaracteres, tipo, tokenCode, lineaActual));
                    contadorTiposTokens.put(tipo, contadorTiposTokens.get(tipo) + 1);
                    salida.append(dosCaracteres + "\t" + tipo + "\t" + tokenCode + "\tLínea: " + lineaActual + "\n");
                    totalTokens++;
                    pos += 2;
                    continue;
                }
            }

            // 8. Operadores de 1 carácter
            if (OPERADORES.containsKey(Character.toString(currentChar))) {
                String tipo = OPERADORES.get(Character.toString(currentChar));
                int tokenCode = getTokenCode(tipo);
                tokensAnalizados.add(new Lexema(Character.toString(currentChar), tipo, tokenCode, lineaActual));
                contadorTiposTokens.put(tipo, contadorTiposTokens.get(tipo) + 1);
                salida.append(currentChar + "\t" + tipo + "\t" + tokenCode + "\tLínea: " + lineaActual + "\n");
                totalTokens++;
                pos++;
                continue;
            }

            // 9. Delimitadores VISIBLES
            if (DELIMITADORES_VISIBLES.contains(Character.toString(currentChar))) {
                tokensAnalizados.add(new Lexema(Character.toString(currentChar), "D", 100, lineaActual));
                contadorTiposTokens.put("D", contadorTiposTokens.get("D") + 1);
                salida.append(currentChar + "\tD\t100\tLínea: " + lineaActual + "\n");
                totalTokens++;
                delimitadores++;
                pos++;
                continue;
            }

            // 10. Identificadores y palabras reservadas
            if (Character.isLetter(currentChar) || currentChar == '_') {
                Pattern idPattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*");
                Matcher matcher = idPattern.matcher(restante);
                if (matcher.find()) {
                    String identificador = matcher.group();
                    
                    if (identificador.equals("Java") || identificador.equals("java")) {
                        errores.add("ERROR LÉXICO: '"+ identificador + "' no es un identificador válido en línea " + lineaActual);
                        tokensAnalizados.add(new Lexema(identificador, "ERROR", 999, lineaActual));
                        contadorTiposTokens.put("ERROR", contadorTiposTokens.get("ERROR") + 1);
                        salida.append(identificador + "\tERROR\t999\tLínea: " + lineaActual + "\n");
                        totalTokens++;
                        pos += identificador.length();
                        continue;
                    }
                    
                    String tipo;
                    int tokenCode;
                    
                    // MAIN siempre es M (Método)
                    if (identificador.equals("main")) {
                        tipo = "M";
                        tokenCode = 200;
                    } 
                    else if (PALABRAS_RESERVADAS.contains(identificador)) {
                        tipo = "PR";
                        tokenCode = 50;
                    } 
                    else if (METODOS.contains(identificador)) {
                        tipo = "M";
                        tokenCode = 200;
                    } 
                    else if (CLASES.contains(identificador)) {
                        tipo = "C";
                        tokenCode = 250;
                    } 
                    else if (ATRIBUTOS.contains(identificador)) {
                        tipo = "A";
                        tokenCode = 300;
                    } 
                    else {
                        if (esArgumentoMetodo(identificador, texto, pos)) {
                            tipo = "AM";
                            tokenCode = 350;
                        } 
                        else if (esPosibleErrorPalabraReservada(identificador)) {
                            errores.add("ERROR LÉXICO: ¿Posible error en palabra reservada? '" + identificador + "' en línea " + lineaActual);
                            tipo = "Id";
                            tokenCode = 150;
                        } 
                        else {
                            tipo = "Id";
                            tokenCode = 150;
                        }
                        
                        // AGREGAR A TABLA DE SÍMBOLOS - MEJORADO
                        if (esVariableDeUsuario(identificador, texto, pos)) {
                            String tipoSimbolo = determinarTipoVariable(identificador, texto, pos);
                            agregarATablaSimbolos(identificador, tipoSimbolo, determinarDireccion(tipoSimbolo), "NULL", lineaActual);
                        }
                    }
                    
                    tokensAnalizados.add(new Lexema(identificador, tipo, tokenCode, lineaActual));
                    contadorTiposTokens.put(tipo, contadorTiposTokens.get(tipo) + 1);
                    salida.append(identificador + "\t" + tipo + "\t" + tokenCode + "\tLínea: " + lineaActual + "\n");
                    totalTokens++;
                    pos += identificador.length();
                    continue;
                }
            }

            errores.add("ERROR: Carácter no reconocido '" + currentChar + "' en línea " + lineaActual);
            pos++;
        }

        if (enComentarioBloque) {
            errores.add("ERROR: Comentario de bloque no cerrado. Iniciado en línea " + inicioComentarioLinea);
        }

        if (!errores.isEmpty()) {
            salida.append("\n--- ERRORES ENCONTRADOS ---\n");
            for (String error : errores) {
                salida.append(error + "\n");
            }
        }

        mostrarEstadisticas(salida);
        
        if (!tablaSimbolos.isEmpty()) {
            mostrarTablaSimbolos();
        }
    }
    
    private void mostrarEstadisticas(JTextArea salida) {
        salida.append("\n--- ESTADÍSTICAS ---\n");
        salida.append("Número de lecturas: " + lecturas + "\n");
        salida.append("Total de tokens encontrados: " + totalTokens + "\n");
        salida.append("Delimitadores encontrados: " + delimitadores + "\n");
        salida.append("Caracteres desechados: " + caracteresDesechados + "\n");
        
        salida.append("\n--- DISTRIBUCIÓN DE TOKENS ---\n");
        for (Map.Entry<String, Integer> entry : contadorTiposTokens.entrySet()) {
            if (entry.getValue() > 0) {
                salida.append(entry.getKey() + ": " + entry.getValue() + " tokens\n");
            }
        }
        
        if (tablaSimbolos.isEmpty()) {
            salida.append("\nNo se encontraron identificadores para la tabla de símbolos\n");
        } else {
            salida.append("\nSe encontraron " + tablaSimbolos.size() + " símbolos en la tabla\n");
        }
    }

    public void mostrarTablaTokens() {
        Map<String, Integer> todosLosTipos = new LinkedHashMap<>();
        todosLosTipos.put("PR", 50);
        todosLosTipos.put("D", 100);
        todosLosTipos.put("Id", 150);
        todosLosTipos.put("M", 200);
        todosLosTipos.put("C", 250);
        todosLosTipos.put("A", 300);
        todosLosTipos.put("AM", 350);
        todosLosTipos.put("P", 400);
        todosLosTipos.put("T", 450);
        todosLosTipos.put("N", 500);
        todosLosTipos.put("OPB", 550);
        todosLosTipos.put("OPA", 600);
        todosLosTipos.put("OPL", 650);
        todosLosTipos.put("OPN", 700);
        todosLosTipos.put("OPR", 750);
        todosLosTipos.put("ERROR", 999);
        
        JFrame frameTokens = new JFrame("Tabla de Tokens - Todos los Tipos");
        frameTokens.setSize(400, 500);
        frameTokens.setLocationRelativeTo(null);
        
        JTable table = new JTable();
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"Tipo de Token", "Número de Token"});
        
        for (Map.Entry<String, Integer> tipo : todosLosTipos.entrySet()) {
            model.addRow(new Object[]{tipo.getKey(), tipo.getValue()});
        }
        
        table.setModel(model);
        JScrollPane scrollPane = new JScrollPane(table);
        frameTokens.add(scrollPane);
        frameTokens.setVisible(true);
    }
    
    public void mostrarTablaTokensCompleta() {
        JFrame frame = new JFrame("Tabla Completa de Tokens");
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null);
        
        String[] columnNames = {"Tipo", "Código", "Descripción"};
        Object[][] data = {
            {"PR", 50, "Palabra Reservada"},
            {"D", 100, "Delimitador"},
            {"Id", 150, "Identificador"},
            {"M", 200, "Método"},
            {"C", 250, "Clase"},
            {"A", 300, "Atributo"},
            {"AM", 350, "Argumento de Método"},
            {"P", 400, "Paquete (import)"},
            {"T", 450, "Texto/Cadena"},
            {"N", 500, "Número"},
            {"OPB", 550, "Operador Booleano"},
            {"OPA", 600, "Operador Aritmético"},
            {"OPL", 650, "Operador Lógico"},
            {"OPN", 700, "Operador Nivel Bits"},
            {"OPR", 750, "Operador Relacional"},
            {"ERROR", 999, "Error Léxico"}
        };
        
        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane);
        frame.setVisible(true);
    }
    
    public void mostrarTablaSimbolos() {
        if (tablaSimbolos.isEmpty()) {
            JOptionPane.showMessageDialog(v, "No hay símbolos para mostrar en la tabla.");
            return;
        }
        
        JFrame frameSimbolos = new JFrame("Tabla de Símbolos");
        frameSimbolos.setSize(900, 400);
        frameSimbolos.setLocationRelativeTo(null);
        
        JTable table = new JTable();
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"Identificador", "Tipo", "Dirección", "Valor", "Línea"});
        
        for (String[] simbolo : tablaSimbolos) {
            model.addRow(simbolo);
        }
        
        table.setModel(model);
        JScrollPane scrollPane = new JScrollPane(table);
        frameSimbolos.add(scrollPane);
        frameSimbolos.setVisible(true);
    }

    public static ArrayList<Lexema> getTokensAnalizados() {
        return tokensAnalizados;
    }

    public static ArrayList<String[]> getTablaSimbolos() {
        return tablaSimbolos;
    }
    // Agrega este método en tu clase Control
public void mostrarPilasSemanticas() {
    if (!AnaSemantico.hayExpresionParaMostrar()) {
        JOptionPane.showMessageDialog(v, "No hay expresión de asignación para mostrar las pilas.\n"
                + "Realice primero el análisis semántico.", 
                "Sin Datos", JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    
    // Crear ventana con pestañas para las pilas
    JFrame framePilas = new JFrame("Pilas Semánticas - Análisis de Expresión");
    framePilas.setSize(800, 600);
    framePilas.setLocationRelativeTo(null);
    
    JTabbedPane tabbedPane = new JTabbedPane();
    
    // Pestaña 1: Pila RID
    JTable tablaRID = new JTable();
    DefaultTableModel modelRID = new DefaultTableModel();
    modelRID.setColumnIdentifiers(new String[]{"Posición", "Elemento"});
    
    ArrayList<String[]> datosRID = AnaSemantico.getPilaRIDData();
    for (String[] fila : datosRID) {
        modelRID.addRow(fila);
    }
    
    tablaRID.setModel(modelRID);
    JScrollPane scrollRID = new JScrollPane(tablaRID);
    tabbedPane.addTab("Pila RID (Identificadores/Constantes)", scrollRID);
    
    // Pestaña 2: Pila IDR
    JTable tablaIDR = new JTable();
    DefaultTableModel modelIDR = new DefaultTableModel();
    modelIDR.setColumnIdentifiers(new String[]{"Posición", "Tipo"});
    
    ArrayList<String[]> datosIDR = AnaSemantico.getPilaIDRData();
    for (String[] fila : datosIDR) {
        modelIDR.addRow(fila);
    }
    
    tablaIDR.setModel(modelIDR);
    JScrollPane scrollIDR = new JScrollPane(tablaIDR);
    tabbedPane.addTab("Pila IDR (Tipos)", scrollIDR);
    
    // Pestaña 3: Resultado del Análisis
    JTextArea areaResultado = new JTextArea();
    areaResultado.setText(AnaSemantico.getResultadoAnalisis());
    areaResultado.setEditable(false);
    areaResultado.setLineWrap(true);
    areaResultado.setWrapStyleWord(true);
    areaResultado.setBackground(new java.awt.Color(240, 240, 240));
    areaResultado.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));
    
    JScrollPane scrollResultado = new JScrollPane(areaResultado);
    tabbedPane.addTab("Resultado del Análisis", scrollResultado);
    
    framePilas.add(tabbedPane);
    framePilas.setVisible(true);
}
}