package nodes;

import java.util.ArrayList;

import visitors.Visitor;

public class VarDeclaration extends Decl {
	private final Type typeVar;
	private final ArrayList<VarDeclarationInit> varList;
	
	public VarDeclaration(Type typeVar, ArrayList<VarDeclarationInit> varList) {
		this.typeVar = typeVar;
		this.varList = varList;
	}

	public Type getTypeVar() {
		return typeVar;
	}

	public ArrayList<VarDeclarationInit> getVarList() {
		return varList;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor) {
		return syntaxVisitor.visit(this);
	}
}
