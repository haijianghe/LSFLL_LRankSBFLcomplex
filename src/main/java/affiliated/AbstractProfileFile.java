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
	protected int tcPassed; //��������ͨ����
	protected int tcFailed; //��������δͨ����
	protected int execTotal; //�ð汾���룬��ִ���������
	protected String objectName; //��������
	protected String profileFilename;//��Ŀ¼��.profile�ļ���

	//���캯����
	public AbstractProfileFile(String objectName, String profileFilename) {
		Initial(0,0,0,objectName,profileFilename);
	}

	//���캯����
	public AbstractProfileFile(int tcPassed, int tcFailed, int execTotal,String objectName) 
	{
		Initial(tcPassed, tcFailed, execTotal,objectName,"");
	}


	//ȫ�����ԵĹ��졣
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

	//��������ͨ����
	@Override
	public int getPassed() {
		return tcPassed;
	}

	@Override
	public int getFailed() {
		return tcFailed;
	}

	//��ȡ�ܵĿ�ִ�������Ŀ
	@Override
	public int getTotalExec() {
		return execTotal;
	}

	//���ļ��ĳ�����
	@Override
	public List<FileSpectrum> getSpectrumList() {
		return null;
	}

	//��Ӧ�ļ����кŵĳ����ס�
	@Override
	public SpectrumStruct getSpectrumFileLineno(String filename, int lineno) {
		// TODO Auto-generated method stub
		return null;
	}

	//��ȡ�ļ���filename���д���ĳ�����
	@Override
	public List<SpectrumStruct> getFileSpectrum(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

	/** ��ȡproject�ģ��������İ汾��
	 * @param project
	 * @param inBugidLst
	 * @return �汾���ϡ�
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
				continue; //�ð汾���μӼ��㡣
			inBugidLst.add(bugId);
		}
		int[] inBugids = inBugidLst.stream().mapToInt(Integer::valueOf).toArray();
		return inBugids;
	}
}
