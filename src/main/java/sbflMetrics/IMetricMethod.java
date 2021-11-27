/**
 * 
 */
package sbflMetrics;

/**
 * @author Administrator
 *  ������ȷ����Ľӿڡ�
 * 
 * Expense����Ҳ��ΪEXAM,P-score����
 * The EXAM or Expense  score is the percentage of statements in a program that has
 *                   to be examined until the first faulty statement is reached:
 * EXAMscore = Number of statements examined ��  Total number of statements in the program * 100%:
 * 
 * P-score = based index of P in L ��   number of predicates in L * 100%;
 * where L is a list of sorted predicates as described above, P is the most fault-relevant predicate to a fault, 
 *         and the notation of 1-based index means the first predicate of L is indexed by 1 (rather than 0).
 * P-score��EXAMscore����
 */
public interface IMetricMethod {
	WorstBestMean getWorstBestMeanResult();  //�ò��worst-best-mean���ԵĽ����
	void calculateWorstBestMean(double[] pSuspicious,int[] pStatement, double maxFaultSuspi);//����WorstBestMean����
	String getMetricName();//��ȵ����֣���T-score,EXAM,Expense
}
