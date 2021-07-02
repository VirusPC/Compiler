package lexis;

import org.junit.Test;

public class enumTest {
    @Test
    public void testReserve(){
        for(Reserve r : Reserve.values()){
            String s = r.toString().toLowerCase();
            System.out.println(s);
        }
    }
}
