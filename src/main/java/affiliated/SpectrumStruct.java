/**
 * 
 */
package affiliated;

/**
 * @author Administrator
 *����List���������Ա�д������һ���࣬�ṹһ��ʹ�á�
 */
public class SpectrumStruct {
	private int lineno; //��ִ�������к�
	private int aep;  //���Ǹ���䣬�ɹ��Ĳ�������������
	private int aef;  //���Ǹ���䣬ʧ�ܵĲ�������������
	
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
	
	/**���ݵ�ǰ�������¸���������ı���д������
	 * @param hit  coverage.xml ���Ǵ���
	 * @param passed ��ǰ���������Ƿ�ɹ�,true=ͨ��
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
