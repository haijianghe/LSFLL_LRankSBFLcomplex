/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import affiliated.SpectrumStruct;
import softComplexMetric.StatementFeatureStruct;
import vfl.VFLSpectraManagement;
import vfl.VFLSpectrumStruct;
import vfl.VariableInStatement;

/**
 * @author Administrator
 *
 */
public class MethodContext extends AbstractSourceContext {
	private String name;  //方法名。
	/*
	 * 注意： localVariables将列入内部类、嵌套类、匿名类等inner class的局部变量。
	 * 这样做，既简化问题，也符合实际需求。
	 */
	private List<String> localVariables;    // 该方法的所有局部变量。
	private List<String> parameters;    // 该方法的参数名列表。
	/*
	 * type 方法类型，1=构造函数，2=getter or setter, 3=Java/C++普通的直接方法（C的函数）。
	 *               4=Initializer, 类的初始化块，特殊处理，其方法名为class_initializer；
	 *               5,二级嵌套类的方法，计算CK等复杂度时特殊处理，
	 *               6，类的带赋值的属性声明语句。 class_FieldDeclaration
	 *               11,C++文件的顶层函数；
	 *               12，缺少头文件的类的方法，类似于C++文件的顶层函数(也可能有头文件，不过头文件、主文件分离)
	 *               13,C++全局变量，文件顶层变量声明语句，TopDeclartionCpp类的方法。
	 *               21,C程序的变量声明语句放在一个函数中。
	 */
	private int type; // 方法类型
	private List<StatementContext> statements; //该方法的所有语句。
	//以下定义特殊的方法名或函数名。
	public final static String GlobalVariableOfC = "GlobalVariableOfC";// C: type=21
	public final static String GlobalVariableOfCpp = "GlobalVariableOfCpp";//C++: type=13
	
	public MethodContext()
	{
		super.setEndLine(0);
		super.setStartLine(0);
		name = "";
		localVariables = new ArrayList<>();
		parameters = new ArrayList<>();
		type = -1;
		statements = new ArrayList<>();		
	}

	/** 将语句的数据填充到此方法中。
	 * @param statements
	 */
	public void fillStatementsToMethod(List<StatementContext> sContexts)
	{
		for( StatementContext cc : sContexts )
			statements.add(cc);
	}
	
	/** 为定位类的带赋值的属性声明语句，在方法的语句列表中添加单个的语句。
	 * @param stmtContext
	 */
	public void addOneStatement(StatementContext stmtContext)
	{
		statements.add(stmtContext);
	}
	
	//方法名。
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	 // 该方法的所有局部变量。
	public List<String> getLocalVariables() {
		return localVariables;
	}

	public void setLocalVariables(List<String> localVariables) {
		this.localVariables = localVariables;
	}

	//拷贝，注意与setLocalVariables的差别
	public void copyLocalVariables(List<String> varLst) {
		for( String var : varLst )
			localVariables.add(var);
	}

	// 该方法的参数名列表。
	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	//添加一个参数到参数列表。
	public void addParameter(String para)
	{
		parameters.add(para);
	}
	
	//方法类型，1=构造函数，2=getter or setter, 3=普通方法。
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	/** 设置为特殊方法。
	 * 该方法是特殊方法，只属于C程序，收集所有函数体外的语句。
	 */
	public void setGlobalVariableOfC()
	{
		name = GlobalVariableOfC;
		type = 21;
	}
	
	/** 设置为特殊方法，TopDeclartionCpp类的方法。
	 * 该方法是特殊方法，只属于C++程序，收集所有C++全局变量，文件顶层变量声明语句。
	 */
	public void setGlobalVariableOfCpp()
	{
		name = GlobalVariableOfCpp;
		type = 13;
	}
	
	//该方法是特殊方法吗？（只属于C程序，收集所有函数体外的语句。）
	public boolean isGlobalVariableOfC()
	{
		if( type==21 )
			return true;
		else
			return false;
	}
	
	//该方法是特殊方法吗？（C++程序，收集所有C++全局变量，文件顶层变量声明语句）
	public boolean isGlobalVariableOfCpp()
	{
		if( type==13 )
			return true;
		else
			return false;
	}
		
