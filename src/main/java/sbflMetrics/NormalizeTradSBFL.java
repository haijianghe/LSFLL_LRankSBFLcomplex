/**
 * 
 */
package sbflMetrics;

/** ��SBFLTradNormalizeTechnique�ظ���
 * @author Administrator
 *
 */
public class NormalizeTradSBFL {
	
	/** ����FBFL������������������ֵ�޶���[0,1]֮�䡣
	 * @param strAlgorithm
	 * @param stProfile
	 * @return
	 */
	public static double normalizeSuspicious(String strAlgorithm,ProfileStatement stProfile)
	{
		float F = stProfile.Aef+stProfile.Anf;
		float P = stProfile.Aep+stProfile.Anp;
		
		double pSuspi = SBFLTradTechnique.algorithmSuspicious(strAlgorithm, stProfile);
		//"Optimal","Opass","Kulczynski2","Wong3","Tarantula","Jaccard","Ochiai","RusselRao","Ample","GP2","GP13"
		if(  strAlgorithm.equalsIgnoreCase("Optimal") )
			pSuspi = (pSuspi+1)/(P+1);
		else if(  strAlgorithm.equalsIgnoreCase("Opass") )
			pSuspi =  (pSuspi+1)/(F+1);
		else if(  strAlgorithm.equalsIgnoreCase("Kulczynski2") )
			{ } //û�б仯��
		else if(  strAlgorithm.equalsIgnoreCase("Wong3") )
			pSuspi = (pSuspi/(P+F)+1)/2;
		else if(  strAlgorithm.equalsIgnoreCase("Tarantula") )
			{ } //û�б仯��
		else if(  strAlgorithm.equalsIgnoreCase("Jaccard") )
			{ } //û�б仯��
		else if(  strAlgorithm.equalsIgnoreCase("Ample") )
			{ } //û�б仯��
		else if(  strAlgorithm.equalsIgnoreCase("GP2") )
			pSuspi = pSuspi/2.0/(P+F);  //����֤����
		else if(  strAlgorithm.equalsIgnoreCase("GP13") )
			pSuspi = pSuspi/(1+F);
		else if(  strAlgorithm.equalsIgnoreCase("Ochiai") )//Ochiai
			{ } //û�б仯��
		else if(  strAlgorithm.equalsIgnoreCase("RusselRao") )
			{ } //û�б仯��
		//"Hamann", "SorensenDice","Dice",	"Kulczynski1","SimpleMatching","Sokal","M1","M2","RogersTanimoto","Goodman",	
		else if(  strAlgorithm.equalsIgnoreCase("Hamann") )
			pSuspi = (pSuspi+1)/2;
		else if(  strAlgorithm.equalsIgnoreCase("SorensenDice") )
			{ } //û�б仯��
		else if(  strAlgorithm.equalsIgnoreCase("Dice") )
			pSuspi = pSuspi/2;
		else if(  strAlgorithm.equalsIgnoreCase("Kulczynski1") )
			pSuspi = pSuspi/F;
		else if(  strAlgorithm.equalsIgnoreCase("SimpleMatching") )
			{ } //û�б仯��
		else if(  strAlgorithm.equalsIgnoreCase("Sokal") )
			{ } //û�б仯��
		else if(  strAlgorithm.equalsIgnoreCase("M1") )
			pSuspi = pSuspi/(P+F);
		else if(  strAlgorithm.equalsIgnoreCase("M2") )
			{ } //û�б仯��
		else if(  strAlgorithm.equalsIgnoreCase("RogersTanimoto") )
			{ } //û�б仯��
		else if(  strAlgorithm.equalsIgnoreCase("Goodman") )
			pSuspi = (pSuspi+1)/2;		
		//"Hamming","Euclid", "Overlap","Anderberg","Zoltar","Wong1","Wong2","SBI","DStar2","GP10"
		else if(  strAlgorithm.equalsIgnoreCase("Hamming") )
			pSuspi = pSuspi/(P+F);
		else if(  strAlgorithm.equalsIgnoreCase("Euclid") )
			pSuspi = pSuspi/(P+F);
		else if(  strAlgorithm.equalsIgnoreCase("Overlap") )
			pSuspi = pSuspi/F;
		else if(  strAlgorithm.equalsIgnoreCase("Anderberg") )
			{ } //û�б仯��
		else if(  strAlgorithm.equalsIgnoreCase("Zoltar") )
			{ } //û�б仯��
		else if(  strAlgorithm.equalsIgnoreCase("Wong1") )
			pSuspi = pSuspi/F;
		else if(  strAlgorithm.equalsIgnoreCase("Wong2") )
			pSuspi = (pSuspi/(P+F)+1)/2;		
		else if(  strAlgorithm.equalsIgnoreCase("SBI") )
			{ } //û�б仯��
		else if(  strAlgorithm.equalsIgnoreCase("DStar2") )
			pSuspi = pSuspi/(F*F);
		else if(  strAlgorithm.equalsIgnoreCase("GP10") )
			pSuspi = pSuspi/F;
		else
			pSuspi = -1;
		
		return pSuspi;
	}
}
