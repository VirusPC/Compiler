package syntax;

public class SemanticNode {
    private Integer type; //����
    private String place; //ֵ
    private Integer fc; //�����
    private Integer tc; //�ٳ���
    private Integer chain; //�������������ͷ
    private Integer quad; //����while���һ����Ԫʽ�����

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }


    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Integer getFc() {
        return fc;
    }

    public void setFc(Integer fc) {
        this.fc = fc;
    }

    public Integer getTc() {
        return tc;
    }

    public void setTc(Integer tc) {
        this.tc = tc;
    }

    public Integer getQuad(){
        return quad;
    }

    public void setQuad(Integer quad){
        this.quad = quad;
    }

    public Integer getChain(){
        return chain;
    }

    public void setChain(Integer chain){
        this.chain = chain;
    }
}
