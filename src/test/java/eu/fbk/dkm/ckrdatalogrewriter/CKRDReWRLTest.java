package eu.fbk.dkm.ckrdatalogrewriter;

import it.unical.mat.wrapper.DLVError;
import it.unical.mat.wrapper.DLVInputProgram;
import it.unical.mat.wrapper.DLVInputProgramImpl;
import it.unical.mat.wrapper.DLVInvocation;
import it.unical.mat.wrapper.DLVInvocationException;
import it.unical.mat.wrapper.DLVWrapper;

import java.io.IOException;
import java.util.List;

import eu.fbk.dkm.ckrdatalogrewriter.rl.cli.CKRDReWRLCLI;

/** 
 * @author Loris 
 * @version 0.1
 * 
 * TEST: imports and rewrites a test CKR, then calls DLV to compute its AS.
 * */
public class CKRDReWRLTest {

	//--- FIELDS -----------------------------------------------
	
	private static String inputFilename = "./testcase/trig-test/50cls.trig";
	private static String outputFilename = "./testcase/trig-test/output.dlv";
	
	private static String dlvPath = "./localdlv/dlv.exe"; //DLV distributed with demo
	
	
	//--- CONSTRUCTOR ------------------------------------------
	
	private CKRDReWRLTest() {}
	

	//--- DLV INTERACTION -------------------------------------------
	
	private static long callDLV(String programFilename) {
		
		DLVInvocation invocation = DLVWrapper.getInstance().createInvocation(dlvPath);
		DLVInputProgram inputProgram = new DLVInputProgramImpl();
		
		//Set options
		for (String s : invocation.getOptions()) {
			System.out.print(s);
		}

		long resultTime = 0;
		
		try {			            
			//Add to DLV input program the input file 
			inputProgram.addFile(programFilename);
						
			//Set input program for current invocation.
			invocation.setInputProgram(inputProgram);
			//invocation.addOption("-OR -ORdr -OGp -OGo2 -OGs -OH -OPf");
			//invocation.addOption("-O0");
			
			//List of computed models, used to check at least a model is computed.
			//final List<Model> models = new ArrayList<Model>();
			
			//Subscribe a (simple) model handler
			//ModelBufferedHandler modelBufferedHandler = new ModelBufferedHandler(invocation);

			long startTime = System.currentTimeMillis();
			
			invocation.run();
			invocation.waitUntilExecutionFinishes();
			
			long endTime = System.currentTimeMillis();
			resultTime = endTime - startTime;
			
			List<DLVError> k = invocation.getErrors();
			if (k.size() > 0)
				System.err.println(k);
			
			//Find number of models
			//while(modelBufferedHandler.hasMoreModels()){
			//	Model model = modelBufferedHandler.nextModel();
			//	models.add(model);
			//}			
			
			//System.out.println("Number of computed models: " + models.size());
			//if(models.size() == 0)
			if(!invocation.haveModel())
				System.err.println("[!] No models for final program.");
			
		} catch (DLVInvocationException | IOException e) {
			e.printStackTrace();
		}
		
		return resultTime;
	}
	
	//--- MAIN ------------------------------------------------------
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//Set input and output files
		String[] argtest = {inputFilename, "-trig",
				            "-out", outputFilename, "-v"};
		
		//Phase 1: rewrite and get rewriting times
		CKRDReWRLCLI cli = new CKRDReWRLCLI(argtest);
		
		cli.go();
		
		//*!!!*
		System.out.println("Number of input logical axioms: " + cli.getCKRSize());
		
		System.out.println("Rewriting time: " + cli.getRewritingTime() + " ms.");
		System.out.println("Global model time: " + cli.getGlobalModelComputationTime() + " ms.");
		
		//*!!!*
		System.out.println("Program size (number of statements): " + cli.getProgramSize());
		
		//Phase 2: call DLV and compute model computation time
		long dlvTime = callDLV(outputFilename);
		
		System.out.println("DLV time: " + dlvTime + " ms.");
		
	}
	
}
//=======================================================================