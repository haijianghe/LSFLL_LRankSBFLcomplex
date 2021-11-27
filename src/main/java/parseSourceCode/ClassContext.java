/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import affiliated.SpectrumStruct;
import softComplexMetric.StatementFeatureStruct;
import vfl.VFLSpectraManagement;
import vfl.VariableInStatement;

/** 类可以由一级嵌套类，但不会将任何嵌套类的属性和方法放入其类中。
 * @author Administrator
 *
 */
/**
 * @author Administrator
 *
 *1, 内部类的属性表已经加入其所有上代类的属性；
 *2，C++的全局变量已经添加到所有类的属性表；
 *3，C程序的全局变量就是TopDeclartionCpp类的属性。
 */
public class ClassContext extends AbstractSourceContext {
	private String name;  //类名或接口名。
	 /*C语言和C++，头文件和主文件可能分开，ProjectContext的String的文件名是指主文件,而parsingFilename则是解析时遇到的第一个文件名；
	 对Java来说，则不存在此问题。ProjectContext的String的文件名和parsingFilename是同一个东西，此时冗余。*/
	String parsingFilename; //解析结果来自于哪个文件，不带目录信息。此数据对Java,c冗余，对C++则不一定冗余。
	/*
	 * 注意： attributes将不会列入内部类、嵌套类、匿名类等inner class的属性。
	 */
	private List<String> attributes;    // 该类的属性名列表。
	private String parentName;    // 若nesting=true,则为该类的嵌套类类名。
	private boolean nesting; //该类是否被嵌套，true,则该类是parent的子类；false，该类在文件中是普通类（非嵌套类）。
	private boolean isInterface; //true:是接口，暂时不考虑接口的错误语句定位。
	private boolean willRemove; //删除二级嵌套类时，没有好算法，设置一个标志位。
	/*
	 * 注意： methods将不会列入内部类、嵌套类、匿名类等inner class的方法。
	 */
	private List<MethodContext> methods; //该类的所有方法。
	/*
	 * category=1,Java类或接口； 
	 * category=10,C++普通类，11, 形如XX::yy的类，类名带限定符号；
	 *          12,C++里的普通函数(顶层函数，不属于任何类); 
	 *          13,缺少头文件的类； 
	 *          14，C++全局变量，文件顶层变量声明语句，组成的TopDeclartionCpp类。
	 *          15，头文件和主文件不在一起，前者由parsingFilename指定，后者由ProjectContext的String指定。
	 *          16,C++保留。
	 * category=20, C 程序的虚拟类。
	 */
	private int category; //类的类型
	//以下定义特殊的类名。 category=15，则有类名（在头文件）
	public final static String TopLevelFunction = "TopLevelFunction";//C++: category=12，13
	public final static String VirtualClassOfC = "VirtualClassOfC";//C: category=20
	public final static String TopDeclartionCpp = "TopDeclartionCpp";//C++: category=14
	
	public ClassContext()
	{
		super.setEndLine(0);
		super.setStartLine(0);
		name = "";
		parsingFilename = "";
		attributes = new ArrayList<>();
		methods = new ArrayList<>();
		parentName = "";
		nesting = false;
		isInterface = false;
		willRemove = false;
		category = 1; //缺省为Java类或接口；
	}

	//类名或接口名。
	public String getName() {
		return name;
	}

	//指定类名或接口名。
	public void setName(String name) {
		this.name = name;
	}

	//c语言程序的虚拟类。
	public void setVirtualNameCategory()
	{
		name = VirtualClassOfC;
		category = 20;
	}
	
	//c++语言程序的TopDeclartionCpp类的类名和类型。
	public void setTopDeclartionNameCategory()
	{
		name = TopDeclartionCpp;
		category = 14;
	}

	//该类是特殊类吗？（C++程序，收集所有C++全局变量，文件顶层变量声明语句）
	public boolean isTopDeclartionClass()
	{
		if( category==14 )
			return true;
		else
			return false;
	}
	
	//解析结果来自于哪个文件，不带目录信息。
	public String getParsingFilename() {
		return parsingFilename;
	}

	public void setParsingFilename(String parsingFilename) {
		this.parsingFilename = parsingFilename;
	}

