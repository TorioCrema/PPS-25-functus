# Requisiti

## Requisiti di business

- Completamento dello sviluppo entro la deadline prestabilita
- Raggiungimento di un esito positivo da parte degli stakeholder al terminen di ciascuna sprint

## Modello di dominio

Il dominio del progetto e' basato sulle seguenti regole di gioco:

Il gioco utilizza il mazzo di carte regionali italiane e prevede due giocatori. Ogni giocatore inizia la partita con quattro carte coperte sul campo. Al primo turno, ogni giocatore puo' osservare due delle sue carte, una volta viste e memorizzate le ripone coperte. Durante la partita i giocatori non possono visione le proprie carte o quelle degli avversari.
Durante il proprio turno, un giocatore pesca una carta dal mazzo, la guarda, e la sostituisce con una delle carte nel suo campo e scarta la carta sostituita a faccia in su in cima alla pila degli scarti.
Alcune carte, quando pescate a inizio turno, possono essere scartate per ottenere un effetto, invece che essere usate per sostiuire
una carta sul propio campo. Queste carte sono:
- 6: permette al giocatore di osservare temporaneamente una delle carte dell'avversario
- 7: permette al giocatore di osservare temporaneamente una delle proprie carte
- 8: permette al giocatore di scambiare una propria carta per una carta dell'avversario, a scelta del giocatore, senza osservare nessuna delle due

All'inizio del proprio turno, se e' presente una carta in cima alla pila degli scarti, il giocatore puo' scartare una
delle proprie carte. Se questa carta non ha lo stesso valore della carta in cima alla pila, il giocatore dovra'
mantenere la carta da lui scartata sul proprio campo, e, in aggiunta, pescare una carta e aggiungerla al proprio campo,
senza guardarla. Se la carta scartata corrisponde in valore alla carta in cima alla pila, il giocatore non riceve alcun
tipo di penalita' e la carta da lui scartata rimane in cima alla pila.
In aggiunta, all'inizio del proprio turno, se la carta in cima alla pila degli scarti e'
un re, il giocatore puo' pescare quella carta invece che pescare dalla cima del mazzo.
Nel caso in cui il mazzo termini, verra' mischiata la pila degli scarti e verra' utilizzata come mazzo.
La partita termina quando, al termine del proprio turno, uno dei giocatori chiama "Cactus", questo sara' il suo ultimo turno e l'avversario giochera' un ultimo turno.
Al termine della partita i giocatori svelano le proprie carte e aggiungono il valore delle proprie carte al proprio punteggio. Il valore delle carte corrisponde al loro numero, partendo dall'asso
(1) fino al cavallo (9). Il re ha valore 0.
Vengono giocate piu' partite finche' un giocatore non supera un punteggio concordato dai giocatori prima di iniziare. Il giocatore che supera questo punteggio perde.
Qualore un giocatore ottiene alla fine di una partita un punteggio totale uguale a quello concordato, il suo punteggio viene dimezzato.

Da cui si rilevano i seguenti elementi principali:

- **Mazzo**: composto dalla carte da gioco tradizionali
- **Tavolo**: ambiente che contiene le carte, sia in gioco che scartate, o nella pila di pesca, e le carte di ogni giocatore
- **Giocatore**: interagisce con il tavolo durante il proprio turno
- **Partita**: consiste in un numero variabile di turni
- **Match**: composto da un numero variabile di partite

## Requisiti Funzionali

### Di utente

- **RFU01 Interazione**
  - Controllo delle mosse tramite input da tastiera (scartare, pescare, scelta della carta da sostituire, e chiamare "Cactus", terminare turno)
  - Configurazione del punteggio limite all'inizio di un match

- **RFU02 Fine partita e match**
  - Visualizzazione dell'esito della partita, con punteggio finale e vincitore nel caso di partita singola
  - Visualizzazione del punteggio cumulativo durante il match tra una partita e l'altra
  - Visualizzazione punteggio finale e vincitore a fine match

### Di sistema

- **RFS01 Gestione del mazzo**
  - Definire e mantenere la struttura del mazzo tradizionale durante la partita, mantenendo integro il numero di carte e il loro valore/seme

- **RFS02 Gestione del tavolo**
  - Gestione durante la partita della divisione del mazzo in: pila di pesca, pila degli scarti, campo di ogni giocatore
  - Gestione dello shuffle: riordinare le carte in maniera randomica, e rimpiazzare le carte dalla pila degli scarti alla pila di pesca

- **RFS03 Gestione del turno**
  - Gestione dell'azione di scarto a inizio turno
  - Gestione dell'eventuale penalita' derivata dallo scarto
  - Gestione della pesca delle carte (dal mazzo o dagli scarti nel caso di un re) e eventuali effetti
  - Gestione della sostituzione delle carte sul campo
  - Gestione della chiamata "Cactus"

- **RFS04 Gestione della partita**
  - Inizializzazione del campo
  - Gestione dell'alternanza dei turni tra i giocatori
  - Calcolo del punteggio di fine partita

- **RFS05 Gestione del match**
  - Impostazione punteggio limite
  - Gestione punteggi cumulativi
  - Gestione caso limite di raggiungimento del punteggio concordato

- **RFS06 Effetti speciali**
  - Dopo aver pescato una delle seguenti carte l'utente puo' scegliere di usufruire del loro effetto:
  - 6: permette al giocatore di osservare temporaneamente una delle carte dell'avversario
  - 7: permette al giocatore di osservare temporaneamente una delle proprie carte
  - 8: permette al giocatore di scambiare una propria carta per una carta dell'avversario, a scelta del giocatore, senza osservare nessuna delle due

- **RFS07 Interfaccia e visualizzazione**
  - Presenza di un menu' iniziale in cui poter scegliere tra partita singola o match
  - Visualizzazione del tavolo tramite CLI
  - Interazione con l'utente tramite CLI
  - Visualizzione esito di partita e match tramite CLI

## Requisiti non funzionali

- **RNF01 Usabilita'**
  - Presenza di controlli intuitivi
  - L'interfaccia deve essere chiara e leggibile

- **RNF02 Prestazioni**
  - Il tempo di risposta ai comandi dell'utente deve adeguatamente veloce 

- **RNF03 Manutenibilita'**
  - Il codice prodotto deve essere modulare, estendibile, e documentato in maniera completa

- **RNF04 Estendibilita'**
  - Il sistema prodotto deve poter permettere l'aggiunta di:
    - condurre partite contro un avversario virtuale
    - interazione tramite un'interfaccia grafica

- **RNF05 Testabilita'**
  - Presenza di testing per validare il funzionamento del sistema prodotto

## Requisiti di implementazione

- **RI01 Linguaggio di programmazione**
  - Il sistema deve essere implementato in Scala

- **RI02 MVC**
  - Il sistema deve seguire il pattern Model-View-Controller

- **RI03 Testing**
  - Utilizzo del metodo di sviluppo Test-Driven-Development (TDD)

- **RI04 Collaborazione tra sviluppatori**
  - Gli sviluppatori collaborano tramite la piattaforma Github per la gestione del codice sorgente, della documentazione, e del versioning del codice