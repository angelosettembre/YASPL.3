package visitors.code_generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import exceptions.VariableNotDeclaredException;
import nodes.ArithOP;
import nodes.AssignOP;
import nodes.Body;
import nodes.BoolOP;
import nodes.CallOP;
import nodes.CharConst;
import nodes.CompStatement;
import nodes.Decl;
import nodes.DoubleConst;
import nodes.Expression;
import nodes.FalseOP;
import nodes.FunctionDeclaration;
import nodes.Identifier;
import nodes.IfThenElseOP;
import nodes.IfThenOP;
import nodes.IntConst;
import nodes.NotOP;
import nodes.ParDef;
import nodes.ParameterDeclaration;
import nodes.ParameterType;
import nodes.Program;
import nodes.ReadOP;
import nodes.RelOP;
import nodes.Statement;
import nodes.StringConst;
import nodes.TrueOP;
import nodes.Type;
import nodes.UminusOP;
import nodes.VarDeclaration;
import nodes.VarDeclarationInit;
import nodes.VarInitValue;
import nodes.WhileOP;
import nodes.WriteOP;
import visitors.Visitor;
import visitors.semantic.SymbolProperties;
import visitors.semantic.SymbolTable;

public class CodeGenerationVisitor implements Visitor<Object> {
	private static final String SPACE = " ";
    private static final String SEMICOLON = ";";
    private static final String NEWLINE = "\n";
    private static final String LEFTRBRA = "(";
    private static final String RIGHTRBRA = ")";
    private static final String LEFTCBRA = "{";
    private static final String RIGHTCBRA = "}";
    private static final String TIMES = "*";
    private static final String WHILE = "while";
    private static final String IF = "if";
    private static final String ELSE = "else";
    private static final String PLUS = "+";
    private static final String MINUS = "-";
    private static final String DIV = "/";
    private Stack<SymbolTable> stackOfTable;			/*STACK DI TABELLE*/
    private String result2c = "";
    private int count = 0;
    private boolean isStat = false;
    private boolean isDefDecl = false;
    private boolean isBody = false;
    private ArrayList<ParDef> listParamOUT = new ArrayList<>();
    private ArrayList<ParDef> st = new ArrayList<>();
    private boolean isPrintf = false;
    private String typeForPrintf = "";
    private boolean iConst = false;
    private boolean dConst = false;
    private boolean cConst = false;
    private boolean sConst = false;
    private List<SymbolTable> listSymTable = new ArrayList<>();
    private int indexSymTableDef = 0;
    private boolean defDeclName = false;
    private boolean inOutScanf = false;
    private boolean isUminus = false;
    private boolean isCallRecursive = false;
    private boolean isAssignINOUT = false;
    private String writeDefType1 = "";
    private String writeDefType2 = "";

    
	public CodeGenerationVisitor(Stack<SymbolTable> stackOfTable, List<SymbolTable> listSymTable) {
		super();
		this.stackOfTable = stackOfTable;
		this.listSymTable = listSymTable;
	}
	
	public Stack<SymbolTable> getStackOfTable() {
		return stackOfTable;
	}

	public void setStackOfTable(Stack<SymbolTable> stackOfTable) {
		this.stackOfTable = stackOfTable;
	}

	@Override
	public Object visit(Program progOP) {	
		result2c += "#include<stdio.h>" + NEWLINE + "#include<stdbool.h>" + NEWLINE+NEWLINE;
		
		for(Decl d: progOP.getDeclarations()) {
			count = 0;
			result2c = (String) d.accept(this) + NEWLINE;
		}
		
		result2c += NEWLINE +"int main() "+LEFTCBRA+NEWLINE;
		
		for(Statement s: progOP.getStatements()) {
			isStat = true;
			result2c += "\t"+(String) s.accept(this) + NEWLINE;
			isStat = false;
		}
		
		result2c += RIGHTCBRA;
		
		return result2c;
	}

