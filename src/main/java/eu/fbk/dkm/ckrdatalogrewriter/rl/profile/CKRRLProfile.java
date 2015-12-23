package eu.fbk.dkm.ckrdatalogrewriter.rl.profile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.profiles.LastPropertyInChainNotInImposedRange;
import org.semanticweb.owlapi.profiles.OWL2RLProfile;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.profiles.UseOfAnonymousIndividual;
import org.semanticweb.owlapi.profiles.UseOfDataOneOfWithMultipleLiterals;
import org.semanticweb.owlapi.profiles.UseOfIllegalAxiom;
import org.semanticweb.owlapi.profiles.UseOfIllegalDataRange;
import org.semanticweb.owlapi.profiles.UseOfObjectOneOfWithMultipleIndividuals;
import org.semanticweb.owlapi.util.OWLObjectPropertyManager;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;

/**
 * @author Loris
 * @version 1.3 
 *
 * Implementation for the profile restricting OWL-RL to 
 * CKR related constructs.
 * (Extended from DreW EL profile). 
 */
public class CKRRLProfile implements OWLProfile {

	//--- FIELDS ---------------------------------------------
	
	//--- CONSTRUCTORS ---------------------------------------------

	//--- GET NAME ---------------------------------------------------
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.profiles.OWLProfile#getName()
	 */
	@Override
	public String getName() {
		return "CKR OWL RL Profile";
	}
	
	//--- PROFILES CHECK ---------------------------------------------
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.profiles.OWLProfile#checkOntology(org.semanticweb.owlapi.model.OWLOntology)
	 */
	@Override
	public OWLProfileReport checkOntology(OWLOntology ontology) {
	    
		//We extend the check from a RL profile
		OWL2RLProfile rlprofile = new OWL2RLProfile();
		
		//Check w.r.t. RL and get violations
		OWLProfileReport report = rlprofile.checkOntology(ontology);
		Set<OWLProfileViolation> violations = new HashSet<>();
		violations.addAll(report.getViolations());
		
		//Walk in the ontology and explore the violations
		//TODO: (add meta.rdf to the mapped IRIs in the ontology manager?)
		Set<OWLOntology> ontologySet = new HashSet<OWLOntology>();
		
		ontologySet.addAll(ontology.getImportsClosure()); 
		OWLOntologyWalker ontologyWalker = new OWLOntologyWalker(ontologySet);
		
		//Create new local visitor for CKR RL
		CKRRLProfileObjectVisitor visitor = new CKRRLProfileObjectVisitor(
				ontologyWalker, ontology.getOWLOntologyManager());
		
		ontologyWalker.walkStructure(visitor);
		violations.addAll(visitor.getProfileViolations());
		
		return new OWLProfileReport(this, violations);
	}
	
	//--- PROFILE OBJECT VISITOR CLASS --------------------------------

