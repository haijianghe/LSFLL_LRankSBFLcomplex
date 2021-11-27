/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;

/**
 * @author Administrator
 * CPPASTVisitor�Ѿ�����
 */
public class CppMethodVisitor extends ASTVisitor {
	private List<MethodContext> methodList; //ĳ��������з�����ע�⣺����������Ƕ�ס�
	private String parentName; //parentNode�����֣�ֻ��ȡ���ֱ�ӷ���������ȡ�ڲ���ķ�����
	private MethodContext fieldDeclareMethod; //Ϊ�����ֵ������������佨�����ⷽ����
	/*��java,Ϊ�ཨ��һ�����ⷽ��������Ϊ class_FieldDeclaration,����Ϊ6
	 * ���Դ���ֵ�ı��붨��Ϊ��ִ����䡣C++���û�аɡ�haveAssignment��ʱ�ò��ϡ�
	 * haveAssignment���ڴ��������
	 */
	//private boolean haveAssignment = false;//�и�ֵ����� Visitor����ʹ�÷����ھֲ�������
	//private List<String> localVariables; //�����ڵ�ֱ�Ӿֲ��������ϡ����������ģ�Ӧ�����ҵĳ��������⡣

	/**
	 * @param unit  ��ȡ�к�
	 * @param parentNodeName  ����Ƿ�Ϊ�����ֱ�ӷ������������
	 */
	public CppMethodVisitor(String parentNodeName)
	{
		super(true);//public ASTVisitor(boolean visitNodes)
		
		methodList = new ArrayList<>();
		//localVariables = new ArrayList<>();
		//unitCompile = unit;
		parentName = parentNodeName;
		//Ϊ�����ֵ��������佨�����ⷽ����
		fieldDeclareMethod = new MethodContext();
		fieldDeclareMethod.setName("class_FieldDeclaration");
		fieldDeclareMethod.setType(6);
	}
	
	//��������з�����ע�⣺�����ڵ�Ƕ���ࡢ�������inner class�ķ����������������С�
	public List<MethodContext> getMethodList() {
		return methodList;
	}
	
	/*
	 * Ϊ�����ֵ������������佨�������ⷽ���� ���÷������뵽�����б�
	 * ע�⣺ �ֹ����ô˷����������Զ����á�
	 */
	public void addMethodByFieldDeclaration()
	{
		if( fieldDeclareMethod.getNumberOfStatements()>0 )
			methodList.add(fieldDeclareMethod);
	}
	
	@Override
	public int visit(IASTDeclaration funcDefin) {
		if ( !(funcDefin instanceof CPPASTFunctionDefinition)  )
			 return PROCESS_CONTINUE;
		CPPASTFunctionDefinition cppFunction = (CPPASTFunctionDefinition)funcDefin;
		if( !isTopMethod(cppFunction) ) //����ָ�����ֱ�ӷ���
		{
			/*
			 * ����������Ǹ����ֱ�ӷ��������Ƿ����ڵ�Ƕ���ࡢ�������inner class�ķ������˴����ü��룬�ڶ�Ӧ�����Ϊ�������롣
			 * �������ǵ������Ϣ��Initializer��Visitor���ߺ��������ᴦ��ġ�
			 */
			return PROCESS_SKIP; //�Լ�������ֱ�ӷ����������������ӽڵ㡣
		}

		//���¶�ȡ��ֱ�ӷ��������ݡ�
		MethodContext method = new MethodContext();
		IASTFileLocation  fileLocation = cppFunction.getFileLocation();
		//�����ʼ�����к�
		int startLine = fileLocation.getStartingLineNumber();
		int endLine = fileLocation.getEndingLineNumber();
		method.setStartLine(startLine);
		method.setEndLine(endLine);
		
		CPPASTFunctionDeclarator funcDeclar = ( CPPASTFunctionDeclarator)cppFunction.getDeclarator();
		IASTName astName = funcDeclar.getName();
		IBinding binding = astName.resolveBinding();
		if ( !(binding instanceof ICPPMethod ) )
			return PROCESS_SKIP;//ʶ�������ַ��������������ӽڵ㡣
		ICPPMethod cppMethod = (ICPPMethod)binding;
		
		//���캯��
		if (cppMethod.isImplicit())  
			method.setType(1);//���캯������assignment operator, etc.��
		else
			method.setType(3); //��ͨ������
		//������
		String methodName = astName.toString();
		method.setName(methodName);
		//Ϊ�˵��ԣ������������䡣
		//if( methodName.getIdentifier().contentEquals("useForType"))
			//System.out.println("This is for debuggin.");
		//�����б�
		ICPPFunction icppf = (ICPPFunction)binding;
		ICPPParameter[] icppars = icppf.getParameters();
		for (ICPPParameter iccp : icppars) {
			String strPara = iccp.getName().toString();
			method.addParameter(strPara);
		}
		//��ȡ�÷��������оֲ�����
		CppCompositeMethodVisitor ccmVisitor = new CppCompositeMethodVisitor(cppFunction);
		cppFunction.accept(ccmVisitor);
		method.copyLocalVariables(ccmVisitor.getVariables());
		
		//��ȡ�����������������ݡ�
		IASTStatement thisBlock = 	cppFunction.getBody();
		if( thisBlock!=null )
		{
			CppStatementVisitor csVisitor = new CppStatementVisitor();
			thisBlock.accept(csVisitor);
			//��Ҫ�����������ݡ�
			method.fillStatementsToMethod(csVisitor.getStatementList());
		}
		//��������Ľ����ӵ��б�
		methodList.add(method);

		return PROCESS_SKIP; //��Ѱ�Һ�����Ƕ�׺��������������ӽڵ㡣
	}
	
	/**  �÷�����ָ�����ֱ�ӷ���top-level��  ϣ���ų��ڲ���ķ�����
	 * @param nodeMethod
	 * @return
	 */
	private boolean isTopMethod(CPPASTFunctionDefinition nodeMethod)
	{
		IASTNode  parentNode = nodeMethod.getParent();
    	if( parentNode instanceof CPPASTCompositeTypeSpecifier )
    	{
    		IASTName parentSimple = ((CPPASTCompositeTypeSpecifier)parentNode).getName();
    		String parentClassName = parentSimple.toString();
    		if( parentClassName.contentEquals(parentName) )
    			return true;
    		else
    			return false;
    	}
    	else
    		return false;
	}
	
	/** C++û������java Initializer������ ������
	 * ���� C++�ľ�̬��Ա���ⲿ��ʼ�����������û�н�����Ժ���˵��
	 * ע�⣺�����ڵ�Ƕ���ࡢ�������inner class�ķ����������������С�
	 * ���ӿڲ��ܵ��ô�visit����ȡ���ǵ�initializer�б�
	 * 
	 * CDT����ʹ�ã�
	 *               
 	 */
	/*@Override
	public boolean visit(Initializer initializer) {
	}*/
	
	
	/**non-static data member initializers only available with -std=c++11 or -std=gnu++11
	 * C++98������Ϊ��ĳ�Ա��ʼ��ֵ����ʱ��������������java������
	 * Ϊ�ཨ��һ�����ⷽ��������Ϊ class_FieldDeclaration,����Ϊ6
	 * �������������䣬 ���и�ֵ��䣬���ܻᱻ��¼Ϊ��ִ����䣬�������ⷽ����
	 *                     ���ޣ���Ϊ��ִ����䣬���������ⷽ����
	 */
/*	@Override
	public boolean visit(FieldDeclaration fieldStatement) {
	}*/
}
