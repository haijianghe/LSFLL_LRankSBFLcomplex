/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * @author Administrator
 *
 */
public class CppCodeVisitor extends ASTVisitor {
	private List<ClassContext> clazzList; //该文件内包含的类以及嵌套类。
	String parsingFilename; //解析结果来自于哪个文件，不带目录信息。
	private ClassContext topDeclartion; //该C++文件内包含的顶层变量（全局变量），为了编程方便，虚拟一个类"TopDeclartionCpp"。
	
	public CppCodeVisitor(String filename) 
	{
		super.includeInactiveNodes = true;  //Per default inactive nodes are not visited.
        super.shouldVisitAmbiguousNodes = true; //Normally neither ambiguous nodes nor their children are visited.
        super.shouldVisitArrayModifiers = true; //Set this flag to visit array modifiers.
        super.shouldVisitBaseSpecifiers = true; //Set this flag to visit base specifiers off composite types.
        super.shouldVisitDeclarations = true;   //Set this flag to visit declarations.
        super.shouldVisitDeclarators = true;    //Set this flag to visit declarators.
        super.shouldVisitDeclSpecifiers = true; //Set this flag to visit declaration specifiers.
        super.shouldVisitDesignators = true;    // Set this flag to visit designators of initializers.
        super.shouldVisitEnumerators = true;    //Set this flag to visit enumerators.
        super.shouldVisitExpressions = true;    //Set this flag to visit expressions.
        //Sometimes more than one implicit name is created for a binding, set this flag to true to visit more than one name for an implicit binding.
        super.shouldVisitImplicitNameAlternates = true;
        //Implicit names are created to allow implicit bindings to be resolved, normally they are not visited, set this flag to true to visit them.
        super.shouldVisitImplicitNames = true;
        super.shouldVisitInitializers = true;  //Set this flag to visit initializers.
        super.shouldVisitNames = true;         // Set this flag to visit names.
        super.shouldVisitNamespaces = true;    // Set this flag to visit to visit namespaces.
        super.shouldVisitParameterDeclarations = true;  //Set this flag to visit parameter declarations.
        super.shouldVisitPointerOperators = true;       //Set this flag to visit pointer operators of declarators.
        super.shouldVisitProblems = true;               //Set this flag to visit problem nodes.
        super.shouldVisitStatements = true;             //Set this flag to visit statements.
        super.shouldVisitTemplateParameters = true;     //Set this flag to visit template parameters.
        super.shouldVisitTranslationUnit = true;        //Set this flag to visit translation units.
        super.shouldVisitTypeIds = true;                //Set this flag to visit typeids.

        clazzList = new ArrayList<>();
		parsingFilename = filename;
		topDeclartion = new ClassContext();
		topDeclartion.setParsingFilename(filename); //记录解析结果来自哪个文件。
		topDeclartion.setTopDeclartionNameCategory();//c++语言程序的TopDeclartionCpp类的类名和类别。
	}
	
