package visitors;
import nodes.*;

public interface Visitor<T> {
	T visit(Program progOP);
	T visit(WriteOP writeOP);
	T visit(ReadOP readOP);
	T visit(ArithOP arithOP, T param);
	T visit(BoolOP boolOP, T param);
	T visit(RelOP relOP, T param);
	T visit(AssignOP assignOP);
	T visit(IntConst intConst, T param);
	T visit(VarDeclaration varDeclar);
	T visit(VarInitValue varInitValue);
	T visit(Type type);
	T visit(Identifier id, T param);
	T visit(FunctionDeclaration funcDef);
	T visit(ParameterDeclaration parDef);
	T visit(ParameterType parType);
	T visit(Body body);
	T visit(CallOP callOP);
	T visit(IfThenElseOP ifThenElseOP);
	T visit(CompStatement compStat);
	T visit(IfThenOP ifThenOP);
	T visit(WhileOP whileOP);
	T visit(UminusOP uminusOP, T param);
	T visit(NotOP notOP, T param);
	T visit(TrueOP trueOP, T param);
	T visit(FalseOP falseOP, T param);
	T visit(DoubleConst doubleConst, T param);
	T visit(CharConst charConst, T param);
	T visit(StringConst stringConst, T param);
}
