/**
 * 
 */
package softComplexMetric;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import affiliated.SpectrumStruct;

/** 以文件为单位的软件复杂度。
 * @author Administrator
 *
 */
public class ClassFileComplexValue {
	private String filename; //对应的文件名 
	private List<StatementFeatureStruct> lineComplexs;//该文件包含的以行为单位的软件复杂度。

	/**  	空的构造函数
	 */
	public ClassFileComplexValue() {
		this.filename = "";
		this.lineComplexs = new ArrayList<StatementFeatureStruct>();
	}

	//对应的文件名 
	public String getFilename() {
		return filename;
	}

	//对应的文件名 
	public void setFilename(String filename) {
		this.filename = filename;
	}

	//该文件包含的以行为单位的软件复杂度。
	public List<StatementFeatureStruct> getLineComplexs() {
		return lineComplexs;
	}

	//该文件包含的以行为单位的软件复杂度。
	public void setLineComplexs(List<StatementFeatureStruct> lineComplexs) {
		this.lineComplexs = lineComplexs;
	}
	
	/** 该文件增加一个ComplexPriceStruct（行号，该行的软件复杂度）
	 * @param cps
	 */
	public void addOneComplexPriceStruct(StatementFeatureStruct cps)
	{
		lineComplexs.add(cps);
	}
	
	//该文件，记录的语句条数
	public int getTotalStatement()
	{
		return lineComplexs.size();
	}
	
	//读入该文件的程序行复杂度特征
	public void readFile(DataInputStream dis) throws IOException
	{
		//读入字符串
	    int len = dis.readInt();
	    byte []buf = new byte[len];
	    dis.read(buf);
	    filename = new String(buf);   
	    int totalLines = dis.readInt();
	    for(int k=0;k<totalLines;k++ )
        {
    		/*
    		 * 有很多情况，导致复杂度特征为空
    		 * 1，解析不到这些语句   2，C++里由于缺乏头文件，宏定义后的语句
    		 * 3，
    		 * 4，
    		 */
    		StatementFeatureStruct item  = new StatementFeatureStruct();
    		item.readFile(dis);
    		lineComplexs.add(item);
        }//end of for...
	}
	
	//保存该文件的软件复杂度
	public void writeFile(DataOutputStream dos) throws IOException
	{
        //写入字符串
		dos.writeInt(filename.length());
		dos.writeBytes(filename);
		dos.writeInt(lineComplexs.size());//该类包含的行个数。
		for( StatementFeatureStruct cps : lineComplexs )
		{
			cps.writeFile(dos);
		}//end of for...
	}
	
	/**先获取行号对应的StatementFeatureStruct， 转换成RankLib要求的格式
	 * @param lineno
	 * @return 找不到，返回值设置为0
	 */
	public String getRankLibFeatureString(int lineno)
	{
		//注意：liblinear-ranksvm只允许一个空格
		String sinfo = "32:0 33:0 34:0 35:0 36:0 37:0 38:1 39:0 40:0 41:0 42:0 43:0 44:0 45:0";
		for( StatementFeatureStruct cps : lineComplexs )
		{
			if( cps.getLineno()==lineno )
			{
				sinfo = cps.getRankLibFeatureString();
				break;
			}
		}
		return sinfo;
	}
}
