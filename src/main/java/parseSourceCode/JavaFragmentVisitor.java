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

/**  以行为单位计算复杂度，不考虑表达式语句和变量声明内的嵌套语句。
 *  此类作为各类语句的visit，找出该语句内的函数调用、逻辑运算符、局部变量、参数、类属性等。
 * @author Administrator
 *
 *Direct Known Subclasses:  表达式种类。
 * Annotation, ArrayAccess, ArrayCreation, ArrayInitializer, Assignment, BooleanLiteral, CastExpression, 
 * CharacterLiteral, ClassInstanceCreation, ConditionalExpression, FieldAccess, InfixExpression, 
 * InstanceofExpression, LambdaExpression, MethodInvocation, MethodReference, Name, NullLiteral, 
 * NumberLiteral, ParenthesizedExpression, PostfixExpression, PrefixExpression, StringLiteral, 
 * SuperFieldAccess, SuperMethodInvocation, ThisExpression, TypeLiteral, VariableDeclarationExpression

 */
public class JavaFragmentVisitor extends ASTVisitor{
	List<String> identiferNames; //记录该语句的所有标识符。排除保留字、函数名、类名、方法名。不允许重复
	List<String> invoMethods; //记录函数名、方法名，将来要统计它们的个数，作为复杂度特征。不允许重复
	List<String> qualifyNames; //记录函数名、方法名前的限定名称，将来要从identiferNames中删除它们。不允许重复
	List<String> logicOperators;  //条件操作符，允许重复。&& || !
	List<String> strayOperators;  //其它操作符号：混合Infix,前缀表达式操作符，后缀表达式操作符，允许重复。 ++ -- ~ ^ & | 
	
	public JavaFragmentVisitor()
	{
		identiferNames = new ArrayList<>();
		invoMethods = new ArrayList<>();
		qualifyNames = new ArrayList<>();
		logicOperators = new ArrayList<>();
		strayOperators = new ArrayList<>();
	}
	
	/**类内部的方法调用，
	 * @param invocation 语句表达式内部包含MethodInvocation
	 * @return
	 */
	@Override
	public boolean visit(MethodInvocation invocation) {
		Expression  express = invocation.getExpression();
		if( express==null )
		{
			String imn = invocation.getName().getIdentifier(); //找出方法名。
			addInvoMethod(imn);//将来要从identiferNames排除此值。
		}
		else
		{
			if( express instanceof QualifiedName )
			{
				String strItem = ((QualifiedName)express).getFullyQualifiedName();
				String[] strParsed = strItem.split("\\.");
				for( String parsed : strParsed )
					addQualifyNames(parsed); //限定名。
				String imn = invocation.getName().getIdentifier(); //找出方法名。
				addInvoMethod(imn);
			}
			else if( express instanceof SimpleName )
			{
				String strItem = ((SimpleName)express).getIdentifier();
				addQualifyNames(strItem);//将来要从identiferNames排除此值。限定名.
				String imn = invocation.getName().getIdentifier(); //找出方法名。
				addInvoMethod(imn);
			}
			else
			{ 
				addInvoMethod(invocation.toString()); //这样做，不知道对不对。
				//TypeLiteral:格式    ( Type | void ) . class
				if( express instanceof TypeLiteral )
					addQualifyNames("class"); //限定名。
				//一个一个地找出express的类型，太麻烦，简单地考虑用.划分。
				String strItem = express.toString();
				String[] strParsed = strItem.split("\\.");
				for( String parsed : strParsed )
					addQualifyNames(parsed); //限定名。
			}
		}
		return true; //用true识别嵌套。
	}
	
	/**父类方法的调用，
	 * @param invocation 语句表达式内部包含MethodInvocation
	 * [ ClassName . ] super .
         [ < Type { , Type } > ]
         Identifier ( [ Expression { , Expression } ]
	 * @return
	 */
	@Override
	public boolean visit(SuperMethodInvocation invocation) {
		String imn = invocation.getName().getIdentifier(); //找出方法名。
		addInvoMethod(imn);//此时，类名称是super，
		//super
		Name refName = 	invocation.getQualifier();
		if( refName!=null )
		{
			String strItem = 	refName.getFullyQualifiedName();
			String[] strParsed = strItem.split("\\.");
			for( String parsed : strParsed )
				addQualifyNames(parsed);
		}
		return true; //用true识别嵌套。
	}
	
