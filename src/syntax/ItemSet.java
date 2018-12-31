package syntax;

import java.util.Set;
import java.util.HashSet;

public class ItemSet extends HashSet {
    @Override
    public boolean contains(Object o) {
        if(!(o instanceof Item)){
            return false;
        }
        super.contains(o);
        return false;
    }
}
