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

/**  供LtoRUsingLibLinearRankSVM使用。 
 * 模型的验证，及模型的测试结果，这些操作都放在此类中。 内存操作，提高速度。
 * 内存中存放特征和故障语句。
 * @author Administrator
 *
 */
public class ValidateTestingFileManager extends ComponentTVFManager {
	
	public ValidateTestingFileManager()
	{
	}
	
	
	/**  训练过程中，验证某个模型的性能。 LtoRUsingLibLinearRankSVM类使用。
	 * @param fold  哪一折，不同的fold有不同的验证集项目  1,2,3.....10
	 * @param wModel  LibLinear训练出的模型。
	 * @return
	 */
	public float validateFaultLocalizationPerformance(int fold,double[] wModel)
	{
		//validateProjects存放需要验证其错误定位性能（train训练出的模型）的项目
		List<String> validateProjects = new ArrayList<>();
		for( int t=4;t<=6;t++ )
		{
			String[] projects = RankLibFeatrure.getProjectSubgroup((fold-1+t)%10);
			for( String project: projects )
				validateProjects.add(project);
		}
		return calculateFaultLocalizationPerformance(validateProjects,wModel);
	}
	
	/**  训练结束后，测试某个模型的性能。 LtoRUsingLibLinearRankSVM类使用。
	 * @param fold  哪一折，不同的fold有不同的验证集项目  1,2,3.....10
	 * @param wModel  LibLinear训练出的模型。
	 * @return
	 */
	public float testingFaultLocalizationPerformance(int fold,double[] wModel)
	{
		//testingProjects存放需要最终测试其错误定位性能（train训练出的模型）的项目
		List<String> testingProjects = new ArrayList<>();
		for( int t=7;t<=9;t++ )
		{
			String[] projects = RankLibFeatrure.getProjectSubgroup((fold-1+t)%10);
			for( String project: projects )
				testingProjects.add(project);
		}
		return calculateFaultLocalizationPerformance(testingProjects,wModel);
	}
	
	/** 将该折训练出的模型，计算出该折对应测试集的性能，存起来，
	 *     每个项目要存三次，因为十折的缘故
	 * @param fold
	 * @param wModel
	 */
	public void recordTestingPerformanceBestModel(int fold,double[] wModel)
	{
		//testingProjects存放需要最终测试其错误定位性能（train训练出的模型）的项目
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