	private class CKRRLProfileObjectVisitor extends
					OWLOntologyWalkerVisitor<Object>{

		//--- FIELDS --------------------------------------------------
		
		private OWLOntologyManager man;
		private OWLObjectPropertyManager propertyManager;
		private Set<OWLProfileViolation> profileViolations;
		
		//--- CONSTRUCTOR --------------------------------------------------
		
		public CKRRLProfileObjectVisitor(OWLOntologyWalker walker, OWLOntologyManager man) {
			super(walker);
			this.man = man;
			profileViolations = new HashSet<>();
		}
		
		//--- GET METHODS --------------------------------------------------
		
		public Set<OWLProfileViolation> getProfileViolations() {
			return new HashSet<>(profileViolations);
		}
		
		private OWLObjectPropertyManager getPropertyManager() {
			if (propertyManager == null) {
				propertyManager = new OWLObjectPropertyManager(man,
						getCurrentOntology());
			}
			return propertyManager;
		}
		
		//--- VISIT METHODS --------------------------------------------------
		
		//TODO: in final version, manage also restricted datatypes?
//		@Override
//		public Object visit(OWLDatatype node) {
//			if (!allowedDatatypes.contains(node.getIRI())) {
//				profileViolations.add(new UseOfIllegalDataRange(
//						getCurrentOntology(), getCurrentAxiom(), node));
//			}
//			return null;
//		}
		
		/* All of the following (OWL-RL) axioms fall out the considered fragment */
		

//		@Override
//		//Disallow (a = b)
//		public Object visit(OWLSameIndividualAxiom axiom) {
//			profileViolations.add(new UseOfIllegalAxiom(
//					getCurrentOntology(), axiom));
//			return null;
//		}
		
//		@Override
//		//Disallow (a \neq b)
//		public Object visit(OWLDifferentIndividualsAxiom axiom) {
//			profileViolations.add(new UseOfIllegalAxiom(
//					getCurrentOntology(), axiom));
//			return null;		
//		}
		
		@Override
		//Disallow anonymous individuals
		public Object visit(OWLAnonymousIndividual individual) {
			profileViolations.add(new UseOfAnonymousIndividual(
					getCurrentOntology(), getCurrentAxiom(), individual));
			return null;
		}
		
		@Override
		//{a} only on singletons (object)
		public Object visit(OWLObjectOneOf desc) {
			if (desc.getIndividuals().size() != 1) {
				profileViolations
						.add(new UseOfObjectOneOfWithMultipleIndividuals(
								getCurrentOntology(), getCurrentAxiom(), desc));
			}
			return null;
		}
		
		@Override
		//{a} only on singletons (data)
		public Object visit(OWLDataOneOf node) {
			if (node.getValues().size() != 1) {
				profileViolations.add(new UseOfDataOneOfWithMultipleLiterals(
						getCurrentOntology(), getCurrentAxiom(), node));
			}
			return null;
		}		
		
//		@Override		
//		//Disallow C \or D (object)
//		public Object visit(OWLObjectUnionOf desc) {
//			profileViolations.add(new UseOfIllegalClassExpression(
//					getCurrentOntology(), getCurrentAxiom(), desc));
//			return null;
//		}
		
//		@Override
//		//Disallow C \or D (data)
//		public Object visit(OWLDataUnionOf node) {
//			profileViolations.add(new UseOfIllegalDataRange(
//					getCurrentOntology(), getCurrentAxiom(), node));
//			return null;
//		}
		
//		@Override
//		//Disallow \not C (object)
//		public Object visit(OWLObjectComplementOf desc) {
//			profileViolations.add(new UseOfIllegalClassExpression(
//					getCurrentOntology(), getCurrentAxiom(), desc));
//			return null;
//		}
		
//		@Override
//		//Disallow \not C (data)
//		public Object visit(OWLDataComplementOf node) {
//			profileViolations.add(new UseOfIllegalDataRange(
//					getCurrentOntology(), getCurrentAxiom(), node));
//			return null;
//		}
		
//		@Override
//		//Disallow Dis(C,D) 
//		public Object visit(OWLDisjointClassesAxiom axiom) {
//			profileViolations.add(new UseOfIllegalAxiom(
//					getCurrentOntology(), axiom));
//			return null;
//		}
		
//		@Override
//		//Disallow \forall R.C (object)
//		public Object visit(OWLObjectAllValuesFrom desc) {
//			profileViolations.add(new UseOfIllegalClassExpression(
//					getCurrentOntology(), getCurrentAxiom(), desc));
//			return null;
//		}
		
//		@Override
//		//Disallow \forall R.C (data)
//		public Object visit(OWLDataAllValuesFrom desc) {
//			profileViolations.add(new UseOfIllegalClassExpression(
//					getCurrentOntology(), getCurrentAxiom(), desc));
//			return null;
//		}

//		@Override
//		//Disallow \leq [0,1] R.C (object)
//		public Object visit(OWLObjectMaxCardinality desc) {
//			profileViolations.add(new UseOfIllegalClassExpression(
//					getCurrentOntology(), getCurrentAxiom(), desc));
//			return null;
//		}
				
//		@Override
//		//Disallow \leq [0,1] R.C (data)
//		public Object visit(OWLDataMaxCardinality desc) {
//			profileViolations.add(new UseOfIllegalClassExpression(
//					getCurrentOntology(), getCurrentAxiom(), desc));
//			return null;
//		}
		
//		@Override
//		//Disallow \not R(a,b) (data)
//		public Object visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
//			profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(),
//					axiom));
//			return null;
//		}

//		@Override
//		//Disallow \not R(a,b) (object)
//		public Object visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
//			profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(),
//					axiom));
//			return null;
//		}
	
//		@Override
//		//Disallow Dis(R,S) (data)
//		public Object visit(OWLDisjointDataPropertiesAxiom axiom) {
//			profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(),
//					axiom));
//			return null;
//		}

//		@Override
//		//Disallow Dis(R,S) (object)
//		public Object visit(OWLDisjointObjectPropertiesAxiom axiom) {
//			profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(),
//					axiom));
//			return null;
//		}
		
//		@Override
//		//Disallow Inv(R) (object)
//		public Object visit(OWLObjectInverseOf property) {
//			profileViolations.add(new UseOfObjectPropertyInverse(
//					getCurrentOntology(), getCurrentAxiom(), property));
//			return null;
//		}

//		@Override
//		//Disallow Inv(R,S) (object)
//		public Object visit(OWLInverseObjectPropertiesAxiom axiom) {
//			profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(),
//					axiom));
//			return null;
//		}
		
//		@Override
//		//Disallow Func(R) (object)
//		public Object visit(OWLFunctionalObjectPropertyAxiom axiom) {
//			profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(),
//					axiom));
//			return null;
//		}

//		@Override
//		//Disallow Func(R) (data)
//		public Object visit(OWLFunctionalDataPropertyAxiom axiom) {
//			profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(),
//					axiom));
//			return null;
//		}
		
//		@Override
//		//Disallow InvFunc(R,S) (object)
//		public Object visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
//			profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(),
//					axiom));
//			return null;
//		}

		@Override
		//Disallow Ref(R) (object)
		public Object visit(OWLReflexiveObjectPropertyAxiom axiom) {
			profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(),
					axiom));
			return null;
		}
		
