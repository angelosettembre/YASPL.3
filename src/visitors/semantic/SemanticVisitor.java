package visitors.semantic;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import exceptions.DuplicateDeclarationException;
import exceptions.FunctionDeclarationException;
import exceptions.IncorrectUtilizationOfVariableOutException;
import exceptions.MatchArgumentException;
import exceptions.TypeMismatchException;
import exceptions.VariableNotDeclaredException;
import nodes.*;
import visitors.Visitor;

public class SemanticVisitor implements Visitor<Object> {
	private Document xmlDocument;
	private SymbolTable symTable;
	private SymbolTable symTableDef;
	private SymbolProperties symProp;
	public  SymbolProperties sym2Pass;
    private Stack<SymbolTable> stackOfTable;			/*STACK DI TABELLE*/
    private boolean varDec = false;
    private boolean assignDec = false;
    private boolean assignID = false;
    private boolean defDecl = false;
    private boolean bodyFlag = false;
    private boolean varDecBody = false;
    private boolean callFunction = false;
    private int callFunctionCount = 0;
    private ArrayList<ParDef> arrParameterDef = new ArrayList<>();
    private boolean callFunctionName = false;
    private boolean OpExpression = false;
    private List<SymbolTable> listSymTable = new ArrayList<>();
    private boolean isRead = false;
    private boolean isWrite = false;
    private boolean isIF = false;
    private boolean isWhile = false;
    
    /**
     * Constructor and creator of XML-document
     */
    public SemanticVisitor() {
        super();
        this.createDocument();
        this.stackOfTable = new Stack<>();
    }

    /**
     * Method to append the root of the tree
     * @param el is the root of the tree
     */
    public void appendRoot(Element el) {
        this.xmlDocument.appendChild(el);
    }

