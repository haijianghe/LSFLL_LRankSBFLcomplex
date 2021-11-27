/**
 * 
 */
package sbflMetrics;

/** 支持 IMetricStrategy, IMetricMethod等的简单工厂模式。
 * @author Administrator
 *
 */
public class MethodStrategyComplexFactory {
	/** 目前实现Expense Acc@n(即Pbugver, or PcheckVer, or Pscore)
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
