/**
 * 
 */
package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import affiliated.AffiliatedFactory;
import affiliated.ExcludeVersion;
import affiliated.IFaultFile;
import affiliated.IProfileFile;
import sbflMetrics.AbstractMetricMethod;

import sbflMetrics.IMetricMethod;

import sbflMetrics.SBFLperformanceAssess;

import sbflMetrics.WorstBestMean;
import softComplexMetric.LineComplexFeatureFile;

/** ��������ʾ���洢 SBFL���ܡ�
 * @author hejiahui
 *
 */
public class EvaluatePerformanceSBFL {
	//�Զ���Ϊkey����������ƽ��SBFL����
	private static Map<String,WorstBestMean> expenseMap = new HashMap<>(); //EXAM
	private static Map<String,float[]> pScoreMap = new HashMap<>(); //Pscore,Pckheck,Pbugver
	private static Map<String,Integer> vernumMap = new HashMap<>(); //�汾������Ϊ�ۺ���ƽ��ֵ��
	private static boolean bShowMe = true; //�Ƿ���ʾÿ���汾���м���
	
	//����ʾÿ���汾���м���
	public static void DonotShowMe()
	{
		bShowMe = false;
	}
	/** һ��ֻ����һ��SBFL������һ����������������ȣ�Pscore EXAM����
	 * @param objectName  
	 * @param strAlgorihtm  "Tarantula" Jaccard,Tarantula,Ochiai,Opass
	 * @param bStoreFile �Ƿ�ʵ�������浽�ļ���
	 */
	public static void evaluateShowOne(String objectName,String strAlgorihtm,boolean bStoreFile)
	{
		//��ȡExpense�Ľ����
		evaluateExpenseShow(objectName,strAlgorihtm,bStoreFile);
		//��ȡPscoree�Ľ����
		evaluatePscoreShow(objectName,strAlgorihtm);
	}

		
	/**���ԣ�һ��ֻ����һ��SBFL������һ������һ��������ȡ�
	 * @param objectName  ��������
	 * @param strAlgorihtm   Tarantula,Jaccard,Tarantula,Ochiai,Opass,...
	 */
	private static void evaluateExpenseShow(String objectName,String strAlgorihtm,boolean bStoreFile)
	{
		IFaultFile ffiAgent = AffiliatedFactory.createFaultFileObject(objectName);
		if( false==ffiAgent.readFaultFile() )
		{
			System.out.println("Read file "+objectName+".fault is error.");
			return;
		}
		
		int vernum = ffiAgent.getVerNo();
		List<WorstBestMean> wbsLst = new ArrayList<>();
		for( int ver=1;ver<=vernum; ver++)
		{
			int bugId = ffiAgent.getBugID(ver);
			if( true==ExcludeVersion.isExcludeVer(objectName,bugId) )
				continue; //�ð汾���μӼ��㡣
			int[] faultStats = ffiAgent.getFaultLinesVer(ver); //�ð汾�Ĺ�����������
			String[] fileNames = ffiAgent.getFaultFilesVer(ver);//�ð汾�Ĺ����ļ������顣
			IProfileFile profileAgent = AffiliatedFactory.createProfileFileObject(objectName, bugId);//ֻ������汾���ԡ�
			if( false==profileAgent.readProfileFile() )
			{
				System.out.println("Read file "+objectName+"_v"+String.valueOf(bugId)+".profile is error.");
				return;
			}
			SBFLperformanceAssess sbflm = new SBFLperformanceAssess(profileAgent.getPassed(),profileAgent.getFailed(),
					profileAgent.getTotalExec(),profileAgent.getSpectrumList(),fileNames,faultStats);
			//=============  worst-best-mean  strategy
			WorstBestMean imsbwm = sbflm.calPerformance(strAlgorihtm,AbstractMetricMethod.ExpenseName);
			if( bShowMe )
			{
				System.out.println("Version(bugID) :"+bugId);
				imsbwm.showExam();
			}
			wbsLst.add((WorstBestMean)imsbwm);
		}
		//=============  worst-best-mean  strategy
		WorstBestMean wbmTmp = WorstBestMean.averageExam(wbsLst);
		System.out.println("Average of "+strAlgorihtm+" by "+String.valueOf(wbsLst.size())+" vers:");
		wbmTmp.showExam();
		//=============����ʵ����
		if( bStoreFile )
			ExperimentalResultFile.ExpenseResultStore(objectName,strAlgorihtm,wbsLst);
		storeVersionMeanExpense(objectName,wbmTmp);
		//============�汾������Ϊ�ۺ���ƽ��ֵ��
		vernumMap.put(objectName, wbsLst.size());
	}//end of evaluateOneMetricSBFL

