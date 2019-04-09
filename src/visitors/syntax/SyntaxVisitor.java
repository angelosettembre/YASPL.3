package visitors.syntax;
import java.io.File;

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

import nodes.*;
import visitors.*;

public class SyntaxVisitor implements Visitor<Element> {
	private Document xmlDocument;

    /**
     * Constructor and creator of XML-document
     */
    public SyntaxVisitor() {
        super();
        this.createDocument();
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
            StreamResult result = new StreamResult(new File("output.xml"));
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

	@Override
	public Element visit(Program progOP) {
		Element el = this.xmlDocument.createElement("ProgramOP");
		
		for(Decl d: progOP.getDeclarations()) {
			el.appendChild(d.accept(this));
		}
		
		for(Statement s: progOP.getStatements()) {
			el.appendChild(s.accept(this));
		}
		return el;
	}

	@Override
	public Element visit(WriteOP writeOP) {
		Element el = this.xmlDocument.createElement("WriteOP");
		
		for(Expression e: writeOP.getExprList()) {
			el.appendChild(e.accept(this, null));
		}
		
		return el;
	}

	@Override
	public Element visit(ArithOP arithOP, Element s) {
		Element el = this.xmlDocument.createElement("ArithOP"+"."+arithOP.getOperation());
		el.appendChild(arithOP.getExpr1().accept(this, null));
		el.appendChild(arithOP.getExpr2().accept(this, null));
		return el;
	}

	@Override
	public Element visit(IntConst intConst, Element s) {
		Element el = this.xmlDocument.createElement("INT_CONST");
		Attr a = this.xmlDocument.createAttribute("value");
		a.setValue(String.valueOf(intConst.getIntValue()));
		el.setAttributeNode(a);
		return el;
	}

	@Override
	public Element visit(VarDeclaration varDeclar) {
		Element el = this.xmlDocument.createElement("VAR_DECL");
		el.appendChild(varDeclar.getTypeVar().accept(this));
		
		for(VarDeclarationInit v: varDeclar.getVarList()) {
			Element e = this.xmlDocument.createElement("VarInitOP");
			e.appendChild(v.getIdentifier().accept(this, null));
			if(v.getVarValue() != null) {								//CONTROLLO SE VarInitValue è vuoto
				e.appendChild(v.getVarValue().accept(this));
			}
			el.appendChild(e);
		}
		
		return el;
	}

	@Override
	public Element visit(Type type) {
		Element el = this.xmlDocument.createElement("TYPE"+"."+type.getVarType());
		return el;
	}

	@Override
	public Element visit(Identifier id, Element s) {
		Element el = this.xmlDocument.createElement("IDENTIFIER"+"."+id.getName());
		return el;
	}

	@Override
	public Element visit(VarInitValue varInitValue) {
		Element el = this.xmlDocument.createElement("VAR_INIT_VALUE");
		el.appendChild(varInitValue.getAssign().accept(this));
		return el;
	}

	@Override
	public Element visit(ReadOP readOP) {
		Element el = this.xmlDocument.createElement("READ_OP");
		
		for(Identifier id: readOP.getIdentifierList()) {
			el.appendChild(id.accept(this, null));
		}
		
		return el;
	}

	@Override
	public Element visit(BoolOP boolOP, Element s) {
		Element el = this.xmlDocument.createElement("BOOL_OP"+"."+boolOP.getOperation());
		el.appendChild(boolOP.getExpr1().accept(this, null));
		el.appendChild(boolOP.getExpr2().accept(this, null));
		return el;
	}

	@Override
	public Element visit(RelOP relOP, Element s) {
		Element el = this.xmlDocument.createElement("REL_OP"+"."+relOP.getOperation());
		el.appendChild(relOP.getExpr1().accept(this, null));
		el.appendChild(relOP.getExpr2().accept(this, null));
		return el;
	}

	@Override
	public Element visit(AssignOP assignOP) {
		Element el = this.xmlDocument.createElement("ASSIGN_OP");
		if(assignOP.getIdentifier() != null) {
			el.appendChild(assignOP.getIdentifier().accept(this, null));
		}
		el.appendChild(assignOP.getExpr().accept(this, null));
		return el;
	}

	@Override
	public Element visit(FunctionDeclaration funcDef) {
		Element el = this.xmlDocument.createElement("DEF_DECL");
		el.appendChild(funcDef.getIdentifier().accept(this, null));
		
		if(funcDef.getParameterDec() != null) {
			for(ParameterDeclaration par: funcDef.getParameterDec()) {
				el.appendChild(par.accept(this));
			}
		}
		el.appendChild(funcDef.getBody().accept(this));
		
		return el;
	}

	@Override
	public Element visit(ParameterDeclaration parDef) {
		Element el = this.xmlDocument.createElement("PAR_DECL");
		el.appendChild(parDef.getTypeParFunc().accept(this));
		el.appendChild(parDef.getType().accept(this));
		el.appendChild(parDef.getIdentifier().accept(this, null));
		return el;
	}

	@Override
	public Element visit(Body body) {
		Element el = this.xmlDocument.createElement("Body");
		
		for(VarDeclaration var: body.getVarDecls()) {
			el.appendChild(var.accept(this));
		}
		for(Statement stmt: body.getStmt()) {
			el.appendChild(stmt.accept(this));
		}
		
		return el;
	}

	@Override
	public Element visit(ParameterType parType) {
		Element el = this.xmlDocument.createElement("PAR_TYPE"+"."+parType.getTypeParFunc());
		return el;
	}

	@Override
	public Element visit(CallOP callOP) {
		Element el = this.xmlDocument.createElement("CALL_OP");
		el.appendChild(callOP.getIdentifier().accept(this, null));
		
		if(callOP.getExprList() != null) {
			for(Expression expr: callOP.getExprList()) {
				el.appendChild(expr.accept(this, null));
			}
		}
		
		return el;
	}

	@Override
	public Element visit(IfThenElseOP ifThenElseOP) {
		Element el = this.xmlDocument.createElement("IF_THEN_ELSE_OP");
		el.appendChild(ifThenElseOP.getExpressionList().accept(this, null));
		el.appendChild(ifThenElseOP.getCompStmt().accept(this));
		el.appendChild(ifThenElseOP.getCompStmtElse().accept(this));
		return el;
	}

	@Override
	public Element visit(CompStatement compStat) {
		Element el = this.xmlDocument.createElement("COMP_STAT");
		
		for(Statement stmt: compStat.getStmts()) {
			el.appendChild(stmt.accept(this));
		}
		
		return el;
	}

	@Override
	public Element visit(IfThenOP ifThenOP) {
		Element el = this.xmlDocument.createElement("IF_THEN_OP");
		el.appendChild(ifThenOP.getExpressionList().accept(this, null));
		el.appendChild(ifThenOP.getCompStmt().accept(this));
		
		return el;
	}

	@Override
	public Element visit(WhileOP whileOP) {
		Element el = this.xmlDocument.createElement("WHILE_OP");
		el.appendChild(whileOP.getExpressionList().accept(this, null));
		el.appendChild(whileOP.getCompStmt().accept(this));
		
		return el;
	}

	@Override
	public Element visit(UminusOP uminusOP, Element s) {
		Element el = this.xmlDocument.createElement("UMINUS_OP");
		el.appendChild(uminusOP.getExpr().accept(this, null));
		
		return el;
	}

	@Override
	public Element visit(NotOP notOP, Element s) {
		Element el = this.xmlDocument.createElement("NOT_OP");
		el.appendChild(notOP.getExpr().accept(this, null));
		
		return el;
	}

	@Override
	public Element visit(TrueOP trueOP, Element s) {
		Element el = this.xmlDocument.createElement("TRUE_OP"+"."+trueOP.getValue());
		
		return el;
	}

	@Override
	public Element visit(FalseOP falseOP, Element s) {
		Element el = this.xmlDocument.createElement("FALSE_OP"+"."+falseOP.getValue());
		
		return el;
	}

	@Override
	public Element visit(DoubleConst doubleConst, Element s) {
		Element el = this.xmlDocument.createElement("DOUBLE_CONST");
		Attr a = this.xmlDocument.createAttribute("value");
		a.setValue(String.valueOf(doubleConst.getDoubleValue()));
		el.setAttributeNode(a);
		
		return el;
	}

	@Override
	public Element visit(CharConst charConst, Element s) {
		Element el = this.xmlDocument.createElement("CHAR_CONST");
		Attr a = this.xmlDocument.createAttribute("value");
		a.setValue(String.valueOf(charConst.getCharValue()));
		el.setAttributeNode(a);
		
		return el;
	}

	@Override
	public Element visit(StringConst stringConst, Element s) {
		Element el = this.xmlDocument.createElement("STRING_CONST");
		Attr a = this.xmlDocument.createAttribute("value");
		a.setValue(stringConst.getStringValue());
		el.setAttributeNode(a);
		
		return el;
	}
}