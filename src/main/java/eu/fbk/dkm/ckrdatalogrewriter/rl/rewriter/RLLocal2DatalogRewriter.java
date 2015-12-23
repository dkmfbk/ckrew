package eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter;

import java.util.Set;

import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

/**
 * @author Loris
 * @version 1.3
 * 
 * Encodes the <code>Irl</code> and <code>Iloc</code>
 * rules for translation to Datalog of the local ontologies.
 */
public class RLLocal2DatalogRewriter extends RLContext2DatalogRewriter {
    
	private OWLAnnotationProperty hasEvalMeta;
	private OWLAnnotationProperty hasEvalObject;

    //--- CONSTRUCTORS ----------------------------------------------------------
   
    public RLLocal2DatalogRewriter() {
    	super();
    	//this.localID = null; 
	}
    	
    //--- REWRITE METHOD --------------------------------------------------------
    
    @Override
	/**
	 * Returns the rewritten (local) ontology as a Datalog program.
	 * 
	 * @param ontology the ontology (local) to be rewritten
	 * @return the output Datalog program
	 */
	public DLProgram rewrite(OWLOntology ontology) {
		datalog = new DLProgram();
		
		//Instantiate local properties.
		this.ontology = ontology;
		this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		this.hasEvalMeta = factory.getOWLAnnotationProperty(
				IRI.create(CKR_SCHEMA_URI, "hasEvalMeta"));
		this.hasEvalObject = factory.getOWLAnnotationProperty(
				IRI.create(CKR_SCHEMA_URI, "hasEvalObject"));
		
		//Normalization for local ontologies.
		ontology = new RLLocalNormalization().normalize(ontology);
		
		//Rewriting of local ontologies.
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
    	
		//datalog.addAll(DeductionRuleset.getPloc());
	    addFact(CKRRewritingVocabulary.PREC, //
				contextID,
				IRI.create("g"));
    	
		return datalog;
	}
	
	//--- ONTOLOGY VISIT METHODS --------------------------------------------------------
	//NOTE: in visiting the axioms, we suppose that the ontology has already been normalized.
	
