/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTConditionalExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTName;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTUnaryExpression;

/**  以行为单位计算C 语言 复杂度，不考虑表达式语句和变量声明内的嵌套语句。
 *  此类作为各类语句的visit，找出该语句内的函数调用、逻辑运算符、局部变量、参数、全局变量等。
 * @author Administrator
 *
 */
public class ProcedureFragmentVisitor extends ASTVisitor {
	List<String> identiferNames; //记录该语句的所有标识符。排除保留字、函数名、类名、方法名。
	List<String> invoMethods; //记录函数名、全局变量名、方法名，将来要从identiferNames中删除它们。
	List<String> qualifyNames; //C语言，此项为空。
	List<String> logicOperators;  //条件操作符，允许重复。&& || !
	List<String> strayOperators;  //混合Infix,前缀表达式操作符，后缀表达式操作符，不允许重复。 ++ -- ~ ^ & | 
	
	public ProcedureFragmentVisitor()
	{
		super(true);//public ASTVisitor(boolean visitNodes)
		
		identiferNames = new ArrayList<>();
		invoMethods = new ArrayList<>();
		logicOperators = new ArrayList<>();
		strayOperators = new ArrayList<>();
		qualifyNames = new ArrayList<>();
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

	
	/**
	 * 用IType getExpressionType() 可判断表达式的类别。
	 *IASTExpression : 接收计算复杂度的各种表达式或表达式符号。
	 *暂时无法识别 类似 class::field的表达式。
	 */
	@Override
	public int visit(IASTExpression  expression) {
		try {
			if( expression instanceof CASTConditionalExpression ) //条件表达式。
				return ProcessConditionalExpression((CASTConditionalExpression)expression);
			else if( expression instanceof CASTBinaryExpression ) //二元表达式。
				return ProcessBinaryOperator((CASTBinaryExpression)expression);
			else if( expression instanceof CASTUnaryExpression ) //一元表达式。
				return ProcessUnaryOperator((CASTUnaryExpression)expression);
			else if( expression instanceof CASTFunctionCallExpression ) //函数调用表达式。
				return ProcessFunctionCallExpression((CASTFunctionCallExpression)expression);
			/*注意：CPPASTFunctionCallExpression的部分内容会当做引用。
			 * 在ProcessFieldReferenceExpression中避免重复计算*/
			else if( expression instanceof CASTFieldReference ) //引用表达式。
				return ProcessFieldReferenceExpression((CASTFieldReference)expression);
			else //其它未知语句
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
	
	/**   复杂度  条件表达式 ? :
	 * @param condExpression  Conditional expression
	 * @return
	 */
	private int ProcessConditionalExpression(CASTConditionalExpression condExpression)
	{
		return PROCESS_CONTINUE; //允许嵌套
	}
	
	/**  二元表达式处理
	 * @param binaryExpression
	 * @return 返回skip，无法visit( IASTName astName ) 。暂时只能continue;
	 */
	private int ProcessBinaryOperator(CASTBinaryExpression binaryExpression)
	{
		int rtnProcess = PROCESS_CONTINUE; //允许嵌套
		int oOperator =binaryExpression.getOperator();
		String strOpor ="B"+String.valueOf(oOperator);
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
	
	/**  一元表达式处理
	 * @param binaryExpression
	 * @return 返回skip，无法visit( IASTName astName ) 。暂时只能continue;
	 */
	private int ProcessUnaryOperator(CASTUnaryExpression unaryExpression)
	{
		int rtnProcess = PROCESS_CONTINUE;//允许嵌套
		int oOperator =unaryExpression.getOperator();
		String strOpor ="U"+String.valueOf(oOperator);
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
	
	/**   复杂度  函数调用，简单区分内部方法调用和外部方法调用，加复杂度。
	 * @param funcExpression   Function Call Expression
	 * @return
	 */
	private int ProcessFunctionCallExpression(CASTFunctionCallExpression funcExpression)
	{
		//metric +=MetricCognitiveComplexity.CppFuncCall; 
		IASTExpression fnameExpress = funcExpression.getFunctionNameExpression();
		EnumInvocationIdentifer(fnameExpress);
		return PROCESS_CONTINUE; //允许嵌套
	}
	
	/**  引用(例如结构、枚举等)的复杂度. 暂时不考虑引用的软件复杂度影响。
	 * This interface represents expressions that access a field reference. 
	 * e.g. a.b => a is the expression, b is the field name. 
	 * @param frefExpression   Field Reference
	 * @return
	 */
	private int ProcessFieldReferenceExpression(CASTFieldReference frefExpression)
	{
		/*
		 * 因为IASTExpression 	astExpress肯定会被当做参数、局部变量或者类的属性计算复杂度。
		 * 所以将它放入invoMethods，不重复计算。
		 */
		IASTName astName = frefExpression.getFieldName();
		//addInvoMethod(astName.toString());
		IASTExpression 	astExpress = frefExpression.getFieldOwner();
		//EnumInvocationIdentifer(astExpress);
		return PROCESS_CONTINUE; //允许嵌套，A.B.C这样的嵌套引用重复计算复杂度。
	}
	
	/**  找出表达式里面的变量名、函数名等，加入到invoMethods
	 * @param fnExpress
	 * @return 表达式里面的标识符个数。
	 */
	private void EnumInvocationIdentifer(IASTExpression iastExpress)
	{
		iastExpress.accept(  new ASTVisitor() { 
			{
		        super.shouldVisitNames = true;         // Set this flag to visit names.
			}
			
			@Override
			public int visit( IASTName astName ) 
			{
				if( astName instanceof CASTName )
				{
					char[] sname = ((CASTName)astName).getSimpleID();
					//System.out.println("            "+new String(sname));
					addInvoMethod(new String(sname));
				}
				else{
				}
				return PROCESS_SKIP; //不再搜索子节点。
			} //end of visit
		}//end of ASTVisitor	
		); //end of accept.
	}
	
	
	//记录该语句的所有标识符。排除保留字、函数名、类名、方法名。
	public List<String> getIdentiferNames() {
		return identiferNames;
	}
	
	//记录函数名、类名、方法名
	public List<String> getInvoMethods() {
		return invoMethods;
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
	 * @param identifer
	 */
	private void addInvoMethod(String invoname)
	{
		if( !isExistOperator(invoMethods,invoname) )
			invoMethods.add(invoname);
	}
	
	/*identiferNames中排除在invoMethods中的符号。
	 * 因为identiferNames是语句的所有标识符，而invoMethods是包含类名、方法名等方法调用的标识符。
	 * 在后续计算复杂度的步骤中，只计算类属性、参数、局部变量的影响；显然，不能再考虑invoMethods内标识符
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
	 * 语句中C名称，包括变量名、类名等。
	 */
	@Override
	public int visit( IASTName astName ) {
		if( astName instanceof CASTName )
		{
			char[] sname = ((CASTName)astName).getSimpleID();
			addIdentiferName(new String(sname));
		}
		else
		{
			//System.out.println("##    "+astName.getLastName().toString());
		}
		return PROCESS_SKIP; //不再搜索子节点。
	}
	

}
