# Design di dettaglio

## Model

Gli elementi di model sono organizzati per composizione, l'entità piu'
elementare è `Card` che rappresenta una singola carta. I campi dei giocatori,
che sono composti da piu' carte, vengono rappresentati dall'entità `Field`.
L'entità `Deck` rappresenta il mazzo da cui i giocatori pescano a inizio turno.
L'entità `Board` rappresenta il tavolo di gioco, è responsabile del mantenimento
di tutte le informazioni inerenti alle carte durante tutte le fasi del gioco, e aggrega
tutte le entità nominate in precedenza in aggiunta alla pila degli scarti.
Le entità `Turn`, `Game`, e `Match`, organizzate a loro volta per composizione, gestiscono
rispettivamente un singolo turno, una partita composta da piu' turni, e un match composto da
piu' partite. Queste entità implementano l'interfaccia `Playable` tramite la quale è possibile
avanzare le fasi del gioco fornendo una delle azioni (`Action`) indicate dall'entità stessa.

![](Model.drawio.png)

### Creazione del tavolo (`Board`, `Field`, `Deck`, `Card`)

La creazione delle entità `Board` avviene
tramite **Factory Methods** contenuti nell'oggetto `BoardFactory`.
Per facilitare e sintetizzare il loro utilizzo, soprattutto a scopo di testing,
è stato realizzato un DSL, che permette di popolare una `Board` indicando
quali carte inserire nei sui vari elementi. Il DSL comprende funzionalità per
la creazione dei sotto-elemnti della `Board`, come le singole carte (`Card`), i campi
dei giocatori (`Field`), il mazzo (`Deck`), e la pila degli scarti.

![DSL per la creazione di `Board`](BoardDSL.drawio.png)

![DSL per la creazione di `Field`](FieldDSL.drawio.png)

![DSL per la creazione di `Card`](CardDSL.drawio.png)

### Playable

L'interfaccia `Playable` rappresenta le entità che racchiudono le funzionalità delle dinamiche
di gioco.
Attraverso di essa è possibile avanzare nelle varie fasi del gioco, cioè alterare lo stato
delle entità `Playable`, scegliendo una tra le azioni (`Action`) disponibili.

L'entità base è `Turn` che rappresenta un singolo turno di un singolo giocatore, e
aggrega al suo interno il giocatore, la sua mano, e il tavolo (`Board`).
L'entità `Game` rappresenta un'intera partita composta da piu' turni, mentre `Match` rappresenta
una o piu' partite ed è composta da uno o piu' `Game`.

`Game` gestisce il progresso di una partita alternando i giocatori a ogni turno, generando
la giusta tipologia di `Turn` in base alla fase di gioco (`GamePhase`) in cui il turno avviene,
e calcolando il punteggio finale della partita.

`Match` gestisce il progresso di piu' partite durante un match con punteggio limite, accumulando
i punteggi delle partite.

## Controller

![Controller e View](Controller.drawio.png)

L'interfaccia `Playable` è utilizzata da `GameController`, che gestisce l'interazione con l'utente permettendo
a quest'ultimo di selezionare l'azione desiderata durante il suo turno.
`MatchController` aggiunge la possibilità di giocare piu partite tramite composizione con `GameController`.
L'istanza del controller adeguato è creata da `MenuController` basandosi sull'input dell'utente.

## View

`CLIMenu` gestisce la rappresentazione degli elementi del menu principale, essi
sono istanze dell'interfaccia `SelectableMenuItem`.

![CLIMenu](CLIMenu.png)

`CLIView` gestisce la rappresentazione del gioco, ottenendo lo stato della partita da `GameController` attraverso
l'entità `GameState`.

![CLIView](CLIView.drawio.png)