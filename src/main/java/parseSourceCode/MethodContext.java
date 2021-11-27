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
	private String name;  //��������
	/*
	 * ע�⣺ localVariables�������ڲ��ࡢǶ���ࡢ�������inner class�ľֲ�������
	 * ���������ȼ����⣬Ҳ����ʵ������
	 */
	private List<String> localVariables;    // �÷��������оֲ�������
	private List<String> parameters;    // �÷����Ĳ������б�
	/*
	 * type �������ͣ�1=���캯����2=getter or setter, 3=Java/C++��ͨ��ֱ�ӷ�����C�ĺ�������
	 *               4=Initializer, ��ĳ�ʼ���飬���⴦���䷽����Ϊclass_initializer��
	 *               5,����Ƕ����ķ���������CK�ȸ��Ӷ�ʱ���⴦��
	 *               6����Ĵ���ֵ������������䡣 class_FieldDeclaration
	 *               11,C++�ļ��Ķ��㺯����
	 *               12��ȱ��ͷ�ļ�����ķ�����������C++�ļ��Ķ��㺯��(Ҳ������ͷ�ļ�������ͷ�ļ������ļ�����)
	 *               13,C++ȫ�ֱ������ļ��������������䣬TopDeclartionCpp��ķ�����
	 *               21,C����ı�������������һ�������С�
	 */
	private int type; // ��������
	private List<StatementContext> statements; //�÷�����������䡣
	//���¶�������ķ�������������
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

	/** ������������䵽�˷����С�
	 * @param statements
	 */
	public void fillStatementsToMethod(List<StatementContext> sContexts)
	{
		for( StatementContext cc : sContexts )
			statements.add(cc);
	}
	
	/** Ϊ��λ��Ĵ���ֵ������������䣬�ڷ���������б�����ӵ�������䡣
	 * @param stmtContext
	 */
	public void addOneStatement(StatementContext stmtContext)
	{
		statements.add(stmtContext);
	}
	
	//��������
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	 // �÷��������оֲ�������
	public List<String> getLocalVariables() {
		return localVariables;
	}

	public void setLocalVariables(List<String> localVariables) {
		this.localVariables = localVariables;
	}

	//������ע����setLocalVariables�Ĳ��
	public void copyLocalVariables(List<String> varLst) {
		for( String var : varLst )
			localVariables.add(var);
	}

	// �÷����Ĳ������б�
	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	//���һ�������������б�
	public void addParameter(String para)
	{
		parameters.add(para);
	}
	
	//�������ͣ�1=���캯����2=getter or setter, 3=��ͨ������
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	/** ����Ϊ���ⷽ����
	 * �÷��������ⷽ����ֻ����C�����ռ����к����������䡣
	 */
	public void setGlobalVariableOfC()
	{
		name = GlobalVariableOfC;
		type = 21;
	}
	
	/** ����Ϊ���ⷽ����TopDeclartionCpp��ķ�����
	 * �÷��������ⷽ����ֻ����C++�����ռ�����C++ȫ�ֱ������ļ��������������䡣
	 */
	public void setGlobalVariableOfCpp()
	{
		name = GlobalVariableOfCpp;
		type = 13;
	}
	
	//�÷��������ⷽ���𣿣�ֻ����C�����ռ����к����������䡣��
	public boolean isGlobalVariableOfC()
	{
		if( type==21 )
			return true;
		else
			return false;
	}
	
	//�÷��������ⷽ���𣿣�C++�����ռ�����C++ȫ�ֱ������ļ��������������䣩
	public boolean isGlobalVariableOfCpp()
	{
		if( type==13 )
			return true;
		else
			return false;
	}
		
	//���ⷽ�������������ڲ�����ڲ���ķ�����
	public void setInner2Type()
	{
		this.type = 5;
	}
	
	//�÷�����������䡣
	public List<StatementContext> getStatements() {
		return statements;
	}
	
	//�÷��������������
	public int getNumberOfStatements()
	{
		return statements.size();
	}
	

	/**��������������׼���Ĳ��֣��ȼ���ã���䵽StatementFeatureStruct�С�
	 * @param attributes �����������
	 */
	public void fillEasyFeature(List<String> attributes)
	{
		for( StatementContext sc : statements )
			sc.fillEasyFeature(attributes,parameters,localVariables);
	}
	
	
	//��һ��鷽���ڵ���䣬���ʹ���˺궨�壬��Ӧ�����Ӹ��Ӷ�.
	public void adjustComplexMetricWithMacroDefinition(List<String> macroDefines)
	{
		for( StatementContext sc : statements )
			sc.adjustComplexMetricWithMacroDefinition(macroDefines);
	}
	
	/** attr�Ƿ��ڲ������߶���ֲ��������ֹ�
	 * @param attr
	 * @return true�����ֹ��� false:δ������
	 */
	private boolean isParameterLocalVariable(String attr)
	{
		boolean have = false;
		//�Ƿ��ڲ������г��ֹ�
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
		//�Ƿ��ھֲ��������г��ֹ�
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
	
	 /** �ҳ����������г��������Ե���䣬���VariableInStatement�ṹ
	 * @param attributes ����������ԣ�����C/C++��ȫ�ֱ�����
	 * @return ����������ʼ�кš������кŵ��б�
	 */
	public List<VariableInStatement> attributeAppearStatements(List<String> attributes)
	 {
		 List<VariableInStatement> varsInMethod = new ArrayList<>();
		 //���ǵ��ֲ��������Ͳ������Ḳ���������ơ��Ƚ�attributes����localVariables��parametersͬ����ɾ����
		 List<String> variAttr = new ArrayList<>();
		 for( String attr: attributes )
		 {
			 if( !isParameterLocalVariable(attr) ) //attrû���ڲ������߶���ֲ��������ֹ�
				 variAttr.add(attr);
		 }
		 //�ҳ����ֹ�variAttr�����Ե�������䣬���VariableInStatement�ṹ
		 for ( StatementContext stmt: statements )
		 {
			 List<VariableInStatement> varsInStatement = stmt.variableAppearStatements(variAttr);
			 varsInMethod.addAll(varsInStatement);
		 }
		 return varsInMethod;
	 }
	
	/**�ѷ������������������VFL���֣��ȼ���ã���䵽StatementFeatureStruct�С�
	 *   VFL: variable-based fault localization
	 * @param lineCodes ÿ�����ĳ����ס�
	 * @param passed ĳ�ļ���Ӧ�ĸð汾�ĳɹ�������������
	 * @param failed �ð汾��δͨ��������������
	 * @param vflSpecta ���Ե�VFL������
	 */
	public void fillVFLFearture(List<VFLSpectrumStruct> vflSpecta,List<SpectrumStruct> lineCodes,int passed,int failed)
	{
		//���ǵ��ֲ��������Ͳ������Ḳ���������ơ��Ƚ�attributes����localVariables��parametersͬ����ɾ����
		List<VFLSpectrumStruct> vflssInMethod = new ArrayList<>();
		for( VFLSpectrumStruct vss : vflSpecta )
		{
			if( !isParameterLocalVariable(vss.getVariable()) ) //��������û���ڲ������߶���ֲ��������ֹ�
				vflssInMethod.add(vss);
		}
		//��localVariables��parameters�ϲ���һ���ַ��������ÿ�������ͬ�������⡣����������������һ���������⡣
		List<String> variParm = new ArrayList<>();
		variParm.addAll(localVariables);
		variParm.addAll(parameters);
		//�Ƚ�ʹ���˾ֲ������Ͳ��������ȫ���ҳ���
		List<VariableInStatement> varsBeMethod = new ArrayList<>();
		for ( StatementContext stmt: statements )
		{
			List<VariableInStatement> varsInStatement = stmt.variableAppearStatements(variParm);
			varsBeMethod.addAll(varsInStatement);
		}
		//�������еľֲ������Ͳ����ĳ����ס�
		VFLSpectraManagement vflsm = new VFLSpectraManagement();
		vflsm.computerSpectraVaiables(varsBeMethod, lineCodes, passed, failed);
		//�����Ե� �� �ֲ������������� VFL�����׺ϲ���һ����
		vflssInMethod.addAll(vflsm.getVflSpecta()); 
		//������������VFL���֡�
		for ( StatementContext stmt: statements )
			stmt.fillVFLFearture(vflssInMethod);
	}
	
	/**���кţ��ҳ���Ӧ��������뾲̬���Ӷ�
	 * @param lineno  �к�
	 * @return ���Ӷ�ֵ��
	 *           �ڷ����У��Ѿ��ҵ����к���linenoֵ>0�������ҵ�����startLineno or endLineno��
	 *         �϶��ڷ����У�δ�ҵ����򷵻�StatementFeatureStruct������к�����Ϊ0. ���������ټ�����
	 *          �϶����ڷ����У�  �򷵻�StatementFeatureStruct������к�����Ϊ-1.
	 */
	public StatementFeatureStruct getCodeStaticComplexMetricByFileLineno(int lineno)
	{
		StatementFeatureStruct fsStatement = new StatementFeatureStruct();
		int startLineno = this.getStartLine();
		int endLineno = this.getEndLine();
		if( (endLineno!=0) && (startLineno!=0) )
		{ //endLineno��startLineno����ʼ��Ϊ0��type=6,13,21��Щ���ͣ����޷�����ʼ�кͽ����С�
			if( lineno<startLineno || lineno>endLineno )
				return fsStatement;//���д��벻�ڴ˷����ڡ��к�-1
		}
		List<StatementContext> finds=new ArrayList<>(); //�����к�lineno������StatementContext
		for( StatementContext stmtCtx : statements )
		{
			if( stmtCtx.haveBe(lineno) )
				finds.add(stmtCtx);
		}
		/*Ϊ�������������Ͽ�ִ�����д��ͬһ�С�
		 * �磺 (49,51)=6  (51,51)=4 (51,51)=7  lineno = 49��ȡ6; =50 ȡ6; =51ȡ7;
		 * ����ԭ����startLine-lineno��Сֵ����䣬������������ж�����ȡ�������ֵ��Ϊ���Ӷ�ֵ��
		 */
		int number = finds.size();
		if( number<=0 ) //�ڷ����У�δ�ҵ���
		{
			if( startLineno==0 || endLineno==0 ) //�൱��(endLineno!=0) && (startLineno!=0)�ķ���
				fsStatement.setLineno(-1);//�к�<0������ClassContext��ݴ��жϲ��ڴ˷����С�
			else
				fsStatement.setLineno(0);//�к�0������ClassContext��ݴ��ж��ڴ˷����У�ֻ���Ҳ�����
		}
		else if ( number==1 )
		{//ֻ��һ��StatementContext
			fsStatement = finds.get(0).getSfsFeature();
			fsStatement.setLineno(lineno); //��Ѱ���к�(����)��Ϊ�Żض����ֵ
		}
		else 
		{//�ж������ôֻ����ʼ�к�= lineno�Ĳŷ���Ҫ�󡣣���ִ����䲻�����໥Ƕ�ס���
			int maxv = 0;
			fsStatement = finds.get(0).getSfsFeature(); //ȱʡ�����ѡ���һ�����Ϊ�д���Ĵ���
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
			fsStatement.setLineno(lineno);//��Ѱ���к�(����)��Ϊ�Żض����ֵ
		}
		return fsStatement;
	}
	
	//��ʾ������Ϣ
	@Override
	public void showMe()
	{
		System.out.print("       Method:  "+name+", type=: "+type+"   parameter: ");
		//��ӡ�������б�
		for( String para : parameters )
			System.out.print(para+",  ");
		System.out.println(".  "+parameters.size());
		//��ӡ�ֲ��������б�
		System.out.print("                Variable: ");
		for( String var : localVariables )
			System.out.print(var+",  ");
		System.out.println("."+localVariables.size());
		super.showMe();
		//��ӡ����������ݡ�
		for( StatementContext sContext : statements )
			sContext.showMe();
	}
}
