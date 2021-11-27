/**
 * 
 */
package sbflMetrics;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 *
 */
/**
 * Mean(Exam,Expense,p-Score),ƽ�����Կ���(Best+Worst)/2����ó���
 * ����������Ŀ��ɶ�ֵ���Ϊmaxsup����ʧһ���ԣ��ٶ�����ɶ�ֵ��maxsup��Ŀ�ִ�������x�������ɶ�ֵ��maxsup��ͬ�Ŀ�ִ�������y��
 *   ��Best���ԣ���Ҫ���x+1����Worst���ԣ���Ҫ���x+y����Mean=(Best+Worst)/2=(2x+y+1)/2
 *   ����ƽ�����ԣ���Ҫ���x+(1+2+...+y)/y = (2x+y+1)/2
 *   
 * ƽ�����Բ���õĹ��ߣ������������㷨��
 *         ��һ���㷨��Best = x ,  Worst = y;  �ڶ����㷨��Best = x-t ,  Worst = y+t;
 *      ��Ȼ�����㷨��Mean��ͬ�����ǵڶ����㷨�ķ�Χ�ر���о��ϣ���һ���㷨���ܸ��ȶ���  
 */

public class WorstBestMean {
	public int nBestIcl; //the inspected code lines of Best strategy.
	public float fBestIcp; //the percentage of inspected code lines of Best strategy.
	public int nWorstIcl; //the inspected code lines of Worst strategy.
	public float fWorstIcp; //the percentage of inspected code lines of Worst strategy.
	public float fnMeanIcl; //the inspected code lines of Mean strategy. �ȸ������������������
	public float fMeanIcp; //the percentage of inspected code lines of Mean strategy.

	//Pscore,Pcheck,Pbugver�Ľ����
	public float[] probBugver;//ֻȡMean�Ľ��������Worst��Best��
	/*
	 * checkLine��probBugver�ĳ��ȱ�����ͬ
	 * �����{1,2,5,10}�����ʱ�����ҵ�bug�ĸ��ʣ�
	 * probBugver��ֵͨ��Ϊ0��1������������bug-free�������ͬ�Ŀ��ɶȺ���֪���Ӷ�ʱ���ͼ�����ʡ�
	 */
	public final static int[] checkLine = {1,2,3,5,10};
	
	/**
	 *  �յĹ��캯����
	 */
	public WorstBestMean() {
		//super();
		fMeanIcp = 0.0f;
		int dimen = checkLine.length;
		probBugver = new float[dimen];
	}
	
	//��ʼ��Ϊ0.
	public void setZero() 
	{
		nBestIcl = 0;
		fBestIcp = 0;
		nWorstIcl = 0;
		fWorstIcp = 0;
		fnMeanIcl = 0;
		fMeanIcp = 0;
		for( int i=0;i<probBugver.length;i++ )
			probBugver[i] = 0;
	}
	/** ������һ�������ֵ��
	 * @param wbm
	 */
	public void copyExamResult(WorstBestMean wbm)
	{
		nBestIcl = wbm.nBestIcl;
		fBestIcp = wbm.fBestIcp;
		nWorstIcl = wbm.nWorstIcl;
		fWorstIcp = wbm.fWorstIcp;
		fnMeanIcl = wbm.fnMeanIcl;
		fMeanIcp = wbm.fMeanIcp;
	}
	
	
	//prob������probBugver
	public void copyPscoreResult(float[] prob) {
		int number = prob.length;
		for( int i=0;i<number;i++ )
			probBugver[i] = prob[i];
	}

	
	//��ʾExam�����
	public void showExam()
	{
		System.out.print("inspected code lines(%) of Best strategy:  ");
		if( fBestIcp>=0.5 )
			System.out.println("("+nBestIcl+",****************************"+fBestIcp*100+"****************************)");
		else
			System.out.println("("+nBestIcl+","+fBestIcp*100+")");
		System.out.print("        Worst:  ");
		if( fWorstIcp>=0.5 )
			System.out.println("("+nWorstIcl+",****************************"+fWorstIcp*100+"****************************)");
		else
			System.out.println("("+nWorstIcl+","+fWorstIcp*100+")");
		System.out.print("        Mean strategy:   ");
		if( fMeanIcp>=0.5 )
			System.out.println("("+fnMeanIcl+",****************************"+fMeanIcp*100+"****************************)");
		else
			System.out.println("("+fnMeanIcl+","+fMeanIcp*100+")");
	}
	
