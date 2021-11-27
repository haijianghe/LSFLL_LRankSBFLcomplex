/**
 * 
 */
package affiliated;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Administrator
 *���������ļ��ĳ�����
 */
public class FileSpectrum {
	private String className;//����
	private String classFilename; //������Ӧ���ļ��� 
	private List<SpectrumStruct> lineCodes;//����������и������ݡ�
	//private int execTotal; //���ļ����룬��ִ���������

	/**  	�յĹ��캯��
	 */
	public FileSpectrum() {
		this.className = "";
		this.classFilename = "";
		//execTotal = 0;
		this.lineCodes = new ArrayList<SpectrumStruct>();
	}

	//����һ��
	public void addLineSpectrum(SpectrumStruct ss)
	{
		lineCodes.add(ss);
	}
	
	//���Ӷ��
	public void addLineCodes(List<SpectrumStruct> lineLst) {
		for( SpectrumStruct lh : lineLst )
			lineCodes.add(lh);
	}
	
	/**
	 * @param lineno ��ִ�������к�
	 * @param hit  coverage.xml ���Ǵ���
	 * @param passed ��ǰ���������Ƿ�ɹ�,true=ͨ��
	 */
	public void assembleLineSpectrum(int lineno,int hit,boolean passed)
	{
		boolean found = false;
		for( SpectrumStruct ss : lineCodes )
		{
			if( ss.getLineNo()==lineno )
			{
				found = true;
				ss.incrementHit(hit, passed);
				break;
			}
		}
		if( !found )
		{//�˴�Ҫ���أ�Ϊʲô�������ǰû�е��кŰ���
			SpectrumStruct adds = new SpectrumStruct(lineno,0,0);
			adds.incrementHit(hit, passed);
			lineCodes.add(adds);
			System.out.println("FileSpectrum: why?????????????????new lineno???????????????????????????????????");
		}
	}
	
	//���ļ�����ִ���������
	public int getTotalExec()
	{
		//return execTotal;
		return lineCodes.size();
	}
	
	//������ļ��ĳ�����
	public void readFile(DataInputStream dis) throws IOException
	{
		//�����ַ���
	    int len = dis.readInt();
	    byte []buf = new byte[len];
	    dis.read(buf);
	    className = new String(buf);   
	    //�����ַ���
	    len = dis.readInt();
	    buf = new byte[len];
	    dis.read(buf);
	    classFilename = new String(buf);   
	    int totalLines = dis.readInt();
	    for(int k=0;k<totalLines;k++ )
        {
    		int lineno = dis.readInt();
    		int aep = dis.readInt();
    		int aef = dis.readInt();
    		SpectrumStruct item  = new SpectrumStruct(lineno,aep,aef);
    		lineCodes.add(item);
        }
	}
	 
    
	//������ļ��ĳ�����
	public void writeFile(DataOutputStream dos) throws IOException
	{
        //д���ַ���
		dos.writeInt(className.length());
		dos.writeBytes(className);
		dos.writeInt(classFilename.length());
		dos.writeBytes(classFilename);
		dos.writeInt(lineCodes.size());//����������и�����
		for( SpectrumStruct ss : lineCodes )
		{
			dos.writeInt(ss.getLineNo());
			dos.writeInt(ss.getAep());
			dos.writeInt(ss.getAef());
		}
	}
	
	//��List<SpectrumStruct> lineCodes�����кŴ�С��������
	//��������򷽷���MainProcess���в��Է�����
	public void sortLineNo()
	{
		List<SpectrumStruct> sortedSpecta = new ArrayList<SpectrumStruct>();
		int nsize = lineCodes.size();
		for( int i=0;i<nsize;i++ )
		{
			int lineno = Integer.MAX_VALUE; //��ǰ������С���к�
			int index = 0;//��С�кŶ�Ӧ��������
			for( int j=0;j<lineCodes.size();j++ ) //lineCodes.size()��仯��
			{
				int jlno = lineCodes.get(j).getLineNo();
				if( jlno<lineno )
				{
					lineno = jlno;
					index = j;
				}
			}
			SpectrumStruct ss = lineCodes.get(index);
			sortedSpecta.add(ss);
			lineCodes.remove(index);
		}
		//����õĶ��С�
		lineCodes = sortedSpecta;
	}
	
	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the classFilename
	 */
	public String getClassFilename() {
		return classFilename;
	}

	/**
	 * @return the lineCodes
	 */
	public List<SpectrumStruct> getLineCodes() {
		return lineCodes;
	}

	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @param classFilename the classFilename to set
	 */
	public void setClassFilename(String classFilename) {
		this.classFilename = classFilename;
	}

	/**
	 * @param lineCodes the lineCodes to set
	 */
	public void setLineCodes(List<SpectrumStruct> lineCodes) {
		this.lineCodes = lineCodes;
	}

	/** ���кŲ��ҵ�������
	 * @param lineno
	 * @return ���δ�ҵ����򷵻�ֵ�к�Ϊ-1.
	 */
	public SpectrumStruct getSpectrum(int lineno)
	{
		SpectrumStruct speS = new SpectrumStruct(-1,0,0);
		for( SpectrumStruct ss : lineCodes )
		{
			if( ss.getLineNo()==lineno )
			{
				speS = ss;
				break;
			}
		}
		return speS;
	}
}
