package lexis;

public class Word {
    public int type;
    public String value = "_";

    public  Word(int type) {
        this.type = type;
    }

    public Word(int type, String value){
        this.type = type;
        this.value = value;
    }
}
