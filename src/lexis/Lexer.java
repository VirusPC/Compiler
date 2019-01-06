package lexis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Lexer {
    

    private String sourceFilePath;
    private int pos;
    private int lineNum;
	private int wordNum;

    private List<Word> wordStream = new ArrayList();
	private List<char[]> lines;




    public Lexer(String sourceFilePath){
        this.sourceFilePath = sourceFilePath;
        getBuffer();
        analyse();
        int i = 0;
    }


    private Character getCh(){
       char[] line = lines.get(lineNum-1);
	   if(pos>=line.length){return null;}
	   return line[pos++];
    }

    private boolean getBuffer(){
		lines = new ArrayList<char[]>();
		String line = null;
        try {
            FileReader fr = new FileReader(sourceFilePath);
            BufferedReader br = new BufferedReader(fr);
            while((line=br.readLine())!=null){
				lines.add(line.toCharArray());
			}
            br.close();
            fr.close();
        } catch (Exception e) {
            System.out.println("Source File Error");
        }
        return true;
    }


    /**
     * 扫描一次读出一个单词
     * @return
     */
    private boolean scan(){
        String token = "";
        Character c = getCh();

        if(c==null){
            return false;
        }

        while(c=='\r' || c=='\n' || c==' ' || c=='\t'){
            c = getCh();
        }
        if(c==null){
            return false;
        }
        if(Character.isLetter(c)){
            /**
             *首字符为字母，为标识符或保留字
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
                wordStream.add(new Word(type, lineNum, wordNum));
            } else {
                wordStream.add(new Word(Identifier.Id.getId(), token, lineNum, wordNum));
            }
            if(c==null){
                return false;
            }
        } else if(Character.isDigit(c)) {
            /**
             * 为数字
             */
            while(c!=null&&Character.isDigit(c)){
                token = token.concat(String.valueOf(c));
                c = getCh();
            }
            wordStream.add(new Word(Constant.Num.getId(), token, lineNum, wordNum));
            if(c==null){
                return false;
            }
            pos--;
        } else{
            /**
             * 首字符为特殊符号
             */
            Word word = null;
            /**
             * 界符
             */
            for(Delimiter d : Delimiter.values()){
                if(d.getName().equals(String.valueOf(c))){
                    word = new Word(d.getId(), lineNum, wordNum);
                    break;
                }
            }

            /**
            操作符
             */
            if(word == null) {
                switch (c) {
                    case '+':
                        c = getCh();
                        if (c == Operator.Plus.getName().charAt(0)) {
                            word = new Word(Operator.SelfAdd.getId(), lineNum, wordNum);
                        } else {
                            pos--;
                            word = new Word(Operator.Plus.getId(), lineNum, wordNum);
                        }
                        break;
                    case '-':
                        c = getCh();
                        if (c == Operator.Minus.getName().charAt(0)) {
                            word = new Word(Operator.SelfSub.getId(), lineNum, wordNum);
                        } else {
                            pos--;
                            word = new Word(Operator.Minus.getId(), lineNum, wordNum);
                        }
                        break;
                    case '*':
                        word = new Word(Operator.Mul.getId(), lineNum, wordNum);
                        break;
                    case '/':
                        word = new Word(Operator.Div.getId(), lineNum, wordNum);
                        break;
                    case '<':
                        c = getCh();
                        if (c == Operator.Equal.getName().charAt(0)) {
                            word = new Word(Operator.LE.getId(), lineNum, wordNum);
                        } else {
                            pos--;
                            word = new Word(Operator.Less.getId(), lineNum, wordNum);
                        }
                        break;
                    case '>':
                        c = getCh();
                        if (c == Operator.Equal.getName().charAt(0)) {
                            wordStream.add(new Word(Operator.ME.getId()));
                        } else {
                            pos--;
                            word = new Word(Operator.More.getId(), lineNum, wordNum);
                        }
                        break;
                    case '!':
                        c = getCh();
                        if (c == Operator.Equal.getName().charAt(0)) {
                            word = new Word(Operator.NE.getId(), lineNum, wordNum);
                        } else {
                            pos--;
                            word = new Word(Operator.Not.getId(), lineNum, wordNum);
                        }
                        break;
                    case '=':
                        c = getCh();
                        if (c == Operator.Equal.getName().charAt(0)) {
                            word = new Word(Operator.DEqual.getId(), lineNum, wordNum);
                        } else {
                            pos--;
                            word = new Word(Operator.Equal.getId(), lineNum, wordNum);
                        }
                        break;
                    case '&':
                        token = c.toString();
                        c = getCh();
                        token += c;
                        if (token.equals(Operator.And.getName())) {
                            word = new Word(Operator.And.getId(), lineNum, wordNum);
                        } else {
                            System.err.println("第"+lineNum+"行，第"+wordNum+"个单词错误");
                            pos--;
                        }
                        break;
                    case '|':
                        token = c.toString();
                        c = getCh();
                        token += c;
                        if (token.equals(Operator.Or.getName())) {
                            word = new Word(Operator.Or.getId(), lineNum, wordNum);
                        } else {
                            System.err.println("第"+lineNum+"行，第"+wordNum+"个单词错误");
                            pos--;
                        }
                        break;
                    default:
                        System.err.println("第"+lineNum+"行，第"+wordNum+"个单词错误");
                }
            }
            wordStream.add(word);
        }
        if(getCh()==null){
            return false;
        }
        pos--;
		wordNum++;
        return true;
    }

    private void analyse(){
        for(int i=1; i<=lines.size(); i++){
			pos=0;
			lineNum = i;
			wordNum = 1;
			while(scan()){ }
		}
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
