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
	 * ����featureScreenFilenameָ��������ɸѡ��׼�����������µ�ѵ������֤���������ݼ���
	 */
	private void adjustFeatureFile()
	{
		//ѵ����
		for( int fold=1;fold<=10;fold++ )
		{
			String trainFile = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\Fold"+
									String.valueOf(fold)+".train2";
			String newTrainFile = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\LibLinear_Fold"+
											String.valueOf(fold)+".train";
			adjustFeatureFileAfterScreen(trainFile,newTrainFile);
		}
		//������Ŀ�����Ϊ��֤���Ͳ��Լ�,
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
	 * ʹ��31��SBFL��ͳ�㷨Ϊ����������14�����븴�Ӷ�������
	 * ����RankLib��RankBoostѧϰģ�ͣ����������㷨�����ܡ�
	 */
	public void evaluateExperiment()
	{
		readFeatureScreen();
		adjustFeatureFile();
		//ѵ��ģ�͡���������Ӧ����Ŀ�������ֵ��ע�⣺ÿ����Ŀ���������Σ�����10�ۣ���
		trainModelCacluateScoreByModel2();		
	}

	
	/** ����cflpProjects��������Ŀ�������а汾��ƽ�����ܣ���ģ��modelFilename�¡�
	 * @param cflpProjects
	 * @param modelFilename
	 * @return
	 */
	private float calculateFaultLocalizationPerformance(List<String> cflpProjects,String modelFilename)
	{
		float cflp = 0.0f; //EXAMֵ��������������ƽ��ֵ
		int vers = 0;//�汾����
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
	
	/**������Cֵ��Ӧ��ģ�����²���һ��.score�ļ�������Ҫ�õ�������Ҫ������ģ�͵�ƽ��ֵ��
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
	 * 10�ۣ�ѵ��ģ�ͣ����洢ģ���ļ���
	 * ���� predict.exe
	 */
	private void trainModelCacluateScoreByModel()
	{
		for( int fold=1; fold<=10;fold++ )
		{
			//validateProjects�����Ҫ��֤�����λ���ܣ�trainѵ������ģ�ͣ�����Ŀ
			List<String> validateProjects = new ArrayList<>();
			for( int t=4;t<=6;t++ )
			{
				String[] projects = RankLibFeatrure.getProjectSubgroup((fold-1+t)%10);
				for( String project: projects )
					validateProjects.add(project);
			}
			String trainFilename = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\LibLinear_Fold" +
					String.valueOf(fold)+".train"; //.train
			double c = 1e-8; //RankSVM ����C
			//��¼ÿ��c�����Ķ�λ���ܽ��
			float resultOfModel[]= {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			int cnums = 20; //����c�ĸ�����
			//����c ��1000��0.001
			for( int cp=1;cp<=cnums;cp++ )
			{
				String modelFilename  = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\Fold" +
											String.valueOf(fold)+"_C"+String.valueOf(cp)+".model";
				train(trainFilename,modelFilename,c);
				resultOfModel[cp-1] = calculateFaultLocalizationPerformance(validateProjects,modelFilename);
				System.out.print(c+":"+resultOfModel[cp-1]+",");
				c = c/3;
			}//end of for( int cp=1;cp<=7;cp++ )
			int bestCp = getBestCp(resultOfModel,cnums);//resultOfModel����Сֵ��������
			System.out.println(" ");
			System.out.println("Fold"+fold+" bestCp="+bestCp);
			String bestModelFilename  = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\Fold" +
					String.valueOf(fold)+"_C"+String.valueOf(bestCp+1)+".model";
			//������Cֵ��Ӧ��ģ�����²���һ��.score�ļ�������Ҫ�õ�������Ҫ������ģ�͵�ƽ��ֵ��
			//testingProjects�����Ҫ���ղ��������λ���ܣ�trainѵ������ģ�ͣ�����Ŀ
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
	 * 10�ۣ�ѵ��ģ�ͣ����洢ģ���ļ��� 
	 * ��trainModelCacluateScoreByModel��ͬ���Լ���ȡģ�ͣ����ڴ�ģ�ͼ��������ֵ��
	 * �Լ��Ĵ������Ranker������ Predict.exe
	 */
	private void trainModelCacluateScoreByModel2()
	{
		//һ���Խ������ļ������ڴ档�����ظ�IO���������Ч�ʡ�
		ValidateTestingFileManager vtfmMemory = new ValidateTestingFileManager();
		vtfmMemory.readTestingFeatureFileToMemeory();
		
		for( int fold=1; fold<=10;fold++ )
		{
			String trainFilename = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\LibLinear_Fold" +
					String.valueOf(fold)+".train"; //.train
			double CCC = 1e-5; //RankSVM ����C
			 //��¼ÿ��cp�����Ķ�λ���ܽ��
			float resultOfModel[]= {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			List<double[]> allModelW = new ArrayList<>(); //�洢ѵ������ģ��
			int cnums = 30; //����c�ĸ�����
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
			int bestCp = getBestCp(resultOfModel,cnums);//resultOfModel����Сֵ��������
			System.out.println(" ");
			System.out.println("Fold"+fold+" bestCp="+(bestCp+1)+"  : "+resultOfModel[bestCp]);
			//������Cֵ��Ӧ��ģ�����ܼ�¼����������Ҫ�õ�������Ҫ������ģ�͵�ƽ��ֵ��
			vtfmMemory.recordTestingPerformanceBestModel( fold,allModelW.get(bestCp) );
		}//end of for...	
		//����Learing to Rank �㷨�Ķ�λЧ��
		vtfmMemory.evaluatePerformance("LibLinear");
	}
}