    /**
     * Method to create factory and builder for the XML-Document
     */
    public void createDocument() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            this.xmlDocument = docBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to print the XML
     */
    public void toXml() {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(this.xmlDocument);
            StreamResult result = new StreamResult(new File("outputSEMANTIC.xml"));
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

	public SemanticVisitor(Stack<SymbolTable> stackOfTable) {
		this.stackOfTable = stackOfTable;
	}

	public Stack<SymbolTable> getStackOfTable() {
		return stackOfTable;
	}

	public void setStackOfTable(Stack<SymbolTable> stackOfTable) {
		this.stackOfTable = stackOfTable;
	}

	public List<SymbolTable> getListSymTable() {
		return listSymTable;
	}

	public void setListSymTable(List<SymbolTable> listSymTable) {
		this.listSymTable = listSymTable;
	}

	@Override
	public Element visit(Program progOP) {
		Element el = this.xmlDocument.createElement("ProgramOP");
				
		symTable = new SymbolTable();											/*CREAZIONE SCOPE PROGRAMMA*/
		stackOfTable.push(symTable);											/*INSERIMENTO TABELLA DEI SIMBOLI NELLO STACK*/
		
		Collections.reverse(progOP.getDeclarations());
		Collections.reverse(progOP.getStatements());
			
		for(Decl d: progOP.getDeclarations()) {
			el.appendChild((Node) d.accept(this));
		}
		varDec = true;									/*DICHIARAZIONI TERMINATE*/
		
		for(Statement s: progOP.getStatements()) {
			el.appendChild((Node) s.accept(this));
		}
		
		/* STAMPA DELLO STACK */
		System.out.println("\n\tSTACK");
		for(int i=0; i<stackOfTable.size(); i++) {
			SymbolTable s = stackOfTable.elementAt(i);
			for (String keys : s.keySet()) {
			   System.out.println("\t\t"+keys + ":\t"+ s.get(keys));
			}
		}

		System.out.println("\n\tTABELLA SIMBOLI DEF ");
		for(SymbolTable sm : listSymTable) {
			for (String keys : sm.keySet()) {
			   System.out.println("\t\t"+keys + ":\t"+ sm.get(keys));
			}
			System.out.println();
		}
		/*
		System.out.println("\n\tSYMBOL TABLE PROGRAM");			
		for (String keys : symTable.keySet()) {
		   System.out.println("\t\t"+keys + ":\t"+ symTable.get(keys));
		}
		
		if(symTableDef != null) {
			System.out.println("\n\tSYMBOL TABLE DEF");
			for (String keys : symTableDef.keySet()) {
			   System.out.println("\t\t"+keys + ":\t"+ symTableDef.get(keys));
			}
		}*/
		
		return el;
	}

	@Override
	public Element visit(WriteOP writeOP) {
		System.out.println("WRITE OP");
		Element el = this.xmlDocument.createElement("WriteOP");
		
		Collections.reverse(writeOP.getExprList());
		
		isWrite = true;
		
		for(Expression e: writeOP.getExprList()) {
			el.appendChild((Node) e.accept(this, null));
		}
		
		isWrite = false;
		
		return el;
	}

	@Override
	public Element visit(ArithOP arithOP, Object s) {
		boolean iSet = false;
		System.out.println("arithOPPPP"+s);
		Element el = this.xmlDocument.createElement(arithOP.getOperation());
		Attr a = this.xmlDocument.createAttribute("type");
		
		if(!OpExpression) {													//SONO IL NODO ARITH OP PADRE, NO CHIAMATA RICORSIVA AD ARITH_OP
			OpExpression = true;
			iSet = true;
		}		
		
		System.out.println("\tEXPR1 ARITH OP "+arithOP.getExpr1().getClass()+" EXPR2 "+arithOP.getExpr2().getClass());
		
		el.appendChild((Node) arithOP.getExpr1().accept(this, s));							//VISITA PER LA PRIMA ESPRESSIONE
		el.appendChild((Node) arithOP.getExpr2().accept(this, s));							//VISITA PER LA SECONDA ESPRESSIONE
		
		System.out.println("\tEXPR1 DOPO ARITH OP "+arithOP.getExpr1().getClass()+" EXPR2 "+arithOP.getExpr2().getClass());

								
		if(!(arithOP.getExpr1() instanceof ArithOP || arithOP.getExpr2() instanceof ArithOP)){					/*SE NESSUNO DEI 2 FIGLI E' UNA ARITH OP*/
			System.out.println("verooo");
			if(el.getFirstChild().getAttributes().item(0).toString().contains("int") || el.getFirstChild().getAttributes().item(0).toString().contains("double")) {			/*CONTROLLO SE IL PRIMO FIGLIO E' UN UminusOP*/
				if(arithOP.getExpr2() instanceof Identifier) {

					Identifier i = (Identifier) arithOP.getExpr2();
					
					SymbolTable st = lookup(i);
					
					if(st.get(i.getName()).getSymbolType().equals("int") && (el.getFirstChild().getAttributes().item(0).toString().contains("int"))) {
						a.setValue(String.valueOf("int"));
						el.setAttributeNode(a);
					} else if(st.get(i.getName()).getSymbolType().equals("int") && (el.getFirstChild().getAttributes().item(0).toString().contains("double"))) {
						a.setValue(String.valueOf("double"));
						el.setAttributeNode(a);
					} else if(st.get(i.getName()).getSymbolType().equals("double") && (el.getFirstChild().getAttributes().item(0).toString().contains("int"))) {
						a.setValue(String.valueOf("double"));
						el.setAttributeNode(a);
					} else if(st.get(i.getName()).getSymbolType().equals("double") && (el.getFirstChild().getAttributes().item(0).toString().contains("double"))) {
						a.setValue(String.valueOf("double"));
						el.setAttributeNode(a);
					} else {
						try {
							throw new TypeMismatchException("TYPE MISMATCH");
						} catch (TypeMismatchException e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
				} else if((arithOP.getExpr2() instanceof IntConst) && (el.getFirstChild().getAttributes().item(0).toString().contains("int"))) {
					a.setValue(String.valueOf("int"));
					el.setAttributeNode(a);
				} else if((arithOP.getExpr2() instanceof IntConst) && (el.getFirstChild().getAttributes().item(0).toString().contains("double"))) {
					a.setValue(String.valueOf("double"));
					el.setAttributeNode(a);
				} else if((arithOP.getExpr2() instanceof DoubleConst) && (el.getFirstChild().getAttributes().item(0).toString().contains("int"))) {
					a.setValue(String.valueOf("double"));
					el.setAttributeNode(a);
				} else if((arithOP.getExpr2() instanceof DoubleConst) && (el.getFirstChild().getAttributes().item(0).toString().contains("double"))) {
					a.setValue(String.valueOf("int"));
					el.setAttributeNode(a);
				} else if((arithOP.getExpr1() instanceof UminusOP) && (arithOP.getExpr2() instanceof UminusOP)) {
					
				} else {
					try {
						throw new TypeMismatchException("TYPE MISMATCH");
					} catch (TypeMismatchException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			} else {
				if((arithOP.getExpr1() instanceof Identifier) && (arithOP.getExpr2() instanceof Identifier)) {						//ENTRAMBI IDENTIFICATORI
					Identifier i1 = (Identifier) arithOP.getExpr1();
					Identifier i2 = (Identifier) arithOP.getExpr2();
										
					SymbolTable st = lookup(i1);
					SymbolTable st1 = lookup(i2);
					
					if(st.get(i1.getName()).getSymbolType().equals("int") && st1.get(i2.getName()).getSymbolType().equals("int")) {
						a.setValue(String.valueOf("int"));
						el.setAttributeNode(a);
					} else if(st.get(i1.getName()).getSymbolType().equals("int") && st1.get(i2.getName()).getSymbolType().equals("double")) {
						a.setValue(String.valueOf("double"));
						el.setAttributeNode(a);
					} else if(st.get(i1.getName()).getSymbolType().equals("double") && st1.get(i2.getName()).getSymbolType().equals("int")) {
						a.setValue(String.valueOf("double"));
						el.setAttributeNode(a);
					} else if(st.get(i1.getName()).getSymbolType().equals("double") && st1.get(i2.getName()).getSymbolType().equals("double")) {
						a.setValue(String.valueOf("double"));
						el.setAttributeNode(a);
					} else {
						try {
							throw new TypeMismatchException("TYPE MISMATCH");
						} catch (TypeMismatchException e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
				} else if((arithOP.getExpr1() instanceof Identifier) && ((arithOP.getExpr2() instanceof IntConst) || arithOP.getExpr2() instanceof DoubleConst)) {
					Identifier i1 = (Identifier) arithOP.getExpr1();
					
					SymbolTable st = lookup(i1);
					
					if(st.get(i1.getName()).getSymbolType().equals("int")) {
						if(arithOP.getExpr2() instanceof IntConst) {
							a.setValue(String.valueOf("int"));												//SE INT_CONST SETTA IL TIPO DEL NOTO A INT
							el.setAttributeNode(a);
						} else {
							a.setValue(String.valueOf("double"));											//SE DOUBLE_CONST SETTA IL TIPO DEL NOTO A DOUBLE
							el.setAttributeNode(a);
						}
					} else if(st.get(i1.getName()).getSymbolType().equals("double")) {
						a.setValue(String.valueOf("double"));
						el.setAttributeNode(a);
					} else {
						try {
							throw new TypeMismatchException("TYPE MISMATCH");
						} catch (TypeMismatchException e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
				} else if(((arithOP.getExpr1() instanceof IntConst) || arithOP.getExpr1() instanceof DoubleConst) && (arithOP.getExpr2() instanceof Identifier)) {
					Identifier i2 = (Identifier) arithOP.getExpr2();
					
					SymbolTable st = lookup(i2);
					
					if(st.get(i2.getName()).getSymbolType().equals("int")) {
						if(arithOP.getExpr1() instanceof IntConst) {
							a.setValue(String.valueOf("int"));
							el.setAttributeNode(a);
						} else {
							a.setValue(String.valueOf("double"));
							el.setAttributeNode(a);
						}
					} else if(st.get(i2.getName()).getSymbolType().equals("double")) {
						a.setValue(String.valueOf("double"));
						el.setAttributeNode(a);
					} else {
						try {
							throw new TypeMismatchException("TYPE MISMATCH");
						} catch (TypeMismatchException e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
				} else if((arithOP.getExpr1() instanceof IntConst) && (arithOP.getExpr2() instanceof IntConst)) {
					System.out.println("siii");
					a.setValue(String.valueOf("int"));
					el.setAttributeNode(a);
					
				} else if((arithOP.getExpr1() instanceof IntConst) && (arithOP.getExpr2() instanceof DoubleConst)) {
					System.out.println("siii 11");
					a.setValue(String.valueOf("double"));
					el.setAttributeNode(a);
					
				} else if((arithOP.getExpr1() instanceof DoubleConst) && (arithOP.getExpr2() instanceof IntConst)) {
					System.out.println("siii 22");
					a.setValue(String.valueOf("double"));
					el.setAttributeNode(a);
					
				} else if((arithOP.getExpr1() instanceof DoubleConst) && (arithOP.getExpr2() instanceof DoubleConst)) {
					System.out.println("siii 33");
					a.setValue(String.valueOf("double"));
					el.setAttributeNode(a);
					
				} else if(((arithOP.getExpr1() instanceof IntConst) || (arithOP.getExpr1() instanceof DoubleConst)) && (arithOP.getExpr2() instanceof UminusOP)) {
					if(el.getLastChild().getAttributes().item(0).toString().contains("int") && (arithOP.getExpr1() instanceof IntConst)){
						a.setValue(String.valueOf("int"));
						el.setAttributeNode(a);
					} else {
						a.setValue(String.valueOf("double"));
						el.setAttributeNode(a);
					}
				} else if((arithOP.getExpr1() instanceof Identifier) && (arithOP.getExpr2() instanceof UminusOP)) {
					Identifier i1 = (Identifier) arithOP.getExpr1();
					
					SymbolTable st = lookup(i1);
					
					if(st.get(i1.getName()).getSymbolType().equals("int") && el.getLastChild().getAttributes().item(0).toString().contains("int")) {
						a.setValue(String.valueOf("int"));
						el.setAttributeNode(a);
					} else if(st.get(i1.getName()).getSymbolType().equals("double")) {
						a.setValue(String.valueOf("double"));
						el.setAttributeNode(a);
					} else {
						try {
							throw new TypeMismatchException("TYPE MISMATCH");
						} catch (TypeMismatchException e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
				} else {
					try {
						throw new TypeMismatchException("TYPE MISMATCH");
					} catch (TypeMismatchException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			}
		} /*else if((arithOP.getExpr1() instanceof ArithOP) || arithOP.getExpr2() instanceof ArithOP) {						
			System.out.println("uno dei due � arithop");*/
		else if((arithOP.getExpr1() instanceof Identifier) && (arithOP.getExpr2() instanceof ArithOP)) {				//SE NELLA OP ARITMETICA CI SONO PIU' OPERAZIONI ARITMETICHE
			Identifier i = (Identifier) arithOP.getExpr1();
			
			SymbolTable st = lookup(i);								//LOOKUP NELLO STACK PER CONTROLLARE LA VARIABILE SE ESISTE, SE ESISTE SI PRENDE IL TIPO DELLA VARIABILE
			
			System.out.println("STAMPA :"+el.getLastChild()+" --- "+el.getLastChild().getAttributes().item(0));
			
			if(st.get(i.getName()).getSymbolType().equals("int") && (el.getLastChild().getAttributes().item(0).toString().contains("int"))) {
				a.setValue(String.valueOf("int"));
				el.setAttributeNode(a);
			} else if(st.get(i.getName()).getSymbolType().equals("int") && (el.getLastChild().getAttributes().item(0).toString().contains("double"))) {
				a.setValue(String.valueOf("double"));
				el.setAttributeNode(a);
			} else if(st.get(i.getName()).getSymbolType().equals("double") && (el.getLastChild().getAttributes().item(0).toString().contains("int"))) {
				a.setValue(String.valueOf("double"));
				el.setAttributeNode(a);
			} else if(st.get(i.getName()).getSymbolType().equals("double") && (el.getLastChild().getAttributes().item(0).toString().contains("double"))) {
				a.setValue(String.valueOf("double"));
				el.setAttributeNode(a);
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else if((arithOP.getExpr1() instanceof ArithOP) && (arithOP.getExpr2() instanceof Identifier)) {
			Identifier i = (Identifier) arithOP.getExpr2();
			
			SymbolTable st = lookup(i);
			
			System.out.println("ID:"+st.get(i.getName()).getSymbolType()+"STAMPA :"+el.getFirstChild()+" --- "+el.getFirstChild().getAttributes().item(0));
						
			if(st.get(i.getName()).getSymbolType().equals("int") && (el.getFirstChild().getAttributes().item(0).toString().contains("int"))) {
				a.setValue(String.valueOf("int"));
				el.setAttributeNode(a);
			} else if(st.get(i.getName()).getSymbolType().equals("int") && (el.getFirstChild().getAttributes().item(0).toString().contains("double"))) {
				a.setValue(String.valueOf("double"));
				el.setAttributeNode(a);
			} else if(st.get(i.getName()).getSymbolType().equals("double") && (el.getFirstChild().getAttributes().item(0).toString().contains("int"))) {
				a.setValue(String.valueOf("double"));
				el.setAttributeNode(a);
			} else if(st.get(i.getName()).getSymbolType().equals("double") && (el.getFirstChild().getAttributes().item(0).toString().contains("double"))) {
				a.setValue(String.valueOf("double"));
				el.setAttributeNode(a);
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else if(arithOP.getExpr1() instanceof Identifier) {
			Identifier i = (Identifier) arithOP.getExpr1();
			
			SymbolTable st = lookup(i);
			
			if(st.get(i.getName()).getSymbolType().equals("int") && (el.getLastChild().getAttributes().item(0).toString().contains("int"))) {
				a.setValue(String.valueOf("int"));
				el.setAttributeNode(a);
			} else if(st.get(i.getName()).getSymbolType().equals("int") && (el.getLastChild().getAttributes().item(0).toString().contains("double"))) {
				a.setValue(String.valueOf("double"));
				el.setAttributeNode(a);
			} else if(st.get(i.getName()).getSymbolType().equals("double") && (el.getLastChild().getAttributes().item(0).toString().contains("int"))) {
				a.setValue(String.valueOf("double"));
				el.setAttributeNode(a);
			} else if(st.get(i.getName()).getSymbolType().equals("double") && (el.getLastChild().getAttributes().item(0).toString().contains("double"))) {
				a.setValue(String.valueOf("double"));
				el.setAttributeNode(a);
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else if((arithOP.getExpr1() instanceof IntConst) && (arithOP.getExpr2() instanceof ArithOP)) {				//SE NELLA OP ARITMETICA CI SONO PIU' OPERAZIONI ARITMETICHE
			if(el.getLastChild().getAttributes().item(0).toString().contains("int")) {
				a.setValue(String.valueOf("int"));
				el.setAttributeNode(a);
			} else if(el.getLastChild().getAttributes().item(0).toString().contains("double")) {
				a.setValue(String.valueOf("double"));
				el.setAttributeNode(a);
			}
		} else if((arithOP.getExpr1() instanceof ArithOP) && (arithOP.getExpr2() instanceof IntConst)) {				//SE NELLA OP ARITMETICA CI SONO PIU' OPERAZIONI ARITMETICHE
			if(el.getFirstChild().getAttributes().item(0).toString().contains("int")) {
				a.setValue(String.valueOf("int"));
				el.setAttributeNode(a);
			} else if(el.getFirstChild().getAttributes().item(0).toString().contains("double")) {
				a.setValue(String.valueOf("double"));
				el.setAttributeNode(a);
			}
		} else if((arithOP.getExpr1() instanceof DoubleConst) && (arithOP.getExpr2() instanceof ArithOP)) {				//SE NELLA OP ARITMETICA CI SONO PIU' OPERAZIONI ARITMETICHE
			a.setValue(String.valueOf("double"));
			el.setAttributeNode(a);
		} else if((arithOP.getExpr1() instanceof ArithOP) && (arithOP.getExpr2() instanceof DoubleConst)) {				//SE NELLA OP ARITMETICA CI SONO PIU' OPERAZIONI ARITMETICHE
			a.setValue(String.valueOf("double"));
			el.setAttributeNode(a);
		} else if((el.getFirstChild().getAttributes().item(0).toString().contains("int")) && el.getLastChild().getAttributes().item(0).toString().contains("int")) {
			a.setValue(String.valueOf("int"));
			el.setAttributeNode(a);
		} else if((el.getFirstChild().getAttributes().item(0).toString().contains("int")) && el.getLastChild().getAttributes().item(0).toString().contains("double")) {
			a.setValue(String.valueOf("double"));
			el.setAttributeNode(a);
		} else if((el.getFirstChild().getAttributes().item(0).toString().contains("double")) && el.getLastChild().getAttributes().item(0).toString().contains("int")) {
			a.setValue(String.valueOf("double"));
			el.setAttributeNode(a);
		} else if((el.getFirstChild().getAttributes().item(0).toString().contains("double")) && el.getLastChild().getAttributes().item(0).toString().contains("double")) {
			a.setValue(String.valueOf("double"));
			el.setAttributeNode(a);
		} else {
			try {
				throw new TypeMismatchException("TYPE MISMATCH");
			} catch (TypeMismatchException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		/*SEMANTICA PER LA CHIAMATA A FUNZIONE PER L'ARITH OP*/
		if(callFunction) {																	//E' UNA CHIAMATA A FUNZIONE
			if(arrParameterDef.get(callFunctionCount).getTypeSymbol().equals("in")) {								//CONTROLLO SE LA VARIABILE E DI TIPO (IN)
				if(arrParameterDef.get(callFunctionCount).getTypeParam().equals("double")) {
					if(a.getValue().equals("double")) {										//TIPO DEL NODO
						
					} else {
						if(!iSet) {																			//SE NON E' L'ARITH OP PADRE, QUINDI IL FIGLIO NON SETTA IL TIPO AL NODO, LO DEMANDA AL PADRE
							if(a.getValue().equals("int")) {
								
							} else {
								try {
									throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
								} catch (TypeMismatchException e) {
									e.printStackTrace();
									System.exit(1);
								}
							}
						} else {
							try {
								throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
							} catch (TypeMismatchException e) {
								e.printStackTrace();
								System.exit(1);
							}
						}
					}
				} else if((arrParameterDef.get(callFunctionCount).getTypeParam().equals("int"))) {
					if(a.getValue().equals("int")) {
						
					} else {
						try {
							throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
						} catch (TypeMismatchException e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
				} else {
					try {
						throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
					} catch (TypeMismatchException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			} else {
				try {
					throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		if(iSet) {
			OpExpression = false;						//E' TERMINATA L'OPERAZIONE ARITMETICA
		}
		
		return el;
	}

	@Override
	public Element visit(IntConst intConst, Object s) {
		SymbolProperties b = new SymbolProperties();
		b = (SymbolProperties) s;
		System.out.println("INT_CONSTTTTTT"+b);
		Element el = this.xmlDocument.createElement("INT_CONST");
		Attr a = this.xmlDocument.createAttribute("value");
		a.setValue(String.valueOf(intConst.getIntValue()));					//SETTO IL VALORE DEL NODO INT_CONST
		el.setAttributeNode(a);
		if (b!= null) {
			System.out.println("int const sssssssss : "+b.getSymbolName());
			
			Identifier i = new Identifier(b.getSymbolName());
			
			SymbolTable st = lookup(i);									//SI VA A CERCARE LA VARIABILE SE ESISTE NELLO STACK
			
			if(((st.get(b.getSymbolName()).getSymbolType().equals("int")) || (st.get(b.getSymbolName()).getSymbolType().equals("double")))) {
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		/*SEMANTICA PER LA CALL_OP*/
		if(callFunction) {
			if(arrParameterDef.get(callFunctionCount).getTypeSymbol().equals("in")) {								//CONTROLLO SE LA VARIABILE E DI TIPO (IN)
				if(OpExpression) {													//SE E' UNA ARITH OP/BOOL_OP/REL_OP/NOT_OP, I CONTROLLI DI TIPO LI DEMANDA ALL'OPERAZIONE
					//Do nothing
				} else {
					if(arrParameterDef.get(callFunctionCount).getTypeParam().equals("int")) {		
						//Do nothing						//SE IL VALORE DI INT_CONST COINCIDE CON IL TIPO DELLA VARIABILE DEFINITO NELLA FUNZIONE
					} else {
						try {
							throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
						} catch (TypeMismatchException e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
				}
			} else {
				try {
					throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		return el;
	}

	/*VISITA PER VAR_DECL*/
	@Override
	public Element visit(VarDeclaration varDeclar) {
		System.out.println("VAR_DECL");
		symProp = new SymbolProperties();

		Element el = this.xmlDocument.createElement("VAR_DECL");
		el.appendChild((Node) varDeclar.getTypeVar().accept(this));
		symProp.setSymbolKind("var");
		
		Collections.reverse(varDeclar.getVarList());
		
		for(VarDeclarationInit v: varDeclar.getVarList()) {
			Element e = this.xmlDocument.createElement("VarInitOP");
			SymbolProperties s = new SymbolProperties();
			s.setSymbolName(v.getIdentifier().getName());
			//System.out.println("VAR DECL INIT "+s);
			e.appendChild((Node) v.getIdentifier().accept(this, s));
			if(v.getVarValue() != null) {								//CONTROLLO SE VarInitValue E' VUOTO
				sym2Pass = s;											//SE NON E' VUOTO SIGNIFICA CHE C'E' UNA ASSEGNAZIONE NELLA DICHIARAZIONE
				e.appendChild((Node) v.getVarValue().accept(this));
			}
			el.appendChild(e);
		}
		
		return el;
	}

	@Override
	public Element visit(Type type) {
		Element el = this.xmlDocument.createElement("TYPE");
		Attr a = this.xmlDocument.createAttribute("type");
		a.setValue(String.valueOf(type.getVarType()));
		el.setAttributeNode(a);
		symProp.setSymbolType(type.getVarType());
		return el;
	}

	@Override
	public Element visit(Identifier id, Object s) {
		System.out.println("IDENTIFIER");
		Element el = this.xmlDocument.createElement("IDENTIFIER");
		Attr a = this.xmlDocument.createAttribute("value");
		a.setValue(String.valueOf(id.getName()));
		el.setAttributeNode(a);
		
		/*****************************************SEMANTICA PER CALLOP*****************************************************/
		if(callFunction) {
			System.out.println("\t\t\t ci entriiiiii");
			System.out.println("iddd "+id.getName());
			if(callFunctionName) {															//SE L'IDENTIFICATORE E' IL NOME DELLA FUNZIONE
				if(symTable.containsKey(id.getName()) && symTable.get(id.getName()).getSymbolKind().equals("function")) {
					System.out.println("okk va bene");
					arrParameterDef = symTable.get(id.getName()).getListParameterDef();		//GET DELLA LISTA DI VARIABILI DICHIARATE NELLA FUNZIONE
					System.out.println("rtttttt "+arrParameterDef);
				} else {
					try {
						throw new FunctionDeclarationException("FUNCTION NOT DECLARED");
					} catch (FunctionDeclarationException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			} else {
				System.out.println("iidddd : "+id.getName());
				System.out.println("\tcounttt : "+callFunctionCount);
				
				SymbolTable st = symTable;											//TABELLA DEI SIMBOLI PRINCIPALE
				
				if(bodyFlag) {
					st = lookup(id);												//SE NELLA FUNZIONE C'E UNA CHIAMATA RICORSIVA
				}
				
				if(st.containsKey(id.getName())) {
					if(OpExpression) {
						//Do nothing
					} else {
						if(arrParameterDef.get(callFunctionCount).getTypeParam().equals(st.get(id.getName()).getSymbolType())) {
							System.out.println("ok match corretto");							//IL TIPO DI DATO DELLA CALL OP E' UGUALE AL TIPO DELLA VARIABILE DICHIARATO NELLA FUNZIONE
						} else {
							try {
								throw new MatchArgumentException("ARGUMENT TYPE MISMATCH");
							} catch (MatchArgumentException e) {
								e.printStackTrace();
								System.exit(1);
							}
						}
					}
				} else {
					try {
						throw new VariableNotDeclaredException("VARIABLE NOT DECLARED");
					} catch (VariableNotDeclaredException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			}
		} /* FINE SEMANTICA CALL_OP */
		else if(defDecl) {									//SE LA DEFINIZIONE DI PARAMETRI DELLA FUNZIONE NON E' FINITA (CI SONO PARAMETRI NELLA FUNZIONE)*/
			System.out.println("NON E' FINITO DEF DECL "+id.getName());
			symTableDef.put(id.getName(), symProp);											//INSERIMENTO DELLE VARIABILI NELLO TAB DEI SIMBOLI DELLA FUNZIONE
		} else if(bodyFlag) {																//CI SI TROVA NEL BODY DELLA FUNZIONE
				System.out.println("BODY ATTIVO  "+id.getName());
				
				if(varDecBody) {										//DICHIARAZIONE DI VARIABILI NEL BODY
					System.out.println("dichiarazioni di variabili nel body");
					if(assignDec) {										//ASSEGNAZIONE IN UNA DICHIARAZIONE
						System.out.println("assegnazione nella dichiarazione nel body");
						SymbolProperties b = (SymbolProperties) s;
						System.out.println("BBBBBB "+b);
						if(b != null) {
							SymbolTable st = lookup(id);					//Lookup variabile alla sinistra dell'assegnazione
							SymbolProperties sp = st.get(id.getName());
							System.out.println("SP:: "+sp.getSymbolType());

							Identifier i = new Identifier(b.getSymbolName());
							System.out.println("iiiii_"+i);
							st = lookup(i);									//Lookup variabile alla destra dell'assegnazione
							SymbolProperties sp2 = st.get(i.getName());
							
							System.out.println("SP2:: "+sp2.getSymbolType());
							if(sp.getSymbolType().equals(sp2.getSymbolType())) {								//SE IL TIPO DELLE VARIABILI COINCIDE
								//do nothing
							} else if(sp.getSymbolType().equals("int") && sp2.getSymbolType().equals("double")) {

							} else {
								try {
									throw new TypeMismatchException("TYPE MISMATCH");
								} catch (TypeMismatchException e) {
									e.printStackTrace();
									System.exit(1);
								}
							}
						}
						System.out.println("YESSS");
					} else {																	//DICHIARAZIONI DI VARIABILI NEL BODY
						System.out.println("NO assegnazione nella dichiarazione nel body");
						if(symTableDef.containsKey(id.getName())) {
							try {
								throw new DuplicateDeclarationException("VARIABLE DUPLICATED");
							} catch (DuplicateDeclarationException e) {
								e.printStackTrace();
								System.exit(1);
							}
						} else {
							System.out.println("inserimento di "+id.getName()+" nella tabella");
							symTableDef.put(id.getName(), symProp);									//INSERIMENTO VARIABILE NELLA TAB DEI SIMBOLI DELLA FUNZIONE
						}
					}
				} else if(assignDec) {
					System.out.println("c'� una assegnazione");									//ASSEGNAZIONE NORMALE
					
					SymbolProperties b = (SymbolProperties) s;
					System.out.println("BBBBBB "+b);
					
					/*CONTROLLO CHE LA VARIABILE DI OUTPUT SI TROVI SEMPRE NELLA PARTE SINISTRA DELL'ASSEGNAZIONE*/
					checkUseForVariableOUTPUT(id);
					/*	*/
					
					if(b != null) {	
						SymbolTable st = lookup(id);
						SymbolProperties sp = st.get(id.getName());							//PARTE DESTRA DELL'ASSEGNAZIONE
						System.out.println("SP:: "+sp.getSymbolType());

						Identifier i = new Identifier(b.getSymbolName());
						System.out.println("iiiii_"+i);
						st = lookup(i);
						SymbolProperties sp2 = st.get(i.getName());							//PARTE SINISTRA DELL'ASSEGNAZIONE
						
						System.out.println("SP2:: "+sp2.getSymbolType());
						
						if(sp.getSymbolType().equals(sp2.getSymbolType())) {
							//do nothing
						} else if(sp.getSymbolType().equals("int") && sp2.getSymbolType().equals("double")) {

						//} else if(sp.getSymbolType().equals("double") && sp2.getSymbolType().equals("int")) {
						} else {
							try {
								throw new TypeMismatchException("TYPE MISMATCH");
							} catch (TypeMismatchException e) {
								e.printStackTrace();
								System.exit(1);
							}
						}
					}
				} else {							//E' UNO STATEMENT
					System.out.println("niente");
					lookup(id);																	/*CONTROLLA LE EVENTUALI VARIABLI NELLO STACK*/
					
					if(isRead) {																/*CONTROLLA SE LA OPERAZIONE E' UNA READ*/
						/*CONTROLLO CHE LA VARIABILE DI OUTPUT NON SI TROVI NELLA READ*/
						checkUseForVariableOUTPUT(id);
					} else if(isWrite) {														/*CONTROLLA SE LA OPERAZIONE E' UNA WRITE*/
						/*CONTROLLO CHE LA VARIABILE DI OUTPUT NON SI TROVI NELLA WRITE*/
						checkUseForVariableOUTPUT(id);
					} else if(isIF) {															/*CONTROLLA SE LA OPERAZIONE E' UN IF*/
						/*CONTROLLO CHE LA VARIABILE DI OUTPUT NON SI TROVI NELLA CONDIZIONE DEL IF*/
						checkUseForVariableOUTPUT(id);
					} else if(isWhile) {														/*CONTROLLA SE LA OPERAZIONE E' UN WHILE*/
						/*CONTROLLO CHE LA VARIABILE DI OUTPUT NON SI TROVI NELLA CONDIZIONE DEL WHILE*/
						checkUseForVariableOUTPUT(id);
					}
				}
		} else if(varDec || assignDec) {					/*(MAIN) SE LE DICHIARAZIONI DI VARIABLI SONO FINITE OPPURE E' STATA FATTA UNA ASSEGNAZIONE IN UNA DICHIARAZIONE*/
			System.out.println("(IDENTIFIER) (MAIN) VAR DEC o ASSIGN DEC"+id.getName());
			
			if(!symTable.containsKey(id.getName())) {
				try {
					throw new VariableNotDeclaredException("VARIABLE NOT DECLARED");
				} catch (VariableNotDeclaredException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
			/* TYPE CHECKING PER L'ASSEGNAZIONE NELLA DICHIARAZIONE O NEL PROGRAMMA MAIN */
			SymbolProperties b = new SymbolProperties();
			b = (SymbolProperties) s;
			System.out.println("SYMBOL RECEIVED: "+b);
			if(b != null) {
				System.out.println("sim1 "+"ID: "+symTable.get(id.getName()).getSymbolType()+" "+id.getName());					/*PARTE DESTRA DELL'ASSEGNAZIONE*/
				System.out.println("sim2 "+"ID: "+symTable.get(b.getSymbolName()).getSymbolType()+" "+b.getSymbolName());		/*PARTE SINISTRA DELL'ASSEGNAZIONE*/
				if (symTable.get(id.getName()).getSymbolType().equals(symTable.get(b.getSymbolName()).getSymbolType())) {
				} else if(symTable.get(id.getName()).getSymbolType().equals("int") && symTable.get(b.getSymbolName()).getSymbolType().equals("double")) {
				
				} else {
					try {
						throw new TypeMismatchException("TYPE MISMATCH");
					} catch (TypeMismatchException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			}
			/*  */
		} else {										//E' UNO STATEMENT
			if(symTable.containsKey(id.getName())) {
				try {
					throw new DuplicateDeclarationException("VARIABLE DUPLICATED");
				} catch (DuplicateDeclarationException e) {
					e.printStackTrace();
					System.exit(1);
				}
			} else {
				System.out.println("NOOOOOOO"+id.getName());
				symTable.put(id.getName(), symProp);							//INSERIMENTO DELLA VARIABILE NELLA TABELLA DEI SIMBOLI
			}
		}
		
		return el;
	}

	@Override
	public Element visit(VarInitValue varInitValue) {
		Element el = this.xmlDocument.createElement("VAR_INIT_VALUE");
		el.appendChild((Node) varInitValue.getAssign().accept(this));			//VISITA SU AssignOP
		return el;
	}

	@Override
	public Element visit(ReadOP readOP) {
		System.out.println("READ OP");
		Element el = this.xmlDocument.createElement("READ_OP");
		
		Collections.reverse(readOP.getIdentifierList());
		
		isRead = true;
		
		for(Identifier id: readOP.getIdentifierList()) {
			el.appendChild((Node) id.accept(this, null));
		}
		
		isRead = false;
		
		return el;
	}

	@Override
	public Element visit(BoolOP boolOP, Object s) {
		//System.out.println("BOOL OP " + (symTable.get(b.getSymbolName()).getSymbolType()));
		Element el = this.xmlDocument.createElement(boolOP.getOperation());
		Attr a = this.xmlDocument.createAttribute("type");
		a.setValue(String.valueOf("bool"));
		el.setAttributeNode(a);
		
		OpExpression = true;
		
		el.appendChild((Node) boolOP.getExpr1().accept(this, null));
		el.appendChild((Node) boolOP.getExpr2().accept(this, null));
		System.out.println("TYPE 1: "+el.getFirstChild().getAttributes().item(0)+" TYPE 2:"+el.getLastChild().getAttributes().item(0));
		
		if((boolOP.getExpr1() instanceof Identifier) && (boolOP.getExpr2() instanceof Identifier)){
			Identifier i1 = (Identifier) boolOP.getExpr1();
			Identifier i2 = (Identifier) boolOP.getExpr2();
			
			SymbolTable st1 = lookup(i1);
			SymbolTable st2 = lookup(i2);
			
			if(st1.get(i1.getName()).getSymbolType().equals("bool") && st2.get(i2.getName()).getSymbolType().equals("bool")) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else if((boolOP.getExpr1() instanceof Identifier) && ((boolOP.getExpr2() instanceof TrueOP) || boolOP.getExpr2() instanceof FalseOP)) {
			Identifier i1 = (Identifier) boolOP.getExpr1();
			
			SymbolTable st1 = lookup(i1);
			
			if(st1.get(i1.getName()).getSymbolType().equals("bool")) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else if(((boolOP.getExpr1() instanceof TrueOP) || boolOP.getExpr1() instanceof FalseOP) && (boolOP.getExpr2() instanceof Identifier)) {
			Identifier i2 = (Identifier) boolOP.getExpr2();
			
			SymbolTable st2 = lookup(i2);
			
			if(st2.get(i2.getName()).getSymbolType().equals("bool")) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else if((boolOP.getExpr1() instanceof TrueOP) && (boolOP.getExpr2() instanceof TrueOP)) {
			System.out.println("siii");
			
		} else if((boolOP.getExpr1() instanceof TrueOP) && (boolOP.getExpr2() instanceof FalseOP)) {
			System.out.println("siii 22");
			
		} else if((boolOP.getExpr1() instanceof FalseOP) && (boolOP.getExpr2() instanceof TrueOP)) {
			System.out.println("siii 22");
			
		} else if((boolOP.getExpr1() instanceof FalseOP) && (boolOP.getExpr2() instanceof FalseOP)) {
			System.out.println("siii 33");
			
		} else if(el.getFirstChild().getAttributes().item(0).toString().contains((el.getLastChild().getAttributes().item(0).toString()))) {				/*CONTROLLO DEL TIPO DEI 2 NODI FLIGLI*/
			System.out.println("VA BENEEE");
		} else if((boolOP.getExpr1() instanceof Identifier) && (el.getLastChild().getAttributes().item(0).toString().contains(("bool")))) {			//CASO NODO FIGLIO E' UN BOOL OP
			Identifier i1 = (Identifier) boolOP.getExpr1();
			
			SymbolTable st1 = lookup(i1);
			
			if(st1.get(i1.getName()).getSymbolType().equals("bool")) {

			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else if((el.getFirstChild().getAttributes().item(0).toString().contains(("bool")) && (boolOP.getExpr2() instanceof Identifier))) {
			Identifier i2 = (Identifier) boolOP.getExpr2();
			
			SymbolTable st2 = lookup(i2);
			
			if(st2.get(i2.getName()).getSymbolType().equals("bool")) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else {
			try {
				throw new TypeMismatchException("TYPE MISMATCH");
			} catch (TypeMismatchException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		/*SEMANTICA PER LA CALL_OP*/
		if(callFunction) {
			if(arrParameterDef.get(callFunctionCount).getTypeSymbol().equals("in")) {								//CONTROLLO SE LA VARIABILE E DI TIPO (IN)
				if(arrParameterDef.get(callFunctionCount).getTypeParam().equals("bool")) {
					if(a.getValue().equals("bool")) {
						
					} else {
						try {
							throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
						} catch (TypeMismatchException e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
				} else {
					try {
						throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
					} catch (TypeMismatchException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			} else {
				try {
					throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		OpExpression = false;
		
		/*CASO ASSEGNAZIONE CON BOOL_OP*/
		if(s != null) {																//CONTROLLO SE LA VARIABIALE A SINISTRA DELL'ASSEGNAZIONE E' UN BOOLEANO
			SymbolProperties sp = (SymbolProperties) s;
			Identifier i = new Identifier(sp.getSymbolName());
			SymbolTable st = lookup(i);
			
			if(st.get(i.getName()).getSymbolType().equals("bool")) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
		}
	
		return el;
	}

	@Override
	public Element visit(RelOP relOP, Object s) {
		System.out.println("RELOP");
		Element el = this.xmlDocument.createElement(relOP.getOperation());
		Attr a = this.xmlDocument.createAttribute("type");
		a.setValue(String.valueOf("bool"));
		el.setAttributeNode(a);
		
		OpExpression = true;
				
		el.appendChild((Node) relOP.getExpr1().accept(this, null));
		el.appendChild((Node) relOP.getExpr2().accept(this, null));
		
		System.out.println("\tEXPR1 REL OP "+relOP.getExpr1().getClass()+" EXPR2 "+relOP.getExpr2().getClass());
		
		if((relOP.getExpr1() instanceof ArithOP) && (relOP.getExpr2() instanceof ArithOP)){
			System.out.println("entrambi arithop");
		} else if((relOP.getExpr1() instanceof ArithOP) || relOP.getExpr2() instanceof ArithOP) {				//SE IN RELOP, UNO DEI MEMBRI E' UNA OPERAZIONE ARITMETICA
			System.out.println("uno dei due è arithop");
			
			if(relOP.getExpr1() instanceof Identifier) {
				Identifier i1 = (Identifier) relOP.getExpr1();
				
				SymbolTable st1 = lookup(i1);
				
				if(st1.get(i1.getName()).getSymbolType().equals("int")) {
					
				} else if(st1.get(i1.getName()).getSymbolType().equals("double")) {
					
				} else {
					try {
						throw new TypeMismatchException("TYPE MISMATCH");
					} catch (TypeMismatchException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			} else if(relOP.getExpr2() instanceof Identifier) {
				Identifier i2 = (Identifier) relOP.getExpr2();
				
				SymbolTable st2 = lookup(i2);
				
				if(st2.get(i2.getName()).getSymbolType().equals("int")) {
					
				} else if(st2.get(i2.getName()).getSymbolType().equals("double")) {
						
				} else {
					try {
						throw new TypeMismatchException("TYPE MISMATCH");
					} catch (TypeMismatchException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			} else if((relOP.getExpr1() instanceof UminusOP) || (relOP.getExpr2() instanceof UminusOP)){  		/*SE UNO DEI DUE MEMBRI E' UN UMINUSOP*/
				
			} else if((relOP.getExpr1() instanceof IntConst) || (relOP.getExpr1() instanceof DoubleConst)){
				
			} else if((relOP.getExpr2() instanceof IntConst) || (relOP.getExpr2() instanceof DoubleConst)){ 
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}																								/*SE IN RELOP NON C'E' UNA OP ARITMETICA */
		} else if((relOP.getExpr1() instanceof UminusOP) && (relOP.getExpr2() instanceof UminusOP)){
			
		} else if((relOP.getExpr1() instanceof UminusOP) || (relOP.getExpr2() instanceof UminusOP)) {
			System.out.println("è uminusssssssssssss");
		} else if((relOP.getExpr1() instanceof Identifier) && (relOP.getExpr2() instanceof Identifier)) {
			Identifier i1 = (Identifier) relOP.getExpr1();
			Identifier i2 = (Identifier) relOP.getExpr2();
			
			SymbolTable st1 = lookup(i1);
			SymbolTable st2 = lookup(i2);
			
			if(st1.get(i1.getName()).getSymbolType().equals("int") && st2.get(i2.getName()).getSymbolType().equals("int")) {
				
			} else if(st1.get(i1.getName()).getSymbolType().equals("int") && st2.get(i2.getName()).getSymbolType().equals("double")) {
				 
			} else if(st1.get(i1.getName()).getSymbolType().equals("double") && st2.get(i2.getName()).getSymbolType().equals("int")) {
				 	
			} else if(st1.get(i1.getName()).getSymbolType().equals("double") && st2.get(i2.getName()).getSymbolType().equals("double")) {
					
			} else if(st1.get(i1.getName()).getSymbolType().equals(st2.get(i2.getName()).getSymbolType())) {		//CASO DUE STRINGHE/CHAR etc.
				if(relOP.getOperation().equals("EQOp")) {															//SE L'OPERAZIONE E' UNA UGUAGLIANZA
					//Do nothing
				} else {
					try {
						throw new TypeMismatchException("TYPE MISMATCH");
					} catch (TypeMismatchException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else if((relOP.getExpr1() instanceof Identifier) && ((relOP.getExpr2() instanceof IntConst) || relOP.getExpr2() instanceof DoubleConst)) {
			Identifier i1 = (Identifier) relOP.getExpr1();
			
			SymbolTable st1 = lookup(i1);
			
			if(st1.get(i1.getName()).getSymbolType().equals("int")) {
				
			} else if(st1.get(i1.getName()).getSymbolType().equals("double")) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else if(((relOP.getExpr1() instanceof IntConst) || relOP.getExpr1() instanceof DoubleConst) && (relOP.getExpr2() instanceof Identifier)) {
			Identifier i2 = (Identifier) relOP.getExpr2();
			
			SymbolTable st2 = lookup(i2);

			if(st2.get(i2.getName()).getSymbolType().equals("int")) {
				
			} else if(st2.get(i2.getName()).getSymbolType().equals("double")) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else if((relOP.getExpr1() instanceof IntConst) && (relOP.getExpr2() instanceof IntConst)) {
			System.out.println("siii");
			
		} else if((relOP.getExpr1() instanceof IntConst) && (relOP.getExpr2() instanceof DoubleConst)) {
			System.out.println("siii 22");
			
		} else if((relOP.getExpr1() instanceof DoubleConst) && (relOP.getExpr2() instanceof IntConst)) {
			System.out.println("siii 22");
			
		} else if((relOP.getExpr1() instanceof DoubleConst) && (relOP.getExpr2() instanceof DoubleConst)) {
			System.out.println("siii 33");
			
		} else if((relOP.getExpr1() instanceof Identifier) && ((relOP.getExpr2() instanceof TrueOP) || relOP.getExpr2() instanceof FalseOP)) {
			Identifier i1 = (Identifier) relOP.getExpr1();
			
			SymbolTable st1 = lookup(i1);
			
			if(st1.get(i1.getName()).getSymbolType().equals("bool")) {
				if(relOP.getOperation().equals("EQOp")) {															//SE L'OPERAZIONE E' UNA UGUAGLIANZA
					//Do nothing
				} else {
					try {
						throw new TypeMismatchException("TYPE MISMATCH");
					} catch (TypeMismatchException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else if((relOP.getExpr1() instanceof Identifier) && (relOP.getExpr2() instanceof StringConst)){		//Stringa e StringConst
			Identifier i1 = (Identifier) relOP.getExpr1();
			
			SymbolTable st1 = lookup(i1);
			
			if(st1.get(i1.getName()).getSymbolType().equals("string")) {
				if(relOP.getOperation().equals("EQOp")) {															//SE L'OPERAZIONE E' UNA UGUAGLIANZA
					//Do nothing
				} else {
					try {
						throw new TypeMismatchException("TYPE MISMATCH");
					} catch (TypeMismatchException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else if((relOP.getExpr1() instanceof Identifier) && (relOP.getExpr2() instanceof CharConst)){		//Char e CharConst
			Identifier i1 = (Identifier) relOP.getExpr1();
			
			SymbolTable st1 = lookup(i1);
			
			if(st1.get(i1.getName()).getSymbolType().equals("char")) {
				if(relOP.getOperation().equals("EQOp")) {															//SE L'OPERAZIONE E' UNA UGUAGLIANZA
					//Do nothing
				} else {
					try {
						throw new TypeMismatchException("TYPE MISMATCH");
					} catch (TypeMismatchException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else if((relOP.getExpr1() instanceof StringConst) && (relOP.getExpr2() instanceof StringConst)){
			if(relOP.getOperation().equals("EQOp")) {															//SE L'OPERAZIONE E' UNA UGUAGLIANZA
				//Do nothing
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else if((relOP.getExpr1() instanceof CharConst) && (relOP.getExpr2() instanceof CharConst)){
			if(relOP.getOperation().equals("EQOp")) {															//SE L'OPERAZIONE E' UNA UGUAGLIANZA
				//Do nothing
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else {
			try {
				throw new TypeMismatchException("TYPE MISMATCH");
			} catch (TypeMismatchException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		/*SEMANTICA PER CALL_OP*/
		if(callFunction) {
			if(arrParameterDef.get(callFunctionCount).getTypeSymbol().equals("in")) {								//CONTROLLO SE LA VARIABILE E DI TIPO (IN)
				if(arrParameterDef.get(callFunctionCount).getTypeParam().equals("bool")) {
					if(a.getValue().equals("bool")) {
						
					} else {
						try {
							throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
						} catch (TypeMismatchException e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
				} else {
					try {
						throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
					} catch (TypeMismatchException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			} else {
				try {
					throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		OpExpression = false;
		
		/*CASO ASSEGNAZIONE CON REL_OP*/
		if(s != null) {														//CONTROLLO SE LA VARIABIALE A SINISTRA DELL'ASSEGNAZIONE E' UN BOOLEANO
			SymbolProperties sp = (SymbolProperties) s;
			Identifier i = new Identifier(sp.getSymbolName());
			SymbolTable st = lookup(i);
			
			if(st.get(i.getName()).getSymbolType().equals("bool")) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
		}
		
		return el;
	}

	@Override
	public Element visit(AssignOP assignOP) {
		System.out.println("ASSIGN _OP");
		Element el = this.xmlDocument.createElement("ASSIGN_OP");
		SymbolProperties s = new SymbolProperties();
		if(assignOP.getIdentifier() != null) {					//ASSEGNAZIONE IN UNO STATEMENT
			assignID = true;
			s.setSymbolName((assignOP.getIdentifier().getName()));
			el.appendChild((Node) assignOP.getIdentifier().accept(this, s));
		}
		assignDec = true;												//ASSEGNAZIONE IN UNA DICHIARAZIONE
		System.out.println("\tASSIGNOP_DECL "+s+ " assignID = "+assignID);
		if(!assignID) {													/*SE L'ASSEGNAZIONE E' STATA FATTA NELLA DICHIARAZIONE*/
			System.out.println("assign in dichiarazione");
			s.setSymbolName(sym2Pass.getSymbolName());				//OGGETTO sym2Pass PRESO DA VAR DECLARATION INIT, ASSEGNAZIONE FATTA PRECEDENTEMENTE
			el.appendChild((Node) assignOP.getExpr().accept(this,s));
		} else {														/*ALTRIMENTI E' UNO STATEMENT*/
			System.out.println("\tSTATTTTTTT");
			el.appendChild((Node) assignOP.getExpr().accept(this,s));
		}
		assignDec = false;
		assignID = false;
		
		return el;
	}

	/*VISITA PER DEF_DECL*/
	@Override
	public Element visit(FunctionDeclaration funcDef) {
		symTableDef = new SymbolTable();		/*CREAZIONE SCOPE PER DEF_DECL*/
		symProp = new SymbolProperties();
		stackOfTable.push(symTableDef);			/*INSERIMENTO TABELLA DEI SIMBOLI PER LA FUNZIONE NELLO STACK*/

		Element el = this.xmlDocument.createElement("DEF_DECL");
		symProp.setSymbolKind("function");
		System.out.println("SIMM PROP :"+symProp);
		
		if(funcDef.getParameterDec() != null) {							//SE LA FUNZIONE HA PARAMETRI
			Collections.reverse(funcDef.getParameterDec());
			ArrayList<ParDef> arrPd = new ArrayList<ParDef>();			//LISTA DI OGGETTI ParDef, CHE CONTIENE TUTTI LE INFORMAZIONI DELLE VARIABILI DEFINITE NELLA DEFINIZIONE DELLA FUNZIONE
			
			for(ParameterDeclaration par: funcDef.getParameterDec()) {
				ParameterType pt = par.getTypeParFunc();				//TIPO DEL PARAMETRO
				Type t = par.getType();									//TIPO DI DATO DELLA VARIABILE
				Identifier i = par.getIdentifier();						//IDENTIFICATORE
				arrPd.add(new ParDef(i.getName(),pt.getTypeParFunc(), t.getVarType()));
			}
			symProp.setListParameterDef(arrPd);					//NELLO STACK ALL'INTERNO C'E' UN ENTRY CHE CONTIENE IL NOME DELLA FUNZIONE, C'E' LA LISTA DEI SUO PARAMETRI DICHIARATI
			arrParameterDef = symProp.getListParameterDef();
		}
				
		el.appendChild((Node) funcDef.getIdentifier().accept(this, null));
		defDecl = true;

		if(funcDef.getParameterDec() != null) {
			for(ParameterDeclaration par: funcDef.getParameterDec()) {
				el.appendChild((Node) par.accept(this));				//VISITA SU PAR_DECL
			}
		}
		defDecl = false;
		
		bodyFlag = true;
		el.appendChild((Node) funcDef.getBody().accept(this));			//VISITA PER IL BODY
		bodyFlag = false;
		
		listSymTable.add(symTableDef);

		stackOfTable.pop();						/*RIMOZIONE TABELLA DEI SIMBOLI DELLA FUNZIONE DAL TOP DELLO STACK*/
				
		return el;
	}

	@Override
	public Element visit(ParameterDeclaration parDef) {
		symProp = new SymbolProperties();

		Element el = this.xmlDocument.createElement("PAR_DECL");
		el.appendChild((Node) parDef.getTypeParFunc().accept(this));		/*VISITA SU PARAMETER TYPE*/
		el.appendChild((Node) parDef.getType().accept(this));
		el.appendChild((Node) parDef.getIdentifier().accept(this, null));
		symProp.setSymbolKind("var");										//SI SETTA IL TIPO DI SIMBOLO (VAR) NELLA TABELLA DEI SIMBOLI DI DEF
		System.out.println("\t\t\tcall");

		return el;
	}

	@Override
	public Element visit(Body body) {
		Element el = this.xmlDocument.createElement("Body");
		
		Collections.reverse(body.getVarDecls());
		Collections.reverse(body.getStmt());
		
		for(VarDeclaration var: body.getVarDecls()) {
			varDecBody = true;
			el.appendChild((Node) var.accept(this));
		}
		varDecBody = false;
		for(Statement stmt: body.getStmt()) {
			el.appendChild((Node) stmt.accept(this));
		}
		
		return el;
	}

	@Override
	public Element visit(ParameterType parType) {
		Element el = this.xmlDocument.createElement("PAR_TYPE");
		Attr a = this.xmlDocument.createAttribute("type");
		a.setValue(String.valueOf(parType.getTypeParFunc()));
		el.setAttributeNode(a);
		return el;
	}

	@Override
	public Element visit(CallOP callOP) {
		System.out.println("CALL OP");
		Element el = this.xmlDocument.createElement("CALL_OP");
		callFunction = true;
		callFunctionName = true;
		el.appendChild((Node) callOP.getIdentifier().accept(this, null));
		callFunctionName = false;
		
		System.out.println("\tCALL OP EXPR");
		
		if(callOP.getExprList() != null) {					//SE LA CHIAMATA A FUNZIONE HA PARAMETRI
			Collections.reverse(callOP.getExprList());
			
			if(callOP.getExprList().size() != arrParameterDef.size()) {							//CONTROLLO DEL NUMERO DI PARAMETRI DI UNA FUNZIONE
				try {
					throw new MatchArgumentException("INCORRECT NUMBER ARGUMENT");
				} catch (MatchArgumentException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			for(Expression expr: callOP.getExprList()) {
				el.appendChild((Node) expr.accept(this, null));
				callFunctionCount++;
			}
		} else {
			if(!arrParameterDef.isEmpty()) {					//NELLA CALL NON CI SONO PARAMETRI, MA LA FUNZIONE RICHIEDE I PARAMETRI
				try {
					throw new MatchArgumentException("MATCH ARGUMENT FAILED");
				} catch (MatchArgumentException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		callFunction = false;
		callFunctionCount = 0;
		
		System.out.println("\tCALL OP EXPR END");
						
		return el;
	}

	@Override
	public Element visit(IfThenElseOP ifThenElseOP) {
		System.out.println("IF THEN ELSE OP");
		isIF = true;
		
		Element el = this.xmlDocument.createElement("IF_THEN_ELSE_OP");
		el.appendChild((Node) ifThenElseOP.getExpressionList().accept(this, null));
		isIF = false;
		el.appendChild((Node) ifThenElseOP.getCompStmt().accept(this));
		el.appendChild((Node) ifThenElseOP.getCompStmtElse().accept(this));
		
		System.out.println("WHATTTT: "+el.getFirstChild().getAttributes().item(0).toString());
		
		if(el.getFirstChild().getAttributes().item(0).toString().contains(("bool"))) {				/*CONTROLLO DEL TIPO DEL PRIMO NODO FLIGLIO*/
		} else if(ifThenElseOP.getExpressionList() instanceof Identifier) {
			Identifier i1 = (Identifier) ifThenElseOP.getExpressionList();
			
			SymbolTable st1 = lookup(i1);
			
			if(st1.get(i1.getName()).getSymbolType().equals("bool")) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else {
			try {
				throw new TypeMismatchException("TYPE MISMATCH");
			} catch (TypeMismatchException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		return el;
	}

	@Override
	public Element visit(CompStatement compStat) {
		Element el = this.xmlDocument.createElement("COMP_STAT");
		
		Collections.reverse(compStat.getStmts());
		
		for(Statement stmt: compStat.getStmts()) {
			el.appendChild((Node) stmt.accept(this));
		}
		
		return el;
	}

	@Override
	public Element visit(IfThenOP ifThenOP) {
		System.out.println("IF THEN OP");
		isIF = true;
		
		Element el = this.xmlDocument.createElement("IF_THEN_OP");
		el.appendChild((Node) ifThenOP.getExpressionList().accept(this, null));
		isIF = false;
		el.appendChild((Node) ifThenOP.getCompStmt().accept(this));
		
		System.out.println("WHATTTT: "+el.getFirstChild().getAttributes().item(0).toString());
						
		if(el.getFirstChild().getAttributes().item(0).toString().contains(("bool"))) {				/*CONTROLLO DEL TIPO DEL PRIMO NODO FIGLIO*/
			System.out.println("IFFFFF");
		} else if(ifThenOP.getExpressionList() instanceof Identifier) {
			Identifier i1 = (Identifier) ifThenOP.getExpressionList();
			
			SymbolTable st1 = lookup(i1);
			
			if(st1.get(i1.getName()).getSymbolType().equals("bool")) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else {
			try {
				throw new TypeMismatchException("TYPE MISMATCH");
			} catch (TypeMismatchException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		return el;
	}

	@Override
	public Element visit(WhileOP whileOP) {
		System.out.println("WHILE OP");
		isWhile = true;
		
		Element el = this.xmlDocument.createElement("WHILE_OP");
		el.appendChild((Node) whileOP.getExpressionList().accept(this, null));
		isWhile = false;
		el.appendChild((Node) whileOP.getCompStmt().accept(this));
		
		if(el.getFirstChild().getAttributes().item(0).toString().contains(("bool"))) {				/*CONTROLLO DEL TIPO DEL PRIMO NODO FLIGLIO*/
		} else if(whileOP.getExpressionList() instanceof Identifier) {
			Identifier i1 = (Identifier) whileOP.getExpressionList();
			
			SymbolTable st1 = lookup(i1);
			
			if(st1.get(i1.getName()).getSymbolType().equals("bool")) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else {
			try {
				throw new TypeMismatchException("TYPE MISMATCH");
			} catch (TypeMismatchException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		return el;
	}

	@Override
	public Element visit(UminusOP uminusOP, Object s) {
		System.out.println("UMINUS OP --- "+s);
		Element el = this.xmlDocument.createElement("UMINUS_OP");
		el.appendChild((Node) uminusOP.getExpr().accept(this, s));
		Attr a = this.xmlDocument.createAttribute("type");
		
		System.out.println("EXPR1 uminusOP "+uminusOP.getExpr().getClass());
		
		if(uminusOP.getExpr() instanceof ArithOP) {								//SE L'ESPRESSIONE E' UNA OP ARITMETICA
			System.out.println("\t\tWHATT "+el.getFirstChild().toString());
			if(el.getFirstChild().getAttributes().item(0).toString().contains("int")) {		/*CONTROLLO DEL TIPO DEL PRIMO NODO FIGLIO*/
				a.setValue(String.valueOf("int"));
			} else {
				a.setValue(String.valueOf("double"));
			}
			el.setAttributeNode(a);
		} else if(uminusOP.getExpr() instanceof Identifier) {
			System.out.println("YEP");
			Identifier i = (Identifier) uminusOP.getExpr();
			
			SymbolTable st = lookup(i);
			
			if(st.get(i.getName()).getSymbolType().equals("int")) {
				a.setValue(String.valueOf("int"));
				el.setAttributeNode(a);
			} else if(st.get(i.getName()).getSymbolType().equals("double")) {
				a.setValue(String.valueOf("double"));
				el.setAttributeNode(a);
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else if(uminusOP.getExpr() instanceof IntConst) {
			a.setValue(String.valueOf("int"));
			el.setAttributeNode(a);
		} else if(uminusOP.getExpr() instanceof DoubleConst) {
			a.setValue(String.valueOf("double"));
			el.setAttributeNode(a);
		} else {
			try {
				throw new TypeMismatchException("TYPE MISMATCH");
			} catch (TypeMismatchException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		return el;
	}

	@Override
	public Element visit(NotOP notOP, Object s) {
		System.out.println("NOT OP");
		Element el = this.xmlDocument.createElement("NOT_OP");
		Attr a = this.xmlDocument.createAttribute("type");
		a.setValue(String.valueOf("bool"));
		el.setAttributeNode(a);
		
		OpExpression = true;
		
		el.appendChild((Node) notOP.getExpr().accept(this, null));
		
		if(el.getFirstChild().getAttributes().item(0).toString().contains(("bool"))) {				/*CONTROLLO DEL TIPO DEL PRIMO NODO FIGLIO*/
		} else if(notOP.getExpr() instanceof Identifier) {
			Identifier i1 = (Identifier) notOP.getExpr();
			
			SymbolTable st1 = lookup(i1);
			
			if(st1.get(i1.getName()).getSymbolType().equals("bool")) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else {
			try {
				throw new TypeMismatchException("TYPE MISMATCH");
			} catch (TypeMismatchException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		/*SEMANTICA PER CALL_OP*/
		if(callFunction) {
			if(arrParameterDef.get(callFunctionCount).getTypeSymbol().equals("in")) {								//CONTROLLO SE LA VARIABILE E DI TIPO (IN)
				if(arrParameterDef.get(callFunctionCount).getTypeParam().equals("bool")) {
					if(a.getValue().equals("bool")) {
						
					} else {
						try {
							throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
						} catch (TypeMismatchException e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
				} else {
					try {
						throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
					} catch (TypeMismatchException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			} else {
				try {
					throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		OpExpression = false;
		
		/*CASO ASSEGNAZIONE CON NOT_OP*/
		if(s != null) {																//CONTROLLO SE LA VARIABILE A SINISTRA DELL'ASSEGNAZIONE E' UN BOOLEANO
			SymbolProperties sp = (SymbolProperties) s;
			Identifier i = new Identifier(sp.getSymbolName());
			SymbolTable st = lookup(i);
			
			if(st.get(i.getName()).getSymbolType().equals("bool")) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
		}
		
		return el;
	}

	@Override
	public Element visit(TrueOP trueOP, Object s) {
		//System.out.println("TRUE OP");
		Element el = this.xmlDocument.createElement("TRUE_OP");
		Attr a = this.xmlDocument.createAttribute("type");
		a.setValue(String.valueOf("bool"));
		el.setAttributeNode(a);
		
		SymbolProperties b = new SymbolProperties();
		b = (SymbolProperties) s;
		System.out.println("SYMBOL RECEIVED: "+b);
			
		if(b != null) {
			Identifier i = new Identifier(b.getSymbolName());
			
			SymbolTable st = lookup(i);
			
			if((st.get(b.getSymbolName()).getSymbolType().equals("bool"))) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		return el;
	}

	@Override
	public Element visit(FalseOP falseOP, Object s) {
		//System.out.println("FALSE OP");
		Element el = this.xmlDocument.createElement("FALSE_OP");
		Attr a = this.xmlDocument.createAttribute("type");
		a.setValue(String.valueOf("bool"));
		el.setAttributeNode(a);
		
		SymbolProperties b = new SymbolProperties();
		b = (SymbolProperties) s;
		System.out.println("SYMBOL RECEIVED: "+b);
		
		if(b != null) {
			Identifier i = new Identifier(b.getSymbolName());
			
			SymbolTable st = lookup(i);
			
			if((st.get(b.getSymbolName()).getSymbolType().equals("bool"))) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		return el;
	}

	@Override
	public Element visit(DoubleConst doubleConst, Object s) {
		System.out.println("DOUBLE CONST");
		SymbolProperties b = new SymbolProperties();
		b = (SymbolProperties) s;
		Element el = this.xmlDocument.createElement("DOUBLE_CONST");
		Attr a = this.xmlDocument.createAttribute("value");
		a.setValue(String.valueOf(doubleConst.getDoubleValue()));
		el.setAttributeNode(a);
		
		if(b != null) {
			Identifier i = new Identifier(b.getSymbolName());
			
			SymbolTable st = lookup(i);
			
			if((st.get(b.getSymbolName()).getSymbolType().equals("double"))) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		if(callFunction) {
			if(arrParameterDef.get(callFunctionCount).getTypeSymbol().equals("in")) {								//CONTROLLO SE LA VARIABILE E DI TIPO (IN)
				if(OpExpression) {													//SE E' UNA ARITH OP/BOOL_OP/REL_OP/NOT_OP, I CONTROLLI DI TIPO LI DEMANDA ALL'OPERAZIONE
					//Do nothing
				} else {
					if(arrParameterDef.get(callFunctionCount).getTypeParam().equals("double")) {
						
					} else {
						try {
							throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
						} catch (TypeMismatchException e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
				}
			} else {
				try {
					throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		return el;
	}

	@Override
	public Element visit(CharConst charConst, Object s) {
		SymbolProperties b = new SymbolProperties();
		b = (SymbolProperties) s;
		System.out.println("CHAR_CONST"+b);
		Element el = this.xmlDocument.createElement("CHAR_CONST");
		Attr a = this.xmlDocument.createAttribute("value");
		a.setValue(String.valueOf(charConst.getCharValue()));
		el.setAttributeNode(a);
		
		if(b != null) {
			Identifier i = new Identifier(b.getSymbolName());
			
			SymbolTable st = lookup(i);
			
			if((st.get(b.getSymbolName()).getSymbolType().equals("char"))) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		if(callFunction) {
			if(arrParameterDef.get(callFunctionCount).getTypeSymbol().equals("in")) {								//CONTROLLO SE LA VARIABILE E DI TIPO (IN)
				if(arrParameterDef.get(callFunctionCount).getTypeParam().equals("char")) {
					
				} else {
					try {
						throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
					} catch (TypeMismatchException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			} else {
				try {
					throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		return el;
	}

	@Override
	public Element visit(StringConst stringConst, Object s) {
		SymbolProperties b = new SymbolProperties();
		b = (SymbolProperties) s;
		System.out.println("STRING_CONST"+b);
		Element el = this.xmlDocument.createElement("STRING_CONST");
		Attr a = this.xmlDocument.createAttribute("value");
		a.setValue(stringConst.getStringValue());
		el.setAttributeNode(a);
		
		if(b != null) {
			Identifier i = new Identifier(b.getSymbolName());
			
			SymbolTable st = lookup(i);
			
			if((st.get(b.getSymbolName()).getSymbolType().equals("string"))) {
				
			} else {
				try {
					throw new TypeMismatchException("TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		if(callFunction) {
			if(arrParameterDef.get(callFunctionCount).getTypeSymbol().equals("in")) {								//CONTROLLO SE LA VARIABILE E DI TIPO (IN)
				if(arrParameterDef.get(callFunctionCount).getTypeParam().equals("string")) {
					
				} else {
					try {
						throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
					} catch (TypeMismatchException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			} else {
				try {
					throw new TypeMismatchException("ARGUMENT TYPE MISMATCH");
				} catch (TypeMismatchException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		return el;
	}
	
	public SymbolTable lookup(Identifier id){						//METODO CHE CONSENTE DI ANDARE A CERCARE LA VARIABILE ALL'INTERNO DELLO STACK
        int index = stackOfTable.size()-1;       //Size stack = 2
        System.out.println("-----stack size = "+index);

        SymbolTable sTable = new SymbolTable();

        if(!stackOfTable.get(index).containsKey(id.getName())){      //Check scope attuale
            index--;
            if(!stackOfTable.get(index).containsKey(id.getName())){  //Check scope-1
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
	
	public void checkUseForVariableOUTPUT(Identifier id) {									/*METODO PER IL CONTROLLO DELL'UTILIZZO DELLA VARIABILE OUT ALL'INTERNO DI UNA FUNZIONE*/
		for(ParDef pd: arrParameterDef) {
			if(pd.getSymbolName().equals(id.getName()) && pd.getTypeSymbol().equals("out")) {
				System.out.println("ERROR "+id.getName());
				try {
					throw new IncorrectUtilizationOfVariableOutException("Incorrect utilization of variable OUT"+" ("+id.getName()+")");
				} catch (IncorrectUtilizationOfVariableOutException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
	}
}
