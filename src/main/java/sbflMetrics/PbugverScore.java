/**
 * 
 */
package sbflMetrics;

/** 计算Pscore,Pcheck,Pbugver
 * @author Administrator
 *
 */
public class PbugverScore extends AbstractMetricMethod {
	
	public PbugverScore(int[] fss)
	{
		super(fss);
	}
	
	//接口的方法
	@Override
	public String getMetricName()
	{
		return PScore;//Pcheck,Pbugver
	}
	
	/**
	 * 计算WorstBestMean策略的结果
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
		int nLargeThanBug = 0;//比maxFaultSuspi大的语句条数。
		int nEqualBug = 0;//与maxFaultSuspi相同，但是属于故障语句。
		int nEqualFree = 0;//与maxFaultSuspi相同，但是属于正常语句。
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
		//检查若干条语句，能发现故障语句的概率。
		int number = WorstBestMean.checkLine.length;
		float[] probBugver = new float[number];
		for( int i=0;i<number;i++ )
		{
			int stmts = WorstBestMean.checkLine[i];
			if( stmts<=nLargeThanBug )
				probBugver[i] = 0;
			else if ( stmts<=(nLargeThanBug+nEqualFree) )
			{
				//类似于nEqualBug个红球，nEqualFree个白球，摸stmts-nLargeThanBug次，至少摸到一个红球的概率。
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
