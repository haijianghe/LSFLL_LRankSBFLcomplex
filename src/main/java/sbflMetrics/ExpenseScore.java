/**
 * 
 */
package sbflMetrics;

/**
 * @author Administrator
 *            Expense����Ҳ��ΪEXAM
 */
public class ExpenseScore extends AbstractMetricMethod {
	
	public ExpenseScore(int[] fss)
	{
		super(fss);
	}
	

	//�ӿڵķ���
	@Override
	public String getMetricName()
	{
		return ExpenseName;//Expense=EXAM
	}
	
	/**
	 * ����WorstBestMean���ԵĽ��
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
		int nSameScore = 0; //���ɶ�ֵ����maxFaultSuspi�����������
		int nFaultLine = 0; //��nSameScore������У��ж������ǹ�����䡣��ֵͨ������1.
		for ( int i=0;i<pSuspicious.length;i++ )
		{
			if ( pSuspicious[i] > maxFaultSuspi )//this line'code must be inspected
				nBestIpspected++;
			else if( pSuspicious[i]==maxFaultSuspi )
			{
				nSameScore ++;//ͳ�ƿ��ɶ�ֵ����maxFaultSuspi�����������
				//nFaultLine���������������ɶ�ֵ����maxFaultSuspi����Ҳ�ǹ�����䡣
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
			 * �����ط������������䣬���ų�true==isFaultCode(pStatement[i])��������nWorstIpspected++;�ټ��ϣ�
			 * ��ô������Ϊ���ܶ���������(fault statement)����ͬ�Ŀ��ɶ�ֵmaxFaultSuspi
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