	 /*无类的函数专用：记录解析结果来自哪个文件,并指定类的种群。
	  * filename :不带目录信息。
	  */
	public void setFuncParsingFilenameCategoy(String filename, int kind)
	{
		if( category==10 || category==15 )//该函数是类的方法，不过，类的声明在parsingFilename指定的文件中。此时，parsingFilename和filename不相同。
			category = 15;//15，头文件和主文件不在一起，前者由parsingFilename指定，后者由ProjectContext的String指定。
		else 
		{ //不是10 & 15（初始值1，代表Java方法，10代表是C++普通类,15 声明定义分离的类），说明这是函数，不是类的方法，或者没有解析过类的头文件。
			this.parsingFilename = filename;
			this.category = kind;
		}
	}
	
	//true:是接口；false ： 类。
	public boolean isInterface() {
		return isInterface;
	}

	//指定类型；true:是接口；false ： 类。
	public void setInterface(boolean isInterface) {
		this.isInterface = isInterface;
	}
	
	//如果是嵌套类，将标示其外部类名及nesting嵌套类标志。
	public void setParentNode(String outtername)
	{
		nesting = true;
		parentName = outtername;
	}

	//获取outter class类名，isNesting=true，此值才有意义。
	public String getParentName() {
		return parentName;
	}

	//=true，说明是内部类;false,是top-level类。
	public boolean isNesting() {
		return nesting;
	}

	//属性名列表，添加一个属性名。
	public void addAttribueName(String attributeName)
	{
		attributes.add(attributeName);
	}
	
	// 该类的属性名列表。
	public List<String> getAttributes() {
		return attributes;
	}

	//该类的所有方法。
	public List<MethodContext> getMethods() {
		return methods;
	}

	public void setMethods(List<MethodContext> methods) {
		this.methods = methods;
	}

	//方法列表，添加一个方法。
	public void addMethod(MethodContext mc)
	{
		methods.add(mc);
	}

	//删除二级嵌套类时，没有好算法，设置一个标志位。
	public boolean isWillRemove() {
		return willRemove;
	}

	//=true，将来会被删除。
	public void setWillRemove(boolean willRemove) {
		this.willRemove = willRemove;
	}
	
	/*
	 * category=1,Java类或接口； 
	 * category=2,C++普通类，3,C++里的普通函数(不属于任何类)；4，C++保留。
	 * category=5, C 程序的普通函数。
	 */
	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	/** 将ccNode内的所有方法并入此类中，并入的方法属性除类型外都不变，类型变为5.
	 * @param ccNode  1，此类是文件的最顶层类；2，ccNode属于此类内部类的内部类。
	 */
	public void mergeInner2Class(ClassContext ccNode)
	{
		List<MethodContext> innerMethods = ccNode.getMethods();
		for( MethodContext mc: innerMethods )
		{
			mc.setInner2Type();
			methods.add(mc);
		}
	}
	
	//逐一检查方法内的语句，如果使用了类的属性，相应地增加复杂度.
	/*public void adjustComplexMetricWithAttribute()
	{
		for( MethodContext method :  methods )
			method.adjustComplexMetricWithAttribute(attributes);
	}*/
	
	/*所有上代类的属性加到此类的属性表。
	 * outerAttributes外部类的属性列表。
	 */
	public void addOutterAttribute(List<String> outerAttributes)
	{
		for( String attr :  outerAttributes )
		{
			if( !attributes.contains(attr) )
				attributes.add(attr);
		}
	}
	
	
	/**把语句特征，容易计算的部分，先计算好，填充到StatementFeatureStruct中。
	 * @param attributes 所在类的属性
	 */
	public void fillEasyFeature()
	{
		for( MethodContext method :  methods )
			method.fillEasyFeature(attributes);
	}
	
	/*
	  * C++程序的虚拟TopDeclartionCpp类，在解析时，将全局变量变为该类的属性
	  * 当检查全局变量对软件复杂度影响时，没有好的做法，全局变量添加到所有类，作为所有类的属性
	  * 对所有方法的所有语句增加全局变量（属性）带来的软件复杂度，全局变量定义语句也不例外。
	  */
	public void cppAdjustAttributeWithGlobalVariable(List<String> globalVariables)
	{
		for( String gvar:  globalVariables )
		{
			if( !attributes.contains(gvar) ) //属性名和全局变量不能重复。
				attributes.add(gvar);
		}
	}
	
