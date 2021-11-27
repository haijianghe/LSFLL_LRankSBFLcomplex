/**
 * 
 */
package sbflMetrics;

/** ֧�� IMetricStrategy, IMetricMethod�ȵļ򵥹���ģʽ��
 * @author Administrator
 *
 */
public class MethodStrategyComplexFactory {
	/** Ŀǰʵ��Expense Acc@n(��Pbugver, or PcheckVer, or Pscore)
	 * @param strMethod
	 * @return
	 */
	public static IMetricMethod createMetricMethodObject(String strMethod,int[] fss)
	{
		if( strMethod.equalsIgnoreCase(AbstractMetricMethod.ExpenseName) )
			return new ExpenseScore(fss);
		else if( strMethod.equalsIgnoreCase(AbstractMetricMethod.PScore) )
			return new PbugverScore(fss);
		else
			return null;
		
	}
}
