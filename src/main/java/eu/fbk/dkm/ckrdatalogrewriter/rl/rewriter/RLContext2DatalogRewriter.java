package eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.semanticweb.drew.dlprogram.model.CacheManager;
import org.semanticweb.drew.dlprogram.model.Clause;
import org.semanticweb.drew.dlprogram.model.Constant;
import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.drew.dlprogram.model.Literal;
import org.semanticweb.drew.dlprogram.model.NormalPredicate;
import org.semanticweb.drew.dlprogram.model.Term;
import org.semanticweb.drew.dlprogram.model.Variable;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;
//import org.semanticweb.drew.el.SymbolEncoder;
//import org.semanticweb.drew.el.reasoner.DatalogFormat;
//import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * @author Loris
 * @version 1.4
 * 
 * General class for translation to Datalog of a contextual ontology.
 * 
 */
public class RLContext2DatalogRewriter extends OWLAxiomVisitorAdapter implements
		OWLEntityVisitor {
	
	protected static final String CKR_SCHEMA_URI = "http://dkm.fbk.eu/ckr/meta#";
	
	protected static final String MAIN = "main";
	protected static final String TOP = "top";
	protected static final String BOT = "bot";
	
	protected OWLOntology ontology;
	protected OWLDataFactory factory;
	//protected OWLOntologyManager manager;
	
    protected DLProgram datalog;
    protected IRI contextID;
    
    //--- CONSTRUCTORS ----------------------------------------------------------
    
    public RLContext2DatalogRewriter() {
        //SymbolEncoder<IRI> iriEncoder = CKRDReWELManager.getInstance().getIRIEncoder();
        //SymbolEncoder<OWLSubClassOfAxiom> superSomeAxiomEncoder = CKRDReWELManager.getInstance()
        //        .getSuperSomeAxiomEncoder();
    	
    	this.contextID = null; 
	}

    //--- SET AND GET METHODS --------------------------------------------------------
    
  	public IRI getContextID() {
  		return contextID;
  	}

  	public void setContextID(String cid) {
  		this.contextID = IRI.create(cid);
  	}    
    
    //--- REWRITE METHOD --------------------------------------------------------
    
	/**
	 * Returns the rewritten ontology as a Datalog program.
	 * 
	 * @param ontology the ontology to be rewritten
	 * @return the output Datalog program
	 */
	public DLProgram rewrite(OWLOntology ontology) {
		datalog = new DLProgram();
		
		//Normalize input ontology.
		ontology = new RLGlobalNormalization().normalize(ontology);

		//Rewriting for ontology components.
		for (OWLLogicalAxiom ax : ontology.getLogicalAxioms()) {
			ax.accept(this);
		}

		for (OWLNamedIndividual i : ontology.getIndividualsInSignature()) {
			i.accept(this);
		}

		for (OWLObjectProperty p : ontology.getObjectPropertiesInSignature()) {
			p.accept(this);
		}

		for (OWLClass cls : ontology.getClassesInSignature()) {
			cls.accept(this);
		}

		//Add Prl deduction rules.
		datalog.addAll(DeductionRuleset.getPrl());
		
		return datalog;
	}
	
	//--- ONTOLOGY VISIT METHODS --------------------------------------------------------
	//NOTE: in visiting the axioms, we suppose that the ontology has already been normalized.
	
	//- - ABOX AXIOMS - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	//Rewrite C(a)
	public void visit(OWLClassAssertionAxiom axiom) {
		// int c =
		// iriEncoder.encode(axiom.getClassExpression().asOWLClass().getIRI());
		// int a =
		// iriEncoder.encode(axiom.getIndividual().asOWLNamedIndividual().getIRI());
		// addFact(RewritingVocabulary.SUB_CLASS, a, c);

		IRI individualIRI = axiom.getIndividual().asOWLNamedIndividual().getIRI(); 
		IRI classIRI = rewConceptName(axiom.getClassExpression().asOWLClass());
		
		addFact(CKRRewritingVocabulary.INSTA,//
				individualIRI, //
				classIRI,
				contextID,
				IRI.create(MAIN));
	}
		
	@Override
	//Rewrite R(a,b)
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		// int p =
		// iriEncoder.encode(axiom.getProperty().asOWLObjectProperty().getIRI());
		// int s =
		// iriEncoder.encode(axiom.getSubject().asOWLNamedIndividual().getIRI());
		// int o =
		// iriEncoder.encode(axiom.getObject().asOWLNamedIndividual().getIRI());
		//
		// addFact(RewritingVocabulary.SUP_EX, s, p, o, o);
		
		addFact(CKRRewritingVocabulary.TRIPLEA, //
				axiom.getSubject().asOWLNamedIndividual().getIRI(), //
				axiom.getProperty().asOWLObjectProperty().getIRI(), //
				axiom.getObject().asOWLNamedIndividual().getIRI(),
				contextID,
				IRI.create(MAIN)//
		);
	}
	
	@Override
	//Translate R(a,b) (data)
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		// int p =
		// iriEncoder.encode(axiom.getProperty().asOWLDataProperty().getIRI());
		// int s =
		// iriEncoder.encode(axiom.getSubject().asOWLNamedIndividual().getIRI());
		// OWLLiteral object = axiom.getObject();
		// object.isRDFPlainLiteral();
		//
		// int o = iriEncoder.encode(axiom.getObject());
		//
		// addFact(RewritingVocabulary.SUP_EX, s, p, o, o);

	    System.err.println("warning: ignore datatype role axiom " + axiom);

		//datalog.add(
		//		new Clause(new Literal[] { new Literal( //
		//				CKRRewritingVocabulary.SUP_EX, //
		//				CacheManager.getInstance().getConstant(
		//						axiom.getSubject().asOWLNamedIndividual()
		//								.getIRI()),//
		//				CacheManager.getInstance().getConstant(
		//						axiom.getProperty().asOWLDataProperty()
		//								.getIRI()),//
		//				CacheManager.getInstance().getConstant(
		//						axiom.getObject().getLiteral()),//
		//				CacheManager.getInstance().getConstant(
		//						axiom.getObject().getLiteral())//
		//		) }, //
		//				new Literal[] {}));

	}	
	
	@Override
	//Rewrite \non R(a,b)
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		
//		addFact(CKRRewritingVocabulary.NEGTRIPLE, //
//				axiom.getSubject().asOWLNamedIndividual().getIRI(), //
//				axiom.getProperty().asOWLObjectProperty().getIRI(), //
//				axiom.getObject().asOWLNamedIndividual().getIRI(),
//				contextID//
//		);
//		addNegativeFact(CKRRewritingVocabulary.TRIPLEA, //
//				axiom.getSubject().asOWLNamedIndividual().getIRI(), //
//				axiom.getProperty().asOWLObjectProperty().getIRI(), //
//				axiom.getObject().asOWLNamedIndividual().getIRI(),
//				contextID//
//		);
		
