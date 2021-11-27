/**
 * 
 */
package softComplexMetric;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import affiliated.SpectrumStruct;

/** ���ļ�Ϊ��λ��������Ӷȡ�
 * @author Administrator
 *
 */
public class ClassFileComplexValue {
	private String filename; //��Ӧ���ļ��� 
	private List<StatementFeatureStruct> lineComplexs;//���ļ�����������Ϊ��λ��������Ӷȡ�

	/**  	�յĹ��캯��
	 */
	public ClassFileComplexValue() {
		this.filename = "";
		this.lineComplexs = new ArrayList<StatementFeatureStruct>();
	}

	//��Ӧ���ļ��� 
	public String getFilename() {
		return filename;
	}

	//��Ӧ���ļ��� 
	public void setFilename(String filename) {
		this.filename = filename;
	}

	//���ļ�����������Ϊ��λ��������Ӷȡ�
	public List<StatementFeatureStruct> getLineComplexs() {
		return lineComplexs;
	}

	//���ļ�����������Ϊ��λ��������Ӷȡ�
	public void setLineComplexs(List<StatementFeatureStruct> lineComplexs) {
		this.lineComplexs = lineComplexs;
	}
	
	/** ���ļ�����һ��ComplexPriceStruct���кţ����е�������Ӷȣ�
	 * @param cps
	 */
	public void addOneComplexPriceStruct(StatementFeatureStruct cps)
	{
		lineComplexs.add(cps);
	}
	
	//���ļ�����¼���������
	public int getTotalStatement()
	{
		return lineComplexs.size();
	}
	
	//������ļ��ĳ����и��Ӷ�����
	public void readFile(DataInputStream dis) throws IOException
	{
		//�����ַ���
	    int len = dis.readInt();
	    byte []buf = new byte[len];
	    dis.read(buf);
	    filename = new String(buf);   
	    int totalLines = dis.readInt();
	    for(int k=0;k<totalLines;k++ )
        {
    		/*
    		 * �кܶ���������¸��Ӷ�����Ϊ��
    		 * 1������������Щ���   2��C++������ȱ��ͷ�ļ����궨�������
    		 * 3��
    		 * 4��
    		 */
    		StatementFeatureStruct item  = new StatementFeatureStruct();
    		item.readFile(dis);
    		lineComplexs.add(item);
        }//end of for...
	}
	
	//������ļ���������Ӷ�
	public void writeFile(DataOutputStream dos) throws IOException
	{
        //д���ַ���
		dos.writeInt(filename.length());
		dos.writeBytes(filename);
		dos.writeInt(lineComplexs.size());//����������и�����
		for( StatementFeatureStruct cps : lineComplexs )
		{
			cps.writeFile(dos);
		}//end of for...
	}
	
	/**�Ȼ�ȡ�кŶ�Ӧ��StatementFeatureStruct�� ת����RankLibҪ��ĸ�ʽ
	 * @param lineno
	 * @return �Ҳ���������ֵ����Ϊ0
	 */
	public String getRankLibFeatureString(int lineno)
	{
		//ע�⣺liblinear-ranksvmֻ����һ���ո�
		String sinfo = "32:0 33:0 34:0 35:0 36:0 37:0 38:1 39:0 40:0 41:0 42:0 43:0 44:0 45:0";
		for( StatementFeatureStruct cps : lineComplexs )
		{
			if( cps.getLineno()==lineno )
			{
				sinfo = cps.getRankLibFeatureString();
				break;
			}
		}
		return sinfo;
	}
}
