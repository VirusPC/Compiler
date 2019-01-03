package lexis;

public class Word {
    private Integer type;
    private Integer varableType;
    private String value; //仅当为标识符或常量时才存在value

    public Word(){

    }

    public  Word(Integer type) {
        this(type, null);
    }

    public Word(Integer type, String value){
        this.type = type;
        this.value = value;
        this.varableType = -1;
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

    public Integer getVarableType() {
        return varableType;
    }

    public void setVarableType(Integer varableType) {
        this.varableType = varableType;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
