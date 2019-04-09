package nodes;

import visitors.*;
import java.util.ArrayList;

public class Program {
	private final ArrayList<Decl> declarations;
	private final ArrayList<Statement> statements;
	
	public Program(ArrayList<Decl> declarations, ArrayList<Statement> statements) {
		this.declarations = declarations;
		this.statements = statements;
	}

	public ArrayList<Decl> getDeclarations() {
		return declarations;
	}

	public ArrayList<Statement> getStatements() {
		return statements;
	}
	
	public <T> T accept(Visitor<T> syntaxVisitor) {
		return syntaxVisitor.visit(this);
	}
}
