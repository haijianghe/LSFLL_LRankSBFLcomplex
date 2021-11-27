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

/**  以行为单位计算复杂度，不考虑表达式语句和变量声明内的嵌套语句。
 *  此类作为各类语句的visit，找出该语句内的函数调用、逻辑运算符、局部变量、参数、类属性等。
 * @author Administrator
 *
 *Direct Known Subclasses:  表达式种类。

 */
public class CppFragmentVisitor extends ASTVisitor {
	List<String> identiferNames; //记录该语句的所有标识符。排除保留字、函数名、类名、方法名。
	List<String> invoMethods; //记录函数名、类名、方法名，将来要从identiferNames中删除它们。
	List<String> qualifyNames; //记录函数名、方法名前的限定名称，将来要从identiferNames中删除它们。不允许重复
	List<String> logicOperators;  //条件操作符，允许重复。&& || !
	List<String> strayOperators;  //其它操作符号：混合Infix,前缀表达式操作符，后缀表达式操作符，允许重复。 ++ -- ~ ^ & | 
	
	public CppFragmentVisitor()
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
			if( expression instanceof CPPASTConditionalExpression ) //条件表达式。
				return ProcessConditionalExpression((CPPASTConditionalExpression)expression);
			else if( expression instanceof CPPASTBinaryExpression ) //二元表达式。
				return ProcessBinaryOperator((CPPASTBinaryExpression)expression);
			else if( expression instanceof CPPASTUnaryExpression ) //一元表达式。
				return ProcessUnaryOperator((CPPASTUnaryExpression)expression);
			else if( expression instanceof CPPASTFunctionCallExpression ) //函数调用表达式。
				return ProcessFunctionCallExpression((CPPASTFunctionCallExpression)expression);
			/*注意：CPPASTFunctionCallExpression的部分内容会当做引用。
			 * 在ProcessFieldReferenceExpression中避免重复计算*/
			else if( expression instanceof CPPASTFieldReference ) //引用表达式。
				return ProcessFieldReferenceExpression((CPPASTFieldReference)expression);
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
	private int ProcessConditionalExpression(CPPASTConditionalExpression condExpression)
	{
		return PROCESS_CONTINUE; //允许嵌套
	}
	
	/**  二元表达式处理
	 * @param binaryExpression
	 * @return 返回skip，无法visit( IASTName astName ) 。暂时只能continue;
	 */
	private int ProcessBinaryOperator(CPPASTBinaryExpression binaryExpression)
	{
		int rtnProcess = PROCESS_CONTINUE; //允许嵌套
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
	
	/**  一元表达式处理
	 * @param binaryExpression
	 * @return 返回skip，无法visit( IASTName astName ) 。暂时只能continue;
	 */
	private int ProcessUnaryOperator(CPPASTUnaryExpression unaryExpression)
	{
		int rtnProcess = PROCESS_CONTINUE;//允许嵌套
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
	
	/**   复杂度  函数调用，简单区分内部方法调用和外部方法调用，加复杂度。
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
		//特别注意：由于代码不完整，解析后，csnVisitor.getLastName()返回的字符串可能为空 or null.
		if( (funcName!=null) && (!funcName.isEmpty()) )
			addInvoMethod(funcName); //最后一个认为是方法（函数）名称。
		//else  //check my parse program.
		//	System.out.println("csnVisitor.getLastName(); is error.");
		return PROCESS_CONTINUE; //允许嵌套
	}
	
	/** 暂时不考虑引用相关代码复杂度对软件错误定位的影响。  
	 * 引用的复杂度，不包括::
	 * This interface represents expressions that access a field reference. 
	 * e.g. a.b => a is the expression, b is the field name. 
	 * e.g. a()->def => a() is the expression, def is the field name.
	 * @param frefExpression   Field Reference
	 * @return
	 */
	private int ProcessFieldReferenceExpression(CPPASTFieldReference frefExpression)
	{
		ASTNodeProperty property = frefExpression.getPropertyInParent();
		
		/* * 注意：CPPASTFunctionCallExpression的部分内容会当做引用。
		 *  在ProcessFieldReferenceExpression中避免重复计算
		 *  frefExpression在父节点的属性为FUNCTION_NAME，则肯定它是函数调用表达式的一部分。
		 *  其它，还有很多种情况，如：IASTFunctionCallExpression.FUNCTION_ARGUMENT等，将是真正的引用。
		 */
		if( IASTFunctionCallExpression.FUNCTION_NAME!=property )
		{ 
			/*
			 * 因为IASTExpression 	astExpress肯定会被当做参数、局部变量或者类的属性计算复杂度。
			 * 所以将它放入invoMethods，不重复计算。
			 */
			//IASTName astName = frefExpression.getFieldName();
			//IASTExpression 	astExpress = frefExpression.getFieldOwner();

		}
		return PROCESS_CONTINUE; //允许嵌套，A.B.C这样的嵌套引用重复计算复杂度。
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
	
	/** 记录函数名、方法名前的限定名称，将来要从identiferNames中删除它们。不允许重复
	 * @param qualification
	 */
	private void addQualifyNames(String qualification)
	{
		if( !isExistOperator(qualifyNames,qualification) )
			qualifyNames.add(qualification);
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
	 * 语句中C++名称，包括变量名、类名等。
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
			/*A::B 形式，类的静态变量加进去。*/
			char[] fullName = astName.toCharArray();
			String strFull = new String(fullName);
			if( strFull.contains("::") )  //静态变量
				addIdentiferName(strFull);
			else //非静态变量
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
					//CPPASTTemplateId类型的也会成为CPPASTQualifiedName的一部分，而CPPASTTemplateId无法转换为CPPASTName;
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
		return PROCESS_SKIP; //不再搜索子节点。
	}
	
}
