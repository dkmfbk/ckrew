/**
 * 
 */
package eu.fbk.dkm.ckrdatalogrewriter.rl.ckr;

import it.unical.mat.dlv.program.Literal;
import it.unical.mat.wrapper.DLVError;
import it.unical.mat.wrapper.DLVInputProgram;
import it.unical.mat.wrapper.DLVInputProgramImpl;
import it.unical.mat.wrapper.DLVInvocation;
import it.unical.mat.wrapper.DLVInvocationException;
import it.unical.mat.wrapper.DLVWrapper;
import it.unical.mat.wrapper.Model;
import it.unical.mat.wrapper.ModelHandler;
import it.unical.mat.wrapper.ModelResult;
import it.unical.mat.wrapper.Predicate;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.drew.dlprogram.format.DLProgramStorer;
import org.semanticweb.drew.dlprogram.format.DLProgramStorerImpl;
import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter.DLRDeductionRuleset;
import eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter.DLRGlobal2DatalogRewriter;
import eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter.DeductionRuleset;
import eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter.RLGlobal2DatalogRewriter;
import eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter.RLLocal2DatalogRewriter;

/**
 * @author Loris
 * @version 1.3
 * 
 * Represents the output program of the translation. 
 * Contains references to the global and local programs and 
 * methods to compute and store the program. 
 */
public class CKRProgram {
	
	//--- FIELDS ---------------------------------------------
	
	//private static final String DEFAULT_DLV_PATH = "./localdlv/dlv";
	//private static final String DEFAULT_OUTPUT_FILENAME = "./testcase/output.dlv";
	
	private CKRKnowledgeBase inputCKR;
	private OWLOntologyManager manager;
	
	private DLProgram datalogGlobal;
	private LinkedList<DLProgram> datalogLocal;

	private DLProgram datalogCKR;
	
	//Set of context computed from GlobalProgram
	final private Set<String> contextsSet = new HashSet<String>();
	
	//Set of context to module associations computed from GlobalProgram
	final private Set<String[]> hasModuleAssociations = new HashSet<String[]>();
	
	//(Map of contexts to local ontologies)
	private HashMap<String, OWLOntology> contextsOntologies = new HashMap<String, OWLOntology>();
	
	private String outputFilePath; //Path to the output datalog file
	private String dlvPath; 	   //Path to DLV solver location
	
	private long globalModelComputationTime;  //time in milliseconds for DLV computation of global model
	private long rewritingTime;  //time in milliseconds for the complete CKR rewriting
	
	//--- CONSTRUCTOR ------------------------------------------

	/**
	 * @param inputCKR the CKR (in normal form) to be rewritten
	 */
	public CKRProgram(CKRKnowledgeBase inputCKR) {
		this.inputCKR = inputCKR;
		this.manager = inputCKR.getGlobalOntology().getOWLOntologyManager();
		
		//this.outputFilePath = DEFAULT_OUTPUT_FILENAME;
		//this.dlvPath = DEFAULT_DLV_PATH;
		
		this.globalModelComputationTime = 0;
		this.rewritingTime = 0;
	}
	
	//--- GET AND SET METHODS ---------------------------------------------
	
	/**
	 * @return the outputFilePath
	 */
	public String getOutputFilePath() {	
		return outputFilePath; 
	}

	/**
	 * @return the dlvPath
	 */
	public String getDlvPath() { 
		return dlvPath; 
	}

	/**
	 * @return the globalModelComputationTime
	 */
	public long getGlobalModelComputationTime() { 
		return globalModelComputationTime; 
	}
	
	/**
	 * @return the rewritingTime
	 */
	public long getRewritingTime() { 
		return rewritingTime; 
	}

	/**
	 * @return the rewritingTime
	 */
	public int getProgramSize() { 
		return datalogCKR.getStatements().size(); 
	}	
	
	/**
	 * @return the contextsSet
	 */
	public Set<String> getContextsSet() {
		return contextsSet;
	}

	/**
	 * @return the hasModuleAssociations
	 */
	public Set<String[]> getHasModuleAssociations() {
		return hasModuleAssociations;
	}	

	/**
	 * @return the datalogCKR
	 */
	public DLProgram getDatalogCKR() {
		return datalogCKR;
	}	

	/**
	 * @return the inputCKR
	 */
	public CKRKnowledgeBase getInputCKR() {
		return inputCKR;
	}	
	
