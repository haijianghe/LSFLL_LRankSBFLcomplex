/**
 * 
 */
package libLinear;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import common.ProjectConfiguration;
import common.XMLConfigFile;

/** 随机选取训练集、验证集和测试集。
 * @author Administrator
 *
 */
public class RandomProjectEvaluateLibLinear extends ComponentLibLinearRankSVM {
	private List<String> trainProjects; //训练集
	private List<String> validateProjects; //验证集
	private List<String> testingProjects; //测试集
	private String[] projectAry; //未聚合时，所有项目名称
	
	public RandomProjectEvaluateLibLinear()
	{
		trainProjects = new ArrayList<>();
		validateProjects = new ArrayList<>();
		testingProjects = new ArrayList<>();
		List<String> projectLst = XMLConfigFile.getAllObjectNames();
		projectAry = (String[])projectLst.toArray(new String[projectLst.size()]);
	}
	

	/**
	 * 显示训练集 验证集 测试集的项目名称
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
		readFeatureScreen(); //读入特征选取文件。
		RandomTrainValidateTestingFileManager rtvtfMemory = new RandomTrainValidateTestingFileManager();
		rtvtfMemory.readTrainFeatureFileToMemeory();  //XX.train文件一次性读入内存
		System.out.println("rtvtfMemory.readTrainFeatureFileToMemeory is over.");
		rtvtfMemory.readTestingFeatureFileToMemeory();//LibLinear_XX.testing文件一次性读入内存
		System.out.println("rtvtfMemory.readTestingFeatureFileToMemeory is over.");
		rtvtfMemory.setFeatureIndexs(featureIndexs);
		
		for( int i=0;i<500;i++ )
		{
			//产生新的trainProjects，validateProjects，testingProjects
			randomMakeTrainValidateTesting();
			if( i==0 ) //只显示第一次的子集划分情况。
				showTrainValidateTesting();
			String mergeFilename = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\RandomRankSVM.train";
			//依照特征文件.featue,将随机产生的trainProjects训练集拼接成一个RandomRankSVM.train文件。
			rtvtfMemory.mergeToTrainFile(mergeFilename,trainProjects);
			//训练模型，训练过程会在validateProjects集验证，将其最优模型用于计算测试集的错误定位性能。
			trainModelCacluateScoreByModel(i+1,mergeFilename,rtvtfMemory);
		}
		//计算Learing to Rank 算法的定位效果
		rtvtfMemory.evaluatePerformance("RandRankSVM");
		//显示模型信息。
		rtvtfMemory.showModelInfomation();
	}
	
	private void trainModelCacluateScoreByModel(int step,String trainFilename,RandomTrainValidateTestingFileManager rtvtfManager)
	{
		double CCC = 1; //RankSVM 参数C (以后的实验，这个值改变为1 )
		 //记录每个cp产生的定位性能结果
		float resultOfModel[]= {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		List<double[]> allModelW = new ArrayList<>(); //存储训练出的模型
		int cnums = 30; //参数c的个数。
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
		int bestCp = getBestCp(resultOfModel,cnums);//resultOfModel里最小值的索引。
		//System.out.println(" ");
		System.out.println("step="+step+" bestCp="+(bestCp+1)+"  : "+resultOfModel[bestCp]);
		//让最优C值对应的模型性能记录下来，后面要用到；将来要求所有随机产生模型的平均值。
		rtvtfManager.recordTestingPerformanceBestModel( testingProjects,allModelW.get(bestCp) );
		//将最优模型存起来。
		rtvtfManager.recordBestRankingModelOfOneFold(allModelW.get(bestCp));
	}
	
	/**
	 * 随机产生训练集 验证集 测试集包含的项目列表。
	 * 这种做法有个小问题，聚合。
	 * 可能gzipV2,有3次测试；gzipV3有2次测试；gzip的值会变成这5次的平均值。
	 */
	private void randomMakeTrainValidateTesting()
	{
		//先清除上次的。
		trainProjects.clear();
		validateProjects.clear();
		testingProjects.clear();
		Random r = new Random();
		int totalProject = projectAry.length;
		boolean[] flag = new boolean[totalProject]; //  projectAry的大小是40，
		for( int i=0;i<totalProject;i++ )
			flag[i] = false;
    	int randInt = 0;  
    	for(int j = 0; j < (16+12) ; j++) 
    	{  //16个训练，12个验证。
    		/**得到28个不同的随机数*/  
    		do{  
    			randInt  = r.nextInt(totalProject-1);  
    		}while(flag[randInt]);   
    		flag[randInt] = true;
    		if( j<16 )//前16个放入训练集
    			trainProjects.add(projectAry[randInt]);
    		else //后12个放入验证集
    			validateProjects.add(projectAry[randInt]);
    	}
    	//其余的，都放入测试集
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
