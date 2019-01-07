package lexis;

public class Word {
    private Integer type; //类型
    private Integer lineNum; //所在行数
	private Integer wordNum; //单词所在位数
    private String value; //值（只有关键字和常量有值）

    public Word(){

    }

    public  Word(Integer type) {
        this(type, null, null, null);
    }

    public  Word(Integer type, Integer lineNum, Integer wordNum) {
        this(type, null, lineNum, wordNum);
    }

    public Word(Integer type, String value, Integer lineNum, Integer wordNum){
        this.type = type;
        this.value = value;
        this.lineNum = lineNum;
		this.wordNum = wordNum;
    }

    public Integer getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
