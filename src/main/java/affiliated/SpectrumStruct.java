/**
 * 
 */
package affiliated;

/**
 * @author Administrator
 *便于List操作，所以编写了这样一个类，结构一样使用。
 */
public class SpectrumStruct {
	private int lineno; //可执行语句的行号
	private int aep;  //覆盖该语句，成功的测试用例个数。
	private int aef;  //覆盖该语句，失败的测试用例个数。
	
	public SpectrumStruct(int lineno,int aep,int aef)
	{
		this.lineno = lineno;
		this.aep = aep;
		this.aef = aef;
	}
	
	public int getLineNo()
	{
		return lineno;
	}
			
	public int getAep()
	{
		return aep;
	}
	
	public int getAef()
	{
		return aef;
	}
	
	/**依据当前语句的最新覆盖情况，改变该行代码的谱
	 * @param hit  coverage.xml 覆盖次数
	 * @param passed 当前测试用例是否成功,true=通过
	 */
	public void incrementHit(int hit,boolean passed)
	{
		if( hit>0 )
		{
			if( passed )
				aep++;
			else
				aef++;
		}
	}

}
