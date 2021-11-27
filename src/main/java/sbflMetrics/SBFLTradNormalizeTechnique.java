/**
 * 
 */
package sbflMetrics;

import affiliated.SpectrumStruct;

/** 归一化的SBFLTradTechnique
 * 与NormalizeTradSBFL重复了
 *
 */
public class SBFLTradNormalizeTechnique {
	/**Calculate  Suspicious 
	* @param strAlgorithm SBFL算法名称  
	* @param stProfile  程序谱。
	* @return
	*/
	public static double zAlgorithmSuspicious(String strAlgorithm,ProfileStatement stProfile)
	{
		double pSuspi = 0;
		//"Optimal","Opass","Kulczynski2","Wong3","Tarantula","Jaccard","Ochiai","RusselRao","Ample","GP2","GP13"
		if(  strAlgorithm.equalsIgnoreCase("Optimal") )
			pSuspi = zOptimalSuspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Opass") )
			pSuspi = zOpassSuspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Kulczynski2") )
			pSuspi = zKulczynski2Suspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Wong3") )
			pSuspi = zWong3Suspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Tarantula") )
			pSuspi = zTarantulaSuspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Jaccard") )
			pSuspi = zJaccardSuspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Ample") )
			pSuspi = zAmpleSuspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("GP2") )
			pSuspi = zGp2Suspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("GP13") )
			pSuspi = zGp13Suspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Ochiai") )//Ochiai
			pSuspi = zOchiaiSuspicious(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("RusselRao") )
			pSuspi = zRusselRaoSuspicious(stProfile);
		//"Hamann", "SorensenDice","Dice",	"Kulczynski1","SimpleMatching","Sokal","M1","M2","RogersTanimoto","Goodman",	
		else if(  strAlgorithm.equalsIgnoreCase("Hamann") )
			pSuspi = zHamann(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("SorensenDice") )
			pSuspi = zSorensenDice(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Dice") )
			pSuspi = zDice(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Kulczynski1") )
			pSuspi = zKulczynski1(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("SimpleMatching") )
			pSuspi = zSimpleMatching(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Sokal") )
			pSuspi = zSokal(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("M1") )
			pSuspi = zM1(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("M2") )
			pSuspi = zM2(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("RogersTanimoto") )
			pSuspi = zRogersTanimoto(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Goodman") )
			pSuspi = zGoodman(stProfile);
		//"Hamming","Euclid", "Overlap","Anderberg","Zoltar","Wong1","Wong2","SBI","DStar2","GP10"
		else if(  strAlgorithm.equalsIgnoreCase("Hamming") )
			pSuspi = zHamming(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Euclid") )
			pSuspi = zEuclid(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Overlap") )
			pSuspi = zOverlap(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Anderberg") )
			pSuspi = zAnderberg(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Zoltar") )
			pSuspi = zZoltar(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Wong1") )
			pSuspi = zWong1(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("Wong2") )
			pSuspi = zWong2(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("SBI") )
			pSuspi = zSBI(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("DStar2") )
			pSuspi = zDStar2(stProfile);
		else if(  strAlgorithm.equalsIgnoreCase("GP10") )
			pSuspi = zGP10(stProfile);
		else
			pSuspi = 0;
		return pSuspi;
	}
	
	
	//Calculate Optimal Suspicious 
	private static double zOptimalSuspicious(ProfileStatement stProfile)
	{
		//totalFailed & totalPassed don't be 0.
		int totalFailed = stProfile.Aef+stProfile.Anf;
		if( stProfile.Aef<totalFailed )
			return 0;
		else
			return ((double)stProfile.Anp+1)/(stProfile.Anp+stProfile.Aep+1);
	}
	
	//Calculate OptimalPass(Naish et al.2011) Suspicious 
	//OP2
	private static double zOpassSuspicious(ProfileStatement stProfile)
	{
		//totalFailed & totalPassed don't be 0.
		int totalPassed = stProfile.Aep+stProfile.Anp;
		double fitem = (double)stProfile.Aep/(totalPassed+1.0);
		return (stProfile.Aef-fitem+1)/(stProfile.Anf+stProfile.Aef);
	}
	
	//Calculate Kulczynski2 Suspicious
	private static double zKulczynski2Suspicious(ProfileStatement stProfile) 
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
	private static double zWong3Suspicious(ProfileStatement stProfile)
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
		return (stProfile.Aef-h)/(stProfile.Anp+stProfile.Aep);
	}
	
	//Calculate Tarantula Suspicious 
	private static double zTarantulaSuspicious(ProfileStatement stProfile)
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
	private static double zJaccardSuspicious(ProfileStatement stProfile)
	{
		//totalFailed & totalPassed don't be 0.
		int totalFailed = stProfile.Aef+stProfile.Anf;
		double fm = (double)(stProfile.Aep+totalFailed);
		return (double)stProfile.Aef/fm;
	}
	
	//Calculate Ample Suspicious 
	private static double zAmpleSuspicious(ProfileStatement stProfile)
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
	private static double zGp2Suspicious(ProfileStatement stProfile)
	{
		double tmp = stProfile.Aef+Math.sqrt(stProfile.Anp);
		double stmp =  2*tmp+Math.sqrt(stProfile.Aep);
		int totalFailed = stProfile.Aef+stProfile.Anf;
		int totalPassed = stProfile.Aep+stProfile.Anp;
		return 0.5*stmp/(totalFailed+totalPassed);
	}

	//Calculate GP13 Suspicious 
	private static double zGp13Suspicious(ProfileStatement stProfile)
	{
		if( 0==stProfile.Aef )
			return 0.0;
		double tmp = 1.0/(2*stProfile.Aep+stProfile.Aef);
		return stProfile.Aef*(1+tmp)/(stProfile.Aef+stProfile.Anf+1);
	}
	
	//Calculate Ochiai Suspicious 
	private static double zOchiaiSuspicious(ProfileStatement stProfile)
	{
		if( 0==stProfile.Aef )
			return 0.0;
		//totalFailed & totalPassed don't be 0.
		int totalFailed = stProfile.Aef+stProfile.Anf;
		int totalCoverage = stProfile.Aep+stProfile.Aef;
		return (double)stProfile.Aef/Math.sqrt(totalFailed*totalCoverage);
	}
	
	//Calculate RusselRao Suspicious 
	private static double zRusselRaoSuspicious(ProfileStatement stProfile)
	{
		//totalFailed & totalPassed don't be 0.
		int totalFailed = stProfile.Aef+stProfile.Anf;
		int totalPassed = stProfile.Aep+stProfile.Anp;
		return (double)stProfile.Aef/(totalFailed+totalPassed);
	}
	
	//Calculate Hamann Suspicious 
	private static double zHamann(ProfileStatement stProfile)
	{
		//totalFailed & totalPassed don't be 0.
		int totalFailed = stProfile.Aef+stProfile.Anf;
		int totalPassed = stProfile.Aep+stProfile.Anp;
		double tmp = (double)(stProfile.Aef+stProfile.Anp-stProfile.Anf-stProfile.Aep)/(totalFailed+totalPassed);
		return (tmp+1)/2;
	}
	
	//Calculate SorensenDice Suspicious 
	private static double zSorensenDice(ProfileStatement stProfile)
	{
		double tmp = 2*stProfile.Aef;
		return tmp/(tmp+stProfile.Anf+stProfile.Aep);
	}
	
	//Calculate Dice Suspicious 
	private static double zDice(ProfileStatement stProfile)
	{
		double tmp = 2*stProfile.Aef;
		return 0.5*tmp/(stProfile.Aef+stProfile.Anf+stProfile.Aep);
	}	
	
	//Calculate Kulczynski1 Suspicious 
	private static double zKulczynski1(ProfileStatement stProfile)
	{
		double totalFailed = stProfile.Aef+stProfile.Anf;
		double tmp = stProfile.Aep+stProfile.Anf;
		if( tmp<=0 )
			return (double)stProfile.Aef/totalFailed;
		else
			return stProfile.Aef/tmp/totalFailed;
	}		
	
	//Calculate SimpleMatching Suspicious 
	private static double zSimpleMatching(ProfileStatement stProfile)
	{
		//totalFailed & totalPassed don't be 0.
		int totalFailed = stProfile.Aef+stProfile.Anf;
		int totalPassed = stProfile.Aep+stProfile.Anp;
		return (double)(stProfile.Aef+stProfile.Anp)/(totalFailed+totalPassed);
	}	
	
	//Calculate Sokal Suspicious 
	private static double zSokal(ProfileStatement stProfile)
	{
		double tmp  = 2.0*(stProfile.Aef+stProfile.Anp);
		return tmp/(tmp+stProfile.Anf+stProfile.Aep);
	}	
	
	//Calculate M1 Suspicious 
	private static double zM1(ProfileStatement stProfile)
	{
		double tcs = stProfile.Aef+stProfile.Anf+ stProfile.Aep+stProfile.Anp;
		double tmp = stProfile.Aep+stProfile.Anf;
		if( tmp<=0 )
			return ((double)stProfile.Aef+stProfile.Anp)/tcs;
		return (stProfile.Aef+stProfile.Anp)/tmp/tcs;
	}	
	
	//Calculate M2 Suspicious 
	private static double zM2(ProfileStatement stProfile)
	{
		//totalFailed & totalPassed don't be 0.
		double tmp = stProfile.Aep+stProfile.Anf;
		return stProfile.Aef/(stProfile.Aef+stProfile.Anp+2*tmp);
	}	
	
	//Calculate RogersTanimoto Suspicious 
	private static double zRogersTanimoto(ProfileStatement stProfile)
	{
		double tmp1 = stProfile.Aef+stProfile.Anp;
		double tmp2 = stProfile.Aep+stProfile.Anf;
		return tmp1/(tmp1+2*tmp2);
	}	
	
	//Calculate Goodman Suspicious 
	private static double zGoodman(ProfileStatement stProfile)
	{
		double tmp = stProfile.Aep+stProfile.Anf;
		double stmp =  (2.0*stProfile.Aef-tmp)/(tmp+2.0*stProfile.Aef);
		return (stmp+1)/2;
	}
	
	//Calculate Hamming Suspicious 
	private static double zHamming(ProfileStatement stProfile)
	{
		double tcs = stProfile.Aef+stProfile.Anf+ stProfile.Aep+stProfile.Anp;
		return (stProfile.Anp+stProfile.Aef)/tcs;
	}	
	
	//Calculate Euclid Suspicious 
	private static double zEuclid(ProfileStatement stProfile)
	{
		double tcs = stProfile.Aef+stProfile.Anf+ stProfile.Aep+stProfile.Anp;
		return Math.sqrt(stProfile.Anp+stProfile.Aef)/tcs;
	}		
	
	//Calculate Overlap Suspicious 
	private static double zOverlap(ProfileStatement stProfile)
	{
		double totalFailed = stProfile.Aef+stProfile.Anf;
		double tmp = Math.min(stProfile.Aef,stProfile.Aep);
		tmp = Math.min(tmp, stProfile.Anf);
		if( tmp<=0 )
			return (double)stProfile.Aef/totalFailed;
		else
			return (double)(stProfile.Aef)/tmp/totalFailed;
	}		
	
	//Calculate Anderberg Suspicious 
	private static double zAnderberg(ProfileStatement stProfile)
	{
		double tmp = 2.0*(stProfile.Aep+stProfile.Anf);
		return stProfile.Aef/(stProfile.Aef+tmp);
	}		
	
	//Calculate Zoltar Suspicious 
	private static double zZoltar(ProfileStatement stProfile)
	{
		if( stProfile.Aef<=0 )
			return 0;
		double tmp = (10000.0*stProfile.Aep*stProfile.Anf)/(double)(stProfile.Aef);
		return stProfile.Aef/(tmp+stProfile.Aep+stProfile.Anf+stProfile.Aef);
	}		
	
	//Calculate Wong1 Suspicious 
	private static double zWong1(ProfileStatement stProfile)
	{
		double totalFailed = stProfile.Aef+stProfile.Anf;
		return (double)(stProfile.Aef)/totalFailed;
	}		
	
	//Calculate Wong2 Suspicious 
	private static double zWong2(ProfileStatement stProfile)
	{
		double tcs = stProfile.Aef+stProfile.Anf+ stProfile.Aep+stProfile.Anp;
		double tmp = (double)(stProfile.Aef-stProfile.Aep)/tcs;
		return (tmp+1)/2;
	}		
	
	//Calculate SBI Suspicious 
	private static double zSBI(ProfileStatement stProfile)
	{
		double tmp = stProfile.Aep;
		if( tmp<=0 )
			return 1;
		else 
			return 1.0-tmp/(tmp+stProfile.Aef);
	}
	
	//Calculate DStar2 Suspicious 
	private static double zDStar2(ProfileStatement stProfile)
	{
		int totalFailed = stProfile.Aef+stProfile.Anf;
		double divisor =  totalFailed*totalFailed;
		double tmp = stProfile.Aep+stProfile.Anf;
		if( tmp<=0 )
			return stProfile.Aef*stProfile.Aef/divisor; //stProfile.Anf=0时，此时有stProfile.Aef=F
		else 
			return stProfile.Aef*stProfile.Aef/tmp/divisor;
	}
	
	//Calculate GP10 Suspicious 
	private static double zGP10(ProfileStatement stProfile)
	{
		int totalFailed = stProfile.Aef+stProfile.Anf;
		double tmp = 1.0;
		if( stProfile.Anp>0 )
			tmp = 1.0/stProfile.Anp;
		return Math.sqrt(Math.abs(stProfile.Aef-tmp))/totalFailed;
	}	
	
	/** 计算所有SBFL技术的值，组成RankLib特征。
	 * @param stProfile
	 * @return 特征组成的字符串，特征号从1到31
	 */
	public static String getAllTechniqueFeature(ProfileStatement stProfile)
	{
		String[] algorithmNames = SBFLTradTechnique.getAlgorithmNames();
		StringBuilder sb = new StringBuilder();
		int sbfls = algorithmNames.length;
		for( int k=1;k<=sbfls;k++ )
		{
			sb.append(k);
			sb.append(":");
			double asv = zAlgorithmSuspicious(algorithmNames[k-1],stProfile);
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