	@Override
	public Object visit(WriteOP writeOP) {
		System.out.println("WRITE OP");
		isUminus = false;
		boolean isPrintInDef = false;
		int counter = 0;
		isPrintf = true;
		String writePar = "";
		writePar += "printf(\"";
		for(Expression e: writeOP.getExprList()) {
			counter++;
			dConst = false;
			iConst = false;
			String thisType = "";
			String writeContent = (String) e.accept(this, null);
			System.out.println("write content "+writeContent);
			
			if(isDefDecl) {																		//CASO WRITE NEL BODY DI UNA FUNZIONE
				if(writeContent.contains("\"") && writeContent.contains("\"")) {			//SE E' UN STRING CONST
					isPrintInDef = true;
					if(writeContent.contains("\n")) {										//SE ALL'INTERNO C'E' UN NEWLINE
						String newLine = writeContent.replace("\n", "\\n");
						writePar += newLine.replace("\"", "");
					} else {
						sConst = true;
						writePar += writeContent.replace("\"", "");
					}
					//writePar += "printf(\"" + writeContent.replace("\"", "") + "\");";
				} else if(writeContent.contains("'")) {										//SE E' UN CHAR CONST
					isPrintInDef = true;
					cConst = true;
					writePar += writeContent.replace("'", "");
					//writePar += "printf(\"" + writeContent.replace("'", "") + "\");";
				} else if(writeContent.contains("+") || writeContent.contains("-") || writeContent.contains("*") || writeContent.contains("/")) {			//SE E' UNA OPERAZIONE ARITMETICA					
					isPrintInDef = true;
					System.out.println("dcnntntnt "+dConst+ " e typeForPrin: "+typeForPrintf);
					System.out.println("WRITEEEEE: 1"+writeDefType1);
					System.out.println("WRITEEEEE: 2 "+writeDefType2);
					if(dConst) {												//SE C'E' UN DOUBLE CONST															
						writePar += "%lf";
						//writePar += "printf(\"%lf\\n\"," + writeContent + ");";
					} else if(writeDefType1.contains("int") && writeDefType2.contains("int")) {			//SE L'ESPRESSIONE CONTIENE UN DOUBLE
						writePar += "%d";
						//writePar += "printf(\"%lf\\n\"," + writeContent + ");";
					} else if(writeDefType1.contains("int") && writeDefType2.contains("double")) {			//SE L'ESPRESSIONE CONTIENE UN DOUBLE
						writePar += "%lf";
					} else if(writeDefType1.contains("double") && writeDefType2.contains("int")) {			//SE L'ESPRESSIONE CONTIENE UN DOUBLE
						writePar += "%lf";
					} else if(writeDefType1.contains("double") && writeDefType2.contains("double")) {			//SE L'ESPRESSIONE CONTIENE UN DOUBLE
						writePar += "%lf";
					} else {
						if(isUminus) {													//SE NELL'ESPRESSIONE C'E' UN UMINUS
							writePar += "%lf";
						} else {
							writePar += "%d";
						}
						//writePar += "printf(\"%d\\n\"," + writeContent + ");";
					}
				} else if(writeContent.contains("<") || writeContent.contains(">") || writeContent.contains("<=") || writeContent.contains(">=") || writeContent.contains("==")) {		//SE E' UN RELOP
					isPrintInDef = true;
					writePar += "%d";
					//writePar += "printf(\"%d\\n\"," + writeContent + ");";
				} else if(dConst) {															//SE E' UN DOUBLE CONST
					isPrintInDef = true;
					System.out.println("Double");
					writePar += "%lf";
					//writePar += "printf(\"%lf\\n\"," + writeContent + ");";
				} else if(iConst) {															//SE E' UN INT CONST
					isPrintInDef = true;
					System.out.println("INTTTT");
					writePar += "%d";
					//writePar += "printf(\"%d\\n\"," + writeContent + ");";
				} else {
					thisType = lookup(writeContent).get(writeContent).getSymbolType();				//TIPO DELLA VARIABILE PRESA DALLO STACK
					System.out.println("<-------------- DEF"+thisType);
				}
			} else {																				//WRITE NEL MAIN
				if(stackOfTable.peek().containsKey(writeContent)){
					thisType = stackOfTable.peek().get(writeContent).getSymbolType();				//TIPO DELLA VARIABILE PRESA DALLO STACK
					System.out.println("<--------------"+thisType);
				}
			}
			
			if(thisType.equals("int")) {
				writePar += "%d";
			} else if(thisType.equals("double")) {
				writePar += "%lf";
			} else if(thisType.equals("bool")) {
				writePar += "%d";
			} else if(thisType.equals("char")) {
				writePar += "%c";
			} else if(thisType.equals("string")) {
				writePar += "%s";
			} else {
				System.out.println("<--------------wPAR"+writeContent);
				if(!isPrintInDef) {																//SE NELLA WRITE DELLA FUNZIONE, GIA' E' STATO ASSEGNATO IL TIPO ALLA PRINTF, NON FARE CIO'
					if(writeContent.contains("\"") && writeContent.contains("\"")) {			//SE E' UN STRING CONST
						if(writeContent.contains("\n")) {										//SE ALL'INTERNO C'E' UN NEWLINE
							String newLine = writeContent.replace("\n", "\\n");
							writePar += newLine.replace("\"", "");
						} else {
							sConst = true;
							writePar += writeContent.replace("\"", "");
						}
						//writePar += "printf(\"" + writeContent.replace("\"", "") + "\");";
					} else if(writeContent.contains("'")) {										//SE E' UN CHAR CONST
						cConst = true;
						writePar += writeContent.replace("'", "");
						//writePar += "printf(\"" + writeContent.replace("'", "") + "\");";
					} else if(writeContent.contains("+") || writeContent.contains("-") || writeContent.contains("*") || writeContent.contains("/")) {			//SE E' UNA OPERAZIONE ARITMETICA					
						System.out.println("dcnntntnt "+dConst);
						if(dConst) {												//SE C'E' UN DOUBLE CONST															
							writePar += "%lf";
							//writePar += "printf(\"%lf\\n\"," + writeContent + ");";
						} else if(typeForPrintf.contains("int")) {			//SE L'ESPRESSIONE CONTIENE UN INT
							writePar += "%d";
							//writePar += "printf(\"%lf\\n\"," + writeContent + ");";
						} else if(typeForPrintf.contains("double")) {		//SE L'ESPRESSIONE CONTIENE UN DOUBLE
							writePar += "%lf";
						} else {
							if(isUminus) {															//SE NELL'ESPRESSIONE C'E' UN UMINUS
								writePar += "%d";
							} else {
								writePar += "%lf";
							}
							//writePar += "printf(\"%d\\n\"," + writeContent + ");";
						}
					} else if(writeContent.contains("<") || writeContent.contains(">") || writeContent.contains("<=") || writeContent.contains(">=") || writeContent.contains("==")) {		//SE E' UN RELOP
						writePar += "%d";
						//writePar += "printf(\"%d\\n\"," + writeContent + ");";
					} else if(dConst) {															//SE E' UN DOUBLE CONST
						System.out.println("Double");
						writePar += "%lf";
						//writePar += "printf(\"%lf\\n\"," + writeContent + ");";
					} else if(iConst) {															//SE E' UN INT CONST
						System.out.println("INTTTT");
						writePar += "%d";
						//writePar += "printf(\"%d\\n\"," + writeContent + ");";
					}
				}
			}
			typeForPrintf = "";
			if(counter != writeOP.getExprList().size()) {
				writePar += " ";
			}
		}
		writePar += "\"";
		
		for(Expression e: writeOP.getExprList()) {
			String writeContent = (String) e.accept(this, null);
			System.out.println("write------<<--<-<>"+writeContent);
			if(writeContent.contains("\"") || writeContent.contains("'")) {					//SE E' UNA STRINGA
				if(cConst || sConst) {
					if(writeContent.contains("\"") || writeContent.contains("'")) {
						cConst = false;
						sConst = false;
					} else {
						writePar += ", " + writeContent;
					}
				}
			} else {
				for(ParDef s: listParamOUT) {													//CONTROLLO SE LA VARIABILE E' UN INOUT
					System.out.println("VAL : "+s.getSymbolName()+" VAL 2 WRITE "+writeContent);
					if(s.getSymbolName().equals(writeContent)) {
						System.out.println("OKKKK");
						if(isDefDecl) {														//SE LA PRINT SI TROVA ALL'INTERNO DI UNA FUNZIONE
							System.out.println("Si in DEF");
							writeContent = "*" + writeContent;
							break;
						}
					}
				}
				//if(!inOutPrintf) {
					writePar += ", " + writeContent;
				//}
			}
		}
		
		writePar += ");";
		writePar += "\t\n";
		
		result2c = writePar;
		isPrintf = false;
		isPrintInDef = false;
		isUminus = false;
		
		return result2c;
	}

