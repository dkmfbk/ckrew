================================================================================
      CKRew: CKR datalog rewriter - DEMO README
================================================================================

= INTRODUCTION = 
The files presented in this demo represent an implementation for examples
introduced in recent CKR related publications [1 - 6]. Their goal is 
to demonstrate the functionality of the CKR rewriter prototype and provide 
an intuitive interpretation for its output.

The five examples included in this demo correspond to the .bat (and .sh) files
contained in this folder.
By running one of these shell files, the prototype is executed using the 
corresponding RDF files in the "../testcase" folder and the output program is 
saved to "output.dlv".

For each example, a query demonstrating the result of the translation is 
contained in one of the .dlv files in the "demo" folder.
Example queries over the output program can be run (assuming that a local copy 
of DLV is installed in "../localdlv") by executing:

../localdlv/dlv output.dlv -cautious queryname.dlv

Example 6 on multi-relational CKRs from [6] requires the installation of 
asprin (ver.3.x). For details on installing and using asprin, we refer 
to: https://potassco.org/asprin/

In the following, for each of the examples, we give a brief description for 
the represented scenario and an intuitive explanation for the query results.
For an extended and formal definition of each example, please consider the 
mentioned references.

--------------------------------------------------------------------------------
= EXAMPLE 1: FOOTBALL CUP WINNER =

Reference: [1]
Shell file: eswc12-demo
Query file: winnerQuery.dlv

This example shows a simple case of knowledge propagation across contexts.
The scenario is that of football competitions and their rules for qualification 
across them.
We consider the case of Club World Cup 2011 and Champions League 2011. The two 
competitions are represented as two distinct contexts.
In Club World Cup 2011, we state that :barcelona was the :Winner team.
In the Champions League context, we define that its ":Winner is a :UEFATeam 
that participated as :Team in this year Club World Cup". This can be easily 
realized using eval expressions that import teams from :UEFAClubFootball class 
of events and teams from Club World Cup 2011 and then considers their 
intersection.

The query contained in winnerQuery.dlv asks for the :Winner in the Champions 
League context. It can be checked that :barcelona is returned as the result, 
since (by local inference in the context of Club World Cup 2011) it is 
recognized as a Club World Cup participant and it is asserted that it is 
an :UEFATeam (in the knowledge module associated to the :UEFAClubFootball 
context class). Eval axioms permit to propagate such knowledge to the context of 
Champions League 2011, where such team is then classified as :Winner.

--------------------------------------------------------------------------------
= EXAMPLE 2: TOURISTS PREFERRED TEAMS =

Reference: [2, 4]
Shell file: tourism-demo
Query file: preferredTeamQuery.dlv

This example shows the mechanism of knowledge organization in modules and
propagation across contexts by eval axioms.
Domain of interest of this example is recommendation of touristic events.
In particular, we concentrate on :SportsEvents (precisely :VolleyMatches) and 
the interests of :SportiveTourists: in this case, contexts are used to represent
the situation of a single volley match and the local set of preferences of 
sportive tourists.
In the modules representing information about single matches we store facts such 
as :Winner and :Loser teams. In modules associated to tourists, we store their 
:PreferredTeams.
Moreover, using eval expressions, in the module associated to the context class
of all :SportiveTourists we state that their ":PreferredTeams include the 
:TopTeams from all :SportiveEvents". In turn, :TopTeams are defined in the 
module associated to :SportiveEvents as ":Winners from :TopMatches" (defined in 
the global context as those volley matches that are played inside a 
:VolleyA1Competition).

The query contained in preferredTeamQuery.dlv asks for the :PreferredTeams in 
the context of a particular :SportiveTourist, :volley_fan_01. 
In the module associated to the tourist, only :andreoli_latina_volley
is explicitly asserted as :PreferredTeam: however, by the above relations 
defined for all :SportiveTourists, also the two :TopTeams 
"itas_diatec_trentino_volley" and "casa_modena_volley" are returned.

--------------------------------------------------------------------------------
= EXAMPLE 3: INTERESTING TOURISM EVENTS =

Reference: [3, 4]
Shell file: tourism-example-d
Query file: interestingQuery.dlv

This example presents a first demonstration for defeasible inheritance of global
axioms.
The presented scenario is similar to that of previous example, as we reason 
about tourists preferences about events.
In this case, under the global context we only have a single context, 
representing one :cultural_tourist.
In the global knowledge we state, using a default axiom that "by default
all of the :Cheap events are :Interesting". Moreover, we assert that a local 
market (:market) and a football match (:fbmatch) are both :Cheap events.
In the module of the local context for the :cultural_tourist, however, we 
explicitly assert that for him "the football match is not :Interesting".

The query in interestingQuery.dlv asks for all :Interesting events in the 
local context of the :cultural_tourist.
By the interpretation of defeasible global axioms, we have that the inclusion of 
:fbmatch in :Interesting events is disregarded at this local context: the local 
negation basically proves the overriding of the defeasible axiom for the 
:fbmatch instance.
However, since we have no evidence of the contrary, the axiom can be 
instantiated for the :market instance, which is in fact returned as an 
:Interesting event for the tourist.

--------------------------------------------------------------------------------
= EXAMPLE 4: CURRENT WORKERS =

Reference: [3, 4]
Shell file: workers-example-d
Query file: workingNowQuery.dlv

Goal of this example is to present the interaction of defeasible axioms with
eval expression in order to represent a form of defeasible knowledge 
propagation.
In this example we want to represent information about employees in an 
organization, where local contexts describe the situation in a single year.
At the global context, we express the defeasible axiom that in a given year 
"in general, all of the employees :WorkingBefore (i.e. the year before) are 
also :WorkingNow".
In the context associated to year 2012, we assert as :WorkingNow three 
employees: :alice, :bob and :charlie.
In the context representing year 2013, we import (using an eval expression) all 
of the employees that were working in 2012 as :WorkingBefore. However, we also 
assert that :charlie is no more :WorkingNow for the organization.

