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

/** �����������ļ��Ƿ���#include
 * @author Administrator
 *
 */
public class IncludeFileResolve {
	private List<String> includePaths;  //ͷ�ļ�����Ŀ¼�ļ��ϡ�
	private List<String> includeFiles;  //ͷ�ļ����ļ��ϡ�
	private String pathFilename;      //���������ļ�,��������·��
	
	//���������ļ�,parsingFilename��������·����
	public IncludeFileResolve(String parsingFilename)
	{
		pathFilename = parsingFilename;
		includePaths = new ArrayList<>();
		includeFiles = new ArrayList<>();
	}
	
	//�ҳ�ͷ�ļ�����������������С�
	public void resolve()
	{
		//����ͷ�ļ�·��
		int pos = pathFilename.lastIndexOf('\\');
		String pathname = pathFilename.substring(0,pos);
		includePaths.add(pathname);
		//�ҳ����������ļ��Ƿ���#include
		try {
			File file = new File(pathFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				String lineTXT = br.readLine(); //the prompt
				while((lineTXT = br.readLine())!= null){
					if( !lineTXT.contains("#include") )
						continue;  //���ǰ���ͷ�ļ���Ԥ����
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
	 * @param lineCode ����Ϊ��λ�Ĵ���
	 */
	private void scanInclude(String lineCode)
	{
		String[] strAry = lineCode.split("\\s+"); 
		if( !(strAry[0].contentEquals("#include") ) ||  strAry.length!=2 )
			return; //��һ���ؼ��ֲ���#include�������ַ�����������2.
		byte[] second = strAry[1].getBytes();
		int len = strAry[1].length();
		if( second[0]!='"' || second[len-1]!='"' )
			return; //�ڶ����ַ�������"XXX"��ʽ
		String preprocessFile = strAry[1].substring(1,len-1);
		//���˵�Ŀ¼�����磺#include "opencv2/core/hal/intrin.hpp"
		int pos = preprocessFile.lastIndexOf('\\');
		if( pos<0 )
			pos = preprocessFile.lastIndexOf('/');
		if( pos>0 )
			preprocessFile = preprocessFile.substring(pos+1);
		//ע�⣺�˴�ֻȡ��һ��Ŀ¼������bug������
		File file = new File(includePaths.get(0)+"\\"+preprocessFile);
		if (file.isFile() && file.exists())  //���ļ�Ҫ���ڣ��ŵ�����Ҫ��ͷ�ļ���
			includeFiles.add(preprocessFile);
	}
	
	/**
	 * @return  ͷ�ļ�����Ŀ¼�ļ��ϡ�
	 */
	public String[] getIncludePaths()
	{
		return includePaths.toArray(new String[] {});
	}
	
	/**
	 * @return   ͷ�ļ����ļ��ϡ�
	 */
	public String[] getIncludeFiles()
	{
		return includeFiles.toArray(new String[] {});
	}
}
