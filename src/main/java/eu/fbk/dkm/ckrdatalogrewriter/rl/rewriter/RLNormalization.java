package eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

/**
 * @author Loris
 * @version 1.3
 * 
 * General class for normalization: contains the common elements for normalization in
 * RL for both global and local parts.
 */
class RLNormalization implements OWLAxiomVisitorEx<Object> {

	//--- FIELDS ---------------------------------------------

	protected OWLOntology ontology;
	protected OWLOntology normalizedOnt;
	
	//TODO: keep this information useful for translation in the Manager?
	protected OWLOntologyManager manager;
	protected OWLDataFactory factory;

	protected OWLAnnotationProperty hasEvalMeta;
	protected OWLAnnotationProperty hasEvalObject; 
	
	private OWLAnnotationProperty hasAxiomType;
	private OWLAnnotationValue defeasible;
	
	//private int freshClassCounter = 0;
	
	//--- CONSTRUCTORS ----------------------------------------------------------
	
	public RLNormalization() {}
	
	//--- NORMALIZATION ---------------------------------------------------------

	/**
	 * Normalizes the given ontology (with respect to RL normal form).
	 * 
	 * @param ontology ontology to be normalized
	 * @return normalized ontology
	 */
	public OWLOntology normalize(OWLOntology ontology) {
		
		//TODO: keep this information useful for translation in the Manager?
		this.ontology = ontology;
		this.manager = ontology.getOWLOntologyManager();
		this.factory = manager.getOWLDataFactory();
		
		this.hasEvalMeta = factory.getOWLAnnotationProperty(
				IRI.create("http://dkm.fbk.eu/ckr/meta#", "hasEvalMeta"));
		this.hasEvalObject = factory.getOWLAnnotationProperty(
				IRI.create("http://dkm.fbk.eu/ckr/meta#", "hasEvalObject"));
		
    	this.hasAxiomType = factory.getOWLAnnotationProperty(
				IRI.create("http://dkm.fbk.eu/ckr/meta#", "hasAxiomType"));
    	this.defeasible = IRI.create("http://dkm.fbk.eu/ckr/meta#", "defeasible");
		
		try {
			normalizedOnt = manager.createOntology();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		for (OWLAxiom axiom : ontology.getAxioms()) {
			
			OWLAxiom axiom_nnf = axiom.getNNF();
			
			//retrieve and add original annotation
			if (axiom.isAnnotated()) { 
				//System.out.println("Annotated axiom: " + axiom);
				//System.out.println(axiom_nnf);
				//if(isDefeasible(axiom)) System.out.println("Defeasible axiom: " + axiom.getAxiomWithoutAnnotations()); 
				
				axiom_nnf = axiom_nnf.getAnnotatedAxiom(axiom.getAnnotations());
				//System.out.println(axiom_nnf);
			}
			
			axiom_nnf.accept(this);
			//axiom.accept(this);
		}
		return normalizedOnt;
	}

	//--- GET FRESH CLASS METHODS -----------------------------------------
	
	/* not currently used in NF checking. */
	
	//private OWLClass getFreshClass() {
	//	freshClassCounter++;
	//	return factory.getOWLClass(IRI.create("http://www.example.org/fresh#"
	//			+ "fresh" + freshClassCounter));
	//}
    //
	//private OWLClass getFreshClass(String suffix) {
	//	freshClassCounter++;
	//	return factory.getOWLClass(IRI.create("http://www.example.org/fresh#"
	//			+ suffix + "fresh" + freshClassCounter));
	//}	

	//--- IS DEFEASIBLE -----------------------------------------

	/**
	 * Verifies if the input axiom is declared as defeasible.
	 * 
	 * @param a the axiom to be verified
	 */
	public boolean isDefeasible(OWLAxiom a){
		for (OWLAnnotation ann : a.getAnnotations(hasAxiomType)) {
			if (ann.getValue().equals(defeasible))
				return true;
		}
		return false;
	}
	
	//--- VISIT METHODS --------------------------------------------------
	
	@Override
	//Restrict Dis(A,C)
	//TODO: NORMALIZE
	public Object visit(OWLDisjointClassesAxiom axiom) {
		System.err.println(axiom + " not allowed in normal form.");
		throw new IllegalStateException();
		
		//for (OWLClassExpression cls1 : axiom.getClassExpressions()) {
		//	for (OWLClassExpression cls2 : axiom.getClassExpressions()) {
		//		if (!cls1.equals(cls2)) {
		//			factory.getOWLSubClassOfAxiom(
		//					factory.getOWLObjectIntersectionOf(cls1, cls2),
		//					factory.getOWLNothing())//
		//					.accept(this);
		//		}
        //
		//	}
		//}
		//return null;
	}
	
	@Override
	//Add domain axiom (data)
	public Object visit(OWLDataPropertyDomainAxiom axiom) {
		manager.addAxiom(normalizedOnt, axiom);
		return null;

		// throw new IllegalStateException();

		// factory.getOWLSubClassOfAxiom(factory.getOWLDataSomeValuesFrom(axiom.getProperty(),
		// factory.getTopDatatype()),
		// axiom.getDomain()) //
		// .accept(this);

		// return null;
	}
	
	@Override
	//Add domain axiom (object)
	public Object visit(OWLObjectPropertyDomainAxiom axiom) {
		manager.addAxiom(normalizedOnt, axiom);
		// factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(axiom.getProperty(),
		// factory.getOWLThing()),
		// axiom.getDomain()) //
		// .accept(this);
		return null;
	}	

	@Override
	//Add range axiom (object)
	public Object visit(OWLObjectPropertyRangeAxiom axiom) {
		manager.addAxiom(normalizedOnt, axiom);
		// factory.getOWLSubClassOfAxiom(factory.getOWLThing(),
		// factory.getOWLObjectAllValuesFrom(axiom.getProperty(),
		// axiom.getRange())) //
		// .accept(this);
		return null;
	}
	
	@Override
	//Add range axiom (data)
	public Object visit(OWLDataPropertyRangeAxiom axiom) {
		manager.addAxiom(normalizedOnt, axiom);
		return null;
	}
	
	@Override
	//TODO: NORMALIZE + DEFEASIBLE
	public Object visit(OWLFunctionalObjectPropertyAxiom axiom) {
		System.err.println(axiom + " not allowed in normal form.");
		throw new IllegalStateException();
		//OWLObjectPropertyExpression property = axiom.getProperty();
		//factory.getOWLSubClassOfAxiom(factory.getOWLThing(),
		//		factory.getOWLObjectMaxCardinality(1, property))//
		//		.accept(this);
		//return null;
	}

	@Override
	//Add declarations
	public Object visit(OWLDeclarationAxiom axiom) {
		manager.addAxiom(normalizedOnt, axiom);
		return null;
	}

	@Override
	//Add annotation assertions
	public Object visit(OWLAnnotationAssertionAxiom axiom) {
		manager.addAxiom(normalizedOnt, axiom);
		return null;
	}
	
	@Override
	//TODO: NORMALIZE + DEFEASIBLE
	//Restrict Sym(R)
	public Object visit(OWLSymmetricObjectPropertyAxiom axiom) {
		System.err.println(axiom + " not allowed in normal form.");
		throw new IllegalStateException();
		//manager.addAxiom(normalizedOnt, axiom);
		//return null;
	}
	
	@Override
	//TODO: NORMALIZE + DEFEASIBLE
	//Restrict Trans(R)
	public Object visit(OWLTransitiveObjectPropertyAxiom axiom) {
		System.err.println(axiom + " not allowed in normal form.");
		throw new IllegalStateException();
		
		//OWLObjectPropertyExpression property = axiom.getProperty();
        //
		//OWLSubPropertyChainOfAxiom ax = factory.getOWLSubPropertyChainOfAxiom(
		//		Arrays.asList(property, property), property);
		//manager.addAxiom(normalizedOnt, ax);
		//return null;
	}
	
	@Override
	//Add Irr(R)
	//TODO: NORMALIZE + DEFEASIBLE
	public Object visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		manager.addAxiom(normalizedOnt, axiom);
		return null;
	}

