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

/** �������һ��Ƕ���࣬�����Ὣ�κ�Ƕ��������Ժͷ������������С�
 * @author Administrator
 *
 */
/**
 * @author Administrator
 *
 *1, �ڲ�������Ա��Ѿ������������ϴ�������ԣ�
 *2��C++��ȫ�ֱ����Ѿ���ӵ�����������Ա�
 *3��C�����ȫ�ֱ�������TopDeclartionCpp������ԡ�
 */
public class ClassContext extends AbstractSourceContext {
	private String name;  //������ӿ�����
	 /*C���Ժ�C++��ͷ�ļ������ļ����ֿܷ���ProjectContext��String���ļ�����ָ���ļ�,��parsingFilename���ǽ���ʱ�����ĵ�һ���ļ�����
	 ��Java��˵���򲻴��ڴ����⡣ProjectContext��String���ļ�����parsingFilename��ͬһ����������ʱ���ࡣ*/
	String parsingFilename; //��������������ĸ��ļ�������Ŀ¼��Ϣ�������ݶ�Java,c���࣬��C++��һ�����ࡣ
	/*
	 * ע�⣺ attributes�����������ڲ��ࡢǶ���ࡢ�������inner class�����ԡ�
	 */
	private List<String> attributes;    // ������������б�
	private String parentName;    // ��nesting=true,��Ϊ�����Ƕ����������
	private boolean nesting; //�����Ƿ�Ƕ�ף�true,�������parent�����ࣻfalse���������ļ�������ͨ�ࣨ��Ƕ���ࣩ��
	private boolean isInterface; //true:�ǽӿڣ���ʱ�����ǽӿڵĴ�����䶨λ��
	private boolean willRemove; //ɾ������Ƕ����ʱ��û�к��㷨������һ����־λ��
	/*
	 * ע�⣺ methods�����������ڲ��ࡢǶ���ࡢ�������inner class�ķ�����
	 */
	private List<MethodContext> methods; //��������з�����
	/*
	 * category=1,Java���ӿڣ� 
	 * category=10,C++��ͨ�࣬11, ����XX::yy���࣬�������޶����ţ�
	 *          12,C++�����ͨ����(���㺯�����������κ���); 
	 *          13,ȱ��ͷ�ļ����ࣻ 
	 *          14��C++ȫ�ֱ������ļ��������������䣬��ɵ�TopDeclartionCpp�ࡣ
	 *          15��ͷ�ļ������ļ�����һ��ǰ����parsingFilenameָ����������ProjectContext��Stringָ����
	 *          16,C++������
	 * category=20, C ����������ࡣ
	 */
	private int category; //�������
	//���¶�������������� category=15��������������ͷ�ļ���
	public final static String TopLevelFunction = "TopLevelFunction";//C++: category=12��13
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
		category = 1; //ȱʡΪJava���ӿڣ�
	}

	//������ӿ�����
	public String getName() {
		return name;
	}

	//ָ��������ӿ�����
	public void setName(String name) {
		this.name = name;
	}

	//c���Գ���������ࡣ
	public void setVirtualNameCategory()
	{
		name = VirtualClassOfC;
		category = 20;
	}
	
	//c++���Գ����TopDeclartionCpp������������͡�
	public void setTopDeclartionNameCategory()
	{
		name = TopDeclartionCpp;
		category = 14;
	}

	//�������������𣿣�C++�����ռ�����C++ȫ�ֱ������ļ��������������䣩
	public boolean isTopDeclartionClass()
	{
		if( category==14 )
			return true;
		else
			return false;
	}
	
	//��������������ĸ��ļ�������Ŀ¼��Ϣ��
	public String getParsingFilename() {
		return parsingFilename;
	}

	public void setParsingFilename(String parsingFilename) {
		this.parsingFilename = parsingFilename;
	}

	 /*����ĺ���ר�ã���¼������������ĸ��ļ�,��ָ�������Ⱥ��
	  * filename :����Ŀ¼��Ϣ��
	  */
	public void setFuncParsingFilenameCategoy(String filename, int kind)
	{
		if( category==10 || category==15 )//�ú�������ķ��������������������parsingFilenameָ�����ļ��С���ʱ��parsingFilename��filename����ͬ��
			category = 15;//15��ͷ�ļ������ļ�����һ��ǰ����parsingFilenameָ����������ProjectContext��Stringָ����
		else 
		{ //����10 & 15����ʼֵ1������Java������10������C++��ͨ��,15 �������������ࣩ��˵�����Ǻ�����������ķ���������û�н��������ͷ�ļ���
			this.parsingFilename = filename;
			this.category = kind;
		}
	}
	
	//true:�ǽӿڣ�false �� �ࡣ
	public boolean isInterface() {
		return isInterface;
	}

	//ָ�����ͣ�true:�ǽӿڣ�false �� �ࡣ
	public void setInterface(boolean isInterface) {
		this.isInterface = isInterface;
	}
	
	//�����Ƕ���࣬����ʾ���ⲿ������nestingǶ�����־��
	public void setParentNode(String outtername)
	{
		nesting = true;
		parentName = outtername;
	}

	//��ȡoutter class������isNesting=true����ֵ�������塣
	public String getParentName() {
		return parentName;
	}

	//=true��˵�����ڲ���;false,��top-level�ࡣ
	public boolean isNesting() {
		return nesting;
	}

	//�������б����һ����������
	public void addAttribueName(String attributeName)
	{
		attributes.add(attributeName);
	}
	
	// ������������б�
	public List<String> getAttributes() {
		return attributes;
	}

	//��������з�����
	public List<MethodContext> getMethods() {
		return methods;
	}

	public void setMethods(List<MethodContext> methods) {
		this.methods = methods;
	}

	//�����б����һ��������
	public void addMethod(MethodContext mc)
	{
		methods.add(mc);
	}

	//ɾ������Ƕ����ʱ��û�к��㷨������һ����־λ��
	public boolean isWillRemove() {
		return willRemove;
	}

	//=true�������ᱻɾ����
	public void setWillRemove(boolean willRemove) {
		this.willRemove = willRemove;
	}
	
	/*
	 * category=1,Java���ӿڣ� 
	 * category=2,C++��ͨ�࣬3,C++�����ͨ����(�������κ���)��4��C++������
	 * category=5, C �������ͨ������
	 */
	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	/** ��ccNode�ڵ����з�����������У�����ķ������Գ������ⶼ���䣬���ͱ�Ϊ5.
	 * @param ccNode  1���������ļ�������ࣻ2��ccNode���ڴ����ڲ�����ڲ��ࡣ
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
	
	//��һ��鷽���ڵ���䣬���ʹ����������ԣ���Ӧ�����Ӹ��Ӷ�.
	/*public void adjustComplexMetricWithAttribute()
	{
		for( MethodContext method :  methods )
			method.adjustComplexMetricWithAttribute(attributes);
	}*/
	
	/*�����ϴ�������Լӵ���������Ա�
	 * outerAttributes�ⲿ��������б�
	 */
	public void addOutterAttribute(List<String> outerAttributes)
	{
		for( String attr :  outerAttributes )
		{
			if( !attributes.contains(attr) )
				attributes.add(attr);
		}
	}
	
	
	/**��������������׼���Ĳ��֣��ȼ���ã���䵽StatementFeatureStruct�С�
	 * @param attributes �����������
	 */
	public void fillEasyFeature()
	{
		for( MethodContext method :  methods )
			method.fillEasyFeature(attributes);
	}
	
	/*
	  * C++���������TopDeclartionCpp�࣬�ڽ���ʱ����ȫ�ֱ�����Ϊ���������
	  * �����ȫ�ֱ�����������Ӷ�Ӱ��ʱ��û�кõ�������ȫ�ֱ�����ӵ������࣬��Ϊ�����������
	  * �����з����������������ȫ�ֱ��������ԣ�������������Ӷȣ�ȫ�ֱ����������Ҳ�����⡣
	  */
	public void cppAdjustAttributeWithGlobalVariable(List<String> globalVariables)
	{
		for( String gvar:  globalVariables )
		{
			if( !attributes.contains(gvar) ) //��������ȫ�ֱ��������ظ���
				attributes.add(gvar);
		}
	}
	
	/*��һ��鷽���ڵ���䣬���ʹ���˺궨�壬��Ӧ�����Ӹ��Ӷ�.
	 * outerAttributes�ⲿ��������б�
	 */
	public void adjustComplexMetricWithMacroDefinition(List<String> macroDefines)
	{
		for( MethodContext method :  methods )
			method.adjustComplexMetricWithMacroDefinition(macroDefines);
	}
	
	/**���кţ��ҳ���Ӧ��������뾲̬���Ӷ�
	 * @param lineno  �к�
	 * @return ���Ӷ�ֵ��
	 * 	          �����У��Ѿ��ҵ����к���linenoֵ>0��
	 *         �϶������У�δ�ҵ����򷵻�StatementFeatureStruct������к�����Ϊ0. ���������ټ�����
	 *          �϶��������У�  �򷵻�StatementFeatureStruct������к�����Ϊ-1.
	 */
	public StatementFeatureStruct getCodeStaticComplexMetricByFileLineno(int lineno)
	{
		StatementFeatureStruct fsStatement = new StatementFeatureStruct();
		int startLineno = this.getStartLine();
		int endLineno = this.getEndLine();
		if( (endLineno!=0) && (startLineno!=0) )
		{ //endLineno��startLineno����ʼ��Ϊ0��category=12,13,14��20��Щ���ͣ����޷�����ʼ�кͽ����С�
			if( lineno<startLineno || lineno>endLineno )
				return fsStatement; //���д��벻�ڴ����С��к�-1
			fsStatement.setLineno(0); //�϶��ڴ����У�����ProjectContext��ݴ��ж��ڴ����У�ֻ���Ҳ�����
		}
		//���startLineno==0 || endLineno==0 ����fsStatement���к���-1������ProjectContext��ݴ��ж�lineno���ڴ�����
		for( MethodContext mCtx : methods )
		{
			StatementFeatureStruct sfs = mCtx.getCodeStaticComplexMetricByFileLineno(lineno);
			int locLineno = sfs.getLineno();//-1 ��ʾ���ڷ����У�0��ʾ�ڷ����У�δ�ҵ���>0�ҵ���
			if( locLineno>0 ) 
			{//�ҵ����кŵ�������뾲̬���Ӷ�
				fsStatement = sfs;
				break;
			}
			else if ( locLineno==0 ) 
			{ //��ʾ�ڷ����У�δ�ҵ����϶��ڴ����У������Ҳ���������䡣���������{, }��
				break;
			}
			else //���ڷ���mCtx�У������ڵ�ǰ����ҡ�
				continue;
		}
		return fsStatement;
	}
	
	/**������������������VFL���֣��ȼ���ã���䵽StatementFeatureStruct�С�
	 *   VFL: variable-based fault localization
	 * @param lineCodes ÿ�����ĳ����ס�
	 * @param passed ĳ�ļ���Ӧ�ĸð汾�ĳɹ�������������
	 * @param failed �ð汾��δͨ��������������
	 */
	public void fillVFLFearture(List<SpectrumStruct> lineCodes,int passed,int failed)
	{
		//�Ƚ�ʹ�������Ե����ȫ���ҳ�����ע��ֲ��������Ͳ������Ḳ���������ơ�
		List<VariableInStatement> varISlst = new ArrayList<>();
		for( MethodContext mCtx : methods )
		{
			List<VariableInStatement> varsInMethod = mCtx.attributeAppearStatements(attributes);
			varISlst.addAll(varsInMethod);
		}
		//�������е����Գ����ס�
		VFLSpectraManagement vflsm = new VFLSpectraManagement();
		vflsm.computerSpectraVaiables(varISlst, lineCodes, passed, failed);
		//������㷽����VFL���֡�
		for( MethodContext mCtx : methods )
			mCtx.fillVFLFearture(vflsm.getVflSpecta(),lineCodes, passed, failed);
	}
	
	//��ʾ������Ϣ
	@Override
	public void showMe()
	{
		if( nesting )
			System.out.println("Filename: "+parsingFilename+"  Class:= "+name+",  Category="+category+", isInterface="+isInterface+", nesting, parent= "+parentName);
		else
			System.out.println("Filename: "+parsingFilename+"  Class:= "+name+",  Category="+category+", isInterface="+isInterface);
		//��ӡ�������б�
		System.out.print("        ");
		for( String attrname :attributes )
			System.out.print(attrname+",");
		System.out.println("         attirbute total = "+attributes.size());
		super.showMe();
		//��ʾ���������ݡ�
		for ( MethodContext mctxt : methods )
			mctxt.showMe();
	}
}
