/**
 * 
 */
package libLinear;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import common.ProjectConfiguration;
import ranking.RankLibFeatrure;
import sbflMetrics.IMetricMethod;
import sbflMetrics.MethodStrategyComplexFactory;
import sbflMetrics.WorstBestMean;

/** ʹ��LibLinear-RankSVM�㷨ʱ��������ģ�Ͳ���.score�ļ�����������ܡ�
 * @author Administrator
 *
 */
public class LibLinearScorePerformanceAssess {
	private String objectName; //��Ŀ���ơ�
	private int bugId; //��Ŀ�汾��������Ȼ˳���verTh
	//��.score�ļ�����к�Ϊ˳��
	private double[] scoreStmts; //�汾�����ļ���������䣬ÿ�������һ��Learning to Rank ������ķ�ֵ��
	//��¼.testing�ļ�����кţ���1��ʼ��
	private int[] faultIndexs;  //����������ڵ�index,ƥ��scoreStmts��������
	
	public LibLinearScorePerformanceAssess(String project,int bugid)
	{
		objectName = project;
		bugId = bugid;
		faultIndexs = null;
		scoreStmts = null;
	}
	
	/**      ���ģ�͵�ƽ��ֵ 
	 * ���� Learning to Rank �㷨�Ķ�λ���ܡ�
	 * @param strMetric  expense,exam,T-score,P-score,...
	 * @return   ���ؽ����Worst-Best-Mean
	 */
	public WorstBestMean calMultiModelPerformance(String strMetric)
	{
		//����.testing�ļ��������д��������кš�
		readTestingFile();
		//objectName,��������Fold�����������ֵ��
		int[] foldsScore = RankLibFeatrure.getFoldthObject(objectName);
		List<WorstBestMean> wbsList = new ArrayList<>(); 
		for( int i=0;i<3;i++ )
		{ //foldsScore�ĳ��ȿ϶���3.
			readScoreFile(foldsScore[i]); //.score file
			int totalExec = scoreStmts.length;
			int[] pStatement = new int[totalExec]; //the pointer of the number of statement
			for(int k=0;k<totalExec;k++)
				pStatement[k] = k+1; //.score�ļ�����кš�
			//find the most suspiciousness,return Max(Suspicious) and his code line no.
			double[] maxFaultSuspi = {0.0}; //max suspiciousness in fault code line.
			getMaxSuspiFaultLine(scoreStmts,pStatement,maxFaultSuspi);
			//ע�⣬faultIndexs��ֵ��1��ʼ�����Ǵ�0��ʼ��
			IMetricMethod esMetric = MethodStrategyComplexFactory.createMetricMethodObject(strMetric, faultIndexs);
			esMetric.calculateWorstBestMean(scoreStmts,pStatement,maxFaultSuspi[0]);
			wbsList.add(esMetric.getWorstBestMeanResult());
		}
		return WorstBestMean.averageExamAndPscore(wbsList);
	}

	/**  ����ģ�� 
	 * ���� Learning to Rank �㷨�Ķ�λ���ܡ�
	 * @param strMetric  expense,exam,T-score,P-score,...
	 * @return   ���ؽ����Worst-Best-Mean
	 */
	public WorstBestMean calPerformance(String strMetric)
	{
		//����.testing�ļ��������д��������кš�
		readTestingFile();
		readScoreFile(0); //.score file
		int totalExec = scoreStmts.length;
		int[] pStatement = new int[totalExec]; //the pointer of the number of statement
		for(int k=0;k<totalExec;k++)
			pStatement[k] = k+1; //.score�ļ�����кš�
		//find the most suspiciousness,return Max(Suspicious) and his code line no.
		double[] maxFaultSuspi = {0.0}; //max suspiciousness in fault code line.
		getMaxSuspiFaultLine(scoreStmts,pStatement,maxFaultSuspi);
		//ע�⣬faultIndexs��ֵ��1��ʼ�����Ǵ�0��ʼ��
		IMetricMethod esMetric = MethodStrategyComplexFactory.createMetricMethodObject(strMetric, faultIndexs);
		esMetric.calculateWorstBestMean(scoreStmts,pStatement,maxFaultSuspi[0]);
		return 	esMetric.getWorstBestMeanResult();
	}

	/**
	 * ����.testing�ļ�������ǩΪ1��������䣩���кţ���1��ʼ��,����faultIndexs
	 */
	public void readTestingFile()
	{
		//��LibLinear_...һ��������label
		String pathFilename = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+
				objectName+"\\"+objectName+"_v"+String.valueOf(bugId)+".testing";
		List<Integer> labelFaultLst = new ArrayList<>();
		
		try {
			File file = new File(pathFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				String lineTXT = ""; //���ж���
				int index = 1; //��¼�кţ���1��ʼ
				while((lineTXT = br.readLine())!= null){
					String[]  strAry = lineTXT.split("\\s+"); //�������ո�ָ��ַ���
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
	 * ����.score�ļ����������ֵ�����ļ����к�˳��,����scoreStmts
	 * fold 1.2.3....10
	 */
	public void readScoreFile(int fold)
	{
		String pathFilename = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+
				objectName+"\\"+objectName+"_v"+String.valueOf(bugId)+"_F"+String.valueOf(fold)+".score";
		if( fold==0 )
			pathFilename = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+
					objectName+"\\"+objectName+"_v"+String.valueOf(bugId)+".score";
		List<Double> scoreLTRLst = new ArrayList<>();
		
		try {
			File file = new File(pathFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				String lineTXT = ""; //���ж���
				while((lineTXT = br.readLine())!= null){
					double dv = Double.valueOf(lineTXT); //ֻ��һ�С�
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
	 * @param nTxtno �������У����к�(����Դ������кţ�����.score�ļ�����к�)
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
	
	/** �����������ж������ҳ�����֮�п��ɶ����ֵ��
	 * find the most suspiciousness,return Max(Suspicious) and his code line no.
	 * if two fault have same Suspicious,then return the mini line no.
	 * @param pSuspicious ���������˳�򣬿��ɶȣ�learning to rank �����ֵ
	 * @param pTxtno  .score�ļ����кţ���pSuspicious˳����ͬ����1��ʼ
	 * @param maxFaultSuspi �����ɶ�ֵ
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
