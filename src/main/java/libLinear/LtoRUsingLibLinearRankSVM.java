/**
 * 
 */
package libLinear;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import affiliated.AbstractProfileFile;
import ciir.umass.edu.eval.Evaluator;
import common.ProjectConfiguration;
import common.XMLConfigFile;
import ranking.RankLibFeatrure;
import sbflMetrics.AbstractMetricMethod;
import sbflMetrics.WorstBestMean;

/**
 * @author Administrator
 *
 */
public class LtoRUsingLibLinearRankSVM extends ComponentLibLinearRankSVM {
	
	public LtoRUsingLibLinearRankSVM()
	{
	}
	
	
	/**
	 * 依据featureScreenFilename指定的特征筛选标准，重新生成新的训练、验证、测试数据集。
	 */
	private void adjustFeatureFile()
	{
		//训练集
		for( int fold=1;fold<=10;fold++ )
		{
			String trainFile = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\Fold"+
									String.valueOf(fold)+".train2";
			String newTrainFile = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\LibLinear_Fold"+
											String.valueOf(fold)+".train";
			adjustFeatureFileAfterScreen(trainFile,newTrainFile);
		}
		//所有项目都会成为验证集和测试集,
		for( int k=0;k<10;k++ )
		{
			String[] projects = RankLibFeatrure.getProjectSubgroup(k);
			for( String project: projects )
			{
				int[] inBugids = AbstractProfileFile.getInclusionBugId(project);
				for( int index=1;index<=inBugids.length; index++)
				{
					int bugId = inBugids[index-1];
					String testingFilename = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+
							project+"\\"+project+"_v"+String.valueOf(bugId)+".testing";
					String newTestingFilename = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+
							project+"\\LibLinear_"+project+"_v"+String.valueOf(bugId)+".testing";
					adjustFeatureFileAfterScreen(testingFilename,newTestingFilename);
				}
			}//end of for( String project: projects )
		}//end of for( int k=0;k<10;k++ )
	}
	
	/**
	 * 使用31个SBFL传统算法为特征，另外14个代码复杂度特征。
	 * 调用RankLib的RankBoost学习模型，并计算新算法的性能。
	 */
	public void evaluateExperiment()
	{
		readFeatureScreen();
		adjustFeatureFile();
		//训练模型。并计算相应的项目的排序分值。注意：每个项目将计算三次（共有10折）。
		trainModelCacluateScoreByModel2();		
	}

	
	/** 计算cflpProjects的所有项目，其所有版本的平均性能，在模型modelFilename下。
	 * @param cflpProjects
	 * @param modelFilename
	 * @return
	 */
	private float calculateFaultLocalizationPerformance(List<String> cflpProjects,String modelFilename)
	{
		float cflp = 0.0f; //EXAM值总数，将来计算平均值
		int vers = 0;//版本总数
		for( String  project : cflpProjects )
		{
			int[] inBugids = AbstractProfileFile.getInclusionBugId(project);
			for( int index=1;index<=inBugids.length; index++)
			{
				int bugId = inBugids[index-1];
				String cflpFilename = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\" + project+
							"\\LibLinear_"+project+"_v"+String.valueOf(bugId)+".testing";
				String scoreFilename = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\" + project+
						"\\"+project+"_v"+String.valueOf(bugId)+".score";
				validateTest(modelFilename,cflpFilename,scoreFilename);
				LibLinearScorePerformanceAssess llspa = new LibLinearScorePerformanceAssess(project,bugId);
				WorstBestMean wbm = llspa.calPerformance(AbstractMetricMethod.ExpenseName);
				cflp += wbm.fMeanIcp;
			}
			vers += inBugids.length;
		}	
		return cflp/vers;
	}
	
	/**让最优C值对应的模型重新产生一次.score文件，后面要用到；将来要求三次模型的平均值。
	 * @param cflpProjects
	 * @param fold
	 */
	private void makeScoreFileUsingBestModel(List<String> cflpProjects,String modelFilename,int fold)
	{
		for( String  project : cflpProjects )
		{
			int[] inBugids = AbstractProfileFile.getInclusionBugId(project);
			for( int index=1;index<=inBugids.length; index++)
			{
				int bugId = inBugids[index-1];
				String cflpFilename = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\" + project+
						"\\LibLinear_"+project+"_v"+String.valueOf(bugId)+".testing";
				String scoreFilename = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\" + project+
						"\\"+project+"_v"+String.valueOf(bugId)+"_F"+fold+".score";
				validateTest(modelFilename,cflpFilename,scoreFilename);
			}
		}
	}
	
