package nodes;

import java.util.ArrayList;

import visitors.Visitor;

public class ReadOP extends Statement {
	private final ArrayList<Identifier> identifierList;

	public ReadOP(ArrayList<Identifier> identifierList) {
		this.identifierList = identifierList;
	}

	public ArrayList<Identifier> getIdentifierList() {
		return identifierList;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor) {
		return syntaxVisitor.visit(this);
	}
}
