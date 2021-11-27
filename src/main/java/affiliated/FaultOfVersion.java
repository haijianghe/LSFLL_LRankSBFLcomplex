/**
 * 
 */
package affiliated;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * 记录单个版本的故障： 故障文件及其故障行号。
 * 一个版本，可能有多个文件有故障，而每个文件又可能有多条语句故障。
 */
public class FaultOfVersion {
	String[] faultFiles;   //该版本的故障语句所在文件的集合。faultFiles和faultLines的二维顺序保持一致。
	int[] faultLines;  //该版本的故障语句行号集合。
	int numberOfFault; //故障语句条数。
	
	public FaultOfVersion()
	{
		faultFiles = null;
		faultLines = null;
		numberOfFault = 0;
	}
	
	//给定一个版本的
	public void assign(List<String> fileLst,List<Integer> linenoLst)
	{
		numberOfFault = linenoLst.size();
		faultLines = new int[numberOfFault]; //the pointer of the number of statement
		Integer[] Istat =  linenoLst.toArray(new Integer[numberOfFault]);
		for(int k=0;k<numberOfFault;k++)
			faultLines[k] = Istat[k].intValue();

		faultFiles = (String[])fileLst.toArray(new String[numberOfFault]);
	}

	//故障语句条数。
	public int getNumberOfFault() {
		return numberOfFault;
	}

	//故障语句条数。
	public void setNumberOfFault(int numberOfFault) {
		this.numberOfFault = numberOfFault;
	}

	//该版本的故障语句所在文件的集合。
	public String[] getFaultFiles() {
		return faultFiles;
	}

	//该版本的故障语句行号集合。
	public int[] getFaultLines() {
		return faultLines;
	}
	
	//filename和lineno指定的语句是故障语句吗？
	public boolean isFaultStatement(String filename,int lineno)
	{
		for(int k=0;k<numberOfFault;k++)
		{
			if( faultFiles[k].contentEquals(filename)
					&& faultLines[k]==lineno )
				return true;
		}
		return false;
	}
		
}