	/**
	 * 10折，训练模型，并存储模型文件。
	 * 调用 predict.exe
	 */
	private void trainModelCacluateScoreByModel()
	{
		for( int fold=1; fold<=10;fold++ )
		{
			//validateProjects存放需要验证其错误定位性能（train训练出的模型）的项目
			List<String> validateProjects = new ArrayList<>();
			for( int t=4;t<=6;t++ )
			{
				String[] projects = RankLibFeatrure.getProjectSubgroup((fold-1+t)%10);
				for( String project: projects )
					validateProjects.add(project);
			}
			String trainFilename = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\LibLinear_Fold" +
					String.valueOf(fold)+".train"; //.train
			double c = 1e-8; //RankSVM 参数C
			//记录每个c产生的定位性能结果
			float resultOfModel[]= {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int cnums = 20; //参数c的个数。
			//参数c 从1000到0.001
			for( int cp=1;cp<=cnums;cp++ )
			{
				String modelFilename  = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\Fold" +
											String.valueOf(fold)+"_C"+String.valueOf(cp)+".model";
				train(trainFilename,modelFilename,c);
				resultOfModel[cp-1] = calculateFaultLocalizationPerformance(validateProjects,modelFilename);
				System.out.print(c+":"+resultOfModel[cp-1]+",");
				c = c/3;
			}//end of for( int cp=1;cp<=7;cp++ )
			int bestCp = getBestCp(resultOfModel,cnums);//resultOfModel里最小值的索引。
			System.out.println(" ");
			System.out.println("Fold"+fold+" bestCp="+bestCp);
			String bestModelFilename  = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\Fold" +
					String.valueOf(fold)+"_C"+String.valueOf(bestCp+1)+".model";
			//让最优C值对应的模型重新产生一次.score文件，后面要用到；将来要求三次模型的平均值。
			//testingProjects存放需要最终测试其错误定位性能（train训练出的模型）的项目
			List<String> testingProjects = new ArrayList<>();
			for( int t=7;t<=9;t++ )
			{
				String[] projects = RankLibFeatrure.getProjectSubgroup((fold-1+t)%10);
				for( String project: projects )
					testingProjects.add(project);
			}
			makeScoreFileUsingBestModel( testingProjects,bestModelFilename,fold);
		}//end of for...	
	}

	
	/**
	 * 10折，训练模型，并存储模型文件。 
	 * 与trainModelCacluateScoreByModel不同，自己读取模型，由内存模型计算排序分值。
	 * 自己的代码计算Ranker，不用 Predict.exe
	 */
	private void trainModelCacluateScoreByModel2()
	{
		//一次性将特征文件读入内存。避免重复IO操作，提高效率。
		ValidateTestingFileManager vtfmMemory = new ValidateTestingFileManager();
		vtfmMemory.readTestingFeatureFileToMemeory();
		
		for( int fold=1; fold<=10;fold++ )
		{
			String trainFilename = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\LibLinear_Fold" +
					String.valueOf(fold)+".train"; //.train
			double CCC = 1e-5; //RankSVM 参数C
			 //记录每个cp产生的定位性能结果
			float resultOfModel[]= {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			List<double[]> allModelW = new ArrayList<>(); //存储训练出的模型
			int cnums = 30; //参数c的个数。
			for( int cp=1;cp<=cnums;cp++ )
			{
				String modelFilename  = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\Fold" +
											String.valueOf(fold)+"_C"+String.valueOf(cp)+".model";
				train(trainFilename,modelFilename,CCC);
				double[] wModel = readModel(modelFilename);
				allModelW.add(wModel);
				resultOfModel[cp-1] = vtfmMemory.validateFaultLocalizationPerformance(fold,wModel);
				System.out.print(CCC+":"+resultOfModel[cp-1]+",");
				CCC = CCC/2.5;
			}//end of for( int cp=1;cp<= ;cp++ )
			int bestCp = getBestCp(resultOfModel,cnums);//resultOfModel里最小值的索引。
			System.out.println(" ");
			System.out.println("Fold"+fold+" bestCp="+(bestCp+1)+"  : "+resultOfModel[bestCp]);
			//让最优C值对应的模型性能记录下来，后面要用到；将来要求三次模型的平均值。
			vtfmMemory.recordTestingPerformanceBestModel( fold,allModelW.get(bestCp) );
		}//end of for...	
		//计算Learing to Rank 算法的定位效果
		vtfmMemory.evaluatePerformance("LibLinear");
	}
}
