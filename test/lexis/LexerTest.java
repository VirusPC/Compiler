package lexis;

import org.junit.Test;

public class LexerTest {
    @Test
    public void testCreateReserveTable() {
        Lexer l = new Lexer("src/testCode.txt");
        //l.printReserveTable();
        l.printWordStream();
        System.out.println(l.getWordStream().size());
    }
}
