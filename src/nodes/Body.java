package nodes;

import java.util.ArrayList;

import visitors.Visitor;

public class Body extends Decl {
	private final ArrayList<VarDeclaration> varDecls;
	private final ArrayList<Statement> stmt;
	
	public Body(ArrayList<VarDeclaration> varDecls, ArrayList<Statement> stmt) {
		this.varDecls = varDecls;
		this.stmt = stmt;
	}

	public ArrayList<VarDeclaration> getVarDecls() {
		return varDecls;
	}

	public ArrayList<Statement> getStmt() {
		return stmt;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor) {
		return syntaxVisitor.visit(this);
	}
}
