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

/** ����RankLibʵ�ֵ�AdaRank
 * @author Administrator
 *
 */
public class BoostRankUsingRankLib {
	
	public BoostRankUsingRankLib()
	{
		
	}
	
	/**
	 * ʹ��31��SBFL��ͳ�㷨Ϊ����������14�����븴�Ӷ�������
	 * ����RankLib��RankBoostѧϰģ�ͣ����������㷨�����ܡ�
	 */
	public void evaluateExperiment()
	{
		//ѵ��ģ�͡�
		trainModel();		
		//����ѵ��ģ�ͣ�������Ӧ����Ŀ�������ֵ��ע�⣺ÿ����Ŀ���������Σ�����10�ۣ���
		cacluateScoreByModel();
		//����Learing to Rank �㷨�Ķ�λЧ��
		List<String> allObjectNames = XMLConfigFile.getAllObjectNames();
		for( String project : allObjectNames )
			EvaluatePerformanceLTR.evaluateShowOne(project);
		EvaluatePerformanceLTR.aggregateSIRProjectSiemensUnix();
		EvaluatePerformanceLTR.storeExpensePscoreToFile();
	}
	
	/**
	 * 10�ۣ�ѵ��ģ�ͣ����洢ģ���ļ���
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
		            //����ѡ���ļ�����������������������ģ�͵�Ӱ�졣
		            "-feature",ProjectConfiguration.PathLineLtoRankTrainFearture+"\\feature4.txt",
		            "-save", modelFile.getPath()
		            });
		}//end of for...
	}
	
	/**
	 * ʹ��ѵ������ģ�ͼ������в��Լ��������ֵ��
	 * �����õ�7:3������ѵ���������Լ���ÿ��project����������Ρ����ս��Ҫ����3.
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
	
	/**  ������Ŀ������ѵ���õ�ģ�ͣ���������ķ�ֵ��
	 * @param modelFilename  ģ���ļ�
	 * @param fold           �ڼ��� 1.2.3,...10
	 * @param project        ��Ŀ����
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
