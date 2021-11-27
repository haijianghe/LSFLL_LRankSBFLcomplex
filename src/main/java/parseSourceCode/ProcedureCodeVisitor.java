/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTKnRFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTName;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclaration;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTElif;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTEndif;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTIfdef;
import  org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTNode;

/**
 * @author Administrator
 *
 */
public class ProcedureCodeVisitor extends ASTVisitor {
	private ClassContext clazzContext; //该文件内包含的解析数据，C语言没有类，为了编程方便，虚拟一个类。
	String parsingFilename; //解析结果来自于哪个文件，不带目录信息。
	
	public ProcedureCodeVisitor(String filename) 
	{
		super(true); //在C++的解析代码，有详细的配置项说明。
		//this.includeInactiveNodes = true;
		clazzContext = new ClassContext();
		parsingFilename = filename;
		clazzContext.setParsingFilename(filename); //记录解析结果来自哪个文件。
		clazzContext.setVirtualNameCategory();//c语言程序的虚拟类的类名和类别。
	}
	
	/** C程序，每个文件被当做一个单独的类，函数是其方法，全局变量是其属性。
	 *  IASTDeclaration。
	 */
	@Override
	public int visit(IASTDeclaration  nodeDecl) {
		try {
			if( !(nodeDecl.isPartOfTranslationUnitFile()) )
				return PROCESS_CONTINUE;
			ASTNodeProperty  nodeProperty = nodeDecl.getPropertyInParent();
			if( nodeProperty!=IASTTranslationUnit.OWNED_DECLARATION ) //是否顶层函数或声明语句。
				return PROCESS_CONTINUE; 
			if ( nodeDecl instanceof CASTSimpleDeclaration  )
			{	//全局变量，函数体外的语句。
				//System.out.println("^^^^  "+nodeDecl.getRawSignature());
				CASTSimpleDeclaration simpDecl = (CASTSimpleDeclaration) nodeDecl;
				processTopDeclaration(simpDecl);
				return PROCESS_SKIP; //不寻找内嵌声明语句。
			}
			else if ( nodeDecl instanceof CASTFunctionDefinition  )
			{ //普通函数。
				//System.out.println("####  "+nodeDecl.getRawSignature());
				processTopFunction((CASTFunctionDefinition)nodeDecl); //普通函数。
				return PROCESS_SKIP; //不寻找函数内嵌套函数，不再搜索子节点。
			}
			else  //IASTAmbiguousSimpleDeclaration, IASTASMDeclaration, IASTProblemDeclaration 
				return PROCESS_CONTINUE;
		}//end of try
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Parse of "+ parsingFilename+" is error.");
			return PROCESS_ABORT;
		}
	}

	/**
	 * @param funcDeclar
	 * @throws DOMException
	 */
	private void processTopFunction(CASTFunctionDefinition funcDefine) throws DOMException
	{
		//如果是函数的话，要先判断，是否在头文件内。
		String pathFilename = funcDefine.getContainingFilename();
		String curParsingFile = ProjectContext.getFilenameFromPathFile(pathFilename);
		if( !curParsingFile.contentEquals(parsingFilename) )
			return; //头文件内的可执行语句不能放到解析文件的ClassContext中。
		
		//构造函数，并填充局部变量、参数和语句。
		MethodContext topLevelMethod = new MethodContext();
		IASTFileLocation  fileLocation = funcDefine.getFileLocation();
		//获得起始结束行号
		int startLine = fileLocation.getStartingLineNumber();
		int endLine = fileLocation.getEndingLineNumber();
		topLevelMethod.setStartLine(startLine);
		topLevelMethod.setEndLine(endLine);
		// CASTFunctionDeclarator or  CASTKnRFunctionDeclarator
		IASTFunctionDeclarator funcDeclar = funcDefine.getDeclarator();
		IASTName astName = funcDeclar.getName();
		String myMethodName = astName.toString(); //方法名
		topLevelMethod.setType(3);//C文件的普通函数；
		topLevelMethod.setName(myMethodName);
		
		//填充参数
		List<String> methodParameters = new ArrayList<>();
		if( funcDeclar instanceof CASTFunctionDeclarator )
		{
			CASTFunctionDeclarator cfFuncDeclar = (CASTFunctionDeclarator)funcDeclar;
			IASTParameterDeclaration[] paraDeclars = cfFuncDeclar.getParameters();
			for( IASTParameterDeclaration icpd : paraDeclars )
			{
				IASTDeclarator 	icdor = icpd.getDeclarator();
				methodParameters.add(icdor.getName().toString());
			}
		}
		else // CASTKnRFunctionDeclarator
		{
			CASTKnRFunctionDeclarator KnrFuncDeclar = (CASTKnRFunctionDeclarator)funcDeclar;
			IASTName[] paraNames = KnrFuncDeclar.getParameterNames();
			for( IASTName iname : paraNames )
				methodParameters.add(iname.toString());
		}
		topLevelMethod.setParameters(methodParameters);
		
		//填充局部变量
		List<String> localVariables = new ArrayList<>();
		EnumInvocationIdentifer(funcDefine,localVariables);
		topLevelMethod.copyLocalVariables(localVariables);
		
	    //找出该方法内的所有语句。
	    ProcedureStatementVisitor csVisitor = new ProcedureStatementVisitor();
	    funcDefine.accept(csVisitor);
	    //还要加上语句的数据。
	    topLevelMethod.fillStatementsToMethod(csVisitor.getStatementList());
		
		//如果某条语句使用了方法的参数或者局部变量，相应地增加复杂度。
	    //topLevelMethod.adjustComplexMetricWithParameterAndVaiable();
		
	    clazzContext.addMethod(topLevelMethod);
	}
	
	/**  找出方法里的局部变量，加入到localVariables
	 * @param funcDefine 指定方法
	 *@param localVariables
	 */
	private void EnumInvocationIdentifer(CASTFunctionDefinition funcDefine,List<String> localVariables)
	{
		funcDefine.accept(  new ASTVisitor() { 
			{
		        super.shouldVisitDeclarations = true;   //Set this flag to visit declarations.
		        super.shouldVisitStatements = true;             //Set this flag to visit statements.
			}
			
			@Override
			public int visit( IASTStatement statement ) {
				if( !(statement instanceof CASTDeclarationStatement) )
					return PROCESS_CONTINUE;
				//判断是指定方法的直接声明语句吗？父节点是否函数体
				IASTNode parentNode = statement.getParent();
				ASTNodeProperty  ppnodeProperty = parentNode.getPropertyInParent(); 
				if( ppnodeProperty!=IASTFunctionDefinition.FUNCTION_BODY ) //是否函数的顶层声明语句。
					return PROCESS_SKIP; //不寻找语句内嵌语句，不再搜索子节点。
				IASTDeclaration astDeclar = ((CASTDeclarationStatement)statement).getDeclaration();
				IASTDeclarator[] astDeclators = ((CASTSimpleDeclaration)astDeclar).getDeclarators();
				for( IASTDeclarator declator : astDeclators )
				{
					localVariables.add(declator.getName().toString());
				}
				return PROCESS_SKIP; //不寻找语句内嵌语句，不再搜索子节点。
			}

		}//end of ASTVisitor	
		); //end of accept.
	}
	
	/**  解析全局变量，函数体外的语句。
	 * 函数体外的语句作为虚拟类的属性，将来计算复杂度时+3
	 * @param simpDecl
	 * @throws DOMException
	 */
	private void processTopDeclaration(CASTSimpleDeclaration simpDecl) throws DOMException
	{
		IASTDeclSpecifier iasdSpeci = simpDecl.getDeclSpecifier();
		if( iasdSpeci.getStorageClass()==IASTDeclSpecifier.sc_typedef )
			return;  //过滤typedef

		IASTDeclarator[] astDecltors = simpDecl.getDeclarators();
		//该语句是可执行语句吗，带=的认为是。 
		boolean isExecute = false;
		List<String> declatorLst = new ArrayList<String>();//记录全局变量。
		//每个标识符都当做全局变量，虚拟类的属性。
		for( IASTDeclarator declar :astDecltors )
		{
			if( 	declar instanceof  IASTFunctionDeclarator    ||
					declar instanceof ICASTKnRFunctionDeclarator ||
					declar instanceof IASTStandardFunctionDeclarator ||
					declar instanceof IASTAmbiguousDeclarator )
				continue;  //避免函数名作为全局变量。
			// IASTArrayDeclarator, IASTFieldDeclarator
			
			String varname = declar.getName().toString(); //标识符名称
			clazzContext.addAttribueName(varname); //函数体外的语句，作为虚拟类的属性。
			declatorLst.add(varname);
			IASTInitializer inital = declar.getInitializer();
			if( inital!=null ) //有初始化的才认为是可执行语句
				isExecute = true;
		}
		if( !isExecute )
			return;  //并非可执行语句，后面的没有必要执行。
		//如果是可执行语句的变量声明，要先判断，是否在头文件内。
		String pathFilename = simpDecl.getContainingFilename();
		String curParsingFile = ProjectContext.getFilenameFromPathFile(pathFilename);
		if( !curParsingFile.contentEquals(parsingFilename) )
			return; //头文件内的可执行语句不能放到解析文件的ClassContext中。
				
		//将该执行语句信息加入到方法中。先创建该语句。
		StatementContext stmtContext = new StatementContext();
		stmtContext.setStatementStyle(StatementContext.StatementRest);
		IASTFileLocation  fileLocation = simpDecl.getFileLocation();
		//获得起始结束行号
		int startLine = fileLocation.getStartingLineNumber();
		int endLine = fileLocation.getEndingLineNumber();
		stmtContext.setStartLine(startLine);
		stmtContext.setEndLine(endLine);
		ProcedureFragmentVisitor fragmentVisitor= new ProcedureFragmentVisitor();
		simpDecl.accept(fragmentVisitor);
		//使用ProcedureFragmentVisitor后，不用类似函数的方法，而是：将getDeclarators获得的符号记录为标识符，
		//   再在所有内容解析完成后，...，也提供了一种验证的做法。
		stmtContext.setSimpleNames(declatorLst);
		stmtContext.setFuncCalls(fragmentVisitor.getInvoMethods());
		stmtContext.setLogicOperaters(fragmentVisitor.getLogicOperators());
		stmtContext.setOtherOperaters(fragmentVisitor.getStrayOperators());

		MethodContext methodCtx = createOrObtainSpecialMethod();
		methodCtx.addOneStatement(stmtContext);
		//createOrObtainSpecialMethod里面已经将方法添加到ClassContext，此处不用重复添加。
	}
	
	/*
	 * 从方法列表中查找，是否存在专为收集函数体外可执行语句设置的方法，如果没有，则创建一个，并给之命名，加入方法队列。
	 * 如果有，则直接返回该方法。
	 */
	private MethodContext createOrObtainSpecialMethod()
	{
		MethodContext methodContext = null;
		for( MethodContext cCtxt: clazzContext.getMethods() )
		{
			if( cCtxt.isGlobalVariableOfC() )
			{
				methodContext = cCtxt;
				break;
			}
		}
		if( methodContext==null )
		{
			methodContext = new MethodContext();
			methodContext.setGlobalVariableOfC();
		    //将该方法添加到类的方法列表
			clazzContext.addMethod(methodContext);
		}	
		return methodContext;
	}
	
	//返回类的列表结果
  	public ClassContext getClazzContext() {
  		return clazzContext;
  	}
  	
}
