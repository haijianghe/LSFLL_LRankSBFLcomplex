/**
 * 
 */
package softComplexMetric;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

/** 处理FaultSeed.h
 *    里面有多个宏定义，按行去掉注释标志后，会启动相应版本的bug
 * @author Administrator
 *
 */
public class FaultSeedIncludeFile {
	/** 唤醒第lineno行的宏定义，启动bug
	 * @param pathname FaultSeed.h 的绝对路径
	 * @param lineno 行号
	 * @return
	 */
	public static boolean awakenIncludeBug(String pathname,int lineno)
	{
		boolean result = true;
		String strSource = pathname+"\\FaultSeeds_backup.h";
		String strDest = pathname+"\\FaultSeeds.h";
		result = copyFile(strSource,strDest);
		String content = readFile(strDest,lineno);
		writeFile(strDest,content);
		return result;
		
	}
	
	/** 拷贝文件
	 * @param strSource
	 * @param strDest
	 * @return
	 * @throws IOException 
	 */
	private static boolean copyFile(String strSource,String strDest) 
	{
		File fileSource = new File(strSource);
		File fileDest = new File(strDest);
		try {
			if( fileDest.exists() )
				fileDest.delete();
			Files.copy(fileSource.toPath(),fileDest.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	
	/** 从文件读入内容，并修改第lineno行，
	 * 去掉注释标志 
	 * @param filePath
	 * @param lineno
	 * @return
	 */
	private static String readFile(String filePath, int lineno) 
	{
        BufferedReader br = null;
        String line;
        StringBuffer bufAll = new StringBuffer();
        try {
            br = new BufferedReader(new FileReader(filePath));
            int index = 1;
            while ((line = br.readLine()) != null) {
            	if( index++==lineno )
            	{
            		line = line.replace("/*", " ");
            		line = line.replace("*/", " ");
            	}
                bufAll.append(line+"\n");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (br != null) {
                try {
                    br.close();
                }catch (IOException e){
                    br = null;
                }
            }
        }
        return bufAll.toString();
    }
 
    /** 将内容写入文件
     * @param filePath
     * @param content
     */
    private static void writeFile(String filePath, String content) 
    {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(filePath));
            bw.write(content);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (bw != null){
                try{
                    bw.close();
                }
                catch (IOException e) {
                    bw = null;
                }
            }//end of if
        }
    }
}
