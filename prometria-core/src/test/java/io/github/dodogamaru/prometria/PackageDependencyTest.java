package io.github.dodogamaru.prometria;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Guards the package dependency direction of {@code prometria-core}.
 *
 * <p>Layering contract (a lower layer must never reference a higher one):
 * <ul>
 *   <li>L0 leaf: {@code model}, {@code exception} — depend on nothing inside Prometria</li>
 *   <li>L1 transport: {@code client} — may only use L0</li>
 *   <li>L2 mechanism: {@code api}, {@code query}, {@code repository} — may use L0/L1,
 *       plus {@code repository -> query}; no other lateral edges</li>
 *   <li>L4 composition root: {@code spring} (separate module) — may use everything</li>
 * </ul>
 *
 * @author Daehwan Baek
 */
@AnalyzeClasses(packages = "io.github.dodogamaru.prometria", importOptions = DoNotIncludeTests.class)
class PackageDependencyTest {

    /**
     * No dependency cycle between the top-level packages.
     */
    @ArchTest
    static final ArchRule no_cycles_between_top_level_packages =
            slices()
                    .matching("io.github.dodogamaru.prometria.(*)..")
                    .should().beFreeOfCycles();
    private static final String MODEL = "io.github.dodogamaru.prometria.model..";
    private static final String EXCEPTION = "io.github.dodogamaru.prometria.exception..";
    private static final String CLIENT = "io.github.dodogamaru.prometria.client..";
    private static final String API = "io.github.dodogamaru.prometria.api..";
    private static final String QUERY = "io.github.dodogamaru.prometria.query..";
    private static final String REPOSITORY = "io.github.dodogamaru.prometria.repository..";
    private static final String SPRING = "io.github.dodogamaru.prometria.spring..";
    /**
     * Leaf packages (model, exception) must not depend on any other Prometria package.
     */
    @ArchTest
    static final ArchRule leaf_packages_must_not_depend_on_upper_layers =
            noClasses().that().resideInAnyPackage(MODEL, EXCEPTION)
                    .should().dependOnClassesThat().resideInAnyPackage(CLIENT, API, QUERY, REPOSITORY, SPRING);
    /**
     * The transport layer must stay ignorant of the mechanism layers above it.
     */
    @ArchTest
    static final ArchRule client_must_not_depend_on_mechanism_layers =
            noClasses().that().resideInAPackage(CLIENT)
                    .should().dependOnClassesThat().resideInAnyPackage(API, QUERY, REPOSITORY, SPRING);
    /**
     * The direct-use API layer must not use the query handler chain or the repository proxy.
     */
    @ArchTest
    static final ArchRule api_must_not_depend_on_query_or_repository =
            noClasses().that().resideInAPackage(API)
                    .should().dependOnClassesThat().resideInAnyPackage(QUERY, REPOSITORY, SPRING);
    /**
     * The query handler chain must not depend on the repository proxy, the API layer, or the root.
     */
    @ArchTest
    static final ArchRule query_must_not_depend_on_api_repository_or_spring =
            noClasses().that().resideInAPackage(QUERY)
                    .should().dependOnClassesThat().resideInAnyPackage(API, REPOSITORY, SPRING);
    /**
     * The repository proxy stays transport-agnostic (no direct client use) and must not
     * reach the API layer or the composition root.
     */
    @ArchTest
    static final ArchRule repository_must_not_depend_on_api_client_or_spring =
            noClasses().that().resideInAPackage(REPOSITORY)
                    .should().dependOnClassesThat().resideInAnyPackage(API, CLIENT, SPRING);
}
