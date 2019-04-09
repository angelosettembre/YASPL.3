import java_cup.runtime.Symbol; 		//This is how we pass tokens to the parser
import java.util.*;

%%
%eofval{
 System.out.println("EOF \n"); return new Symbol(ParserSym.EOF);
%eofval}

										// Declarations for JFlex
%class Lexer
%implements ParserSym
%unicode 								// We wish to read text files
%cup 									// Declare that we expect to use Java CUP
%line									// Activate count of line
%column									// Activate count of column
%{   
    StringBuffer string = new StringBuffer();
    
    private Symbol symbol(int type) {
        return new Symbol(type, yyline+1, yycolumn+1);
  	}
  	
  	private Symbol symbol(int type, Object value) {
    	return new Symbol(type, yyline+1, yycolumn+1, value);
  	}
%}

/* Regular definition */
LineTerminator = \r|\n|\r\n 
InputCharacter = [^\r\n] /*All except carriage return*/
WhiteSpace = {LineTerminator} | [ \t\f] /*White space can be a line terminator and must be ignored*/
IntLiteral = 0 | [1-9][0-9]* /*Decimal number is 0 or start with a number that not is zero followed by 0 or plus digits*/
DoubleLiteral = (0 | [1-9][0-9]*)\.[0-9]+

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment} | {DocumentationComment}
TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
// Comment can be the last line of the file, without line terminator.
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?
DocumentationComment = "/**" {CommentContent} "*"+ "/"
CommentContent = ( [^*] | \*+ [^/*] )*

Identifier = [:jletter:] [:jletterdigit:]* /*identifier must start with a letter*/

%state STRING, CHARLITERAL

%%
<YYINITIAL> {										// Now for the actual tokens and assocated actions
	{Comment} { /*Ignore Comments*/}
  	{WhiteSpace} { /*Ignore Whitespaces*/ }
	"head" { System.out.println("head"); return symbol(ParserSym.HEAD); }
    "start" { System.out.println("start"); return symbol(ParserSym.START); }
    "int" {System.out.println("int");  return symbol(ParserSym.INT); }
    "bool" {System.out.println("bool");  return symbol(ParserSym.BOOL); }
    "double" {System.out.println("double"); return symbol(ParserSym.DOUBLE); }
    "string" {System.out.println("string"); return symbol(ParserSym.STRING); }
    "char" {System.out.println("char"); return symbol(ParserSym.CHAR); }
    "def" {System.out.println("def"); return symbol(ParserSym.DEF); }
    "<-" {System.out.println("<-"); return symbol(ParserSym.READ); }
    "->" {System.out.println("->"); return symbol(ParserSym.WRITE); }
    "true" {System.out.println("true"); return symbol(ParserSym.TRUE); }
    "false" {System.out.println("false"); return symbol(ParserSym.FALSE); }
    "=" {System.out.println("="); return symbol(ParserSym.ASSIGN); }
    "if" {System.out.println("if"); return symbol(ParserSym.IF); }
    "then" {System.out.println("then"); return symbol(ParserSym.THEN); }
    "while" {System.out.println("while"); return symbol(ParserSym.WHILE); }
    "do" {System.out.println("do"); return symbol(ParserSym.DO); }
    "else" {System.out.println("else"); return symbol(ParserSym.ELSE); }
    "(" {System.out.println("("); return symbol(ParserSym.LPAR); }
    ")" {System.out.println(")"); return symbol(ParserSym.RPAR); }
    "{" {System.out.println("{"); return symbol(ParserSym.LGPAR); }
    "}" {System.out.println("}"); return symbol(ParserSym.RGPAR); }
    "," {System.out.println(","); return symbol(ParserSym.COMMA); }
    ";" {System.out.println(";"); return symbol(ParserSym.SEMI); }
    "+" {System.out.println("+"); return symbol(ParserSym.PLUS); }
    "-" {System.out.println("-"); return symbol(ParserSym.MINUS); }
    "*" {System.out.println("*"); return symbol(ParserSym.TIMES); }
    "/" {System.out.println("/"); return symbol(ParserSym.DIV); }
    ">" {System.out.println(">"); return symbol(ParserSym.GT); }
    ">=" {System.out.println(">="); return symbol(ParserSym.GE); }
    "<=" {System.out.println("<="); return symbol(ParserSym.LE); }
    "<" {System.out.println("<"); return symbol(ParserSym.LT); }
    "==" {System.out.println("=="); return symbol(ParserSym.EQ); }
    "not" {System.out.println("not"); return symbol(ParserSym.NOT); }
    "and" {System.out.println("and"); return symbol(ParserSym.AND); }
    "or" {System.out.println("or"); return symbol(ParserSym.OR); }
    "in" {System.out.println("in"); return symbol(ParserSym.IN); }
    "out" {System.out.println("out"); return symbol(ParserSym.OUT); }
    "inout" {System.out.println("inout"); return symbol(ParserSym.INOUT); }
    {Identifier} {System.out.println(yytext()); return symbol(ParserSym.ID, yytext()); }
    {IntLiteral} {System.out.println(yytext());return symbol(ParserSym.INT_CONST, Integer.parseInt(yytext())); }
    {DoubleLiteral} {System.out.println(yytext()); return symbol(ParserSym.DOUBLE_CONST, Double.parseDouble(yytext())); }
    
    /*When found " start state string*/
    \"   { yybegin(STRING); string.setLength(0);  }
    
    /* character literal */
  	\'                             { yybegin(CHARLITERAL); }
}

/*Handle String state*/
<STRING> {
      \"                             { yybegin(YYINITIAL);
      								   System.out.println(string.toString()); 
                                       }
      [^\n\r\"\\]+                   { string.append( yytext() ); return symbol(ParserSym.STRING_CONST, string.toString()); }
      \\t                            { string.append('\t'); }
      \\n                            { string.append('\n'); }

      \\r                            { string.append('\r'); }
      \\\"                           { string.append('\"'); }
      \\                             { string.append('\\'); }
}

<CHARLITERAL> {
  [^\r\n\'\\]\'            { yybegin(YYINITIAL); return symbol(ParserSym.CHAR_CONST, yytext().charAt(0)); }
  
  /* escape sequences */
  "\\b"\'                        { yybegin(YYINITIAL); return symbol(ParserSym.CHAR_CONST, '\b');}
  "\\t"\'                        { yybegin(YYINITIAL); return symbol(ParserSym.CHAR_CONST, '\t');}
  "\\n"\'                        { yybegin(YYINITIAL); return symbol(ParserSym.CHAR_CONST, '\n');}
  "\\f"\'                        { yybegin(YYINITIAL); return symbol(ParserSym.CHAR_CONST, '\f');}
  "\\r"\'                        { yybegin(YYINITIAL); return symbol(ParserSym.CHAR_CONST, '\r');}
  "\\\""\'                       { yybegin(YYINITIAL); return symbol(ParserSym.CHAR_CONST, '\"');}
  "\\'"\'                        { yybegin(YYINITIAL); return symbol(ParserSym.CHAR_CONST, '\'');}
  "\\\\"\'                       { yybegin(YYINITIAL); return symbol(ParserSym.CHAR_CONST, '\\'); }
}

/* error fallback */
[^]|\n  { throw new Error("Illegal character <"+yytext()+">"); }