	//特殊方法，是最顶层类的内部类的内部类的方法。
	public void setInner2Type()
	{
		this.type = 5;
	}
	
	//该方法的所有语句。
	public List<StatementContext> getStatements() {
		return statements;
	}
	
	//该方法的语句条数。
	public int getNumberOfStatements()
	{
		return statements.size();
	}
	

	/**把语句特征，容易计算的部分，先计算好，填充到StatementFeatureStruct中。
	 * @param attributes 所在类的属性
	 */
	public void fillEasyFeature(List<String> attributes)
	{
		for( StatementContext sc : statements )
			sc.fillEasyFeature(attributes,parameters,localVariables);
	}
	
	
	//逐一检查方法内的语句，如果使用了宏定义，相应地增加复杂度.
	public void adjustComplexMetricWithMacroDefinition(List<String> macroDefines)
	{
		for( StatementContext sc : statements )
			sc.adjustComplexMetricWithMacroDefinition(macroDefines);
	}
	
	/** attr是否在参数或者顶层局部变量出现过
	 * @param attr
	 * @return true：出现过； false:未曾出现
	 */
	private boolean isParameterLocalVariable(String attr)
	{
		boolean have = false;
		//是否在参数表中出现过
		for( String para: parameters )
		{
			if( para.contentEquals(attr) )
			{
				have = true;
				break;
			}
		}
		if( have )
			return true;
		//是否在局部变量表中出现过
		for( String lvar: localVariables )
		{
			if( lvar.contentEquals(attr) )
			{
				have = true;
				break;
			}
		}
		return have;
	}
	
	 /** 找出方法内所有出现了属性的语句，组成VariableInStatement结构
	 * @param attributes 所属类的属性，包括C/C++的全局变量。
	 * @return 属性名，开始行号、结束行号的列表
	 */
	public List<VariableInStatement> attributeAppearStatements(List<String> attributes)
	 {
		 List<VariableInStatement> varsInMethod = new ArrayList<>();
		 //考虑到局部变量名和参数名会覆盖属性名称。先将attributes中与localVariables和parameters同名的删除。
		 List<String> variAttr = new ArrayList<>();
		 for( String attr: attributes )
		 {
			 if( !isParameterLocalVariable(attr) ) //attr没有在参数或者顶层局部变量出现过
				 variAttr.add(attr);
		 }
		 //找出出现过variAttr内属性的所有语句，组成VariableInStatement结构
		 for ( StatementContext stmt: statements )
		 {
			 List<VariableInStatement> varsInStatement = stmt.variableAppearStatements(variAttr);
			 varsInMethod.addAll(varsInStatement);
		 }
		 return varsInMethod;
	 }
	
	/**把方法的所有语句特征，VFL部分，先计算好，填充到StatementFeatureStruct中。
	 *   VFL: variable-based fault localization
	 * @param lineCodes 每条语句的程序谱。
	 * @param passed 某文件对应的该版本的成功测试用例个数
	 * @param failed 该版本的未通过测试用例个数
	 * @param vflSpecta 属性的VFL程序谱
	 */
	public void fillVFLFearture(List<VFLSpectrumStruct> vflSpecta,List<SpectrumStruct> lineCodes,int passed,int failed)
	{
		//考虑到局部变量名和参数名会覆盖属性名称。先将attributes中与localVariables和parameters同名的删除。
		List<VFLSpectrumStruct> vflssInMethod = new ArrayList<>();
		for( VFLSpectrumStruct vss : vflSpecta )
		{
			if( !isParameterLocalVariable(vss.getVariable()) ) //属性名称没有在参数或者顶层局部变量出现过
				vflssInMethod.add(vss);
		}
		//将localVariables和parameters合并成一个字符串，不用考虑它们同名的问题。这种情况，代码编译一般会出现问题。
		List<String> variParm = new ArrayList<>();
		variParm.addAll(localVariables);
		variParm.addAll(parameters);
		//先将使用了局部变量和参数的语句全部找出来
		List<VariableInStatement> varsBeMethod = new ArrayList<>();
		for ( StatementContext stmt: statements )
		{
			List<VariableInStatement> varsInStatement = stmt.variableAppearStatements(variParm);
			varsBeMethod.addAll(varsInStatement);
		}
		//计算所有的局部变量和参数的程序谱。
		VFLSpectraManagement vflsm = new VFLSpectraManagement();
		vflsm.computerSpectraVaiables(varsBeMethod, lineCodes, passed, failed);
		//将属性的 和 局部变量、参数的 VFL程序谱合并成一个。
		vflssInMethod.addAll(vflsm.getVflSpecta()); 
		//逐条计算语句的VFL部分。
		for ( StatementContext stmt: statements )
			stmt.fillVFLFearture(vflssInMethod);
	}
	
