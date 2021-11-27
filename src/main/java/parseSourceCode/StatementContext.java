/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import softComplexMetric.StatementFeatureStruct;
import vfl.VFLSpectrumStruct;
import vfl.VariableInStatement;

/**
 * @author Administrator
 *
 */
public class StatementContext extends AbstractSourceContext{
	public final static int LoopStyle = 1; //循环语句，
	public final static int IfStyle = 2; //条件语句，
	public final static int ReturnStyle = 3; //return语句，
	public final static int StatementRest = 4; //其它语句，
	
	/*语句类型，以后考虑嵌套问题。*/
	private boolean bLoop; //循环语句，包括for, while , do while, foreach, lamada
	private boolean bCondition; //条件语句，包括if, else if, switch,catch(...), ? : (三元表达式),try,case,assert, 
	private boolean bReturn; //return语句
	private boolean bOtherStatement;//其它语句，包括 throw, 赋值，变量声明，goto,break XXX, continue XXX,....
	
	/*操作符号形成的软件复杂度*/
	private List<String> logicOperaters; //该语句包含的逻辑操作符， && || !，允许重复。
	private List<String> otherOperaters; //该语句包含的其它操作符， 除逻辑操作符外的，允许重复。

	/*函数调用形成的软件复杂度*/
	private List<String> funcCalls; //该语句包含的函数调用，存储函数（方法）名称，不允许重复。
	
	/*用于计算变量（顶层局部变量、参数、属性）的SBFL特征
	 * 注意，simpleNames里面不允许有重复的名称，这样，顶层局部变量》参数》属性*/
	private List<String> simpleNames; //记录该语句的所有标识符。排除保留字、函数名、类名、方法名。
	/*
	 *属性， 局部变量，参数变量，计算结束后，会从simpleNames删除。
	 *所以，一般情况下，simpleNames为空。
	 *但是，在for，while等语句里面，可以定义内部变量，这些变量不能影响复杂度，最后会留在simpleNames里。
	 *还有，如果局部变量的数据类型并非原子型，在调用它的方法时，会重复统计软件复杂度特征，因为变量名会留在simpleNames里。
	 *     如： 有局部变量SimpleSample ssm;   ssm.get();
	 *     get()调用有特征：函数调用，而ssm是局部变量有相关的局部变量特征值；
	 *     因为在统计完函数get的特征后，名称ssm会留在simpleNames里。
	 */
	//特别注意，没有地方设置sfsFeature的lineno
	private StatementFeatureStruct sfsFeature; //代码复杂度特征
	
	public StatementContext()
	{
		super.setEndLine(0);
		super.setStartLine(0);
		bLoop = false;
		bCondition = false;
		bReturn = false;
		bOtherStatement = false;
		logicOperaters = new ArrayList<>();
		otherOperaters = new ArrayList<>();
		funcCalls = new ArrayList<>();
		simpleNames = new ArrayList<>();
		sfsFeature = new StatementFeatureStruct(); //代码复杂度特征
	}
	
	//由一个数1,2,3,4决定语句的类型。
	public void setStatementStyle(int style)
	{
		switch( style )
		{
		case LoopStyle:
			bLoop = true;   //循环
			bCondition = false;
			bReturn = false;
			bOtherStatement = false;
			break;
		case IfStyle:
			bCondition = true; //条件
			bLoop = false;
			bReturn = false;
			bOtherStatement = false;
			break;
		case ReturnStyle:
			bReturn = true; //return
			bLoop = false;
			bCondition = false;
			bOtherStatement = false;
			break;
		case StatementRest: 
			bOtherStatement = true; //其它类型
			bLoop = false;
			bCondition = false;
			bReturn = false;
			break;
		default: //出错！
			bLoop = false;
			bCondition = false;
			bReturn = false;
			bOtherStatement = false;
			break;
		}
	}
	
	/** 当前语句肯定包含行号。
	 * @param lineno 行号
	 * @return true:包含行号  false:不包含行号
	 */
	public boolean haveBe(int lineno)
	{
		int startLine = getStartLine();
		int endLine = getEndLine();
		if( (lineno>=startLine) && (lineno<=endLine) )
			return true;
		else
			return false;
	}
	
