package lexis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


enum Operator{
    /**
     * 操作符 编号空间0~19
     */
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
    /**
     * 界符 编号空间20~39
     */
    Comma(0, ","), Colon(1, ":"), Semicolon(2, ";"), LParen(3, "("), RParen(4, ")"),
    LBrace(5, "{"), RBrace(6, "}");
    private int id;
    private String name;
    Delimiter(int id, String name){
        this.id = id+20;
        this.name = name;
    }
    public Integer getId(){
        return id;
    }
    public String getName(){
        return name;
    }
}

enum Reserve{
    /**
     * 保留字 40~79
     */
    Auto(0), Break(1), Case(2), Char(3), Const(4), Continue(5),
    Default(6), Do(7), Double(8), Eles(9), Enum(10), Extern(11),
    Float(12), For(13), Goto(14), If(15), Int(16), Long(17), Register(18),
    Return(19), Short(20), Signed(21), Sizeof(22), Static(23), Struct(24),
    Switch(25), Typedef(26), Union(27), Usigned(28), Void(29), Volatile(30), While(31);
    private int id;
    Reserve(int id){
        this.id = id+40;
    }
    public Integer getId() {
        return id;
    }
}


public class Lexer {

    /**
     * 文件缓冲区大小
     */
    public static final int MAX_BUFFER_SIZE = 1024;

    /**
     * 标识符id
     */
    public static final int IDENTIFIER_ID = 80;

    /**
     * 常数中的数字的id
     */
    public static final int NUMBER_ID = 81;

    /**
     * 常数中的字符串的id
     */
    public static final int LETTER_ID = 82 ;
    

    private String sourceFilePath;
    public char[] buffer = new char[MAX_BUFFER_SIZE];
    private int off = 0;
    private int numInBuffer = 0;
    private int pos = 0;


    private List<Word> wordStream = new ArrayList();

    public Lexer(String sourceFilePath){
        this.sourceFilePath = sourceFilePath;
        getBuffer();
        analyse();
    }


    private Character getCh(){
        if(pos>=numInBuffer) {
            return null;
        }
        return buffer[pos++];
    }

    private boolean getBuffer(){
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


    /**
     * 一次扫描，得出一个单词
     * @return
     */
    private boolean scan(){
        String token = "";
        Character c = getCh();
        while(c=='\r' || c=='\n' || c==' ' || c=='\t'){
            c = getCh();
        }
        if(c==null){
            return false;
        }
        if(Character.isLetter(c)){
            /**
             * 关键字或标识符
             */
            while(c!=null&&(Character.isLetter(c) || Character.isDigit(c))) {
                token = token.concat(String.valueOf(c));
                c = getCh();
            }
            pos--;
            Integer type = null;
            for(Reserve r : Reserve.values()){
                if(r.toString().toLowerCase().equals(String.valueOf(c))){
                    type = r.getId();
                }
            }
            if(type!=null) {
                wordStream.add(new Word(type, token));
            } else {
                wordStream.add(new Word(IDENTIFIER_ID, token));
            }
        } else if(Character.isDigit(c)) {
            /**
             * 常数
             */
            while(c!=null&&Character.isDigit(c)){
                token = token.concat(String.valueOf(c));
                c = getCh();
            }
            pos--;
            wordStream.add(new Word(NUMBER_ID, token));
        } else{
            /**
             * 运算符或界符
             */
            Word word = null;
            /**
             * 界符
             */
            for(Delimiter d : Delimiter.values()){
                if(d.getName().equals(String.valueOf(c))){
                    word = new Word(d.getId(), d.getName());
                    wordStream.add(word);
                    break;
                }
            }

            if(word == null) {
                switch (c) {
                    case '+':
                        wordStream.add(new Word(Operator.Plus.getId(), Operator.Plus.getName()));
                        break;
                    case '-':
                        wordStream.add(new Word(Operator.Minus.getId(), Operator.Minus.getName()));
                        break;
                    case '*':
                        wordStream.add(new Word(Operator.Mul.getId(), Operator.Mul.getName()));
                        break;
                    case '/':
                        wordStream.add(new Word(Operator.Div.getId(), Operator.Div.getName()));
                        break;
                    case '<':
                        c = getCh();
                        if (c == Operator.Equal.getName().charAt(0)) {
                            wordStream.add(new Word(Operator.LE.getId(), Operator.LE.getName()));
                        } else {
                            pos--;
                            wordStream.add(new Word(Operator.Less.getId(), Operator.Less.getName()));
                        }
                        break;
                    case '>':
                        c = getCh();
                        if (c == Operator.Equal.getName().charAt(0)) {
                            wordStream.add(new Word(Operator.ME.getId(), Operator.ME.getName()));
                        } else {
                            pos--;
                            wordStream.add(new Word(Operator.More.getId(), Operator.More.getName()));
                        }
                        break;
                    case '=':
                        c = getCh();
                        if (c == Operator.Equal.getName().charAt(0)) {
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
        }// else if() {}
        if(getCh()==null){
            return false;
        }
        pos--;
        return true;
    }

    private void analyse(){
        while(scan()){}
    }


    public List getWordStream(){
        return wordStream;
    }


    public void printWordStream(){
        for(Word w : wordStream){
            System.out.println("( "+w.type + ",  " + w.value+" )");
        }
    }

}