	/*逐一检查方法内的语句，如果使用了宏定义，相应地增加复杂度.
	 * outerAttributes外部类的属性列表。
	 */
	public void adjustComplexMetricWithMacroDefinition(List<String> macroDefines)
	{
		for( MethodContext method :  methods )
			method.adjustComplexMetricWithMacroDefinition(macroDefines);
	}
	
	/**由行号，找出对应的软件代码静态复杂度
	 * @param lineno  行号
	 * @return 复杂度值，
	 * 	          在类中，已经找到，行号是lineno值>0。
	 *         肯定在类中，未找到，则返回StatementFeatureStruct对象的行号设置为0. 后续不用再继续找
	 *          肯定不在类中，  则返回StatementFeatureStruct对象的行号设置为-1.
	 */
	public StatementFeatureStruct getCodeStaticComplexMetricByFileLineno(int lineno)
	{
		StatementFeatureStruct fsStatement = new StatementFeatureStruct();
		int startLineno = this.getStartLine();
		int endLineno = this.getEndLine();
		if( (endLineno!=0) && (startLineno!=0) )
		{ //endLineno和startLineno都初始化为0，category=12,13,14，20这些类型，都无法读起始行和结束行。
			if( lineno<startLineno || lineno>endLineno )
				return fsStatement; //该行代码不在此类中。行号-1
			fsStatement.setLineno(0); //肯定在此类中，后面ProjectContext会据此判断在此类中，只是找不到。
		}
		//如果startLineno==0 || endLineno==0 ，则fsStatement的行号是-1；后面ProjectContext会据此判断lineno不在此类中
		for( MethodContext mCtx : methods )
		{
			StatementFeatureStruct sfs = mCtx.getCodeStaticComplexMetricByFileLineno(lineno);
			int locLineno = sfs.getLineno();//-1 表示不在方法中；0表示在方法中，未找到；>0找到。
			if( locLineno>0 ) 
			{//找到该行号的软件代码静态复杂度
				fsStatement = sfs;
				break;
			}
			else if ( locLineno==0 ) 
			{ //表示在方法中，未找到。肯定在此类中，但是找不到具体语句。可能语句是{, }等
				break;
			}
			else //不在方法mCtx中，继续在当前类查找。
				continue;
		}
		return fsStatement;
	}
	
	/**把类的所有语句特征，VFL部分，先计算好，填充到StatementFeatureStruct中。
	 *   VFL: variable-based fault localization
	 * @param lineCodes 每条语句的程序谱。
	 * @param passed 某文件对应的该版本的成功测试用例个数
	 * @param failed 该版本的未通过测试用例个数
	 */
	public void fillVFLFearture(List<SpectrumStruct> lineCodes,int passed,int failed)
	{
		//先将使用了属性的语句全部找出来，注意局部变量名和参数名会覆盖属性名称。
		List<VariableInStatement> varISlst = new ArrayList<>();
		for( MethodContext mCtx : methods )
		{
			List<VariableInStatement> varsInMethod = mCtx.attributeAppearStatements(attributes);
			varISlst.addAll(varsInMethod);
		}
		//计算所有的属性程序谱。
		VFLSpectraManagement vflsm = new VFLSpectraManagement();
		vflsm.computerSpectraVaiables(varISlst, lineCodes, passed, failed);
		//逐个计算方法的VFL部分。
		for( MethodContext mCtx : methods )
			mCtx.fillVFLFearture(vflsm.getVflSpecta(),lineCodes, passed, failed);
	}
	
	//显示所有信息
	@Override
	public void showMe()
	{
		if( nesting )
			System.out.println("Filename: "+parsingFilename+"  Class:= "+name+",  Category="+category+", isInterface="+isInterface+", nesting, parent= "+parentName);
		else
			System.out.println("Filename: "+parsingFilename+"  Class:= "+name+",  Category="+category+", isInterface="+isInterface);
		//打印属性名列表
		System.out.print("        ");
		for( String attrname :attributes )
			System.out.print(attrname+",");
		System.out.println("         attirbute total = "+attributes.size());
		super.showMe();
		//显示方法的数据。
		for ( MethodContext mctxt : methods )
			mctxt.showMe();
	}
}
