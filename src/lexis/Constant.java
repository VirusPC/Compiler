package lexis;

public enum Constant {

    /**
     * �����������ռ�91~
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
