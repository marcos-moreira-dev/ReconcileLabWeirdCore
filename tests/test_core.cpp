#include "../src/model/ProblemInstance.h"
#include "../src/engine/SearchEngine.h"
#include "../src/io/CaseFile.h"
#include "../src/io/MoneyText.h"

#include <iostream>
#include <cstdio>

static int failures = 0;

/**
 * Ayudante minimo de pruebas, deliberadamente sin framework para el toolchain de XP.
 */
static void check(bool condition, const char* name)
{
    if (condition)
    {
        std::cout << "PASS  " << name << "\n";
    }
    else
    {
        std::cout << "FAIL  " << name << "\n";
        ++failures;
    }
}

static void testMoneyParser()
{
    int cents = 0;
    std::string error;

    check(MoneyText::ParseCents("287.40", cents, error) && cents == 28740,
          "parser monetario acepta punto decimal");

    check(MoneyText::ParseCents("287,40", cents, error) && cents == 28740,
          "parser monetario acepta coma decimal");

    check(!MoneyText::ParseCents("-1.00", cents, error),
          "parser monetario rechaza montos negativos");

    check(!MoneyText::ParseCents("12.345", cents, error),
          "parser monetario rechaza exceso de decimales");
}

static void testSearchAndSolutions()
{
    ProblemInstance p;
    p.name = "multiple";
    p.targetCents = 100;
    p.objective = OBJECTIVE_ALL;

    p.valuesCents.push_back(20);
    p.valuesCents.push_back(30);
    p.valuesCents.push_back(50);
    p.valuesCents.push_back(70);

    SearchEngine engine;
    engine.Reset(p);
    engine.SetStrategy(STRATEGY_PRUNING);

    while (!engine.IsComplete())
        engine.RunBatch(100);

    ExecutionSnapshot snapshot = engine.Snapshot();

    check(snapshot.completed, "el motor completa la ejecucion");
    check(snapshot.matches >= 2, "el motor encuentra varias combinaciones compatibles");
    check(snapshot.solutions.size() >= 2, "el motor conserva selecciones compatibles");
    check(snapshot.visited > 0, "el motor registra estados visitados");
    check(snapshot.potentialStates == 31,
          "cuatro candidatos exponen un arbol completo de 31 estados");
    check(snapshot.visited + snapshot.avoidedStates == snapshot.potentialStates,
          "la busqueda completa contabiliza todo el arbol potencial");
    check(!snapshot.events.empty(), "el motor conserva eventos significativos");
}

static void testPruningPreservesAnswer()
{
    ProblemInstance p;
    p.name = "poda";
    p.targetCents = 100;
    p.objective = OBJECTIVE_ALL;

    p.valuesCents.push_back(10);
    p.valuesCents.push_back(20);
    p.valuesCents.push_back(30);
    p.valuesCents.push_back(40);
    p.valuesCents.push_back(90);

    SearchEngine exhaustive;
    exhaustive.Reset(p);
    exhaustive.SetStrategy(STRATEGY_EXHAUSTIVE);
    while (!exhaustive.IsComplete())
        exhaustive.RunBatch(1000);

    SearchEngine pruned;
    pruned.Reset(p);
    pruned.SetStrategy(STRATEGY_PRUNING);
    while (!pruned.IsComplete())
        pruned.RunBatch(1000);

    ExecutionSnapshot a = exhaustive.Snapshot();
    ExecutionSnapshot b = pruned.Snapshot();

    check(a.matches == b.matches,
          "la poda con valores positivos conserva el numero de compatibles");

    check(b.visited <= a.visited,
          "la poda no visita mas estados que la busqueda exhaustiva");

    check(a.visited + a.avoidedStates == a.potentialStates,
          "la busqueda exhaustiva contabiliza los estados potenciales");

    check(b.visited + b.avoidedStates == b.potentialStates,
          "la busqueda con poda contabiliza visitados mas evitados");
}


static void testRepositoryExamples()
{
    const char* files[] =
    {
        "examples/01-small-exact.case",
        "examples/02-multiple-solutions.case",
        "examples/03-no-solution.case",
        "examples/04-pruning-showcase.case",
        "examples/05-medium-reconciliation.case",
        "examples/06-stress.case",
        "examples/07-horizontal-compact.case"
    };

    for (int i = 0; i < 7; ++i)
    {
        ProblemInstance problem;
        std::string error;
        bool loaded = CaseFile::Load(files[i], problem, error);

        std::string name = "carga el ejemplo del repositorio: ";
        name += files[i];
        check(loaded, name.c_str());
    }

    ProblemInstance multiple;
    std::string error;
    bool loadedMultiple =
        CaseFile::Load("examples/02-multiple-solutions.case", multiple, error);

    if (loadedMultiple)
    {
        SearchEngine engine;
        engine.Reset(multiple);
        engine.SetStrategy(STRATEGY_PRUNING);

        while (!engine.IsComplete())
            engine.RunBatch(1000);

        check(engine.Snapshot().matches > 1,
              "el ejemplo multiple realmente tiene varias coincidencias");
    }

    ProblemInstance none;
    bool loadedNone =
        CaseFile::Load("examples/03-no-solution.case", none, error);

    if (loadedNone)
    {
        SearchEngine engine;
        engine.Reset(none);
        engine.SetStrategy(STRATEGY_PRUNING);

        while (!engine.IsComplete())
            engine.RunBatch(1000);

        check(engine.Snapshot().matches == 0,
              "el ejemplo sin solucion realmente no tiene coincidencias");
    }

    ProblemInstance horizontal;
    bool loadedHorizontal =
        CaseFile::Load(
            "examples/07-horizontal-compact.case",
            horizontal,
            error);

    if (loadedHorizontal)
    {
        SearchEngine engine;
        engine.Reset(horizontal);
        engine.SetStrategy(STRATEGY_PRUNING);

        while (!engine.IsComplete())
            engine.RunBatch(1000);

        ExecutionSnapshot snapshot =
            engine.Snapshot();

        check(
            snapshot.visited >= 80,
            "el ejemplo horizontal genera una traza suficientemente ancha");

        check(
            snapshot.matches >= 2,
            "el ejemplo horizontal conserva varias coincidencias");
    }
}

static void testCaseRoundTrip()
{
    ProblemInstance original;
    original.name = "ida y vuelta";
    original.description = "prueba";
    original.targetCents = 12345;
    original.objective = OBJECTIVE_COUNT;
    original.valuesCents.push_back(1200);
    original.valuesCents.push_back(345);
    original.valuesCents.push_back(10800);

    std::string error;
    const char* path = ".local\\test-roundtrip.case";

    bool saved = CaseFile::Save(path, original, error);
    check(saved, "el archivo de caso se guarda");

    ProblemInstance loaded;
    bool read = saved && CaseFile::Load(path, loaded, error);
    check(read, "el archivo de caso se carga despues de guardar");

    if (read)
    {
        check(loaded.targetCents == original.targetCents,
              "el round-trip conserva el monto objetivo");
        check(loaded.valuesCents.size() == original.valuesCents.size(),
              "el round-trip conserva la cantidad de candidatos");
        check(loaded.objective == original.objective,
              "el round-trip conserva el objetivo");
    }

    std::remove(path);
}

int main()
{
    testMoneyParser();
    testSearchAndSolutions();
    testPruningPreservesAnswer();
    testRepositoryExamples();
    testCaseRoundTrip();

    std::cout << "\nRESULTADO: "
              << (failures == 0 ? "PASS" : "FAIL")
              << "\n";

    return failures == 0 ? 0 : 1;
}
