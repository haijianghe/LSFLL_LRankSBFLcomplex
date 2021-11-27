/**
 * 
 */
package affiliated;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 *
 */
public class AbstractProfileFile implements IProfileFile{
	protected int tcPassed; //测试用例通过数
	protected int tcFailed; //测试用例未通过数
	protected int execTotal; //该版本代码，可执行语句条数
	protected String objectName; //对象名。
	protected String profileFilename;//带目录的.profile文件名

	//构造函数。
	public AbstractProfileFile(String objectName, String profileFilename) {
		Initial(0,0,0,objectName,profileFilename);
	}

	//构造函数。
	public AbstractProfileFile(int tcPassed, int tcFailed, int execTotal,String objectName) 
	{
		Initial(tcPassed, tcFailed, execTotal,objectName,"");
	}


	//全部属性的构造。
	public void Initial(int tcPassed, int tcFailed, int execTotal, String objectName, String profileFilename) 
	{
		this.tcPassed = tcPassed;
		this.tcFailed = tcFailed;
		this.execTotal = execTotal;
		this.objectName = objectName;
		this.profileFilename = profileFilename;
	}

	@Override
	public boolean readProfileFile() {
		return false;
	}

	@Override
	public boolean writeProfileFile() {
		return false;
	}

	@Override
	public void testMe() {
	}

	//测试用例通过数
	@Override
	public int getPassed() {
		return tcPassed;
	}

	@Override
	public int getFailed() {
		return tcFailed;
	}

	//获取总的可执行语句数目
	@Override
	public int getTotalExec() {
		return execTotal;
	}

	//单文件的程序谱
	@Override
	public List<FileSpectrum> getSpectrumList() {
		return null;
	}

	//对应文件、行号的程序谱。
	@Override
	public SpectrumStruct getSpectrumFileLineno(String filename, int lineno) {
		// TODO Auto-generated method stub
		return null;
	}

	//获取文件名filename所有代码的程序谱
	@Override
	public List<SpectrumStruct> getFileSpectrum(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

	/** 获取project的，参与计算的版本。
	 * @param project
	 * @param inBugidLst
	 * @return 版本集合。
	 */
	public static int[] getInclusionBugId(String project)
	{
		List<Integer> inBugidLst = new ArrayList<>();
		IFaultFile ffiAgent = AffiliatedFactory.createFaultFileObject(project);
		if( false==ffiAgent.readFaultFile() )
		{
			System.out.println("Read file "+project+".fault is error.");
			return null;
		}
		
		int vernum = ffiAgent.getVerNo();
		for( int ver=1;ver<=vernum; ver++)
		{
			int bugId = ffiAgent.getBugID(ver);
			if( true==ExcludeVersion.isExcludeVer(project,bugId) )
				continue; //该版本不参加计算。
			inBugidLst.add(bugId);
		}
		int[] inBugids = inBugidLst.stream().mapToInt(Integer::valueOf).toArray();
		return inBugids;
	}
}
