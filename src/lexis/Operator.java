package lexis;

public enum Operator {
    /**
     * ²Ù×÷·û ±àºÅ¿Õ¼ä0~19
     */
    Plus(0, "+"), Minus(1, "-"), Mul(2, "*"), Div(3, "/"), Equal(4, "="), DEqual(5, "=="),
    Less(6, "<"), More(7, ">"), LE(8, "<="), ME(9, ">="), Not(10, "!"),NE(11, "!="),
    And(12, "&&"), Or(13, "||"), SelfAdd(14, "++"), SelfSub(15, "--");
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
