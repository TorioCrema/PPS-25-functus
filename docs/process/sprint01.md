# Sprint 1

## Obiettivo

L'obiettivo di questa sprint e' di otterne la possibilta' di generare il campo di un giocatore e visualizzarlo

## Scadenza

La scadenza della sprint e' il 20/07/26.

## Backlog

<table>
    <thead>
        <tr>
        <th>Priorita'</th>
        <th>Nome</th>
        <th>Descrizione</th>
        <th>Sprint Task</th>
        <th>Volontario</th>
        <th>Stima iniziale</th>
        <th>Stima sprint 1</th>
    </thead>
    <tbody>
        <tr>
        <td rowspan="4">1</td>
        <td rowspan="4">Organizzazione progetto</td>
        <td rowspan="4">Creazione e configurazione del repository GitHub e progetto Scala</td>
        <td>Setup del git flow e GitHub actions</td>
        <td>Simone</td>
        <td>2</td>
        <td>0</td>
        </tr>
        <tr>
        <td>Analisi dell'architettura</td>
        <td>Alex, Luca, Simone</td>
        <td>5</td>
        <td>2</td>
        </tr>
        <tr>
        <td>Setup progetto Scala</td>
        <td>Alex</td>
        <td>2</td>
        <td>0</td>
        </tr>
        <tr>
        <td>Setup documentazione</td>
        <td>Alex, Luca, Simone</td>
        <td>2</td>
        <td>0</td>
        </tr>
        <tr>
        <td rowspan="6">2</td>
        <td rowspan="6">Tavolo</td>
        <td rowspan="6">Generazione e gestione del tavolo</td>
        <td>Deck</td>
        <td>Luca</td>
        <td>4</td>
        <td>0</td>
        </tr>
        <tr>
        <td>Field</td>
        <td>Luca</td>
        <td>4</td>
        <td>0</td>
        </tr>
        <tr>
        <td>Board</td>
        <td></td>
        <td></td>
        </tr>
        <tr>
        <td>Card View</td>
        <td>Simone</td>
        <td>4</td>
        <td>0</td>
        </tr>
        <tr>
        <td>Field View</td>
        <td>Alex</td>
        <td>2</td>
        <td>2</td>
        </tr>
        <tr>
        <td>Board View</td>
        <td></td>
        <td></td>
        </tr>
        <tr>
        <td rowspan="3">3</td>
        <td rowspan="3">Turno</td>
        <td rowspan="3">Come utente, voglio poter eseguire il mio turno, secondo le azioni disponibili</td>
        <td>Turn</td>
        <td></td>
        <td></td>
        </tr>
        <tr>
        <td>Actions</td>
        <td></td>
        <td></td>
        </tr>
        <tr>
        <td>Input Utente</td>
        <td></td>
        <td></td>
        </tr>
        <tr>
        <td rowspan="2">4</td>
        <td rowspan="2">Match</td>
        <td rowspan="2">Come utente, voglio poter iniziare un match</td>
        <td>Game</td>
        <td></td>
        <td></td>
        </tr>
        <tr>
        <td>Match</td>
        <td></td>
        <td></td>
        </tr>
    </tbody>
</table>

## Sprint review

Nonostante la mancanza della possibilita' di visualizzaze un campo, le sue funzionalita'
sono stante implementate e validate dal committente, che risulta soddisfatto del risultato.
Inoltre, durante la riunione di fine sprint, il committente ha individuato una regola del gioco che
non era presente nei requisiti, questa regola e' quindi stata aggiunta ai requisiti funzionali.

## Sprint retrospective
Le dipendenze tra le sezioni della struttura del progetto hanno imposto dei rallentamenti
allo sviluppo, in quanto alcune parti necessitano della presenza di altre per il loro
funzionamento (come la presenza di `Card` per `Field`). Questo tipo di conflitto era
stato previsto e il team di sviluppo ha deciso di fornire le interfacce di ogni sezione,
anche se non implementate, in modo da portele utilizzare in caso siano Depended on Components
dei test di altre sezioni tramite la libreria `Mockito`.