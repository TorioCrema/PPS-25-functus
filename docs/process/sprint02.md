# Sprint 2

## Obiettivo

L'obiettivo di questa sprint è di ottenere la possibility' di eseguire un turno intero,
partendo da un tavolo completo, con eccezione degli effetti delle carte pescate.

## Scadenza

La scadenza della sprint è il 03/08/26.

## Backlog

<table>
    <thead>
        <tr>
        <th>Priorità'</th>
        <th>Nome</th>
        <th>Descrizione</th>
        <th>Sprint Task</th>
        <th>Volontario</th>
        <th>Stima iniziale</th>
        <th>Stima sprint 1</th>
        <th>Stima sprint 2</th>
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
        <td>0</td>
        </tr>
        <tr>
        <td>Analisi dell'architettura</td>
        <td>Alex, Luca, Simone</td>
        <td>5</td>
        <td>2</td>
        <td>0</td>
        </tr>
        <tr>
        <td>Setup progetto Scala</td>
        <td>Alex</td>
        <td>2</td>
        <td>0</td>
        <td>0</td>
        </tr>
        <tr>
        <td>Setup documentazione</td>
        <td>Alex, Luca, Simone</td>
        <td>2</td>
        <td>0</td>
        <td>0</td>
        </tr>
        <tr>
        <td rowspan="7">2</td>
        <td rowspan="7">Tavolo</td>
        <td rowspan="7">Generazione e gestione del tavolo</td>
        <td>Deck</td>
        <td>Luca</td>
        <td>4</td>
        <td>1</td>
        <td>0</td>
        </tr>
        <tr>
        <td>Field</td>
        <td>Luca</td>
        <td>4</td>
        <td>1</td>
        <td>0</td>
        </tr>
        <tr>
        <td>Board</td>
        <td>Luca</td>
        <td>8</td>
        <td>8</td>
        <td>0</td>
        </tr>
        <tr>
        <td>Card/Field/Board DSL</td>
        <td>Simone, Luca</td>
        <td>5</td>
        <td>5</td>
        <td>2</td>
        </tr>
        <tr>
        <td>Card View</td>
        <td>Simone</td>
        <td>4</td>
        <td>0</td>
        <td>0</td>
        </tr>
        <tr>
        <td>Field View</td>
        <td>Alex</td>
        <td>2</td>
        <td>4</td>
        <td>0</td>
        </tr>
        <tr>
        <td>Board View</td>
        <td>Alex</td>
        <td>4</td>
        <td>4</td>
        <td>0</td>
        </tr>
        <tr>
        <td rowspan="3">3</td>
        <td rowspan="3">Turno</td>
        <td rowspan="3">Come utente, voglio poter eseguire il mio turno, secondo le azioni disponibili</td>
        <td>Turn</td>
        <td>Simone</td>
        <td>5</td>
        <td>5</td>
        <td>2</td>
        </tr>
        <tr>
        <td>Actions</td>
        <td>Simone</td>
        <td>5</td>
        <td>5</td>
        <td>2</td>
        </tr>
        <tr>
        <td>Input Utente</td>
        <td>Alex</td>
        <td>7</td>
        <td>7</td>
        <td>0</td>
        </tr>
        <tr>
        <td rowspan="2">4</td>
        <td rowspan="2">Match</td>
        <td rowspan="2">Come utente, voglio poter iniziare un match</td>
        <td>Game</td>
        <td>Luca</td>
        <td>6</td>
        <td></td>
        </tr>
        <tr>
        <td>Match</td>
        <td></td>
        <td></td>
        <td></td>
        </tr>
    </tbody>
</table>

## Sprint review

Il committente risulta soddisfatto del risultato ottenuto, riportando le seguenti richieste e migliorie:
- rendere consistente la lingua dell'interfaccia, attualmente è parzialmente in italiano e inglese
- la carta scartata a inizio turno deve poter essere vista dall'utente anche se errata

## Sprint retrospective

La divisione del lavoro e la coordinazione tra gli sviluppatori è risultata piu' efficiente rispetto alla sprint
precedente.
