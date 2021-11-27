/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBreakStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCaseStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTConditionalExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNewExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTOperatorName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeIdInitializerExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypenameExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;

/**  ����Ϊ��λ���㸴�Ӷȣ������Ǳ��ʽ���ͱ��������ڵ�Ƕ����䡣
 *  ������Ϊ��������visit���ҳ�������ڵĺ������á��߼���������ֲ������������������Եȡ�
 * @author Administrator
 *
 *Direct Known Subclasses:  ���ʽ���ࡣ

 */
public class CppFragmentVisitor extends ASTVisitor {
	List<String> identiferNames; //��¼���������б�ʶ�����ų������֡�����������������������
	List<String> invoMethods; //��¼��������������������������Ҫ��identiferNames��ɾ�����ǡ�
	List<String> qualifyNames; //��¼��������������ǰ���޶����ƣ�����Ҫ��identiferNames��ɾ�����ǡ��������ظ�
	List<String> logicOperators;  //�����������������ظ���&& || !
	List<String> strayOperators;  //�����������ţ����Infix,ǰ׺���ʽ����������׺���ʽ�������������ظ��� ++ -- ~ ^ & | 
	
	public CppFragmentVisitor()
	{
		super(true);//public ASTVisitor(boolean visitNodes)
		
		identiferNames = new ArrayList<>();
		invoMethods = new ArrayList<>();
		logicOperators = new ArrayList<>();
		strayOperators = new ArrayList<>();
		qualifyNames = new ArrayList<>();
	}	

	//��¼��������������ǰ���޶����ƣ�����Ҫ��identiferNames��ɾ�����ǡ��������ظ�
	public List<String> getQualifyNames() {
		return qualifyNames;
	}

	//�����������������ظ���&& || !
	public List<String> getLogicOperators() {
		return logicOperators;
	}

	//�����������ţ����Infix,ǰ׺���ʽ����������׺���ʽ�������������ظ��� ++ -- ~ ^ & | 
	public List<String> getStrayOperators() {
		return strayOperators;
	}


