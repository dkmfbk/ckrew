package eu.fbk.dkm.ckrdatalogrewriter.rl.cli;

import it.unical.mat.wrapper.DLVInputProgram;
import it.unical.mat.wrapper.DLVInvocationException;

import java.io.File;
import java.io.IOException;

import org.semanticweb.drew.dlprogram.parser.ParseException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.profiles.OWLProfileReport;

import eu.fbk.dkm.ckrdatalogrewriter.cli.CommandLine;
import eu.fbk.dkm.ckrdatalogrewriter.rl.ckr.CKRKnowledgeBase;
import eu.fbk.dkm.ckrdatalogrewriter.rl.ckr.CKRModule;
import eu.fbk.dkm.ckrdatalogrewriter.rl.ckr.CKRProgram;

/** 
 * @author Loris 
 * @version 1.4
 * 
 * Command line interface for CKR Datalog Rewriter (for OWL-RL).
 * (Extended from DreW CLI).
 * */
public class CKRDReWRLCLI extends CommandLine {

	//--- FIELDS -----------------------------------------------
	
	private static final String DEFAULT_DLV_PATH = "./localdlv/dlv";
	private static final String DEFAULT_OUTPUT_FILENAME = "./testcase/output.dlv";
	
    private CKRKnowledgeBase inputCKR;
    private CKRProgram outputCKRProgram;
    	
	private String dlvPath = null;
	private String outputFilePath = null;
	
	private boolean verbose = false;
	private boolean trigInput = false;	

	private String[] args;
	
	//--- CONSTRUCTOR ------------------------------------------
	
	public CKRDReWRLCLI(String[] args) {
		this.args = args;
	}
	
	//--- MAIN THREAD ------------------------------------------

