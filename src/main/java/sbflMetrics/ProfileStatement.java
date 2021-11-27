/**
 * 
 */
package sbflMetrics;

/**
 * @author Administrator
 *和SpectrumStruct差不多，便于计算，再做一个。
 */
public class ProfileStatement {
	public int no;  //the statement code number. from 1 start
	public int Aep; //the number of passed runs in which "no" is involved.
	public int Aef; //the number of failed runs in which "no" is involved.
	public int Anp; //the number of passed runs in which "no" is not involved.
	public int Anf; //the number of failed runs in which "no" is not involved.
	
	public ProfileStatement()
	{
		no = -1;
		Aep = -1;
		Aef = -1;
		Anp = -1;
		Anf = -1;
	}
}
