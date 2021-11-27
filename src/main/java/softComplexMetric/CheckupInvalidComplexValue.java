/**
 * 
 */
package softComplexMetric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** 从解析结果中查找某行的复杂度值时，
 *     有不为0的是有效值；如果全部为0，是无效值；手工检查代码，看看是何种情况造成无效值。
 *  可能性，包括：
 *  1，{ }等代码， 2，我的解析程序问题，  3，我的算法问题，...
 *  此类用于排除第一种情况。
 * @author Administrator
 *
 */
public class CheckupInvalidComplexValue {
	private List<Integer> lineInvalidValue; //记录无效值的行号。
	private String pathname; //对应文件的绝对路径
	private String filename; //对应文件的名称
	
	public CheckupInvalidComplexValue(String pathname, String filename) {
		super();
		this.pathname = pathname;
		this.filename = filename;
		lineInvalidValue = new ArrayList<Integer>();
	}
	
	/**
	 * 该行是一个无效值。
	 */
	public void addLineno(int lineno)
	{
		lineInvalidValue.add(lineno);
	}
	
	/** 检查行号为lineno，其内容为info，它们是非执行语句吗？
	 * @param lineno 
	 * @param info
	 */
	private void checkNonExecuteStatement(int lineno,String info)
	{
		Iterator<Integer> iterator = lineInvalidValue.iterator();
        while (iterator.hasNext()) 
		{
        	int stmt = iterator.next();
        	if( stmt!=lineno )
        		continue;
        	//发现行号，检查它的内容，如果是{ 或者} break else，则认为是非执行语句，删除它的信息，减轻检查工作量。
        	info = info.trim();
        	if( info.contentEquals("{") || info.contentEquals("}") ||  
        			info.contentEquals("break") || info.contentEquals("else") )
        	{
        		iterator.remove();
        		break; //不再继续
        	}
		}//end of while...
	}
	
	/**
	 *  移除 { }这样的非执行语句。
	 */
	private void removeSpecialLine()
	{
		String pathFilename = pathname+"\\"+filename;
		try {
			File file = new File(pathFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				String lineTXT = br.readLine(); //the prompt
				int lindex = 2; //行号索引
				while((lineTXT = br.readLine())!= null){
					checkNonExecuteStatement(lindex,lineTXT);
					lindex++;
				}
				br.close();
				read.close();
			}
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *  显示无效值的行号，在此之前，先移除非执行语句的行号。
	 */
	public void showInvalidLineno()
	{
		removeSpecialLine();
		Iterator<Integer> iterator = lineInvalidValue.iterator();
        while (iterator.hasNext()) 
        {
        	int stmt = iterator.next();
        	System.out.println(filename+","+stmt+"    complex value is error");
        }
	}
}
