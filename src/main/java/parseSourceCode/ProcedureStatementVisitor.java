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
	private List<StatementContext> statementList; //ĳ��������������䡣ע�⣺��������Ϊ��λ������䣬��ͬ��AST�������
	
	public ProcedureStatementVisitor()
	{
		super(true);//public ASTVisitor(boolean visitNodes)
		statementList = new ArrayList<>();
	}
	//ĳ��������������䡣
	public List<StatementContext> getStatementList() {
		return statementList;
	}
	
	/** 
	 *IASTStatement : ���ո�����䡣
	 * assert �����Ϊ �������á�
	 * For�����ĳ�ʼ������IASTForStatement.INITIALIZER���⣬����CASTExpressionStatement����ʽ�ظ�visit��
	 *           ����������while,do while,if������򲻻᣻����for
	 *     ����update����Ҳ�����ظ�visit��
	 */
	@Override
	public int visit(IASTStatement  statement) {
		try {
			if( statement instanceof CASTBreakStatement ) //break����޸��Ӷȣ�������ӵ�����б�
				return PROCESS_SKIP;
			else if( statement instanceof  CASTContinueStatement )  //continue����޸��Ӷȣ�������ӵ�����б�
				return 	PROCESS_SKIP;
			else if( statement instanceof  CASTDeclarationStatement )  //��������
				return 	ProcessDeclarationStatement((CASTDeclarationStatement)statement);
			else if( statement instanceof  CASTCaseStatement ) //case...
				return 	ProcessCaseStatement((CASTCaseStatement)statement);
			else if( statement instanceof  CASTDefaultStatement )  //Default����޸��Ӷȣ�������ӵ�����б�
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
			else if( statement instanceof  CASTExpressionStatement ) //��ͨ���ʽ���
			{
				ASTNodeProperty nodeProperty = statement.getPropertyInParent();
				if( nodeProperty==IASTForStatement.INITIALIZER  ) //�ų�ѭ����ı��ʽ���
					return 	PROCESS_SKIP;
				return 	ProcessExpressionStatement((CASTExpressionStatement)statement);
			}
			else if( statement instanceof  CASTGotoStatement )  //goto ...
				return 	ProcessGotoStatement((CASTGotoStatement)statement);
			else //����δ֪���
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
	
	/**  ����case ���ĸ��Ӷȣ�����ӵ�List<StatementContext> statementList��
	 * @param caseStmt
	 * @return case ���,��Ϊһ�����壬���ټ����ӽڵ㡣
	 */
	private int ProcessCaseStatement(CASTCaseStatement caseStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(caseStmt,StatementContext.IfStyle);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;
	}
	
	/**  ������ͨ���ʽ���ĸ��Ӷȣ�����ӵ�List<StatementContext> statementList��
	 * @param expressStmt 
	 * @return �����,��Ϊһ�����壬���ټ����ӽڵ㡣
	 */
	private int ProcessExpressionStatement(CASTExpressionStatement expressStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(expressStmt,StatementContext.StatementRest);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;
	}
	
	/**  ��������������ĸ��Ӷȣ�����ӵ�List<StatementContext> statementList��
	 * û�е��ںŵı���������䲻�ǿ�ִ����䣬������û���ų����Ժ��ڵ��о���������Ӱ�졣
	 * @param declarStmt 
	 * @return �����,��Ϊһ�����壬���ټ����ӽڵ㡣
	 */
	private int ProcessDeclarationStatement(CASTDeclarationStatement declarStmt )
	{
		// IASTDeclaration	getDeclaration()
		StatementContext stmtCtx = evaluateComplexMetric(declarStmt,StatementContext.StatementRest);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;
	}
	
	
	/**  for ��䣬��
	 * @param forStatement 
	 * @return ����Ƕ�ף������ӽڵ㡣
	 */
	private int ProcessForStatement(CASTForStatement forStatement )
	{
		boolean isSpecialFor = true;  //for(;;)����for���û��ForInit��Expression��ForUpdate
		StatementContext forContext = new StatementContext();
		//for���ĳ�ʼ�����֡�
		IASTStatement initialStmt = forStatement.getInitializerStatement();
		if(  initialStmt!=null )
		{
			isSpecialFor = false; //��ForInit
			StatementContext statement = evaluateComplexMetric(initialStmt,StatementContext.StatementRest);
			forContext.enlargeToComplexStatement(statement);
		}
		//forContext
		IASTExpression 	forExpress = forStatement.getConditionExpression();
		if( forExpress!=null )
		{
			isSpecialFor = false; //��Expression
			StatementContext statement = evaluateComplexMetric(forExpress,StatementContext.StatementRest);
			forContext.enlargeToComplexStatement(statement);
		}
		//for����update���֡�
		IASTExpression 	forUpdates = forStatement.getIterationExpression();
		if( forUpdates!=null )
		{
			isSpecialFor = false; //��ForUpdate
			StatementContext statement = evaluateComplexMetric(forUpdates,StatementContext.StatementRest);
			forContext.enlargeToComplexStatement(statement);
		}		
		//for�������������һ��StatementContext�����뵽����б�
		//����for����� for ����һ�У���ô���кŽ�������Ϊ����for������ʼ�кš�
		IASTFileLocation  fileLocation = forStatement.getFileLocation();
		int startLine = fileLocation.getStartingLineNumber();
		if( isSpecialFor )
		{ //for(;;)����for���û��ForInit��Expression��ForUpdate
			forContext.setStartLine(startLine);
			forContext.setEndLine(startLine); //����ȡ�����С�����ʼ�в�׼ȷ��������Ӱ��SBFL��
		}
		else
			forContext.enlargeStartLineno(startLine);
		forContext.setStatementStyle(StatementContext.LoopStyle);
		statementList.add(forContext);
		return PROCESS_CONTINUE; //����Ƕ�ף������ӽڵ�
	}
	
	/**  do while��䣬do���ǿ�ִ����䣬loop���ô���ֻ����while���ĸ��Ӷȡ�
	 * @param doStatement 
	 * @return ����Ƕ�ף������ӽڵ㡣
	 */
	private int ProcessDoStatement(CASTDoStatement doStatement )
	{
		IASTExpression 	astExpression = doStatement.getCondition();
		StatementContext stmtContext = evaluateComplexMetric(astExpression,StatementContext.LoopStyle);
		
		//body�Ľ����к���Ϊ������ʼ�кţ���Ȼ��׼ȷ�����������
		IASTStatement astBody = doStatement.getBody();
		IASTFileLocation fileLocation = astBody.getFileLocation();
		int bodyEndLine = fileLocation.getEndingLineNumber();
		stmtContext.enlargeStartLineno(bodyEndLine);
		statementList.add(stmtContext);
		return PROCESS_CONTINUE; //����Ƕ�ף������ӽڵ�
	}
	
	/**  if ��䣬then���ǿ�ִ����䣬ֻ����if���ĸ��Ӷȡ�
	 * else ����ܸ�if
	 * @param ifStatement 
	 * @return ����Ƕ�ף������ӽڵ㡣
	 */
	private int ProcessIfStatement(CASTIfStatement ifStatement )
	{
		IASTExpression 	astExpression = ifStatement.getConditionExpression();
		if( astExpression==null )
		{  //������	if( int ppp= Overview) ��������ʱ��if�����ﲢ�Ǳ��ʽ������������䡣
			System.out.println("$$$$$        ProcessIfStatement  astExpression==null $$$$$$$");
			return PROCESS_SKIP; //��ͬ��C++����Ӧ�ó���NULL�������
		}
		StatementContext stmtCtx = evaluateComplexMetric(astExpression,StatementContext.IfStyle);
		//����if����� if ����һ�У���ô���кŽ�������Ϊ����if()������ʼ�кš�
		IASTFileLocation fileLocation = ifStatement.getFileLocation();
		int startLine = fileLocation.getStartingLineNumber();
		stmtCtx.enlargeStartLineno(startLine);
		statementList.add(stmtCtx);
		return PROCESS_CONTINUE; //����Ƕ�ף������ӽڵ�
	}
	
	/**  Return��䡣
	 * @param returnStmt 
	 * @return �����,��Ϊһ�����壬���ټ����ӽڵ㡣
	 */
	private int ProcessReturnStatement(CASTReturnStatement returnStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(returnStmt,StatementContext.ReturnStyle);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;//����һ��������䣬�������ӽڵ㡣
	}
	
	/**  switch��䡣
	 * @param switchStmt 
	 * @return 
	 */
	private int ProcessSwitchStatement(CASTSwitchStatement switchStmt )
	{
		IASTExpression 	astExpression = switchStmt.getControllerExpression();
		StatementContext stmtCtx = evaluateComplexMetric(astExpression,StatementContext.IfStyle);
		//����switch����� switch ����һ�У���ô���кŽ�������Ϊ����switch()������ʼ�кš�
		IASTFileLocation fileLocation = switchStmt.getFileLocation();
		int startLine = fileLocation.getStartingLineNumber();
		stmtCtx.enlargeStartLineno(startLine);
		
		statementList.add(stmtCtx);
		return PROCESS_CONTINUE;//����Ƕ�ף������ӽڵ�
	}
	
	/**  while��䡣
	 * @param whileStmt 
	 * @return 
	 */
	private int ProcessWhileStatement(CASTWhileStatement whileStmt )
	{
		IASTExpression 	astExpression = whileStmt.getCondition();
		if( astExpression==null )
		{
			System.out.println("$$$$$        ProcessWhileStatement  astExpression==null $$$$$$$");
			return PROCESS_SKIP; //��ͬ��C++����Ӧ�ó���NULL�������
		}
		StatementContext stmtCtx= evaluateComplexMetric(astExpression,StatementContext.LoopStyle);
		//����while����� while ����һ�У���ô���кŽ�������Ϊ����while()������ʼ�кš�
		IASTFileLocation fileLocation = whileStmt.getFileLocation();
		int startLine = fileLocation.getStartingLineNumber();
		stmtCtx.enlargeStartLineno(startLine);
		
		statementList.add(stmtCtx);
		return PROCESS_CONTINUE;//����Ƕ�ף������ӽڵ�
	}
	
	/**  goto ��䡣
	 * @param gotoStmt 
	 * @return �����,��Ϊһ�����壬���ټ����ӽڵ㡣
	 */
	private int ProcessGotoStatement(CASTGotoStatement gotoStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(gotoStmt,StatementContext.StatementRest);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;//����һ��������䣬�������ӽڵ㡣
	}
	
	/**   ��ǰ�ڵ���Ϊһ����䣬�������������֪���Ӷȡ�
	 * @param statement  ����������䣬���ʽ��䣬if,for...
	 * @return
	 */
	private StatementContext evaluateComplexMetric(IASTStatement  statement,int style)
	{
		return evaluateComplexMetric((IASTNode)statement,style);
	}
	
	/**   ��ǰ�ڵ���Ϊһ�����ʽ���������������֪���Ӷȡ�
	 * @param expression  ���ֱ��ʽ
	 * @return
	 */
	private StatementContext evaluateComplexMetric(IASTExpression  expression,int style)
	{
		//expression.get
		return evaluateComplexMetric((IASTNode)expression,style);
	}
	
	/**   ��ǰ�ڵ���Ϊһ�����ʽ���������������֪���Ӷȡ�
	 * @param declaration  ��������
	 * @return
	 */
	private StatementContext evaluateComplexMetric(IASTDeclaration  declaration,int style)
	{
		return evaluateComplexMetric((IASTNode)declaration,style);
	}
	
	/**   Ϊ��ͬ�Ķ��������ã����������Ӷȵ������㡣
	 * @param astNode  : ���ʽ����ͨ��䣬����������...
	 * @return
	 */
	private StatementContext evaluateComplexMetric(IASTNode  astNode,int style)
	{
		StatementContext stmtContext = new StatementContext();
		stmtContext.setStatementStyle(style); //������͡���ǰ����Ƕ�ס�
		
		IASTFileLocation  fileLocation = astNode.getFileLocation();
		//�����ʼ�����к�
		int startLine = fileLocation.getStartingLineNumber();
		int endLine = fileLocation.getEndingLineNumber();
		stmtContext.setStartLine(startLine);
		stmtContext.setEndLine(endLine);
		ProcedureFragmentVisitor fragmentVisitor= new ProcedureFragmentVisitor();
		astNode.accept(fragmentVisitor);
		//����ϴ��ʶ�����ٽ��丳ֵ����䡣
		fragmentVisitor.excludeInvoMethodFromIdentiferName();
		stmtContext.setSimpleNames(fragmentVisitor.getIdentiferNames());
		stmtContext.setFuncCalls(fragmentVisitor.getInvoMethods());
		stmtContext.setLogicOperaters(fragmentVisitor.getLogicOperators());
		stmtContext.setOtherOperaters(fragmentVisitor.getStrayOperators());

		//������䣬���߱��ʽ���Ƿ��к궨�壬�еĻ�����������simpleNames��������Ӹ��Ӷȡ�
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
