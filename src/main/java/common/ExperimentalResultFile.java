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

/** ��ʵ�������浽.csv�ļ���
 * @author Administrator
 *
 */
public class ExperimentalResultFile {
	
	/** ��vernumMap��ȡ��������ΪobjectName�İ汾������
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
	
	/**��strAlgorihtm����������strAlgorihtm�����ж����Pscore������浽һ���ļ�
	 * bAggregation = false,���ۺ�  =true,�ۺ�
	 * @param strAlgorihtm
	 * @param pScoreMap ���ж����Pscore Pcheck Pbugver���
	 */
	public static void PscoreAllObjectResult(boolean bAggregation,String strAlgorihtm,
			Map<String,float[]> pScoreMap,Map<String,Integer> vernumMap)
	{
		String filename = ProjectConfiguration.PathAggregatedExperiment+"\\"+"Pscore_"+strAlgorihtm+".csv";
		if( bAggregation )
			filename = ProjectConfiguration.PathAggregatedExperiment+"\\"+"aggr_Pscore_"+strAlgorihtm+".csv";
		StringBuilder strResult = new StringBuilder();
		int[] checkCodeLines = WorstBestMean.checkLine;
		//��ͷ
		strResult.append(",ver,,");
		for( int ccl: checkCodeLines )
		{
			strResult.append("Mean");
			strResult.append(ccl);
			strResult.append(",");
		}
		strResult.append("\n");
		//ʵ������
		List<float[]> pscoreProjects = new ArrayList<>();  //������Ŀ��PScore����
		List<Integer> verProjects = new ArrayList<>();  //������Ŀ�İ汾��Ŀ
		for(Map.Entry<String, float[]>  entry  :  pScoreMap.entrySet()){
			float[] lfPbugver = entry.getValue();
			String projectName = entry.getKey(); //��������
			int numberOfVersion = getVersionNumber(vernumMap,projectName);
			strResult.append(projectName);
			strResult.append(",");
			strResult.append(numberOfVersion);
			strResult.append(",,");
			//����worst-best-mean  
			strResult.append(toFileString(lfPbugver));
			strResult.append("\n");
			pscoreProjects.add(lfPbugver);
			verProjects.add(numberOfVersion);
		}
		//��������Ŀ�ļ�Ȩƽ��WorstBestMean�������ܱ��浽�ļ�
		float[] avgPscore = WorstBestMean.averageAggrPscore(pscoreProjects, verProjects);
		strResult.append(",,,");
		for( int k=0;k<avgPscore.length;k++ )
			strResult.append(avgPscore[k]*100+",");
		strResult.append("\n");
		//���浽�ļ�
		DeleteFile(filename);
		OutputToFile(filename,strResult.toString(),true);
	}
	
	//��ȡ������ļ����ַ���
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
	/** ��strAlgorihtm����������strAlgorihtm�����ж����Expense������浽һ���ļ�
	 * bAggregation = false,���ۺ�  =true,�ۺ�
	 * @param strAlgorihtm
	 * 
	 * @param expenseMap ���ж����Expense���
	 */
	public static void ExpenseAllObjectResult(boolean bAggregation,String strAlgorihtm,
			Map<String,WorstBestMean> expenseMap,Map<String,Integer> vernumMap)
	{
		String filename = ProjectConfiguration.PathAggregatedExperiment+"\\"+"expense_"+strAlgorihtm+".csv";
		if( bAggregation )
			filename = ProjectConfiguration.PathAggregatedExperiment+"\\"+"aggr_expense_"+strAlgorihtm+".csv";
		StringBuilder strResult = new StringBuilder();
		strResult.append(",vers,,,BESTs,WORSTs,MEANs,,,BEST(%),WORST(%),MEAN(%)\n");
		List<WorstBestMean> wbmProjects = new ArrayList<>();  //������Ŀ��WBM����
		List<Integer> verProjects = new ArrayList<>();  //������Ŀ�İ汾��Ŀ
		for(Map.Entry<String, WorstBestMean>  entry  :  expenseMap.entrySet()){
			WorstBestMean wbm = entry.getValue();
			String strInfo="";
			String projectName = entry.getKey(); //��������
			int numberOfVersion = getVersionNumber(vernumMap,projectName);
			strInfo = projectName+","+numberOfVersion+",,,"+toFileString(wbm);
			strResult.append(strInfo+"\n");
			wbmProjects.add(wbm);
			verProjects.add(numberOfVersion);
		}
		//��������Ŀ�ļ�Ȩƽ��WorstBestMean�������ܱ��浽�ļ�
		WorstBestMean avgWBM = WorstBestMean.aggrMeanExam(wbmProjects, verProjects);
		strResult.append(",,,,,,,,,");
		strResult.append(avgWBM.fBestIcp*100+","+avgWBM.fWorstIcp*100+","+avgWBM.fMeanIcp*100+"\n");
		//���浽�ļ�
		DeleteFile(filename);
		OutputToFile(filename,strResult.toString(),true);
	}
	
	
	//����ĳһ������ĳ��SBFL�㷨��ʵ������
	//��ͷ��Txx������Vxx��������⣬�˴�xx�Ǵ�1��ʼ����Ȼ���֣����� bugid.
	public static void ExpenseResultStore(String objectName,String strAlgorihtm,List<WorstBestMean> wbsLst)
	{
		String filename = ProjectConfiguration.PathSingleObjectExperiment+"\\"+objectName+"_"+strAlgorihtm+".csv";
		StringBuilder strResult = new StringBuilder();
		strResult.append(",,BESTs,WORSTs,MEANs,,,BEST(%),WORST(%),MEAN(%)\n");
		int vernum = wbsLst.size();
		for( int k=0;k<vernum;k++ )
		{
			WorstBestMean wbm = wbsLst.get(k);
			String strInfo = "T"+(k+1)+",,"+toFileString(wbm);//��ͷ��Txx������Vxx��������⣬�˴�xx�Ǵ�1��ʼ����Ȼ���֣����� bugid.
			strResult.append(strInfo+"\n");
		}
		//��ƽ��ֵ��
		//wbm��sos��scsȡ����ǰ��һ��������޸�����ĳ�������ı�����
		WorstBestMean wbm = WorstBestMean.averageExam(wbsLst); 
		String strInfo = "average"+",,"+toFileString(wbm);
		strResult.append(strInfo);
		//���浽�ļ�
		DeleteFile(filename);
		OutputToFile(filename,strResult.toString(),true);
	}
	
	//��ȡ������ļ����ַ���
	private static String toFileString(WorstBestMean wbm)
	{
		String strInfo = wbm.nBestIcl+","+wbm.nWorstIcl+","+wbm.fnMeanIcl+",,"+
				","+icpCovert(wbm);
		return strInfo;
	}
	
	/** ������ת�����ַ���ʱ��ֻ������λС����
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
	
	//ɾ�������ļ���fileName���ļ�����
	public static void DeleteFile(String fileName) 
	{
		File file = new File(fileName);
		
		if (file.exists()) {
			if (file.isFile()) //�ļ�
			{
				file.delete();
			} 
		} 
	}//end of deleteFile
		
	//�ر��ļ����ͷ���Դ��
	//writer��FileWriter ��� OutputStreamWriter ��̳ж��������ఴ�ַ�������д�����ݡ�
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

	//�ر��ļ����ͷ���Դ��
	// bw,BufferedWriter�࣬���ı�д���ַ�������������ַ����Ա���Ч��д�뵥���ַ���������ַ���
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
	 * ���ַ�������һ���ļ�.
	 * @param fileName, �ļ��������ļ��С�
	 * @param strData, ���������.
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
