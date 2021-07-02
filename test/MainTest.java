import assembly.Asm;
import lexis.Lexer;
import syntax.Parser;

public class MainTest {
    public static void main(String[] args){
        Lexer lexer = new Lexer("src/resource/code/semanticTestCode.txt");
        lexer.printWordStream();

        Parser parser = new Parser("src/resource/syntax/semanticTest.txt", "program");
        parser.parseWordStream(lexer.getWordStream());
        parser.printSymbolTable();
        parser.printFourElementList();

        Asm asm = new Asm(parser.getFourElementList(), parser.getSymbolTable());
        asm.printtAsm();
        //asm.writeAsmToFile("src/resource/assembly", "objectCode.asm");
    }
}