	@Override
	public Object visit(ReadOP readOP) {
		int counter = 0;
		String readPar = "";
		System.out.println("READ OP ");
		readPar += "scanf(\"";
		for(Identifier id: readOP.getIdentifierList()) {
			counter++;
			String thisType = "";
			
			if(isDefDecl) {
				thisType = lookup(id.getName()).get(id.getName()).getSymbolType();				//TIPO DELLA VARIABILE DALLO STACK
			} else {
				thisType = stackOfTable.peek().get(id.getName()).getSymbolType();				//TIPO DELLA VARIABILE DALLO STACK
				System.out.println("<--------------"+thisType);
			}
			
			/*if(stackOfTable.peek().containsKey(id.getName())){
				thisType = stackOfTable.peek().get(id.getName()).getSymbolType();				//TIPO DELLA VARIABILE DALLO STACK
				System.out.println("<--------------"+thisType);
			}*/
			
			/*INFERENZA DI TIPO*/
			if(thisType.equals("int")) {
				readPar += "%d";
				//readPar += "scanf(\"%d\", &" + id.getName() + ");";
			} else if(thisType.equals("double")) {
				readPar += "%lf";
				//readPar += "scanf(\"%lf\", &" + id.getName() + ");";
			} else if(thisType.equals("bool")) {
				readPar += "%d";
				//readPar += "scanf(\"%d\", &" + id.getName() + ");";
			} else if(thisType.equals("char")) {
				readPar += "%c";
				//readPar += "scanf(\"%c\", &" + id.getName() + ");";
			} else if(thisType.equals("string")) {
				readPar += "%s";
				//readPar += "scanf(\"%s\", &" + id.getName() + ");";
			}  
			if(counter != readOP.getIdentifierList().size()) {
				readPar += " ";
			}
		}
		
		readPar += "\"";
		
		for(Identifier id: readOP.getIdentifierList()) {
			readPar += ", &";
			if(isBody) {													//SE LA READ VIENE EFFETTUATA ALL'INTERNO DI UN BODY DI UNA FUNZIONE
				for(ParDef s: listParamOUT) {								//CONTROLLO SE LA VARIABILE E' DI TIPO INOUT
					if(s.getSymbolName().equals(id.getName())) {
						readPar += "*";										//SI AGGIUNGE (&*) ALLA SCANF
						inOutScanf = true;
						break;
					}
				}
			}
			System.out.println("issss: "+inOutScanf);
			readPar +=  id.getName();
		}	
		
		readPar += ");";
		readPar += "\t\n";
		inOutScanf = false;
		
		result2c = readPar;
		return result2c;
	}

