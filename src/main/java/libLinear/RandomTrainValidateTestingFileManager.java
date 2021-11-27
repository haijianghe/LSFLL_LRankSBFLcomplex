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

/** 供 RandomProjectEvaluateLibLinear
 *  训练样本读入、存放内存；
 *  模型的验证，及模型的测试结果，这些操作都放在此类中。 
 *  内存操作，提高速度。
 * 内存中存放特征和故障语句。
 */
public class RandomTrainValidateTestingFileManager extends ComponentTVFManager {
	private  Map<String,List<FeatureOfProjectVer>> trainProjectFeature; //所有项目的.train文件里特征及故障语句。
	private List<double[]> bestModelWList; //存储训练出的最优模型，存储500次，可以求模型的平均值。
	
	public RandomTrainValidateTestingFileManager()
	{
		trainProjectFeature = new HashMap<>();
		bestModelWList = new ArrayList<>();
	}
	
	
	/** 由项目名称和版本号找到训练集内存中的FeatureOfProjectVer对象
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
	 * 将所有项目，其特征文件.train的特征向量一次性读入内存
	 * 所有项目的版本列表一次性读入。
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
	
	
	/**依照特征文件.featue读出来的featureIndexs,将随机产生的trainProjects训练集拼接成一个RandomRankSVM.train文件。
	 * @param mergeFilename， 合并特征，放入此文件。
	 * @param trainProjects训练集包含的项目名称列表
	 */
	public void mergeToTrainFile(String mergeFilename,List<String> trainProjects)
	{
		try {
			File destFile = new File(mergeFilename);
			if (destFile.isFile() && destFile.exists())
				destFile.delete();//存入文件存在，则删除。
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(destFile));
			BufferedWriter bw = new BufferedWriter(writer);
			for( String project : trainProjects )
			{ //每个项目
				int[] vers = verListMap.get(project);
				for( int i=0;i<vers.length;i++ )
				{ //项目里每个版本
					int bugId = vers[i];
					FeatureOfProjectVer fopv = getTrainProjectVer(project,bugId);
					//该版本的训练数据依照featureIndexs筛选出特征，写入bw
					fopv.writeToTrainFile(bw, featureIndexs); //featureIndexs在子类ComponentTVFManager中定义。
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
	
	/**  训练过程中，验证某个模型的性能。 RandomProjectEvaluateLibLinear类使用。
	 * @param validateProjects  validateProjects存放需要验证其错误定位性能（train训练出的模型）的项目
	 * @param wModel  LibLinear训练出的模型。
	 * @return
	 */
	public float validateFaultLocalizationPerformance(List<String> validateProjects,double[] wModel)
	{
		return calculateFaultLocalizationPerformance(validateProjects,wModel);
	}
	
	/** 将该折训练出的模型，计算出该折对应测试集的性能，存起来，
	 *     每个项目要存的次数不一定，随机产生的。
	 *     另外，聚合有个小问题，gzipV2随机产生2次，gzipV3随机产生3次，gzip聚合会计算5次的平均值。
	 *     正常的情况下，应该要求gzipV2和gzipV3的随机次数一样。
	 * @param testingProjects  存放需要最终测试其错误定位性能（train训练出的模型）的项目
	 * @param wModel
	 */
	public void recordTestingPerformanceBestModel(List<String> testingProjects,double[] wModel)
	{
		for( String project: testingProjects )
		{
			addModelResultToTesting(project,wModel);
		}//end of for...
	}
	
	//显示模型，四舍五入，保留三位小数。
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
	
	/** 存储训练出的最优模型，存储500次，可以求模型的平均值。
	 * @param wModel
	 */
	public void recordBestRankingModelOfOneFold(double[] wModel)
	{
		bestModelWList.add(wModel);
		//showOneModel(wModel);
	}
	
	/**
	 * 显示模型情况
	 */
	public void showModelInfomation()
	{
		int steps = bestModelWList.size();
		if( steps <1)
			return;
		int dimen = bestModelWList.get(0).length;
		System.out.println("the number of bestModelWList is : "+steps+",     feature number:  "+dimen);
		double[] maxAbs = new double[dimen]; //最大绝对值的数值
		double[] minAbs = new double[dimen]; //最小绝对值的数值
		double[] average = new double[dimen]; //算术平均值
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
