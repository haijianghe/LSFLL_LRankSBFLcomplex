/**
 * 
 */
package affiliated;

import common.ProjectConfiguration;
import common.XMLConfigFile;

/**
 * @author Administrator
 * 辅助工厂，依据配置文件创建一些辅助类相关的对象。
 */
public class AffiliatedFactory {
	//_fault.csv .testcase,....
	public static IFaultFile createFaultFileObject(String objectName)
	{
		IFaultFile ffiAgent = null;
		String comment = XMLConfigFile.getCommentOfObject(objectName);
		String directory = XMLConfigFile.getDirectoryOfObject(objectName);
		String tcFilename = ProjectConfiguration.RawDatasetDirectory + "\\" +
				directory +"\\"+objectName+"\\"+objectName+".testcase";
		if( comment.contentEquals("solo") )
		{
			String faultFilename = ProjectConfiguration.RawDatasetDirectory + "\\" +
					directory +"\\"+objectName+"\\"+objectName+".fault";
			String sourceCode = XMLConfigFile.getSourceCodeFileOfObject(objectName);
			ffiAgent = new SoloFaultFile(objectName,faultFilename,tcFilename,sourceCode);
		}
		else if( comment.contentEquals("multi") )
		{
			String faultFilename = ProjectConfiguration.RawDatasetDirectory + "\\" +
					directory +"\\"+objectName+"\\"+objectName+"_fault.csv";
			ffiAgent = new MultiFaultFile(objectName,faultFilename,tcFilename);
		}
		else 
			System.out.println("comment is error, must be solo or multi.");
		return ffiAgent;
	}//end of public static FaultFileInterface createFaultFileObject(String objectName)
	
	//处理.profile的工厂，输出两个类型的对象，单文件profile，多文件profile
	//thVer: 第几个版本，bugid.
	public static IProfileFile createProfileFileObject(String objectName,int thVer)
	{
		IProfileFile pfiAgent = null;
		String comment = XMLConfigFile.getCommentOfObject(objectName);
		String directory = XMLConfigFile.getDirectoryOfObject(objectName);
		String profileFilename = ProjectConfiguration.RawDatasetDirectory + "\\" +
				directory+"\\"+objectName +"\\profile\\"+	objectName+"_v"+String.valueOf(thVer)+".profile";
		if( comment.contentEquals("solo") )
		{
			String sourceCode = XMLConfigFile.getSourceCodeFileOfObject(objectName);
			pfiAgent = new SoloProfileFile(objectName,thVer,profileFilename,sourceCode);
		}
		else if( comment.contentEquals("multi") )
		{
			pfiAgent = new MultiProfileFile(objectName,thVer,profileFilename);
		}
		else 
			System.out.println("comment is error, must be solo or multi.");
		return pfiAgent;
	}//end of createProfileFileObject
}
