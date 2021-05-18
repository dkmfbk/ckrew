package eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

/**
 * @author Loris
 * @version 1.6
 * 
 * Encodes the <code>Irl</code> and <code>Iglob</code>
 * rules for translation to Datalog of the global ontology
 * for a Multi-Relational CKR.
 * 
 */
public class MRGlobal2DatalogRewriter extends RLContext2DatalogRewriter {
		
	//private OWLAnnotationProperty hasAxiomType;
	//private OWLAnnotationValue defeasible;
	
	private OWLObjectProperty precTime;
	private OWLObjectProperty precCovers;
	private OWLObjectProperty hasModule;
	
	private IRI time;
	private IRI covers;
	
	private IRI contextClassIRI;


	//Set of contexts
	final private Set<String> contextsSet = new HashSet<String>();
	
	//Set of context to module associations
	final private Set<String[]> hasModuleAssociations = new HashSet<String[]>();
		
    //--- CONSTRUCTORS ----------------------------------------------------------
    
    public MRGlobal2DatalogRewriter() {
    	
    	this.contextID = IRI.create("g");    	
	}
    
    //--- REWRITE METHOD --------------------------------------------------------
    
    @Override
	/**
	 * Returns the rewritten (global) ontology as a Datalog program.
	 * 
	 * @param ontology the ontology (global) to be rewritten
	 * @return the output Datalog program
	 */
	public DLProgram rewrite(OWLOntology ontology) {
		datalog = new DLProgram();
				
		//Instantiate global properties.
		this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		this.precTime = factory.getOWLObjectProperty(
				IRI.create(CKR_SCHEMA_URI, "prec-t"));
		
		this.precCovers = factory.getOWLObjectProperty(
				IRI.create(CKR_SCHEMA_URI, "prec-c"));
		
		this.time   = IRI.create(CKR_SCHEMA_URI, "time");
		this.covers = IRI.create(CKR_SCHEMA_URI, "covers");
		
		this.contextClassIRI = IRI.create(CKR_SCHEMA_URI, "Context");
		this.hasModule = factory.getOWLObjectProperty(
				IRI.create(CKR_SCHEMA_URI, "hasModule"));
		
		//Normalize input ontology.
		//TODO: add normalization (check)
		//ontology = new RLGlobalNormalization().normalize(ontology);
		    	    	
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
		
 		//Add Pglob deduction rules.
		datalog.addAll(MRDeductionRuleset.getPglob());
		
		return datalog;
	}

	//XXX: #####################
    
	//--- IS DEFEASIBLE -----------------------------------------------------------------

	//public boolean isDefeasible(OWLAxiom a){
	//	for (OWLAnnotation ann : a.getAnnotations(hasAxiomType)) {
	//		if (ann.getValue().equals(defeasible))
	//			return true;
	//	}
	//	return false;
	//}
    
	//--- GET AND SET METHODS ---------------------------------------------
  
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

    
	//--- ONTOLOGY VISIT METHODS --------------------------------------------------------
	//NOTE: in visiting the axioms, we suppose that the ontology has already been normalized.
	
	//- - ABOX AXIOMS - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    
	@Override
	//Rewrite C(a)
	public void visit(OWLClassAssertionAxiom axiom) {

		IRI individualIRI = axiom.getIndividual().asOWLNamedIndividual().getIRI(); 
		IRI classIRI = rewConceptName(axiom.getClassExpression().asOWLClass());

		if(!individualIRI.getStart().equals(CKR_SCHEMA_URI)){//leave out CKR metaknowledge...
		
			if(classIRI.equals(contextClassIRI)) {
				//System.out.println("Context: " + individualIRI.getFragment());
				
				//add context(c) to program
				addFact(MRRewritingVocabulary.CTX,
						individualIRI);				
				
				//store context name in contexts set
				contextsSet.add(individualIRI.getFragment());
				
			} else {
				//Ignore any other ABox axiom...
				
				//addFact(MRRewritingVocabulary.INSTA,
				//		individualIRI, 
				//		classIRI,
				//		contextID,
				//		IRI.create(MAIN));
			}
		}
	}
		