	/*
	 * ����ö������а汾��ƽ��SBFL �����
	 * �ر�ע�⣺ û�п���strAlgorihtm�����ԣ�ֻ����strAlgorihtm��ͬ������£�����storeVersionMeanExpense�������塣
	 */
	private static void storeVersionMeanExpense(String objectName,WorstBestMean wbm)
	{
		expenseMap.put(objectName, wbm);
	}

	/**���ԣ�һ��ֻ����һ��SBFL������һ������һ��������ȡ�
	 * ������SOS����
	 * @param objectName  ��������
	 * @param strAlgorihtm   Tarantula,Jaccard,Tarantula,Ochiai,Opass,...
	 */
	private static void evaluatePscoreShow(String objectName,String strAlgorihtm)
	{
		IFaultFile ffiAgent = AffiliatedFactory.createFaultFileObject(objectName);
		if( false==ffiAgent.readFaultFile() )
		{
			System.out.println("Read file "+objectName+".fault is error.");
			return;
		}
		
		int vernum = ffiAgent.getVerNo();
		List<WorstBestMean> wbsLst = new ArrayList<>(); // worst-best-mean  strategy
		for( int ver=1;ver<=vernum; ver++)
		{
			int bugId = ffiAgent.getBugID(ver);
			if( true==ExcludeVersion.isExcludeVer(objectName,bugId) )
				continue; //�ð汾���μӼ��㡣
			int[] faultStats = ffiAgent.getFaultLinesVer(ver); //�ð汾�Ĺ�����������
			String[] fileNames = ffiAgent.getFaultFilesVer(ver);//�ð汾�Ĺ����ļ������顣
			IProfileFile profileAgent = AffiliatedFactory.createProfileFileObject(objectName, bugId);//ֻ������汾���ԡ�
			if( false==profileAgent.readProfileFile() )
			{
				System.out.println("Read file "+objectName+"_v"+String.valueOf(bugId)+".profile is error.");
				return;
			}
			SBFLperformanceAssess sbflm = new SBFLperformanceAssess(profileAgent.getPassed(),profileAgent.getFailed(),
					profileAgent.getTotalExec(),profileAgent.getSpectrumList(),fileNames,faultStats);
			//=============  worst-best-mean  strategy
			WorstBestMean imsbwm = sbflm.calPerformance(strAlgorihtm,AbstractMetricMethod.PScore);
			wbsLst.add(imsbwm);
		}
		//=============  worst-best-mean  strategy
		float[] meanBugver = WorstBestMean.averagePscore(wbsLst);
		WorstBestMean.showPscore(meanBugver);
		System.out.println("Object name is "+objectName+". Pscore is ok.");
		//=============����ʵ����
		storeVersionMeanPscore(objectName,meanBugver);
	}//end of evaluateOneMetricSBFL
	
	/*
	 * ����ö������а汾��ƽ��SBFL Pscore�����
	 * �ر�ע�⣺ û�п���strAlgorihtm�����ԣ�ֻ����strAlgorihtm��ͬ������£�����storeVersionMeanPscore�������塣
	 */
	private static void storeVersionMeanPscore(String objectName,float[] means)
	{
		pScoreMap.put(objectName, means);
	}

