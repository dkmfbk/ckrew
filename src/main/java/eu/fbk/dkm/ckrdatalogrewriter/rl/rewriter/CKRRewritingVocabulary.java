package eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter;

import org.semanticweb.drew.dlprogram.model.CacheManager;
import org.semanticweb.drew.dlprogram.model.NormalPredicate;

/**
 * @author Loris
 * @version 1.3
 * 
 * Static set of predicate names used in the rewriting process.
 */
public class CKRRewritingVocabulary {
	
	private static CacheManager manager = CacheManager.getInstance();
	
	//--- VOCABULARY PREDICATES ---------------------------------------------------------
	
	public final static NormalPredicate NOM  = manager.getPredicate("nom", 2);
	public final static NormalPredicate CLS  = manager.getPredicate("cls", 2);
	public final static NormalPredicate ROLE = manager.getPredicate("rol", 2);

	//--- INSTANCE LEVEL PREDICATES -----------------------------------------------------
	
	//public final static NormalPredicate INST = manager.getPredicate("inst", 3);
	public final static NormalPredicate INSTA = manager.getPredicate("insta", 3);
	public final static NormalPredicate INSTD = manager.getPredicate("instd", 3);	
	
	//public final static NormalPredicate TRIPLE = CacheManager.getInstance().getPredicate("triple", 4);
	public final static NormalPredicate TRIPLEA = manager.getPredicate("triplea", 4);
	public final static NormalPredicate TRIPLED = manager.getPredicate("tripled", 4);	
	
	//public final static NormalPredicate NEGTRIPLE = CacheManager.getInstance().getPredicate("negtriple", 4);
	
	public final static NormalPredicate EQ  = manager.getPredicate("eq", 3);
	public final static NormalPredicate NEQ = manager.getPredicate("neq", 3);
	
	//--- CLASS AXIOMS PREDICATES -------------------------------------------------------
	
	public final static NormalPredicate SUB_CLASS  = manager.getPredicate("subClass", 3);
	public final static NormalPredicate SUB_CONJ   = manager.getPredicate("subConj", 4);	
	public final static NormalPredicate SUB_EX     = manager.getPredicate("subEx", 4);
	
	public final static NormalPredicate SUP_EX     = manager.getPredicate("supEx", 4);
	public final static NormalPredicate SUP_ALL    = manager.getPredicate("supForall", 4);
	public final static NormalPredicate SUP_LEQONE = manager.getPredicate("supLeqOne", 4);
	public final static NormalPredicate SUP_NOT    = manager.getPredicate("supNot", 3);
	
	//--- PROPERTY AXIOMS PREDICATES ----------------------------------------------------
	
	public final static NormalPredicate SUB_ROLE    = manager.getPredicate("subRole", 3);
	public final static NormalPredicate SUB_R_CHAIN = manager.getPredicate("subRChain", 4);

	public final static NormalPredicate INV_ROLE = manager.getPredicate("inv", 3);
	public final static NormalPredicate DIS_ROLE = manager.getPredicate("dis", 3);
	public final static NormalPredicate IRR_ROLE = manager.getPredicate("irr", 2);
	
	public final static NormalPredicate DOMAIN   = manager.getPredicate("domain", 3);
	public final static NormalPredicate DOMAIN_D = manager.getPredicate("domain_d", 3);
	
	public final static NormalPredicate RANGE = manager.getPredicate("range", 3);
	
	//--- EVAL PREDICATES ---------------------------------------------------------------

	public final static NormalPredicate SUB_EVAL_AT = manager.getPredicate("subEval", 4);
	public final static NormalPredicate SUB_EVAL_R  = manager.getPredicate("subEvalR", 4);

	//--- OVERRIDING PREDICATES ---------------------------------------------------------
	
	public final static NormalPredicate OVRINSTA    = manager.getPredicate("ovrInsta", 3);
	public final static NormalPredicate OVRTRIPLEA  = manager.getPredicate("ovrTriplea", 4);
	public final static NormalPredicate OVRNTRIPLEA = manager.getPredicate("ovrNTriplea", 4);
	
	public final static NormalPredicate OVRSUBCLASS = manager.getPredicate("ovrSubClass", 4);
	public final static NormalPredicate OVRSUBCONJ  = manager.getPredicate("ovrSubConj", 5);
	public final static NormalPredicate OVRSUBEX    = manager.getPredicate("ovrSubEx", 5);
	
	public final static NormalPredicate OVRSUPEX     = manager.getPredicate("ovrSupEx", 5);
	public final static NormalPredicate OVRSUPALL    = manager.getPredicate("ovrSupAll", 6);
	public final static NormalPredicate OVRSUPLEQONE = manager.getPredicate("ovrSupLeqOne", 7);	
	public final static NormalPredicate OVRSUPNOT    = manager.getPredicate("ovrSupNot", 4);
	
	public final static NormalPredicate OVRSUBROLE   = manager.getPredicate("ovrSubRole", 5);
	public final static NormalPredicate OVRSUBRCHAIN = manager.getPredicate("ovrSubRChain", 7);
	
	public final static NormalPredicate OVRINVROLE = manager.getPredicate("ovrInv", 5);
	public final static NormalPredicate OVRIRRROLE = manager.getPredicate("ovrIrr", 3);
	public final static NormalPredicate OVRDISROLE = manager.getPredicate("ovrDis", 5);
	
	public final static NormalPredicate PREC = manager.getPredicate("prec", 2);
	
	//-----------------------------------------------------------------------------------
	
	//public final static NormalPredicate ISA = CacheManager.getInstance().getPredicate("inst", 2);
    //
	//public final static NormalPredicate TOP = CacheManager.getInstance().getPredicate("top", 1);
	//public final static NormalPredicate BOT = CacheManager.getInstance().getPredicate("bot", 1);
    //
	//public final static NormalPredicate SUB_SELF = CacheManager.getInstance().getPredicate("subSelf", 2);
	//public final static NormalPredicate SUP_SELF = CacheManager.getInstance().getPredicate("supSelf", 3);
	//
	//public final static NormalPredicate SUB_R_CONJ = CacheManager.getInstance().getPredicate("subRConj", 3);
    //
	//public static final NormalPredicate INPUT = CacheManager.getInstance().getPredicate("input", 1);
	////public final static List<NormalPredicate> elRewritingPredicates = new ArrayList<NormalPredicate>();
    //
	//public final static NormalPredicate SUBSET = CacheManager.getInstance().getPredicate("subset", 2);
	//
	//public final static NormalPredicate ISA_P = CacheManager.getInstance().getPredicate("isa_p", 3);
    //
	//public final static NormalPredicate TRIPLE_P = CacheManager.getInstance().getPredicate("triple_p", 4);
	//
	//public final static String DL_ATOM = "dl_ATOM";
	
}
//=======================================================================