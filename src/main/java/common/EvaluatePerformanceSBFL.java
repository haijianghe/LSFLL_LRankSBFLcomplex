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

/** 评估、显示、存储 SBFL性能。
 * @author hejiahui
 *
 */
public class EvaluatePerformanceSBFL {
	//以对象为key，保存它的平均SBFL性能
	private static Map<String,WorstBestMean> expenseMap = new HashMap<>(); //EXAM
	private static Map<String,float[]> pScoreMap = new HashMap<>(); //Pscore,Pckheck,Pbugver
	private static Map<String,Integer> vernumMap = new HashMap<>(); //版本个数，为聚合求平均值。
	private static boolean bShowMe = true; //是否显示每个版本的中间结果
	
	//不显示每个版本的中间结果
	public static void DonotShowMe()
	{
		bShowMe = false;
	}
	/** 一次只计算一个SBFL技术，一个对象，两种评估测度（Pscore EXAM）。
	 * @param objectName  
	 * @param strAlgorihtm  "Tarantula" Jaccard,Tarantula,Ochiai,Opass
	 * @param bStoreFile 是否将实验结果保存到文件。
	 */
	public static void evaluateShowOne(String objectName,String strAlgorihtm,boolean bStoreFile)
	{
		//获取Expense的结果。
		evaluateExpenseShow(objectName,strAlgorihtm,bStoreFile);
		//获取Pscoree的结果。
		evaluatePscoreShow(objectName,strAlgorihtm);
	}

		
	/**测试，一次只计算一个SBFL技术，一个对象，一种评估测度。
	 * @param objectName  对象名称
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
				continue; //该版本不参加计算。
			int[] faultStats = ffiAgent.getFaultLinesVer(ver); //该版本的故障语句号数组
			String[] fileNames = ffiAgent.getFaultFilesVer(ver);//该版本的故障文件名数组。
			IProfileFile profileAgent = AffiliatedFactory.createProfileFileObject(objectName, bugId);//只能逐个版本测试。
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
		//=============保存实验结果
		if( bStoreFile )
			ExperimentalResultFile.ExpenseResultStore(objectName,strAlgorihtm,wbsLst);
		storeVersionMeanExpense(objectName,wbmTmp);
		//============版本个数，为聚合求平均值。
		vernumMap.put(objectName, wbsLst.size());
	}//end of evaluateOneMetricSBFL

	/*
	 * 保存该对象所有版本的平均SBFL 结果。
	 * 特别注意： 没有考虑strAlgorihtm。所以，只能在strAlgorihtm相同的情况下，调用storeVersionMeanExpense才有意义。
	 */
	private static void storeVersionMeanExpense(String objectName,WorstBestMean wbm)
	{
		expenseMap.put(objectName, wbm);
	}

	/**测试，一次只计算一个SBFL技术，一个对象，一种评估测度。
	 * 不考虑SOS策略
	 * @param objectName  对象名称
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
				continue; //该版本不参加计算。
			int[] faultStats = ffiAgent.getFaultLinesVer(ver); //该版本的故障语句号数组
			String[] fileNames = ffiAgent.getFaultFilesVer(ver);//该版本的故障文件名数组。
			IProfileFile profileAgent = AffiliatedFactory.createProfileFileObject(objectName, bugId);//只能逐个版本测试。
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
		//=============保存实验结果
		storeVersionMeanPscore(objectName,meanBugver);
	}//end of evaluateOneMetricSBFL
	
	/*
	 * 保存该对象所有版本的平均SBFL Pscore结果。
	 * 特别注意： 没有考虑strAlgorihtm。所以，只能在strAlgorihtm相同的情况下，调用storeVersionMeanPscore才有意义。
	 */
	private static void storeVersionMeanPscore(String objectName,float[] means)
	{
		pScoreMap.put(objectName, means);
	}

	/**
	 * 实验结果存入文件。文件名以strAlgorihtm命名
	 *  bAggregation = false,不聚合  =true,聚合
	 */
	public static void storeExpensePscoreToFile(boolean bAggregation,String strAlgorihtm)
	{
		ExperimentalResultFile.ExpenseAllObjectResult(bAggregation,strAlgorihtm, 
										expenseMap,vernumMap); //EXAM
		ExperimentalResultFile.PscoreAllObjectResult(bAggregation,strAlgorihtm
										, pScoreMap,vernumMap); //Pbugver
	}
	
	/**
	 * 聚合 Siemens gzip grep sed等SIR对象。
	 */
	public static void aggregateSIRProjectSiemensUnix()
	{
		Map<String,List<String>> aggregations =XMLConfigFile.getAggregations();
		for( Map.Entry<String, List<String>>  entry  :  aggregations.entrySet()){
			//找出聚合的对象名和子项
			String aggrProject = entry.getKey(); //聚合的对象名，如grep
			List<String> items = entry.getValue();//聚合的子项，如grepV2,grepV3,grepV4
			//从EXAM & Pscore HashMap中取出这些子项的结果，并删除这些子项。
			List<WorstBestMean> expenseResults = new ArrayList<>();
			List<float[]> pscoreResults = new ArrayList<>();
			List<Integer> versions = new ArrayList<>();//版本个数，将来求平均值
			int aggrVers = 0; //聚合后的版本总数。
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
			//求出他们的平均值。
			WorstBestMean mStrgResult = WorstBestMean.aggrMeanExam(expenseResults,versions);
			float[] mpsResult = WorstBestMean.averageAggrPscore(pscoreResults,versions);
			//重新加入到HashMAP
			expenseMap.put(aggrProject, mStrgResult);
			pScoreMap.put(aggrProject, mpsResult);
			vernumMap.put(aggrProject, aggrVers);
		}//end of for,... MAP
	}
		
	/** 从expenseMap中取出对象名为objectName的实验结果，并从集合中删除该结果。
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
				expenseMap.remove(project);//删除该结果。
				break;
			}
		}
		return stgMetric;
	}
	
	/** 从pScoreMap中取出对象名为objectName的实验结果，并从集合中删除该结果。
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
				pScoreMap.remove(project);//删除该结果。
				break;
			}
		}
		return scoreMetric;
	}
	
	/** 从vernumMap中取出对象名为objectName的版本个数。
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