//		@Override
//		//Disallow Irr(R) (object)
//		public Object visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
//			profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(),
//					axiom));
//			return null;
//		}

//		@Override
//		//Disallow Sym(R) (object)
//		public Object visit(OWLSymmetricObjectPropertyAxiom axiom) {
//			profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(),
//					axiom));
//			return null;
//		}
		
//		@Override
//		//Disallow Asym(R) (object)
//		public Object visit(OWLAsymmetricObjectPropertyAxiom axiom) {
//			profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(),
//					axiom));
//			return null;
//		}
		
		@Override
		//Limit chains to simple roles
		public Object visit(OWLSubPropertyChainOfAxiom axiom) {
			Set<OWLObjectPropertyRangeAxiom> rangeAxioms = getCurrentOntology()
					.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE, true);
			if (rangeAxioms.isEmpty()) {
				return false;
			}

			// Do we have a range restriction imposed on our super property?
			for (OWLObjectPropertyRangeAxiom rngAx : rangeAxioms) {
				if (getPropertyManager().isSubPropertyOf(
						axiom.getSuperProperty(), rngAx.getProperty())) {
					// Imposed range restriction!
					OWLClassExpression imposedRange = rngAx.getRange();
					// There must be an axiom that imposes a range on the last
					// prop in the chain
					List<OWLObjectPropertyExpression> chain = axiom
							.getPropertyChain();
					if (!chain.isEmpty()) {
						OWLObjectPropertyExpression lastProperty = chain
								.get(chain.size() - 1);
						boolean rngPresent = false;
						for (OWLOntology ont : getCurrentOntology()
								.getImportsClosure()) {
							for (OWLObjectPropertyRangeAxiom lastPropRngAx : ont
									.getObjectPropertyRangeAxioms(lastProperty)) {
								if (lastPropRngAx.getRange().equals(
										imposedRange)) {
									// We're o.k.
									rngPresent = true;
									break;
								}
							}
						}
						if (!rngPresent) {
							profileViolations
									.add(new LastPropertyInChainNotInImposedRange(
											getCurrentOntology(), axiom, rngAx));
						}
					}
				}
			}

			return null;
		}
		
		@Override
		//Disallow HasKey
		public Object visit(OWLHasKeyAxiom axiom) {
			profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(),
					axiom));
			return null;
		}
		
		//--------------------------------------------------------------------
		
//		@Override
//		public Object visit(OWLDataExactCardinality desc) {
//			profileViolations.add(new UseOfIllegalClassExpression(
//					getCurrentOntology(), getCurrentAxiom(), desc));
//			return null;
//		}
//
//		@Override
//		public Object visit(OWLDataMaxCardinality desc) {
//			profileViolations.add(new UseOfIllegalClassExpression(
//					getCurrentOntology(), getCurrentAxiom(), desc));
//			return null;
//		}
//
//		@Override
//		public Object visit(OWLObjectExactCardinality desc) {
//			profileViolations.add(new UseOfIllegalClassExpression(
//					getCurrentOntology(), getCurrentAxiom(), desc));
//			return null;
//		}

//		@Override
//		public Object visit(OWLObjectMinCardinality desc) {
//			profileViolations.add(new UseOfIllegalClassExpression(
//					getCurrentOntology(), getCurrentAxiom(), desc));
//			return null;
//		}
		
//		@Override
//		public Object visit(OWLDisjointUnionAxiom axiom) {
//			profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(),
//					axiom));
//			return null;
//		}

		@Override
		public Object visit(OWLDatatypeRestriction node) {
			profileViolations.add(new UseOfIllegalDataRange(
					getCurrentOntology(), getCurrentAxiom(), node));
			return null;
		}
		
		@Override
		public Object visit(SWRLRule rule) {
			profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(),
					rule));
			return super.visit(rule);
		}

		@Override
		public Object visit(OWLOntology ontology) {
			propertyManager = null;
			return null;
		}

		//---------------------------------------------------------------		
	}
		
}
//=======================================================================