/**
 * 
 */
package common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sbflMetrics.WorstBestMean;

/** 将实验结果保存到.csv文件。
 * @author Administrator
 *
 */
public class ExperimentalResultFile {
	
	/** 从vernumMap中取出对象名为objectName的版本个数。
	 * @param objectName
	 * @return
	 */
	private static int getVersionNumber(Map<String,Integer> vernumMap,String objectName)
	{
		int nbugid = 0; 
		for(Map.Entry<String, Integer>  entry  :  vernumMap.entrySet()){
			String project = entry.getKey();
			if( project.contentEquals(objectName) )
			{
				nbugid = entry.getValue();
				break;
			}
		}
		return nbugid;
	}
	
	/**以strAlgorihtm命名，将该strAlgorihtm下所有对象的Pscore结果保存到一个文件
	 * bAggregation = false,不聚合  =true,聚合
	 * @param strAlgorihtm
	 * @param pScoreMap 所有对象的Pscore Pcheck Pbugver结果
	 */
	public static void PscoreAllObjectResult(boolean bAggregation,String strAlgorihtm,
			Map<String,float[]> pScoreMap,Map<String,Integer> vernumMap)
	{
		String filename = ProjectConfiguration.PathAggregatedExperiment+"\\"+"Pscore_"+strAlgorihtm+".csv";
		if( bAggregation )
			filename = ProjectConfiguration.PathAggregatedExperiment+"\\"+"aggr_Pscore_"+strAlgorihtm+".csv";
		StringBuilder strResult = new StringBuilder();
		int[] checkCodeLines = WorstBestMean.checkLine;
		//表头
		strResult.append(",ver,,");
		for( int ccl: checkCodeLines )
		{
			strResult.append("Mean");
			strResult.append(ccl);
			strResult.append(",");
		}
		strResult.append("\n");
		//实验数据
		List<float[]> pscoreProjects = new ArrayList<>();  //所有项目的PScore性能
		List<Integer> verProjects = new ArrayList<>();  //所有项目的版本数目
		for(Map.Entry<String, float[]>  entry  :  pScoreMap.entrySet()){
			float[] lfPbugver = entry.getValue();
			String projectName = entry.getKey(); //对象名称
			int numberOfVersion = getVersionNumber(vernumMap,projectName);
			strResult.append(projectName);
			strResult.append(",");
			strResult.append(numberOfVersion);
			strResult.append(",,");
			//首先worst-best-mean  
			strResult.append(toFileString(lfPbugver));
			strResult.append("\n");
			pscoreProjects.add(lfPbugver);
			verProjects.add(numberOfVersion);
		}
		//将所有项目的加权平均WorstBestMean策略性能保存到文件
		float[] avgPscore = WorstBestMean.averageAggrPscore(pscoreProjects, verProjects);
		strResult.append(",,,");
		for( int k=0;k<avgPscore.length;k++ )
			strResult.append(avgPscore[k]*100+",");
		strResult.append("\n");
		//保存到文件
		DeleteFile(filename);
		OutputToFile(filename,strResult.toString(),true);
	}
	
