/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * @author Administrator
 *
 */
public class JavaMethodVisitor extends ASTVisitor {
	private List<MethodContext> methodList; //ĳ��������з�����ע�⣺����������Ƕ�ס�
	private CompilationUnit unitCompile; //Ϊ��ȡ�кš�
	private List<String> localVariables; //�����ڵ�ֱ�Ӿֲ��������ϡ����������ģ�Ӧ�����ҵĳ��������⡣
	private String parentName; //parentNode�����֣�ֻ��ȡ���ֱ�ӷ���������ȡ�ڲ���ķ�����
	private MethodContext fieldDeclareMethod; //Ϊ�����ֵ������������佨�����ⷽ����
	private boolean haveAssignment = false;//�и�ֵ����� Visitor����ʹ�÷����ھֲ�������

	
	/**
	 * @param unit  ��ȡ�к�
	 * @param parentNodeName  ����Ƿ�Ϊ�����ֱ�ӷ������������
	 */
	public JavaMethodVisitor(CompilationUnit unit,String parentNodeName)
	{
		methodList = new ArrayList<>();
		localVariables = new ArrayList<>();
		unitCompile = unit;
		parentName = parentNodeName;
		//Ϊ�����ֵ��������佨�����ⷽ����
		fieldDeclareMethod = new MethodContext();
		fieldDeclareMethod.setName("class_FieldDeclaration");
		fieldDeclareMethod.setType(6);
	}
	
