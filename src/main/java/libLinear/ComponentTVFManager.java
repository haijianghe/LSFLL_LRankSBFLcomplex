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
	protected  Map<String,List<FeatureOfProjectVer>> testingProjectFeature; //������Ŀ��.testing�ļ���������������䡣
	/*�� LtoRUsingLibLinearRankSVM ��˵��testingResults������WorstBestMean
	 * �� RandomProjectEvaluateLibLinear ��˵��testingResults�����WorstBestMean������ȷ������Ϊ��ȡ����Ĳ��ԡ�*/
	protected  Map<String,List<WorstBestMean>> testingResults; //������Ŀ���ж�β�ͬģ�͵Ĳ�������
	protected  Map<String,int[]> verListMap; //������Ŀ�İ汾�б�
	////�ر�ע�⣺ featureIndexsһ��Ҫ����˳���š�
	protected int[] featureIndexs; //��ǰ������ѡ�񣬼�����֤���Ͳ��Լ������ܶ�Ҫ�õ���

	protected ComponentTVFManager()
	{
		testingProjectFeature = new HashMap<>();
		testingResults = new HashMap<>();
		verListMap = new HashMap<>();
		featureIndexs = null;
	}
	
	/** ָ��ɸѡ���������1��45.
	 * @param fis ������֤���Ͳ��Լ������ܶ�Ҫ�õ���
	 */
	public void setFeatureIndexs(int[] fis)
	{
		featureIndexs = fis;
	}

	
	/** ����Ŀ���ƺͰ汾���ҵ�FeatureOfProjectVer����
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
	 * �����Լ�����Ŀ���������ļ�����������һ���Զ����ڴ�
	 * ������Ŀ�İ汾�б�һ���Զ��롣
	 */
	public void readTestingFeatureFileToMemeory()
	{
		List<String> allObjectNames = XMLConfigFile.getAllObjectNames();
		for( String project : allObjectNames )
		{
			List<FeatureOfProjectVer> fopvLst = new ArrayList<>();
			int[] inBugids = AbstractProfileFile.getInclusionBugId(project);
			verListMap.put(project, inBugids); //һ���Զ���������Ŀ�İ汾�б�
			for( int kid=1; kid<=inBugids.length;kid++ )
			{
				FeatureOfProjectVer fopv = new FeatureOfProjectVer(project,inBugids[kid-1]);
				fopv.readTrainTestingFile(true);
				fopvLst.add(fopv);
			}//end of for...
			testingProjectFeature.put(project, fopvLst);
			//ͬʱ����ʼ��testingResults
			List<WorstBestMean> wmLst = new ArrayList<>();
			testingResults.put(project, wmLst);
		}//end of for...
	}
	
	/**  ��Ȩƽ��
	 * ����vtProjects������Ŀ���а汾��ƽ�� ����λ ����
	 * @param vtProjects
	 * @param wModel LibLinearѵ������ģ�͡�
	 * @return
	 */
	protected float calculateFaultLocalizationPerformance(List<String> vtProjects,double[] wModel)
	{
		float optimizationGoals = 0;
		int totalVers = 0;//�汾����
		for( String project : vtProjects )
		{
			WorstBestMean wbm = averageProjectPerformance(AbstractMetricMethod.ExpenseName,project,wModel);
			//��ȡ����ѧϰ�㷨�����Ż�Ŀ�ꡣ
			int ver = verListMap.get(project).length; 
			int optimizationObjective = ComponentLibLinearRankSVM.getOptimizationObjective();
			if( 2==optimizationObjective )
				optimizationGoals += wbm.fMeanIcp*ver;
			else if( 1==optimizationObjective )
				optimizationGoals += wbm.fWorstIcp*ver; //����������Ч����
			else
			{}
			totalVers += ver;
		}
		return optimizationGoals/totalVers;
	}
	
	/** ���㵥����Ŀ��ƽ�����ܡ�
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
				pStatement[k] = k+1; //.score�ļ�����кš�
			//find the most suspiciousness,return Max(Suspicious) and his code line no.
			double[] maxFaultSuspi = {0.0}; //max suspiciousness in fault code line.
			getMaxSuspiFaultLine(faultIndexs,scoreStmts,pStatement,maxFaultSuspi);
			//ע�⣬faultIndexs��ֵ��1��ʼ�����Ǵ�0��ʼ��
			IMetricMethod esMetric = MethodStrategyComplexFactory.createMetricMethodObject(strMetric, faultIndexs);
			esMetric.calculateWorstBestMean(scoreStmts,pStatement,maxFaultSuspi[0]);
			wbsList.add(esMetric.getWorstBestMeanResult());
		}
		return WorstBestMean.averageExamAndPscore(wbsList);
	}
	
	/** ��ģ������Ŀproject������Ĵ���λ���ܽ������testingResults
	 * @param project
	 * @param wModel ģ��
	 */
	protected void addModelResultToTesting(String project,double[] wModel)
	{
		WorstBestMean wbm = averageProjectPerformance(AbstractMetricMethod.ExpenseName,project,wModel);
		WorstBestMean pScore = averageProjectPerformance(AbstractMetricMethod.PScore,project,wModel);
		wbm.copyPscoreResult(pScore.probBugver); //ExpenseName��PScoreƴ�ӵ�һ����
		List<WorstBestMean> wbmLst = testingResults.get(project);
		wbmLst.add(wbm);
	}
	
	/** Check nTxtno is or not fault code line.
	 * @param nTxtno �������У����к�(����Դ������кţ�����.score�ļ�����к�)
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
	
	/** �����������ж������ҳ�����֮�п��ɶ����ֵ��
	 * find the most suspiciousness,return Max(Suspicious) and his code line no.
	 * if two fault have same Suspicious,then return the mini line no.
	 * @param pSuspicious ���������˳�򣬿��ɶȣ�learning to rank �����ֵ
	 * @param pTxtno  .score�ļ����кţ���pSuspicious˳����ͬ����1��ʼ
	 * @param maxFaultSuspi �����ɶ�ֵ
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
	 *  ��threeTestingResultȡƽ��ֵ�����浽�ļ���
	 */
	protected void evaluatePerformance(String strAlgorihtm)
	{
		Map<String,WorstBestMean> expenseMap = new HashMap<>(); //EXAM���ۺ�ǰ��
		Map<String,float[]> pScoreMap = new HashMap<>(); //Pscore,Pckheck,Pbugver���ۺ�ǰ��
		Map<String,Integer> vernumMap = new HashMap<>(); //�汾������Ϊ�ۺ���ƽ��ֵ��

		//�����Ժ������ֵ��ƽ�����ٴ���HashMap
		List<String> allObjectNames = XMLConfigFile.getAllObjectNames();
		for( String project : allObjectNames )
		{
			List<WorstBestMean> wbmLst = testingResults.get(project);
			if( wbmLst.size()<=0 )
			{
				WorstBestMean wbm = new WorstBestMean();
				wbm.setZero(); //��Ϊ0�����˹���顣
				expenseMap.put(project, wbm);
				pScoreMap.put(project, wbm.probBugver);
			}
			else {
				//�������ѡ��һ����Ϊ���Լ���������ƽ��ֵ��
				WorstBestMean wbm = WorstBestMean.averageExam(wbmLst);
				expenseMap.put(project, wbm);
				float[] pScore = WorstBestMean.averagePscore(wbmLst);
				pScoreMap.put(project, pScore);
			}
			int[] bugs = verListMap.get(project);
			vernumMap.put(project, bugs.length);
		}
		//�Ⱦۺ�,ע�⣺������ۺϣ��ֹ�ȥ�ۺϡ�
		//aggregateSIRProjectSiemensUnix(expenseMap,pScoreMap,vernumMap);
		//�ٱ�����
		ExperimentalResultFile.ExpenseAllObjectResult(false,strAlgorihtm, //���ۺ� 
										expenseMap,vernumMap); //EXAM
		ExperimentalResultFile.PscoreAllObjectResult(false,strAlgorihtm  //���ۺ� 
										, pScoreMap,vernumMap); //Pbugver
	}
	
	/**
	 * �ۺ� Siemens gzip grep sed��SIR����
	 */
	protected static void aggregateSIRProjectSiemensUnix(Map<String,WorstBestMean> expenseMap,
							Map<String,float[]> pScoreMap,Map<String,Integer> vernumMap)
	{
		Map<String,List<String>> aggregations =XMLConfigFile.getAggregations();
		for( Map.Entry<String, List<String>>  entry  :  aggregations.entrySet()){
			//�ҳ��ۺϵĶ�����������
			String aggrProject = entry.getKey();
			List<String> items = entry.getValue();
			//��EXAM & Pscore HashMap��ȡ����Щ����Ľ������ɾ����Щ���
			List<WorstBestMean> expenseResults = new ArrayList<>();
			List<float[]> pscoreResults = new ArrayList<>();
			List<Integer> versions = new ArrayList<>();//�汾������������ƽ��ֵ
			int aggrVers = 0; //�ۺϺ�İ汾������
			for( String project : items )
			{
				WorstBestMean expenseMetric = expenseMap.get(project); //EXAM
				expenseMap.remove(project);
				expenseResults.add(expenseMetric);
				float[] pscoreMetric =  pScoreMap.get(project); //Pscore
				pScoreMap.remove(project);
				pscoreResults.add(pscoreMetric);
				int bugs = vernumMap.get(project);
				//��ǰproject�İ汾����ɾ��
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

}
