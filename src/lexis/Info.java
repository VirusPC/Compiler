package lexis;

public class Info {
    private Integer typeId;
    private Integer value;

    public Info(Integer typeId, Integer value) {
        this.typeId = typeId;
        this.value = value;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
