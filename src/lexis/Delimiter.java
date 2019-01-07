package lexis;

public enum Delimiter {
    /**
     * ½ç·û ±àºÅ¿Õ¼ä20~39
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
