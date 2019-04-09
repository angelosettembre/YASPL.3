import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java_cup.runtime.Symbol;

public class TesterLex {

	public static void main(String[] args) throws Exception {
		Lexer lexer;
		File file = new File("yaspl3.yasp");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		lexer = new Lexer(fis);
		Symbol o;
		do {
			o = lexer.next_token();
			System.out.println("TOKEN="+ParserSym.terminalNames[o.sym] + " (" + o.value + ")" + "\n");
		}while(o.sym!=ParserSym.EOF);
	}
}
