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
	public final static int LoopStyle = 1; //ѭ����䣬
	public final static int IfStyle = 2; //������䣬
	public final static int ReturnStyle = 3; //return��䣬
	public final static int StatementRest = 4; //������䣬
	
	/*������ͣ��Ժ���Ƕ�����⡣*/
	private boolean bLoop; //ѭ����䣬����for, while , do while, foreach, lamada
	private boolean bCondition; //������䣬����if, else if, switch,catch(...), ? : (��Ԫ���ʽ),try,case,assert, 
	private boolean bReturn; //return���
	private boolean bOtherStatement;//������䣬���� throw, ��ֵ������������goto,break XXX, continue XXX,....
	
	/*���������γɵ�������Ӷ�*/
	private List<String> logicOperaters; //�����������߼��������� && || !�������ظ���
	private List<String> otherOperaters; //���������������������� ���߼���������ģ������ظ���

	/*���������γɵ�������Ӷ�*/
	private List<String> funcCalls; //���������ĺ������ã��洢���������������ƣ��������ظ���
	
	/*���ڼ������������ֲ����������������ԣ���SBFL����
	 * ע�⣬simpleNames���治�������ظ������ƣ�����������ֲ�����������������*/
	private List<String> simpleNames; //��¼���������б�ʶ�����ų������֡�����������������������
	/*
	 *���ԣ� �ֲ�������������������������󣬻��simpleNamesɾ����
	 *���ԣ�һ������£�simpleNamesΪ�ա�
	 *���ǣ���for��while��������棬���Զ����ڲ���������Щ��������Ӱ�츴�Ӷȣ���������simpleNames�
	 *���У�����ֲ��������������Ͳ���ԭ���ͣ��ڵ������ķ���ʱ�����ظ�ͳ��������Ӷ���������Ϊ������������simpleNames�
	 *     �磺 �оֲ�����SimpleSample ssm;   ssm.get();
	 *     get()�������������������ã���ssm�Ǿֲ���������صľֲ���������ֵ��
	 *     ��Ϊ��ͳ���꺯��get������������ssm������simpleNames�
	 */
	//�ر�ע�⣬û�еط�����sfsFeature��lineno
	private StatementFeatureStruct sfsFeature; //���븴�Ӷ�����
	
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
		sfsFeature = new StatementFeatureStruct(); //���븴�Ӷ�����
	}
	
	//��һ����1,2,3,4�����������͡�
	public void setStatementStyle(int style)
	{
		switch( style )
		{
		case LoopStyle:
			bLoop = true;   //ѭ��
			bCondition = false;
			bReturn = false;
			bOtherStatement = false;
			break;
		case IfStyle:
			bCondition = true; //����
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
			bOtherStatement = true; //��������
			bLoop = false;
			bCondition = false;
			bReturn = false;
			break;
		default: //����
			bLoop = false;
			bCondition = false;
			bReturn = false;
			bOtherStatement = false;
			break;
		}
	}
	
	/** ��ǰ���϶������кš�
	 * @param lineno �к�
	 * @return true:�����к�  false:�������к�
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
	
	//ѭ����䣬����for, while , do while, foreach
	public boolean isLoop() {
		return bLoop;
	}

	public void setLoop(boolean bLoop) {
		this.bLoop = bLoop;
	}

	//������䣬����if, else if, switch,catch(...), ? : (��Ԫ���ʽ),
	public boolean isCondition() {
		return bCondition;
	}

	public void setCondition(boolean bCondition) {
		this.bCondition = bCondition;
	}

	//return���
	public boolean isReturn() {
		return bReturn;
	}

	public void setReturn(boolean bReturn) {
		this.bReturn = bReturn;
	}

	//������䣬���� goto,break XXX, continue XXX, throw, ....
	public boolean isOtherStatement() {
		return bOtherStatement;
	}

	public void setOtherStatement(boolean bOtherStatement) {
		this.bOtherStatement = bOtherStatement;
	}

	//�����������߼��������� && || !�������ظ���
	public List<String> getLogicOperaters() {
		return logicOperaters;
	}

	public void setLogicOperaters(List<String> logicOperaters) {
		this.logicOperaters = logicOperaters;
	}

	//���������������������� ���߼���������ģ������ظ���
	public List<String> getOtherOperaters() {
		return otherOperaters;
	}

	public void setOtherOperaters(List<String> otherOperaters) {
		this.otherOperaters = otherOperaters;
	}

	//���������ĺ������ã��洢���������������ƣ��������ظ���
	public List<String> getFuncCalls() {
		return funcCalls;
	}

	public void setFuncCalls(List<String> funcCalls) {
		this.funcCalls = funcCalls;
	}

	//��¼���������б�ʶ�����ų������֡�����������������������
	public List<String> getSimpleNames() {
		return simpleNames;
	}


	public void setSimpleNames(List<String> simpleNames) {
		this.simpleNames = simpleNames;
	}

	//���븴�Ӷ�����
	public StatementFeatureStruct getSfsFeature() {
		return sfsFeature;
	}

	/** for����� for����һ�У�while����� while����һ�� 
	 * ��������£�for(while,if, foreach,....)���ڵ��кŲ�������������ʼ�кţ����ô˷����󣬿ɽ���������⡣
	 * @param lineno
	 */
	public void enlargeStartLineno(int lineno)
	{
		if( getStartLine()>lineno )
			setStartLine(lineno);
	}
	

	/** ����һ�����ӵ���䣬����������֣��������չ���Ա�����µ�����sContext
	 * ��չ���ݰ����� ��ʼ����չ����������չ����֪���Ӷ��ۼӣ�simpleNames�ۼӡ�
	 * @param sContext ��һ������һ���֣��������
	 */
	public void enlargeToComplexStatement(StatementContext sContext)
	{
		//��ʼ�У���������չ
		int scstart = sContext.getStartLine();
		int scend = sContext.getEndLine();
		if( getStartLine()==0 || getEndLine()==0 )
		{ //������0��˵������仹δ��AST��
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
			//��ʼ����չ����С���к�
			if( getStartLine()>scstart )
				setStartLine(scstart);
			//��������չ���Ҵ���к�
			if( getEndLine()<scend )
				setEndLine(scend);
			//�����ĸ��������ر�ע�⣬��ʱ������Ƕ�ף������ǵ�ֵ�����ı䣬�ǿ������ĵ�һ�д��������ֵ��
			//bLoop = ;
			//bCondition = ;
			//bReturn = ;
			//bOtherStatement = ;
			//�߼������� �� ���������� �������ظ�
			logicOperaters.addAll(sContext.getLogicOperaters());
			otherOperaters.addAll(sContext.getOtherOperaters());
			//�������� , �������ظ�
			List<String> funcName =  sContext.getFuncCalls();
			for( String item: funcName)
				if( !funcCalls.contains(item) )
					funcCalls.add(item);
			
			//simpleNames�ۼӣ������ظ��� 
			List<String> simpleOfSC = sContext.getSimpleNames();
			for( String item : simpleOfSC)
				if( !simpleNames.contains(item) )
					simpleNames.add(item);
		}//end of else
	}
	
	/*���ƶ��ĺ궨�壬��������funcCalls��������Ӻ������ö�Ӧ��������Ӷȡ�
	 * �����ֶ�����ʽ�ĺ궨��PASTObjectMacro�ͺ�����ʽ�ĺ궨��PASTFunctionMacro,
	*/
	public void addMacroDefinitionName(String macroName)
	{
		if( !funcCalls.contains(macroName) )
			funcCalls.add(macroName);
	}
	
	/** ͳ��seek�����ַ�����source���ֵĸ���
	 * @param seeking ���ԡ�ȫ�ֱ������������߾ֲ�����
	 * @param snameStatement  ���ı�ʶ��
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
	
	/**��������������׼���Ĳ��֣��ȼ���ã���䵽StatementFeatureStruct sfsFeature�С�
	 * @param attributes �����������
	 * @param parameters ���ڷ����Ĳ���
	 * @param localVariables ���ڷ����ľֲ�����
	 */
	public void fillEasyFeature(List<String> attributes,List<String> parameters,List<String> localVariables)
	{
		//����ʼ�к���Ϊ��sfsFeature���кź�����
		sfsFeature.setLineno(this.getStartLine());
		//ͳ�Ƹ�����ڳ��ֵ����Ը����������������ֲ���������
		//û�п������Ժ� ����������ֲ�����ͬ�������⡣@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		int n_attr = statisticsAppearToken(attributes,simpleNames); //���Ը���
		int n_para = statisticsAppearToken(parameters,simpleNames); //��������
		int n_vari = statisticsAppearToken(localVariables,simpleNames); //�ֲ���������
		sfsFeature.setAttr_global(n_attr);
		sfsFeature.setParameter(n_para);
		sfsFeature.setLocalvar(n_vari);
		//�����������
		sfsFeature.setIsloop(bLoop?(byte)1:(byte)0);
		sfsFeature.setIsif(bCondition?(byte)1:(byte)0);
		sfsFeature.setIsreturn(bReturn?(byte)1:(byte)0);
		sfsFeature.setIsother(bOtherStatement?(byte)1:(byte)0);
		//����߼��������������������������ͺ������ô�����
		List<String> dinLogic = logicOperaters.stream().distinct().collect(Collectors.toList());
		sfsFeature.setOplogic(dinLogic.size());
		List<String> dinRest = otherOperaters.stream().distinct().collect(Collectors.toList());
		sfsFeature.setOpalgor(dinRest.size());
		sfsFeature.setFuncall(funcCalls.size());
	}
	
	//��һ��鷽���ڵ���䣬���ʹ���˾ֲ�������ÿʹ��һ������ͬ�������ʹ�ã���Ϊһ���������Ӹ��Ӷ�1.
	//methodVariable �����ڵľֲ�������
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
	
	
	
	/*��һ��鷽���ڵ���䣬���ʹ���˺궨�壬ÿʹ��һ������ͬ���Զ��ʹ�ã���Ϊһ����������һ������������������.
	 *macroDefine �궨������
	 * �����ֶ�����ʽ�ĺ궨��PASTObjectMacro�ͺ�����ʽ�ĺ궨��PASTFunctionMacro,
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
	
	/**�ҳ����ֹ�variables�ڱ������Ƶ�������䣬���VariableInStatement�ṹ
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
				 //������variables�ڱ�������
				 VariableInStatement vis = new VariableInStatement(sname,getStartLine(),getEndLine());
				 varsInStatement.add(vis);
				 break;//simpleNames�ҵ����˳��˲�ѭ����
			 }
		 }
		 return varsInStatement;
	 }
	
	/**�� ���������VFL���֣��ȼ���ã���䵽StatementFeatureStruct�С�
	 * @param vflSpecta ���ԡ�����������ֲ�������VFL������
	 */
	public void fillVFLFearture(List<VFLSpectrumStruct> vflSpecta)
	{
		//ע�⣺�����ĸ�ֵ����ͬһ�����������ǵ�����ȡֵ
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
				//���ԡ�����������ֲ����������ڸ�����ڣ��������VFL������ֵ
				if( vss.largeSuspicious(maxCovfail, maxCovpass, maxNCcovok, maxNcovbug) )
				{ //maxCovfail, maxCovpass, maxNCcovok, maxNcovbug��Ԫ��Ŀ��ɶȱ�vssҪС��
					maxCovfail = vss.getCovfail();
					maxCovpass = vss.getCovpass();
					maxNCcovok = vss.getNcovok();
					maxNcovbug = vss.getNcovbug();
				}
				break; //vflSpecta��洢���ҵ��ˣ��˳��˲�ѭ����
			}//end of for( VFLSpectrumStruct vss: vflSpecta )
		}//end of for ( String sname: simpleNames )
		//���ø�����VFL����
		sfsFeature.setCovfail(maxCovfail);
		sfsFeature.setCovpass(maxCovpass);
		sfsFeature.setNcovbug(maxNcovbug);
		sfsFeature.setNcovok(maxNCcovok);
	}
	
	//��ʾ������Ϣ
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
		//��ӡ�߼��������б�
		for( String para : logicOperaters )
			System.out.print(para+",  ");
		System.out.print("        ");
		//��ӡ ���߼���������� �б�
		for( String para : otherOperaters )
			System.out.print(para+",  ");
		System.out.print("        ");
		//��ӡ ���������������� �б�
		for( String para : funcCalls )
			System.out.print(para+",  ");
		System.out.println("  .");
		//��ӡsimpleNames�б�
		System.out.print("               ");
		for( String para : simpleNames )
			System.out.print(para+",  ");
		System.out.println("=> "+simpleNames.size());
	}
}