//		Constant ind1 = getConstant(axiom.getSubject().asOWLNamedIndividual().getIRI());
//		Constant roleTerm = getConstant(axiom.getProperty().asOWLObjectProperty().getIRI());
//		Constant ind2 = getConstant(axiom.getObject().asOWLNamedIndividual().getIRI());
//		
//		Literal unsat = getLiteral(true, CKRRewritingVocabulary.UNSAT, 
//                getConstant(contextID), getVariable("T"));
//		
//		Literal tripled = getLiteral(true, CKRRewritingVocabulary.TRIPLED, 
//                ind1, roleTerm, ind2, getConstant(contextID), getVariable("T"));
//
//		addRule(unsat, tripled);
		
		addFact(CKRRewritingVocabulary.NTRIPLEA, //
				axiom.getSubject().asOWLNamedIndividual().getIRI(), //
				axiom.getProperty().asOWLObjectProperty().getIRI(), //
				axiom.getObject().asOWLNamedIndividual().getIRI(),
				contextID
		);
	}
	
	@Override
	//Translate \non R(a,b) (data)
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {

	    System.err.println("warning: ignore datatype role axiom " + axiom);

		//datalog.add(
		//		new Clause(new Literal[] { new Literal( //
		//				CKRRewritingVocabulary.SUP_EX, //
		//				CacheManager.getInstance().getConstant(
		//						axiom.getSubject().asOWLNamedIndividual()
		//								.getIRI()),//
		//				CacheManager.getInstance().getConstant(
		//						axiom.getProperty().asOWLDataProperty()
		//								.getIRI()),//
		//				CacheManager.getInstance().getConstant(
		//						axiom.getObject().getLiteral()),//
		//				CacheManager.getInstance().getConstant(
		//						axiom.getObject().getLiteral())//
		//		) }, //
		//				new Literal[] {}));

	}	
	
	@Override
	//Rewrite (a = b)
	//### ASSUME N=2! ###
	public void visit(OWLSameIndividualAxiom axiom) {
		
		addFact(CKRRewritingVocabulary.EQ, //
				axiom.getIndividualsAsList().get(0).asOWLNamedIndividual().getIRI(), //
				axiom.getIndividualsAsList().get(1).asOWLNamedIndividual().getIRI(), //
				contextID,
				IRI.create(MAIN)//
		);
	}
		
	@Override
	//Rewrite (a \neq b)
	//### ASSUME N=2! ###
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		
//		addNegativeFact(CKRRewritingVocabulary.EQ, //
//				axiom.getIndividualsAsList().get(0).asOWLNamedIndividual().getIRI(), //
//				axiom.getIndividualsAsList().get(1).asOWLNamedIndividual().getIRI(), //
//				contextID//
//		);

