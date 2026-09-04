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


### Creazione del tavolo (`Board`)

La creazione delle entità `Board` avviene
tramite **Factory Methods** contenuti nell'oggetto `BoardFactory`.
Per facilitare e sintetizzare il loro utilizzo, soprattutto a scopo di testing,
è stato realizzato un DSL, che permette di popolare una `Board` indicando
quali carte inserire nei sui vari elementi. Il DSL comprende funzionalita per
la creazione dei sottoelemnti della `Board`, come le singole carte (`Card`), i campi
dei giocatori (`Field`), il mazzo (`Deck`), e la pila degli scarti.

### Playable

L'interfaccia `Playable` rappresenta le entità che racchiudono le funzionalità delle dinamiche
di gioco. L'entità base è `Turn` che rappresenta un singolo turno di un singolo giocatore.
L'entità `Game` rappresenta un'intera partita composta da piu' turni, mentre `Match` rappresenta
una o piu' partite ed è composta da uno o piu' `Game`.
Attraverso l'interfaccia `Playable` è possibile avanzare nelle varie fasi del gioco scegliendo
una tra le azioni (`Action`) disponibili.
