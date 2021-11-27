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
	private List<String> variables;//�ֲ������б�
	private CPPASTFunctionDefinition assignParentMethod;//ָ������������visit������Ƕ����������������fo��while�������Ƕ������������
	
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
			return PROCESS_SKIP; //��Ѱ�������Ƕ��䣬���������ӽڵ㡣
		IASTDeclaration astDeclar = ((CPPASTDeclarationStatement)statement).getDeclaration();
		if( !(astDeclar instanceof CPPASTSimpleDeclaration) )
			return PROCESS_SKIP; //��Ѱ�������Ƕ��䣬���������ӽڵ㡣
		IASTDeclarator[] astDeclators = ((CPPASTSimpleDeclaration)astDeclar).getDeclarators();
		for( IASTDeclarator declator : astDeclators )
		{
			variables.add(declator.getName().toString());
		}
		return PROCESS_SKIP; //��Ѱ�������Ƕ��䣬���������ӽڵ㡣
	}
	
	/**
	 * @param statement ��ָ��������ֱ�����������
	 * @return true: ��
	 */
	private boolean isDirectDeclarationStatementOfMethod(IASTStatement statement)
	{
		IASTNode parentNode = statement.getParent();
		//���Ǻ궨������⡣
		if( !(parentNode instanceof CPPASTCompoundStatement) )
		{//��parent���Ƿ���body��ֱ����䡣
		/*	if( !(parentNode instanceof IASTPreprocessorStatement) ) 
				return false; 
			else
			{//��parent�Ǻ궨�������䡣
				 parentNode = parentNode.getParent(); //�궨���parentNodeӦ����CPPASTCompoundStatement
				 if( !(parentNode instanceof CPPASTCompoundStatement) ) 
					 return false;  //��֧��Ƕ�׺궨�塣
			}*/
			return false;
		}
		IASTNode parentParentNode = parentNode.getParent(); 
		if( !(parentParentNode instanceof CPPASTFunctionDefinition) )
			return false;//��parent��parent���Ƿ�����
		CPPASTFunctionDefinition cfdOf = (CPPASTFunctionDefinition)parentParentNode;
		if( assignParentMethod==cfdOf ) //���ֶ�������==�Ƚ��𣿴��ɣ�
			return true;
		else //����ָ�������ĵ�ֱ����䡣
			return false;
	}
	
	//��������б�
	public List<String> getVariables() {
		return variables;
	}
}
