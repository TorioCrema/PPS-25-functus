# Testing

## Tecnologie utilizzate

Per l'implementazione dei test è stata utilizzata la libreria `scalatest`.
Questa libreria ha permesso di creare classi di test sintetiche e leggibili, soprattutto
grazie all'utilizzo di `Matchers`.

## Metodologia

I test sono stati realizzati tramite l'approccio Test Driven Development (TDD).

Per assicurare che le implementazioni siano testate sono state utilizzate le seguenti tecniche:
- PR rules: una regola applicata alla `main` branch che permette di effettuare commit solo attraverso pull request sulle
  quali sono passati i test effettuati tramite github actions.
- Coverage: attraverso il plugin `scoverage` è possibile generare un report in cui sono presenti le percentuali di
  coverage di ogni sorgente.

## Risultati di coverage

Durante lo sviluppo sono stati implementati test i sorgenti di tutti i moduli ponendo maggiore
attenzione ai risultati di coverage per il package `model`, questi risultati sono osservabili
in questo [coverage report](org.pps.functus.model.html).

![model coverage](img/model_coverage.jpeg)

---

1. [Processo di sviluppo](processo.md)
    1. [Sprint 1](process/sprint01.md)
    2. [Sprint 2](process/sprint02.md)
    3. [Sprint 3](process/sprint03.md)
2. [Requisiti](requisiti.md)
3. [Architettura](architettura.md)
4. [Design di dettaglio](design-di-dettaglio.md)
5. [Implementazione](implementazione.md)
6. [Testing](testing.md)
7. [**Retrospettiva (prossimo)**](retrospettiva.md)