	//- - ABOX AXIOMS - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	    
	//- - TBOX AXIOMS - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    
	@Override
	//Rewrite A \subs B
	public void visit(OWLSubClassOfAxiom axiom) {
		OWLClassExpression subClass = axiom.getSubClass();
		OWLClassExpression superClass = axiom.getSuperClass();

		// atomic case: A \subs C
		if ((subClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
				&& (superClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS)) {
			// int a =
			// iriEncoder.encode(axiom.getSubClass().asOWLClass().getIRI());
			// int c =
			// iriEncoder.encode(axiom.getSuperClass().asOWLClass().getIRI());

			//if (subClass.isOWLThing()) {
			//	// addFact(RewritingVocabulary.TOP, c);
			//	addFact(CKRRewritingVocabulary.TOP, axiom.getSuperClass()
			//			.asOWLClass().getIRI());
			//} else if (superClass.isOWLNothing()) {
			//	// addFact(RewritingVocabulary.BOT, a);
			//	addFact(CKRRewritingVocabulary.BOT, axiom.getSubClass()
			//			.asOWLClass().getIRI());
			//} else /* (!subClass.isOWLNothing() && !superClass.isOWLThing()) */{
				// addFact(RewritingVocabulary.SUB_CLASS, a, c);
			
			//CASE FOR EVAL: eval(A,C) \subs B
			IRI metaClass = null;
			IRI objectClass = null; 
			for (OWLAnnotationAssertionAxiom a : subClass.asOWLClass().getAnnotationAssertionAxioms(ontology)) {
				if (a.getProperty().equals(hasEvalMeta)){
					metaClass = (IRI) a.getValue();
				} else if (a.getProperty().equals(hasEvalObject)) {
					objectClass = (IRI) a.getValue();
				}
			}
			if ((metaClass != null) && (objectClass != null)){
			    addFact(CKRRewritingVocabulary.SUB_EVAL_AT,
			    		objectClass, metaClass,//
						superClass.asOWLClass().getIRI(),
						contextID);				
			}
			
		    addFact(CKRRewritingVocabulary.SUB_CLASS, //
					subClass.asOWLClass().getIRI(), 
					superClass.asOWLClass().getIRI(),
					contextID);
			//}
		}
		
		// A' subclass C
		else if (superClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
			// and(A, B) subclass C
			if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
				// TODO: CASE WITH N>2 IN NORMALIZATION
				// OWLObjectIntersectionOf inter = (OWLObjectIntersectionOf) subClass;
				// Set<OWLClassExpression> operands = inter.getOperands();
				// int[] params = new int[3];
				// int i = 0;
				// for (OWLClassExpression op : operands) {
				//   params[i++] = iriEncoder.encode(op.asOWLClass().getIRI());
				// }
				// params[2] = iriEncoder.encode(superClass.asOWLClass().getIRI());
				// addFact(RewritingVocabulary.SUB_CONJ, params);
				OWLObjectIntersectionOf inter = (OWLObjectIntersectionOf) subClass;
				Set<OWLClassExpression> operands = inter.getOperands();
				IRI[] params = new IRI[4];
				int i = 0;
				for (OWLClassExpression op : operands) {
					params[i++] = op.asOWLClass().getIRI();
				}
				params[2] = superClass.asOWLClass().getIRI();
				params[3] = contextID;
				
				addFact(CKRRewritingVocabulary.SUB_CONJ, params);
			}

			// exist(R,A) subclass B
			else if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
				OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) subClass;

				// int r =
				// iriEncoder.encode(some.getProperty().asOWLObjectProperty().getIRI());
				// int a =
				// iriEncoder.encode(some.getFiller().asOWLClass().getIRI());
				// int b = iriEncoder.encode(superClass.asOWLClass().getIRI());
				//
				// addFact(RewritingVocabulary.SUB_EX, r, a, b);
				addFact(CKRRewritingVocabulary.SUB_EX,//
						some.getProperty().asOWLObjectProperty().getIRI(),//
						some.getFiller().asOWLClass().getIRI(),//
						superClass.asOWLClass().getIRI(), 
						contextID);

			} else if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_ONE_OF) {
				// {c} \subs A -> inst(c, A)
				OWLObjectOneOf oneOf = (OWLObjectOneOf) subClass;
				// int c =
				// iriEncoder.encode(oneOf.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI());
				// int a = iriEncoder.encode(superClass.asOWLClass().getIRI());
				// addFact(RewritingVocabulary.SUB_CLASS, c, a);
				addFact(CKRRewritingVocabulary.INSTA, //
						oneOf.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI(),//
						superClass.asOWLClass().getIRI(),
						contextID);

			//} else if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
			//	throw new IllegalStateException();	
			//} else if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_HAS_SELF) {
			//	// OWLObjectHasSelf self = (OWLObjectHasSelf) subClass;
			//	// int r = DReWELManager.getInstance().getIRIEncoder()
			//	// .encode(self.getProperty().asOWLObjectProperty().getIRI());
			//	// int a =
			//	// DReWELManager.getInstance().getIRIEncoder().encode(superClass.asOWLClass().getIRI());
			//	// addFact(RewritingVocabulary.SUB_SELF, r, a);
			//	OWLObjectHasSelf self = (OWLObjectHasSelf) subClass;
			//	addFact(CKRRewritingVocabulary.SUB_SELF,//
			//			self.getProperty().asOWLObjectProperty().getIRI(),//
			//			superClass.asOWLClass().getIRI());
			} else {
				throw new IllegalStateException();
			}
		}
		
		// A subclass C'
		else if (subClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {

			if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
				
					OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) superClass;
					OWLObjectOneOf object = (OWLObjectOneOf) some.getFiller();
					
					addFact(CKRRewritingVocabulary.SUP_EX, //
							subClass.asOWLClass().getIRI(),//
							some.getProperty().asOWLObjectProperty().getIRI(),
							object.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI(),
							contextID);
		
			// A subclass all(R, C')		
			} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM) {

				OWLObjectAllValuesFrom all = (OWLObjectAllValuesFrom) superClass;
				OWLClass object = (OWLClass) all.getFiller();
				
				addFact(CKRRewritingVocabulary.SUP_ALL, //
						subClass.asOWLClass().getIRI(),//
						all.getProperty().asOWLObjectProperty().getIRI(),
						object.asOWLClass().getIRI(),
						contextID);
				
			// A subclass max(R, 1, B')
		    // Assume n=1
			} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_MAX_CARDINALITY) {
				
				OWLObjectMaxCardinality max = (OWLObjectMaxCardinality) superClass;
				OWLClass object = (OWLClass) max.getFiller();
				
				addFact(CKRRewritingVocabulary.SUP_LEQONE, //
						subClass.asOWLClass().getIRI(),//
						max.getProperty().asOWLObjectProperty().getIRI(),
						object.asOWLClass().getIRI(),
						contextID);
						
			// A subclass not(B)
			} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF) {

				OWLObjectComplementOf not = (OWLObjectComplementOf) superClass;
				IRI negatedClass = null;
				for (OWLClassExpression ce : not.getNestedClassExpressions()) {
					if (!ce.isAnonymous()) negatedClass = ce.asOWLClass().getIRI();
				}
				
				addFact(CKRRewritingVocabulary.SUP_NOT, //
						subClass.asOWLClass().getIRI(),//
						negatedClass,
						contextID);				

			// (A -> or(B, C)) ~>
			//if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
			//	throw new IllegalStateException();
			//}
			// A subclass and(B, C)
			//else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
			//	throw new IllegalStateException();
			//}
			// A subclass exists(R, {a})
			//else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
			//	OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) superClass;
			//	// datalog.getClauses().add(
			//	// new Clause(new Literal[] { new Literal( //
			//	// RewritingVocabulary.SUP_EX, //
			//	// CacheManager.getInstance().getConstant(
			//	// iriEncoder.encode(subClass.asOWLClass().getIRI())),//
			//	// CacheManager.getInstance().getConstant(
			//	// iriEncoder.encode(some.getProperty().asOWLObjectProperty().getIRI())),//
			//	// CacheManager.getInstance().getConstant(
			//	// iriEncoder.encode(some.getFiller().asOWLClass().getIRI())),//
			//	// CacheManager.getInstance().getConstant("e" +
			//	// superSomeAxiomEncoder.encode(axiom))//
			//	// ) }, //
			//	// new Literal[] {}));
			//	datalog.add(new Clause(new Literal[] { new Literal( //
			//			CKRRewritingVocabulary.SUP_EX, //
			//			CacheManager.getInstance().getConstant(
			//					subClass.asOWLClass().getIRI()),//
			//			CacheManager.getInstance().getConstant(
			//					some.getProperty().asOWLObjectProperty()
			//							.getIRI()),//
			//			CacheManager.getInstance().getConstant(
			//					some.getFiller().asOWLClass().getIRI()),//
			//			// CacheManager.getInstance().getConstant("e" +
			//			// superSomeAxiomEncoder.encode(axiom))//
			//			CacheManager.getInstance().getConstant(
			//					"e^{"
			//							+ subClass.asOWLClass().getIRI()
			//									.getFragment()
			//							+ "->" //
			//							+ some.getProperty()
			//									.asOWLObjectProperty().getIRI()
			//									.getFragment()
			//							+ "_SOME_" //
			//							+ some.getFiller().asOWLClass()
			//									.getIRI().getFragment() + "}"
			//			// superSomeAxiomEncoder.encode(axiom)
			//					)//
			//	) }, //
			//			new Literal[] {}));
	        //			
			//} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_ONE_OF) {
			//	// A ⊑ {c} -> SubClass(A, c)
			//	// OWLObjectOneOf oneOf = (OWLObjectOneOf) superClass;
			//	// int c =
			//	// iriEncoder.encode(oneOf.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI());
			//	// int a = iriEncoder.encode(subClass.asOWLClass().getIRI());
			//	// addFact(RewritingVocabulary.SUB_CLASS, a, c);
			//	OWLObjectOneOf oneOf = (OWLObjectOneOf) superClass;
			//	addFact(CKRRewritingVocabulary.SUB_CLASS, //
			//			subClass.asOWLClass().getIRI(),//
			//			oneOf.getIndividuals().iterator().next()
			//					.asOWLNamedIndividual().getIRI());
	        //
			//} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_MIN_CARDINALITY) {
			//	throw new IllegalStateException();	
			//} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_HAS_SELF) {
			//	// OWLObjectHasSelf self = (OWLObjectHasSelf) superClass;
			//	// int r = DReWELManager.getInstance().getIRIEncoder()
			//	// .encode(self.getProperty().asOWLObjectProperty().getIRI());
			//	// int a =
			//	// DReWELManager.getInstance().getIRIEncoder().encode(subClass.asOWLClass().getIRI());
			//	// addFact(RewritingVocabulary.SUP_SELF, a, r);
			//	OWLObjectHasSelf self = (OWLObjectHasSelf) superClass;
			//	addFact(CKRRewritingVocabulary.SUP_SELF, //
			//			subClass.asOWLClass().getIRI(), //
			//			self.getProperty().asOWLObjectProperty().getIRI());
			} else {
				throw new IllegalStateException();
			}
		}

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
		
		//CASE FOR EVAL: eval(R,C) \subs S
		IRI metaClass = null;
		IRI objectProperty = null; 
		for (OWLAnnotationAssertionAxiom a : axiom.getSubProperty().asOWLObjectProperty()
				.getAnnotationAssertionAxioms(ontology)) {
			if (a.getProperty().equals(hasEvalMeta)){
				metaClass = (IRI) a.getValue();
			} else if (a.getProperty().equals(hasEvalObject)) {
				objectProperty = (IRI) a.getValue();
			}
		}
		if ((metaClass != null) && (objectProperty != null)){
		    addFact(CKRRewritingVocabulary.SUB_EVAL_R,
		    		objectProperty, metaClass,//
					axiom.getSuperProperty().asOWLObjectProperty().getIRI(),
					contextID);				
		}
	}
			
	//- - SIGNATURE - - - - - - - - - - - - - - - - - - - - - - - -
	
}
//=======================================================================