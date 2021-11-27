/**
 * 
 */
package sbflMetrics;

import affiliated.SpectrumStruct;

/**
 * @author Administrator
 *
 */
public class SBFLTradTechnique {
	private static String[] algorithmNames = {"Optimal","Opass","Kulczynski2","Wong3", //1-4
			"Tarantula","Jaccard","Ochiai","RusselRao","Ample","GP2","GP13",           //5-11
			"Hamann", "SorensenDice","Dice","Kulczynski1","SimpleMatching","Sokal",    //12-17
			"M1","M2","RogersTanimoto","Goodman","Hamming","Euclid",                   //18-23
			 "Overlap","Anderberg","Zoltar","Wong1","Wong2","SBI","DStar2","GP10"      //24-31
			 };

	public static String[] getAlgorithmNames()
	{
		return algorithmNames;
	}
	/**Calculate  Suspicious 
	* @param strAlgorithm SBFL算法名称  
	* @param stProfile  程序谱。
	* @return
	*/
	public static double algorithmSuspicious(String strAlgorithm,ProfileStatement stProfile)
	{
		double pSuspi = 0;
		//"Optimal","Opass","Kulczynski2","Wong3","Tarantula","Jaccard","Ochiai","RusselRao","Ample","GP2","GP13"
		if(  strAlgorithm.equalsIgnoreCase("Optimal") )
			pSuspi = optimalSuspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Opass") )
			pSuspi = opassSuspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Kulczynski2") )
			pSuspi = kulczynski2Suspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Wong3") )
			pSuspi = wong3Suspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Tarantula") )
			pSuspi = tarantulaSuspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Jaccard") )
			pSuspi = jaccardSuspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Ample") )
			pSuspi = ampleSuspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("GP2") )
			pSuspi = gp2Suspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("GP13") )
			pSuspi = gp13Suspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Ochiai") )//Ochiai
			pSuspi = ochiaiSuspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("RusselRao") )
			pSuspi = russelRaoSuspicious(stProfile);
		//"Hamann", "SorensenDice","Dice",	"Kulczynski1","SimpleMatching","Sokal","M1","M2","RogersTanimoto","Goodman",	
		else if(  strAlgorithm.equalsIgnoreCase("Hamann") )
			pSuspi = hamann(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("SorensenDice") )
			pSuspi = sorensenDice(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Dice") )
			pSuspi = dice(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Kulczynski1") )
			pSuspi = kulczynski1(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("SimpleMatching") )
			pSuspi = simpleMatching(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Sokal") )
			pSuspi = sokal(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("M1") )
			pSuspi = m1(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("M2") )
			pSuspi = m2(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("RogersTanimoto") )
			pSuspi = rogersTanimoto(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Goodman") )
			pSuspi = goodman(stProfile);
		//"Hamming","Euclid", "Overlap","Anderberg","Zoltar","Wong1","Wong2","SBI","DStar2","GP10"
		else if(  strAlgorithm.equalsIgnoreCase("Hamming") )
			pSuspi = hamming(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Euclid") )
			pSuspi = euclid(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Overlap") )
			pSuspi = overlap(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Anderberg") )
			pSuspi = anderberg(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Zoltar") )
			pSuspi = zoltar(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Wong1") )
			pSuspi = wong1(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Wong2") )
			pSuspi = wong2(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("SBI") )
			pSuspi = sBI(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("DStar2") )
			pSuspi = dStar2(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("GP10") )
			pSuspi = gP10(stProfile);
		else
			pSuspi = 0;
		return pSuspi;
	}
	
	
	//Calculate Optimal Suspicious 
	private static double optimalSuspicious(ProfileStatement stProfile)
	{
		//totalFailed & totalPassed don't be 0.
		int totalFailed = stProfile.Aef+stProfile.Anf;
		if( stProfile.Aef<totalFailed )
			return -1.0;
		else
			return (double)stProfile.Anp;
	}
	
	//Calculate OptimalPass(Naish et al.2011) Suspicious 
	//OP2
	private static double opassSuspicious(ProfileStatement stProfile)
	{
		//totalFailed & totalPassed don't be 0.
		int totalPassed = stProfile.Aep+stProfile.Anp;
		double fitem = (double)stProfile.Aep/(totalPassed+1.0);
		return stProfile.Aef-fitem;
	}
	
	//Calculate Kulczynski2 Suspicious
	private static double kulczynski2Suspicious(ProfileStatement stProfile) 
	{
		if( 0==stProfile.Aef )
			return 0.0;
		//totalFailed & totalPassed don't be 0.
		int totalFailed = stProfile.Aef+stProfile.Anf;
		int totalCoverage = stProfile.Aep+stProfile.Aef;
		double f1 = (double)stProfile.Aef/totalFailed;
		double f2 = (double)stProfile.Aef/totalCoverage;
		return 0.5*(f1+f2);
	}
	
	//Calculate Wong3 Suspicious 
	private static double wong3Suspicious(ProfileStatement stProfile)
	{
		//Wong1: stProfile.Aef
		//Wong3 ,first calculation h
		double h = 0;
		if ( stProfile.Aep<=2 )//0.1.2
			h = stProfile.Aep;
		else if( stProfile.Aep<=10 )
			h = 2+0.1*(stProfile.Aep-2);
		else //stProfile.Aep>10
			h = 2.8+0.001*(stProfile.Aep-10);
		return stProfile.Aef-h;
	}
	
	//Calculate Tarantula Suspicious 
	private static double tarantulaSuspicious(ProfileStatement stProfile)
	{
		if( 0==stProfile.Aef )
			return 0.0;
		//totalFailed & totalPassed don't be 0.
		int totalFailed = stProfile.Aef+stProfile.Anf;
		int totalPassed = stProfile.Aep+stProfile.Anp;
		double fz = (double)stProfile.Aef/totalFailed;
		return fz/(fz+(double)stProfile.Aep/totalPassed);
	}
	
	//Calculate Jaccard Suspicious 
	private static double jaccardSuspicious(ProfileStatement stProfile)
	{
		//totalFailed & totalPassed don't be 0.
		int totalFailed = stProfile.Aef+stProfile.Anf;
		double fm = (double)(stProfile.Aep+totalFailed);
		return (double)stProfile.Aef/fm;
	}
	
	//Calculate Ample Suspicious 
	private static double ampleSuspicious(ProfileStatement stProfile)
	{
		//totalFailed & totalPassed don't be 0.
		int totalFailed = stProfile.Aef+stProfile.Anf;
		int totalPassed = stProfile.Aep+stProfile.Anp;
		double tff = (double)stProfile.Aef/totalFailed;
		double tpp = (double)stProfile.Aep/totalPassed;
		return Math.abs(tff-tpp);
	}
	
	//Calculate GP2 Suspicious 
	//依据原作者论文的公式，在FLUCCS论文中公式不同，认为是FLUCCS出错。
	private static double gp2Suspicious(ProfileStatement stProfile)
	{
		double tmp = stProfile.Aef+Math.sqrt(stProfile.Anp);
		return 2*tmp+Math.sqrt(stProfile.Aep);
	}

	//Calculate GP13 Suspicious 
	private static double gp13Suspicious(ProfileStatement stProfile)
	{
		if( 0==stProfile.Aef )
			return 0.0;
		double tmp = 1.0/(2*stProfile.Aep+stProfile.Aef);
		return stProfile.Aef*(1+tmp);
	}
	
	//Calculate Ochiai Suspicious 
	private static double ochiaiSuspicious(ProfileStatement stProfile)
	{
		if( 0==stProfile.Aef )
			return 0.0;
		//totalFailed & totalPassed don't be 0.
		int totalFailed = stProfile.Aef+stProfile.Anf;
		int totalCoverage = stProfile.Aep+stProfile.Aef;
		return (double)stProfile.Aef/Math.sqrt(totalFailed*totalCoverage);
	}
	
	//Calculate RusselRao Suspicious 
	private static double russelRaoSuspicious(ProfileStatement stProfile)
	{
		//totalFailed & totalPassed don't be 0.
		int totalFailed = stProfile.Aef+stProfile.Anf;
		int totalPassed = stProfile.Aep+stProfile.Anp;
		return (double)stProfile.Aef/(totalFailed+totalPassed);
	}
	
	//Calculate Hamann Suspicious 
	private static double hamann(ProfileStatement stProfile)
	{
		//totalFailed & totalPassed don't be 0.
		int totalFailed = stProfile.Aef+stProfile.Anf;
		int totalPassed = stProfile.Aep+stProfile.Anp;
		return (double)(stProfile.Aef+stProfile.Anp-stProfile.Anf-stProfile.Aep)/(totalFailed+totalPassed);
	}
	
	//Calculate SorensenDice Suspicious 
	private static double sorensenDice(ProfileStatement stProfile)
	{
		double tmp = 2*stProfile.Aef;
		return tmp/(tmp+stProfile.Anf+stProfile.Aep);
	}
	
	//Calculate Dice Suspicious 
	private static double dice(ProfileStatement stProfile)
	{
		double tmp = 2*stProfile.Aef;
		return tmp/(stProfile.Aef+stProfile.Anf+stProfile.Aep);
	}	
	
	//Calculate Kulczynski1 Suspicious 
	private static double kulczynski1(ProfileStatement stProfile)
	{
		double tmp = stProfile.Aep+stProfile.Anf;
		if( tmp<=0 )
			return stProfile.Aef;
		else
			return stProfile.Aef/tmp;
	}		
	
	//Calculate SimpleMatching Suspicious 
	private static double simpleMatching(ProfileStatement stProfile)
	{
		//totalFailed & totalPassed don't be 0.
		int totalFailed = stProfile.Aef+stProfile.Anf;
		int totalPassed = stProfile.Aep+stProfile.Anp;
		return (double)(stProfile.Aef+stProfile.Anp)/(totalFailed+totalPassed);
	}	
	
	//Calculate Sokal Suspicious 
	private static double sokal(ProfileStatement stProfile)
	{
		double tmp  = 2.0*(stProfile.Aef+stProfile.Anp);
		return tmp/(tmp+stProfile.Anf+stProfile.Aep);
	}	
	
	//Calculate M1 Suspicious 
	private static double m1(ProfileStatement stProfile)
	{
		double tmp = stProfile.Aep+stProfile.Anf;
		if( tmp<=0 )
			return (double)stProfile.Aef+stProfile.Anp;
		return (stProfile.Aef+stProfile.Anp)/tmp;
	}	
	
	//Calculate M2 Suspicious 
	private static double m2(ProfileStatement stProfile)
	{
		//totalFailed & totalPassed don't be 0.
		double tmp = stProfile.Aep+stProfile.Anf;
		return stProfile.Aef/(stProfile.Aef+stProfile.Anp+2*tmp);
	}	
	
	//Calculate RogersTanimoto Suspicious 
	private static double rogersTanimoto(ProfileStatement stProfile)
	{
		double tmp1 = stProfile.Aef+stProfile.Anp;
		double tmp2 = stProfile.Aep+stProfile.Anf;
		return tmp1/(tmp1+2*tmp2);
	}	
	
	//Calculate Goodman Suspicious 
	private static double goodman(ProfileStatement stProfile)
	{
		double tmp = stProfile.Aep+stProfile.Anf;
		return (2.0*stProfile.Aef-tmp)/(tmp+2.0*stProfile.Aef);
	}
	
	//Calculate Hamming Suspicious 
	private static double hamming(ProfileStatement stProfile)
	{
		return stProfile.Anp+stProfile.Aef;
	}	
	
	//Calculate Euclid Suspicious 
	private static double euclid(ProfileStatement stProfile)
	{
		return Math.sqrt(stProfile.Anp+stProfile.Aef);
	}		
	
	//Calculate Overlap Suspicious 
	private static double overlap(ProfileStatement stProfile)
	{
		double tmp = Math.min(stProfile.Aef,stProfile.Aep);
		tmp = Math.min(tmp, stProfile.Anf);
		if( tmp<=0 )
			return stProfile.Aef;
		else
			return (double)(stProfile.Aef)/tmp;
	}		
	
	//Calculate Anderberg Suspicious 
	private static double anderberg(ProfileStatement stProfile)
	{
		double tmp = 2.0*(stProfile.Aep+stProfile.Anf);
		return stProfile.Aef/(stProfile.Aef+tmp);
	}		
	
	//Calculate Zoltar Suspicious 
	private static double zoltar(ProfileStatement stProfile)
	{
		if( stProfile.Aef<=0 )
			return 0;
		double tmp = (10000.0*stProfile.Aep*stProfile.Anf)/(double)(stProfile.Aef);
		return stProfile.Aef/(tmp+stProfile.Aep+stProfile.Anf+stProfile.Aef);
	}		
	
	//Calculate Wong1 Suspicious 
	private static double wong1(ProfileStatement stProfile)
	{
		return (double)(stProfile.Aef);
	}		
	
	//Calculate Wong2 Suspicious 
	private static double wong2(ProfileStatement stProfile)
	{
		return (double)(stProfile.Aef-stProfile.Aep);
	}		
	
	//Calculate SBI Suspicious 
	private static double sBI(ProfileStatement stProfile)
	{
		double tmp = stProfile.Aep;
		if( tmp<=0 )
			return 1;
		else 
			return 1.0-tmp/(tmp+stProfile.Aef);
	}
	
	//Calculate DStar2 Suspicious 
	private static double dStar2(ProfileStatement stProfile)
	{
		double tmp = stProfile.Aep+stProfile.Anf;
		if( tmp<=0 )
			return stProfile.Aef*stProfile.Aef; //stProfile.Anf=0时，此时有stProfile.Aef=F
		else 
			return stProfile.Aef*stProfile.Aef/tmp;
	}
	
	//Calculate GP10 Suspicious 
	private static double gP10(ProfileStatement stProfile)
	{
		double tmp = 1.0;
		if( stProfile.Anp>0 )
			tmp = 1.0/stProfile.Anp;
		return Math.sqrt(Math.abs(stProfile.Aef-tmp));
	}	
	
	/** 计算所有SBFL技术的值，组成RankLib特征。
	 * @param stProfile
	 * @return 特征组成的字符串，特征号从1到31
	 */
	public static String getAllTechniqueFeature(ProfileStatement stProfile)
	{
		StringBuilder sb = new StringBuilder();
		int sbfls = algorithmNames.length;
		for( int k=1;k<=sbfls;k++ )
		{
			sb.append(k);
			sb.append(":");
			double asv = algorithmSuspicious(algorithmNames[k-1],stProfile);
			sb.append(asv);
			sb.append(" ");//注意：liblinear-ranksvm只允许一个空格
		}
		return sb.toString().trim();
	}
	
	/** 通过SBFLTradTechnique计算所有SBFL技术的特征，组成字符串
	 * @param spects
	 * @param tcPassed
	 * @param tcFailed
	 * @param lineno
	 * @return
	 */
	public static String getSBFLTradFeature(SpectrumStruct spects, int tcPassed,int tcFailed,int lineno)
	{
		ProfileStatement stProfile = new ProfileStatement();
		stProfile.no = lineno; //多余。
		stProfile.Aep = spects.getAep();
		stProfile.Aef = spects.getAef();
		stProfile.Anp = tcPassed-stProfile.Aep;
		stProfile.Anf = tcFailed-stProfile.Aef;
		return getAllTechniqueFeature(stProfile);
	}
}