	//循环语句，包括for, while , do while, foreach
	public boolean isLoop() {
		return bLoop;
	}

	public void setLoop(boolean bLoop) {
		this.bLoop = bLoop;
	}

	//条件语句，包括if, else if, switch,catch(...), ? : (三元表达式),
	public boolean isCondition() {
		return bCondition;
	}

	public void setCondition(boolean bCondition) {
		this.bCondition = bCondition;
	}

	//return语句
	public boolean isReturn() {
		return bReturn;
	}

	public void setReturn(boolean bReturn) {
		this.bReturn = bReturn;
	}

	//其它语句，包括 goto,break XXX, continue XXX, throw, ....
	public boolean isOtherStatement() {
		return bOtherStatement;
	}

	public void setOtherStatement(boolean bOtherStatement) {
		this.bOtherStatement = bOtherStatement;
	}

	//该语句包含的逻辑操作符， && || !，允许重复。
	public List<String> getLogicOperaters() {
		return logicOperaters;
	}

	public void setLogicOperaters(List<String> logicOperaters) {
		this.logicOperaters = logicOperaters;
	}

	//该语句包含的其它操作符， 除逻辑操作符外的，允许重复。
	public List<String> getOtherOperaters() {
		return otherOperaters;
	}

	public void setOtherOperaters(List<String> otherOperaters) {
		this.otherOperaters = otherOperaters;
	}

	//该语句包含的函数调用，存储函数（方法）名称，不允许重复。
	public List<String> getFuncCalls() {
		return funcCalls;
	}

	public void setFuncCalls(List<String> funcCalls) {
		this.funcCalls = funcCalls;
	}

	//记录该语句的所有标识符。排除保留字、函数名、类名、方法名。
	public List<String> getSimpleNames() {
		return simpleNames;
	}


	public void setSimpleNames(List<String> simpleNames) {
		this.simpleNames = simpleNames;
	}

	//代码复杂度特征
	public StatementFeatureStruct getSfsFeature() {
		return sfsFeature;
	}

	/** for语句里 for单独一行，while语句里 while单独一行 
	 * 这种情况下，for(while,if, foreach,....)所在的行号不是整个语句的起始行号，调用此方法后，可解决整个问题。
	 * @param lineno
	 */
	public void enlargeStartLineno(int lineno)
	{
		if( getStartLine()>lineno )
			setStartLine(lineno);
	}
	

	/** 这是一条复杂的语句，包含多个部分，将语句扩展，以便加入新的内容sContext
	 * 扩展内容包括： 开始行扩展，结束行扩展，认知复杂度累加，simpleNames累加。
	 * @param sContext 是一条语句的一部分，加入进来
	 */
	public void enlargeToComplexStatement(StatementContext sContext)
	{
		//起始行，结束行扩展
		int scstart = sContext.getStartLine();
		int scend = sContext.getEndLine();
		if( getStartLine()==0 || getEndLine()==0 )
		{ //都等于0，说明该语句还未读AST。
			setStartLine(scstart);
			setEndLine(scend);
			bLoop = sContext.isLoop();
			bCondition = sContext.isCondition();
			bReturn = sContext.isReturn();
			bOtherStatement = sContext.isOtherStatement();
			logicOperaters = sContext.getLogicOperaters();
			otherOperaters = sContext.getOtherOperaters();
			funcCalls = sContext.getFuncCalls();
			simpleNames = sContext.getSimpleNames();
		}
		else
		{
			//起始行扩展，找小的行号
			if( getStartLine()>scstart )
				setStartLine(scstart);
			//结束行扩展，找大的行号
			if( getEndLine()<scend )
				setEndLine(scend);
			//下面四个变量，特别注意，暂时不允许嵌套，则它们的值并不改变，是跨行语句的第一行代码的属性值。
			//bLoop = ;
			//bCondition = ;
			//bReturn = ;
			//bOtherStatement = ;
			//逻辑操作符 和 其它操作符 ，允许重复
			logicOperaters.addAll(sContext.getLogicOperaters());
			otherOperaters.addAll(sContext.getOtherOperaters());
			//函数名称 , 不允许重复
			List<String> funcName =  sContext.getFuncCalls();
			for( String item: funcName)
				if( !funcCalls.contains(item) )
					funcCalls.add(item);
			
			//simpleNames累加，并不重复。 
			List<String> simpleOfSC = sContext.getSimpleNames();
			for( String item : simpleOfSC)
				if( !simpleNames.contains(item) )
					simpleNames.add(item);
		}//end of else
	}
	
