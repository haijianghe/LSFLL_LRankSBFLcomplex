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

/** 记录某个项目的一个版本的特征，及其错误语句。
 * @author Administrator
 *
 */
public class FeatureOfProjectVer {
	private String objectName;//项目名称
	private int bugId; //项目版本，并非自然顺序的verTh
	private int qid; //document query id.只有读.train文件才需要记录，因为拼接训练文件时用到。读.testing文件用不到。
	//以.testing文件里的行号为顺序
	private List<double[]> featureStmts; //版本所有文件的所有语句，每条语句的特征向量，按照feature文件里指定的顺序存放。
	//记录.testing文件里的行号，从1开始。
	private int[] faultIndexs;  //错误语句所在的index,匹配featureStmts的索引。从1开始
	
	public FeatureOfProjectVer(String project,int bugid)
	{
		objectName = project;
		bugId = bugid;
		faultIndexs = null;
		featureStmts = new ArrayList<>();
		qid = 0;
	}

	//错误语句所在的index,匹配featureStmts的索引。
	public int[] getFaultIndex()
	{
		return faultIndexs;
	}
	
	//项目版本，并非自然顺序的verTh
	public int getBugId()
	{
		return bugId;
	}
	
	/**
	 * @param wModel
	 * @param posFearture，存储1-45，指明哪些特征用上了。
	 * 特别注意： posFearture一定要按照顺序存放。
	 * @return
	 */
	public double[] computerScore(double[] wModel,int[] posFearture)
	{
		int statmates = featureStmts.size();//该版本可执行语句条数
		double[] scores = new double[statmates];
		for( int st=0;st< statmates;st++ )
		{
			double total = 0;
			double[] feature = featureStmts.get(st);
			int dim = wModel.length;//向量维数。
			for( int k=0;k<dim;k++ )
				total += wModel[k]*feature[posFearture[k]-1];
			scores[st] = total;
		}
		return scores;
	}
	
	/**
	 * 读入LibLinear_XX_vXX.testing文件，将标签为1（故障语句）的行号（从1开始）,存入faultIndexs
	 * 读入XXX.train文件，将标签为1（故障语句）的行号（从1开始）,存入faultIndexs
	 * 将特征按照顺序，读入featureStmts
	 */
	public void readTrainTestingFile(boolean testing)
	{
		//读LibLinear_...一样，都有label
		String pathFilename = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+
				objectName+"\\"+objectName+"_v"+String.valueOf(bugId)+".testing";
		if( testing==false ) //读XXX.train文件
			pathFilename = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+
					objectName+"\\LibLinear_"+objectName+"_v"+String.valueOf(bugId)+".train";
		List<Integer> labelFaultLst = new ArrayList<>();
		
		try {
			File file = new File(pathFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				String lineTXT = ""; //逐行读入
				int index = 1; //记录行号，从1开始
				while((lineTXT = br.readLine())!= null){
					String[]  strAry = lineTXT.split("\\s+"); //允许多个空格分割字符串
					int label = Integer.valueOf(strAry[0]); //1=bug,0=free
					if( label==1 )
						labelFaultLst.add(index);
					else if( label==0 )
						{}
					else
						System.out.println(pathFilename+" first lable is 0 or 1");
					//读qid
					String[] qidStrs = strAry[1].split(":");
					qid = Integer.valueOf(qidStrs[1]); //只有.train文件才用到。
					//读特征值
					double[] feature = new double[strAry.length-2];
					for( int k=0;k<strAry.length-2;k++ )
					{
						String[] fivStrs = 	strAry[k+2].split(":"); //冒号分割索引：特征值
						double dv = Double.valueOf(fivStrs[1]);
						feature[k] = dv;
					}
					 featureStmts.add(feature);//增加一个实例的特征。一行
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
	
	//stmt: 文件里的行号，从0开始。
	//faultIndexs 从1开始
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
	
	/** 将当前的 特征 依据featureIndexs，写入bw
	 * @param bw
	 * @param featureIndexs 其值从1到45.
	 * @throws IOException
	 */
	public void writeToTrainFile(BufferedWriter bw ,int[] featureIndexs)  throws IOException
	{
		int lines = featureStmts.size(); //当前特征文件的语句行数。主要针对训练集特征文件
		for( int stmt=0; stmt<lines; stmt++ )
		{
			//注意： LibLinear-RankSVM文件格式，值允许一个空格
			StringBuilder sb = new StringBuilder();
			if( isFalutLine(stmt) ) //是故障语句
				sb.append("1 "); //label
			else
				sb.append("0 ");
			sb.append("qid:"+qid+" ");// qid:XX
			double[] vInstances = featureStmts.get(stmt); //stmt行的实例特征向量。
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
