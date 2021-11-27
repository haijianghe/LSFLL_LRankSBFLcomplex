/**
 * 
 */
package libLinear;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import common.ProjectConfiguration;
import common.XMLConfigFile;

/** ���ѡȡѵ��������֤���Ͳ��Լ���
 * @author Administrator
 *
 */
public class RandomProjectEvaluateLibLinear extends ComponentLibLinearRankSVM {
	private List<String> trainProjects; //ѵ����
	private List<String> validateProjects; //��֤��
	private List<String> testingProjects; //���Լ�
	private String[] projectAry; //δ�ۺ�ʱ��������Ŀ����
	
	public RandomProjectEvaluateLibLinear()
	{
		trainProjects = new ArrayList<>();
		validateProjects = new ArrayList<>();
		testingProjects = new ArrayList<>();
		List<String> projectLst = XMLConfigFile.getAllObjectNames();
		projectAry = (String[])projectLst.toArray(new String[projectLst.size()]);
	}
	

	/**
	 * ��ʾѵ���� ��֤�� ���Լ�����Ŀ����
	 */
	private void showTrainValidateTesting()
	{
		System.out.print(trainProjects.size()+" : ");
		for( String project: trainProjects)
			System.out.print(project+",");
		System.out.println(" . ");
		System.out.print(validateProjects.size()+" : ");
		for( String project: validateProjects)
			System.out.print(project+",");
		System.out.println(" . ");
		System.out.print(testingProjects.size()+" : ");
		for( String project: testingProjects)
			System.out.print(project+",");
		System.out.println(" . ");
	}

	
	
	/**
	 * 
	 */
	public void evaluateExperiment()
	{
		readFeatureScreen(); //��������ѡȡ�ļ���
		RandomTrainValidateTestingFileManager rtvtfMemory = new RandomTrainValidateTestingFileManager();
		rtvtfMemory.readTrainFeatureFileToMemeory();  //XX.train�ļ�һ���Զ����ڴ�
		System.out.println("rtvtfMemory.readTrainFeatureFileToMemeory is over.");
		rtvtfMemory.readTestingFeatureFileToMemeory();//LibLinear_XX.testing�ļ�һ���Զ����ڴ�
		System.out.println("rtvtfMemory.readTestingFeatureFileToMemeory is over.");
		rtvtfMemory.setFeatureIndexs(featureIndexs);
		
		for( int i=0;i<500;i++ )
		{
			//�����µ�trainProjects��validateProjects��testingProjects
			randomMakeTrainValidateTesting();
			if( i==0 ) //ֻ��ʾ��һ�ε��Ӽ����������
				showTrainValidateTesting();
			String mergeFilename = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\RandomRankSVM.train";
			//���������ļ�.featue,�����������trainProjectsѵ����ƴ�ӳ�һ��RandomRankSVM.train�ļ���
			rtvtfMemory.mergeToTrainFile(mergeFilename,trainProjects);
			//ѵ��ģ�ͣ�ѵ�����̻���validateProjects����֤����������ģ�����ڼ�����Լ��Ĵ���λ���ܡ�
			trainModelCacluateScoreByModel(i+1,mergeFilename,rtvtfMemory);
		}
		//����Learing to Rank �㷨�Ķ�λЧ��
		rtvtfMemory.evaluatePerformance("RandRankSVM");
		//��ʾģ����Ϣ��
		rtvtfMemory.showModelInfomation();
	}
	
	private void trainModelCacluateScoreByModel(int step,String trainFilename,RandomTrainValidateTestingFileManager rtvtfManager)
	{
		double CCC = 1; //RankSVM ����C (�Ժ��ʵ�飬���ֵ�ı�Ϊ1 )
		 //��¼ÿ��cp�����Ķ�λ���ܽ��
		float resultOfModel[]= {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		List<double[]> allModelW = new ArrayList<>(); //�洢ѵ������ģ��
		int cnums = 30; //����c�ĸ�����
		for( int cp=1;cp<=cnums;cp++ )
		{
			String modelFilename  = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\RandomRankSVM.model";
			train(trainFilename,modelFilename,CCC);
			double[] wModel = readModel(modelFilename);
			allModelW.add(wModel);
			resultOfModel[cp-1] = rtvtfManager.validateFaultLocalizationPerformance(validateProjects,wModel);
			//System.out.print(CCC+":"+resultOfModel[cp-1]+",");
			CCC = CCC/2;
		}//end of for( int cp=1;cp<= ;cp++ )
		int bestCp = getBestCp(resultOfModel,cnums);//resultOfModel����Сֵ��������
		//System.out.println(" ");
		System.out.println("step="+step+" bestCp="+(bestCp+1)+"  : "+resultOfModel[bestCp]);
		//������Cֵ��Ӧ��ģ�����ܼ�¼����������Ҫ�õ�������Ҫ�������������ģ�͵�ƽ��ֵ��
		rtvtfManager.recordTestingPerformanceBestModel( testingProjects,allModelW.get(bestCp) );
		//������ģ�ʹ�������
		rtvtfManager.recordBestRankingModelOfOneFold(allModelW.get(bestCp));
	}
	
	/**
	 * �������ѵ���� ��֤�� ���Լ���������Ŀ�б�
	 * ���������и�С���⣬�ۺϡ�
	 * ����gzipV2,��3�β��ԣ�gzipV3��2�β��ԣ�gzip��ֵ������5�ε�ƽ��ֵ��
	 */
	private void randomMakeTrainValidateTesting()
	{
		//������ϴεġ�
		trainProjects.clear();
		validateProjects.clear();
		testingProjects.clear();
		Random r = new Random();
		int totalProject = projectAry.length;
		boolean[] flag = new boolean[totalProject]; //  projectAry�Ĵ�С��40��
		for( int i=0;i<totalProject;i++ )
			flag[i] = false;
    	int randInt = 0;  
    	for(int j = 0; j < (16+12) ; j++) 
    	{  //16��ѵ����12����֤��
    		/**�õ�28����ͬ�������*/  
    		do{  
    			randInt  = r.nextInt(totalProject-1);  
    		}while(flag[randInt]);   
    		flag[randInt] = true;
    		if( j<16 )//ǰ16������ѵ����
    			trainProjects.add(projectAry[randInt]);
    		else //��12��������֤��
    			validateProjects.add(projectAry[randInt]);
    	}
    	//����ģ���������Լ�
    	for( int k=0;k<totalProject;k++ )
    	{
    		if( trainProjects.contains(projectAry[k]) )
    			continue;
    		if( validateProjects.contains(projectAry[k]) )
    			continue;
    		testingProjects.add(projectAry[k]);
    	}
	}
	
}