	@Override
	//TODO: NORMALIZE + DEFEASIBLE
	//Restrict Asym(R) 
	public Object visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		System.err.println(axiom + " not allowed in normal form.");
		throw new IllegalStateException();		
	}
	
	@Override
	//Restrict E1 \equiv E2
	//TODO: NORMALIZE
	public Object visit(OWLEquivalentClassesAxiom axiom) {
		System.err.println(axiom + " not allowed in normal form.");
		throw new IllegalStateException();

		//Set<OWLClassExpression> classes = axiom.getClassExpressions();
        //
		//for (OWLClassExpression cls1 : classes) {
		//	for (OWLClassExpression cls2 : classes) {
		//		if (cls1.equals(cls2))
		//			continue;
		//		factory.getOWLSubClassOfAxiom(cls1, cls2).accept(this);
		//	}
		//}
		//return null;
	}
	
	@Override
	//Add R(a,b) (data)
	public Object visit(OWLDataPropertyAssertionAxiom axiom) {
		manager.addAxiom(normalizedOnt, axiom);
		return null;
	}

	@Override
	//Add \non R(a,b) (data)
	public Object visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		manager.addAxiom(normalizedOnt, axiom);
		return null;
	}
	
	@Override
	//Disallow R \subs S on datatype
	public Object visit(OWLSubDataPropertyOfAxiom axiom) {
		//System.err.println("ignore " + axiom);
		//return null;
		System.err.println(axiom + " not allowed in normal form.");
		throw new IllegalStateException();
	}

	@Override
	//Restrict InvFunc(R)
	//TODO: NORMALIZE (using Inv(R,R)) + DEFEASIBLE
	public Object visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		//OWLObjectPropertyExpression property = axiom.getProperty();
		//factory.getOWLSubClassOfAxiom(
		//		factory.getOWLThing(),
		//		factory.getOWLObjectMaxCardinality(1,
		//				property.getInverseProperty()))//
		//		.accept(this);
		//return null;
		System.err.println(axiom + " not allowed in normal form.");
		throw new IllegalStateException();	
	}
	
	@Override
	//TODO: NORMALIZE + DEFEASIBLE
	//Add Inv(R,S)
	public Object visit(OWLInverseObjectPropertiesAxiom axiom) {
		manager.addAxiom(normalizedOnt, axiom);
		return null;
		//System.err.println(axiom + " not allowed in normal form.");
		//throw new IllegalStateException();
	}
	
	@Override
	//Add (a \neq b)
	//TODO: Restrict to N=2!
	public Object visit(OWLDifferentIndividualsAxiom axiom) {
		// do nothing because we already assumed UNA
		//return null;
		manager.addAxiom(normalizedOnt, axiom);
		return null;
	}

	@Override
	//Add (a = b)
	//TODO: Restrict to N=2!
	public Object visit(OWLSameIndividualAxiom axiom) {
		manager.addAxiom(normalizedOnt, axiom);
		return null;
	}
	
	@Override
	//Add Dis(R,S) (data)
	public Object visit(OWLDisjointDataPropertiesAxiom axiom) {
		manager.addAxiom(normalizedOnt, axiom);		
		return null;
	}    

	//---------------------------------------------------------------------
	
	/* All the following axioms are treated differently in global and local normalization */
	
	@Override
	//Restrict R(a,b)
	public Object visit(OWLObjectPropertyAssertionAxiom axiom) {
		return null;
	}
	
	@Override
	//Restrict \non R(a,b) 
	public Object visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		return null;
	}
	
	@Override
	//Restrict C(a)
	public Object visit(OWLClassAssertionAxiom axiom) {
		return null;
	}	
	
	@Override
	public Object visit(OWLSubClassOfAxiom arg0) {
		return null;
	}

	@Override
	public Object visit(OWLDisjointObjectPropertiesAxiom arg0) {
		return null;
	}

	@Override
	public Object visit(OWLSubObjectPropertyOfAxiom arg0) {
		return null;
	}

	@Override
	public Object visit(OWLSubPropertyChainOfAxiom arg0) {
		return null;
	}
	
	//---------------------------------------------------------------------
	
	/* All the following axioms fall out the fragment and are disallowed */
	
	@Override
	public Object visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		throw new IllegalArgumentException(axiom.toString());
	}

	@Override
	public Object visit(OWLAnnotationPropertyDomainAxiom axiom) {
		throw new IllegalArgumentException(axiom.toString());
	}

	@Override
	public Object visit(OWLAnnotationPropertyRangeAxiom axiom) {
		throw new IllegalArgumentException(axiom.toString());
	}

	@Override
	public Object visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		throw new IllegalArgumentException(axiom.toString());
	}

	@Override
	public Object visit(OWLDisjointUnionAxiom axiom) {
		throw new IllegalArgumentException(axiom.toString());
	}

	@Override
	public Object visit(OWLReflexiveObjectPropertyAxiom axiom) {
		throw new IllegalArgumentException(axiom.toString());
	}

	@Override
	public Object visit(OWLFunctionalDataPropertyAxiom axiom) {
		throw new IllegalArgumentException(axiom.toString());
	}

	@Override
	public Object visit(OWLEquivalentDataPropertiesAxiom axiom) {
		throw new IllegalArgumentException(axiom.toString());
	}

	@Override
	public Object visit(OWLHasKeyAxiom axiom) {
		throw new IllegalArgumentException(axiom.toString());
	}

	@Override
	public Object visit(OWLDatatypeDefinitionAxiom axiom) {
		throw new IllegalArgumentException(axiom.toString());
	}

	@Override
	public Object visit(SWRLRule rule) {
		throw new IllegalArgumentException(rule.toString());
	}

	//---------------------------------------------------------------------
}
//=======================================================================