	/* (non-Javadoc)
	 * @see eu.fbk.dkm.ckrdatalogrewriter.cli.CommandLine#go()
	 */
	@Override
	public void go() {
		
		//Create a new CKR knowledge base to manage input ontologies
		inputCKR = new CKRKnowledgeBase();
		
		//Parse input for global and local (modules) ontologies
		printBanner();
		if (!parseArgs(args)) {
			printUsage();
			System.exit(1);
		}
		
		//Check global input file existence
		File ontofile = new File(inputCKR.getGlobalOntologyFilename());
		if(!ontofile.exists()){
			System.err.println("[!] Input global ontology file does not exists.");
			System.exit(1);
		}
		
		//Manage TRIG files.
		if(trigInput) parseTrigFile();

		//Check local input file existence
		for(CKRModule mod : inputCKR.getLocalModule()){
			File modfile = new File(mod.getModuleFilename());
			
			if(!modfile.exists()){
				System.err.println("[!] Input local ontology file does not exist: " + mod.getModuleFilename());
				System.exit(1);
			}		
		}	
		
		//Load ontology files.
		try {
			inputCKR.loadOntologies();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if(verbose) System.out.println("Global and local ontologies loaded.");
		
		if(verbose) System.out.println("Number of input logical axioms: " + inputCKR.getCKRSize());
		
		//Check if profile of ontologies is in the correct fragment.
		OWLProfileReport report = inputCKR.checkProfiles();
		if (!report.isInProfile()) {
			System.err.println(report);
			System.exit(1);
		} 
		if(verbose) System.out.println("Global and local ontologies in OWL-RL.");
		
		//Handle translation of input CKR
		handleOntology(inputCKR);
	}

	//--- INPUT HANDLING METHODS ------------------------------------
    /**
     * Handles the case in which transformation to Datalog 
     * of the input CKR is required. 
     * 
     * @param ckr input CKR knowledge base
     * */
	private void handleOntology(CKRKnowledgeBase ckr) {
		
		//Create new program for the input CKR.
		outputCKRProgram = new CKRProgram(ckr);
		
		//Set possibly custom DLV path
		if(dlvPath != null) 
			outputCKRProgram.setDlvPath(dlvPath);
		else{
			dlvPath = DEFAULT_DLV_PATH;
			outputCKRProgram.setDlvPath(dlvPath);			
		}
		
		//Set possibly custom output file path
		if(outputFilePath != null) 
			outputCKRProgram.setOutputFilePath(outputFilePath);
		else{
			outputFilePath = DEFAULT_OUTPUT_FILENAME;
			outputCKRProgram.setOutputFilePath(outputFilePath);			
		}
		
		//Rewrite the program.
		if(verbose) System.out.println("Rewriting program...");
		outputCKRProgram.rewrite();
		
		if(verbose) {
			System.out.println("Global model computation time: " + outputCKRProgram.getGlobalModelComputationTime() + " ms.");
			
			System.out.println("Set of contexts and modules associations computed:");
			for (String s : outputCKRProgram.getContextsSet()) {
				System.out.println(s);
				for(String[] a : outputCKRProgram.getHasModuleAssociations()){
					if(a[0].equals(s))
						System.out.println("  -> " + a[1]);
				}
			}
			System.out.println("Rewriting completed in " + outputCKRProgram.getRewritingTime() + " ms.");
			
			System.out.println("Program size (number of statements): " + outputCKRProgram.getProgramSize());
		}
		
		//Store the program to file.
		try {
			outputCKRProgram.storeToFile();
			if(verbose) System.out.println("CKR program saved in: " + outputCKRProgram.getOutputFilePath() + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("CKRew: process complete.");
	}
	
	@Override
	public void handleCQ(OWLOntology ontology, DLVInputProgram inputProgram) {
		//CQ not managed in this version of CKRew 
		return;
	}

	@Override
	/** 
	 * Parses the input parameters. 
	 * 
	 * @param args input parameter string
	 * */
	public boolean parseArgs(String[] args) {
		
		if (args.length == 0) {			
			System.err.println("[!] Missing argument: <global-context-file>\n");
			System.err.println();			
			return false;
			
		} else {
			inputCKR.setGlobalOntologyFilename(args[0]);
			if(verbose) System.out.println("Global ontology: " + inputCKR.getGlobalOntologyFilename());
			
			for (int i = 1; i < args.length; i++) {				
				switch (args[i]) {
				case "-dlv":
					if(i+1 < args.length && !args[i+1].startsWith("-"))
					   dlvPath = args[++i];
					else {
						System.err.println("[!] Missing argument: <dlv-path>");						
						return false;
					}					
					break;
				case "-out":
					if(i+1 < args.length && !args[i+1].startsWith("-"))
					   outputFilePath = args[++i];
					else {
						System.err.println("[!] Missing argument: <output-file>");						
						return false;
					}					
					break;
				case "-v":
				    verbose = true;
					break;
				case "-trig":
				    trigInput = true;
					break;
				default:
					if(trigInput) {
						return false; //more arguments than expected... 
					} else {//add local module to input CKR
						inputCKR.addLocalOntologyWithFilename(args[i]);
						if(verbose) System.out.println("Local ontology: " + args[i]);					
					break;
					}
				}
			}
			return true;
		}
	}
	
	/**
	 * Split and uses the TRIG file in input. 
	 * 
	 */
	void parseTrigFile() {
		
		String trigFilename = inputCKR.getGlobalOntologyFilename();
		
		TrigSplitter splitter = new TrigSplitter(trigFilename);
		try {
			splitter.split();
			splitter.saveToTurtleFiles();
			
			//Set global module
			inputCKR.setGlobalOntologyFilename(splitter.getTrigFolder() + "global.n3");
			if(verbose) System.out.println("Global turtle: " + inputCKR.getGlobalOntologyFilename());
			
			//Set local modules
			for (String name : splitter.getOutputFileNames()) {
				if (!name.contains("global")){
					inputCKR.addLocalOntologyWithFilename(splitter.getTrigFolder() + name);
					if(verbose) System.out.println("Local module:" + splitter.getTrigFolder() + name);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return;
	}
	
	/**
	 * Runs DLV on the computed program. 
	 * 
	 * @param inputProgram the program submitted to DLV.
	 */
	void runDLV(DLVInputProgram inputProgram) {
		//Not implemented in this version of CKRew
		return;
	}

	/**
	 * Prints usage of the command line and exits.
	 */
	void printUsage() {

		String usage = 
				"Usage: ckrew <global-context-file> [<local-module-file> | <options>]\n"
				+ //
				" <global-context-file>\n"
				+ //
				"  Ontology file containing the global context for the input CKR. \n"
				+ //
				" <local-module-file>\n"
				+ //
				"  Ontology files (zero or more) containing a knowledge module for the input CKR. \n"
				+ //
				"Example: ckrew global.n3 m1.n3 m2.n3\n"
				+ //
				"Options:\n"
				+ //
				" -v: verbose (prints more information about loading and rewriting process)\n"
				+ //
				" -out <output-file>: specifies the path to the output program file (default: output.dlv)\n"
				+ //
				" -trig: specifies that the input is provided as a single TRIG file\n"
		        + //
		        " -dlv <dlv-path>: specifies the path to the DLV executable (default: localdlv/dlv)\n";

		System.out.println(usage);
	}
	
	/**
	 * Prints initial banner and version.
	 */
	void printBanner(){
		String banner = "=== CKRew v.1.4 ===\n";
		System.out.println(banner);
	}

	//--- OUTPUT INFORMATION METHODS --------------------------------

	/**
	 * Returns the time in milliseconds for the DLV computation time of global model.
	 */
	public long getGlobalModelComputationTime(){
		return outputCKRProgram.getGlobalModelComputationTime();
	}
	
	/**
	 * Returns the time in milliseconds for the complete rewriting of the CKR program.
	 */
	public long getRewritingTime(){
		return outputCKRProgram.getRewritingTime();
	}

	/**
	 * Returns the size of the input CKR in terms of logical axioms.
	 */
	public int getCKRSize(){
		return inputCKR.getCKRSize();
	}	
	
	/**
	 * Returns the size of the output CKR program in terms of number of statements.
	 */
	public int getProgramSize(){
		return outputCKRProgram.getProgramSize();
	}	
	
	//--- MAIN ------------------------------------------------------
	/**
	 * @param args
	 * @throws OWLOntologyCreationException
	 * @throws IOException
	 * @throws ParseException
	 * @throws DLVInvocationException
	 */
	public static void main(String[] args) throws OWLOntologyCreationException,
			IOException, ParseException, DLVInvocationException {
		
        new CKRDReWRLCLI(args).go();
		
        //(Test application)
		//String[] argtest = {"./testcase/Test01.owl", 
		//		            "./testcase/eswc12-fb-clubwc11k.n3",
		//		            "./testcase/eswc12-fb-uefaclubfb.n3"};
		//String[] argtest = {"./testcase/Test01.owl", 
	    //        "./testcase/eswc12-fb-clubwc11k.n3",
	    //        "./testcase/eswc12-fb-global.n3"}; //*!* NOT IN PROFILE
						
		//Simple Test only global
		//String[] argtest = {"./testcase/simple-el/global.n3"};
		//String[] argtest = {"./testcase/tourism-demo/tourism-demo-global.n3"};
		
		//Simple Test complete
		//String[] argtest = {"./testcase/simple-el/global.n3",
		//		"./testcase/simple-el/m1.n3",
		//		"./testcase/simple-el/m2.n3",
		//		"./testcase/simple-el/mB.n3"};

		//Simple Test RL complete
		//String[] argtest = {"./testcase/simple-rl/global.n3",
		//		"./testcase/simple-rl/m1.n3",
		//		"./testcase/simple-rl/m2.n3",
		//		"./testcase/simple-rl/mB.n3"};

		//***Simple Test RL only global DEFEASIBLE***
		//String[] argtest = {"./testcase/simple-rl-d/global.n3"};
		
		//***Simple Test RL complete DEFEASIBLE***
		//String[] argtest = {"./testcase/simple-rl-d/global.n3",
		//		"./testcase/simple-rl-d/m1.n3",
		//		"./testcase/simple-rl-d/m2.n3",
		//		"./testcase/simple-rl-d/mB.n3"};

		//***Defeasible Rewriting Test (only global)***
		//String[] argtest = {"./testcase/defeasible-rew-test/global.n3", "-v"};

		//***Defeasible Rewriting Test complete***
		//String[] argtest = {"./testcase/defeasible-rew-test/global.n3", 
		//		            "./testcase/defeasible-rew-test/m1.n3", "-v"};
		
		//***Cheap Events Example***
		//String[] argtest = {"./testcase/tourism-example-d/global.n3",
		//		"./testcase/tourism-example-d/ctourist_m.n3"};

		//***Workers Example***
		//String[] argtest = {"./testcase/workers-example-d/global.n3",
		//		"./testcase/workers-example-d/em2012_m.n3",
		//		"./testcase/workers-example-d/em2013_m.n3"};
		
		//Paths test
		//String[] argtest = {"./testcase/simple-rl-d/global.n3",
		//		            "./testcase/simple-rl-d/m1.n3",
		//		            "-dlv", "./localdlv/dlv",
		//		            "-out", "./testcase/prova.txt", "-v"};

		//TRIG test
		//String[] argtest = {"./testcase/trig-test/trig-test-file.trig",
		//		            "-trig", "-v"};
		//String[] argtest = {"./testcase/trig-test/50cls.trig",
	    //        			"-trig", "-v"};
		
		//ESWC Test complete
		//String[] argtest = {"./testcase/eswc12-fb/eswc12-fb-global.n3", 
		//		"./testcase/eswc12-fb/m_clubwc11.n3",
		//		"./testcase/eswc12-fb/m_chleague11.n3",
		//		"./testcase/eswc12-fb/m_uefaclubfb.n3"};
		
		//Tourism demo Test complete
		//String[] argtest = {"./testcase/tourism-demo/tourism-demo-global.n3",
		//		"./testcase/tourism-demo/m_sport_event.n3",
		//		"./testcase/tourism-demo/m_volley_match.n3",
		//		"./testcase/tourism-demo/m_modena_trento_130112.n3",
		//		"./testcase/tourism-demo/m_trento_cuneo_120922.n3",
		//		"./testcase/tourism-demo/m_trento_latina_130203.n3",
		//		"./testcase/tourism-demo/m_sportive_tourist.n3",
		//		"./testcase/tourism-demo/m_volley_fan_01.n3"};
		
		//new CKRDReWRLCLI(argtest).go();
	}
	
	//XXX: #####################
	
}
//=======================================================================