	/*将制定的宏定义，加入语句的funcCalls；将来添加函数调用对应的软件复杂度。
	 * 不区分对象形式的宏定义PASTObjectMacro和函数形式的宏定义PASTFunctionMacro,
	*/
	public void addMacroDefinitionName(String macroName)
	{
		if( !funcCalls.contains(macroName) )
			funcCalls.add(macroName);
	}
	
	/** 统计seek中与字符串在source出现的个数
	 * @param seeking 属性、全局变量、参数或者局部变量
	 * @param snameStatement  语句的标识符
	 * @return
	 */
	private int statisticsAppearToken(List<String> seeking, List<String> snameStatement )
	{
		int total = 0;
		for( String item : seeking )
		{
			for( String sname: snameStatement )
			{
				if( sname.contentEquals(item) )
				{
					total++;
					break;
				}
			}//end of for( String sname: simpleNames ) 
		}
		return total;
	}
	
	/**
	 * @param cContext
	 */
	
	/**把语句特征，容易计算的部分，先计算好，填充到StatementFeatureStruct sfsFeature中。
	 * @param attributes 所在类的属性
	 * @param parameters 所在方法的参数
	 * @param localVariables 所在方法的局部变量
	 */
	public void fillEasyFeature(List<String> attributes,List<String> parameters,List<String> localVariables)
	{
		//将起始行号作为它sfsFeature的行号合理吗？
		sfsFeature.setLineno(this.getStartLine());
		//统计该语句内出现的属性个数、参数个数、局部变量个数
		//没有考虑属性和 参数、顶层局部变量同名的问题。@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		int n_attr = statisticsAppearToken(attributes,simpleNames); //属性个数
		int n_para = statisticsAppearToken(parameters,simpleNames); //参数个数
		int n_vari = statisticsAppearToken(localVariables,simpleNames); //局部变量个数
		sfsFeature.setAttr_global(n_attr);
		sfsFeature.setParameter(n_para);
		sfsFeature.setLocalvar(n_vari);
		//填充语句的类型
		sfsFeature.setIsloop(bLoop?(byte)1:(byte)0);
		sfsFeature.setIsif(bCondition?(byte)1:(byte)0);
		sfsFeature.setIsreturn(bReturn?(byte)1:(byte)0);
		sfsFeature.setIsother(bOtherStatement?(byte)1:(byte)0);
		//填充逻辑运算符个数、其它运算符个数和函数调用次数。
		List<String> dinLogic = logicOperaters.stream().distinct().collect(Collectors.toList());
		sfsFeature.setOplogic(dinLogic.size());
		List<String> dinRest = otherOperaters.stream().distinct().collect(Collectors.toList());
		sfsFeature.setOpalgor(dinRest.size());
		sfsFeature.setFuncall(funcCalls.size());
	}
	
	//逐一检查方法内的语句，如果使用了局部变量，每使用一个（相同变量多次使用，视为一个），增加复杂度1.
	//methodVariable 方法内的局部变量。
	/*public void adjustComplexMetricWithVariable(String methodVariable)
	{
		for( Iterator<String> iterator = simpleNames.iterator(); iterator.hasNext();)
		{
			String snames = iterator.next();
			if( snames.contentEquals(methodVariable) )
			{
				iterator.remove();
				break;
			}//end of if
		}//end of for...
	}*/
	
	
	
