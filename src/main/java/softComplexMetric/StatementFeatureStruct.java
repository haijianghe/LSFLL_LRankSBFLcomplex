/**
 * 
 */
package softComplexMetric;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** 以行为单位的软件复杂度
 * @author Administrator
 *
 */
public class StatementFeatureStruct {
	private int lineno; //可执行语句的行号
	private int localvar;  //局部变量个数。
	private int parameter;  //参数个数
	private int attr_global; //属性和全局变量个数
	private byte isloop;  //是否循环语句，0 or 1
	private byte isif;  //是否条件语句，0 or 1
	private byte isreturn;  //是否return语句，0 or 1
	private byte isother;  //是否其它类型语句，0 or 1
	private int oplogic;  //逻辑运算符个数
	private int opalgor;  //除逻辑之外的运算符个数
	private int funcall;  //函数调用个数
	private float covfail; //Aef/F
	private float covpass; //Aep/P
	private float ncovok;  //Anp/(Anf+Anp)
	private float ncovbug; //Anf/(Anf+Anp)
	
	
	public StatementFeatureStruct(int lineno, int localvar, int parameter, int attr_global, byte isloop, byte isif,
			byte isreturn, byte isother, int oplogic, int opalgor, int funcall, float covfail, float covpass,
			float ncovok, float ncovbug) {
		super();
		this.lineno = lineno;
		this.localvar = localvar;
		this.parameter = parameter;
		this.attr_global = attr_global;
		this.isloop = isloop;
		this.isif = isif;
		this.isreturn = isreturn;
		this.isother = isother;
		this.oplogic = oplogic;
		this.opalgor = opalgor;
		this.funcall = funcall;
		this.covfail = covfail;
		this.covpass = covpass;
		this.ncovok = ncovok;
		this.ncovbug = ncovbug;
	}

	/**用-1表示无效的复杂度值。可能多种原因，参看其它地方的说明。
	 *  初始化成员
	 */
	public StatementFeatureStruct() {
		this.lineno = -1;
		this.localvar = -1;
		this.parameter = -1;
		this.attr_global = -1;
		this.isloop = -1;
		this.isif = -1;
		this.isreturn = -1;
		this.isother = -1;
		this.oplogic = -1;
		this.opalgor = -1;
		this.funcall = -1;
		this.covfail = -1;
		this.covpass = -1;
		this.ncovok = -1;
		this.ncovbug = -1;
	}

	/** 有些语句在.profile有程序谱，但是我却解析不到它们的代码，此时将其设为其余语句。其它复杂度=0
	 * @param lineno
	 */
	public void setNotFoundStatement(int lineno)
	{
		this.lineno = lineno;
		localvar = 0;
		parameter = 0;
		attr_global = 0;
		isloop = 0;
		isif = 0;
		isreturn = 0;
		isother = 1;
		oplogic = 0;
		opalgor = 0;
		funcall = 0;
		covfail = 0;
		covpass = 0;
		ncovok = 0;
		ncovbug = 0;
	}
	
	/** 当多条语句写在同一行时，Gcov CppConverage Cobe... 等代码覆盖软件将它们的程序谱记为相同值，
	 * 也就是covfail，covpass,ncovok,ncovbug都相等。
	 * 此时，需要选出复杂度最大值对应的语句作为该行代码的代表。
	 * @return
	 */
	public int getComplexComparisonRule()
	{
		return localvar+parameter+attr_global+oplogic+opalgor+funcall;
	}
	
	//可执行语句的行号
	public int getLineno() {
		return lineno;
	}
	
	public void setLineno(int lineno) {
		this.lineno = lineno;
	}

	
	/** 从文件读入数组，组成StatementFeatureStruct对象
	 * @param dis
	 * @return
	 * @throws IOException 
	 */
	public void readFile(DataInputStream dis) throws IOException
	{
		lineno = dis.readInt();
		localvar = dis.readInt();
		parameter = dis.readInt();
		attr_global = dis.readInt();
		isloop = dis.readByte();
		isif = dis.readByte();
		isreturn = dis.readByte();
		isother = dis.readByte();
		oplogic = dis.readInt();
		opalgor = dis.readInt();
		funcall = dis.readInt();
		covfail = dis.readFloat();
		covpass = dis.readFloat();
		ncovok = dis.readFloat();
		ncovbug = dis.readFloat();
	}
	
	/** 将对象数据存入文件。
	 * @param dos
	 * @throws IOException
	 */
	public void writeFile(DataOutputStream dos) throws IOException
	{
		dos.writeInt(lineno);
		dos.writeInt(localvar);
		dos.writeInt(parameter);
		dos.writeInt(attr_global);
		dos.writeByte(isloop);
		dos.writeByte(isif);
		dos.writeByte(isreturn);
		dos.writeByte(isother);
		dos.writeInt(oplogic);
		dos.writeInt(opalgor);
		dos.writeInt(funcall);
		dos.writeFloat(covfail);
		dos.writeFloat(covpass);
		dos.writeFloat(ncovok);
		dos.writeFloat(ncovbug);
	}
	
