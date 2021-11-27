# LSFLL_LRankSBFLcomplex
Learning to rank based Statement-level Fault Localization Lightweight approach  for identifying buggy statements.

During the automatic program repair and the manual debugging activities of developers, spectrum-based fault localization (SBFL) technique can provide effective help. SBFL utilizes statement coverage information and test case execution results to assign a suspicious value to each program statement. These statements are then ranked in order of their suspiciousness. When determining a fault, developers may inspect source code starting from the top of the ranking list. Obviously, SBFL ignores the buggy information contained in the program statement itself. In order to improve the performance of SBFL, a lightweight approach based on learning to rank is introduced for locating buggy statements. The approach uses the linear ranking support vector machine to learn the fault localization model. In the model, the feature vector of a statement is composed of two different types of information: (1) the SBFL suspicious score, (2) the static feature such as variables, operators, statement category, and so on. To verify the effectiveness of the proposed approach, cross-project training way was adopted, and experiments were performed on 19 Java projects, 19 C projects and 2 C++ projects. Experimental results confirmed that the proposed method reduced the number of inspected codes by 26.1% than the best SBFL technique when evaluated with EXAM of the worst strategy.

Usge:
1, Download file 
          F_MySBFLRawDataset.part001.rar  F_MySBFLRawDataset.part002.rar  F_MySBFLRawDataset.part003.rar  F_MySBFLRawDataset.part004.rar
          F_MySBFLRawDataset.part005.rar  F_MySBFLRawDataset.part006.rar  F_MySBFLRawDataset.part007.rar
   from https://github.com/haijianghe/MySBFLRawDataset.
2, Unpack compression to 
   for example:  F:\MySBFLRawDataset
3, Download this project, pom.xml said it was created with MAVEN.
4,  src/main/java/common/ProjectConfiguration.java
      this is a configuration class.
              (a)	public static final String RawDatasetDirectory = "F:\\MySBFLRawDataset";   //包含原始.profile和源代码的文件夹。
              (b) ....
5,MainProcessLRankSBFL.java is startup class, run it, 
     Learning to Rank for Spectrum based Fault Localization with Software Complex Metrics. 
        1, evaluate (one algorithm,one object ) SBFL performance of profile dataset. 
        2, evaluate (one algorithm,all object ) profile dataset.
        3, aggregative evaluate (one algorithm,all object ) profile dataset.
        5, calculate statement complex metric feature and store to file.
        7, make learing to rank feature file at RankLIB.
        9, Evaluate Exam using RankLIB.
       @@@   10, Evaluate Exam using LibLinear-RankSVM.
       ###             20, Random evaluate using LibLinear-RankSVM. 
        65, check  .profile file of v(xx). 
       66, check .fault,.testcase,_falut.csv, .profile file. 
      76, print information of all projects. 

       Others, exit............ .
        OperatorInterface:      Please key your choice.
  6, Input 1,2,or 3,
           calculate SBFL
  7, Input 5 calculate statement complex metric feature and store to file.
  8, Input 7 make learing to rank feature file at RankLIB.
 9,  Input 20 Random evaluate using LibLinear-RankSVM.  

