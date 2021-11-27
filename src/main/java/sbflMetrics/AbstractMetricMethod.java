/**
 * 
 */
package sbflMetrics;

/** ExpenseScore �� PbugverScore �Ĺ������ࡣ
 * @author Administrator
 *
 */
public class AbstractMetricMethod implements IMetricMethod{
	public static String ExpenseName = "Expense"; //Expense
	public static String PScore = "PScore"; //PScore	

	protected WorstBestMean wbmResult; //Worst Best & Mean �洢��Ӧ�㷨����ȵĽ����
	protected int[] faultStatms;  //ĳ�汾�Ĺ�������кż��ϡ�����ʱ��Ҫ�õ���

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
	 * @param nStatement ��������䣬���к�
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