	/**
	 * 当前语句的软件静态复杂度值转换为字符串。
	 */
	public String toString()
	{
		StringBuilder infbuilder = new StringBuilder();
		infbuilder.append(lineno+",");
		infbuilder.append(localvar+",");
		infbuilder.append(parameter+",");
		infbuilder.append(attr_global+",");
		infbuilder.append(isloop==1?"loop,":" ,");
		infbuilder.append(isif==1?"if,":" ,");
		infbuilder.append(isreturn==1?"return,":" ,");
		infbuilder.append(isother==1?"rest,":" ,");
		infbuilder.append(oplogic+",");
		infbuilder.append(opalgor+",");
		infbuilder.append(funcall+",");
		infbuilder.append(covfail+",");
		infbuilder.append(covpass+",");
		infbuilder.append(ncovok+",");
		infbuilder.append(ncovbug+".");
		return infbuilder.toString();
	}

	//局部变量个数。
	public int getLocalvar() {
		return localvar;
	}

	public void setLocalvar(int localvar) {
		this.localvar = localvar;
	}

	//参数个数
	public int getParameter() {
		return parameter;
	}

	public void setParameter(int parameter) {
		this.parameter = parameter;
	}

	//属性和全局变量个数
	public int getAttr_global() {
		return attr_global;
	}

	public void setAttr_global(int attr_global) {
		this.attr_global = attr_global;
	}

	//是否循环语句，0 or 1
	public byte getIsloop() {
		return isloop;
	}

	public void setIsloop(byte isloop) {
		this.isloop = isloop;
	}
	//是否条件语句，0 or 1
	public byte getIsif() {
		return isif;
	}

	public void setIsif(byte isif) {
		this.isif = isif;
	}
	
	//是否return语句，0 or 1
	public byte getIsreturn() {
		return isreturn;
	}

	public void setIsreturn(byte isreturn) {
		this.isreturn = isreturn;
	}

	//是否其它类型语句，0 or 1
	public byte getIsother() {
		return isother;
	}

	public void setIsother(byte isother) {
		this.isother = isother;
	}

	//逻辑运算符个数
	public int getOplogic() {
		return oplogic;
	}

	public void setOplogic(int oplogic) {
		this.oplogic = oplogic;
	}

	//除逻辑之外的运算符个数
	public int getOpalgor() {
		return opalgor;
	}

	public void setOpalgor(int opalgor) {
		this.opalgor = opalgor;
	}

	//函数调用个数
	public int getFuncall() {
		return funcall;
	}

	public void setFuncall(int funcall) {
		this.funcall = funcall;
	}

	//Aef/F
	public float getCovfail() {
		return covfail;
	}

	public void setCovfail(float covfail) {
		this.covfail = covfail;
	}

	//Aep/P
	public float getCovpass() {
		return covpass;
	}

	public void setCovpass(float covpass) {
		this.covpass = covpass;
	}

	//Anp/(Anf+Anp)
	public float getNcovok() {
		return ncovok;
	}

	public void setNcovok(float ncovok) {
		this.ncovok = ncovok;
	}

	//Anf/(Anf+Anp)
	public float getNcovbug() {
		return ncovbug;
	}

	public void setNcovbug(float ncovbug) {
		this.ncovbug = ncovbug;
	}
	
	
	/** 转换成RankLib要求的格式
	 * @return
	 */
	public String getRankLibFeatureString()
	{
		int start = 31;
		float lpaZnorm = 20.0f; //localvar,parameter,attr_global的归一化
		float operZnorm = 10.0f; //oplogic,opalgor的归一化
		StringBuilder infbuilder = new StringBuilder();
		//infbuilder.append(start+":"+lineno+" ");
		//注意：liblinear-ranksvm只允许一个空格
		infbuilder.append((start+1)+":"+localvar/lpaZnorm+" "); //32
		infbuilder.append((start+2)+":"+parameter/lpaZnorm+" ");//33
		infbuilder.append((start+3)+":"+attr_global/lpaZnorm+" ");
		infbuilder.append((start+4)+":"+ (isloop==1?"1":"0")   +" ");
		infbuilder.append((start+5)+":"+ (isif==1?"1":"0")     +" ");
		infbuilder.append((start+6)+":"+ (isreturn==1?"1":"0") +" ");
		infbuilder.append((start+7)+":"+ (isother==1?"1":"0")  + " ");//38
		infbuilder.append((start+8)+":"+oplogic/operZnorm+" ");
		infbuilder.append((start+9)+":"+opalgor/operZnorm+" ");
		infbuilder.append((start+10)+":"+funcall/operZnorm+" "); //41
		infbuilder.append((start+11)+":"+covfail+" ");
		infbuilder.append((start+12)+":"+covpass+" ");
		infbuilder.append((start+13)+":"+ncovok+" ");
		infbuilder.append((start+14)+":"+ncovbug);//45
		return infbuilder.toString();
	}
}
