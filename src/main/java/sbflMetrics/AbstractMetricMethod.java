/**
 * 
 */
package sbflMetrics;

/** ExpenseScore 和 PbugverScore 的公共父类。
 * @author Administrator
 *
 */
public class AbstractMetricMethod implements IMetricMethod{
	public static String ExpenseName = "Expense"; //Expense
	public static String PScore = "PScore"; //PScore	

	protected WorstBestMean wbmResult; //Worst Best & Mean 存储对应算法、测度的结果。
	protected int[] faultStatms;  //某版本的故障语句行号集合。计算时需要用到。

	public AbstractMetricMethod()
	{
		wbmResult = null;
		faultStatms = null;
	}

	public AbstractMetricMethod(int[] fs)
	{
		wbmResult = null;
		faultStatms = fs;
	}

	/** Check nStatement is or not fault code line.
	 * @param nStatement 待检查的语句，其行号
	 * @return return TRUE: is fault; else is not fault.
	 */
	public boolean isFaultCode(int nStatement)
	{
		boolean bFault = false;
		//judge this is a fault line.
		for( int item : faultStatms )
		{
			if( nStatement==item )
			{
				bFault = true;
				break;
			}
		}
		return bFault;
	}
	
	@Override
	public WorstBestMean getWorstBestMeanResult() {
		return wbmResult;
	}

	@Override
	public void calculateWorstBestMean(double[] pSuspicious, int[] pStatement, double maxFaultSuspi) {
		
	}

	@Override
	public String getMetricName() {
		return null;
	}

}
