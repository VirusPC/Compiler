package syntax;

public class Info {
	private Kind kind;
    private Integer typeId;
    private String value;

    public Info(){super();}

    public Info(Kind kind){
        this(kind, null, null);
    }

	public Info(Kind kind, Integer typeId) {
		this(kind, typeId, null);
    }
	
    public Info(Kind kind, Integer typeId, String value) {
        super();
		this.kind = kind;
        this.typeId = typeId;
        this.value = value;
    }

	public Kind getKind(){
		return kind;
	}
	
	public void setKind(Kind kind){
		this.kind = kind;
	}
	
    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
