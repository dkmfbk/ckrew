/**
 * 
 */
package eu.fbk.dkm.ckrdatalogrewriter.rl.ckr;

import java.io.File;
import java.util.LinkedList;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWLProfileReport;

import eu.fbk.dkm.ckrdatalogrewriter.rl.profile.CKRRLProfile;

/**
 * @author Loris
 * @version 1.3
 * 
 * Represents the input CKR to be translated. 
 * Contains references to the global context and local modules and 
 * methods to load and verify each of the parts. 
 */
public class CKRKnowledgeBase {
	
	//--- FIELDS ---------------------------------------------
	
	private static final String META_SCHEMA_FILENAME = "./schemas/meta.n3";
	
	private CKRModule globalModule;
	private LinkedList<CKRModule> localModule;

	
	//--- CONSTRUCTOR ---------------------------------------------
	
	public CKRKnowledgeBase() {
		this.globalModule = new CKRModule("");
		this.localModule = new LinkedList<CKRModule>();
	}

	//--- GET METHODS ---------------------------------------------

	public String getGlobalOntologyFilename() {
		return globalModule.getModuleFilename();
	}

	public File getGlobalOntologyFile() {
		return globalModule.getModuleFile();
	}
	
	public OWLOntology getGlobalOntology() {
		return globalModule.getModuleOntology();
	}

	public CKRModule getGlobalModule() {
		return globalModule;
	}	
	
	public LinkedList<CKRModule> getLocalModule(){
		return localModule;
	}

	/**
	 * Counts number of axioms in the global and local knowledge modules.
	 * 
	 */
	public int getCKRSize(){
		
		int axcount;
		
		axcount = this.getGlobalOntology().getLogicalAxiomCount();
		
		for(CKRModule mod : localModule){
			axcount += mod.getModuleOntology().getLogicalAxiomCount();
		}		
		return axcount;
	}
	
	
	//--- SET METHODS ---------------------------------------------
	
	public void setGlobalOntologyFilename(String globalOntologyFilename) {
		this.globalModule.setModuleFilename(globalOntologyFilename);
	}
	
	public void setGlobalModule(CKRModule mod){
		this.globalModule = mod;
	}

	//--- MODULE SEARCH BY NAME ----------------------------------
	
	/**
	 * Returns the local <code>CKRModule</code> named as the input string.
	 * 
	 * @param moduleName name of the module to be retrieved  
	 */
	public CKRModule getLocalModule(String moduleName){
		CKRModule result = null;
		
		for(CKRModule mod : localModule){
			if (mod.getModuleName().equals(moduleName)) 
				result = mod;
		}
		return result;
	}
	
	//--- FILES LOAD ---------------------------------------------

	/**
	 * Creates and adds a local module with the specified filename.
	 * 
	 * @param filename name of the module file
	 */
	public boolean addLocalOntologyWithFilename(String filename){
		CKRModule mod = new CKRModule(filename);
		
		return localModule.add(mod);
	}
	
	/**
	 * Initializes the files and loads the ontologies
	 * for global and local knowledge bases.
	 * The filenames are assumed to be set by the corresponding methods.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	public void loadOntologies() throws OWLOntologyCreationException {
		File globalOntologyFile = new File(globalModule.getModuleFilename());
		globalModule.setModuleFile(globalOntologyFile);
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		
		//Load CKR primitives definition vocabulary.
		//TODO: add import to the global and local ontologies for CKR vocabulary
		File metaFile = new File(META_SCHEMA_FILENAME);
		OWLOntology meta = man.loadOntologyFromOntologyDocument(metaFile);
		
		//Load global ontology.
		OWLOntology globalOntology = man.loadOntologyFromOntologyDocument(globalOntologyFile);
		man.addAxioms(globalOntology, meta.getAxioms()); //*!*
		globalModule.setModuleOntology(globalOntology);
		//System.out.println("Global ontology loaded.");
		
		//Load list of local ontologies
		//localOntologyFile = new LinkedList<File>();
		//localOntology = new LinkedList<OWLOntology>();
		
		for (CKRModule mod : localModule) {
			  String s = mod.getModuleFilename();
			  File f = new File(s);
			  //localOntologyFile.add(f);
			  mod.setModuleFile(f);
			  
			  //As module name we take the filename of the module ontology
			  //System.out.println(f.getName().substring(0, f.getName().lastIndexOf('.')));
			  //localOntologyName.add("\"" + f.getName().substring(0, f.getName().lastIndexOf('.')) + "\"");
			  mod.setModuleName("\"" + f.getName().substring(0, f.getName().lastIndexOf('.')) + "\"");
			  
			  OWLOntology ont = man.loadOntologyFromOntologyDocument(f);
			  man.addAxioms(ont, meta.getAxioms()); //*!*
			  //localOntology.add(man.loadOntologyFromOntologyDocument(f));
			  //localOntology.add(ont);
			  mod.setModuleOntology(ont);
			  
			  //System.out.println("Local ontology loaded: " + s); 
		}
		
	}

	//--- PROFILES CHECK ---------------------------------------------
	
	/**
	 * Verifies whether global and local ontologies belong to
	 * the admitted OWL profile.
	 * 
	 * @return report about possible profile violations
	 */
	public OWLProfileReport checkProfiles() {		
		
		CKRRLProfile ckrrlprofile = new CKRRLProfile();
		
		//Check global ontology.
		OWLProfileReport report = ckrrlprofile.checkOntology(globalModule.getModuleOntology());
		
		if (!report.isInProfile()) {
			return report;
		} else{
			//System.out.println("Global ontology in OWL-RL Global.");	
		}

		//Check local ontologies.
		for (CKRModule mod : localModule) {
			OWLOntology o = mod.getModuleOntology();
			report = ckrrlprofile.checkOntology(o);
			
			if (!report.isInProfile()) {
				return report;
			} else{
				//System.out.println("Local ontology " 
			    //        + mod.getModuleFilename() + " in OWL-RL Local.");	
			}			
		}
		return report;
	}
	
}
//=======================================================================