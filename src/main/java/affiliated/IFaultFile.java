/**
 * 
 */
package affiliated;

/**
 * @author Administrator
 *
 */
public interface IFaultFile {
	boolean readFaultFile(); //读_fault.csv或者.fault文件。
	int getVerNo();//获取版本总的数目。
	int getBugID(int index);//获取版本号，对单个文件的.profile，就是自然序列值；多个文件的.profile，是数据集的bugid.
	void testMe(); //测试文件内容读入是否正确。
	boolean checkTestcasePassedFailed(); //检查.testcase文件与.fault(_fault.csv)文件是否一致
	boolean checkProfileAndFaultFile();  //检查.profile文件与.fault(_fault.csv)文件是否一致
	int[] getFaultLinesVer(int index);//获取某个版本的故障语句数组。index from 1,....VerNo
	String[] getFaultFilesVer(int index);//获取某个版本的故障语句所在文件的集合。
	boolean isFaultStatement(int index,String filename,int lineno);//filename和lineno指定的语句是故障语句吗？
}