	@Override
	public Object visit(ArithOP arithOP, Object param) {
		result2c = (String) arithOP.getExpr1().accept(this, null);

		if(arithOP.getOperation().equals("AddOP")) {
			result2c += " "+PLUS+" ";;
		} else if(arithOP.getOperation().equals("DiffOP")) {
			result2c += " "+MINUS+" ";;
		} else if(arithOP.getOperation().equals("MulOP")) {
			result2c += " "+TIMES+" ";
		} else if(arithOP.getOperation().equals("DivOP")) {
			result2c += " "+DIV+" ";;
		}
		
		result2c += (String) arithOP.getExpr2().accept(this, null);
		
		if(isDefDecl) {																	//SE CI SI TROVA IN UNA FUNZIONE
			if(arithOP.getExpr1() instanceof Identifier && arithOP.getExpr2() instanceof Identifier) {
				Identifier i1 = (Identifier) arithOP.getExpr1();
				Identifier i2 = (Identifier) arithOP.getExpr2();
				
				writeDefType1 = lookup(i1.getName()).get(i1.getName()).getSymbolType();					//TIPO DEL PRIMO OPERANDO
				writeDefType2 = lookup(i2.getName()).get(i2.getName()).getSymbolType();					//TIPO DEL SECONDO OPERANDO
			}
		}
		
		return result2c;
	}