	/**Lambda表达式，
	 * 此部分内容，实际可能很复杂，简化了问题。比如Lambda的参数，Lambda body内可能包含许多SimpleName
	 * @param lambda 语句表达式内部包含lambda
	 * @return
	 */
	@Override
	public boolean visit(LambdaExpression lambda) {
		//将该lambda表达式所有的参数都加到qualifyNames中。
		lambda.accept(  new ASTVisitor() { 
						public boolean visit(VariableDeclarationFragment vds) { 
							String identifer = vds.getName().getIdentifier();
							addQualifyNames(identifer);
							return true;
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.
		/*此处有bug ,将来要从identiferNames排除qualifyNames时，可能出现问题。
		 *因为包含该lambda的语句与lambda可能包含相同标识符。
		 *一个还过得去的解决方案，设置标志，调用两次Accept，分开lamada与其它表达式的复杂度计算。
		 */
		
		return true; //用true识别嵌套。
	}
	
	/**CreationReference，
	 * 格式：     Type ::  [ < Type { , Type } > ]    new
	 * @param reference  创建型方法引用 ClassName::new 
	 * @return
	 */
	@Override
	public boolean visit(CreationReference reference) {
		addInvoMethod(reference.toString()); //这样做，不知道对不对。
		
		//将该引用的type名都加到invoMethods中。
		reference.accept(  new ASTVisitor() { 
						public boolean visit(SimpleName sn) { 
							String identifer = sn.getIdentifier();
							addQualifyNames(identifer);
							return false;
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.
		//可能bug: 该引用的创建TYPE可能未加入。
		return false; //简化问题，不识别嵌套。
	}
	
	/**ExpressionMethodReference，
	 * 格式：     Expression :: [ < Type { , Type } > ]      Identifier
	 * @param reference  引用静态方法 和静态方法调用相比，只是把.换为::
	 * @return
	 */
	@Override
	public boolean visit(ExpressionMethodReference reference) {
		//将该引用的type名都加到invoMethods中。
		reference.accept(  new ASTVisitor() { 
						public boolean visit(SimpleName sn) { 
							String identifer = sn.getIdentifier();
							addQualifyNames(identifer);
							return false;//没有检索子节点。
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.
		//bug: 该引用的Express未加入。
		String identifer = reference.getName().getIdentifier();
		addInvoMethod(identifer);
		return false; //没有检索子节点。
	}
	
	
	/**SuperMethodReference，
	 * 格式：     [ ClassName . ] super ::   [ < Type { , Type } > ]       Identifier
	 * @param reference 
	 * @return
	 */
	@Override
	public boolean visit(SuperMethodReference reference) {
		//将该引用的type名都加到invoMethods中。
		reference.accept(  new ASTVisitor() { 
						public boolean visit(SimpleName sn) { 
							String identifer =sn.getIdentifier();
							addQualifyNames(identifer);
							return false;//没有检索子节点。
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
		return false;//没有检索子节点。
	}
	
	/**TypeMethodReference，
	 * 格式：  Type ::    [ < Type { , Type } > ]         Identifier
	 * @param reference  
	 * @return
	 */
	@Override
	public boolean visit( TypeMethodReference reference) {
		//将该引用的type名都加到invoMethods中。
		reference.accept(  new ASTVisitor() { 
						public boolean visit(SimpleName sn) { 
							String identifer = sn.getIdentifier();
							addQualifyNames(identifer);
							return false;//没有检索子节点。
						}//end of visit
					}//end of ASTVisitor
					); //end of accept.	
		String identifer = reference.getName().getIdentifier();
		addInvoMethod(identifer);
		return false;//没有检索子节点。
	}

	/**  Conditional expression AST node type.  
	 * 格式： Expression ? Expression : Expression   三元表达式，
	 */
	@Override
	public boolean visit(ConditionalExpression ceExpress) {
		return true; //识别嵌套。
	}
		
	/**记录该语句的所有标识符。排除保留字、函数名、类名、方法名。
	 * @param  simple 找出语句的所有SimpleName，将来计算他们的复杂度。
	 * 局部变量，
	 * 参数，
	 * 类属性等，
	 * @return
	 */
	@Override
	public boolean visit(SimpleName simple) {
		addIdentiferName(simple.getIdentifier());
		return false; //没有子节点。
	}
	
	/**条件表达式中有&& || != 三种类型，
	 * @param invocation 语句表达式内部包含MethodInvocation
	 * @return
	 */
	@Override
	public boolean visit(InfixExpression infExpress) {
		addInfixOperator(infExpress.getOperator());
		return true; //识别嵌套。age>0 && df>10 || df<20 里面有5个关系表达式符号，必须true
	}
	
	//前缀表达式 ++ --
	@Override
	public boolean visit(PrefixExpression prefExpress) {
		addPrefPostOperator(prefExpress.getOperator());
		return true; //识别嵌套。
	}

	//后缀表达式  ++ -- ! + - % 
	@Override
	public boolean visit(PostfixExpression postExpress) {
		addPrefPostOperator(postExpress.getOperator());
		return true; //识别嵌套。
	}

	
	//记录该语句的所有标识符。排除保留字、函数名、类名、方法名。
	public List<String> getIdentiferNames() {
		return identiferNames;
	}
	
	//记录函数名、类名、方法名
	public List<String> getInvoMethods() {
		return invoMethods;
	}

	//记录函数名、方法名前的限定名称，将来要从identiferNames中删除它们。不允许重复
	public List<String> getQualifyNames() {
		return qualifyNames;
	}
	
	//条件操作符，允许重复。&& || !
	public List<String> getLogicOperators() {
		return logicOperators;
	}

	//其它操作符号：混合Infix,前缀表达式操作符，后缀表达式操作符，允许重复。 ++ -- ~ ^ & | 
	public List<String> getStrayOperators() {
		return strayOperators;
	}

	//检查是否新的条件操作符，未出现过的添加入其中。
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
	
	//检查是否新的前缀表达式操作符，未出现过的添加入其中。
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
	
	//检查是否新的后缀表达式操作符，未出现过的添加入其中。
	private void addPrefPostOperator(PostfixExpression.Operator ifOpor)
	{
		String strOpor = ifOpor.toString();
		if( !isExistOperator(strayOperators,strOpor) )
			strayOperators.add(strOpor);
	}
	
	//opList中存在opor吗？true=存在；false:不存在。
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

	/** identifer 加到identiferNames中，将来用于计算复杂度。
	 * identiferNames 记录该语句的所有标识符。排除保留字、函数名、类名、方法名。
	 * @param identifer
	 */
	private void addIdentiferName(String identifer)
	{
		if( !isExistOperator(identiferNames,identifer) )
			identiferNames.add(identifer);
	}

	/** invoname 加到invoMethods中，将来用于计算复杂度。
	 * invoMethods记录函数名、类名、方法名，将来要从identiferNames中删除它们。
	 * @param invoname
	 */
	private void addInvoMethod(String invoname)
	{
		if( !isExistOperator(invoMethods,invoname) )
			invoMethods.add(invoname);
	}
	
	/** 记录函数名、方法名前的限定名称，将来要从identiferNames中删除它们。不允许重复
	 * @param qualification
	 */
	private void addQualifyNames(String qualification)
	{
		if( !isExistOperator(qualifyNames,qualification) )
			qualifyNames.add(qualification);
	}
	
	/*identiferNames中排除在invoMethods和 qualifyNames 中的符号。
	 * 因为identiferNames是语句的所有标识符，而invoMethods和 qualifyNames 是包含类名、方法名等方法调用的标识符。
	 * 通过这个步骤，可以检查我代码的问题。
	 * 暂时没用到！
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

	/*identiferNames中排除在invoMethods 中的符号。
	 * 因为identiferNames是语句的所有标识符，而invoMethods是方法名。
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
