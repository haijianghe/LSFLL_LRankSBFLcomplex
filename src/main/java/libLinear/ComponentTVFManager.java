/**
 * 
 */
package libLinear;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import affiliated.AbstractProfileFile;
import common.ExperimentalResultFile;
import common.XMLConfigFile;
import sbflMetrics.AbstractMetricMethod;
import sbflMetrics.IMetricMethod;
import sbflMetrics.MethodStrategyComplexFactory;
import sbflMetrics.WorstBestMean;

/**
 * @author Administrator
 *
 */
public class ComponentTVFManager {
	protected  Map<String,List<FeatureOfProjectVer>> testingProjectFeature; //所有项目的.testing文件里特征及故障语句。
	/*对 LtoRUsingLibLinearRankSVM 来说，testingResults存三个WorstBestMean
	 * 对 RandomProjectEvaluateLibLinear 来说，testingResults里面的WorstBestMean个数不确定，因为采取随机的策略。*/
	protected  Map<String,List<WorstBestMean>> testingResults; //所有项目都有多次不同模型的测试性能
	protected  Map<String,int[]> verListMap; //所有项目的版本列表。
	////特别注意： featureIndexs一定要按照顺序存放。
	protected int[] featureIndexs; //当前的特征选择，计算验证集和测试集的性能都要用到。

	protected ComponentTVFManager()
	{
		testingProjectFeature = new HashMap<>();
		testingResults = new HashMap<>();
		verListMap = new HashMap<>();
		featureIndexs = null;
	}
	
	/** 指定筛选的特征项，从1到45.
	 * @param fis 计算验证集和测试集的性能都要用到。
	 */
	public void setFeatureIndexs(int[] fis)
	{
		featureIndexs = fis;
	}

	
	/** 由项目名称和版本号找到FeatureOfProjectVer对象
	 * @param project
	 * @param bugId
	 * @return
	 */
	protected FeatureOfProjectVer getProjectVer(String project,int bugId)
	{
		FeatureOfProjectVer fopv = null;
		List<FeatureOfProjectVer> fopVers = testingProjectFeature.get(project);
		for( FeatureOfProjectVer item: fopVers )
			if( item.getBugId()==bugId )
			{
				fopv = item;
				break;
			}
		return fopv;
	}
	
	/**
	 * 将测试集里项目，其特征文件的特征向量一次性读入内存
	 * 所有项目的版本列表一次性读入。
	 */
	public void readTestingFeatureFileToMemeory()
	{
		List<String> allObjectNames = XMLConfigFile.getAllObjectNames();
		for( String project : allObjectNames )
		{
			List<FeatureOfProjectVer> fopvLst = new ArrayList<>();
			int[] inBugids = AbstractProfileFile.getInclusionBugId(project);
			verListMap.put(project, inBugids); //一次性读入所有项目的版本列表
			for( int kid=1; kid<=inBugids.length;kid++ )
			{
				FeatureOfProjectVer fopv = new FeatureOfProjectVer(project,inBugids[kid-1]);
				fopv.readTrainTestingFile(true);
				fopvLst.add(fopv);
			}//end of for...
			testingProjectFeature.put(project, fopvLst);
			//同时，初始化testingResults
			List<WorstBestMean> wmLst = new ArrayList<>();
			testingResults.put(project, wmLst);
		}//end of for...
	}
	
	/**  加权平均
	 * 计算vtProjects所有项目所有版本的平均 错误定位 性能
	 * @param vtProjects
	 * @param wModel LibLinear训练出的模型。
	 * @return
	 */
	protected float calculateFaultLocalizationPerformance(List<String> vtProjects,double[] wModel)
	{
		float optimizationGoals = 0;
		int totalVers = 0;//版本总数
		for( String project : vtProjects )
		{
			WorstBestMean wbm = averageProjectPerformance(AbstractMetricMethod.ExpenseName,project,wModel);
			//获取机器学习算法的最优化目标。
			int ver = verListMap.get(project).length; 
			int optimizationObjective = ComponentLibLinearRankSVM.getOptimizationObjective();
			if( 2==optimizationObjective )
				optimizationGoals += wbm.fMeanIcp*ver;
			else if( 1==optimizationObjective )
				optimizationGoals += wbm.fWorstIcp*ver; //看看这样的效果。
			else
			{}
			totalVers += ver;
		}
		return optimizationGoals/totalVers;
	}
	
	/** 计算单个项目的平均性能。
	 * @param strMetric
	 * @param project
	 * @param wModel
	 * @return
	 */
	protected WorstBestMean averageProjectPerformance(String strMetric,String project,double[] wModel)
	{
		List<WorstBestMean> wbsList = new ArrayList<>(); 
		int[] vers = verListMap.get(project);
		for( int i=0;i<vers.length;i++ )
		{ 
			int bugId = vers[i];
			FeatureOfProjectVer fopv = getProjectVer(project,bugId);
			double[] scoreStmts = fopv.computerScore(wModel,featureIndexs);
			int[] faultIndexs = fopv.getFaultIndex();
			int totalExec = scoreStmts.length;
			int[] pStatement = new int[totalExec]; //the pointer of the number of statement
			for(int k=0;k<totalExec;k++)
				pStatement[k] = k+1; //.score文件里的行号。
			//find the most suspiciousness,return Max(Suspicious) and his code line no.
			double[] maxFaultSuspi = {0.0}; //max suspiciousness in fault code line.
			getMaxSuspiFaultLine(faultIndexs,scoreStmts,pStatement,maxFaultSuspi);
			//注意，faultIndexs的值从1开始，并非从0开始。
			IMetricMethod esMetric = MethodStrategyComplexFactory.createMetricMethodObject(strMetric, faultIndexs);
			esMetric.calculateWorstBestMean(scoreStmts,pStatement,maxFaultSuspi[0]);
			wbsList.add(esMetric.getWorstBestMeanResult());
		}
		return WorstBestMean.averageExamAndPscore(wbsList);
	}
	