	@Override
	public Object visit(BoolOP boolOP, Object param) {
		result2c = (String) boolOP.getExpr1().accept(this, null);

		if(boolOP.getOperation().equals("ANDOp")) {
			result2c += " && ";
		} else if(boolOP.getOperation().equals("OROp")) {
			result2c += " || ";
		}
		
		result2c += (String) boolOP.getExpr2().accept(this, null);
		return result2c;
	}

	@Override
	public Object visit(RelOP relOP, Object param) {
		result2c = (String) relOP.getExpr1().accept(this, null);

		if(relOP.getOperation().equals("GTOp")) {
			result2c += " > ";
		} else if(relOP.getOperation().equals("GEOp")) {
			result2c += " >= ";
		} else if(relOP.getOperation().equals("LTOp")) {
			result2c += " < ";
		} else if(relOP.getOperation().equals("LEOp")) {
			result2c += " <= ";
		} else if(relOP.getOperation().equals("EQOp")) {
			result2c += " == ";
		}
		
		result2c += (String) relOP.getExpr2().accept(this, null);
		return result2c;
	}

	@Override
	public Object visit(AssignOP assignOP) {
		System.out.println("\tASSIGN OP");
		String temp = "";
		String assignInStat = "";
		if(assignOP.getIdentifier() != null) {
			System.out.println("yess");
			assignInStat = (String) assignOP.getIdentifier().accept(this, null);
			System.out.println("ASSIGN IN STAT: "+assignInStat);
		}
		if(isStat) {																	//CI SI TROVA IN UNO STATEMENT
			System.out.println("STAT ASSIGN OP");
			result2c = assignInStat + " = " +(String) assignOP.getExpr().accept(this, null) + SEMICOLON;			
		} else {
			if(isBody) {
				//SE L'ASSEGNAZIONE E' STATA FATTA NEL BODY DI UNA FUNZIONE
				System.out.println("STAT ASSIGN OP");
				if(assignInStat.isEmpty()) {
					result2c = assignInStat + " = " +(String) assignOP.getExpr().accept(this, null) + SEMICOLON;			//DICHIARAZIONE NEL BODY DELLA FUNZIONE
				} else {
					System.out.println("STAT ASSIGN OP 1111"+assignInStat);
					for(ParDef s: listParamOUT) {
						System.out.println("SSSS "+s.getSymbolName());
						if(s.getSymbolName().equals(assignInStat)) {					//SE IL PARAMETRO E DI TIPO OUT O INOUT, SI AGGIUNGE LO * ALL'INIZIO DELL'ASSEGNAZIONE
							result2c = "*" + assignInStat + " = " +(String) assignOP.getExpr().accept(this, null) + SEMICOLON;
							if(s.getTypeSymbol().equals("inout")) {																			//SE IL TIPO E' INOUT, METTI LO * NELLA PARTE DESTRA
								System.out.println("COSAAAAA "+s.getSymbolName()+ " " +s.getTypeSymbol()+ " assignInstat "+assignInStat);
								temp = (String) assignOP.getExpr().accept(this, null);
								System.out.println("TEMPP : "+temp + " assign "+assignInStat);
								if(temp.contains(assignInStat)) {																
									String val = temp.replace(s.getSymbolName(), "*"+s.getSymbolName());									
									System.out.println("TEMPP DOPO : "+val);
									result2c = "*" + assignInStat + " = " + val + SEMICOLON;
								} else {
									System.out.println("NONONO");
									result2c = "*" + assignInStat + " = " + (String) assignOP.getExpr().accept(this, null) + SEMICOLON;
								}
							}
							break;
						} else {
							System.out.println("VISITA"+assignInStat);
							isAssignINOUT = true;
							result2c = assignInStat + " = " +(String) assignOP.getExpr().accept(this, null) + SEMICOLON;
							isAssignINOUT = false;
						}
					}
				}
			} else {
				result2c = assignInStat + " = " +(String) assignOP.getExpr().accept(this, null);			//DICHIARAZIONE
			}
		}
		return result2c;
	}

