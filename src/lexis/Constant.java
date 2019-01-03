package lexis;

public enum Constant {

    /**
     * 常量，命名空间91~
     */
    Num(0), Char(1);

    private int id;
    Constant(int id){
        this.id = id+91;
    }

    public int getId() {
        return id;
    }
}
