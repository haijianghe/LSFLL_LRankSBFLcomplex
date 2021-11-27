/**
 * 
 */
package sbflMetrics;

/**
 * @author Administrator
 *  评估测度方法的接口。
 * 
 * Expense方法也称为EXAM,P-score类似
 * The EXAM or Expense  score is the percentage of statements in a program that has
 *                   to be examined until the first faulty statement is reached:
 * EXAMscore = Number of statements examined ÷  Total number of statements in the program * 100%:
 * 
 * P-score = based index of P in L ÷   number of predicates in L * 100%;
 * where L is a list of sorted predicates as described above, P is the most fault-relevant predicate to a fault, 
 *         and the notation of 1-based index means the first predicate of L is indexed by 1 (rather than 0).
 * P-score与EXAMscore类似
 */
public interface IMetricMethod {
	WorstBestMean getWorstBestMeanResult();  //该测度worst-best-mean策略的结果。
	void calculateWorstBestMean(double[] pSuspicious,int[] pStatement, double maxFaultSuspi);//计算WorstBestMean策略
	String getMetricName();//测度的名字，如T-score,EXAM,Expense
}