	@Override
	public Object visit(IntConst intConst, Object param) {
		System.out.println("int const");
		iConst = true;
		result2c = String.valueOf(intConst.getIntValue());
		return result2c;
	}

	@Override
	public Object visit(VarDeclaration varDeclar) {
		if(isBody) {															//BODY ATTIVO
			if(varDeclar.getTypeVar().getVarType().equals("string")) {
				result2c = "char ";
			} else {
				result2c = varDeclar.getTypeVar().getVarType() + SPACE;
			}
			
			for(VarDeclarationInit v: varDeclar.getVarList()) {
				count++;
				SymbolProperties s = new SymbolProperties();
				s.setSymbolName(v.getIdentifier().getName());
				
				if(varDeclar.getTypeVar().getVarType().equals("string")) {
					result2c += (String) v.getIdentifier().accept(this, s) + "[]";
				} else {
					result2c += (String) v.getIdentifier().accept(this, s);
				}
				
				if(v.getVarValue() != null) {								//CONTROLLO SE VarInitValue � vuoto
					result2c += (String) v.getVarValue().accept(this);
				} else {
					result2c += ";";
				}
			}
			result2c += "\n";
		} else {
			if(varDeclar.getTypeVar().getVarType().equals("string")) {
				result2c += "char ";
			} else {
				result2c += varDeclar.getTypeVar().getVarType() + SPACE;
			}
			
			for(VarDeclarationInit v: varDeclar.getVarList()) {
				count++;
				SymbolProperties s = new SymbolProperties();
				s.setSymbolName(v.getIdentifier().getName());
				
				if(varDeclar.getTypeVar().getVarType().equals("string")) {
					result2c += (String) v.getIdentifier().accept(this, s) + "[]";
				} else {
					result2c += (String) v.getIdentifier().accept(this, s);
				}
				
				if(v.getVarValue() != null) {								//CONTROLLO SE VarInitValue � vuoto
					result2c += (String) v.getVarValue().accept(this);
				}
			}
			
			result2c += SEMICOLON;
		}
			
		return result2c;
	}

	@Override
	public Object visit(VarInitValue varInitValue) {
		result2c = (String) varInitValue.getAssign().accept(this);
		return result2c;
	}

	@Override
	public Object visit(Type type) {
		result2c = type.getVarType();
		return result2c;
	}

	@Override
	public Object visit(Identifier id, Object param) {
		System.out.println("identifier");
		if(isStat) {												//SE SI TROVA ALL'INTERNO DI UNO STATEMENT
			System.out.println("stattt: "+id.getName());
			if(isPrintf) {																		//SE E' UNA OPERAZIONE DI WRITE	
				typeForPrintf += stackOfTable.peek().get(id.getName()).getSymbolType() + "-";	//TIPO DELL'IDENTIFICATORE PER CONTROLLARE IL TIPO DELL'ARITHOP
			}
			result2c = id.getName();
		} else if(isDefDecl) {												//SE LA VISITA E' STATA CHIAMATA DA UNA FUNZIONE
			System.out.println("deff: "+id.getName());
			if(defDeclName) {
				System.out.println("CI VAII");
				System.out.println("stakkckc "+stackOfTable);
				st = stackOfTable.firstElement().get(id.getName()).getListParameterDef();				
				System.out.println("\t\t\t\t\t ---___>_-<-<-<-<-<-"+st);
				for(ParDef pd: st) {																			//LISTA DI PARAMETRI OUT ALL'INTERNO DI UNA DICHIARAZIONE DI FUNZIONE
					if(pd.getTypeSymbol().equals("out") || pd.getTypeSymbol().equals("inout")) {
						listParamOUT.add(pd);							//SALVATAGGIO NELLA LISTA DEI PARAMETRI SOLO DEI TIPI "OUT" O "INOUT"
					}
				}
			}
			if(isCallRecursive || isAssignINOUT) {							//SE ALL'INTERNO DELLA CHIAMATA RICORSIVA C'E' UNA VARIABILE DI TIPO INOUT, OPPURE NELLA PARTE DESTRA DELL'ASSEGNAZIONE C'E' UN INOUT
				System.out.println("111idddd::::: "+id.getName() + "SYT "+st);
				for(ParDef pd: st) {
					if(pd.getSymbolName().equals(id.getName()) && pd.getTypeSymbol().equals("inout")) {
						result2c = "*" + id.getName();
						break;
					} else {
						result2c = id.getName();
					}
				}
			} else {
				result2c = id.getName();
			}
		} else {
			if(count == 1) {
				result2c = id.getName();
			}
			if(count > 1) {
				result2c = ", " + id.getName();
			}
		}
		
		return result2c;
	}

