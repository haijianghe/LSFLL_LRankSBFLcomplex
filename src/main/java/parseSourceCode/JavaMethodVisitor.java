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
	private List<MethodContext> methodList; //某个类的所有方法。注意：方法不允许嵌套。
	private CompilationUnit unitCompile; //为获取行号。
	private List<String> localVariables; //方法内的直接局部变量集合。若有重名的，应该是我的程序有问题。
	private String parentName; //parentNode的名字，只获取类的直接方法，不获取内部类的方法。
	private MethodContext fieldDeclareMethod; //为类带赋值的属性声明语句建立特殊方法。
	private boolean haveAssignment = false;//有赋值语句吗？ Visitor不能使用方法内局部变量。

	
	/**
	 * @param unit  获取行号
	 * @param parentNodeName  检测是否为本类的直接方法或声明语句
	 */
	public JavaMethodVisitor(CompilationUnit unit,String parentNodeName)
	{
		methodList = new ArrayList<>();
		localVariables = new ArrayList<>();
		unitCompile = unit;
		parentName = parentNodeName;
		//为类带赋值的声明语句建立特殊方法。
		fieldDeclareMethod = new MethodContext();
		fieldDeclareMethod.setName("class_FieldDeclaration");
		fieldDeclareMethod.setType(6);
	}
	
	/** 注意：方法内的嵌套类、匿名类等inner class的方法将不会列入其中。
	 * 类或接口才能调用此visit，获取他们的MethodNode列表。
	 */
	@Override
	public boolean visit(MethodDeclaration nodeMethod) {
		if( !isTopMethod(nodeMethod) )  //并非指定类的直接方法
		{
			/*
			 * 如果方法不是该类的直接方法，就是方法内的嵌套类、匿名类等inner class的方法；此处不用加入，在对应类会作为方法加入。
			 * 至于他们的语句信息，Initializer等Visitor或者后续工作会处理的。
			 */
			return false; //自己都不是直接方法，无须搜索其子节点。
		}
		//以下读取类直接方法的数据。
		MethodContext method = new MethodContext();
		int startLine = unitCompile.getLineNumber(nodeMethod.getStartPosition());
		int endLine = unitCompile.getLineNumber(nodeMethod.getStartPosition()+nodeMethod.getLength()-1);
		method.setStartLine(startLine);
		method.setEndLine(endLine);
		//构造函数
		if (nodeMethod.isConstructor())  
			method.setType(1);//构造函数。
		else
			method.setType(3); //普通函数。
		//方法名
		SimpleName methodName = nodeMethod.getName();
		method.setName(methodName.getIdentifier());
		//为了调试，加入下面的语句。
		//if( methodName.getIdentifier().contentEquals("useForType"))
		//	System.out.println("This is for debuggin.");
		//参数列表。
		List<?> parameters = nodeMethod.parameters();
		for (Object obj : parameters) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) obj;
			method.addParameter(svd.getName().getIdentifier());
		}
		//获取该方法内所有局部变量
		localVariables.clear();//先清除上个方法的
		enumerateLocalVariables(nodeMethod); //列出所有局部变量。 
		method.copyLocalVariables(localVariables);
		//获取方法内所有语句的数据。
		Block thisBlock = nodeMethod.getBody();
		if( thisBlock!=null )
		{
			JavaStatementVisitor jsVisitor = new JavaStatementVisitor(unitCompile);
			thisBlock.accept(jsVisitor);
			//还要加上语句的数据。
			method.fillStatementsToMethod(jsVisitor.getStatementList());
		}
		//将解析后的结果添加到列表。
		methodList.add(method);
		return false; //忽略方法内部的方法以及其他子节点。
	}
	
	/**  该方法是指定类的直接方法top-level吗？  希望排除内部类的方法。
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
	 * 为类带赋值的属性声明语句建立了特殊方法。 将该方法加入到方法列表。
	 * 注意： 手工调用此方法，不好自动调用。
	 */
	public void addMethodByFieldDeclaration()
	{
		if( fieldDeclareMethod.getNumberOfStatements()>0 )
			methodList.add(fieldDeclareMethod);
	}
	
	//该类的所有方法。注意：方法内的嵌套类、匿名类等inner class的方法将不会列入其中。
	public List<MethodContext> getMethodList() {
		return methodList;
	}

	/**
	 * 方法Method才能调用此visit，获取他们的局部变量列表。
	 */
	private void enumerateLocalVariables(MethodDeclaration nodeMethod) 
	{
		String methodName = nodeMethod.getName().getIdentifier(); //获取方法名，避免visit方法内嵌方法的声明变量。
		nodeMethod.accept(  new ASTVisitor() { 
/* 
 * VariableDeclarationExpression	包含  ForStatement initializers 语句里的变量。
 * VariableDeclarationStatement  不包含 ForStatement initializers 语句里的变量。		
 */
			public boolean visit(VariableDeclarationStatement nodeStatement) { 
				ASTNode  parentParentNode = nodeStatement.getParent().getParent(); //parent是body
				if( !(parentParentNode instanceof MethodDeclaration) )
					return true; //其父节点不是方法
				MethodDeclaration parentMethod = (MethodDeclaration)parentParentNode;
				String parentMethodName = parentMethod.getName().getIdentifier(); 
				if( !parentMethodName.contentEquals(methodName) ) 
					return true;//并非本方法的直接局部变量。
				List<?> fragments = nodeStatement.fragments();
				for( Object object: fragments )
				{
					VariableDeclarationFragment vdf = (VariableDeclarationFragment)object;
					localVariables.add(vdf.getName().getIdentifier());//应该不会重名吧。
				}
				return false;//不再检索其子节点。
			}//end of visit
		}//end of ASTVisitor
		); //end of accept.
	}
	
	/** 注意：方法内的嵌套类、匿名类等inner class的方法将不会列入其中。
	 * 类或接口才能调用此visit，获取他们的initializer列表。
	 */
	@Override
	public boolean visit(Initializer initializer) {
		if( !isChildOfThisClass(initializer) )
			return false;  //不是parentName的直接子节点。更不要搜索子节点。
		MethodContext method = new MethodContext();
		int startLine = unitCompile.getLineNumber(initializer.getStartPosition());
		int endLine = unitCompile.getLineNumber(initializer.getStartPosition()+initializer.getLength()-1);
		method.setStartLine(startLine);
		method.setEndLine(endLine);
		method.setName(parentName+"_initializer"); //给一个特殊名字。
		method.setType(4); //Initializer, 类的初始化块，将特殊处理，其名为class_initializer。
		JavaStatementVisitor jsVisitor = new JavaStatementVisitor(unitCompile);
		Block block = initializer.getBody();
		block.accept(jsVisitor);
		//initializer没有参数和局部变量
		//还要加上语句的数据。
		method.fillStatementsToMethod(jsVisitor.getStatementList());
		//将解析后的结果添加到列表。
		methodList.add(method);

		return false;//不再检索其子节点。
	}
	
	/** searchNode 是parentName指定类的直接子节点吗？避免重复内部类的节点。
	 * @param searchNode 待检索的节点
	 * @return true:是
	 */
	private boolean isChildOfThisClass(ASTNode searchNode)
	{
		boolean result = false;
		ASTNode  parentNode = searchNode.getParent();
		do
		{
			if( parentNode==null )
				break; //不是parentName指定类的直接子节点
			if( parentNode instanceof TypeDeclaration ) 
			{//是类节点
				SimpleName parentSimple = ((TypeDeclaration)parentNode).getName();
				String parentClassName = parentSimple.getIdentifier();
				if( parentClassName.contentEquals(parentName) )
				{ //找到了它的类节点，并且是parentName指定类的直接子节点
					result = true;
					break;
				}
				break; //其它类的直接子节点
			}
			//并非类节点，继续搜索。
			parentNode = parentNode.getParent();
		}while( true );
		return result;
	}
	
	/** EnumDeclaration 也许是可执行语句，也许不是，简化问题，整个定义当做一条可执行语句。
	 * 格式： [ Javadoc ] { ExtendedModifier } enum Identifier
         [ implements Type { , Type } ]
         {
         [ EnumConstantDeclaration { , EnumConstantDeclaration } ] [ , ]
         [ ; { ClassBodyDeclaration | ; } ]
         }
	 */
	/*@Override
	public boolean visit(EnumDeclaration enumNode) {
		if( !isChildOfThisClass(enumNode) )
			return true;  //不是parentName的直接子节点。
		MethodContext method = new MethodContext();
		int startLine = unitCompile.getLineNumber(enumNode.getStartPosition());
		int endLine = unitCompile.getLineNumber(enumNode.getStartPosition()+enumNode.getLength()-1);
		method.setStartLine(startLine);
		method.setEndLine(endLine);
		method.setName(parentName+"_EnumDeclaration"); //给一个特殊名字。
		method.setType(6); //Initializer, 类的初始化块，将特殊处理，其名为class_EnumDeclaration。
		//为简化问题，该FieldDeclaration看做一个虚拟方法的话，该方法只有一条语句。
		StatementContext sc = new StatementContext();
		sc.setStartLine(startLine);
		sc.setEndLine(endLine);
		List<?>	enumConsts = enumNode.enumConstants();
		sc.setCognitiveComplexity(enumConsts.size());//每个EnumConstantDeclaration，复杂度+1
		//构造一个语句序列，将整个EnumConstantDeclaration视作一条可执行语句。
		List<StatementContext> scList = new ArrayList<>();
		scList.add(sc);
		method.fillStatementsToMethod(scList);
		//EnumConstantDeclaration没有参数和局部变量
		//将解析后的结果添加到列表。
		methodList.add(method);
		return false;//不再检索其子节点。
	}*/
	/**为类建立一个特殊方法，命名为 class_FieldDeclaration,类型为6
	 * 类的属性声明语句， 若有赋值语句，可能会被记录为可执行语句，加入特殊方法；
	 *                     若无，则为非执行语句，不加入特殊方法。
	 * FieldDeclaration:
    [Javadoc] { ExtendedModifier } Type VariableDeclarationFragment
         { , VariableDeclarationFragment } ;
	 */
	@Override
	public boolean visit(FieldDeclaration fieldStatement) {
		if( !isChildOfThisClass(fieldStatement) )
			return false;  //不是parentName的直接子节点。 也不再检索其子节点。
		//检查该语句里面包含赋值语句吗？不包含的话，并非为可执行语句。
		haveAssignment = false;//缺省没有赋值语句
		fieldStatement.accept(  new ASTVisitor() { 
			public boolean visit(VariableDeclarationFragment vdFragment) {
				Expression initer = vdFragment.getInitializer();
				if( initer!=null )
					haveAssignment = true;
				return false;//不再检索其子节点。
			}//end of visit
		}//end of ASTVisitor
		); //end of accept.
		if( !haveAssignment )
			return false; //不再检索其子节点。
		//FieldDeclaration只有一条语句，直接加到方法中。
		StatementContext statement = new StatementContext();
		statement.setStatementStyle(StatementContext.StatementRest);
		//int startLine = unitCompile.getLineNumber(fieldStatement.getStartPosition());
		//注释掉的那句，会将[Javadoc]的起始行包含进来。
		int startLine = unitCompile.getLineNumber(fieldStatement.getType().getStartPosition());
		int endLine = unitCompile.getLineNumber(fieldStatement.getStartPosition()+fieldStatement.getLength()-1);
		statement.setStartLine(startLine);
		statement.setEndLine(endLine);
		JavaFragmentVisitor fragmentVisitor= new JavaFragmentVisitor();
		fieldStatement.accept(fragmentVisitor);
		//先清洗标识符，再将其赋值给语句。
		fragmentVisitor.excludeInvoMethodFromIdentiferName();
		statement.setSimpleNames(fragmentVisitor.getIdentiferNames());
		statement.setFuncCalls(fragmentVisitor.getInvoMethods());
		statement.setLogicOperaters(fragmentVisitor.getLogicOperators());
		statement.setOtherOperaters(fragmentVisitor.getStrayOperators());
		//FieldDeclaration没有参数和局部变量
		//将解析后的结果添加到列表。
		fieldDeclareMethod.addOneStatement(statement);
		return false;//不再检索其子节点。
	}
}
