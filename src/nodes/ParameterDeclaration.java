package nodes;

import visitors.Visitor;

public class ParameterDeclaration extends Decl {
	private final ParameterType typeParFunc;
	private final Type type;
	private final Identifier identifier;

	public ParameterDeclaration(ParameterType typeParFunc, Type type, Identifier identifier) {
		this.typeParFunc = typeParFunc;
		this.type = type;
		this.identifier = identifier;
	}
	
	public ParameterType getTypeParFunc() {
		return typeParFunc;
	}
	
	public Type getType() {
		return type;
	}
	
	public Identifier getIdentifier() {
		return identifier;
	}

	@Override
	public <T> T accept(Visitor<T> syntaxVisitor) {
		return syntaxVisitor.visit(this);
	}
}