	/** 将模型在项目project计算出的错误定位性能结果存入testingResults
	 * @param project
	 * @param wModel 模型
	 */
	protected void addModelResultToTesting(String project,double[] wModel)
	{
		WorstBestMean wbm = averageProjectPerformance(AbstractMetricMethod.ExpenseName,project,wModel);
		WorstBestMean pScore = averageProjectPerformance(AbstractMetricMethod.PScore,project,wModel);
		wbm.copyPscoreResult(pScore.probBugver); //ExpenseName和PScore拼接到一个。
		List<WorstBestMean> wbmLst = testingResults.get(project);
		wbmLst.add(wbm);
	}
	
	/** Check nTxtno is or not fault code line.
	 * @param nTxtno 待检查的行，其行号(并非源代码的行号，而是.score文件里的行号)
	 * @return return TRUE: is fault; else is not fault.
	 */
	private boolean isFaultCode(int[] faultIndexs,int nTxtno)
	{
		boolean bFault = false;
		//judge this is a fault line.
		for( int item : faultIndexs )
		{
			if( nTxtno==item )
			{
				bFault = true;
				break;
			}
		}
		return bFault;
	}
	
	/** 故障语句可能有多条，找出他们之中可疑度最大值。
	 * find the most suspiciousness,return Max(Suspicious) and his code line no.
	 * if two fault have same Suspicious,then return the mini line no.
	 * @param pSuspicious 各语句依照顺序，可疑度，learning to rank 排序分值
	 * @param pTxtno  .score文件的行号，与pSuspicious顺序相同。从1开始
	 * @param maxFaultSuspi 最大可疑度值
	 */
	private void getMaxSuspiFaultLine(int[] faultIndexs,double[] pSuspicious,int[] pTxtno,double[] maxFaultSuspi)
	{
		//get the most suspiciousness
		double maxSuspi = -999999;
		for ( int i=0;i<pSuspicious.length;i++ )
		{
			if( false==isFaultCode(faultIndexs,pTxtno[i]) )//this line'code  is not a fault
				continue;
			if ( pSuspicious[i]>maxSuspi )
				maxSuspi = pSuspicious[i];
		}

		maxFaultSuspi[0] = maxSuspi;
	}
	
	/**
	 *  将threeTestingResult取平均值，保存到文件。
	 */
	protected void evaluatePerformance(String strAlgorihtm)
	{
		Map<String,WorstBestMean> expenseMap = new HashMap<>(); //EXAM，聚合前。
		Map<String,float[]> pScoreMap = new HashMap<>(); //Pscore,Pckheck,Pbugver，聚合前。
		Map<String,Integer> vernumMap = new HashMap<>(); //版本个数，为聚合求平均值。

		//将测试后的三个值先平均，再存入HashMap
		List<String> allObjectNames = XMLConfigFile.getAllObjectNames();
		for( String project : allObjectNames )
		{
			List<WorstBestMean> wbmLst = testingResults.get(project);
			if( wbmLst.size()<=0 )
			{
				WorstBestMean wbm = new WorstBestMean();
				wbm.setZero(); //设为0，由人工检查。
				expenseMap.put(project, wbm);
				pScoreMap.put(project, wbm.probBugver);
			}
			else {
				//至少随机选中一次作为测试集，才能求平均值。
				WorstBestMean wbm = WorstBestMean.averageExam(wbmLst);
				expenseMap.put(project, wbm);
				float[] pScore = WorstBestMean.averagePscore(wbmLst);
				pScoreMap.put(project, pScore);
			}
			int[] bugs = verListMap.get(project);
			vernumMap.put(project, bugs.length);
		}
		//先聚合,注意：不允许聚合；手工去聚合。
		//aggregateSIRProjectSiemensUnix(expenseMap,pScoreMap,vernumMap);
		//再保存结果
		ExperimentalResultFile.ExpenseAllObjectResult(false,strAlgorihtm, //不聚合 
										expenseMap,vernumMap); //EXAM
		ExperimentalResultFile.PscoreAllObjectResult(false,strAlgorihtm  //不聚合 
										, pScoreMap,vernumMap); //Pbugver
	}
	
	/**
	 * 聚合 Siemens gzip grep sed等SIR对象。
	 */
	protected static void aggregateSIRProjectSiemensUnix(Map<String,WorstBestMean> expenseMap,
							Map<String,float[]> pScoreMap,Map<String,Integer> vernumMap)
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
				WorstBestMean expenseMetric = expenseMap.get(project); //EXAM
				expenseMap.remove(project);
				expenseResults.add(expenseMetric);
				float[] pscoreMetric =  pScoreMap.get(project); //Pscore
				pScoreMap.remove(project);
				pscoreResults.add(pscoreMetric);
				int bugs = vernumMap.get(project);
				//当前project的版本数可删除
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

}
