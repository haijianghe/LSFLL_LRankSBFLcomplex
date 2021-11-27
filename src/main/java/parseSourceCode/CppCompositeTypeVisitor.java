/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeId;

/** CPPASTCompositeTypeSpecifier 类型为ProblemBinding不是ICPPClassType，另外处理。
 * @author Administrator
 *  获取属性。
 */
public class CppCompositeTypeVisitor extends ASTVisitor {
	private List<String> attributes;//属性列表。
	
	public CppCompositeTypeVisitor()
	{
		super(true);//public ASTVisitor(boolean visitNodes)
		attributes = new ArrayList<>();
	}
	
	/*@Override
	public int visit(IASTDeclaration funcDefin) {
		if ( !(funcDefin instanceof CPPASTFunctionDefinition)  )
			 return PROCESS_CONTINUE;
		CPPASTFunctionDefinition cppFunction = (CPPASTFunctionDefinition)funcDefin;
		IASTFunctionDeclarator funcDeclar = ( IASTFunctionDeclarator)cppFunction.getDeclarator();
		if ( !(funcDeclar instanceof CPPASTFunctionDeclarator) )
			return PROCESS_SKIP;//不是类的直接函数，不再搜索子节点。
		CPPASTFunctionDeclarator cppDeclar = (CPPASTFunctionDeclarator)funcDeclar; 
		System.out.println("****    "+cppDeclar.getName().toString());
		return PROCESS_SKIP; //不寻找函数内嵌套函数，不再搜索子节点。
	}*/
	
	@Override
	public int visit( IASTDeclarator typeId ) {
		IASTName astName =  typeId.getName();
		String strName = astName.toString();
		if( !strName.isEmpty() )
			attributes.add(strName);
		return PROCESS_SKIP; //C++是否允许内嵌属性，不再搜索子节点。
	}

	//类的属性列表。
	public List<String> getAttributes() {
		return attributes;
	}
	
}
	