	/*逐一检查方法内的语句，如果使用了宏定义，每使用一个（相同属性多次使用，视为一个），增加一个函数（方法）名称.
	 *macroDefine 宏定义名。
	 * 不区分对象形式的宏定义PASTObjectMacro和函数形式的宏定义PASTFunctionMacro,
	 */
	public void adjustComplexMetricWithMacroDefinition(List<String> macroDefines)
	{
		for( String mdef:macroDefines )
		{
			if( simpleNames.contains(mdef) )
			{
				if( !funcCalls.contains(mdef) )
					funcCalls.add(mdef);
				simpleNames.remove(mdef);
			}
		}//end of for...
	}
	
	/**找出出现过variables内变量名称的所有语句，组成VariableInStatement结构
	 * @param variables
	 * @return
	 */
	public List<VariableInStatement> variableAppearStatements(List<String>  variables)
	 {
		 List<VariableInStatement> varsInStatement = new ArrayList<>();
		 for( String vari : variables )
		 {
			 for ( String sname: simpleNames )
			 {
				 if( !sname.contentEquals(vari) )
					 continue;
				 //出现了variables内变量名称
				 VariableInStatement vis = new VariableInStatement(sname,getStartLine(),getEndLine());
				 varsInStatement.add(vis);
				 break;//simpleNames找到，退出此层循环。
			 }
		 }
		 return varsInStatement;
	 }
	
	/**把 语句特征，VFL部分，先计算好，填充到StatementFeatureStruct中。
	 * @param vflSpecta 属性、参数、顶层局部变量的VFL程序谱
	 */
	public void fillVFLFearture(List<VFLSpectrumStruct> vflSpecta)
	{
		//注意：以下四个值属于同一个变量，并非单独的取值
		float maxCovfail = 0; //Aef/F
		float maxCovpass = 0; //Aep/P
		float maxNCcovok = 0;  //Anp/(Anf+Anp)
		float maxNcovbug = 0; //Anf/(Anf+Anp)

		for ( String sname: simpleNames )
		{
			for( VFLSpectrumStruct vss: vflSpecta )
			{
				if( !sname.contentEquals(vss.getVariable()) )
					continue;
				//属性、参数、顶层局部变量出现在该语句内，检查它的VFL程序谱值
				if( vss.largeSuspicious(maxCovfail, maxCovpass, maxNCcovok, maxNcovbug) )
				{ //maxCovfail, maxCovpass, maxNCcovok, maxNcovbug四元组的可疑度比vss要小。
					maxCovfail = vss.getCovfail();
					maxCovpass = vss.getCovpass();
					maxNCcovok = vss.getNcovok();
					maxNcovbug = vss.getNcovbug();
				}
				break; //vflSpecta里存储的找到了，退出此层循环。
			}//end of for( VFLSpectrumStruct vss: vflSpecta )
		}//end of for ( String sname: simpleNames )
		//设置该语句的VFL特征
		sfsFeature.setCovfail(maxCovfail);
		sfsFeature.setCovpass(maxCovpass);
		sfsFeature.setNcovbug(maxNcovbug);
		sfsFeature.setNcovok(maxNCcovok);
	}
	
	//显示所有信息
	@Override
	public void showMe()
	{
		System.out.print("        ("+getStartLine()+","+getEndLine()+")=");
		if( bLoop )
			System.out.print(" loop ");
		if( bCondition )
			System.out.print(" condition ");
		if( bReturn )
			System.out.print(" return ");
		if( bOtherStatement )
			System.out.print(" rest ");
		
		System.out.print("    ");
		//打印逻辑操作符列表
		for( String para : logicOperaters )
			System.out.print(para+",  ");
		System.out.print("        ");
		//打印 除逻辑操作符外的 列表
		for( String para : otherOperaters )
			System.out.print(para+",  ");
		System.out.print("        ");
		//打印 函数（方法）名称 列表
		for( String para : funcCalls )
			System.out.print(para+",  ");
		System.out.println("  .");
		//打印simpleNames列表
		System.out.print("               ");
		for( String para : simpleNames )
			System.out.print(para+",  ");
		System.out.println("=> "+simpleNames.size());
	}
}