	//��ƽ��ֵ��
	public static WorstBestMean averageExam(WorstBestMean[] wbsAry)
	{
		final WorstBestMean wbsTmp = new  WorstBestMean();
		int bestIcls = 0;
		float bestIcp = 0.0f;
		int worstIcl  = 0;
		float worstIcp = 0.0f;
		float  meanIcl = 0.0f;
		float meanIcp = 0.0f;
		for( WorstBestMean wbs : wbsAry)
		{
			bestIcls += wbs.nBestIcl;
			bestIcp += wbs.fBestIcp;
			worstIcl += wbs.nWorstIcl;
			worstIcp += wbs.fWorstIcp;
			meanIcl += wbs.fnMeanIcl;
			meanIcp += wbs.fMeanIcp;
		}
		int number = wbsAry.length;
 		wbsTmp.nBestIcl = bestIcls/number;
		wbsTmp.fBestIcp = bestIcp/number;
		wbsTmp.nWorstIcl = worstIcl/number;
		wbsTmp.fWorstIcp = worstIcp/number;
		wbsTmp.fnMeanIcl = meanIcl/number;
		wbsTmp.fMeanIcp= meanIcp/number;
		return wbsTmp;
	}
	
	//��ƽ��ֵ��
	public static WorstBestMean averageExam(List<WorstBestMean> wbsList)
	{
		int len = wbsList.size();
		WorstBestMean[] wbsAry = new WorstBestMean[len];
		wbsList.toArray(wbsAry);
		return averageExam(wbsAry);
	}
	
	/**��ۺ�ƽ��ֵ,��������ƽ��ֵ�� 
	 * ��������ĿA��B��C�����ǵ����ֱܷ�Ϊwbm1,wbm2,wbm3�����ǵİ汾��Ϊv1,v2,v3;��ۺ�ƽ��ֵΪ
	 *      (wbm1*v1+wbm2*v2+wbm3*v3)/(v1+v2+v3)
	 * @param stgList  
	 * @param versions  versions�Ƕ�Ӧ��Ŀ˳��İ汾����
	 * @return
	 */
	public static WorstBestMean aggrMeanExam(List<WorstBestMean> stgList,List<Integer> versions) {
		final WorstBestMean wbsTmp = new  WorstBestMean();
		int bestIcls = 0;
		float bestIcp = 0.0f;
		int worstIcl  = 0;
		float worstIcp = 0.0f;
		float  meanIcl = 0.0f;
		float meanIcp = 0.0f;

		//���оۺϵ� �汾������
		int subs = versions.size();
		int numberOfBugId = 0;
		for( int p=0;p<subs;p++ )
			numberOfBugId += versions.get(p); 
		
		for( int i=0;i<subs;i++ )
		{
			WorstBestMean wbs = (WorstBestMean) stgList.get(i);
			int cver = versions.get(i);
			bestIcls += wbs.nBestIcl*cver;
			bestIcp += wbs.fBestIcp*cver;
			worstIcl += wbs.nWorstIcl*cver;
			worstIcp += wbs.fWorstIcp*cver;
			meanIcl += wbs.fnMeanIcl*cver;
			meanIcp += wbs.fMeanIcp*cver;
		}
		wbsTmp.nBestIcl = bestIcls/numberOfBugId;
		wbsTmp.fBestIcp = bestIcp/numberOfBugId;
		wbsTmp.nWorstIcl = worstIcl/numberOfBugId;
		wbsTmp.fWorstIcp = worstIcp/numberOfBugId;
		wbsTmp.fnMeanIcl = meanIcl/numberOfBugId;
		wbsTmp.fMeanIcp= meanIcp/numberOfBugId;
		return wbsTmp;
	}
	
	//���ͬһ����Ŀ��ƽ��ֵ��
	public static float[] averagePscore(List<WorstBestMean> asbList)
	{
		int verLen = asbList.size();
		int dimen = checkLine.length;
		float[] totalPbug = new float[dimen];
		for( int i=0;i<dimen;i++ )
			totalPbug[i] = 0;
		for( WorstBestMean asb: asbList)
		{
			float[] bugProbability = asb.probBugver;
			for( int i=0;i<dimen;i++ )
				totalPbug[i] += bugProbability[i];
		}
		for( int i=0;i<dimen;i++ )
			totalPbug[i] = totalPbug[i]/verLen;
		return totalPbug;
	}
		