	@Override
	//Rewrite R(a,b)
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		
		if(axiom.getProperty().asOWLObjectProperty().equals(precTime)) {
			//System.out.println("precTime: " + axiom);

			addFact(MRRewritingVocabulary.PREC, //
					axiom.getSubject().asOWLNamedIndividual().getIRI(),
					axiom.getObject().asOWLNamedIndividual().getIRI(),
					time//
			);
			
		} else if(axiom.getProperty().asOWLObjectProperty().equals(precCovers)) {
			//System.out.println("precCovers: " + axiom);

			addFact(MRRewritingVocabulary.PREC, //
					axiom.getSubject().asOWLNamedIndividual().getIRI(),
					axiom.getObject().asOWLNamedIndividual().getIRI(),
					covers//
			);

		} else if(axiom.getProperty().asOWLObjectProperty().equals(hasModule)) {
			//System.out.println("hasModule: " + axiom);

			//do not save to program, store in context to module association
			String[] association = new String[2];
			association[0] = axiom.getSubject().asOWLNamedIndividual().getIRI().getFragment();
			association[1] = "\"" + axiom.getObject().asOWLNamedIndividual().getIRI().getFragment() + "\"";
			hasModuleAssociations.add(association);
			
		} else {	
			//Ignore any other ABox axiom...
	
			//addFact(MRRewritingVocabulary.TRIPLEA, //
			//		axiom.getSubject().asOWLNamedIndividual().getIRI(),
			//		axiom.getProperty().asOWLObjectProperty().getIRI(),
			//		axiom.getObject().asOWLNamedIndividual().getIRI(),
			//		contextID,
			//		IRI.create(MAIN)//
			//);
		}
	}
	
	@Override
	//Rewrite \non R(a,b)
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		
		System.err.println("warning! ignore axiom " + axiom);
		// throw new IllegalStateException();
		
		//addFact(CKRRewritingVocabulary.NTRIPLEA, //
		//		axiom.getSubject().asOWLNamedIndividual().getIRI(), //
		//		axiom.getProperty().asOWLObjectProperty().getIRI(), //
		//		axiom.getObject().asOWLNamedIndividual().getIRI(),
		//		contextID
		//);
	}
		
	//- - TBOX AXIOMS - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    
	//No TBox axioms can appear in global context for MR-CKR!
	
	@Override
	//Rewrite A \subs B
	public void visit(OWLSubClassOfAxiom axiom) {
		//OWLClassExpression subClass = axiom.getSubClass();
		//OWLClassExpression superClass = axiom.getSuperClass();

		System.err.println("warning! ignore axiom " + axiom);
		// throw new IllegalStateException();
				
		//// atomic case: A \subs C
		//if ((subClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
		//		&& (superClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS)) {
        //
		//	//if (subClass.isOWLThing()) {
		//	//	// addFact(RewritingVocabulary.TOP, c);
		//	//	addFact(CKRRewritingVocabulary.TOP, axiom.getSuperClass()
		//	//			.asOWLClass().getIRI());
		//	//} else if (superClass.isOWLNothing()) {
		//	//	// addFact(RewritingVocabulary.BOT, a);
		//	//	addFact(CKRRewritingVocabulary.BOT, axiom.getSubClass()
		//	//			.asOWLClass().getIRI());
		//	//} else /* (!subClass.isOWLNothing() && !superClass.isOWLThing()) */{
		//		// addFact(RewritingVocabulary.SUB_CLASS, a, c);
        //
		//	       addFact(CKRRewritingVocabulary.SUB_CLASS, //
		//				rewConceptName(subClass.asOWLClass()), 
		//				rewConceptName(superClass.asOWLClass()),
		//				contextID);
		//	//}
		//}
		//
		//// A' subclass C
		//else if (superClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
		//	// and(A, B) subclass C
		//	if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
		//		// TODO: CASE WITH N>2 IN NORMALIZATION
        //
		//		OWLObjectIntersectionOf inter = (OWLObjectIntersectionOf) subClass;
		//		Set<OWLClassExpression> operands = inter.getOperands();
		//		IRI[] params = new IRI[4];
		//		int i = 0;
		//		for (OWLClassExpression op : operands) {
		//			params[i++] = rewConceptName(op.asOWLClass());
		//		}
		//		params[2] = rewConceptName(superClass.asOWLClass());
		//		params[3] = contextID;
        //
		//		addFact(CKRRewritingVocabulary.SUB_CONJ, params);				
		//	}
		//	
		//	// exist(R,A) subclass B
		//	else if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
		//		OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) subClass;
        //
		//		addFact(CKRRewritingVocabulary.SUB_EX,//
		//				some.getProperty().asOWLObjectProperty().getIRI(),//
		//				rewConceptName(some.getFiller().asOWLClass()),//
		//				rewConceptName(superClass.asOWLClass()), 
		//				contextID);				
        //
		//	} else if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_ONE_OF) {
		//		// {c} \subs A -> inst(c, A)
		//		OWLObjectOneOf oneOf = (OWLObjectOneOf) subClass;
        //
		//		addFact(CKRRewritingVocabulary.INSTA, //
		//				oneOf.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI(),//
		//				rewConceptName(superClass.asOWLClass()),
		//				contextID,
		//				IRI.create(MAIN));				
        //
		//	} else {
		//		throw new IllegalStateException();
		//	}
		//}
		//
		//// A subclass C'
		//else if (subClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
		//	
		//	if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
		//		
		//		OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) superClass;
		//		OWLObjectOneOf object = (OWLObjectOneOf) some.getFiller();
		//		
		//		addFact(CKRRewritingVocabulary.SUP_EX, //
		//				rewConceptName(subClass.asOWLClass()),//
		//				some.getProperty().asOWLObjectProperty().getIRI(),
		//				object.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI(),
		//				contextID);
		//		
		//	// A subclass all(R, C')		
		//	} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM) {
        //
		//		OWLObjectAllValuesFrom all = (OWLObjectAllValuesFrom) superClass;
		//		OWLClass object = (OWLClass) all.getFiller();
		//		
		//		addFact(CKRRewritingVocabulary.SUP_ALL, //
		//				rewConceptName(subClass.asOWLClass()),//
		//				all.getProperty().asOWLObjectProperty().getIRI(),
		//				rewConceptName(object.asOWLClass()),
		//				contextID);
		//					    
		//	// A subclass max(R, 1, B')
		//    // Assuming n=1
		//	} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_MAX_CARDINALITY) {
		//		
		//		OWLObjectMaxCardinality max = (OWLObjectMaxCardinality) superClass;
		//		//OWLClass object = (OWLClass) max.getFiller();
		//		
		//		addFact(CKRRewritingVocabulary.SUP_LEQONE, //
		//				rewConceptName(subClass.asOWLClass()),//
		//				max.getProperty().asOWLObjectProperty().getIRI(),
		//				contextID);
		//					    
		//	// A subclass not(B)
		//	//No more in normal form! 
		//	} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF) {
        //
		//	} else {
		//		throw new IllegalStateException();
		//	}
		//}
		
		//super.visit(axiom);
	}
	
	//- - RBOX AXIOMS - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

	//No RBox axioms can appear in global context for MR-CKR!
	
	@Override
	//Rewrite R \subs S
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {

		System.err.println("warning! ignore axiom " + axiom);
		// throw new IllegalStateException();
		
		//addFact(CKRRewritingVocabulary.SUB_ROLE, //
		//		axiom.getSubProperty().asOWLObjectProperty().getIRI(),//
		//		axiom.getSuperProperty().asOWLObjectProperty().getIRI(),
		//		contextID);
		
	}
		
	@Override
	//Rewrite R \circ S \subs T
	public void visit(OWLSubPropertyChainOfAxiom axiom) {

		System.err.println("warning! ignore axiom " + axiom);
		// throw new IllegalStateException();
		
		//List<OWLObjectPropertyExpression> chain = axiom.getPropertyChain();
		//Iterator<OWLObjectPropertyExpression> iterator = chain.iterator();
        //
		//IRI r1 = iterator.next().asOWLObjectProperty().getIRI();
		//IRI r2 = iterator.next().asOWLObjectProperty().getIRI();
		//
		//addFact(CKRRewritingVocabulary.SUB_R_CHAIN, //
		//		r1,//
		//		r2,//
		//		axiom.getSuperProperty().asOWLObjectProperty().getIRI(),
		//		contextID//
		//);
		
	}
		
	@Override
	//Rewrite INV(R, S)
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {

		System.err.println("warning! ignore axiom " + axiom);
		// throw new IllegalStateException();
		
		//addFact(CKRRewritingVocabulary.INV_ROLE, //
		//		axiom.getFirstProperty().asOWLObjectProperty().getIRI(),
		//		axiom.getSecondProperty().asOWLObjectProperty().getIRI(),
		//		contextID);
	}
	
	@Override
	//Rewrite IRR(R)
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {

		System.err.println("warning! ignore axiom " + axiom);
		// throw new IllegalStateException();
		
		//addFact(CKRRewritingVocabulary.IRR_ROLE, //
		//		axiom.getProperty().asOWLObjectProperty().getIRI(),
		//		contextID);
		
	}
	
	@Override
	//Rewrite DIS(R,S)
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {

		System.err.println("warning! ignore axiom " + axiom);
		// throw new IllegalStateException();
		
		//ArrayList<IRI> r = new ArrayList<IRI>();		
		//for(OWLObjectPropertyExpression pe : axiom.getProperties()){
		//	r.add(pe.asOWLObjectProperty().getIRI());
		//}
		//
		//addFact(CKRRewritingVocabulary.DIS_ROLE,
		//		r.get(0),
		//		r.get(1),
		//		contextID);		
	}
	
	@Override
	//Ignore Domain
	public void visit(OWLObjectPropertyDomainAxiom axiom) {

		return;
	}

	@Override
	//Ignore Range
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		
		return;
	}	
	
	//- - SIGNATURE - - - - - - - - - - - - - - - - - - - - - - - -	

	@Override
	//Individuals in signature
	public void visit(OWLNamedIndividual individual) {
		
		//if(!individual.getIRI().getStart().equals(CKR_SCHEMA_URI)){
		//
		//	addFact(MRRewritingVocabulary.NOM, 
		//			individual.getIRI(), 
		//			contextID
		//	);		
		//}
	}
	
	@Override
	//Concepts in signature
	public void visit(OWLClass cls) {
		
		//if(!cls.getIRI().getStart().equals(CKR_SCHEMA_URI)){
		//
		//	addFact(MRRewritingVocabulary.CLS, 
		//			rewConceptName(cls), 
		//			contextID
		//	);
		//}
	}
	
	@Override
	//Roles in signature
	public void visit(OWLObjectProperty property) {

		//if(!property.getIRI().getStart().equals(CKR_SCHEMA_URI)){
		//	addFact(MRRewritingVocabulary.ROLE, 
		//			property.getIRI(), 
		//			contextID
		//	);
		//}
	}
		
}
//=======================================================================