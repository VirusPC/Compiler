package lexis;

public class Info {
    private Integer typeId;
    private String value;

    public Info(){super();}
    public Info(Integer typeId, String value) {
        super();
        this.typeId = typeId;
        this.value = value;
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
