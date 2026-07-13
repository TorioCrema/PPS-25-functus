# Functus

## Introduzione

Implementazione in Scala del gioco di carte Cactus.

## Regole generali del gioco

Il gioco utilizza il mazzo di carte regionali italiane e prevede due giocatori. Ogni giocatore inizia la partita con quattro carte coperte sul campo. Al primo turno,
ogni giocatore puo' osservare due delle sue carte, una volta viste e memorizzate le ripone coperte. Durante la partita i giocatori non possono visione le proprie carte o
quelle degli avversari. Durante il proprio turno, un giocatore pesca una carta dal mazzo, la guarda, e la sostituisce con una delle carte nel suo campo e scarta la carta
sostituita a faccia in su in cima alla pila degli scarti.
Alcune carte, quando pescate a inizio turno, possono essere scartate per ottenere un effetto, invece che essere usate per sostiuire
una carta sul propio campo. Queste carte sono:
- 6: permette al giocatore di osservare temporaneamente una delle carte dell'avversario
- 7: permette al giocatore di osservare temporaneamente una delle proprie carte
- 8: permette al giocatore di scambiare una propria carta per una carta dell'avversario, a scelta del giocatore, senza osservare nessuna delle due

All'inizio del proprio turno, se e' presente una carta in cima alla pila degli scarti, il giocatore puo' scartare una delle proprie carte. Se questa carta non ha lo stesso valore
della carta in cima alla pila, il giocatore dovra' mantenere la carta da lui scartata sul proprio campo, e, in aggiunta, pescare una carta e aggiungerla al proprio campo, senza
guardarla. Se la carta scartata corrisponde in valore alla carta in cima alla pila, il giocatore non riceve alcun tipo di penalita' e la carta da lui scartata rimane in cima alla
pila.
Nel caso in cui il mazzo termini, verra' mischiata la pila degli scarti e verra' utilizzata come mazzo.
La partita termina quando, al termine del proprio turno, uno dei giocatori chiama "Cactus", questo sara' il suo ultimo turno e l'avversario giochera' un ultimo turno.
Al termine della partita i giocatori svelano le proprie carte e aggiungono il valore delle proprie carte al proprio punteggio. Il valore delle carte corrisponde al loro numero, partendo dall'asso
(1) fino al cavallo (9). Il re ha valore 0.
Vengono giocate piu' partite finche' un giocatore non supera un punteggio concordato dai giocatori prima di iniziare. Il giocatore che supera questo punteggio perde.
Qualore un giocatore ottiene alla fine di una partita un punteggio totale uguale a quello concordato, il suo punteggio viene dimezzato.

## Requisiti di massima obbligatori:

- Implemntazione delle funzionalita' del gioco descritte precedentemente, prevedendo che i giocatori giochino sulla stessa macchina a turno.
- Possibilita' di giocare una partita singola, oppure di una partita con punteggio cumulativo.
- Interazione con l'applicazione tramite CLI.

## Requisiti di massima opzionali:

- Implementazione di una modalita' per giocatore singolo contro un avversario virtuale.
- Interazione con l'applicazione tramite GUI.

