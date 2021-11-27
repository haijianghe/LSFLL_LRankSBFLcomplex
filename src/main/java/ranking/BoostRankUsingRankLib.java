/**
 * 
 */
package ranking;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import affiliated.AbstractProfileFile;
import affiliated.AffiliatedFactory;
import affiliated.ExcludeVersion;
import affiliated.IFaultFile;
import ciir.umass.edu.eval.Evaluator;
import common.EvaluatePerformanceSBFL;
import common.ProjectConfiguration;
import common.XMLConfigFile;
import sbflMetrics.WorstBestMean;

/** 调用RankLib实现的AdaRank
 * @author Administrator
 *
 */
public class BoostRankUsingRankLib {
	
	public BoostRankUsingRankLib()
	{
		
	}
	
	/**
	 * 使用31个SBFL传统算法为特征，另外14个代码复杂度特征。
	 * 调用RankLib的RankBoost学习模型，并计算新算法的性能。
	 */
	public void evaluateExperiment()
	{
		//训练模型。
		trainModel();		
		//运用训练模型，计算相应的项目的排序分值。注意：每个项目将计算三次（共有10折）。
		cacluateScoreByModel();
		//计算Learing to Rank 算法的定位效果
		List<String> allObjectNames = XMLConfigFile.getAllObjectNames();
		for( String project : allObjectNames )
			EvaluatePerformanceLTR.evaluateShowOne(project);
		EvaluatePerformanceLTR.aggregateSIRProjectSiemensUnix();
		EvaluatePerformanceLTR.storeExpensePscoreToFile();
	}
	
	/**
	 * 10折，训练模型，并存储模型文件。
	 */
	private void trainModel()
	{
		for( int fold=1; fold<=10;fold++ )
		{
			File dataFile = new File(ProjectConfiguration.PathLineLtoRankTrainFearture+"\\Fold" +
										String.valueOf(fold)+".train2"); //.train2
			File validateFile = new File(ProjectConfiguration.PathLineLtoRankTrainFearture+"\\Fold" +
					String.valueOf(fold)+".validate2");
			File modelFile  = new File(ProjectConfiguration.PathLineLtoRankTrainFearture+"\\Fold" +
										String.valueOf(fold)+".model");
			Evaluator.main(new String[] {
		            "-train", dataFile.getPath(),
		            "-metric2t", "map",
		            "-ranker", "2",  //2=RankBoost,1=RankNet
					//"-round","300",  //default=300		
		            //"-tvs","0.5",
		            //"-kcv","5",
		            "-norm","linear",
		            "-validate",validateFile.getPath(),
		            //特征选择文件，可以用来检查各个特征对模型的影响。
		            "-feature",ProjectConfiguration.PathLineLtoRankTrainFearture+"\\feature4.txt",
		            "-save", modelFile.getPath()
		            });
		}//end of for...
	}
	
	/**
	 * 使用训练出的模型计算所有测试集的排序分值。
	 * 由于用的7:3比例的训练集：测试集；每个project将会计算三次。最终结果要除以3.
	 */
	private void cacluateScoreByModel()
	{
		for( int fold=1; fold<=10;fold++ )
		{
			File modelFile  = new File(ProjectConfiguration.PathLineLtoRankTrainFearture+"\\Fold" +
										String.valueOf(fold)+".model");
			String modelFilename = modelFile.getPath();
			for( int k=7;k<10;k++ )
			{
				String[] projects = RankLibFeatrure.getProjectSubgroup((fold-1+k)%10);
				for( String project: projects )
					verCacluateScoreByModel(modelFilename,fold,project);
			}//end of for( int k=7
		}//end of for( int fold=1; fold<=10;fold++ )		
	}
	
	/**  单个项目，运用训练好的模型，计算排序的分值。
	 * @param modelFilename  模型文件
	 * @param fold           第几折 1.2.3,...10
	 * @param project        项目名称
	 */
	private void verCacluateScoreByModel(String modelFilename,int fold,String project)
	{
		int[] inBugids = AbstractProfileFile.getInclusionBugId(project);

		for( int index=1;index<=inBugids.length; index++)
		{
			int bugId = inBugids[index-1];
			File rankerFile  = new File(ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+
					project+"\\"+project+"_v"+String.valueOf(bugId)+".testing");
			File scoreFile  = new File(ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+
					project+"\\"+project+"_v"+String.valueOf(bugId)+"_F"+String.valueOf(fold)+".score");
			Evaluator.main(new String[] {
			        "-norm","linear",
			        "-load", modelFilename,
			        "-rank",rankerFile.getPath(),
			        "-score",scoreFile.getPath()});
		}//end of for...
	}
}
