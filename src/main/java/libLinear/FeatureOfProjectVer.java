/**
 * 
 */
package libLinear;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import common.ProjectConfiguration;

/** ��¼ĳ����Ŀ��һ���汾�����������������䡣
 * @author Administrator
 *
 */
public class FeatureOfProjectVer {
	private String objectName;//��Ŀ����
	private int bugId; //��Ŀ�汾��������Ȼ˳���verTh
	private int qid; //document query id.ֻ�ж�.train�ļ�����Ҫ��¼����Ϊƴ��ѵ���ļ�ʱ�õ�����.testing�ļ��ò�����
	//��.testing�ļ�����к�Ϊ˳��
	private List<double[]> featureStmts; //�汾�����ļ���������䣬ÿ��������������������feature�ļ���ָ����˳���š�
	//��¼.testing�ļ�����кţ���1��ʼ��
	private int[] faultIndexs;  //����������ڵ�index,ƥ��featureStmts����������1��ʼ
	
	public FeatureOfProjectVer(String project,int bugid)
	{
		objectName = project;
		bugId = bugid;
		faultIndexs = null;
		featureStmts = new ArrayList<>();
		qid = 0;
	}

	//����������ڵ�index,ƥ��featureStmts��������
	public int[] getFaultIndex()
	{
		return faultIndexs;
	}
	
	//��Ŀ�汾��������Ȼ˳���verTh
	public int getBugId()
	{
		return bugId;
	}
	
	/**
	 * @param wModel
	 * @param posFearture���洢1-45��ָ����Щ���������ˡ�
	 * �ر�ע�⣺ posFeartureһ��Ҫ����˳���š�
	 * @return
	 */
	public double[] computerScore(double[] wModel,int[] posFearture)
	{
		int statmates = featureStmts.size();//�ð汾��ִ���������
		double[] scores = new double[statmates];
		for( int st=0;st< statmates;st++ )
		{
			double total = 0;
			double[] feature = featureStmts.get(st);
			int dim = wModel.length;//����ά����
			for( int k=0;k<dim;k++ )
				total += wModel[k]*feature[posFearture[k]-1];
			scores[st] = total;
		}
		return scores;
	}
	
	/**
	 * ����LibLinear_XX_vXX.testing�ļ�������ǩΪ1��������䣩���кţ���1��ʼ��,����faultIndexs
	 * ����XXX.train�ļ�������ǩΪ1��������䣩���кţ���1��ʼ��,����faultIndexs
	 * ����������˳�򣬶���featureStmts
	 */
	public void readTrainTestingFile(boolean testing)
	{
		//��LibLinear_...һ��������label
		String pathFilename = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+
				objectName+"\\"+objectName+"_v"+String.valueOf(bugId)+".testing";
		if( testing==false ) //��XXX.train�ļ�
			pathFilename = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+
					objectName+"\\LibLinear_"+objectName+"_v"+String.valueOf(bugId)+".train";
		List<Integer> labelFaultLst = new ArrayList<>();
		
		try {
			File file = new File(pathFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				String lineTXT = ""; //���ж���
				int index = 1; //��¼�кţ���1��ʼ
				while((lineTXT = br.readLine())!= null){
					String[]  strAry = lineTXT.split("\\s+"); //�������ո�ָ��ַ���
					int label = Integer.valueOf(strAry[0]); //1=bug,0=free
					if( label==1 )
						labelFaultLst.add(index);
					else if( label==0 )
						{}
					else
						System.out.println(pathFilename+" first lable is 0 or 1");
					//��qid
					String[] qidStrs = strAry[1].split(":");
					qid = Integer.valueOf(qidStrs[1]); //ֻ��.train�ļ����õ���
					//������ֵ
					double[] feature = new double[strAry.length-2];
					for( int k=0;k<strAry.length-2;k++ )
					{
						String[] fivStrs = 	strAry[k+2].split(":"); //ð�ŷָ�����������ֵ
						double dv = Double.valueOf(fivStrs[1]);
						feature[k] = dv;
					}
					 featureStmts.add(feature);//����һ��ʵ����������һ��
					index++;
				}
				br.close();
				read.close();
			}
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
		}
		faultIndexs =  labelFaultLst.stream().mapToInt(Integer::valueOf).toArray();
	}
	
	//stmt: �ļ�����кţ���0��ʼ��
	//faultIndexs ��1��ʼ
	private boolean isFalutLine(int stmt)
	{
		boolean isFault = false;
		for( int index: faultIndexs )
			if( index==(stmt+1) )
			{
				isFault = true;
				break;
			}
		return isFault;
	}
	
	/** ����ǰ�� ���� ����featureIndexs��д��bw
	 * @param bw
	 * @param featureIndexs ��ֵ��1��45.
	 * @throws IOException
	 */
	public void writeToTrainFile(BufferedWriter bw ,int[] featureIndexs)  throws IOException
	{
		int lines = featureStmts.size(); //��ǰ�����ļ��������������Ҫ���ѵ���������ļ�
		for( int stmt=0; stmt<lines; stmt++ )
		{
			//ע�⣺ LibLinear-RankSVM�ļ���ʽ��ֵ����һ���ո�
			StringBuilder sb = new StringBuilder();
			if( isFalutLine(stmt) ) //�ǹ������
				sb.append("1 "); //label
			else
				sb.append("0 ");
			sb.append("qid:"+qid+" ");// qid:XX
			double[] vInstances = featureStmts.get(stmt); //stmt�е�ʵ������������
			for( int i=0;i<featureIndexs.length;i++ )
			{
				double dv = vInstances[featureIndexs[i]-1];
				sb.append(i+1);
				sb.append(":");
				sb.append(dv);
				sb.append(" ");
			}
			bw.append(sb.toString().trim()+"\n");
			bw.flush();
		}//end of for( int stmt=0;stmt<lines;stmt++ )
	}
}
