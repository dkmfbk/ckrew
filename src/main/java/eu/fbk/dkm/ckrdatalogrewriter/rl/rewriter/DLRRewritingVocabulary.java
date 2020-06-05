package eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter;

import org.semanticweb.drew.dlprogram.model.CacheManager;
import org.semanticweb.drew.dlprogram.model.NormalPredicate;

/**
 * @author Loris
 * @version 1.5
 * 
 * Static set of predicate names used in the DLliteR rewriting process.
 */
public class DLRRewritingVocabulary {
	
	private static CacheManager manager = CacheManager.getInstance();
	
	//--- VOCABULARY PREDICATES ---------------------------------------------------------
	
	public final static NormalPredicate NOM  = manager.getPredicate("nom", 1);
	public final static NormalPredicate CLS  = manager.getPredicate("cls", 1);
	public final static NormalPredicate ROLE = manager.getPredicate("rol", 1);

	//--- INSTANCE LEVEL PREDICATES -----------------------------------------------------
	
	public final static NormalPredicate INSTA = manager.getPredicate("insta", 2);
	public final static NormalPredicate INSTD = manager.getPredicate("instd", 2);
	
	public final static NormalPredicate TRIPLEA = manager.getPredicate("triplea", 3);
	public final static NormalPredicate TRIPLED = manager.getPredicate("tripled", 3);
	public final static NormalPredicate NTRIPLEA = manager.getPredicate("ntriplea", 3);
	
	//--- CLASS AXIOMS PREDICATES -------------------------------------------------------
	
	public final static NormalPredicate SUB_CLASS  = manager.getPredicate("subClass", 2);	
	public final static NormalPredicate SUB_EX     = manager.getPredicate("subEx", 2);// *!*
	
	public final static NormalPredicate SUP_EX     = manager.getPredicate("supEx", 3);
	public final static NormalPredicate SUP_NOT    = manager.getPredicate("supNot", 2);
	
	//--- PROPERTY AXIOMS PREDICATES ----------------------------------------------------
	
	public final static NormalPredicate SUB_ROLE    = manager.getPredicate("subRole", 2);

	public final static NormalPredicate INV_ROLE = manager.getPredicate("inv", 2);
	public final static NormalPredicate DIS_ROLE = manager.getPredicate("dis", 2);
	public final static NormalPredicate IRR_ROLE = manager.getPredicate("irr", 1);
		

	//--- DEFEASIBLE PREDICATES ---------------------------------------------------------	
		
	public final static NormalPredicate DEF_SUBCLASS = manager.getPredicate("def_subclass", 2);
	
	public final static NormalPredicate DEF_SUBROLE = manager.getPredicate("def_subr", 2);
	
	public final static NormalPredicate DEF_INVROLE = manager.getPredicate("def_inv", 2);
	public final static NormalPredicate DEF_IRRROLE = manager.getPredicate("def_irr", 1);
	
	//--- CONSTANTS ---------------------------------------------------------------------
	
	public final static NormalPredicate CONST = manager.getPredicate("const", 1);
	
	public final static NormalPredicate FIRST = manager.getPredicate("first", 1);
	public final static NormalPredicate LAST = manager.getPredicate("last", 1);
	public final static NormalPredicate NEXT = manager.getPredicate("next", 2);
	
	//XXX: ###########################
		
	//-----------------------------------------------------------------------------------
	
	
}
//=======================================================================