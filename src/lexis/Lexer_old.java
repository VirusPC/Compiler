/*
package lexis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


enum Operator{
    */
/**
     * 操作符
     *//*

    Plus(0, "*"), Minus(1, "-"), Mul(2, "*"), Div(3, "/"), Equal(4, "="), DEqual(5, "=="),
    Less(6, "<"), More(7, ">"), LE(8, "<="), ME(9, ">=");
    private int id;
    private String name;
    Operator(int id, String name){
        this.id = id;
        this.name = name;
    }
    public Integer getId(){
        return id;
    }
    public String getName(){
        return name;
    }
}


enum Delimiter{
    */
/**
     * 界符
     *//*

    Comma(10, ","), Colon(11, ":"), Semicolon(12, ";"), LParen(13, "("), RParen(14, ")"),
    LBrace(15, "{"), RBrace(16, "}");
    private int id;
    private String name;
    Delimiter(int id, String name){
        this.id = id;
        this.name = name;
    }
    public Integer getId(){
        return id;
    }
    public String getName(){
        return name;
    }
}



public class Lexer {

    */
/**
     * 文件缓冲区大小
     *//*

    public static final int MAX_BUFFER_SIZE = 1024;

    */
/**
     * 标识符id
     *//*

    public static final int IDENTIFIER_ID = 17;

    */
/**
     * 常数中的数字的id
     *//*

    public static final int NUMBER_ID = 18;

    */
/**
     * 常数中的字符串的id
     *//*

    public static final int LETTER_ID = 19 ;
    

    private String sourceFilePath;
    public char[] buffer = new char[MAX_BUFFER_SIZE];
    private int off = 0;
    private int numInBuffer = 0;
    private int pos = 0;

    */
/**
     * 保留字
     *//*

    private Map<String, Integer> reserveTable = new HashMap();


    private List<Word> wordStream = new ArrayList();

    public Lexer(String reserveFilePath, String sourceFilePath){
        this.sourceFilePath = sourceFilePath;
        createReserveTable(reserveFilePath);
        getBuffer();
        analyse();
    }

    public Character getCh(){
        if(pos>=numInBuffer) {
            return null;
        }
        return buffer[pos++];
    }

    public boolean getBuffer(){
        try {
            FileReader fr = new FileReader(sourceFilePath);
            BufferedReader br = new BufferedReader(fr);
            numInBuffer = br.read(buffer, off, MAX_BUFFER_SIZE);
            br.close();
            fr.close();
            if(numInBuffer == -1){
                return false;
            }
            off += MAX_BUFFER_SIZE;
        } catch (Exception e) {
            System.out.println("Source File Error");
        }
        return true;
    }


    */
/**
     * 一次扫描，得出一个单词
     * @return
     *//*

    public boolean scan(){
        String token = "";
        Character c = getCh();
        while(c=='\r' || c=='\n' || c==' ' || c=='\t'){
            c = getCh();
        }
        if(c==null){
            return false;
        }
        if(Character.isLetter(c)){
            */
/**
             * 关键字或标识符
             *//*

            while(c!=null&&(Character.isLetter(c) || Character.isDigit(c))) {
                token = token.concat(String.valueOf(c));
                c = getCh();
            }
            pos--;
            Integer type = reserveTable.get(token);
            if(type!=null) {
                wordStream.add(new Word(type, token));
            } else {
                wordStream.add(new Word(IDENTIFIER_ID, token));
            }
        } else if(Character.isDigit(c)) {
            */
/**
             * 常数
             *//*

            while(c!=null&&Character.isDigit(c)){
                token = token.concat(String.valueOf(c));
                c = getCh();
            }
            pos--;
            wordStream.add(new Word(NUMBER_ID, token));
        } else{
            */
/**
             * 运算符或界符
             *//*


            switch(c){
                case '+':
                    wordStream.add(new Word(Operator.Plus.getId(), Operator.Plus.getName()));
                    break;
                case'-':
                    wordStream.add(new Word(Operator.Minus.getId(), Operator.Minus.getName()));
                    break;
                case'*':
                    wordStream.add(new Word(Operator.Mul.getId(), Operator.Mul.getName()));
                    break;
                case'/':
                    wordStream.add(new Word(Operator.Div.getId(), Operator.Div.getName()));
                    break;
                case'<':
                    c = getCh();
                    if(c == Operator.Equal.getName().charAt(0)){
                        wordStream.add(new Word(Operator.LE.getId(), Operator.LE.getName()));
                    } else {
                        pos--;
                        wordStream.add(new Word(Operator.Less.getId(), Operator.Less.getName()));
                    }
                    break;
                case'>':
                    c = getCh();
                    if(c == Operator.Equal.getName().charAt(0)){
                        wordStream.add(new Word(Operator.ME.getId(), Operator.ME.getName()));
                    } else {
                        pos--;
                        wordStream.add(new Word(Operator.More.getId(), Operator.More.getName()));
                    }
                    break;
                case':':
                    wordStream.add(new Word(Delimiter.Colon.getId(), Delimiter.Colon.getName()));
                    break;
                case';':
                    wordStream.add(new Word(Delimiter.Semicolon.getId(), Delimiter.Semicolon.getName()));
                    break;
                case'(':
                    wordStream.add(new Word(Delimiter.LParen.getId(), Delimiter.LParen.getName()));
                    break;
                case')':
                    wordStream.add(new Word(Delimiter.RParen.getId(), Delimiter.RParen.getName()));
                    break;
                case'{':
                    wordStream.add(new Word(Delimiter.LBrace.getId(), Delimiter.LBrace.getName()));
                    break;
                case'}':
                    wordStream.add(new Word(Delimiter.RBrace.getId(), Delimiter.RBrace.getName()));
                    break;
                case',':
                    wordStream.add(new Word(Delimiter.Comma.getId(), Delimiter.Comma.getName()));
                    break;
                case'=':
                    c = getCh();
                    if(c == Operator.Equal.getName().charAt(0)){
                        wordStream.add(new Word(Operator.DEqual.getId(), Operator.DEqual.getName()));
                    } else {
                        pos--;
                        wordStream.add(new Word(Operator.Equal.getId(), Operator.Equal.getName()));
                    }
                    break;
                    default:
                        System.err.println("Wrong Symbol: " + c.toString());
            }
        }
        if(getCh()==null){
            return false;
        }
        pos--;
        return true;
    }

    public void analyse(){
        while(scan()){}
    }
    

    public void createReserveTable(String reserveFilePath){
        try {
            FileReader fr = new FileReader(reserveFilePath);
            BufferedReader br = new BufferedReader(fr);
            String reserve_word = null;
            int count = LETTER_ID;
            while((reserve_word=br.readLine())!=null){
                reserveTable.put(reserve_word.trim(), ++count);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("File Error");
        }
    }

    public List getWordStream(){
        return wordStream;
    }

    public void printReserveTable(){
        for(String key : reserveTable.keySet()){
            System.out.println(key+": "+reserveTable.get(key));
        }
    }

    public void printWordStream(){
        for(Word w : wordStream){
            System.out.println("( "+w.type + ",  " + w.value+" )");
        }
    }

}
*/
