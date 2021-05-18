<div class="well sidebar" id="well-home">
    <h1>
        CKR<em>ew</em>
    </h1>

    <p class='title2'>
        CKR datalog rewriter
    </p>

    <p class='title2'>
        <a class="btn btn-primary btn-large" href="installation.html">Download</a>
        <a class="btn btn-primary btn-large" href="demos.html">Examples</a>
        <a class="btn btn-primary btn-large" href="testsets.html">Testsets</a>
    </p>
</div>

---------------------------------------

### About

**CKRew** (CKR datalog rewriter) provides a datalog translation of OWL2-RL based 
[CKR](https://dkm.fbk.eu/technologies/ckr-contextualized-knowledge-repository) 
and supports reasoning with global defeasible axioms (or justifiable exceptions). 
CKRew is implemented as an extension of the DL to datalog rewriter [DReW](https://github.com/ghxiao/drew) 
and provides a command line utility for the translation of CKRs represented as RDF/TRIG files.

<!-- [learn more...](description.html) -->

### Features

- CKR expressible in [OWL2-RL](http://www.w3.org/TR/owl2-profiles/#OWL_2_RL)
- Option for defeasible KB in DL-Lite_R ([OWL2 QL](https://www.w3.org/TR/owl2-profiles/#OWL_2_QL))
- Input as [N3](http://www.w3.org/TeamSubmission/n3/) or [TRIG](http://www.w3.org/TR/trig/) files.
- Datalog translation based on [DReW](https://github.com/ghxiao/drew) rewriter.
- Permits expression of defeasible global axioms (OWL axioms annotated with `ckr:defeasible`).
- Available demo examples to show behaviour with non-monotonic axiom propagation. 
- Available testsets for scalability and defeasibility evaluation tests.

### News

- 2020-06-05: CKRew 1.5.1 package released
- 2020-05-04: CKRew 1.5 package released
- 2016-12-07: CKRew 1.4 package released
- 2015-12-14: CKRew 1.3 package released
- 2015-11-11: CKRew site launched
