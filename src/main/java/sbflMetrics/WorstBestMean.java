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
 * Mean(Exam,Expense,p-Score),平均策略可由(Best+Worst)/2计算得出。
 * 假设故障语句的可疑度值最大为maxsup，不失一般性，假定其可疑度值比maxsup大的可执行语句有x个，可疑度值与maxsup相同的可执行语句有y个
 *   则Best策略，需要检查x+1个，Worst策略，需要检查x+y个，Mean=(Best+Worst)/2=(2x+y+1)/2
 *   按照平均策略，需要检查x+(1+2+...+y)/y = (2x+y+1)/2
 *   
 * 平均策略不算好的工具，加入有两个算法，
 *         第一个算法的Best = x ,  Worst = y;  第二个算法的Best = x-t ,  Worst = y+t;
 *      虽然两个算法的Mean相同，但是第二个算法的范围特别宽，感觉上，第一个算法性能更稳定。  
 */

public class WorstBestMean {
	public int nBestIcl; //the inspected code lines of Best strategy.
	public float fBestIcp; //the percentage of inspected code lines of Best strategy.
	public int nWorstIcl; //the inspected code lines of Worst strategy.
	public float fWorstIcp; //the percentage of inspected code lines of Worst strategy.
	public float fnMeanIcl; //the inspected code lines of Mean strategy. 先浮点数，再整数结果。
	public float fMeanIcp; //the percentage of inspected code lines of Mean strategy.

	//Pscore,Pcheck,Pbugver的结果，
	public float[] probBugver;//只取Mean的结果，忽略Worst和Best。
	/*
	 * checkLine和probBugver的长度必须相同
	 * 当检查{1,2,5,10}条语句时，能找到bug的概率；
	 * probBugver的值通常为0或1；当故障语句和bug-free语句有相同的可疑度和认知复杂度时，就计算概率。
	 */
	public final static int[] checkLine = {1,2,3,5,10};
	
	/**
	 *  空的构造函数。
	 */
	public WorstBestMean() {
		//super();
		fMeanIcp = 0.0f;
		int dimen = checkLine.length;
		probBugver = new float[dimen];
	}
	
	//初始化为0.
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
	/** 拷贝另一个对象的值。
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
	
	
	//prob拷贝到probBugver
	public void copyPscoreResult(float[] prob) {
		int number = prob.length;
		for( int i=0;i<number;i++ )
			probBugver[i] = prob[i];
	}

	
	//显示Exam结果。
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
	
	//求平均值。
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
	
	//求平均值。
	public static WorstBestMean averageExam(List<WorstBestMean> wbsList)
	{
		int len = wbsList.size();
		WorstBestMean[] wbsAry = new WorstBestMean[len];
		wbsList.toArray(wbsAry);
		return averageExam(wbsAry);
	}
	
	/**求聚合平均值,并非算术平均值。 
	 * 比如有项目A、B、C；它们的性能分别为wbm1,wbm2,wbm3；它们的版本数为v1,v2,v3;则聚合平均值为
	 *      (wbm1*v1+wbm2*v2+wbm3*v3)/(v1+v2+v3)
	 * @param stgList  
	 * @param versions  versions是对应项目顺序的版本数。
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

		//所有聚合的 版本总数。
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
	
	//针对同一个项目求平均值。
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
		
	//显示Pscore结果。
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

	//显示Pscore结果。
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
		
	/**针对不同项目求平均值。求聚合对象的平均值。
	 * 比如有项目A、B、C；它们的性能分别为bugv1,bugv2,bugv3；它们的版本数为v1,v2,v3;则聚合平均值为
	 *      (bugv1*v1+bugv2*v2+bugv3*v3)/(v1+v2+v3)
	 * @param aggResults  
	 * @return  这些对象的平均Acc@n 结果
	 */
	public static WorstBestMean averageAggrAccur(List<WorstBestMean> aggResults,List<Integer> versions)
	{
		//所有聚合的 版本总数。
		int subs = versions.size();
		int numberOfBugId = 0;
		for( int p=0;p<subs;p++ )
			numberOfBugId += versions.get(p); 

		//初始化总的概率，将来除以版本总数。
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

	/**针对不同项目求平均值。求聚合对象的平均值。
	 * 比如有项目A、B、C；它们的性能分别为bugv1,bugv2,bugv3；它们的版本数为v1,v2,v3;则聚合平均值为
	 *      (bugv1*v1+bugv2*v2+bugv3*v3)/(v1+v2+v3)
	 * @param aggResults  
	 * @return  这些对象的平均Acc@n 结果
	 */
	public static float[] averageAggrPscore(List<float[]> aggResults,List<Integer> versions)
	{
		//所有聚合的 版本总数。
		int subs = versions.size();
		int numberOfBugId = 0;
		for( int p=0;p<subs;p++ )
			numberOfBugId += versions.get(p); 

		//初始化总的概率，将来除以版本总数。
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
	 * 求WorstBestMean数组的平均值。
	 * EXAM 和 PScore 不会同时有。
	 * 不过，十折学习时，需要计算几次的平均值；此时，不好判断是平均EXAM 还是 平均PScore；干脆两者都平均。
	 */
	public static WorstBestMean averageExamAndPscore(List<WorstBestMean> wbsList)
	{
		final WorstBestMean wbsResult = new  WorstBestMean();
		//填入EXAM平均值
		WorstBestMean wbsTmp = averageExam(wbsList);
		wbsResult.copyExamResult(wbsTmp);
		//填入PScore平均值
		float[] pBugs = averagePscore(wbsList);
		wbsResult.copyPscoreResult(pBugs);
		return wbsResult;
	}
}

