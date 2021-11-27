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

import common.ProjectConfiguration;

/** 作为LtoRUsingLibLinearRankSVM和RandomProjectEvaluateLibLinear的子类。
 * @author Administrator
 *
 */
public class ComponentLibLinearRankSVM {
	//特征文件，指明应用哪些特征训练模型。
	protected final String featureScreenFilename= ProjectConfiguration.PathFeartureScreen+"\\" +
									"featureUn41.txt";//#开头的屏蔽掉该特征。
	//优化目标1=fWorstIcp 2=fMeanIcp 3=probBugver[0] 4==probBugver[1] 5= probBugver[2] ,....
	private final static int OptimizationObjective = 1;
	
	//特别注意： featureIndexs一定要按照顺序存放。
	protected int[] featureIndexs; //指明哪些特征参与学习、验证和测试。从1到45.
	
	protected ComponentLibLinearRankSVM()
	{
	}
	
	
	/**
	 * @return
	 */
	public static int getOptimizationObjective()
	{
		return OptimizationObjective;
	}

	/**
	 * 读特征文件。筛选特征。
	 */
	protected void readFeatureScreen()
	{
		List<Integer> findexLst = new ArrayList<>();
		try {
			File file = new File(featureScreenFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				String lineTXT = ""; //逐行读入
				while((lineTXT = br.readLine())!= null){
					if( lineTXT.startsWith("#") )
						continue; //以#开头的特征不参与。
					int iv = Integer.valueOf(lineTXT);
					if( iv<=0 || iv>45 )
						System.out.println("Read feature index file is error. ");
					findexLst.add(iv);
				}
				br.close();
				read.close();
			}
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Read feature index file is error. ");
		}
		featureIndexs =  findexLst.stream().mapToInt(Integer::valueOf).toArray();
	}
	
	/** 按照featureIndexs的顺序重新生成新的特征文件
	 * @param before 原始文件
	 * @param after  筛选后的特征文件
	 */
	protected void adjustFeatureFileAfterScreen(String before,String after)
	{
		try {
			File destFile = new File(after);
			if (destFile.isFile() && destFile.exists())
				destFile.delete();//存入文件存在，则删除。
			File sourceFile = new File(before);
			if (sourceFile.isFile() && sourceFile.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(sourceFile));
				BufferedReader br = new BufferedReader(read);

				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(destFile));
				BufferedWriter bw = new BufferedWriter(writer);
				
				String lineTXT = ""; //逐行读入
				while((lineTXT = br.readLine())!= null){
					String[]  strAry = lineTXT.split("\\s+"); //允许多个空格分割字符串
					StringBuilder sb = new StringBuilder();
					sb.append(strAry[0]+" "+strAry[1]+" ");//label qid:XX
					for( int i=0;i<featureIndexs.length;i++ )
					{
						String strFearture = strAry[featureIndexs[i]-1+2];//从第3个开始，才是特征1。
						String[]  strColon = strFearture.split(":");//冒号前是特征序号，后面是值。
						double dv = Double.valueOf(strColon[1]);
						sb.append(i+1);
						sb.append(":");
						sb.append(dv);
						sb.append(" ");
					}
					bw.append(sb.toString().trim()+"\n");
					bw.flush();
				}
				br.close();
				read.close();
				bw.close();
				writer.close();
			}
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Screening feature file is error. ");
		}
	}
	
	/**
	 *版权声明：本文为CSDN博主「大道之简」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
	 *原文链接：https://blog.csdn.net/HcJsJqJSSM/article/details/84188788
	 * @param shellCommand
	 */
	private void shell(String shellCommand)
	{
		 String cmd = shellCommand;//"ping www.baidu.com";
		 try {
		     Process process = Runtime.getRuntime().exec(cmd);
		     InputStream is = process.getInputStream();
		     InputStreamReader isr = new InputStreamReader(is);
		     BufferedReader br = new BufferedReader(isr);
		     String content = br.readLine();
		     while (content != null) {
		         System.out.println(content);
		         content = br.readLine();
		     }
		 } 
		 catch (IOException e) {
		     e.printStackTrace();
		 }
	}
	
	/** 训练模型
	 * @param trainFilename  训练集文件
	 * @param modelFilename  保存训练出的模型。
	 * @param c   min_w w^Tw/2 + C \sum max(0, 1- y_i w^Tx_i)^2
	 */
	protected void train(String trainFilename,String modelFilename,double c)
	{
		//-q
		String shellCommand = ProjectConfiguration.PathLibLinearRankSVM+"\\train.exe  -s 8 -q -c  "+
				Double.toString(c)+" "+trainFilename+" "+modelFilename;
		shell(shellCommand);
	}
	
	/** 将模型文件里的 w 读出来。
	 * @param modelFilename
	 * @return
	 */
	protected double[] readModel(String modelFilename)
	{
		List<Double> wFeatureLst = new ArrayList<>(); //属性的权重值。
		
		try {
			File file = new File(modelFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				String lineTXT = ""; //逐行读入
				lineTXT = br.readLine(); //solver_type L2R_L2LOSS_RANKSVM
				lineTXT = br.readLine(); //nr_class 2
				lineTXT = br.readLine();//nr_feature 45
				lineTXT = br.readLine();//bias -1
				lineTXT = br.readLine();//w
				if( !lineTXT.contentEquals("w") )
				{
					br.close();
					read.close();
					throw new Exception("It is not model file of LibLinear!");
				}
				while((lineTXT = br.readLine())!= null){
						double dv = Double.valueOf(lineTXT);
						wFeatureLst.add(dv);
					}
				br.close();
				read.close();
			}
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
		}
		return  wFeatureLst.stream().mapToDouble(Double::valueOf).toArray();
	}
	
	/**使用训练出的模型给被测样本算可疑度值
	 * @param modelFilename
	 * @param testingFilename
	 * @param scoreFilename
	 */
	protected void validateTest(String modelFilename,String testingFilename,String scoreFilename)
	{
		//-q
		String shellCommand = ProjectConfiguration.PathLibLinearRankSVM+"\\predict.exe  -q "+
				testingFilename+" "+modelFilename+" "+scoreFilename;
		shell(shellCommand);
	}
	
	/** 求resultOfModel里面最小值的索引，从0开始。
	 * @param resultOfModel
	 * @return 从0开始的索引
	 */
	protected int  getBestCp(float[] resultOfModel,int cnums)
	{
		int bestCp = 0;
		float min = resultOfModel[0];
		for( int i=1;i<cnums;i++ )
		{
			if( resultOfModel[i]<min )
			{
				min = resultOfModel[i];
				bestCp = i;
			}
		}
		return bestCp;
	}
	
}
