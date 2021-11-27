/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;

/**
 * @author Administrator
 *
 */
public class CppCompositeMethodVisitor extends ASTVisitor {
	private List<String> variables;//局部变量列表。
	private CPPASTFunctionDefinition assignParentMethod;//指定方法，避免visit方法内嵌方法的声明变量，fo、while等语句内嵌的声明变量。
	
	public CppCompositeMethodVisitor(CPPASTFunctionDefinition funcDefin)
	{
		super(true);//public ASTVisitor(boolean visitNodes)
		variables = new ArrayList<>();
		assignParentMethod = funcDefin;
	}
	
	@Override
	public int visit( IASTStatement statement ) {
		if( !(statement instanceof CPPASTDeclarationStatement) )
			return PROCESS_CONTINUE;
		if( !isDirectDeclarationStatementOfMethod(statement) )
			return PROCESS_SKIP; //不寻找语句内嵌语句，不再搜索子节点。
		IASTDeclaration astDeclar = ((CPPASTDeclarationStatement)statement).getDeclaration();
		if( !(astDeclar instanceof CPPASTSimpleDeclaration) )
			return PROCESS_SKIP; //不寻找语句内嵌语句，不再搜索子节点。
		IASTDeclarator[] astDeclators = ((CPPASTSimpleDeclaration)astDeclar).getDeclarators();
		for( IASTDeclarator declator : astDeclators )
		{
			variables.add(declator.getName().toString());
		}
		return PROCESS_SKIP; //不寻找语句内嵌语句，不再搜索子节点。
	}
	
	/**
	 * @param statement 是指定方法的直接声明语句吗？
	 * @return true: 是
	 */
	private boolean isDirectDeclarationStatementOfMethod(IASTStatement statement)
	{
		IASTNode parentNode = statement.getParent();
		//考虑宏定义的问题。
		if( !(parentNode instanceof CPPASTCompoundStatement) )
		{//其parent不是方法body的直接语句。
		/*	if( !(parentNode instanceof IASTPreprocessorStatement) ) 
				return false; 
			else
			{//其parent是宏定义里的语句。
				 parentNode = parentNode.getParent(); //宏定义的parentNode应该是CPPASTCompoundStatement
				 if( !(parentNode instanceof CPPASTCompoundStatement) ) 
					 return false;  //不支持嵌套宏定义。
			}*/
			return false;
		}
		IASTNode parentParentNode = parentNode.getParent(); 
		if( !(parentParentNode instanceof CPPASTFunctionDefinition) )
			return false;//其parent的parent不是方法。
		CPPASTFunctionDefinition cfdOf = (CPPASTFunctionDefinition)parentParentNode;
		if( assignParentMethod==cfdOf ) //这种对象能用==比较吗？存疑！
			return true;
		else //不是指定方法的的直接语句。
			return false;
	}
	
	//类的属性列表。
	public List<String> getVariables() {
		return variables;
	}
}
