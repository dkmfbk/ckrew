/**
 * 
 */
package eu.fbk.dkm.ckrdatalogrewriter.rl.ckr;

import java.io.File;

import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Loris
 * @version 1.3
 * 
 * Represents a global or local module of the input CKR. 
 * Contains references to the ontology file representing the module,
 * its filename and module name and the reference to its <code>OWLOntology</code> representation.
 */
public class CKRModule {
	
	private String moduleFilename;
	private String moduleName;
	
	private File moduleFile;
	private OWLOntology moduleOntology;
	
	//--- CONSTRUCTOR ---------------------------------------------
	
	public CKRModule(String moduleFilename) {
		this.moduleFilename = moduleFilename;
	}
	
	//--- GET METHODS ---------------------------------------------
	
	public String getModuleFilename() {
		return moduleFilename;
	}

	public String getModuleName() {
		return moduleName;
	}

	public File getModuleFile() {
		return moduleFile;
	}
	
	public OWLOntology getModuleOntology() {
		return moduleOntology;
	}
	
	//--- SET METHODS ---------------------------------------------
	
	public void setModuleFilename(String fn) {
		this.moduleFilename = fn;
	}

	public void setModuleName(String mn) {
		this.moduleName = mn;
	}
	
	public void setModuleFile(File f) {
		this.moduleFile = f;
	}	
	
	public void setModuleOntology(OWLOntology o) {
		this.moduleOntology = o;
	}
	
	//--- EQUALS ---------------------------------------------
	
	public boolean equals(CKRModule other){
		return this.moduleFilename.equals(other.getModuleFilename());
	}
	
	//--- TO STRING ---------------------------------------------
	
	public String toString() {
		return this.moduleFilename;
	}
	
}