	/**由行号，找出对应的软件代码静态复杂度
	 * @param lineno  行号
	 * @return 复杂度值，
	 *           在方法中，已经找到，行号是lineno值>0，并非找到语句的startLineno or endLineno。
	 *         肯定在方法中，未找到，则返回StatementFeatureStruct对象的行号设置为0. 后续不用再继续找
	 *          肯定不在方法中，  则返回StatementFeatureStruct对象的行号设置为-1.
	 */
	public StatementFeatureStruct getCodeStaticComplexMetricByFileLineno(int lineno)
	{
		StatementFeatureStruct fsStatement = new StatementFeatureStruct();
		int startLineno = this.getStartLine();
		int endLineno = this.getEndLine();
		if( (endLineno!=0) && (startLineno!=0) )
		{ //endLineno和startLineno都初始化为0，type=6,13,21这些类型，都无法读起始行和结束行。
			if( lineno<startLineno || lineno>endLineno )
				return fsStatement;//该行代码不在此方法内。行号-1
		}
		List<StatementContext> finds=new ArrayList<>(); //包含行号lineno的所有StatementContext
		for( StatementContext stmtCtx : statements )
		{
			if( stmtCtx.haveBe(lineno) )
				finds.add(stmtCtx);
		}
		/*为避免两条或以上可执行语句写在同一行。
		 * 如： (49,51)=6  (51,51)=4 (51,51)=7  lineno = 49，取6; =50 取6; =51取7;
		 * 查找原则：找startLine-lineno最小值的语句，如果最近的语句有多条，取其中最大值作为复杂度值。
		 */
		int number = finds.size();
		if( number<=0 ) //在方法中，未找到。
		{
			if( startLineno==0 || endLineno==0 ) //相当于(endLineno!=0) && (startLineno!=0)的反面
				fsStatement.setLineno(-1);//行号<0，后面ClassContext会据此判断不在此方法中。
			else
				fsStatement.setLineno(0);//行号0，后面ClassContext会据此判断在此方法中，只是找不到。
		}
		else if ( number==1 )
		{//只有一个StatementContext
			fsStatement = finds.get(0).getSfsFeature();
			fsStatement.setLineno(lineno); //找寻的行号(参数)设为放回对象的值
		}
		else 
		{//有多个。那么只有起始行号= lineno的才符合要求。（可执行语句不可能相互嵌套。）
			int maxv = 0;
			fsStatement = finds.get(0).getSfsFeature(); //缺省情况，选择第一条语句为行代码的代表。
			for( StatementContext stCtx : finds )
			{
				int startLine = stCtx.getStartLine();
				if( lineno==startLine )
				{
					StatementFeatureStruct sfsTmp = stCtx.getSfsFeature();
					int cognv = sfsTmp.getComplexComparisonRule();
					if( cognv>maxv )
					{
						maxv = cognv;
						fsStatement = sfsTmp;
					}
				}//end of if...
			}//end of for...
			fsStatement.setLineno(lineno);//找寻的行号(参数)设为放回对象的值
		}
		return fsStatement;
	}
	
	//显示所有信息
	@Override
	public void showMe()
	{
		System.out.print("       Method:  "+name+", type=: "+type+"   parameter: ");
		//打印参数名列表
		for( String para : parameters )
			System.out.print(para+",  ");
		System.out.println(".  "+parameters.size());
		//打印局部变量名列表
		System.out.print("                Variable: ");
		for( String var : localVariables )
			System.out.print(var+",  ");
		System.out.println("."+localVariables.size());
		super.showMe();
		//打印所有语句数据。
		for( StatementContext sContext : statements )
			sContext.showMe();
	}
}
