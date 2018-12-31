package syntax;

public class Item {

    private Integer grammarId;
    private Integer pointPos;
    private String forward;

    public Item(Integer grammarId, Integer pointPos, String forward) {
        this.grammarId = grammarId;
        this.pointPos = pointPos;
        this.forward = forward;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Item) {
            Item item = (Item) o;
            if (this.grammarId.equals(item.grammarId)
                    && this.pointPos.equals(item.pointPos)
                    && this.forward.equals(item.forward)) {
                return true;
            }
        }
        return  false;
    }


    @Override
    public int hashCode() {
        return 1;
    }

    public Integer getGrammarId() {
        return grammarId;
    }

    public void setGrammarId(Integer grammarId) {
        this.grammarId = grammarId;
    }

    public Integer getpointPos() {
        return pointPos;
    }

    public void setpointPos(Integer pointPos) {
        this.pointPos = pointPos;
    }

    public String getForward() {
        return forward;
    }

    public void setForward(String forward) {
        this.forward = forward;
    }

}
