/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBreakStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCaseStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTContinueStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDefaultStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDoStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTForStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTGotoStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTIfStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSwitchStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTWhileStatement;


/**
 * @author Administrator
 *
 */
public class ProcedureStatementVisitor  extends ASTVisitor {
	private List<StatementContext> statementList; //某个方法的所有语句。注意：尽量以行为单位定义语句，不同于AST的语句概念。
	
	public ProcedureStatementVisitor()
	{
		super(true);//public ASTVisitor(boolean visitNodes)
		statementList = new ArrayList<>();
	}
	//某个方法的所有语句。
	public List<StatementContext> getStatementList() {
		return statementList;
	}
	
	/** 
	 *IASTStatement : 接收各种语句。
	 * assert 语句作为 函数调用。
	 * For语句里的初始化部分IASTForStatement.INITIALIZER特殊，会以CASTExpressionStatement的形式重复visit。
	 *           而其它，象while,do while,if等语句则不会；甚至for
	 *     语句的update部分也不会重复visit。
	 */
	@Override
	public int visit(IASTStatement  statement) {
		try {
			if( statement instanceof CASTBreakStatement ) //break语句无复杂度，不用添加到语句列表。
				return PROCESS_SKIP;
			else if( statement instanceof  CASTContinueStatement )  //continue语句无复杂度，不用添加到语句列表。
				return 	PROCESS_SKIP;
			else if( statement instanceof  CASTDeclarationStatement )  //变量声明
				return 	ProcessDeclarationStatement((CASTDeclarationStatement)statement);
			else if( statement instanceof  CASTCaseStatement ) //case...
				return 	ProcessCaseStatement((CASTCaseStatement)statement);
			else if( statement instanceof  CASTDefaultStatement )  //Default语句无复杂度，不用添加到语句列表。
				return 	PROCESS_SKIP;
			else if( statement instanceof  CASTDoStatement )  //do while
				return 	ProcessDoStatement((CASTDoStatement)statement);
			else if( statement instanceof  CASTWhileStatement )  //while
				return 	ProcessWhileStatement((CASTWhileStatement)statement);
			else if( statement instanceof  CASTForStatement )  //for...
				return 	ProcessForStatement((CASTForStatement)statement);
			else if( statement instanceof  CASTIfStatement )  //if...
				return 	ProcessIfStatement((CASTIfStatement)statement);
			else if( statement instanceof  CASTReturnStatement )  //return...
				return 	ProcessReturnStatement((CASTReturnStatement)statement);
			else if( statement instanceof  CASTSwitchStatement )  //switch...
				return 	ProcessSwitchStatement((CASTSwitchStatement)statement);
			else if( statement instanceof  CASTExpressionStatement ) //普通表达式语句
			{
				ASTNodeProperty nodeProperty = statement.getPropertyInParent();
				if( nodeProperty==IASTForStatement.INITIALIZER  ) //排除循环里的表达式语句
					return 	PROCESS_SKIP;
				return 	ProcessExpressionStatement((CASTExpressionStatement)statement);
			}
			else if( statement instanceof  CASTGotoStatement )  //goto ...
				return 	ProcessGotoStatement((CASTGotoStatement)statement);
			else //其它未知语句
				return PROCESS_CONTINUE;
		}//end of try
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Parse of statement is error.");
			System.out.println(statement.getRawSignature());
			return PROCESS_ABORT;
		}
	}
	
	/**  计算case 语句的复杂度，并添加到List<StatementContext> statementList。
	 * @param caseStmt
	 * @return case 语句,作为一个整体，不再检索子节点。
	 */
	private int ProcessCaseStatement(CASTCaseStatement caseStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(caseStmt,StatementContext.IfStyle);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;
	}
	
	/**  计算普通表达式语句的复杂度，并添加到List<StatementContext> statementList。
	 * @param expressStmt 
	 * @return 该语句,作为一个整体，不再检索子节点。
	 */
	private int ProcessExpressionStatement(CASTExpressionStatement expressStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(expressStmt,StatementContext.StatementRest);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;
	}
	
	/**  计算变量声明语句的复杂度，并添加到List<StatementContext> statementList。
	 * 没有等于号的变量声明语句不是可执行语句，但这里没有排除，对后期的研究工作并无影响。
	 * @param declarStmt 
	 * @return 该语句,作为一个整体，不再检索子节点。
	 */
	private int ProcessDeclarationStatement(CASTDeclarationStatement declarStmt )
	{
		// IASTDeclaration	getDeclaration()
		StatementContext stmtCtx = evaluateComplexMetric(declarStmt,StatementContext.StatementRest);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;
	}
	
	
	/**  for 语句，。
	 * @param forStatement 
	 * @return 允许嵌套，检索子节点。
	 */
	private int ProcessForStatement(CASTForStatement forStatement )
	{
		boolean isSpecialFor = true;  //for(;;)这种for语句没有ForInit，Expression和ForUpdate
		StatementContext forContext = new StatementContext();
		//for语句的初始化部分。
		IASTStatement initialStmt = forStatement.getInitializerStatement();
		if(  initialStmt!=null )
		{
			isSpecialFor = false; //有ForInit
			StatementContext statement = evaluateComplexMetric(initialStmt,StatementContext.StatementRest);
			forContext.enlargeToComplexStatement(statement);
		}
		//forContext
		IASTExpression 	forExpress = forStatement.getConditionExpression();
		if( forExpress!=null )
		{
			isSpecialFor = false; //有Expression
			StatementContext statement = evaluateComplexMetric(forExpress,StatementContext.StatementRest);
			forContext.enlargeToComplexStatement(statement);
		}
		//for语句的update部分。
		IASTExpression 	forUpdates = forStatement.getIterationExpression();
		if( forUpdates!=null )
		{
			isSpecialFor = false; //有ForUpdate
			StatementContext statement = evaluateComplexMetric(forUpdates,StatementContext.StatementRest);
			forContext.enlargeToComplexStatement(statement);
		}		
		//for语句的三部分组成一个StatementContext，加入到语句列表。
		//担心for语句里 for 单独一行，那么该行号将不会作为整个for语句的起始行号。
		IASTFileLocation  fileLocation = forStatement.getFileLocation();
		int startLine = fileLocation.getStartingLineNumber();
		if( isSpecialFor )
		{ //for(;;)这种for语句没有ForInit，Expression和ForUpdate
			forContext.setStartLine(startLine);
			forContext.setEndLine(startLine); //不好取结束行。用起始行不准确，但不会影响SBFL。
		}
		else
			forContext.enlargeStartLineno(startLine);
		forContext.setStatementStyle(StatementContext.LoopStyle);
		statementList.add(forContext);
		return PROCESS_CONTINUE; //允许嵌套，搜索子节点
	}
	
	/**  do while语句，do不是可执行语句，loop不用处理，只计算while这句的复杂度。
	 * @param doStatement 
	 * @return 允许嵌套，检索子节点。
	 */
	private int ProcessDoStatement(CASTDoStatement doStatement )
	{
		IASTExpression 	astExpression = doStatement.getCondition();
		StatementContext stmtContext = evaluateComplexMetric(astExpression,StatementContext.LoopStyle);
		
		//body的结束行号作为它的起始行号，虽然不准确，但不会出错。
		IASTStatement astBody = doStatement.getBody();
		IASTFileLocation fileLocation = astBody.getFileLocation();
		int bodyEndLine = fileLocation.getEndingLineNumber();
		stmtContext.enlargeStartLineno(bodyEndLine);
		statementList.add(stmtContext);
		return PROCESS_CONTINUE; //允许嵌套，搜索子节点
	}
	
	/**  if 语句，then不是可执行语句，只计算if这句的复杂度。
	 * else 后可能跟if
	 * @param ifStatement 
	 * @return 允许嵌套，检索子节点。
	 */
	private int ProcessIfStatement(CASTIfStatement ifStatement )
	{
		IASTExpression 	astExpression = ifStatement.getConditionExpression();
		if( astExpression==null )
		{  //当形如	if( int ppp= Overview) 的语句出现时，if条件里并非表达式，而是声明语句。
			System.out.println("$$$$$        ProcessIfStatement  astExpression==null $$$$$$$");
			return PROCESS_SKIP; //不同于C++，不应该出现NULL的情况。
		}
		StatementContext stmtCtx = evaluateComplexMetric(astExpression,StatementContext.IfStyle);
		//担心if语句里 if 单独一行，那么该行号将不会作为整个if()语句的起始行号。
		IASTFileLocation fileLocation = ifStatement.getFileLocation();
		int startLine = fileLocation.getStartingLineNumber();
		stmtCtx.enlargeStartLineno(startLine);
		statementList.add(stmtCtx);
		return PROCESS_CONTINUE; //允许嵌套，搜索子节点
	}
	
	/**  Return语句。
	 * @param returnStmt 
	 * @return 该语句,作为一个整体，不再检索子节点。
	 */
	private int ProcessReturnStatement(CASTReturnStatement returnStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(returnStmt,StatementContext.ReturnStyle);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;//视作一条完整语句，不检索子节点。
	}
	
	/**  switch语句。
	 * @param switchStmt 
	 * @return 
	 */
	private int ProcessSwitchStatement(CASTSwitchStatement switchStmt )
	{
		IASTExpression 	astExpression = switchStmt.getControllerExpression();
		StatementContext stmtCtx = evaluateComplexMetric(astExpression,StatementContext.IfStyle);
		//担心switch语句里 switch 单独一行，那么该行号将不会作为整个switch()语句的起始行号。
		IASTFileLocation fileLocation = switchStmt.getFileLocation();
		int startLine = fileLocation.getStartingLineNumber();
		stmtCtx.enlargeStartLineno(startLine);
		
		statementList.add(stmtCtx);
		return PROCESS_CONTINUE;//允许嵌套，搜索子节点
	}
	
	/**  while语句。
	 * @param whileStmt 
	 * @return 
	 */
	private int ProcessWhileStatement(CASTWhileStatement whileStmt )
	{
		IASTExpression 	astExpression = whileStmt.getCondition();
		if( astExpression==null )
		{
			System.out.println("$$$$$        ProcessWhileStatement  astExpression==null $$$$$$$");
			return PROCESS_SKIP; //不同于C++，不应该出现NULL的情况。
		}
		StatementContext stmtCtx= evaluateComplexMetric(astExpression,StatementContext.LoopStyle);
		//担心while语句里 while 单独一行，那么该行号将不会作为整个while()语句的起始行号。
		IASTFileLocation fileLocation = whileStmt.getFileLocation();
		int startLine = fileLocation.getStartingLineNumber();
		stmtCtx.enlargeStartLineno(startLine);
		
		statementList.add(stmtCtx);
		return PROCESS_CONTINUE;//允许嵌套，搜索子节点
	}
	
	/**  goto 语句。
	 * @param gotoStmt 
	 * @return 该语句,作为一个整体，不再检索子节点。
	 */
	private int ProcessGotoStatement(CASTGotoStatement gotoStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(gotoStmt,StatementContext.StatementRest);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;//视作一条完整语句，不检索子节点。
	}
	
	/**   当前节点作为一条语句，整体地评估其认知复杂度。
	 * @param statement  包括定义语句，表达式语句，if,for...
	 * @return
	 */
	private StatementContext evaluateComplexMetric(IASTStatement  statement,int style)
	{
		return evaluateComplexMetric((IASTNode)statement,style);
	}
	
	/**   当前节点作为一个表达式，整体地评估其认知复杂度。
	 * @param expression  各种表达式
	 * @return
	 */
	private StatementContext evaluateComplexMetric(IASTExpression  expression,int style)
	{
		//expression.get
		return evaluateComplexMetric((IASTNode)expression,style);
	}
	
	/**   当前节点作为一个表达式，整体地评估其认知复杂度。
	 * @param declaration  变量声明
	 * @return
	 */
	private StatementContext evaluateComplexMetric(IASTDeclaration  declaration,int style)
	{
		return evaluateComplexMetric((IASTNode)declaration,style);
	}
	
	/**   为不同的对象来调用，产生带复杂度的语句计算。
	 * @param astNode  : 表达式，普通语句，变量声明，...
	 * @return
	 */
	private StatementContext evaluateComplexMetric(IASTNode  astNode,int style)
	{
		StatementContext stmtContext = new StatementContext();
		stmtContext.setStatementStyle(style); //语句类型。当前不能嵌套。
		
		IASTFileLocation  fileLocation = astNode.getFileLocation();
		//获得起始结束行号
		int startLine = fileLocation.getStartingLineNumber();
		int endLine = fileLocation.getEndingLineNumber();
		stmtContext.setStartLine(startLine);
		stmtContext.setEndLine(endLine);
		ProcedureFragmentVisitor fragmentVisitor= new ProcedureFragmentVisitor();
		astNode.accept(fragmentVisitor);
		//先清洗标识符，再将其赋值给语句。
		fragmentVisitor.excludeInvoMethodFromIdentiferName();
		stmtContext.setSimpleNames(fragmentVisitor.getIdentiferNames());
		stmtContext.setFuncCalls(fragmentVisitor.getInvoMethods());
		stmtContext.setLogicOperaters(fragmentVisitor.getLogicOperators());
		stmtContext.setOtherOperaters(fragmentVisitor.getStrayOperators());

		//检查该语句，或者表达式里是否有宏定义，有的话，加入语句的simpleNames；将来添加复杂度。
		IASTNodeLocation[] location = astNode.getNodeLocations();
		for (IASTNodeLocation loc : location) {
		    if (loc instanceof IASTMacroExpansionLocation) {
		    	IASTPreprocessorMacroExpansion iapmExpansion = ((IASTMacroExpansionLocation)loc).getExpansion();
		    	// IASTPreprocessorMacroDefinition imd= iapme.getMacroDefinition(); //<< returns the macro that generated "node"
		        IASTName macroName = iapmExpansion.getMacroReference();
		        stmtContext.addMacroDefinitionName(macroName.getLastName().toString());
		    }
		}
		return stmtContext;
	}
}
