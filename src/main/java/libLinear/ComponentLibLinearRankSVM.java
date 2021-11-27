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

/** ��ΪLtoRUsingLibLinearRankSVM��RandomProjectEvaluateLibLinear�����ࡣ
 * @author Administrator
 *
 */
public class ComponentLibLinearRankSVM {
	//�����ļ���ָ��Ӧ����Щ����ѵ��ģ�͡�
	protected final String featureScreenFilename= ProjectConfiguration.PathFeartureScreen+"\\" +
									"featureUn41.txt";//#��ͷ�����ε���������
	//�Ż�Ŀ��1=fWorstIcp 2=fMeanIcp 3=probBugver[0] 4==probBugver[1] 5= probBugver[2] ,....
	private final static int OptimizationObjective = 1;
	
	//�ر�ע�⣺ featureIndexsһ��Ҫ����˳���š�
	protected int[] featureIndexs; //ָ����Щ��������ѧϰ����֤�Ͳ��ԡ���1��45.
	
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
	 * �������ļ���ɸѡ������
	 */
	protected void readFeatureScreen()
	{
		List<Integer> findexLst = new ArrayList<>();
		try {
			File file = new File(featureScreenFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				String lineTXT = ""; //���ж���
				while((lineTXT = br.readLine())!= null){
					if( lineTXT.startsWith("#") )
						continue; //��#��ͷ�����������롣
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
	
	/** ����featureIndexs��˳�����������µ������ļ�
	 * @param before ԭʼ�ļ�
	 * @param after  ɸѡ��������ļ�
	 */
	protected void adjustFeatureFileAfterScreen(String before,String after)
	{
		try {
			File destFile = new File(after);
			if (destFile.isFile() && destFile.exists())
				destFile.delete();//�����ļ����ڣ���ɾ����
			File sourceFile = new File(before);
			if (sourceFile.isFile() && sourceFile.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(sourceFile));
				BufferedReader br = new BufferedReader(read);

				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(destFile));
				BufferedWriter bw = new BufferedWriter(writer);
				
				String lineTXT = ""; //���ж���
				while((lineTXT = br.readLine())!= null){
					String[]  strAry = lineTXT.split("\\s+"); //�������ո�ָ��ַ���
					StringBuilder sb = new StringBuilder();
					sb.append(strAry[0]+" "+strAry[1]+" ");//label qid:XX
					for( int i=0;i<featureIndexs.length;i++ )
					{
						String strFearture = strAry[featureIndexs[i]-1+2];//�ӵ�3����ʼ����������1��
						String[]  strColon = strFearture.split(":");//ð��ǰ��������ţ�������ֵ��
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
	 *��Ȩ����������ΪCSDN���������֮�򡹵�ԭ�����£���ѭCC 4.0 BY-SA��ȨЭ�飬ת���븽��ԭ�ĳ������Ӽ���������
	 *ԭ�����ӣ�https://blog.csdn.net/HcJsJqJSSM/article/details/84188788
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
	
	/** ѵ��ģ��
	 * @param trainFilename  ѵ�����ļ�
	 * @param modelFilename  ����ѵ������ģ�͡�
	 * @param c   min_w w^Tw/2 + C \sum max(0, 1- y_i w^Tx_i)^2
	 */
	protected void train(String trainFilename,String modelFilename,double c)
	{
		//-q
		String shellCommand = ProjectConfiguration.PathLibLinearRankSVM+"\\train.exe  -s 8 -q -c  "+
				Double.toString(c)+" "+trainFilename+" "+modelFilename;
		shell(shellCommand);
	}
	
	/** ��ģ���ļ���� w ��������
	 * @param modelFilename
	 * @return
	 */
	protected double[] readModel(String modelFilename)
	{
		List<Double> wFeatureLst = new ArrayList<>(); //���Ե�Ȩ��ֵ��
		
		try {
			File file = new File(modelFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				String lineTXT = ""; //���ж���
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
	
	/**ʹ��ѵ������ģ�͸�������������ɶ�ֵ
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
	
	/** ��resultOfModel������Сֵ����������0��ʼ��
	 * @param resultOfModel
	 * @return ��0��ʼ������
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
