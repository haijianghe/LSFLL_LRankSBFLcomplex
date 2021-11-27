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

/** ����FaultSeed.h
 *    �����ж���궨�壬����ȥ��ע�ͱ�־�󣬻�������Ӧ�汾��bug
 * @author Administrator
 *
 */
public class FaultSeedIncludeFile {
	/** ���ѵ�lineno�еĺ궨�壬����bug
	 * @param pathname FaultSeed.h �ľ���·��
	 * @param lineno �к�
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
	
	/** �����ļ�
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
	
	
	/** ���ļ��������ݣ����޸ĵ�lineno�У�
	 * ȥ��ע�ͱ�־ 
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
 
    /** ������д���ļ�
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
