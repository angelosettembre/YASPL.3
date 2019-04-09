package nodes;

import java.util.ArrayList;

import visitors.Visitor;

public class CompStatement extends Statement {
	private final ArrayList<Statement> stmts;

	public CompStatement(ArrayList<Statement> stmts) {
		this.stmts = stmts;
	}

	public ArrayList<Statement> getStmts() {
		return stmts;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor) {
		return syntaxVisitor.visit(this);
	}
}
