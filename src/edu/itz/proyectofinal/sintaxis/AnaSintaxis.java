package edu.itz.proyectofinal.sintaxis;

import edu.itz.proyectofinal.control.Control;
import edu.itz.proyectofinal.lexemas.Lexema;
import edu.itz.proyectofinal.vistas.Ventana;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JTextArea;

public class AnaSintaxis {
    private static ArrayList<Lexema> lexes;
    private static int pos = 0;
    private static JTextArea salida;
    private static int errores = 0;
    private static final int MAX_ERRORES = 100;
    private static final List<Integer> TOKENS_SINCRON = Arrays.asList(15, 20, 25, 30, 45, 50, 55, 60, 65, 70, 75, 80, 85);
    private static final List<Integer> OL = Arrays.asList(90, 95, 100, 105, 110, 115);
    

    public static void programa(Ventana ventana) {
        salida = ventana.getTxtSalida();
        salida.setText("");
        lexes = Control.getTokensAnalizados();
        pos = 0;
        errores = 0;
        
        if (lexes.isEmpty()) {
            error("No hay tokens para analizar");
            return;
        }
        
        salida.append("\nAnálisis completado con exito.\n");
        
        bloque(); // Programa ::= Bloque .
        if (token() != 5) { // Se espera el punto final 
            error("Se esperaba '.' al final del programa");
        } 
        
        salida.append("\nErrores: " + errores + "\n");
    }

    // Bloque ::= B2 B4 B5 Proposición
    private static void bloque() {
        b2();
        b4();
        b5();
        proposicion();
    }
    
    // B2 ::= const B1 ; | NULL
    private static void b2() {
        //Si no es el token esperado, salte 
        if (token() != 45) { // const
         return;//NULL
    }
        //Si llaga hasta aqui , sabemos que el token es 45
        avanzar();
        b1();
        if(token() != 15){
            error("Se esperaba  ';'");
            return;
        }
        avanzar();
    }
    
    // B1 ::= id = num BB1
    private static void b1() {
        if (token() != 155) { error("Se esperaba id"); return; }
        avanzar();
        
        if (token() != 120) { error("Se esperaba '='"); return; }
        avanzar();
        
        if (token() != 160) { error("Se esperaba num"); return; }
        avanzar();
        
        bb1();
    }
    
    // BB1 ::= , B1 | NULL
    private static void bb1() {
        if (token() != 10) { // S i no es una ( , ) , salte
            return;//NULL
        }
            avanzar();
            b1();
    }
    
    // B4 ::= var B3 ; | NULL
    private static void b4() {
        if (token() == 50) { // var
            avanzar();
            b3();
            if (token() != 15) { error("Se esperaba ';'"); return; }
            avanzar();
        } else {
            return; // NULL
        }
    }
    
    // B3 ::= id BB3
    private static void b3() {
        if (token() != 155) { error("Se esperaba id"); return; }
        avanzar();
        bb3();
    }
    
    // BB3 ::= , B3 | NULL
    private static void bb3() {
        if (token() == 10) { // ,
            avanzar();
            b3();
        } else {
            return; // NULL
        }
    }
    
    // B5 ::= Proced id ; Bloque ; B5 | NULL
    private static void b5() {
        if (token() == 55) { // Proced
            avanzar();
            
            if (token() != 155) { error("Se esperaba id"); return; }
            avanzar();
            
            if (token() != 15) { error("Se esperaba ';'"); return; }
            avanzar();
            
            bloque();
            
            if (token() != 15) { error("Se esperaba ';'"); return; }
            avanzar();
            
            b5();
        } else {
            return; // NULL
        }
    }
    
