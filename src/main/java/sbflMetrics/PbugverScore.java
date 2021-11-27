/**
 * 
 */
package sbflMetrics;

/** ����Pscore,Pcheck,Pbugver
 * @author Administrator
 *
 */
public class PbugverScore extends AbstractMetricMethod {
	
	public PbugverScore(int[] fss)
	{
		super(fss);
	}
	
	//�ӿڵķ���
	@Override
	public String getMetricName()
	{
		return PScore;//Pcheck,Pbugver
	}
	
	/**
	 * ����WorstBestMean���ԵĽ��
	 */
	//@Override
	public void calculateWorstBestMean(double[] pSuspicious,int[] pStatement, double maxFaultSuspi)
	{
		wbmResult = new WorstBestMean(); 
		//calculate Mean Strategy localization performance
		calMeanStrategy(pSuspicious,pStatement,maxFaultSuspi);
	}
	
	/************************************************************************
    cal Mean strategy
	We assumed that programmer can find bug when they locate locate any fault which
	have same suppiousious.
	************************************************************************/
	private void calMeanStrategy(double[] pSuspicious,int[] pStatement,double maxFaultSuspi)
	{
		int nLargeThanBug = 0;//��maxFaultSuspi������������
		int nEqualBug = 0;//��maxFaultSuspi��ͬ���������ڹ�����䡣
		int nEqualFree = 0;//��maxFaultSuspi��ͬ����������������䡣
		for ( int i=0;i<pSuspicious.length;i++ )
		{
			if ( pSuspicious[i] > maxFaultSuspi )//this line'code must be inspected
				nLargeThanBug++;
			else if ( pSuspicious[i] == maxFaultSuspi )
			{
				if( true==isFaultCode(pStatement[i]) )//this line'code  is  a fault
					nEqualBug++; //may be two fault have same maxFaultSuspi
				else
					nEqualFree++;
			}
			else {}
		}
		//�����������䣬�ܷ��ֹ������ĸ��ʡ�
		int number = WorstBestMean.checkLine.length;
		float[] probBugver = new float[number];
		for( int i=0;i<number;i++ )
		{
			int stmts = WorstBestMean.checkLine[i];
			if( stmts<=nLargeThanBug )
				probBugver[i] = 0;
			else if ( stmts<=(nLargeThanBug+nEqualFree) )
			{
				//������nEqualBug������nEqualFree��������stmts-nLargeThanBug�Σ���������һ������ĸ��ʡ�
				float tmpProb = 1;
				for( int k=0;k<(stmts-nLargeThanBug);k++ )
					tmpProb *= (float)(nEqualFree-k)/(nEqualBug+nEqualFree-k);
				probBugver[i] = 1-tmpProb;
			}
			else //stmts>=(nLargeThanBug+nEqualBug+nEqualFree)
				probBugver[i] = 1;
		}
		wbmResult.copyPscoreResult(probBugver);
	}
}
