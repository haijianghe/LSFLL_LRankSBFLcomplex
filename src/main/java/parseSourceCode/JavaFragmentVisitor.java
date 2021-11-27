/**
 * 
 */
package parseSourceCode;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**  ����Ϊ��λ���㸴�Ӷȣ������Ǳ��ʽ���ͱ��������ڵ�Ƕ����䡣
 *  ������Ϊ��������visit���ҳ�������ڵĺ������á��߼���������ֲ������������������Եȡ�
 * @author Administrator
 *
 *Direct Known Subclasses:  ���ʽ���ࡣ
 * Annotation, ArrayAccess, ArrayCreation, ArrayInitializer, Assignment, BooleanLiteral, CastExpression, 
 * CharacterLiteral, ClassInstanceCreation, ConditionalExpression, FieldAccess, InfixExpression, 
 * InstanceofExpression, LambdaExpression, MethodInvocation, MethodReference, Name, NullLiteral, 
 * NumberLiteral, ParenthesizedExpression, PostfixExpression, PrefixExpression, StringLiteral, 
 * SuperFieldAccess, SuperMethodInvocation, ThisExpression, TypeLiteral, VariableDeclarationExpression

 */
public class JavaFragmentVisitor extends ASTVisitor{
	List<String> identiferNames; //��¼���������б�ʶ�����ų������֡������������������������������ظ�
	List<String> invoMethods; //��¼��������������������Ҫͳ�����ǵĸ�������Ϊ���Ӷ��������������ظ�
	List<String> qualifyNames; //��¼��������������ǰ���޶����ƣ�����Ҫ��identiferNames��ɾ�����ǡ��������ظ�
	List<String> logicOperators;  //�����������������ظ���&& || !
	List<String> strayOperators;  //�����������ţ����Infix,ǰ׺���ʽ����������׺���ʽ�������������ظ��� ++ -- ~ ^ & | 
	
	public JavaFragmentVisitor()
	{
		identiferNames = new ArrayList<>();
		invoMethods = new ArrayList<>();
		qualifyNames = new ArrayList<>();
		logicOperators = new ArrayList<>();
		strayOperators = new ArrayList<>();
	}
	
	/**���ڲ��ķ������ã�
	 * @param invocation �����ʽ�ڲ�����MethodInvocation
	 * @return
	 */
	@Override
	public boolean visit(MethodInvocation invocation) {
		Expression  express = invocation.getExpression();
		if( express==null )
		{
			String imn = invocation.getName().getIdentifier(); //�ҳ���������
			addInvoMethod(imn);//����Ҫ��identiferNames�ų���ֵ��
		}
		else
		{
			if( express instanceof QualifiedName )
			{
				String strItem = ((QualifiedName)express).getFullyQualifiedName();
				String[] strParsed = strItem.split("\\.");
				for( String parsed : strParsed )
					addQualifyNames(parsed); //�޶�����
				String imn = invocation.getName().getIdentifier(); //�ҳ���������
				addInvoMethod(imn);
			}
			else if( express instanceof SimpleName )
			{
				String strItem = ((SimpleName)express).getIdentifier();
				addQualifyNames(strItem);//����Ҫ��identiferNames�ų���ֵ���޶���.
				String imn = invocation.getName().getIdentifier(); //�ҳ���������
				addInvoMethod(imn);
			}
			else
			{ 
				addInvoMethod(invocation.toString()); //����������֪���Բ��ԡ�
				//TypeLiteral:��ʽ    ( Type | void ) . class
				if( express instanceof TypeLiteral )
					addQualifyNames("class"); //�޶�����
				//һ��һ�����ҳ�express�����ͣ�̫�鷳���򵥵ؿ�����.���֡�
				String strItem = express.toString();
				String[] strParsed = strItem.split("\\.");
				for( String parsed : strParsed )
					addQualifyNames(parsed); //�޶�����
			}
		}
		return true; //��trueʶ��Ƕ�ס�
	}
	
	/**���෽���ĵ��ã�
	 * @param invocation �����ʽ�ڲ�����MethodInvocation
	 * [ ClassName . ] super .
         [ < Type { , Type } > ]
         Identifier ( [ Expression { , Expression } ]
	 * @return
	 */
	@Override
	public boolean visit(SuperMethodInvocation invocation) {
		String imn = invocation.getName().getIdentifier(); //�ҳ���������
		addInvoMethod(imn);//��ʱ����������super��
		//super
		Name refName = 	invocation.getQualifier();
		if( refName!=null )
		{
			String strItem = 	refName.getFullyQualifiedName();
			String[] strParsed = strItem.split("\\.");
			for( String parsed : strParsed )
				addQualifyNames(parsed);
		}
		return true; //��trueʶ��Ƕ�ס�
	}
	