	/**
	 * ʵ���������ļ����ļ�����strAlgorihtm����
	 *  bAggregation = false,���ۺ�  =true,�ۺ�
	 */
	public static void storeExpensePscoreToFile(boolean bAggregation,String strAlgorihtm)
	{
		ExperimentalResultFile.ExpenseAllObjectResult(bAggregation,strAlgorihtm, 
										expenseMap,vernumMap); //EXAM
		ExperimentalResultFile.PscoreAllObjectResult(bAggregation,strAlgorihtm
										, pScoreMap,vernumMap); //Pbugver
	}
	
	/**
	 * �ۺ� Siemens gzip grep sed��SIR����
	 */
	public static void aggregateSIRProjectSiemensUnix()
	{
		Map<String,List<String>> aggregations =XMLConfigFile.getAggregations();
		for( Map.Entry<String, List<String>>  entry  :  aggregations.entrySet()){
			//�ҳ��ۺϵĶ�����������
			String aggrProject = entry.getKey(); //�ۺϵĶ���������grep
			List<String> items = entry.getValue();//�ۺϵ������grepV2,grepV3,grepV4
			//��EXAM & Pscore HashMap��ȡ����Щ����Ľ������ɾ����Щ���
			List<WorstBestMean> expenseResults = new ArrayList<>();
			List<float[]> pscoreResults = new ArrayList<>();
			List<Integer> versions = new ArrayList<>();//�汾������������ƽ��ֵ
			int aggrVers = 0; //�ۺϺ�İ汾������
			for( String project : items )
			{
				WorstBestMean expenseMetric = getSubObjectExpenseFromAggregate(project); //EXAM
				expenseResults.add(expenseMetric);
				float[] pscoreMetric =  getSubObjectPscoreFromAggregate(project); //Pscore
				pscoreResults.add(pscoreMetric);
				int bugs = getVersionNumber(project);
				versions.add(bugs);
				aggrVers += bugs;
			}
			//������ǵ�ƽ��ֵ��
			WorstBestMean mStrgResult = WorstBestMean.aggrMeanExam(expenseResults,versions);
			float[] mpsResult = WorstBestMean.averageAggrPscore(pscoreResults,versions);
			//���¼��뵽HashMAP
			expenseMap.put(aggrProject, mStrgResult);
			pScoreMap.put(aggrProject, mpsResult);
			vernumMap.put(aggrProject, aggrVers);
		}//end of for,... MAP
	}
		
	/** ��expenseMap��ȡ��������ΪobjectName��ʵ���������Ӽ�����ɾ���ý����
	 * @param objectName
	 * @return
	 */
	private static WorstBestMean getSubObjectExpenseFromAggregate(String objectName)
	{
		WorstBestMean stgMetric = null; 
		for(Map.Entry<String, WorstBestMean>  entry  :  expenseMap.entrySet()){
			String project = entry.getKey();
			if( project.contentEquals(objectName) )
			{
				stgMetric = entry.getValue();
				expenseMap.remove(project);//ɾ���ý����
				break;
			}
		}
		return stgMetric;
	}
	
	/** ��pScoreMap��ȡ��������ΪobjectName��ʵ���������Ӽ�����ɾ���ý����
	 * @param objectName
	 * @return
	 */
	private static float[] getSubObjectPscoreFromAggregate(String objectName)
	{
		float[] scoreMetric = null; 
		for(Map.Entry<String, float[]>  entry  :  pScoreMap.entrySet()){
			String project = entry.getKey();
			if( project.contentEquals(objectName) )
			{
				scoreMetric = entry.getValue();
				pScoreMap.remove(project);//ɾ���ý����
				break;
			}
		}
		return scoreMetric;
	}
	
	/** ��vernumMap��ȡ��������ΪobjectName�İ汾������
	 * @param objectName
	 * @return
	 */
	private static Integer getVersionNumber(String objectName)
	{
		int nbugid = 0; 
		for(Map.Entry<String, Integer>  entry  :  vernumMap.entrySet()){
			String project = entry.getKey();
			if( project.contentEquals(objectName) )
			{
				nbugid = entry.getValue();
				break;
			}
		}
		return nbugid;
	}
}