	//��ʾPscore�����
	public void showPscore()
	{
		int dimen = checkLine.length;
		System.out.print("Mean Pscore :  ");
		for( int i=0;i<dimen;i++ )
		{
			System.out.print("("+checkLine[i]+","+probBugver[i]+")    ");
		}
		System.out.println(".");
	}

	//��ʾPscore�����
	public static void showPscore(float[] pScore)
	{
		int dimen = checkLine.length;
		System.out.print("Mean Pscore :  ");
		for( int i=0;i<dimen;i++ )
		{
			System.out.print("("+checkLine[i]+","+pScore[i]+")    ");
		}
		System.out.println(".");
	}
		
	/**��Բ�ͬ��Ŀ��ƽ��ֵ����ۺ϶����ƽ��ֵ��
	 * ��������ĿA��B��C�����ǵ����ֱܷ�Ϊbugv1,bugv2,bugv3�����ǵİ汾��Ϊv1,v2,v3;��ۺ�ƽ��ֵΪ
	 *      (bugv1*v1+bugv2*v2+bugv3*v3)/(v1+v2+v3)
	 * @param aggResults  
	 * @return  ��Щ�����ƽ��Acc@n ���
	 */
	public static WorstBestMean averageAggrAccur(List<WorstBestMean> aggResults,List<Integer> versions)
	{
		//���оۺϵ� �汾������
		int subs = versions.size();
		int numberOfBugId = 0;
		for( int p=0;p<subs;p++ )
			numberOfBugId += versions.get(p); 

		//��ʼ���ܵĸ��ʣ��������԰汾������
		int dimen = checkLine.length;
		float[] totalPbug = new float[dimen];
		for( int i=0;i<dimen;i++ )
			totalPbug[i] = 0;

		for( int i=0;i<subs;i++ )
		{
			float[] bugProbability = aggResults.get(i).probBugver;
			int cver = versions.get(i);
			for( int t=0;t<dimen;t++ )
				totalPbug[t] += bugProbability[t]*cver;
		}
		for( int t=0;t<dimen;t++ )
			totalPbug[t] = totalPbug[t]/numberOfBugId;
		
		final WorstBestMean abmTmp = new WorstBestMean();
		abmTmp.copyPscoreResult(totalPbug);
		return abmTmp;
	}

	/**��Բ�ͬ��Ŀ��ƽ��ֵ����ۺ϶����ƽ��ֵ��
	 * ��������ĿA��B��C�����ǵ����ֱܷ�Ϊbugv1,bugv2,bugv3�����ǵİ汾��Ϊv1,v2,v3;��ۺ�ƽ��ֵΪ
	 *      (bugv1*v1+bugv2*v2+bugv3*v3)/(v1+v2+v3)
	 * @param aggResults  
	 * @return  ��Щ�����ƽ��Acc@n ���
	 */
	public static float[] averageAggrPscore(List<float[]> aggResults,List<Integer> versions)
	{
		//���оۺϵ� �汾������
		int subs = versions.size();
		int numberOfBugId = 0;
		for( int p=0;p<subs;p++ )
			numberOfBugId += versions.get(p); 

		//��ʼ���ܵĸ��ʣ��������԰汾������
		int dimen = checkLine.length;
		float[] totalPbug = new float[dimen];
		for( int i=0;i<dimen;i++ )
			totalPbug[i] = 0;

		for( int i=0;i<subs;i++ )
		{
			float[] bugProbability = aggResults.get(i);
			int cver = versions.get(i);
			for( int t=0;t<dimen;t++ )
				totalPbug[t] += bugProbability[t]*cver;
		}
		for( int t=0;t<dimen;t++ )
			totalPbug[t] = totalPbug[t]/numberOfBugId;
		
		return totalPbug;
	}
	
	/*
	 * ��WorstBestMean�����ƽ��ֵ��
	 * EXAM �� PScore ����ͬʱ�С�
	 * ������ʮ��ѧϰʱ����Ҫ���㼸�ε�ƽ��ֵ����ʱ�������ж���ƽ��EXAM ���� ƽ��PScore���ɴ����߶�ƽ����
	 */
	public static WorstBestMean averageExamAndPscore(List<WorstBestMean> wbsList)
	{
		final WorstBestMean wbsResult = new  WorstBestMean();
		//����EXAMƽ��ֵ
		WorstBestMean wbsTmp = averageExam(wbsList);
		wbsResult.copyExamResult(wbsTmp);
		//����PScoreƽ��ֵ
		float[] pBugs = averagePscore(wbsList);
		wbsResult.copyPscoreResult(pBugs);
		return wbsResult;
	}
}