	//获取输出到文件的字符串
	private static String toFileString(float[] pScoreResult)
	{
		StringBuilder sb = new StringBuilder();
		for( float fv: pScoreResult )
		{
			DecimalFormat dcmFormat=new DecimalFormat(".00");
			sb.append(dcmFormat.format(fv*100));
			sb.append(",");
		}
		return sb.toString();
	}
	/** 以strAlgorihtm命名，将该strAlgorihtm下所有对象的Expense结果保存到一个文件
	 * bAggregation = false,不聚合  =true,聚合
	 * @param strAlgorihtm
	 * 
	 * @param expenseMap 所有对象的Expense结果
	 */
	public static void ExpenseAllObjectResult(boolean bAggregation,String strAlgorihtm,
			Map<String,WorstBestMean> expenseMap,Map<String,Integer> vernumMap)
	{
		String filename = ProjectConfiguration.PathAggregatedExperiment+"\\"+"expense_"+strAlgorihtm+".csv";
		if( bAggregation )
			filename = ProjectConfiguration.PathAggregatedExperiment+"\\"+"aggr_expense_"+strAlgorihtm+".csv";
		StringBuilder strResult = new StringBuilder();
		strResult.append(",vers,,,BESTs,WORSTs,MEANs,,,BEST(%),WORST(%),MEAN(%)\n");
		List<WorstBestMean> wbmProjects = new ArrayList<>();  //所有项目的WBM性能
		List<Integer> verProjects = new ArrayList<>();  //所有项目的版本数目
		for(Map.Entry<String, WorstBestMean>  entry  :  expenseMap.entrySet()){
			WorstBestMean wbm = entry.getValue();
			String strInfo="";
			String projectName = entry.getKey(); //对象名称
			int numberOfVersion = getVersionNumber(vernumMap,projectName);
			strInfo = projectName+","+numberOfVersion+",,,"+toFileString(wbm);
			strResult.append(strInfo+"\n");
			wbmProjects.add(wbm);
			verProjects.add(numberOfVersion);
		}
		//将所有项目的加权平均WorstBestMean策略性能保存到文件
		WorstBestMean avgWBM = WorstBestMean.aggrMeanExam(wbmProjects, verProjects);
		strResult.append(",,,,,,,,,");
		strResult.append(avgWBM.fBestIcp*100+","+avgWBM.fWorstIcp*100+","+avgWBM.fMeanIcp*100+"\n");
		//保存到文件
		DeleteFile(filename);
		OutputToFile(filename,strResult.toString(),true);
	}
	
	
	//保存某一个对象，某种SBFL算法的实验结果。
	//表头用Txx，不用Vxx；避免误解，此处xx是从1开始的自然数字，并非 bugid.
	public static void ExpenseResultStore(String objectName,String strAlgorihtm,List<WorstBestMean> wbsLst)
	{
		String filename = ProjectConfiguration.PathSingleObjectExperiment+"\\"+objectName+"_"+strAlgorihtm+".csv";
		StringBuilder strResult = new StringBuilder();
		strResult.append(",,BESTs,WORSTs,MEANs,,,BEST(%),WORST(%),MEAN(%)\n");
		int vernum = wbsLst.size();
		for( int k=0;k<vernum;k++ )
		{
			WorstBestMean wbm = wbsLst.get(k);
			String strInfo = "T"+(k+1)+",,"+toFileString(wbm);//表头用Txx，不用Vxx；避免误解，此处xx是从1开始的自然数字，并非 bugid.
			strResult.append(strInfo+"\n");
		}
		//求平均值。
		//wbm、sos、scs取名和前面一样，免得修改下面的长串代码的变量。
		WorstBestMean wbm = WorstBestMean.averageExam(wbsLst); 
		String strInfo = "average"+",,"+toFileString(wbm);
		strResult.append(strInfo);
		//保存到文件
		DeleteFile(filename);
		OutputToFile(filename,strResult.toString(),true);
	}
	
	//获取输出到文件的字符串
	private static String toFileString(WorstBestMean wbm)
	{
		String strInfo = wbm.nBestIcl+","+wbm.nWorstIcl+","+wbm.fnMeanIcl+",,"+
				","+icpCovert(wbm);
		return strInfo;
	}
	
	/** 浮点数转换成字符串时，只保留两位小数。
	 * @param imPolicy
	 * @return
	 */
	private static String icpCovert(WorstBestMean imPolicy)
	{
		String strInfo = "";
		DecimalFormat dcmFormat=new DecimalFormat(".00");
		WorstBestMean wbm = (WorstBestMean)imPolicy;
		strInfo = dcmFormat.format(wbm.fBestIcp*100)+","+dcmFormat.format(wbm.fWorstIcp*100)+","
				+dcmFormat.format(wbm.fMeanIcp*100)+",";
		return strInfo;
	}
	
	//删除单个文件，fileName：文件名。
	public static void DeleteFile(String fileName) 
	{
		File file = new File(fileName);
		
		if (file.exists()) {
			if (file.isFile()) //文件
			{
				file.delete();
			} 
		} 
	}//end of deleteFile
		
	//关闭文件，释放资源。
	//writer，FileWriter 类从 OutputStreamWriter 类继承而来。该类按字符向流中写入数据。
	public static void CloseFile(FileWriter writer) 
	{
		try {
			if (writer != null) 
			{
				writer.close();
				writer = null;
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	//关闭文件，释放资源。
	// bw,BufferedWriter类，将文本写入字符输出流，缓冲字符，以便有效地写入单个字符，数组和字符串
	public static void CloseFile(BufferedWriter bw) 
	{
		try {
			if (bw != null) {
				bw.close();
				bw = null;
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	/**              
	 * 将字符串存入一个文件.
	 * @param fileName, 文件名，带文件夹。
	 * @param strData, 存入的数据.
	 */
	public static void OutputToFile(String fileName, String strData,boolean append) 
	{
		File file= new File(fileName);
		FileWriter writer = null;
		BufferedWriter bw = null;

		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new FileWriter(file,append);
			bw = new BufferedWriter(writer);
			bw.write(strData);
			bw.flush();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		finally 
		{
			CloseFile(bw);
			CloseFile(writer);
		}
	}//end of  static void OutputToFile(File file, 
}