	@Override
	public Object visit(FunctionDeclaration funcDef) {
		System.out.println("def decl");
		int count = 1;
		isDefDecl = true;
				
		System.out.println("STACKKK "+stackOfTable.toString());
		
		defDeclName = true;
		
		result2c += "\nvoid "+(String) funcDef.getIdentifier().accept(this, null) + LEFTRBRA;
		
		defDeclName = false;
		
		stackOfTable.push(listSymTable.get(indexSymTableDef));			//INSERIMENTO NELLO STACK DELLA TABELLA DEI SIMBOLI DELLA FUNZIONE SPECIFICA
		
		if(funcDef.getParameterDec() != null) {
			for(ParameterDeclaration par: funcDef.getParameterDec()) {
				if(count == funcDef.getParameterDec().size()) {
					result2c += (String) par.accept(this);
				} else {
					result2c += (String) par.accept(this)+", ";
					count++;
				}
			}
		}
		
		result2c += RIGHTRBRA;
		
		result2c += "{\n";
		
		result2c = (String) funcDef.getBody().accept(this);
		
		result2c += "\n}";
		
		isDefDecl = false;
		
		stackOfTable.pop();					//POP DALLO STACK
		
		indexSymTableDef++;
		
		return result2c;
	}

	@Override
	public Object visit(ParameterDeclaration parDef) {
		System.out.println("par decl");
		String typeParFunc = "";
		
		typeParFunc = (String) parDef.getTypeParFunc().accept(this);
		System.out.println("\t\tt\t type: "+typeParFunc);
		if(typeParFunc.equals("out") || typeParFunc.equals("inout")) {
			if(parDef.getType().getVarType().equals("string")) {
				result2c = "char"+TIMES;
			} else {
				result2c = parDef.getType().getVarType()+TIMES;
			}
		} else {
			if(parDef.getType().getVarType().equals("string")) {
				result2c = "char";
			} else {
				result2c = parDef.getType().getVarType();
			}
		}
		result2c += SPACE+(String) parDef.getIdentifier().accept(this, null);
		
		if(parDef.getType().getVarType().equals("string")) {
			result2c += "[]";
		}
		
		return result2c;
	}

	@Override
	public Object visit(ParameterType parType) {
		result2c = parType.getTypeParFunc();
		return result2c;
	}

	@Override
	public Object visit(Body body) {
		isBody = true;
		for(VarDeclaration var: body.getVarDecls()) {
			result2c += "\t"+(String) var.accept(this);
		}
		for(Statement stmt: body.getStmt()) {
			System.out.println("BODY STATEMENT");
			result2c += "\t"+(String) stmt.accept(this) + "\n";
		}
		isBody = false;
		return result2c;
	}

	@Override
	public Object visit(CallOP callOP) {
		int countOUT = 0;
		result2c = (String) callOP.getIdentifier().accept(this, null)+"(";
		
		if(callOP.getExprList() != null) {
			for(Expression expr: callOP.getExprList()) {
				if(countOUT > 0) {
					result2c += ",";
				}
				ParDef pd = stackOfTable.firstElement().get(callOP.getIdentifier().getName()).getListParameterDef().get(countOUT);
				if(isStat) {																							//CHIAMATA NEL MAIN
					if(pd.getTypeSymbol().equals("out") || pd.getTypeSymbol().equals("inout")) {
						result2c += "&";
					}
				} else {																			//CHIAMATA RICORSIVA ALL'INTERNO DELLA FUNZIONE
					if(pd.getTypeSymbol().equals("out")) {
						result2c += "&*";
					} else if(pd.getTypeSymbol().equals("inout")) {
						isCallRecursive = true;
						result2c += "&";
					}
				}
				result2c += (String) expr.accept(this, null);
				countOUT++;
			}
		}
		
		isCallRecursive = false;
		
		return result2c+");";
	}