	/**
	 * @param outputFilePath the outputFilePath to set
	 */
	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}

	/**
	 * @param dlvPath the dlvPath to set
	 */
	public void setDlvPath(String dlvPath) {
		this.dlvPath = dlvPath;
	}

	//--- REWRITING ---------------------------------------------
	
	/**
	 * Rewrites the whole input CKR to global and local programs.
	 */
	public void rewrite(){
		//SROEL2DatalogRewriter rewriter = new SROEL2DatalogRewriter();
		
		long startTime = System.currentTimeMillis();
		
		//Rewriting for global context.
		RLGlobal2DatalogRewriter globalrewriter = new RLGlobal2DatalogRewriter();
		datalogGlobal = globalrewriter.rewrite(inputCKR.getGlobalOntology());
		//System.out.println("Rewriting program for global context complete.");
		
		//Computation of set of contexts and associations to modules
		computeSets(datalogGlobal);
		//System.out.println("Set of contexts and modules associations computed.");
		
		//Computation of local contexts knowledge bases.
		computeLocalKB();
		
		//Rewriting for local contexts knowledge bases.
		RLLocal2DatalogRewriter localrewriter = new RLLocal2DatalogRewriter();
		
		datalogLocal = new LinkedList<DLProgram>();
		for (String c : contextsSet) {
			//System.out.println("Rewriting program for " + c.replaceAll("\"", ""));
			localrewriter.setContextID(c.replaceAll("\"", ""));
			datalogLocal.add(localrewriter.rewrite(contextsOntologies.get(c)));
		}
		
		//for (OWLOntology o : inputCKR.getLocalOntology()) {
		//	localrewriter.setLocalID("c");
		//	//o.getOWLOntologyManager().addAxioms(o, inputCKR.getGlobalOntology().getAxioms());
		//	
		//	datalogLocal.add(localrewriter.rewrite(o));
		//	//System.out.println(datalogLocal.getLast().getStatements().size());
		//}
				
		long endTime = System.currentTimeMillis();
		rewritingTime = endTime - startTime;

		//System.out.println("Rewriting completed in " + rewritingTime + " ms.");
		//System.out.println(datalogGlobal.getStatements().size());
		
		//Compute CKR Program.
		datalogCKR = new DLProgram();
	
		datalogCKR.addAll(datalogGlobal.getStatements());
		for (DLProgram dlProgram : datalogLocal) {
			datalogCKR.addAll(dlProgram.getStatements());	
		}
		//Add local inference rules.
		datalogCKR.addAll(DeductionRuleset.getPloc());
		//Add local propagation rules.
		datalogCKR.addAll(DeductionRuleset.getPd());
		
		//StringReader reader = new StringReader("hasModule(X,Y) :- triple(X, \"hasModule\", Y, \"g\").");
		//DLProgramParser dlProgramParser = new DLProgramParser(reader);
		//try {
		//	datalogCKR.addAll(dlProgramParser.program().getStatements());
		//} catch (ParseException e) {
		//	e.printStackTrace();
		//}
	}
	
	//--- REWRITING: option for DLliteR ------------------------
	
	/**
	 * Rewrites the global file as a single-context DLliteR DKB
	 */
	public void rewriteDLR(){
		
		long startTime = System.currentTimeMillis();
		
		//Rewriting for global context.
		DLRGlobal2DatalogRewriter globalrewriter = new DLRGlobal2DatalogRewriter();
		datalogGlobal = globalrewriter.rewrite(inputCKR.getGlobalOntology());
		
		long endTime = System.currentTimeMillis();
		rewritingTime = endTime - startTime;
		
		//Compute DKB Program.
		datalogCKR = new DLProgram();
	
		datalogCKR.addAll(datalogGlobal.getStatements());

		//Add local propagation rules.
		datalogCKR.addAll(DLRDeductionRuleset.getPd()); //TODO: update rules
	}	
	
	//--- REWRITING: DLV INTERACTION ------------------------------
	
	/**
	 * Interacts with DLV to compute the set of contexts
	 * and their associations to knowledge modules.
	 * 
	 * @param datalogGlobal global program to be evaluated by DLV
	 */
	private void computeSets(DLProgram datalogGlobal){
		
		DLVInvocation invocation = DLVWrapper.getInstance().createInvocation(dlvPath);
		DLVInputProgram inputProgram = new DLVInputProgramImpl();

		try {			
			DLProgramStorer storer = new DLProgramStorerImpl();
			StringBuilder target = new StringBuilder();
			storer.store(datalogGlobal, target);
            
			//Add to DLV input program the contents of global program. 
			String datalogGlobalText = target.toString();
			inputProgram.addText(datalogGlobalText);
			
			//inputProgram.addText("triple(c1,\"hasModule\",m1,\"g\")." + 
			//                     "  inst(c1,\"Context\",\"g\")."+ 
			//                     "triple(c1,\"hasModule\",m2,\"g\")." + 
			//                     "triple(X, \"hasModule\", m3, \"g\") :- inst(X,\"Context\",\"g\").");
			
			//Set input program for current invocation.
			invocation.setInputProgram(inputProgram);
			
			//Filter for \triple and \inst predicates. 
			//System.out.println(inputProgram.getCompleteText());
			List<String> filters = new LinkedList<String>();
			filters.add("tripled");
			filters.add("instd");
			invocation.setFilter(filters, true);
			
			//List of computed models, used to check at least a model is computed.
			final List<Model> models = new ArrayList<Model>();
			
			//Model handler: retrieves contexts and associations in the computed model(s).
			invocation.subscribe(new ModelHandler() {

				@Override
				public void handleResult(DLVInvocation paramDLVInvocation,
						ModelResult modelResult) {
					
					//System.out.print("{ ");
					Model model = (Model) modelResult;
					models.add(model);

					//model.beforeFirst();
					//while (model.hasMorePredicates()) {}

					//Predicate predicate = model.nextPredicate();
					Predicate predicate = model.getPredicate("instd");
					if (predicate != null){
						//System.out.println(predicate.name() + ": ");
						while (predicate.hasMoreLiterals()) {

							Literal literal = predicate.nextLiteral();
							if (literal.getAttributeAt(1).toString().equals("\"Context\"")) {
								
								//Add context to list of inferred contexts.
								contextsSet.add(literal.getAttributeAt(0).toString());
							
								//System.out.print(literal);
								//System.out.println(", ");	
							}
						}
					}
					
					predicate = model.getPredicate("tripled");
					if (predicate != null){
						//System.out.println(predicate.name() + ": ");
						while (predicate.hasMoreLiterals()) {

							//Add module association for each context.
							Literal literal = predicate.nextLiteral();
							if (literal.getAttributeAt(1).toString().equals("\"hasModule\"")) {
									
								String[] association = new String[2];
								association[0] = literal.getAttributeAt(0).toString();
								association[1] = literal.getAttributeAt(2).toString();
								hasModuleAssociations.add(association);
							
								//System.out.print(literal);
								//System.out.println(", ");	
							}
						}
					}
					
					//System.out.println("}");
					//System.out.println();
				}
			});
			
			long startTime = System.currentTimeMillis();
			
			invocation.run();
			invocation.waitUntilExecutionFinishes();
			
			long endTime = System.currentTimeMillis();
			globalModelComputationTime = endTime - startTime;
			
			//System.out.println("Global computation time: " + globalModelComputationTime + " ms.");
			
			List<DLVError> k = invocation.getErrors();
			if (k.size() > 0)
				System.err.println(k);
			
			//System.out.println("Number of computed models: " + models.size());
			if(models.size() == 0) 
				System.err.println("[!] No models for global context program.");
			
			//for (String[] a : hasModuleAssociations) {
			//	System.out.println(a[0] + " -> " + a[1]);
			//}
			
			//System.out.println("Contexts: ");
			//for (String s : contextsSet) {
			//	System.out.println(s);
			//	for(String[] a : hasModuleAssociations){
			//		if(a[0].equals(s))
			//		System.out.println("  -> " + a[1]);	
			//	}
			//}
		} catch (DLVInvocationException | IOException e) {
			e.printStackTrace();
		}
	}
	
	//--- REWRITING: COMPUTATION OF LOCAL KBs -------------------------
	
	/**
	 * Associates a local knowledge base to each of the contexts,
	 * by adding the associated knowledge modules (and the global knowledge 
	 * from global).
	 * 
	 */
	private void computeLocalKB(){
		for (String s : contextsSet) {
			//System.out.println(s);
			
			OWLOntology contextOntology = null;
			try {
				contextOntology = manager.createOntology();
				
				for(String[] a : hasModuleAssociations){
					if(a[0].equals(s)){
						//System.out.println("  -> " + a[1]);
					
						//Check that to each module name a local ontology exists!
						//int moduleIndex = inputCKR.getLocalOntologyName().indexOf(a[1]);
						CKRModule mod = inputCKR.getLocalModule(a[1]);
					
						if (mod == null) {
							System.err.println("[!] Warning: no ontology associated to module " + a[1]);
						} else {
							manager.addAxioms(contextOntology, 
									//inputCKR.getLocalOntology().get(moduleIndex).getAxioms()
									mod.getModuleOntology().getAxioms());
							//System.out.println("Module " + a[1] + " added to " + a[0]);
						}
					}
				}
				
				//***DO NOT*** Add axioms from global ontology.
				//manager.addAxioms(contextOntology, inputCKR.getGlobalOntology().getAxioms());
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			} 
			//if ((contextOntology == null) || (contextOntology.isEmpty()))
			//System.out.println("Warning: ontology for " + s + " is empty.");
			
			contextsOntologies.put(s, contextOntology);
		}
	}
	
	//--- FILE MANAGEMENT ---------------------------------------------
	
	/**
	 * Stores the computed CKR program to file.
	 * 
	 * @throws IOException
	 */
	public void storeToFile() throws IOException {
		
		DLProgramStorer storer = new DLProgramStorerImpl();
		
		//String datalogFile = inputCKR.getGlobalOntologyFilename() + ".dlv";
		//String datalogFile = "./testcase/output.dlv";
		//System.out.println(datalogGlobal.getStatements().size());
		
		FileWriter writer = new FileWriter(outputFilePath);
		storer.store(datalogCKR, writer);
		//writer.flush();
		writer.close();
		
		//System.out.println("CKR program saved in: " + outputFilePath);
	}
}
//=======================================================================