	/** 每个类或接口类型的声明为一个CPPASTSimpleDeclaration 节点，包括注释文档。同一编译单元可以有多个类声明。
	 *  IASTDeclaration。
	 */
	@Override
	public int visit(IASTDeclaration  nodeDecl) {
		//System.out.println("####  "+nodeDecl.getClass().toString());
		try {
			if( !(nodeDecl.isPartOfTranslationUnitFile()) )
				return PROCESS_CONTINUE;
			if ( nodeDecl instanceof CPPASTSimpleDeclaration  )
			{	//普通类，内嵌的类也在此求出。
				CPPASTSimpleDeclaration simpDecl = (CPPASTSimpleDeclaration) nodeDecl;
				IASTDeclSpecifier declSpecifier = simpDecl.getDeclSpecifier();
				if ( declSpecifier instanceof CPPASTCompositeTypeSpecifier )
				{
					processCppClassStruct(simpDecl);//C++类，结构，带限定符的类；
					return PROCESS_CONTINUE;
				}
				else
				{ //处理顶层的变量定义语句。
					processTopDeclaration(simpDecl);
					return PROCESS_SKIP;//不寻找内嵌声明语句。
				}
			}
			else if ( nodeDecl instanceof CPPASTFunctionDefinition  )
			{ //普通函数。
				IASTNode parentNode = nodeDecl.getParent();
				//System.out.println("parentNode "+parentNode.getClass().toString());
				if( parentNode instanceof CPPASTTranslationUnit || parentNode instanceof CPPASTNamespaceDefinition )
				{//找出文件的top-level函数
					CPPASTFunctionDefinition cppFunction = (CPPASTFunctionDefinition)nodeDecl;
					IASTFunctionDeclarator funcDeclar = ( IASTFunctionDeclarator)cppFunction.getDeclarator();
					if ( !(funcDeclar instanceof CPPASTFunctionDeclarator) )
						return PROCESS_SKIP;//不是顶层函数，不再搜索子节点。
					processTopFunction(cppFunction); //普通函数。
					return PROCESS_SKIP; //不寻找函数内嵌套函数，不再搜索子节点。
				}
				else
					return PROCESS_SKIP; //不是顶层函数，不再搜索子节点。
			}
			else if ( nodeDecl instanceof CPPASTTemplateDeclaration  )
			{ //模板函数。
				IASTNode parentNode = nodeDecl.getParent();
				if( parentNode instanceof CPPASTTranslationUnit || parentNode instanceof CPPASTNamespaceDefinition )
				{//找出文件的top-level模板函数
					CPPASTTemplateDeclaration cppTemplate = (CPPASTTemplateDeclaration)nodeDecl;
					IASTDeclaration tempDeclar = ( IASTDeclaration)cppTemplate.getDeclaration();
					if ( !(tempDeclar instanceof CPPASTFunctionDefinition) )
						return PROCESS_SKIP;//不是顶层函数，不再搜索子节点。
					CPPASTFunctionDefinition funcDefin = (CPPASTFunctionDefinition)tempDeclar; 
					IASTFunctionDeclarator funcDeclar = ( IASTFunctionDeclarator)funcDefin.getDeclarator();
					if ( !(funcDeclar instanceof CPPASTFunctionDeclarator) )
						return PROCESS_SKIP;//不是顶层函数，不再搜索子节点。
					processTopFunction(funcDefin); //模板函数。
					return PROCESS_SKIP; //不寻找函数内嵌套函数，不再搜索子节点。
				}
				else
					return PROCESS_SKIP; //不是顶层函数，不再搜索子节点。
			}
			/*
			 * 不属于任何类的赋值语句没有加进来，以后考虑。
			 */
			else
				return PROCESS_CONTINUE;
		}//end of try
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Parse of "+ parsingFilename+" is error.");
			return PROCESS_ABORT;
		}
	}

	/*
	 * 从clazzList中查找，名为className的类，如果没有，则创建一个，并给之命名。
	 * 特别注意：此处会添加到列表，后面不要重复添加。
	 */
	private ClassContext createOrObtainClass(String className)
	{
		ClassContext rtnCtx = null;
		for( ClassContext cCtxt: clazzList )
		{
			if( cCtxt.getName().contentEquals(className) )
			{
				rtnCtx = cCtxt;
				break;
			}
		}
		if( rtnCtx==null )
		{
			rtnCtx = new ClassContext();
			rtnCtx.setName(className);
		    //将类添加到列表
			clazzList.add(rtnCtx);
		}	
		return rtnCtx;
	}
	
	
	/**
	 * @param funcDeclar
	 * @throws DOMException
	 */
	private void processTopFunction(CPPASTFunctionDefinition funcDefine) throws DOMException
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

		CPPASTFunctionDeclarator funcDeclar = ( CPPASTFunctionDeclarator)funcDefine.getDeclarator();
		IASTName astName = funcDeclar.getName();
		String myClassName;//类名。
		String myMethodName; //方法名
		//是topLevelFunction，还是缺少头文件的类；
		String declartor = astName.toString();
		String[] parsed = declartor.split("::");
		int len = parsed.length;
		int category;// 类的类别。12,C++里的普通函数(不属于任何类); 13,缺少头文件的类；
		if( len<=1 )
		{  //无限定名，只有函数名。
			myClassName = ClassContext.TopLevelFunction;
			myMethodName = declartor;
			category =12;
			topLevelMethod.setType(11);//C++文件的顶层函数；
		}
		else
		{
			myClassName = parsed[len-2];//倒数第二个符号。即算形如XX::yy::zz的多个限定符，取倒数第二个符号的做法也很好。
			myMethodName = parsed[len-1];//最后一个符号
			category =13;
			topLevelMethod.setType(12);//缺少头文件的类的方法，类似于C++文件的顶层函数
		}
		topLevelMethod.setName(myMethodName);
		
		//填充参数
		List<String> methodParameters = new ArrayList<>();
		ICPPASTParameterDeclaration[] paraDeclars = funcDeclar.getParameters();
		for( ICPPASTParameterDeclaration icpd : paraDeclars )
		{
			ICPPASTDeclarator 	icdor = icpd.getDeclarator();
			methodParameters.add(icdor.getName().toString());
		}
		topLevelMethod.setParameters(methodParameters);
		
		//填充局部变量
		CppCompositeMethodVisitor ccmVisitor = new CppCompositeMethodVisitor(funcDefine);
		funcDefine.accept(ccmVisitor);
		topLevelMethod.copyLocalVariables(ccmVisitor.getVariables());
		
	    //找出该方法内的所有语句。
	    CppStatementVisitor csVisitor = new CppStatementVisitor();
	    funcDefine.accept(csVisitor);
	    //还要加上语句的数据。
	    topLevelMethod.fillStatementsToMethod(csVisitor.getStatementList());
		
		//如果某条语句使用了方法的参数或者局部变量，相应地增加复杂度。
	    //topLevelMethod.adjustComplexMetricWithParameterAndVaiable();
		//暂时不考虑此类型函数的的属性，当然不计算其复杂度。
    
		//构造类，并填充类的所有属性和方法。 
		ClassContext clazz = createOrObtainClass(myClassName);//查找类（若无，则创建、并加入队列）
		//解决头文件，主文件不同的问题。
		String filename = ProjectContext.getFilenameFromPathFile(funcDefine.getContainingFilename());
		clazz.setFuncParsingFilenameCategoy(filename,category); //无类的函数专用：记录解析结果来自哪个文件,并指定类的种群。
		//clazz.setParsingFilename(parsingFilename); //记录解析结果来自哪个文件。
		clazz.addMethod(topLevelMethod);
	    //如果某条语句使用了类的属性，相应地增加复杂度。某些函数实际是类的方法，类在头文件声明。
	    //clazz.adjustComplexMetricWithAttribute();
		//createOrObtainClass里已经添加了此类，不要重复添加。
	}
	
	/**  解析C++类或结构
	 * @param simpDecl
	 * @throws DOMException
	 */
	private void processCppClassStruct(CPPASTSimpleDeclaration simpDecl) throws DOMException
	{
		IASTDeclSpecifier declSpecifier = simpDecl.getDeclSpecifier();
		//这里不用判断declSpecifier是否CPPASTCompositeTypeSpecifier类型的实例，调用前已经确认。
		CPPASTCompositeTypeSpecifier compTypeSpecifier = (CPPASTCompositeTypeSpecifier)declSpecifier;
		//构造类，并填充类的所有属性和方法。 
		ClassContext clazz = new ClassContext();
		//解决头文件，主文件不同的问题。
		String curParsingFile = ProjectContext.getFilenameFromPathFile(simpDecl.getContainingFilename());
		clazz.setParsingFilename(curParsingFile); //记录解析结果来自哪个文件。
		//clazz.setParsingFilename(parsingFilename); //记录解析结果来自哪个文件。
		if( curParsingFile.contentEquals(parsingFilename) )
		{
			//头文件的话，就不会计算下面的步骤，其起始行和结束行依然为0。
			IASTFileLocation  fileLocation = simpDecl.getFileLocation();
			//获得起始结束行号
			int startLine = fileLocation.getStartingLineNumber();
			int endLine = fileLocation.getEndingLineNumber();
			clazz.setStartLine(startLine);
			clazz.setEndLine(endLine);
		}
		
		IASTName astName = compTypeSpecifier.getName();
		String myClassName = astName.toString();//类名。
		/*C++有匿名struct或者匿名类，不记录这些.
		 * 注意：这些匿名类或结构会导致adjustNestedComplexMetricWithAttribute死循环。
		 * 如果是内嵌类或结构，取其parent.*/
		if( myClassName.isEmpty() )
			return;
		clazz.setName(myClassName);
		
		//设置嵌套标志。
		IASTNode parentNode = simpDecl.getParent();
		String[] parentName = new String[1];
		boolean isNestClass = isCppClassStruct(parentNode,parentName);
		if( isNestClass )
			clazz.setParentNode(parentName[0]); //父类名及嵌套标志

		//填充属性。
		IBinding binding = astName.resolveBinding();
		if ( binding instanceof ICPPClassType )
		{
			ICPPClassType cppClass = (ICPPClassType)binding;
			clazz.setCategory(10);//C++普通类
			/*  ICPPField[] getDeclaredFields() 
			 * Returns a list of ICPPField objects representing fields declared in this class. 
			 * It does not include fields inherited from base classes. 
			 */
			ICPPField[] cppFields = cppClass.getDeclaredFields();
			for( ICPPField field : cppFields)
				clazz.addAttribueName(field.getName());
		}
		else
		{//dnn.cpp 里面有形如XX::yy的类，类型为ProblemBinding不是ICPPClassType，另外处理。
			clazz.setCategory(11);// 形如XX::yy的类，类名带限定符号
			//用 visit的方法获取其属性。
			CppCompositeTypeVisitor ctv = new CppCompositeTypeVisitor();
			simpDecl.accept(ctv);
			List<String> attributes = ctv.getAttributes();
			for( String item : attributes )
				clazz.addAttribueName(item);
		}
		//如果是类的可执行语句的话，要先判断，是否在头文件内。
		if( curParsingFile.contentEquals(parsingFilename) )
		{//不是头文件内的可执行语句，可以放到解析文件的ClassContext中。
		    //找出该类的所有方法。
		    CppMethodVisitor cmVisitor = new CppMethodVisitor(myClassName);
		    simpDecl.accept(cmVisitor);
		    //为类带赋值的属性声明语句建立了特殊方法，将它加入进来。
		    cmVisitor.addMethodByFieldDeclaration();
		    clazz.setMethods(cmVisitor.getMethodList());
		    //如果某条语句使用了类的属性，相应地增加复杂度。
		    //clazz.adjustComplexMetricWithAttribute();
		}
	    //将类添加到列表
		clazzList.add(clazz);
	}
	
	
	/**  该节点是C++类或结构，并且在本文件内定义。
	 * @param cppNode
	 * @return
	 */
	private boolean isCppClassStruct(IASTNode cppNode,String[] parentName)
	{
		if ( cppNode instanceof CPPASTCompositeTypeSpecifier 
				&& cppNode.isPartOfTranslationUnitFile() )
		{
			CPPASTCompositeTypeSpecifier compTypeSpecifier = (CPPASTCompositeTypeSpecifier)cppNode;
			IASTName astName = compTypeSpecifier.getName();
			IBinding binding = astName.resolveBinding();
			if ( binding instanceof ICPPClassType )
			{
				ICPPClassType cppClass = (ICPPClassType)binding;
				parentName[0] = cppClass.getName();
				return true;
			}//end of if binding 
		}//end of if cppNode....
		return false;
	}
	
  //返回类的列表结果
  	public List<ClassContext> getClazzList() {
  		return clazzList;
  	}
  	
  	/**  解析全局变量，文件顶层的变量声明语句。
	 * @param simpDecl
	 * @throws DOMException
	 */
	private void processTopDeclaration(CPPASTSimpleDeclaration simpDecl) throws DOMException
	{
		IASTDeclSpecifier iasdSpeci = simpDecl.getDeclSpecifier();
		if( iasdSpeci.getStorageClass()==IASTDeclSpecifier.sc_typedef )
			return;  //过滤typedef

		ASTNodeProperty  nodeProperty = simpDecl.getPropertyInParent();
		if( nodeProperty!=IASTTranslationUnit.OWNED_DECLARATION ) //是否声明语句。
			return; 
		IASTDeclarator[] astDecltors = simpDecl.getDeclarators();
		//该语句是可执行语句吗，带=的认为是。 
		boolean isExecute = false;
		List<String> declatorLst = new ArrayList<String>();//记录全局变量。
		//每个标识符都当做全局变量，虚拟类的属性。
		for( IASTDeclarator declar :astDecltors )
		{
			if( 	declar instanceof  ICPPASTFunctionDeclarator     ||
					declar instanceof IASTAmbiguousDeclarator )
				continue;  //避免函数名作为全局变量。
			//  ICPPASTArrayDeclarator,  ICPPASTFieldDeclarator

			String varname = declar.getName().toString(); //标识符名称
			topDeclartion.addAttribueName(varname); //函数体外的语句，作为虚拟TopDeclartionCpp类的属性。
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
		CppFragmentVisitor fragmentVisitor= new CppFragmentVisitor();
		simpDecl.accept(fragmentVisitor);
		//使用CppFragmentVisitor后，不用类似函数的方法，而是：将getDeclarators获得的符号记录为标识符，
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
	 * 从虚拟TopDeclartionCpp类的方法列表中查找，是否存在专为收集函数体外可执行语句设置的方法，
	 * 如果没有，则创建一个，并给之命名，加入方法队列。
	 * 如果有，则直接返回该方法。
	 */
	private MethodContext createOrObtainSpecialMethod()
	{
		MethodContext methodContext = null;
		for( MethodContext cCtxt: topDeclartion.getMethods() )
		{
			if( cCtxt.isGlobalVariableOfCpp() )
			{
				methodContext = cCtxt;
				break;
			}
		}
		if( methodContext==null )
		{
			methodContext = new MethodContext();
			methodContext.setGlobalVariableOfCpp();
		    //将该方法添加到类的方法列表
			topDeclartion.addMethod(methodContext);
			//将该类加入类的列表。
			clazzList.add(topDeclartion);
		}	
		return methodContext;
	}
}

   