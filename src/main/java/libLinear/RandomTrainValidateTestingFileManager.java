/**
 * 
 */
package libLinear;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import affiliated.AbstractProfileFile;
import common.ProjectConfiguration;
import common.XMLConfigFile;
import ranking.RankLibFeatrure;
import sbflMetrics.AbstractMetricMethod;
import sbflMetrics.WorstBestMean;

/** �� RandomProjectEvaluateLibLinear
 *  ѵ���������롢����ڴ棻
 *  ģ�͵���֤����ģ�͵Ĳ��Խ������Щ���������ڴ����С� 
 *  �ڴ����������ٶȡ�
 * �ڴ��д�������͹�����䡣
 */
public class RandomTrainValidateTestingFileManager extends ComponentTVFManager {
	private  Map<String,List<FeatureOfProjectVer>> trainProjectFeature; //������Ŀ��.train�ļ���������������䡣
	private List<double[]> bestModelWList; //�洢ѵ����������ģ�ͣ��洢500�Σ�������ģ�͵�ƽ��ֵ��
	
	public RandomTrainValidateTestingFileManager()
	{
		trainProjectFeature = new HashMap<>();
		bestModelWList = new ArrayList<>();
	}
	
	
	/** ����Ŀ���ƺͰ汾���ҵ�ѵ�����ڴ��е�FeatureOfProjectVer����
	 * @param project
	 * @param bugId
	 * @return
	 */
	protected FeatureOfProjectVer getTrainProjectVer(String project,int bugId)
	{
		FeatureOfProjectVer fopv = null;
		List<FeatureOfProjectVer> fopVers = trainProjectFeature.get(project);
		for( FeatureOfProjectVer item: fopVers )
			if( item.getBugId()==bugId )
			{
				fopv = item;
				break;
			}
		return fopv;
	}
	
	/**
	 * ��������Ŀ���������ļ�.train����������һ���Զ����ڴ�
	 * ������Ŀ�İ汾�б�һ���Զ��롣
	 */
	public void readTrainFeatureFileToMemeory()
	{
		List<String> allObjectNames = XMLConfigFile.getAllObjectNames();
		for( String project : allObjectNames )
		{
			List<FeatureOfProjectVer> fopvLst = new ArrayList<>();
			int[] inBugids = AbstractProfileFile.getInclusionBugId(project);
			for( int kid=1; kid<=inBugids.length;kid++ )
			{
				FeatureOfProjectVer fopv = new FeatureOfProjectVer(project,inBugids[kid-1]);
				fopv.readTrainTestingFile(false);
				fopvLst.add(fopv);
			}//end of for...
			trainProjectFeature.put(project, fopvLst);
		}//end of for...
	}
	
	
	/**���������ļ�.featue��������featureIndexs,�����������trainProjectsѵ����ƴ�ӳ�һ��RandomRankSVM.train�ļ���
	 * @param mergeFilename�� �ϲ�������������ļ���
	 * @param trainProjectsѵ������������Ŀ�����б�
	 */
	public void mergeToTrainFile(String mergeFilename,List<String> trainProjects)
	{
		try {
			File destFile = new File(mergeFilename);
			if (destFile.isFile() && destFile.exists())
				destFile.delete();//�����ļ����ڣ���ɾ����
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(destFile));
			BufferedWriter bw = new BufferedWriter(writer);
			for( String project : trainProjects )
			{ //ÿ����Ŀ
				int[] vers = verListMap.get(project);
				for( int i=0;i<vers.length;i++ )
				{ //��Ŀ��ÿ���汾
					int bugId = vers[i];
					FeatureOfProjectVer fopv = getTrainProjectVer(project,bugId);
					//�ð汾��ѵ����������featureIndexsɸѡ��������д��bw
					fopv.writeToTrainFile(bw, featureIndexs); //featureIndexs������ComponentTVFManager�ж��塣
				}
			}
			bw.close();
			writer.close();
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Screening feature file is error. ");
		}
	}
	
	/**  ѵ�������У���֤ĳ��ģ�͵����ܡ� RandomProjectEvaluateLibLinear��ʹ�á�
	 * @param validateProjects  validateProjects�����Ҫ��֤�����λ���ܣ�trainѵ������ģ�ͣ�����Ŀ
	 * @param wModel  LibLinearѵ������ģ�͡�
	 * @return
	 */
	public float validateFaultLocalizationPerformance(List<String> validateProjects,double[] wModel)
	{
		return calculateFaultLocalizationPerformance(validateProjects,wModel);
	}
	
	/** ������ѵ������ģ�ͣ���������۶�Ӧ���Լ������ܣ���������
	 *     ÿ����ĿҪ��Ĵ�����һ������������ġ�
	 *     ���⣬�ۺ��и�С���⣬gzipV2�������2�Σ�gzipV3�������3�Σ�gzip�ۺϻ����5�ε�ƽ��ֵ��
	 *     ����������£�Ӧ��Ҫ��gzipV2��gzipV3���������һ����
	 * @param testingProjects  �����Ҫ���ղ��������λ���ܣ�trainѵ������ģ�ͣ�����Ŀ
	 * @param wModel
	 */
	public void recordTestingPerformanceBestModel(List<String> testingProjects,double[] wModel)
	{
		for( String project: testingProjects )
		{
			addModelResultToTesting(project,wModel);
		}//end of for...
	}
	
	//��ʾģ�ͣ��������룬������λС����
	private void showOneModel(double[] wModel)
	{
		DecimalFormat df = new DecimalFormat("######0.000");  
		StringBuilder sb = new StringBuilder();
		for( double d: wModel)
		{
			String str = df.format(d);
			sb.append(str+",");
		}	
		System.out.println(sb);
	}
	
	/** �洢ѵ����������ģ�ͣ��洢500�Σ�������ģ�͵�ƽ��ֵ��
	 * @param wModel
	 */
	public void recordBestRankingModelOfOneFold(double[] wModel)
	{
		bestModelWList.add(wModel);
		//showOneModel(wModel);
	}
	
	/**
	 * ��ʾģ�����
	 */
	public void showModelInfomation()
	{
		int steps = bestModelWList.size();
		if( steps <1)
			return;
		int dimen = bestModelWList.get(0).length;
		System.out.println("the number of bestModelWList is : "+steps+",     feature number:  "+dimen);
		double[] maxAbs = new double[dimen]; //������ֵ����ֵ
		double[] minAbs = new double[dimen]; //��С����ֵ����ֵ
		double[] average = new double[dimen]; //����ƽ��ֵ
		for( int i=0;i<dimen;i++ )
		{
			maxAbs[i] = 0;
			minAbs[i] = 9999;
			average[i] = 0;
		}//end of for...
		for (double[] wModel : bestModelWList) 
		{
			for(int i=0;i<dimen;i++ )
			{
				if( Math.abs(wModel[i])>Math.abs(maxAbs[i]))
					maxAbs[i] = wModel[i];
				if( Math.abs(wModel[i])<Math.abs(minAbs[i]))
					minAbs[i] = wModel[i];
				average[i] += wModel[i];
			}
		}//end of for double[]
		for( int i=0;i<dimen;i++ )
			average[i] /= steps;
		showOneModel(maxAbs);
		showOneModel(minAbs);
		showOneModel(average);
		}
}
