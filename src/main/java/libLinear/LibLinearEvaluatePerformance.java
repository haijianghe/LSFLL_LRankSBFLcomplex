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
import sbflMetrics.WorstBestMean;

/** 
 * ������������࣬�ظ��ˡ�
 * @author Administrator
 *
 */
public class LibLinearEvaluatePerformance {
	//�Զ���Ϊkey����������ƽ�� LTR : learning to rank ����
	private static Map<String,WorstBestMean> expenseMap = new HashMap<>(); //EXAM
	private static Map<String,float[]> pScoreMap = new HashMap<>(); //Pscore,Pckheck,Pbugver
	private static Map<String,Integer> vernumMap = new HashMap<>(); //�汾������Ϊ�ۺ���ƽ��ֵ��

	/** һ����������������ȣ�Pscore EXAM����
	 * @param objectName  
	 */
	public static void evaluateShowOne(String objectName)
	{
		//��ȡExpense�Ľ����
		evaluateExpenseShow(objectName);
		//��ȡPscoree�Ľ����
		evaluatePscoreShow(objectName);
	}
	
	/**Expense: һ��ֻ���� һ������Learning to Rank��
	 * @param objectName  ��������
	 */
	private static void evaluateExpenseShow(String objectName)
	{
		int[] inBugids = AbstractProfileFile.getInclusionBugId(objectName);

		List<WorstBestMean> wbsLst = new ArrayList<>();
		for( int index=1;index<=inBugids.length; index++)
		{
			int bugId = inBugids[index-1];
			LibLinearScorePerformanceAssess sbflm = new LibLinearScorePerformanceAssess(objectName,bugId);
			//=============  worst-best-mean  strategy
			WorstBestMean imsbwm = sbflm.calMultiModelPerformance(AbstractMetricMethod.ExpenseName);
			wbsLst.add((WorstBestMean)imsbwm);
		}
		//=============  worst-best-mean  strategy
		WorstBestMean wbmTmp = WorstBestMean.averageExam(wbsLst);
		System.out.println("Average of LTR by "+String.valueOf(wbsLst.size())+" vers:");
		wbmTmp.showExam();
		//=============����ʵ����
		expenseMap.put(objectName, wbmTmp);
		//============�汾������Ϊ�ۺ���ƽ��ֵ��
		vernumMap.put(objectName, wbsLst.size());
	}//end of evaluateOneMetricSBFL


	/**���ԣ�һ��ֻ����һ������Learning to Rank��
	 * ������SOS����
	 * @param objectName  ��������
	 */
	private static void evaluatePscoreShow(String objectName)
	{
		int[] inBugids = AbstractProfileFile.getInclusionBugId(objectName);

		List<WorstBestMean> wbsLst = new ArrayList<>(); // worst-best-mean  strategy
		for( int index=1;index<=inBugids.length; index++)
		{
			int bugId = inBugids[index-1];
			LibLinearScorePerformanceAssess sbflm = new LibLinearScorePerformanceAssess(objectName,bugId);
			//=============  worst-best-mean  strategy
			WorstBestMean imsbwm = sbflm.calMultiModelPerformance(AbstractMetricMethod.PScore);
			wbsLst.add(imsbwm);
		}
		//=============  worst-best-mean  strategy
		float[] meanBugver = WorstBestMean.averagePscore(wbsLst);
		WorstBestMean.showPscore(meanBugver);
		System.out.println("Object name is "+objectName+". Pscore is ok.");
		//=============����ʵ����
		pScoreMap.put(objectName, meanBugver);
	}//end of evaluateOneMetricSBFL
	

	/**
	 * ʵ���������ļ����ļ�����strAlgorihtm����
	 *  bAggregation = false,���ۺ�  =true,�ۺ�
	 */
	public static void storeExpensePscoreToFile()
	{
		String strAlgorihtm = "LibLinear";
		ExperimentalResultFile.ExpenseAllObjectResult(true,strAlgorihtm, 
										expenseMap,vernumMap); //EXAM
		ExperimentalResultFile.PscoreAllObjectResult(true,strAlgorihtm
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
			String aggrProject = entry.getKey();
			List<String> items = entry.getValue();
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