//		Constant ind1 = getConstant(axiom.getIndividualsAsList().get(0).asOWLNamedIndividual().getIRI());
//		Constant ind2 = getConstant(axiom.getIndividualsAsList().get(1).asOWLNamedIndividual().getIRI());
//		
//		Literal unsat = getLiteral(true, CKRRewritingVocabulary.UNSAT, 
//                getConstant(contextID), getVariable("T"));
//		
//		Literal peq = getLiteral(true, CKRRewritingVocabulary.EQ, 
//                ind1, ind2, getConstant(contextID), getVariable("T"));
//
//		addRule(unsat, peq);		
		
		
		//Add nothing, we already consider UNA
		//addFact(CKRRewritingVocabulary.NEQ, //
		//				axiom.getIndividualsAsList().get(0).asOWLNamedIndividual().getIRI(), //
		//				axiom.getIndividualsAsList().get(1).asOWLNamedIndividual().getIRI(), //
		//				contextID//
		//);
	}
	
	//- - TBOX AXIOMS - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

	@Override
	//Rewrite A \subs B
	public void visit(OWLSubClassOfAxiom axiom) {
		
		//Dependent on Global or Local version of the class.
		super.visit(axiom);
	}
	
	//- - RBOX AXIOMS - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
	
	@Override
	//Rewrite R \subs S
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		// int r =
		// iriEncoder.encode(axiom.getSubProperty().asOWLObjectProperty().getIRI());
		// int s =
		// iriEncoder.encode(axiom.getSuperProperty().asOWLObjectProperty().getIRI());
		//
		// addFact(RewritingVocabulary.SUB_ROLE, r, s);
		addFact(CKRRewritingVocabulary.SUB_ROLE, //
				axiom.getSubProperty().asOWLObjectProperty().getIRI(),//
				axiom.getSuperProperty().asOWLObjectProperty().getIRI(),
				contextID);
	}
	
	@Override
	//Rewrite R \circ S \subs T
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		List<OWLObjectPropertyExpression> chain = axiom.getPropertyChain();
		Iterator<OWLObjectPropertyExpression> iterator = chain.iterator();
		// int r1 =
		// iriEncoder.encode(iterator.next().asOWLObjectProperty().getIRI());
		// int r2 =
		// iriEncoder.encode(iterator.next().asOWLObjectProperty().getIRI());
		// int r =
		// iriEncoder.encode(axiom.getSuperProperty().asOWLObjectProperty().getIRI());
		//
		// addFact(RewritingVocabulary.SUB_R_CHAIN, r1, r2, r);
		addFact(CKRRewritingVocabulary.SUB_R_CHAIN, //
				iterator.next().asOWLObjectProperty().getIRI(),//
				iterator.next().asOWLObjectProperty().getIRI(),//
				axiom.getSuperProperty().asOWLObjectProperty().getIRI(),
				contextID//
		);
	}
	
	@Override
	//TODO: OK?
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		System.err.println("warning! ignore axiom " + axiom);

		// throw new IllegalStateException();
	}

	@Override
	//TODO: OK?
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		System.err.println("warning! ignore axiom " + axiom);

		// throw new IllegalStateException();
	}

	@Override
	//TODO: DO WE CONSIDER THEM?
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		// int p =
		// iriEncoder.encode(axiom.getProperty().asOWLObjectProperty().getIRI());
		// int d = iriEncoder.encode(axiom.getDomain().asOWLClass().getIRI());
		// addFact(RewritingVocabulary.DOMAIN, p, d);
		addFact(CKRRewritingVocabulary.DOMAIN, //
				axiom.getProperty().asOWLObjectProperty().getIRI(),//
				rewConceptName(axiom.getDomain().asOWLClass()), contextID);

	}

	@Override
	//TODO: DO WE CONSIDER THEM?
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		// int p =
		// iriEncoder.encode(axiom.getProperty().asOWLObjectProperty().getIRI());
		// int r = iriEncoder.encode(axiom.getRange().asOWLClass().getIRI());
		// addFact(RewritingVocabulary.RANGE, p, r);
		addFact(CKRRewritingVocabulary.RANGE, //
				axiom.getProperty().asOWLObjectProperty().getIRI(),//
				rewConceptName(axiom.getRange().asOWLClass()),
				contextID);

	}
	
	@Override
	//(already translated to chains) 
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		System.err.println("warning! ignore axiom " + axiom);
		// throw new IllegalStateException();
	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		//System.err.println("warning: ignore axiom " + axiom);
		
		addFact(CKRRewritingVocabulary.INV_ROLE, //
				axiom.getFirstProperty().asOWLObjectProperty().getIRI(),
				axiom.getSecondProperty().asOWLObjectProperty().getIRI(),
				contextID);		
	}
	
	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		//System.err.println("warning: ignore axiom " + axiom);

		addFact(CKRRewritingVocabulary.IRR_ROLE, //
				axiom.getProperty().asOWLObjectProperty().getIRI(),
				contextID);		
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		//System.err.println("warning: ignore axiom " + axiom);
		
		ArrayList<IRI> r = new ArrayList<IRI>();		
		for(OWLObjectPropertyExpression pe : axiom.getProperties()){
			r.add(pe.asOWLObjectProperty().getIRI());
		}
		
		addFact(CKRRewritingVocabulary.DIS_ROLE, //
				r.get(0),
				r.get(1),
				contextID);				
	}
	
	//- - SIGNATURE - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	//Concepts in signature
	public void visit(OWLClass cls) {
		// int c = iriEncoder.encode(cls.getIRI());
		// addFact(RewritingVocabulary.CLASS, c);
		addFact(CKRRewritingVocabulary.CLS, rewConceptName(cls), contextID);
	}

	@Override
	//Roles in signature
	public void visit(OWLObjectProperty property) {
		// int r = iriEncoder.encode(property.getIRI());
		// addFact(RewritingVocabulary.ROLE, r);
		addFact(CKRRewritingVocabulary.ROLE, property.getIRI(), contextID);
	}

	@Override
	//TODO: OK?
	public void visit(OWLDataProperty property) {
		// do nothing
	}

	@Override
	//Individuals in signature
	public void visit(OWLNamedIndividual individual) {
		// int i = iriEncoder.encode(individual.getIRI());
		// addFact(RewritingVocabulary.NOM, i);
		addFact(CKRRewritingVocabulary.NOM, individual.getIRI(), contextID);
	}
	
	@Override
	//TODO: OK?
	public void visit(OWLDatatype datatype) {
		// do nothing
	}

	@Override
	public void visit(OWLAnnotationProperty property) {
		// do nothing
	}
	
	//--- REWRITE CONCEPT NAMES --------------------------------------------------
	/**
	 * Rewrites top and bottom as constants used in the program.
	 * Returns IRI of input concept if not a top or bottom concept.
	 * 
	 * @param c concept name to be rewritten
	 */
	protected IRI rewConceptName(OWLClass c){
		
		if(c.isOWLThing())
			return IRI.create(TOP);
		else if (c.isOWLNothing())
			return IRI.create(BOT);
		else 
			return c.getIRI();
	}
	
	//XXX: ###################
	
	//--- ADD FACT METHODS -------------------------------------------------------

	/**
	 * Adds a fact to the translated program given a predicate and 
	 * an integer encoding of parameters.
	 * 
	 * @param predicate predicate of fact.
	 * @param params list of (encoded) parameters of fact.
	 */
	protected void addFact(NormalPredicate predicate, int... params) {
		List<Term> terms = new ArrayList<>();

		for (int param : params) {
			terms.add(CacheManager.getInstance().getConstant(param));
		}

		datalog.add(new Clause(new Literal[] { new Literal(predicate, terms
                .toArray(new Term[terms.size()])) }, new Literal[] {}));
	}

	/**
	 * Adds a fact to the translated program given a predicate and 
	 * a list of parameter IRIs.
	 * 
	 * @param predicate predicate of fact.
	 * @param params list of IRI for parameters of fact.
	 */
	protected void addFact(NormalPredicate predicate, IRI... params) {

		List<Term> terms = new ArrayList<>();

		for (IRI param : params) {
			terms.add(CacheManager.getInstance().getConstant(param));
		}

		datalog.add(
				new Clause(new Literal[] { new Literal(predicate, terms
                        .toArray(new Term[terms.size()])) }, new Literal[] {}));
	}

	
	/**
	 * Add a negative fact to the translated program given a predicate and 
	 * a list of parameter IRIs.
	 * 
	 * @param predicate predicate of fact.
	 * @param params list of IRI for parameters of fact.
	 */
	protected void addNegativeFact(NormalPredicate predicate, IRI... params) {

		List<Term> terms = new ArrayList<>();

		for (IRI param : params) {
			terms.add(CacheManager.getInstance().getConstant(param));
		}
		
		Literal negliteral = new Literal(predicate, terms.toArray(new Term[terms.size()]));
		negliteral.setNegative(true);

		datalog.add( new Clause(new Literal[] {negliteral}, new Literal[] {}) );
	}
	
	//--- ADD RULE METHOD -------------------------------------------------------

	/**
	 * Adds a rule to the translated program.
	 * 
	 */
	protected void addRule(Literal head, Literal... body) {		
		datalog.add( new Clause(new Literal[] {head},  body) );	  
	}

	/**
	 * Provides a literal with the given predicate and terms
	 * 
	 * @param positive indicates whether output literal is positive or negative.
	 */	
	protected Literal getLiteral(boolean positive, NormalPredicate predicate, Term... params) {		
		Literal literal = new Literal(predicate, params);
		
		if(!positive) literal.setNegative(true);
		
		return literal;
	}	

	/**
	 * Provides a constant representing the given IRI.
	 * 
	 */	
	protected Constant getConstant(IRI i) {
		return CacheManager.getInstance().getConstant(i);
	}

	/**
	 * Provides a variable identified from the given string.
	 * 
	 */	
	protected Variable getVariable(String s) {
		return CacheManager.getInstance().getVariable(s);
	}

}
//=======================================================================