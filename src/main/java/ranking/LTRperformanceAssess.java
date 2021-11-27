/**
 * 
 */
package ranking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import common.ProjectConfiguration;
import sbflMetrics.IMetricMethod;
import sbflMetrics.MethodStrategyComplexFactory;
import sbflMetrics.WorstBestMean;

/**
 * @author Administrator
 * * 要考虑具有相同可疑度值的故障语句个数对性能的影响。
 * 许多版本都具有多个故障语句，它们都很可能具有相同的可疑度值，特别是数据集Defects4j，Bears等.
 */
public class LTRperformanceAssess {
	private String objectName; //项目名称。
	private int bugId; //项目版本，并非自然顺序的verTh
	//以.score文件里的行号为顺序
	private double[] scoreStmts; //版本所有文件的所有语句，每条语句有一个Learning to Rank 计算出的分值。
	//记录.testing文件里的行号，从1开始。
	private int[] faultIndexs;  //错误语句所在的index,匹配scoreStmts的索引。
	
	public LTRperformanceAssess(String project,int bugid)
	{
		objectName = project;
		bugId = bugid;
		faultIndexs = null;
		scoreStmts = null;
	}
	
	/** 计算 Learning to Rank 算法的定位性能。
	 * @param strMetric  expense,exam,T-score,P-score,...
	 * @return   返回结果。Worst-Best-Mean
	 */
	public WorstBestMean calPerformance(String strMetric)
	{
		//读入.testing文件，里面有错误语句的行号。
		readTestingFile();
		//objectName,在哪三个Fold里面有排序分值。
		int[] foldsScore = RankLibFeatrure.getFoldthObject(objectName);
		List<WorstBestMean> wbsList = new ArrayList<>(); 
		for( int i=0;i<3;i++ )
		{ //foldsScore的长度肯定是3.
			readScoreFile(foldsScore[i]); //.score file
			int totalExec = scoreStmts.length;
			int[] pStatement = new int[totalExec]; //the pointer of the number of statement
			for(int k=0;k<totalExec;k++)
				pStatement[k] = k+1; //.score文件里的行号。
			//find the most suspiciousness,return Max(Suspicious) and his code line no.
			double[] maxFaultSuspi = {0.0}; //max suspiciousness in fault code line.
			getMaxSuspiFaultLine(scoreStmts,pStatement,maxFaultSuspi);
			//注意，faultIndexs的值从1开始，并非从0开始。
			IMetricMethod esMetric = MethodStrategyComplexFactory.createMetricMethodObject(strMetric, faultIndexs);
			esMetric.calculateWorstBestMean(scoreStmts,pStatement,maxFaultSuspi[0]);
			wbsList.add(esMetric.getWorstBestMeanResult());
		}
		return WorstBestMean.averageExamAndPscore(wbsList);
	}
	
	/**
	 * 读入.testing文件，将标签为1（故障语句）的行号（从1开始）,存入faultIndexs
	 */
	public void readTestingFile()
	{
		String pathFilename = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+
				objectName+"\\"+objectName+"_v"+String.valueOf(bugId)+".testing";
		List<Integer> labelFaultLst = new ArrayList<>();
		
		try {
			File file = new File(pathFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				String lineTXT = ""; //逐行读入
				int index = 1; //记录行号，从1开始
				while((lineTXT = br.readLine())!= null){
					if( lineTXT.length()<=2 )
						break; //结束
					String[]  strAry = lineTXT.split("\\s+"); //允许多个空格分割字符串
					int label = Integer.valueOf(strAry[0]); //1=bug,0=free
					if( label==1 )
						labelFaultLst.add(index);
					else if( label==0 )
						{}
					else
						System.out.println(pathFilename+" first lable is 0 or 1");
					index++;
				}
				br.close();
				read.close();
			}
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
		}
		faultIndexs =  labelFaultLst.stream().mapToInt(Integer::valueOf).toArray();
	}
	
	/**
	 * 读入.score文件，将排序分值按照文件里行号顺序,存入scoreStmts
	 * fold 1.2.3....10
	 */
	public void readScoreFile(int fold)
	{
		String pathFilename = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+
				objectName+"\\"+objectName+"_v"+String.valueOf(bugId)+"_F"+String.valueOf(fold)+".score";
		List<Double> scoreLTRLst = new ArrayList<>();
		
		try {
			File file = new File(pathFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				String lineTXT = ""; //逐行读入
				while((lineTXT = br.readLine())!= null){
					if( lineTXT.length()<=2 )
						break; //结束
					String[]  strAry = lineTXT.split("\\s+"); //允许多个空格分割字符串
					double dv = Double.valueOf(strAry[2]); //最后一列为分值。
					scoreLTRLst.add(dv);
				}
				br.close();
				read.close();
			}
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
		}
		scoreStmts =  scoreLTRLst.stream().mapToDouble(Double::valueOf).toArray();
	}

	/** Check nTxtno is or not fault code line.
	 * @param nTxtno 待检查的行，其行号(并非源代码的行号，而是.score文件里的行号)
	 * @return return TRUE: is fault; else is not fault.
	 */
	public boolean isFaultCode(int nTxtno)
	{
		boolean bFault = false;
		//judge this is a fault line.
		for( int item : faultIndexs )
		{
			if( nTxtno==item )
			{
				bFault = true;
				break;
			}
		}
		return bFault;
	}
	
	/** 故障语句可能有多条，找出他们之中可疑度最大值。
	 * find the most suspiciousness,return Max(Suspicious) and his code line no.
	 * if two fault have same Suspicious,then return the mini line no.
	 * @param pSuspicious 各语句依照顺序，可疑度，learning to rank 排序分值
	 * @param pTxtno  .score文件的行号，与pSuspicious顺序相同。从1开始
	 * @param maxFaultSuspi 最大可疑度值
	 */
	private void getMaxSuspiFaultLine(double[] pSuspicious,int[] pTxtno,double[] maxFaultSuspi)
	{
		//get the most suspiciousness
		double maxSuspi = -999999;
		for ( int i=0;i<pSuspicious.length;i++ )
		{
			if( false==isFaultCode(pTxtno[i]) )//this line'code  is not a fault
				continue;
			if ( pSuspicious[i]>maxSuspi )
				maxSuspi = pSuspicious[i];
		}

		maxFaultSuspi[0] = maxSuspi;
	}
}
