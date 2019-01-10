package syntax;

public class SemanticNode {
    private Integer type; //类型
    private String place; //值
    private Integer fc; //真出口
    private Integer tc; //假出口
    private Integer chain; //保留待回填的链头
    private Integer quad; //保留while后第一个四元式的序号

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