	/** ע�⣺�����ڵ�Ƕ���ࡢ�������inner class�ķ����������������С�
	 * ���ӿڲ��ܵ��ô�visit����ȡ���ǵ�MethodNode�б�
	 */
	@Override
	public boolean visit(MethodDeclaration nodeMethod) {
		if( !isTopMethod(nodeMethod) )  //����ָ�����ֱ�ӷ���
		{
			/*
			 * ����������Ǹ����ֱ�ӷ��������Ƿ����ڵ�Ƕ���ࡢ�������inner class�ķ������˴����ü��룬�ڶ�Ӧ�����Ϊ�������롣
			 * �������ǵ������Ϣ��Initializer��Visitor���ߺ��������ᴦ��ġ�
			 */
			return false; //�Լ�������ֱ�ӷ����������������ӽڵ㡣
		}
		//���¶�ȡ��ֱ�ӷ��������ݡ�
		MethodContext method = new MethodContext();
		int startLine = unitCompile.getLineNumber(nodeMethod.getStartPosition());
		int endLine = unitCompile.getLineNumber(nodeMethod.getStartPosition()+nodeMethod.getLength()-1);
		method.setStartLine(startLine);
		method.setEndLine(endLine);
		//���캯��
		if (nodeMethod.isConstructor())  
			method.setType(1);//���캯����
		else
			method.setType(3); //��ͨ������
		//������
		SimpleName methodName = nodeMethod.getName();
		method.setName(methodName.getIdentifier());
		//Ϊ�˵��ԣ������������䡣
		//if( methodName.getIdentifier().contentEquals("useForType"))
		//	System.out.println("This is for debuggin.");
		//�����б�
		List<?> parameters = nodeMethod.parameters();
		for (Object obj : parameters) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) obj;
			method.addParameter(svd.getName().getIdentifier());
		}
		//��ȡ�÷��������оֲ�����
		localVariables.clear();//������ϸ�������
		enumerateLocalVariables(nodeMethod); //�г����оֲ������� 
		method.copyLocalVariables(localVariables);
		//��ȡ�����������������ݡ�
		Block thisBlock = nodeMethod.getBody();
		if( thisBlock!=null )
		{
			JavaStatementVisitor jsVisitor = new JavaStatementVisitor(unitCompile);
			thisBlock.accept(jsVisitor);
			//��Ҫ�����������ݡ�
			method.fillStatementsToMethod(jsVisitor.getStatementList());
		}
		//��������Ľ����ӵ��б�
		methodList.add(method);
		return false; //���Է����ڲ��ķ����Լ������ӽڵ㡣
	}
	
	/**  �÷�����ָ�����ֱ�ӷ���top-level��  ϣ���ų��ڲ���ķ�����
	 * @param nodeMethod
	 * @return
	 */
	private boolean isTopMethod(MethodDeclaration nodeMethod)
	{
		ASTNode  parentNode = nodeMethod.getParent();
    	if( parentNode instanceof TypeDeclaration )
    	{
    		SimpleName parentSimple = ((TypeDeclaration)parentNode).getName();
    		String parentClassName = parentSimple.getIdentifier();
    		if( parentClassName.contentEquals(parentName) )
    			return true;
    		else
    			return false;
    	}
    	else
    		return false;
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
	
	//��������з�����ע�⣺�����ڵ�Ƕ���ࡢ�������inner class�ķ����������������С�
	public List<MethodContext> getMethodList() {
		return methodList;
	}

	/**
	 * ����Method���ܵ��ô�visit����ȡ���ǵľֲ������б�
	 */
	private void enumerateLocalVariables(MethodDeclaration nodeMethod) 
	{
		String methodName = nodeMethod.getName().getIdentifier(); //��ȡ������������visit������Ƕ����������������
		nodeMethod.accept(  new ASTVisitor() { 
/* 
 * VariableDeclarationExpression	����  ForStatement initializers �����ı�����
 * VariableDeclarationStatement  ������ ForStatement initializers �����ı�����		
 */
			public boolean visit(VariableDeclarationStatement nodeStatement) { 
				ASTNode  parentParentNode = nodeStatement.getParent().getParent(); //parent��body
				if( !(parentParentNode instanceof MethodDeclaration) )
					return true; //�丸�ڵ㲻�Ƿ���
				MethodDeclaration parentMethod = (MethodDeclaration)parentParentNode;
				String parentMethodName = parentMethod.getName().getIdentifier(); 
				if( !parentMethodName.contentEquals(methodName) ) 
					return true;//���Ǳ�������ֱ�Ӿֲ�������
				List<?> fragments = nodeStatement.fragments();
				for( Object object: fragments )
				{
					VariableDeclarationFragment vdf = (VariableDeclarationFragment)object;
					localVariables.add(vdf.getName().getIdentifier());//Ӧ�ò��������ɡ�
				}
				return false;//���ټ������ӽڵ㡣
			}//end of visit
		}//end of ASTVisitor
		); //end of accept.
	}
	
	/** ע�⣺�����ڵ�Ƕ���ࡢ�������inner class�ķ����������������С�
	 * ���ӿڲ��ܵ��ô�visit����ȡ���ǵ�initializer�б�
	 */
	@Override
	public boolean visit(Initializer initializer) {
		if( !isChildOfThisClass(initializer) )
			return false;  //����parentName��ֱ���ӽڵ㡣����Ҫ�����ӽڵ㡣
		MethodContext method = new MethodContext();
		int startLine = unitCompile.getLineNumber(initializer.getStartPosition());
		int endLine = unitCompile.getLineNumber(initializer.getStartPosition()+initializer.getLength()-1);
		method.setStartLine(startLine);
		method.setEndLine(endLine);
		method.setName(parentName+"_initializer"); //��һ���������֡�
		method.setType(4); //Initializer, ��ĳ�ʼ���飬�����⴦������Ϊclass_initializer��
		JavaStatementVisitor jsVisitor = new JavaStatementVisitor(unitCompile);
		Block block = initializer.getBody();
		block.accept(jsVisitor);
		//initializerû�в����;ֲ�����
		//��Ҫ�����������ݡ�
		method.fillStatementsToMethod(jsVisitor.getStatementList());
		//��������Ľ����ӵ��б�
		methodList.add(method);

		return false;//���ټ������ӽڵ㡣
	}
	
	/** searchNode ��parentNameָ�����ֱ���ӽڵ��𣿱����ظ��ڲ���Ľڵ㡣
	 * @param searchNode �������Ľڵ�
	 * @return true:��
	 */
	private boolean isChildOfThisClass(ASTNode searchNode)
	{
		boolean result = false;
		ASTNode  parentNode = searchNode.getParent();
		do
		{
			if( parentNode==null )
				break; //����parentNameָ�����ֱ���ӽڵ�
			if( parentNode instanceof TypeDeclaration ) 
			{//����ڵ�
				SimpleName parentSimple = ((TypeDeclaration)parentNode).getName();
				String parentClassName = parentSimple.getIdentifier();
				if( parentClassName.contentEquals(parentName) )
				{ //�ҵ���������ڵ㣬������parentNameָ�����ֱ���ӽڵ�
					result = true;
					break;
				}
				break; //�������ֱ���ӽڵ�
			}
			//������ڵ㣬����������
			parentNode = parentNode.getParent();
		}while( true );
		return result;
	}
	
	/** EnumDeclaration Ҳ���ǿ�ִ����䣬Ҳ���ǣ������⣬�������嵱��һ����ִ����䡣
	 * ��ʽ�� [ Javadoc ] { ExtendedModifier } enum Identifier
         [ implements Type { , Type } ]
         {
         [ EnumConstantDeclaration { , EnumConstantDeclaration } ] [ , ]
         [ ; { ClassBodyDeclaration | ; } ]
         }
	 */
	/*@Override
	public boolean visit(EnumDeclaration enumNode) {
		if( !isChildOfThisClass(enumNode) )
			return true;  //����parentName��ֱ���ӽڵ㡣
		MethodContext method = new MethodContext();
		int startLine = unitCompile.getLineNumber(enumNode.getStartPosition());
		int endLine = unitCompile.getLineNumber(enumNode.getStartPosition()+enumNode.getLength()-1);
		method.setStartLine(startLine);
		method.setEndLine(endLine);
		method.setName(parentName+"_EnumDeclaration"); //��һ���������֡�
		method.setType(6); //Initializer, ��ĳ�ʼ���飬�����⴦������Ϊclass_EnumDeclaration��
		//Ϊ�����⣬��FieldDeclaration����һ�����ⷽ���Ļ����÷���ֻ��һ����䡣
		StatementContext sc = new StatementContext();
		sc.setStartLine(startLine);
		sc.setEndLine(endLine);
		List<?>	enumConsts = enumNode.enumConstants();
		sc.setCognitiveComplexity(enumConsts.size());//ÿ��EnumConstantDeclaration�����Ӷ�+1
		//����һ��������У�������EnumConstantDeclaration����һ����ִ����䡣
		List<StatementContext> scList = new ArrayList<>();
		scList.add(sc);
		method.fillStatementsToMethod(scList);
		//EnumConstantDeclarationû�в����;ֲ�����
		//��������Ľ����ӵ��б�
		methodList.add(method);
		return false;//���ټ������ӽڵ㡣
	}*/
	/**Ϊ�ཨ��һ�����ⷽ��������Ϊ class_FieldDeclaration,����Ϊ6
	 * �������������䣬 ���и�ֵ��䣬���ܻᱻ��¼Ϊ��ִ����䣬�������ⷽ����
	 *                     ���ޣ���Ϊ��ִ����䣬���������ⷽ����
	 * FieldDeclaration:
    [Javadoc] { ExtendedModifier } Type VariableDeclarationFragment
         { , VariableDeclarationFragment } ;
	 */
	@Override
	public boolean visit(FieldDeclaration fieldStatement) {
		if( !isChildOfThisClass(fieldStatement) )
			return false;  //����parentName��ֱ���ӽڵ㡣 Ҳ���ټ������ӽڵ㡣
		//����������������ֵ����𣿲������Ļ�������Ϊ��ִ����䡣
		haveAssignment = false;//ȱʡû�и�ֵ���
		fieldStatement.accept(  new ASTVisitor() { 
			public boolean visit(VariableDeclarationFragment vdFragment) {
				Expression initer = vdFragment.getInitializer();
				if( initer!=null )
					haveAssignment = true;
				return false;//���ټ������ӽڵ㡣
			}//end of visit
		}//end of ASTVisitor
		); //end of accept.
		if( !haveAssignment )
			return false; //���ټ������ӽڵ㡣
		//FieldDeclarationֻ��һ����䣬ֱ�Ӽӵ������С�
		StatementContext statement = new StatementContext();
		statement.setStatementStyle(StatementContext.StatementRest);
		//int startLine = unitCompile.getLineNumber(fieldStatement.getStartPosition());
		//ע�͵����Ǿ䣬�Ὣ[Javadoc]����ʼ�а���������
		int startLine = unitCompile.getLineNumber(fieldStatement.getType().getStartPosition());
		int endLine = unitCompile.getLineNumber(fieldStatement.getStartPosition()+fieldStatement.getLength()-1);
		statement.setStartLine(startLine);
		statement.setEndLine(endLine);
		JavaFragmentVisitor fragmentVisitor= new JavaFragmentVisitor();
		fieldStatement.accept(fragmentVisitor);
		//����ϴ��ʶ�����ٽ��丳ֵ����䡣
		fragmentVisitor.excludeInvoMethodFromIdentiferName();
		statement.setSimpleNames(fragmentVisitor.getIdentiferNames());
		statement.setFuncCalls(fragmentVisitor.getInvoMethods());
		statement.setLogicOperaters(fragmentVisitor.getLogicOperators());
		statement.setOtherOperaters(fragmentVisitor.getStrayOperators());
		//FieldDeclarationû�в����;ֲ�����
		//��������Ľ����ӵ��б�
		fieldDeclareMethod.addOneStatement(statement);
		return false;//���ټ������ӽڵ㡣
	}
}
