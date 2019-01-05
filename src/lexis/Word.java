package lexis;

public class Word {
    private Integer type;
    private Integer lineNum;
	private Integer wordNum;
    private String value;

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