	/**
	 * ��IType getExpressionType() ���жϱ��ʽ�����
	 *IASTExpression : ���ռ��㸴�Ӷȵĸ��ֱ��ʽ����ʽ���š�
	 *��ʱ�޷�ʶ�� ���� class::field�ı��ʽ��
	 */
	@Override
	public int visit(IASTExpression  expression) {
		try {
			if( expression instanceof CPPASTConditionalExpression ) //�������ʽ��
				return ProcessConditionalExpression((CPPASTConditionalExpression)expression);
			else if( expression instanceof CPPASTBinaryExpression ) //��Ԫ���ʽ��
				return ProcessBinaryOperator((CPPASTBinaryExpression)expression);
			else if( expression instanceof CPPASTUnaryExpression ) //һԪ���ʽ��
				return ProcessUnaryOperator((CPPASTUnaryExpression)expression);
			else if( expression instanceof CPPASTFunctionCallExpression ) //�������ñ��ʽ��
				return ProcessFunctionCallExpression((CPPASTFunctionCallExpression)expression);
			/*ע�⣺CPPASTFunctionCallExpression�Ĳ������ݻᵱ�����á�
			 * ��ProcessFieldReferenceExpression�б����ظ�����*/
			else if( expression instanceof CPPASTFieldReference ) //���ñ��ʽ��
				return ProcessFieldReferenceExpression((CPPASTFieldReference)expression);
			else //����δ֪���
				return PROCESS_CONTINUE;
		}//end of try
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Parse of Fragment is error.");
			System.out.println(expression.getRawSignature());
			return PROCESS_ABORT;
		}
	}
	
	/**   ���Ӷ�  �������ʽ ? :
	 * @param condExpression  Conditional expression
	 * @return
	 */
	private int ProcessConditionalExpression(CPPASTConditionalExpression condExpression)
	{
		return PROCESS_CONTINUE; //����Ƕ��
	}
	
	/**  ��Ԫ���ʽ����
	 * @param binaryExpression
	 * @return ����skip���޷�visit( IASTName astName ) ����ʱֻ��continue;
	 */
	private int ProcessBinaryOperator(CPPASTBinaryExpression binaryExpression)
	{
		int rtnProcess = PROCESS_CONTINUE; //����Ƕ��
		int oOperator =binaryExpression.getOperator();
		String strOpor = "B"+String.valueOf(oOperator);
		
		if( oOperator== IASTBinaryExpression.op_logicalAnd || oOperator== IASTBinaryExpression.op_logicalOr )
		{
			if( !isExistOperator(logicOperators,strOpor) )
				logicOperators.add(strOpor);
		}
		else 
		{
			if( !isExistOperator(strayOperators,strOpor) )
				strayOperators.add(strOpor);
		}
		return rtnProcess;
	}
	
	/**  һԪ���ʽ����
	 * @param binaryExpression
	 * @return ����skip���޷�visit( IASTName astName ) ����ʱֻ��continue;
	 */
	private int ProcessUnaryOperator(CPPASTUnaryExpression unaryExpression)
	{
		int rtnProcess = PROCESS_CONTINUE;//����Ƕ��
		int oOperator =unaryExpression.getOperator();
		String strOpor = "U"+String.valueOf(oOperator);

		if( oOperator== IASTUnaryExpression.op_not )
		{
			if( !isExistOperator(logicOperators,strOpor) )
				logicOperators.add(strOpor);
		}
		else 
		{
			if( !isExistOperator(strayOperators,strOpor) )
				strayOperators.add(strOpor);
		}
		return rtnProcess;
	}
	
	/**   ���Ӷ�  �������ã��������ڲ��������ú��ⲿ�������ã��Ӹ��Ӷȡ�
	 * @param funcExpression   Function Call Expression
	 * @return
	 */
	private int ProcessFunctionCallExpression(CPPASTFunctionCallExpression funcExpression)
	{
		//metric +=MetricCognitiveComplexity.CppFuncCall; 
		IASTExpression fnameExpress = funcExpression.getFunctionNameExpression();
		CppIdentifierNameVisitor csnVisitor = new CppIdentifierNameVisitor();
		fnameExpress.accept(csnVisitor);
		List<String> idNames = csnVisitor.getQualifyNames();
		for( String item: idNames )
			addQualifyNames(item); 
		String funcName = csnVisitor.getLastName();
		//�ر�ע�⣺���ڴ��벻������������csnVisitor.getLastName()���ص��ַ�������Ϊ�� or null.
		if( (funcName!=null) && (!funcName.isEmpty()) )
			addInvoMethod(funcName); //���һ����Ϊ�Ƿ��������������ơ�
		//else  //check my parse program.
		//	System.out.println("csnVisitor.getLastName(); is error.");
		return PROCESS_CONTINUE; //����Ƕ��
	}
	
	/** ��ʱ������������ش��븴�Ӷȶ��������λ��Ӱ�졣  
	 * ���õĸ��Ӷȣ�������::
	 * This interface represents expressions that access a field reference. 
	 * e.g. a.b => a is the expression, b is the field name. 
	 * e.g. a()->def => a() is the expression, def is the field name.
	 * @param frefExpression   Field Reference
	 * @return
	 */
	private int ProcessFieldReferenceExpression(CPPASTFieldReference frefExpression)
	{
		ASTNodeProperty property = frefExpression.getPropertyInParent();
		
		/* * ע�⣺CPPASTFunctionCallExpression�Ĳ������ݻᵱ�����á�
		 *  ��ProcessFieldReferenceExpression�б����ظ�����
		 *  frefExpression�ڸ��ڵ������ΪFUNCTION_NAME����϶����Ǻ������ñ��ʽ��һ���֡�
		 *  ���������кܶ���������磺IASTFunctionCallExpression.FUNCTION_ARGUMENT�ȣ��������������á�
		 */
		if( IASTFunctionCallExpression.FUNCTION_NAME!=property )
		{ 
			/*
			 * ��ΪIASTExpression 	astExpress�϶��ᱻ�����������ֲ���������������Լ��㸴�Ӷȡ�
			 * ���Խ�������invoMethods�����ظ����㡣
			 */
			//IASTName astName = frefExpression.getFieldName();
			//IASTExpression 	astExpress = frefExpression.getFieldOwner();

		}
		return PROCESS_CONTINUE; //����Ƕ�ף�A.B.C������Ƕ�������ظ����㸴�Ӷȡ�
	}
	
	
	//��¼���������б�ʶ�����ų������֡�����������������������
	public List<String> getIdentiferNames() {
		return identiferNames;
	}
	
	//��¼��������������������
	public List<String> getInvoMethods() {
		return invoMethods;
	}
	
	//opList�д���opor��true=���ڣ�false:�����ڡ�
	private boolean isExistOperator(List<String> opList,String opor)
	{
		boolean found = false;
		for( String item: opList )
		{
			if( item.contentEquals(opor) )
			{
				found = true;
				break;
			}
		}
		return found;
	}
	
	/** identifer �ӵ�identiferNames�У��������ڼ��㸴�Ӷȡ�
	 * identiferNames ��¼���������б�ʶ�����ų������֡�����������������������
	 * @param identifer
	 */
	private void addIdentiferName(String identifer)
	{
		if( !isExistOperator(identiferNames,identifer) )
			identiferNames.add(identifer);
	}

	/** invoname �ӵ�invoMethods�У��������ڼ��㸴�Ӷȡ�
	 * invoMethods��¼��������������������������Ҫ��identiferNames��ɾ�����ǡ�
	 * @param identifer
	 */
	private void addInvoMethod(String invoname)
	{
		if( !isExistOperator(invoMethods,invoname) )
			invoMethods.add(invoname);
	}
	
	/** ��¼��������������ǰ���޶����ƣ�����Ҫ��identiferNames��ɾ�����ǡ��������ظ�
	 * @param qualification
	 */
	private void addQualifyNames(String qualification)
	{
		if( !isExistOperator(qualifyNames,qualification) )
			qualifyNames.add(qualification);
	}
	
	/*identiferNames���ų���invoMethods�еķ��š�
	 * ��ΪidentiferNames���������б�ʶ������invoMethods�ǰ����������������ȷ������õı�ʶ����
	 * �ں������㸴�ӶȵĲ����У�ֻ���������ԡ��������ֲ�������Ӱ�죻��Ȼ�������ٿ���invoMethods�ڱ�ʶ��
	 */
	public void excludeInvoMethodFromIdentiferName()
	{
		Iterator<String> iterator = identiferNames.iterator();
        while (iterator.hasNext()) 
		{
        	String identifer = iterator.next();
        	if( isExistOperator(invoMethods,identifer) )
        		iterator.remove();
		}//end of while...
	}	
	
	/**
	 * �����C++���ƣ������������������ȡ�
	 */
	@Override
	public int visit( IASTName astName ) {
		if( astName instanceof CPPASTName )
		{
			char[] sname = ((CPPASTName)astName).getSimpleID();
			addIdentiferName(new String(sname));
			//System.out.println("            "+new String(sname));
		}
		else if( astName instanceof CPPASTQualifiedName )
		{
			/*A::B ��ʽ����ľ�̬�����ӽ�ȥ��*/
			char[] fullName = astName.toCharArray();
			String strFull = new String(fullName);
			if( strFull.contains("::") )  //��̬����
				addIdentiferName(strFull);
			else //�Ǿ�̬����
			{
				@SuppressWarnings("deprecation")
				IASTName[] 	subNames = ((CPPASTQualifiedName)astName).getNames();
				//System.out.print("           ");
				for( IASTName icans : subNames )
				{
					if( icans instanceof CPPASTName )
					{
						char[] sname = ((CPPASTName)icans).getSimpleID();
						addIdentiferName(new String(sname));
					}
					//CPPASTTemplateId���͵�Ҳ���ΪCPPASTQualifiedName��һ���֣���CPPASTTemplateId�޷�ת��ΪCPPASTName;
				}
				/*IASTName lastName = ((CPPASTQualifiedName)astName).getLastName();
				char[] sname = ((CPPASTName)lastName).getSimpleID();
				addIdentiferName(new String(sname));*/
			}//end of else
		}//end of else if...

		else
		{
			/*
			 *  CPPASTConversionName, CPPASTImplicitName, CPPASTName, 
			 *  CPPASTNameBase, CPPASTOperatorName, CPPASTQualifiedName, CPPASTTemplateId
			 */
		}
		return PROCESS_SKIP; //���������ӽڵ㡣
	}
	
}
