package my.scanner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class Scanner {
    
    private Map<Transition, String> transitionTable = new HashMap<>();
    private Map<Character, String> classifierTable = new HashMap<>();
    private Map<String, String> tokenTypeTable = new HashMap<>();
    
    private LinkedList<String> stack = new LinkedList<>();
    
    private RandomAccessFile rf;
    private int column = 1;
    private int row = 1;
    
    public Scanner(String filePath){
        
        try {
            rf = new RandomAccessFile(filePath, "r");
        } catch (FileNotFoundException e) {
            System.err.println("[Error] failed to open the given file");
            return;
        }
        
        buildClassifierTable();
        buildTransitionTable();
        buildTokenTypeTable();

    }
    private void buildTokenTypeTable(){

        tokenTypeTable.put("S1", "NUM(0)");
        tokenTypeTable.put("S2", "NUM");
        tokenTypeTable.put("S3", "ID(i)");
        tokenTypeTable.put("S4", "IF");
        tokenTypeTable.put("S5", "ID");
    }

    //construct transition table
    private void buildTransitionTable(){
        transitionTable.put(new Transition("S0", "other"), "S0");
        transitionTable.put(new Transition("S0", "0"), "S1");
        transitionTable.put(new Transition("S0", "digit"), "S2");
        
        
        transitionTable.put(new Transition("S2", "0"), "S2");
        transitionTable.put(new Transition("S2", "digit"), "S2");

        transitionTable.put(new Transition("S0", "i"), "S3");
        transitionTable.put(new Transition("S3", "f"), "S4");

        transitionTable.put(new Transition("S3", "i"), "S5");
        transitionTable.put(new Transition("S3", "letter"), "S5");
        transitionTable.put(new Transition("S3", "0"), "S5");
        transitionTable.put(new Transition("S3", "digit"), "S5");
        
        
        transitionTable.put(new Transition("S4", "letter"), "S5");
        transitionTable.put(new Transition("S4", "f"), "S5");
        transitionTable.put(new Transition("S4", "i"), "S5");
        transitionTable.put(new Transition("S4", "0"), "S5");
        transitionTable.put(new Transition("S4", "digit"), "S5");
        
        transitionTable.put(new Transition("S0", "letter"), "S5");
        transitionTable.put(new Transition("S0", "f"), "S5");
        
        
        transitionTable.put(new Transition("S5", "letter"), "S5");
        transitionTable.put(new Transition("S5", "i"), "S5");
        transitionTable.put(new Transition("S5", "f"), "S5");
        transitionTable.put(new Transition("S5", "0"), "S5");
        transitionTable.put(new Transition("S5", "digit"), "S5");
    }

    //construct classifier table
    private void buildClassifierTable(){
        classifierTable.put('_', "letter");
        classifierTable.put(' ', "other");
        classifierTable.put('\n', "other");
        classifierTable.put('\r', "other");
        classifierTable.put('$', "EOF");
        //'0'->48
        for(char c = '0'; c <='9'; c++){
            classifierTable.put(c, "digit");
        }
        classifierTable.put('0', "0");
        //'A'->65
        for(char c = 'A'; c <= 'Z'; c++ ){
            classifierTable.put(c, "letter");
        }
        //'a'->97
        for(char c = 'a'; c <= 'z'; c++ ){
            classifierTable.put(c, "letter");
        }
        classifierTable.put('i', "i");
        classifierTable.put('I', "i");
        classifierTable.put('f', "f");
        classifierTable.put('F', "f");

    }
    
    private String nextWord(){
        String state = "S0";
        String lexeme = "";
        stack.clear();
        stack.push("bad");
        
        while(!state.equals("Se")){
            char cnext = nextChar(rf);
            if(cnext == '\n'){
               row++; 
               column=1;
            }

            lexeme = lexeme + cnext;
            if(lexeme.equals("$")){
                return null;
            }
            if(tokenTypeTable.containsKey(state)){
                stack.clear();
            }
            stack.push(state);
            String category = classifierTable.get(cnext);
            state = transitionTable.getOrDefault(new Transition(state, category),"Se");
        }
        
        while(!state.equals("bad") && !tokenTypeTable.containsKey(state)){
            state = stack.pop();
            lexeme = lexeme.substring(0, lexeme.length()-1);
            rollBack(rf);
        }
        
        if(tokenTypeTable.containsKey(state)){
            String result = "";
            if(state.equals("S1") || state.equals("S3")){
                result = tokenTypeTable.get(state) + " ("+ row + ", " + (column - 1) + ")";
            }else if(state.equals("S4")){
                result = tokenTypeTable.get(state) + " ("+ row + ", " + (column - 2) + ")";
            }else{
                String trimedLexeme = lexeme.trim();
                result = tokenTypeTable.get(state) + "(" + trimedLexeme +")" + " ("+ row + ", " + (column - trimedLexeme.length() ) + ")";
            }
            return result;
        }else{
            try {
                rf.close();
            } catch (IOException e) {
                System.err.println("[error] failed to close the given file");
                System.exit(1);
            }
            return null;
        }
    }
    
    private char nextChar(RandomAccessFile rf){
        char c = ' ';
        try {
            int i = rf.read();
            if(i == -1){
               c = '$';
            }else{
               c = (char)i;
            }
        } catch (IOException e) {
            System.err.println("[error] failed to read a character from given file");
            System.exit(1);
        }
        column++;
        return c;
    }
    
    private void rollBack(RandomAccessFile rf){
        try {
            if(rf.getFilePointer() == rf.length()){
                return;
            }
            rf.seek(rf.getFilePointer()-1);
            column--;
        } catch (IOException e) {
            System.err.println("[error] failed to operate the rollback() method ");
            System.exit(1);
        }
    }
    
    private class Transition{
        private String state;
        private String condition;
        public Transition(String s, String c){
            state = s;
            condition = c;
        }
        @Override
        public boolean equals(Object obj){
            if (obj == null) { return false; }
            if (obj == this) { return true; }
            if (obj.getClass() != getClass()) {
              return false;
            }
            Transition rhs = (Transition) obj;
            return new EqualsBuilder()
                          .append(state, rhs.state)
                          .append(condition, rhs.condition)
                          .isEquals();
        }
        @Override
        public int hashCode(){
            return new HashCodeBuilder(17, 37).
                    append(state).
                    append(condition).
                    toHashCode();
        }
    }
    
    public static void parese(String filename){
        Scanner scanner = new Scanner(filename);
        String nextParsedWord = "";
        while(nextParsedWord != null){
            nextParsedWord = scanner.nextWord();
            if(nextParsedWord == null){
                System.out.println("EOF");
            }else{
                System.out.println(nextParsedWord);
            }
        }
    }
}
