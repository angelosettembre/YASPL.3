package nodes;

import java.util.ArrayList;

import org.w3c.dom.Node;

import visitors.Visitor;

public class FunctionDeclaration extends Decl {
	private Identifier identifier;
	private ArrayList<ParameterDeclaration> parameterDec;
	private Body body;
	
	public FunctionDeclaration(Identifier identifier, ArrayList<ParameterDeclaration> parameterDec, Body body) {
		this.identifier = identifier;
		this.parameterDec = parameterDec;
		this.body = body;
	}

	public FunctionDeclaration(Identifier identifier, Body body) {
		this.identifier = identifier;
		this.body = body;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public ArrayList<ParameterDeclaration> getParameterDec() {
		return parameterDec;
	}

	public Body getBody() {
		return body;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor) {
		return syntaxVisitor.visit(this);
	}
}
