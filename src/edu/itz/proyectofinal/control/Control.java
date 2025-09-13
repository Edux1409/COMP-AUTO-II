package edu.itz.proyectofinal.control;

import edu.itz.proyectofinal.lexemas.Lexema;
import edu.itz.proyectofinal.vistas.Ventana;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
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
            "setContextClassLoader", "getStackTrace", "getAllStackTraces"
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

    // Delimitadores
    private static final Set<String> DELIMITADORES = new HashSet<>();
    static {
        String[] delimitadores = {
            "[", "]", "{", "}", "(", ")", ";", ",", ".", "\"", " ", "\t", "\n", "\r"
        };
        for (String del : delimitadores) {
            DELIMITADORES.add(del);
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
            default: return 0;
        }
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

        String texto = v.getTxtContenido().getText();
        JTextArea salida = v.getTxtSalida();
        salida.setText("");

        int pos = 0;
        int lineaActual = 1;
        boolean enComentarioBloque = false;

        while (pos < texto.length()) {
            lecturas++;
            char currentChar = texto.charAt(pos);

            // Manejo de saltos de línea
            if (currentChar == '\n') {
                lineaActual++;
                pos++;
                continue;
            }

            // Saltar espacios en blanco
            if (Character.isWhitespace(currentChar)) {
                if (currentChar == ' ' || currentChar == '\t') {
                    caracteresDesechados++;
                }
                pos++;
                continue;
            }

            // Manejo de comentarios
            if (enComentarioBloque) {
                if (pos + 1 < texto.length() && texto.substring(pos, pos + 2).equals("*/")) {
                    enComentarioBloque = false;
                    pos += 2;
                    caracteresDesechados += 2;
                    continue;
                }
                pos++;
                caracteresDesechados++;
                continue;
            }

            if (pos + 1 < texto.length() && texto.substring(pos, pos + 2).equals("//")) {
                // Comentario de línea - saltar hasta el final de la línea
                while (pos < texto.length() && texto.charAt(pos) != '\n') {
                    pos++;
                    caracteresDesechados++;
                }
                continue;
            }

            if (pos + 1 < texto.length() && texto.substring(pos, pos + 2).equals("/*")) {
                enComentarioBloque = true;
                pos += 2;
                caracteresDesechados += 2;
                continue;
            }

            String restante = texto.substring(pos);

            // 1. Cadenas de texto (entre comillas)
            if (currentChar == '"') {
                int endQuote = texto.indexOf('"', pos + 1);
                if (endQuote == -1) {
                    errores.add("ERROR: Comilla no cerrada en línea " + lineaActual);
                    break;
                }
                String cadena = texto.substring(pos, endQuote + 1);
                tokensAnalizados.add(new Lexema(cadena, "T", 450, lineaActual));
                contadorTiposTokens.put("T", contadorTiposTokens.get("T") + 1);
                salida.append(cadena + "\tT\t450\tLínea: " + lineaActual + "\n");
                totalTokens++;
                pos = endQuote + 1;
                continue;
            }

            // 2. Números
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

            // 3. Operadores de 3 caracteres
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

            // 4. Operadores de 2 caracteres
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

            // 5. Operadores de 1 carácter
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

            // 6. Delimitadores
            if (DELIMITADORES.contains(Character.toString(currentChar))) {
                tokensAnalizados.add(new Lexema(Character.toString(currentChar), "D", 100, lineaActual));
                contadorTiposTokens.put("D", contadorTiposTokens.get("D") + 1);
                salida.append(currentChar + "\tD\t100\tLínea: " + lineaActual + "\n");
                totalTokens++;
                delimitadores++;
                pos++;
                continue;
            }

            // 7. Identificadores y palabras reservadas
            if (Character.isLetter(currentChar) || currentChar == '_') {
                Pattern idPattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*");
                Matcher matcher = idPattern.matcher(restante);
                if (matcher.find()) {
                    String identificador = matcher.group();
                    
                    // Verificar tipo de token
                    String tipo;
                    int tokenCode;
                    
                    if (PALABRAS_RESERVADAS.contains(identificador)) {
                        tipo = "PR";
                        tokenCode = 50;
                    } else if (METODOS.contains(identificador)) {
                        tipo = "M";
                        tokenCode = 200;
                    } else if (CLASES.contains(identificador)) {
                        tipo = "C";
                        tokenCode = 250;
                    } else if (ATRIBUTOS.contains(identificador)) {
                        tipo = "A";
                        tokenCode = 300;
                    } else {
                        tipo = "Id";
                        tokenCode = 150;
                        // Agregar a tabla de símbolos
                        agregarATablaSimbolos(identificador, "S", determinarDireccion("S"), "NULL", lineaActual);
                    }
                    
                    tokensAnalizados.add(new Lexema(identificador, tipo, tokenCode, lineaActual));
                    contadorTiposTokens.put(tipo, contadorTiposTokens.get(tipo) + 1);
                    salida.append(identificador + "\t" + tipo + "\t" + tokenCode + "\tLínea: " + lineaActual + "\n");
                    totalTokens++;
                    pos += identificador.length();
                    continue;
                }
            }

            // 8. Paquetes (import statements)
            if (restante.startsWith("import ")) {
                int endImport = texto.indexOf(';', pos);
                if (endImport == -1) {
                    errores.add("ERROR: Import no terminado con ; en línea " + lineaActual);
                    break;
                }
                String importStatement = texto.substring(pos, endImport + 1);
                tokensAnalizados.add(new Lexema(importStatement, "P", 400, lineaActual));
                contadorTiposTokens.put("P", contadorTiposTokens.get("P") + 1);
                salida.append(importStatement + "\tP\t400\tLínea: " + lineaActual + "\n");
                totalTokens++;
                pos = endImport + 1;
                continue;
            }

            // Si no se encontró ningún patrón válido
            errores.add("ERROR: Carácter no reconocido '" + currentChar + "' en línea " + lineaActual);
            pos++;
        }

        // Mostrar errores
        if (!errores.isEmpty()) {
            salida.append("\n--- ERRORES ENCONTRADOS ---\n");
            for (String error : errores) {
                salida.append(error + "\n");
            }
        }

        // Mostrar estadísticas
        mostrarEstadisticas(salida);
        
        // Mostrar tabla de símbolos si no hay errores
        if (errores.isEmpty() && !tablaSimbolos.isEmpty()) {
            mostrarTablaSimbolos();
        }
    }

    private void agregarATablaSimbolos(String identificador, String tipo, int direccion, String valor, int linea) {
        // Verificar si ya existe en la tabla
        for (String[] simbolo : tablaSimbolos) {
            if (simbolo[0].equals(identificador)) {
                return; // Ya existe, no agregar duplicado
            }
        }
        
        String[] nuevoSimbolo = {identificador, tipo, String.valueOf(direccion), valor, String.valueOf(linea)};
        tablaSimbolos.add(nuevoSimbolo);
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
        
        // Información sobre la tabla de símbolos
        if (tablaSimbolos.isEmpty()) {
            salida.append("\nNo se encontraron identificadores para la tabla de símbolos\n");
        } else {
            salida.append("\nSe encontraron " + tablaSimbolos.size() + " símbolos en la tabla\n");
        }
    }

    public void mostrarTablaTokens() {
        if (tokensAnalizados.isEmpty()) {
            JOptionPane.showMessageDialog(v, "No hay tokens para mostrar. Primero ejecute el análisis léxico.");
            return;
        }
        
           
        JFrame frameTokens = new JFrame("Tabla de Tokens");
        frameTokens.setSize(800, 500);
        frameTokens.setLocationRelativeTo(null);
        
        JTable table = new JTable();
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"Elemento", "Tipo", "Token", "Línea"});
        
        for (Lexema lexema : tokensAnalizados) {
            model.addRow(new Object[]{
                lexema.getElemento(),
                lexema.getTipoToken(),
                lexema.getToken(),
                lexema.getLinea()
            });
        }
        
        table.setModel(model);
        JScrollPane scrollPane = new JScrollPane(table);
        frameTokens.add(scrollPane);
        frameTokens.setVisible(true);
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
}

