package nodes;

public class VarDeclarationInit {
	private final Identifier identifier;
	private final VarInitValue varValue;
	
	public VarDeclarationInit(Identifier identifier, VarInitValue varValue) {
		this.identifier = identifier;
		this.varValue = varValue;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public VarInitValue getVarValue() {
		return varValue;
	}
}
	