    // Proposición ::= { P1 } | id=Expresión | print P2 | input id | exec id | 
    //                 if Condición:Proposición | while Condición:Proposición | 
    //                 for id=Expresión P3 Expresión: Proposición
    private static void proposicion() {
        switch (token()) {
            case 20: // {
                avanzar();
                p1();
                if (token() != 25) { error("Se esperaba '}'"); return; }
                avanzar();
                break;
                
            case 155: // id
                avanzar();
                if (token() != 120) { error("Se esperaba '='"); return; }
                avanzar();
                expresion();
                break;
                
            case 60: // print
                avanzar();
                p2();
                break;
                
            case 65: // input
                avanzar();
                if (token() != 155) { error("Se esperaba id"); return; }
                avanzar();
                break;
                
            case 70: // exec
                avanzar();
                if (token() != 155) { error("Se esperaba id"); return; }
                avanzar();
                break;
                
            case 75: // if
                avanzar();
                condicion();
                if (token() != 30) { error("Se esperaba ':'"); return; }
                avanzar();
                proposicion();
                break;
                
            case 80: // while
                avanzar();
                condicion();
                if (token() != 30) { error("Se esperaba ':'"); return; }
                avanzar();
                proposicion();
                break;
                
            case 85: // for
                avanzar();
                if (token() != 155) { error("Se esperaba id"); return; }
                avanzar();
                if (token() != 120) { error("Se esperaba '='"); return; }
                avanzar();
                expresion();
                p3();
                expresion();
                if (token() != 30) { error("Se esperaba ':'"); return; }
                avanzar();
                proposicion();
                break;
                
            default:
                error("Proposición inválida");
        }
    }
    
    // P1 ::= Proposición PP1
    private static void p1() {
        proposicion();
        pp1();
    }
    
    // PP1 ::= ; P1 | NULL
    private static void pp1() {
        if (token() == 15) { // ;
            avanzar();
            p1();
        } else {
            return; // NULL
        }
    }
    
    // P2 ::= id | num
    private static void p2() {
        if (token() != 155 && token() != 160) { 
            error("Se esperaba id o num"); 
            return; 
        }
        avanzar();
    }
    
    // P3 ::= -> | <-
    private static void p3() {
        if (token() != 125 && token() != 130) { 
            error("Se esperaba '->' o '<-'"); 
            return; 
        }
        avanzar();
    }
    
    // Factor ::= ( Expresión ) | id | num
    private static void factor() {
        if (token() == 35) { // (
            avanzar();
            expresion();
            if (token() != 40) { error("Se esperaba ')'"); return; }
            avanzar();
        } 
        else if (token() == 155 || token() == 160) { // id o num
            avanzar();
        } 
        else {
            error("Se esperaba '(', id o num");
        }
    }
    
    // Término ::= Factor term
    private static void termino() {
        factor();
        term();
    }
    
    // term ::= * Término | / Término | NULL
    private static void term() {
        if (token() == 145 || token() == 150) { // * o /
            avanzar();
            termino();
        } else {
            return; // NULL
        }
    }
    
    // Expresión ::= Término exp
    private static void expresion() {
        termino();
        exp();
    }
    
    // exp ::= - Expresión | + Expresión | NULL
    private static void exp() {
        if (token() == 135 || token() == 140) { // + o -
            avanzar();
            expresion();
        } else {
            return; // NULL
        }
    }
    
    // Condición ::= Expresión OL Expresión
    private static void condicion() {
        expresion();
        ol();
        expresion();
    }
    
    // OL ::= == | <> | < | > | <= | >=
    private static void ol() {
        if (!OL.contains(token())) { 
            error("Operador lógico inválido"); 
            return; 
        }
        avanzar();
    }
    
    
    private static void avanzar() {
        if (pos < lexes.size()) pos++;
    }
    
    private static int token() {
        return (pos < lexes.size()) ? lexes.get(pos).getToken() : -1;
    }
    
    private static void error(String msg) {
    errores++;
    if (errores > MAX_ERRORES) {
        salida.append("\nDEMASIADOS ERRORES. Análisis abortado.\n");
        throw new RuntimeException("Demasiados errores");
    }

    if (pos < lexes.size()) {
        Lexema l = lexes.get(pos);
        salida.append("\nERROR [" + errores + "] en línea " + l.getLinea() + 
                     ": '" + l.getElemento() + "' - " + msg + "\n");
    } else {
        // Para errores al final
        int ultimaLinea = !lexes.isEmpty() ? lexes.get(lexes.size() - 1).getLinea() : 1;
        salida.append("\nERROR [" + errores + "] en línea " + ultimaLinea + ": " + msg + "\n");
    }

    // avanza hasta un token de recuperación
    while (pos < lexes.size() && !TOKENS_SINCRON.contains(token())) {
        pos++;
    }
}
}