# Design architetturale

La struttura architetturale è stata individuata partendo dai requisiti funzionali e non funzionali definiti
durante la fase di analisi.
Si è cercato di realizzare un sistema estendibile, mantenibile, modulare, con nette separazioni di responsabilità.

## MVC

L'architettura è stata realizzata aderendo al pattern MVC (Model-View-Controller), che consente di
mantenere separate le sezioni dedite alla logica del sistema, la sua rappresentazione grafica, e il coordinamento
tra questi.
Nel contesto di questo progetto queste sezioni si occupano di:
- Model: gestisce i dati e la logica dell'applicazione, ovvero le carte, la loro suddivisione
  nel tavolo, l'esecuzione delle meccaniche di gioco, e il calcolo dei punteggi. È composto da:
  - `Turn`: mantiene le informazioni del tavolo (`Board`) e la mano del giocatore durante il suo turno, è responsabile
    dell'esecuzione delle meccaniche di gioco
  - `Game`: gestisce una singola partita concatenando turni e alternando i giocatori
  - `Match`: gestisce la modalità di gioco con punteggio massimo, permettendo di giocare piu' `Game` consecutivi
  - `Opponent`: gestisce la logica dell'avversario virtuale
- View: gestisce la rappresentazione dai dati all'utente e ne raccoglie l'input. È composto da:
  - `CLIMenu`
  - `CLIView`
- Controller: gestisce la coordinazione tra View e Model. Ottiene gli input dell'utente dalla
  View e fornisce le scelte dell'utente al Model. È composto da:
  - `GameController`
  - `MatchController`
  - `MenuController`

Questa struttura acconsente di raggiungere gli obiettivi di manutenibilità, modularità, ed estensibilità,
in quanto ogni sezione ha responsabilità ben separate dalle altre e puo' essere modificata in maniera
indipendente da esse.