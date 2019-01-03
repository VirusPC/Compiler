package lexis;

public enum Identifier {
    /**
     * ±êÊ¶·û
     */
    Id(90);

    private int id;
    Identifier(int id){
        this.id = id;
    }
    public int getId(){
        return this.id;
    }
}
