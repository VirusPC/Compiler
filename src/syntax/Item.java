package syntax;
import java.util.Set;

public class Item {

    private Integer grammarId;
    private Integer pointPos;
    private Set<String> forwards;

    public Item(Integer grammarId, Integer pointPos, Set<String> forwards) {
        this.grammarId = grammarId;
        this.pointPos = pointPos;
        this.forwards = forwards;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Item) {
            Item item = (Item) o;
            if (this.grammarId.equals(item.grammarId)
                    && this.pointPos.equals(item.pointPos)
                    && this.forwards.size()==item.forwards.size()
                    && this.forwards.containsAll(item.forwards)){ //此行和上面一行可用equals代替
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

    public Set<String> getForwards() {
        return forwards;
    }

    public void setForwards(Set<String> forward) {
        this.forwards = forwards;
    }

}