	@Override
	public Object visit(IfThenElseOP ifThenElseOP) {
		System.out.println("if then op else");
		result2c = IF+"(" + (String) ifThenElseOP.getExpressionList().accept(this, null) + ") {\n\t";
		result2c = (String) ifThenElseOP.getCompStmt().accept(this)+"\n";
		result2c += "\t}";
		
		result2c += " "+ELSE+" {\n\t\t";
		result2c = (String) ifThenElseOP.getCompStmtElse().accept(this)+"\n";
		result2c += "\t}";
		
		return result2c;
	}

	@Override
	public Object visit(CompStatement compStat) {
		for(Statement stmt: compStat.getStmts()) {
			result2c += ""+stmt.accept(this) + "\n\t\t";
		}
		return result2c;
	}

	@Override
	public Object visit(IfThenOP ifThenOP) {
		System.out.println("if then op");
		result2c = IF+"(" + (String) ifThenOP.getExpressionList().accept(this, null) + ") {\n\t\t";
		result2c = (String) ifThenOP.getCompStmt().accept(this)+"\n";
		
		result2c += "\t}";
		return result2c;
	}

	@Override
	public Object visit(WhileOP whileOP) {
		result2c = WHILE+"(" + (String) whileOP.getExpressionList().accept(this, null) + ") {\n\t\t";
		result2c = (String) whileOP.getCompStmt().accept(this);
		
		result2c += "\n\t}";
		return result2c;
	}

	@Override
	public Object visit(UminusOP uminusOP, Object param) {
		System.out.println("uminus op");
		isUminus = true;
		result2c = "-"+LEFTRBRA+(String) uminusOP.getExpr().accept(this, null)+RIGHTRBRA;
		return result2c;
	}

	@Override
	public Object visit(NotOP notOP, Object param) {
		result2c = "!"+LEFTRBRA+(String) notOP.getExpr().accept(this, null)+RIGHTRBRA;
		return result2c;
	}

	@Override
	public Object visit(TrueOP trueOP, Object param) {
		result2c = String.valueOf(trueOP.getValue());
		return result2c;
	}

	@Override
	public Object visit(FalseOP falseOP, Object param) {
		result2c = String.valueOf(falseOP.getValue());
		return result2c;
	}

	@Override
	public Object visit(DoubleConst doubleConst, Object param) {
		System.out.println("double const");
		dConst = true;
		result2c = String.valueOf(doubleConst.getDoubleValue());
		return result2c;
	}

	@Override
	public Object visit(CharConst charConst, Object param) {
		result2c = "'" + String.valueOf(charConst.getCharValue()) + "'";
		return result2c;
	}

	@Override
	public Object visit(StringConst stringConst, Object param) {
		result2c = "\"" + stringConst.getStringValue() + "\"";
		return result2c;
	}
	
	public SymbolTable lookup(String id){
        int index = stackOfTable.size()-1;       //Size stack = 2
        System.out.println("-----stack size = "+index);

        SymbolTable sTable = new SymbolTable();

        if(!stackOfTable.get(index).containsKey(id)){      //Check scope attuale
            index--;
            if(!stackOfTable.get(index).containsKey(id)){  //Check scope-1
                try {
                    throw new VariableNotDeclaredException("VARIABILE NON TROVATA NELLO STACK!");
                } catch (VariableNotDeclaredException e) {
                	e.printStackTrace();
                    System.out.println(e.getMessage());
                    System.exit(0);
                }    
            }else{
                sTable = stackOfTable.get(index);
            }
        }else{
            sTable = stackOfTable.get(index);
        }

        return sTable;
	}
}
