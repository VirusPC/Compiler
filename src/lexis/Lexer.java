package lexis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Lexer {

    /**
     * 文件缓冲区大小
     */
    public static final int MAX_BUFFER_SIZE = 1024;

    

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
        int i = 0;
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
            while(c!=null&&   (Character.isLetter(c) || Character.isDigit(c))   ) {
                token = token.concat(String.valueOf(c));
                c = getCh();
            }
            pos--;
            Integer type = null;

            for(Reserve r : Reserve.values()){
                if(r.toString().toLowerCase().equals(String.valueOf(token))){
                    type = r.getId();
                    break;
                }
            }

            if(type!=null) {
                wordStream.add(new Word(type));
            } else {
                wordStream.add(new Word(Identifier.Id.getId(), token));
            }
            if(c==null){
                return false;
            }
        } else if(Character.isDigit(c)) {
            /**
             * 常数
             */
            while(c!=null&&Character.isDigit(c)){
                token = token.concat(String.valueOf(c));
                c = getCh();
            }
            wordStream.add(new Word(Constant.Num.getId(), token));
            if(c==null){
                return false;
            }
            pos--;
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
                    word = new Word(d.getId());
                    wordStream.add(word);
                    break;
                }
            }

            if(word == null) {
                switch (c) {
                    case '+':
                        c = getCh();
                        if (c == Operator.Plus.getName().charAt(0)) {
                            wordStream.add(new Word(Operator.SelfAdd.getId()));
                        } else {
                            pos--;
                            wordStream.add(new Word(Operator.Plus.getId()));
                        }
                        break;
                    case '-':
                        c = getCh();
                        if (c == Operator.Minus.getName().charAt(0)) {
                            wordStream.add(new Word(Operator.SelfSub.getId()));
                        } else {
                            pos--;
                            wordStream.add(new Word(Operator.Minus.getId()));
                        }
                        break;
                    case '*':
                        wordStream.add(new Word(Operator.Mul.getId()));
                        break;
                    case '/':
                        wordStream.add(new Word(Operator.Div.getId()));
                        break;
                    case '<':
                        c = getCh();
                        if (c == Operator.Equal.getName().charAt(0)) {
                            wordStream.add(new Word(Operator.LE.getId()));
                        } else {
                            pos--;
                            wordStream.add(new Word(Operator.Less.getId()));
                        }
                        break;
                    case '>':
                        c = getCh();
                        if (c == Operator.Equal.getName().charAt(0)) {
                            wordStream.add(new Word(Operator.ME.getId()));
                        } else {
                            pos--;
                            wordStream.add(new Word(Operator.More.getId()));
                        }
                        break;
                    case '!':
                        c = getCh();
                        if (c == Operator.Equal.getName().charAt(0)) {
                            wordStream.add(new Word(Operator.NE.getId()));
                        } else {
                            pos--;
                            wordStream.add(new Word(Operator.Not.getId()));
                        }
                        break;
                    case '=':
                        c = getCh();
                        if (c == Operator.Equal.getName().charAt(0)) {
                            wordStream.add(new Word(Operator.DEqual.getId()));
                        } else {
                            pos--;
                            wordStream.add(new Word(Operator.Equal.getId()));
                        }
                        break;
                    case '&':
                        token = c.toString();
                        c = getCh();
                        token += c;
                        if (token.equals(Operator.And.getName())) {
                            wordStream.add(new Word(Operator.And.getId()));
                        } else {
                            System.out.println("error");
                            pos--;
                        }
                        break;
                    case '|':
                        token = c.toString();
                        c = getCh();
                        token += c;
                        if (token.equals(Operator.Or.getName())) {
                            wordStream.add(new Word(Operator.Or.getId()));
                        } else {
                            System.out.println("error");
                            pos--;
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
        Word over = new Word(Reserve.Over.getId());
        wordStream.add(over);
        return wordStream;
    }


    public void printWordStream(){
        for(Word w : wordStream){
            System.out.println("( "+w.getType() + ",  " + w.getValue()+" )");
        }
    }


}
