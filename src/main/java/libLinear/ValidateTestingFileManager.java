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
import ranking.RankLibFeatrure;
import sbflMetrics.AbstractMetricMethod;
import sbflMetrics.IMetricMethod;
import sbflMetrics.MethodStrategyComplexFactory;
import sbflMetrics.WorstBestMean;

/**  ��LtoRUsingLibLinearRankSVMʹ�á� 
 * ģ�͵���֤����ģ�͵Ĳ��Խ������Щ���������ڴ����С� �ڴ����������ٶȡ�
 * �ڴ��д�������͹�����䡣
 * @author Administrator
 *
 */
public class ValidateTestingFileManager extends ComponentTVFManager {
	
	public ValidateTestingFileManager()
	{
	}
	
	
	/**  ѵ�������У���֤ĳ��ģ�͵����ܡ� LtoRUsingLibLinearRankSVM��ʹ�á�
	 * @param fold  ��һ�ۣ���ͬ��fold�в�ͬ����֤����Ŀ  1,2,3.....10
	 * @param wModel  LibLinearѵ������ģ�͡�
	 * @return
	 */
	public float validateFaultLocalizationPerformance(int fold,double[] wModel)
	{
		//validateProjects�����Ҫ��֤�����λ���ܣ�trainѵ������ģ�ͣ�����Ŀ
		List<String> validateProjects = new ArrayList<>();
		for( int t=4;t<=6;t++ )
		{
			String[] projects = RankLibFeatrure.getProjectSubgroup((fold-1+t)%10);
			for( String project: projects )
				validateProjects.add(project);
		}
		return calculateFaultLocalizationPerformance(validateProjects,wModel);
	}
	
	/**  ѵ�������󣬲���ĳ��ģ�͵����ܡ� LtoRUsingLibLinearRankSVM��ʹ�á�
	 * @param fold  ��һ�ۣ���ͬ��fold�в�ͬ����֤����Ŀ  1,2,3.....10
	 * @param wModel  LibLinearѵ������ģ�͡�
	 * @return
	 */
	public float testingFaultLocalizationPerformance(int fold,double[] wModel)
	{
		//testingProjects�����Ҫ���ղ��������λ���ܣ�trainѵ������ģ�ͣ�����Ŀ
		List<String> testingProjects = new ArrayList<>();
		for( int t=7;t<=9;t++ )
		{
			String[] projects = RankLibFeatrure.getProjectSubgroup((fold-1+t)%10);
			for( String project: projects )
				testingProjects.add(project);
		}
		return calculateFaultLocalizationPerformance(testingProjects,wModel);
	}
	
	/** ������ѵ������ģ�ͣ���������۶�Ӧ���Լ������ܣ���������
	 *     ÿ����ĿҪ�����Σ���Ϊʮ�۵�Ե��
	 * @param fold
	 * @param wModel
	 */
	public void recordTestingPerformanceBestModel(int fold,double[] wModel)
	{
		//testingProjects�����Ҫ���ղ��������λ���ܣ�trainѵ������ģ�ͣ�����Ŀ
		List<String> testingProjects = new ArrayList<>();
		for( int t=7;t<=9;t++ )
		{
			String[] projects = RankLibFeatrure.getProjectSubgroup((fold-1+t)%10);
			for( String project: projects )
			{
				testingProjects.add(project);
				addModelResultToTesting(project,wModel);
			}//end of for...
		}//end of for...
	}
}
