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

/** CPPASTCompositeTypeSpecifier ����ΪProblemBinding����ICPPClassType�����⴦��
 * @author Administrator
 *  ��ȡ���ԡ�
 */
public class CppCompositeTypeVisitor extends ASTVisitor {
	private List<String> attributes;//�����б�
	
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
			return PROCESS_SKIP;//�������ֱ�Ӻ��������������ӽڵ㡣
		CPPASTFunctionDeclarator cppDeclar = (CPPASTFunctionDeclarator)funcDeclar; 
		System.out.println("****    "+cppDeclar.getName().toString());
		return PROCESS_SKIP; //��Ѱ�Һ�����Ƕ�׺��������������ӽڵ㡣
	}*/
	
	@Override
	public int visit( IASTDeclarator typeId ) {
		IASTName astName =  typeId.getName();
		String strName = astName.toString();
		if( !strName.isEmpty() )
			attributes.add(strName);
		return PROCESS_SKIP; //C++�Ƿ�������Ƕ���ԣ����������ӽڵ㡣
	}

	//��������б�
	public List<String> getAttributes() {
		return attributes;
	}
	
}
	