	/**Lambda���ʽ��
	 * �˲������ݣ�ʵ�ʿ��ܸܺ��ӣ��������⡣����Lambda�Ĳ�����Lambda body�ڿ��ܰ������SimpleName
	 * @param lambda �����ʽ�ڲ�����lambda
	 * @return
	 */
	@Override
	public boolean visit(LambdaExpression lambda) {
		//����lambda���ʽ���еĲ������ӵ�qualifyNames�С�
		lambda.accept(  new ASTVisitor() { 
						public boolean visit(VariableDeclarationFragment vds) { 
							String identifer = vds.getName().getIdentifier();
							addQualifyNames(identifer);
							return true;
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.
		/*�˴���bug ,����Ҫ��identiferNames�ų�qualifyNamesʱ�����ܳ������⡣
		 *��Ϊ������lambda�������lambda���ܰ�����ͬ��ʶ����
		 *һ��������ȥ�Ľ�����������ñ�־����������Accept���ֿ�lamada���������ʽ�ĸ��Ӷȼ��㡣
		 */
		
		return true; //��trueʶ��Ƕ�ס�
	}
	
	/**CreationReference��
	 * ��ʽ��     Type ::  [ < Type { , Type } > ]    new
	 * @param reference  �����ͷ������� ClassName::new 
	 * @return
	 */
	@Override
	public boolean visit(CreationReference reference) {
		addInvoMethod(reference.toString()); //����������֪���Բ��ԡ�
		
		//�������õ�type�����ӵ�invoMethods�С�
		reference.accept(  new ASTVisitor() { 
						public boolean visit(SimpleName sn) { 
							String identifer = sn.getIdentifier();
							addQualifyNames(identifer);
							return false;
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.
		//����bug: �����õĴ���TYPE����δ���롣
		return false; //�����⣬��ʶ��Ƕ�ס�
	}
	
	/**ExpressionMethodReference��
	 * ��ʽ��     Expression :: [ < Type { , Type } > ]      Identifier
	 * @param reference  ���þ�̬���� �;�̬����������ȣ�ֻ�ǰ�.��Ϊ::
	 * @return
	 */
	@Override
	public boolean visit(ExpressionMethodReference reference) {
		//�������õ�type�����ӵ�invoMethods�С�
		reference.accept(  new ASTVisitor() { 
						public boolean visit(SimpleName sn) { 
							String identifer = sn.getIdentifier();
							addQualifyNames(identifer);
							return false;//û�м����ӽڵ㡣
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.
		//bug: �����õ�Expressδ���롣
		String identifer = reference.getName().getIdentifier();
		addInvoMethod(identifer);
		return false; //û�м����ӽڵ㡣
	}
	
	
	/**SuperMethodReference��
	 * ��ʽ��     [ ClassName . ] super ::   [ < Type { , Type } > ]       Identifier
	 * @param reference 
	 * @return
	 */
	@Override
	public boolean visit(SuperMethodReference reference) {
		//�������õ�type�����ӵ�invoMethods�С�
		reference.accept(  new ASTVisitor() { 
						public boolean visit(SimpleName sn) { 
							String identifer =sn.getIdentifier();
							addQualifyNames(identifer);
							return false;//û�м����ӽڵ㡣
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.		
		Name refName = 	reference.getQualifier();
		if( refName!=null )
		{
			String strItem = 	refName.getFullyQualifiedName();
			String[] strParsed = strItem.split("\\.");
			for( String parsed : strParsed )
				addQualifyNames(parsed);
		}
		String identifer = reference.getName().getIdentifier();
		addInvoMethod(identifer);
		addQualifyNames("super");
		return false;//û�м����ӽڵ㡣
	}
	
	/**TypeMethodReference��
	 * ��ʽ��  Type ::    [ < Type { , Type } > ]         Identifier
	 * @param reference  
	 * @return
	 */
	@Override
	public boolean visit( TypeMethodReference reference) {
		//�������õ�type�����ӵ�invoMethods�С�
		reference.accept(  new ASTVisitor() { 
						public boolean visit(SimpleName sn) { 
							String identifer = sn.getIdentifier();
							addQualifyNames(identifer);
							return false;//û�м����ӽڵ㡣
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.	
		String identifer = reference.getName().getIdentifier();
		addInvoMethod(identifer);
		return false;//û�м����ӽڵ㡣
	}

	/**  Conditional expression AST node type.  
	 * ��ʽ�� Expression ? Expression : Expression   ��Ԫ���ʽ��
	 */
	@Override
	public boolean visit(ConditionalExpression ceExpress) {
		return true; //ʶ��Ƕ�ס�
	}
		
	/**��¼���������б�ʶ�����ų������֡�����������������������
	 * @param  simple �ҳ���������SimpleName�������������ǵĸ��Ӷȡ�
	 * �ֲ�������
	 * ������
	 * �����Եȣ�
	 * @return
	 */
	@Override
	public boolean visit(SimpleName simple) {
		addIdentiferName(simple.getIdentifier());
		return false; //û���ӽڵ㡣
	}
	
	/**�������ʽ����&& || != �������ͣ�
	 * @param invocation �����ʽ�ڲ�����MethodInvocation
	 * @return
	 */
	@Override
	public boolean visit(InfixExpression infExpress) {
		addInfixOperator(infExpress.getOperator());
		return true; //ʶ��Ƕ�ס�age>0 && df>10 || df<20 ������5����ϵ���ʽ���ţ�����true
	}
	
	//ǰ׺���ʽ ++ --
	@Override
	public boolean visit(PrefixExpression prefExpress) {
		addPrefPostOperator(prefExpress.getOperator());
		return true; //ʶ��Ƕ�ס�
	}

	//��׺���ʽ  ++ -- ! + - % 
	@Override
	public boolean visit(PostfixExpression postExpress) {
		addPrefPostOperator(postExpress.getOperator());
		return true; //ʶ��Ƕ�ס�
	}

	
	//��¼���������б�ʶ�����ų������֡�����������������������
	public List<String> getIdentiferNames() {
		return identiferNames;
	}
	
	//��¼��������������������
	public List<String> getInvoMethods() {
		return invoMethods;
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

	//����Ƿ��µ�������������δ���ֹ�����������С�
	private void addInfixOperator(InfixExpression.Operator ifOpor)
	{
		String strOpor = ifOpor.toString();
		if( ifOpor==InfixExpression.Operator.CONDITIONAL_AND
				|| ifOpor==InfixExpression.Operator.CONDITIONAL_OR )
		{
			if( !isExistOperator(logicOperators,strOpor) )
				logicOperators.add(strOpor);
		}
		else
		{
			if( !isExistOperator(strayOperators,strOpor) )
				strayOperators.add(strOpor);
		}
	}
	
	//����Ƿ��µ�ǰ׺���ʽ��������δ���ֹ�����������С�
	private void addPrefPostOperator(PrefixExpression.Operator ifOpor)
	{
		String strOpor = ifOpor.toString();
		if( ifOpor==PrefixExpression.Operator.NOT )
		{
			if( !isExistOperator(logicOperators,strOpor) )
				logicOperators.add(strOpor);
		}
		else
		{
			if( !isExistOperator(strayOperators,strOpor) )
				strayOperators.add(strOpor);
		}
	}
	
	//����Ƿ��µĺ�׺���ʽ��������δ���ֹ�����������С�
	private void addPrefPostOperator(PostfixExpression.Operator ifOpor)
	{
		String strOpor = ifOpor.toString();
		if( !isExistOperator(strayOperators,strOpor) )
			strayOperators.add(strOpor);
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
	 * @param invoname
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
	
	/*identiferNames���ų���invoMethods�� qualifyNames �еķ��š�
	 * ��ΪidentiferNames���������б�ʶ������invoMethods�� qualifyNames �ǰ����������������ȷ������õı�ʶ����
	 * ͨ��������裬���Լ���Ҵ�������⡣
	 * ��ʱû�õ���
	 */
	private void excludeInvoMethodQualtifyFromIdentiferName()
	{
		Iterator<String> iterator = identiferNames.iterator();
        while (iterator.hasNext()) 
		{
        	String identifer = iterator.next();
        	if( isExistOperator(invoMethods,identifer) )
        		iterator.remove();
        	if( isExistOperator(qualifyNames,identifer) )
        		iterator.remove();
		}//end of while...
	}

	/*identiferNames���ų���invoMethods �еķ��š�
	 * ��ΪidentiferNames���������б�ʶ������invoMethods�Ƿ�������
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
}
