/**
 * 
 */
package ranking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import affiliated.AbstractProfileFile;
import affiliated.AffiliatedFactory;
import affiliated.ExcludeVersion;
import affiliated.IFaultFile;
import affiliated.IProfileFile;
import common.ExperimentalResultFile;
import common.XMLConfigFile;
import sbflMetrics.AbstractMetricMethod;
import sbflMetrics.SBFLperformanceAssess;
import sbflMetrics.WorstBestMean;

/** 评估、显示、存储 learning to rank 性能。
 * @author hejiahui
 *
 */
public class EvaluatePerformanceLTR {
	//以对象为key，保存它的平均 LTR : learning to rank 性能
	private static Map<String,WorstBestMean> expenseMap = new HashMap<>(); //EXAM
	private static Map<String,float[]> pScoreMap = new HashMap<>(); //Pscore,Pckheck,Pbugver
	private static Map<String,Integer> vernumMap = new HashMap<>(); //版本个数，为聚合求平均值。

	/** 一个对象，两种评估测度（Pscore EXAM）。
	 * @param objectName  
	 */
	public static void evaluateShowOne(String objectName)
	{
		//获取Expense的结果。
		evaluateExpenseShow(objectName);
		//获取Pscoree的结果。
		evaluatePscoreShow(objectName);
	}
	
	/**Expense: 一次只计算 一个对象，Learning to Rank。
	 * @param objectName  对象名称
	 */
	private static void evaluateExpenseShow(String objectName)
	{
		int[] inBugids = AbstractProfileFile.getInclusionBugId(objectName);

		if( inBugids==null )
			return;
		List<WorstBestMean> wbsLst = new ArrayList<>();
		for( int index=1;index<=inBugids.length; index++)
		{
			int bugId = inBugids[index-1];
			LTRperformanceAssess sbflm = new LTRperformanceAssess(objectName,bugId);
			//=============  worst-best-mean  strategy
			WorstBestMean imsbwm = sbflm.calPerformance(AbstractMetricMethod.ExpenseName);
			wbsLst.add((WorstBestMean)imsbwm);
		}
		//=============  worst-best-mean  strategy
		WorstBestMean wbmTmp = WorstBestMean.averageExam(wbsLst);
		System.out.println("Average of LTR by "+String.valueOf(wbsLst.size())+" vers:");
		wbmTmp.showExam();
		//=============保存实验结果
		expenseMap.put(objectName, wbmTmp);
		//============版本个数，为聚合求平均值。
		vernumMap.put(objectName, wbsLst.size());
	}//end of evaluateOneMetricSBFL


	/**测试，一次只计算一个对象，Learning to Rank。
	 * 不考虑SOS策略
	 * @param objectName  对象名称
	 */
	private static void evaluatePscoreShow(String objectName)
	{
		int[] inBugids = AbstractProfileFile.getInclusionBugId(objectName);

		if( inBugids==null )
			return;
		List<WorstBestMean> wbsLst = new ArrayList<>(); // worst-best-mean  strategy
		for( int index=1;index<=inBugids.length; index++)
		{
			int bugId = inBugids[index-1];
			LTRperformanceAssess sbflm = new LTRperformanceAssess(objectName,bugId);
			//=============  worst-best-mean  strategy
			WorstBestMean imsbwm = sbflm.calPerformance(AbstractMetricMethod.PScore);
			wbsLst.add(imsbwm);
		}
		//=============  worst-best-mean  strategy
		float[] meanBugver = WorstBestMean.averagePscore(wbsLst);
		WorstBestMean.showPscore(meanBugver);
		System.out.println("Object name is "+objectName+". Pscore is ok.");
		//=============保存实验结果
		pScoreMap.put(objectName, meanBugver);
	}//end of evaluateOneMetricSBFL
	

	/**
	 * 实验结果存入文件。文件名以strAlgorihtm命名
	 *  bAggregation = false,不聚合  =true,聚合
	 */
	public static void storeExpensePscoreToFile()
	{
		String strAlgorihtm = "RankBoost";
		ExperimentalResultFile.ExpenseAllObjectResult(true,strAlgorihtm, 
										expenseMap,vernumMap); //EXAM
		ExperimentalResultFile.PscoreAllObjectResult(true,strAlgorihtm
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
			String aggrProject = entry.getKey();
			List<String> items = entry.getValue();
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