The query in workingNowQuery.dlv asks for employees :WorkingNow in the 
context of year 2013.
Following from the eval expression, :alice, :bob and :charlie are imported as 
:WorkingBefore. However, only :alice and :bob are returned as :WorkingNow: 
the local negative assertion about :charlie overrides the instance of the 
defeasible inclusion for this individual.

--------------------------------------------------------------------------------
= EXAMPLE 5: DEPARTMENT MEMBERS =

Reference: [5]
Shell file: department-example-d
Query file: hasCourseQuery.dlv

In this example we demonstrate the option for rewriting DLliteR defeasible
knowledge bases (DKBs) and the possibility of reasoning on defeasible 
existential axioms.
We consider the scenario of the organization of an university research
department: using a defeasible axiom, we specify that "in general, department
members need to teach at least a course". This is represented as an axiom of
the kind D(:DeptMember \isa \exists :hasCourse).
Moreover, each :Professor and each :PhDStudent is a :DeptMember. 
However, a :PhDStudent can not hold a course, i.e. we have a strict axiom
:PhDStudent \isa \not \exists :hasCourse.
In the example DKB we have two members: :alice is a :Professor and :bob is a
:PhDStudent.

The query in hasCourseQuery.dlv asks for members for which it is known that
they teach at least a course.
By the interpretation of the defeasible axiom, we have that the axiom is 
disregarded in the case of :bob. This is due to the fact that the strict
axiom for :PhDStudent allows to derive a justification for the overriding 
of the :bob instance. On the other hand, the axiom is applied to the instance
of :alice, since no overriding can be derived: thus :alice is the only
instance that is returned by the query.

--------------------------------------------------------------------------------
= EXAMPLE 6: CORPORATE ORGANIZATION =

Reference: [6]
Shell file: mr-ckr-example-d
Auxiliary files: pref.dlv, mrExampleAux.dlv

This example demonstrates the use of the option for multi-relational simple CKRs
and the interpretation of overriding preference for time and coverage context 
hierarchies.
The input sCKR describes the organization of a corporation and the evolution
of corporate rules over time. By coverage hierarchy, we assert that the 
corporation has rules at the level :world, :branch and :local. The three 
contexts representing the rule levels are represented in 3 "time slices": over 
the time relation, we have versions of the contexts for 2019, 2020 and 2021. 
Their organization (and association to knowledge modules) is defined in the 
file global.n3.

Local knowledge modules (contained in the corresponding .n3 files) define the 
(possibly defeasible) corporate rules adopted in a particular organization level 
and time. In particular:
- in :world_2019, we assert that each :Supervisor can be either assigned to the 
  :Robotics or :Electronics areas. By default, every :Supervisor is assigned 
  to :Electronics.
- in :branch_2019, we declare that, by default, each :Supervisor has to work
  :OnSite (and work :OnSite and :OnRemote are disjoint).
- in :branch_2020, we update the previous rules: by default, each :Supervisor
  under this branch need to work :OnRemote and in the area of :Robotics.
- in :local_2019, the instance :bob of :Supervisor is declared.

Intuitively, we expect that the assignment of :bob to different areas and
working place in contexts :local_2019, :local_2020 and :local_2021 is decided
by the most specific and recent rules.

By executing CKRew on the input files and running an ASP solver (like DLV or 
clingo) on the resulting output.dlv file, we obtain 8 answer sets corresponding 
to the possible combinations of assignments for :bob in the different contexts.
In order to enforce the intended preference on overridings with respect to the 
time and coverage hierarchies (and filter the results), we can call asprin with:

  asprin  output.dlv  pref.dlv  mrExampleAux.dlv -q 1 -Wno-atom-undefined

File pref.dlv contains the definition of the asprin preference, while file
mrExampleAux.dlv contains directives and auxiliary predicates "worksAt" and
"hasArea" to filter and better visualize the results.

asprin returns a single preferred model: as expected, in 2019 :bob works 
:OnSite, while in 2020 and 2021 :bob works :OnRemote. Moreover, in 2019 :bob
is assigned to :Electronics using the general corporation rule, but in 2020 :bob
is assigned to :Robotics by the updated branch rule. In 2021, no updates are
given to this rule, thus :bob still works on :Robotics.
  
--------------------------------------------------------------------------------
= REFERENCES =

[1] Bozzato, L., Corcoglioniti, F., Homola, M., Joseph, M., Serafini, L.: 
    Managing Contextualized Knowledge with the CKR. 
    Poster in: ESWC2012 (2012)

[2] Bozzato, L., Serafini, L.: 
    Materialization Calculus for Contexts in the Semantic Web. 
    In: DL2013. CEUR-WP, CEUR-WS.org (2013)

[3] Bozzato, L., Eiter, T., Serafini, L.: 
    Defeasibility in contextual reasoning with CKR. 
    In: ARCOE-13 (2013)

[4] Bozzato, L., Eiter, T., Serafini, L.: 
    Enhancing context knowledge repositories with justifiable exceptions.
    In: Artificial Intelligence 257:72-126 (2018)

[5] Bozzato, L., Eiter, T., Serafini, L.: 
    Reasoning on DL-Lite_R with Defeasibility in ASP.
    In: RuleML+RR 2019 (2019)

[6] Bozzato, L., Keisel R., Eiter, T.:
    Reasoning on Multi-Relational Contextual Hierarchies
    via Answer Set Programming with Algebraic Measures.

================================================================================