/**
 * 
 */
package parseSourceCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/** 检查待解析的文件是否有#include
 * @author Administrator
 *
 */
public class IncludeFileResolve {
	private List<String> includePaths;  //头文件所在目录的集合。
	private List<String> includeFiles;  //头文件名的集合。
	private String pathFilename;      //待解析的文件,包含绝对路径
	
	//待解析的文件,parsingFilename包含绝对路径。
	public IncludeFileResolve(String parsingFilename)
	{
		pathFilename = parsingFilename;
		includePaths = new ArrayList<>();
		includeFiles = new ArrayList<>();
	}
	
	//找出头文件，并将结果存入类中。
	public void resolve()
	{
		//填入头文件路径
		int pos = pathFilename.lastIndexOf('\\');
		String pathname = pathFilename.substring(0,pos);
		includePaths.add(pathname);
		//找出待解析的文件是否有#include
		try {
			File file = new File(pathFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				String lineTXT = br.readLine(); //the prompt
				while((lineTXT = br.readLine())!= null){
					if( !lineTXT.contains("#include") )
						continue;  //不是包含头文件的预处理。
					scanInclude(lineTXT);
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
	 * @param lineCode 以行为单位的代码
	 */
	private void scanInclude(String lineCode)
	{
		String[] strAry = lineCode.split("\\s+"); 
		if( !(strAry[0].contentEquals("#include") ) ||  strAry.length!=2 )
			return; //第一个关键字不是#include，或者字符串个数不是2.
		byte[] second = strAry[1].getBytes();
		int len = strAry[1].length();
		if( second[0]!='"' || second[len-1]!='"' )
			return; //第二个字符串不是"XXX"形式
		String preprocessFile = strAry[1].substring(1,len-1);
		//过滤掉目录，例如：#include "opencv2/core/hal/intrin.hpp"
		int pos = preprocessFile.lastIndexOf('\\');
		if( pos<0 )
			pos = preprocessFile.lastIndexOf('/');
		if( pos>0 )
			preprocessFile = preprocessFile.substring(pos+1);
		//注意：此处只取第一个目录，存在bug隐患。
		File file = new File(includePaths.get(0)+"\\"+preprocessFile);
		if (file.isFile() && file.exists())  //该文件要存在，才当做需要的头文件。
			includeFiles.add(preprocessFile);
	}
	
	/**
	 * @return  头文件所在目录的集合。
	 */
	public String[] getIncludePaths()
	{
		return includePaths.toArray(new String[] {});
	}
	
	/**
	 * @return   头文件名的集合。
	 */
	public String[] getIncludeFiles()
	{
		return includeFiles.toArray(new String[] {});
	}
}
