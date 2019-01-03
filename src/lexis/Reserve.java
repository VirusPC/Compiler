package lexis;

public enum Reserve {
    /**
     * ±£Áô×Ö 40~79
     */
    Auto(0), Break(1), Case(2), Char(3), Const(4), Continue(5),
    Default(6), Do(7), Double(8), Else(9), Enum(10), Extern(11),
    Float(12), For(13), Goto(14), If(15), Int(16), Long(17), Register(18),
    Return(19), Short(20), Signed(21), Sizeof(22), Static(23), Struct(24),
    Switch(25), Typedef(26), Union(27), Usigned(28), Void(29), Volatile(30), While(31),
    Printf(32), Scanf(33), Main(34), Over(35), Bool(36);
    private int id;
    Reserve(int id){
        this.id = id+40;
    }
    public Integer getId() {
        return id;
    }
}
