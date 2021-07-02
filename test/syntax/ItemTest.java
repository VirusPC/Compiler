package syntax;

import org.junit.Test;
import java.util.Set;
import java.util.HashSet;
import  java.util.List;
import  java.util.ArrayList;

public class ItemTest {
    @Test
    public void testItem() {
            Set<Item> itemSet1 = new HashSet<Item>();
            Set<Item> itemSet2 = new HashSet<Item>();
            Set<String> forward1 = new HashSet<String>();
            Set<String> forward2 = new HashSet<String>();
            forward1.add("a");
            forward2.add("a");
            Item item1 = new Item(1, 2, forward1);
            Item item2 = new Item(1, 2, forward2);
            itemSet1.add(item1);
            itemSet2.add(item2);
            System.out.println(itemSet1.equals(itemSet2));
    }

    @Test
    public void testArrayList(){
        List<String> list= new ArrayList<String>();
        list.add("0");
        list.add("1");
        list.add("2");
        list.add("3");
        System.out.println(list.subList(1, 3));
    }

    @Test
    public void testHashSet(){
        Set<String> set = new HashSet<String>();
        set.add("0");
        System.out.println(set.toArray());
    }

}
