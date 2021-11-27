/**
 * 
 */
package sbflMetrics;

/**
 * @author Administrator
 *            Expense方法也称为EXAM
 */
public class ExpenseScore extends AbstractMetricMethod {
	
	public ExpenseScore(int[] fss)
	{
		super(fss);
	}
	

	//接口的方法
	@Override
	public String getMetricName()
	{
		return ExpenseName;//Expense=EXAM
	}
	
	/**
	 * 计算WorstBestMean策略的结果
	 */
	//@Override
	public void calculateWorstBestMean(double[] pSuspicious,int[] pStatement, double maxFaultSuspi)
	{
		wbmResult = new WorstBestMean(); 
		//calculate Best localization performance
		calBestStrategy(pSuspicious,maxFaultSuspi);
		//calculate  Worst  localization performance
		calWorstStrategy(pSuspicious,pStatement,maxFaultSuspi);
		//calculate Mean Strategy localization performance
		calMeanStrategy(pSuspicious,pStatement,maxFaultSuspi);
	}
	
		
	/************************************************************************
    cal best strategy
	We assumed that programmer can find bug when they inspect any fault code which
	have same suppiousious.
	************************************************************************/
	private void calBestStrategy(double[] pSuspicious,double maxFaultSuspi)
	{
		int nIpspected = 0;//the inspected code line
		for ( double item :  pSuspicious)
		{
			if ( item > maxFaultSuspi )//this line'code must be inspected
				nIpspected++;
		}
		nIpspected++;//You must inspect the fault statement.
		//maxFaultSuspi is most suspiou,so > it ,will fault-free code.
		wbmResult.nBestIcl = nIpspected;
		wbmResult.fBestIcp = (float)nIpspected/pSuspicious.length;
	}

	/************************************************************************
	         cal Worst strategy
	We assumed that programmer can find bug when they locate locate any fault which
	have same suppiousious.
	************************************************************************/
	private void calWorstStrategy(double[] pSuspicious,int[] pStatement,double maxFaultSuspi)
	{
		int nIpspected = 0;//the inspected code line
		for ( int i=0;i<pSuspicious.length;i++ )
		{
			if( true==isFaultCode(pStatement[i]) )//this line'code  is  a fault
				continue; //may be two fault have same maxFaultSuspi
			if ( pSuspicious[i]>=maxFaultSuspi )//this line'code must be inspected
				nIpspected++;
		}
		nIpspected++;//You must inspect the fault statement.
		wbmResult.nWorstIcl = nIpspected;
		wbmResult.fWorstIcp = (float)nIpspected/pSuspicious.length;
	}
	
	/************************************************************************
    cal Mean strategy
	We assumed that programmer can find bug when they locate locate any fault which
	have same suppiousious.
	************************************************************************/
	/*private void calMeanStrategy(double[] pSuspicious,int[] pStatement,double maxFaultSuspi)
	{
		int nBestIpspected = 0;//the inspected code line of Best
		int nSameScore = 0; //可疑度值等于maxFaultSuspi的语句条数。
		int nFaultLine = 0; //在nSameScore条语句中，有多少条是故障语句。此值通常等于1.
		for ( int i=0;i<pSuspicious.length;i++ )
		{
			if ( pSuspicious[i] > maxFaultSuspi )//this line'code must be inspected
				nBestIpspected++;
			else if( pSuspicious[i]==maxFaultSuspi )
			{
				nSameScore ++;//统计可疑度值等于maxFaultSuspi的语句条数。
				//nFaultLine：语句条数，其可疑度值等于maxFaultSuspi，它也是故障语句。
				if( true==isFaultCode(pStatement[i]) )//this line'code  is  a fault)
					nFaultLine ++;
			}
			else
			{}
		}
		wbmResult.fnMeanIcl = nBestIpspected+(nSameScore+1.0f)/(nFaultLine+1.0f);
		wbmResult.fMeanIcp = wbmResult.fnMeanIcl/pSuspicious.length;
	}*/
	private void calMeanStrategy(double[] pSuspicious,int[] pStatement,double maxFaultSuspi)
	{
		int nWorstIpspected = 0;//the inspected code line of Worst
		int nBestIpspected = 0;//the inspected code line of Best
		for ( int i=0;i<pSuspicious.length;i++ )
		{
			if ( pSuspicious[i] > maxFaultSuspi )//this line'code must be inspected
				nBestIpspected++;
			/*
			 * 几个地方都有下面的语句，先排除true==isFaultCode(pStatement[i])，后面用nWorstIpspected++;再加上；
			 * 这么做，因为可能多个故障语句(fault statement)有相同的可疑度值maxFaultSuspi
			 */
			if( true==isFaultCode(pStatement[i]) )//this line'code  is  a fault
				continue; //may be two fault have same maxFaultSuspi
			if ( pSuspicious[i]>=maxFaultSuspi )//this line'code must be inspected
				nWorstIpspected++;
		}
		nWorstIpspected++;//You must inspect the fault statement.
		nBestIpspected++;
		wbmResult.fnMeanIcl = (float)(nBestIpspected+nWorstIpspected)/2.0f;
		wbmResult.fMeanIcp = (float)(nBestIpspected+nWorstIpspected)/(2*pSuspicious.length);
	}
}
