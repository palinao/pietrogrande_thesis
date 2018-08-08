# pietrogrande_thesis
Nanopublication generation

Light and not fully working version due to upload limits

Reference for Neji documentation at https://github.com/BMDSoftware/neji/wiki

## SourceLoader

Codice per creare un dataset di abstract di paper da pubmed e relative assertions.
Il dataset è creato dall esecuzione di SourceLoader con parametro a linea di comando per il numero di paper da scaricare. Il codice funziona come segue:

Esegue il parsing del file "../Additional_files/nanopublications_v4.0.0.0.trig" disponibile al link 

http://rdf.disgenet.org/download/v4.0.0/nanopublications_v4.0.0.0.trig.gz 

dopo aver ottenuto il codice identificativo dell articolo di una singola nanopublications (solo quelle "manually curated"), grazie alla libreria jsoup, scarica il sorgente html dalla pagina pubmed dell'articolo e ottiene titolo e abstract (se non presenti si scarta la nanopublication). Crea una cartella dedicata in "nanopubs/dataset", denominata con l'identificativo di pubmed, e organizza due file, uno con abstract e titolo e uno con la sola assertion della nanopublication. 

Dopo aver raggiunto il numero di papers stabilito alla chiamata del programma, il codice continua a fare il parsing del file .trig per verificare se ci sono altre nanopubs per un paper già scaricato e le aggiunge in coda alle altre assertion.

Inoltre, si raccolgono statistiche sul dataset di nanopublications, disponibili nella cartella "nanopubs/statistics" che contiene tre file con tutti gli identificativi di "disorders" (identificativi UMLS), "genes" (identificativi NCBI) e "relations" (identificativi SIO) che sono presenti nel file .trig ed infine un file con statistiche sul dataset di nanopublications.

file statistics.info per il dataset d'esempio di 1001 coppie abstract/assertions

>Number of assertions in the source file: 67299  
>Number of folders: 1001  
>Number of manually curated assertions in the source file: 55640  
>Number of automatically generated assertions in the source file: 11658  
  
## Dictionaries Builders

C'è la necessita di costruire dei dizionari (Neji utilizza dizionari .tsv) che servano da mappa tra gli id utilizzati nelle assertions e i concetti espressi a parole nei testi. Nei dizionari si utilizza come chiave l'identificativo del concetto da descrivere e come valore un insieme di label che lo rappresentino, questo si fa separatamente per geni e malattie poichè nel dataset di nanopublications esse hanno due sorgenti diverse (NCBI geneID per geni e UMLS conceptID per malattie).

__DIZIONARIO PER I GENI__

In Builder Genes, il codice DictionaryBuilderGenes fa il parsing del file "Homo_sapiens.gene_info" disponibile al link

ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Mammalia/

che contiene vari campi tra cui l'id richiesto e alcune label utili, di nomenclatura e simboliche. Produce come output un file dizionario dove le entries hanno come chiave l'id del gene e come valore tutte le etichette separate dal simbolo "|", le etichette che ho scelto sono nomi ufficiali, sinonimi e simboli. Il file "Builder Genes/geneslist_ncbi.tsv" contiene il dizionario finale.

__DIZIONARIO PER LE MALATTIE__

In Builder Disorders si ottiene il dizionario che descrive le possibili malattie di cui fare Named Entity Recognition. L'UMLS non fornisce file comprensivi come quello per i geni umani di NCBI, perciò è necessario scaricare una alla volta le label di ogni singolo concetto dal suo URL. Inoltre UMLS è un raccoglitore di concetti molto più ampio di sole malattie e divide gli stessi in vari gruppi semantici, a questo link è possibile vedere tutti i gruppi

https://metamap.nlm.nih.gov/Docs/SemGroups_2013.txt

Noi siamo interessati al gruppo DISO che si riferisce ai "disorders", inoltre in questo gruppo ci sono vari tipi semantici, per motivi pratici non era possibile scaricare tutti i tipi semantici, perciò ho scaricato solo i concetti dei tipi che era più sensato fossero collegati alla genetica (il concetto di una gamba rotta non era utile per esempio). I tipi utilizzati sono

T019 Anatomical Abnormality  
T020 Acquired Abnormality  
T046 Pathologic Function  
T047 Disease or Syndrome  
T048 Mental or Behavioral Dysfunction  
T049 Cell or Molecular Dysfunction  
T190 Anatomical Abnormality  
T191 Neoplastic Process  

Nella cartella "Builder Disorders/DISO_files", per ogni tipo sono elencati tutti i concept corrispondenti scaricati dal sito 

http://linkedlifedata.com/resource/umls

che è il @prefix utilizzato nelle nanopubs, il codice "DictionaryBuilderDisoder", attraverso la libreria jsoup, scarica identificativi e label dai relativi URI. Le pagine di linkedlifedata per i singoli concept utilizzano AJAX e non era possibile con jsoup risalire alla label dei sinonimi, perciò ho ovviato utilizzando come sorgente il sito dell'NCBI al link

https://www.ncbi.nlm.nih.gov/gtr/conditions/

che fornisce le label in solo html.

Inoltre, dato che il codice avrebbe (ed ha) impiegato giorni, si è velocizzato il tutto utilizzando per il gruppo T047 (il più ampio) un dizionario abbastanza ben fornito già incluso in Neji, perciò se un concetto era già presente in questo dizionario si è evitato di scaricarlo. Per questo motivo sono presenti alcuni doppioni, dovuti al modo in cui le mappe che utilizzo nel codice organizzano le chiavi, ed è necessario eseguire successivamente "GroupSolver" per avere un dizionario pulito.

Il file "Builder Disorders/Disorders_UMLS.tsv" contiene il dizionario finale e il file "Builder Disorders/statistics_umls.rtf" descrive il suo contenuto e distribuzione per gruppi.

__PER ENTRAMBI I DIZIONARI__

Entrambi i dizionari seguono lo schema suggerito da Neji

>1. identifier that contains 4 fields concatenated with a ":", following the template <source>:<id>:<type>:<group>;  
>2. names concatenated with a "|".  
>  
>UMLS:C0001327:T047:DISO acute laryngitis|acute laryngitis nos  
>UMLS:C0001339:T047:DISO acute pancreatitis|pancreatitis, acute  
>UMLS:C0001344:T047:DISO pharyngitis nos acute|acute pharyngitis|pharyngitis acute  
>UMLS:C0001360:T047:DISO acute thyroiditis|thyroiditis acute   

## Learning Set

Attraverso Neji è stato possibile trainare dei modelli basati su Conditional Random Field per il riconoscimento di specifiche entità mediche, nel nostro caso modelli per geni e per malattie.

Secondo le specifiche di Neji il corpus deve essere nel seguente formato 

>The sentences file should contain one sentence per line, which includes the unique identifier and respective sentence   >separated by a white space. The unique identifier should not contain white spaces.  
>  
>P00001606T0076 Comparison with alkaline phosphatases and 5-nucleotidase  
>P00008171T0000 Pharmacologic aspects of neonatal hyperbilirubinemia.  
>  
>The annotations file should contain one annotation per line, which follows the following format: SENTENCE_ID|FIRST_CHAR >LAST_CHAR|TEXT. The character counting used for the FIRST_CHAR and LAST_CHAR, must be performed discarding white spaces.  
>  
>P00001606T0076|14 33|alkaline phosphatases    
>P00001606T0076|37 50|5-nucleotidase    

Nel file "../Additional_files/nanopublications_v4.0.0.0.trig" delle nanopublication, per ogni assertion, è riportato l'id della malattia, l'id del gene e la frase da cui si è ottenuta l'assertion che le lega. 

Per rispettare il formato richiesto, è necessario annotare ogni frase trovando dove il gene e la malattia sono citati. Il codice di "SetBuilder" si occupa di annotare ogni frase del corpus confrontanto la frase con le etichette di geni e malattie indicati dalle relative assertion, il confronto è case unsensitive e tralascia gli spazi come previsto da Neji. 

Il file delle nanopublications contiene 67299 nanopublications di cui 55640 sono manually curated, però pesso le nanopublications si riferiscono alle stesse frasi, con assertion in cui varia solo la relazione, portando a 28129 distinct phrases. Si sono creati due corpus differenti per il training dei modelli, uno per i geni e uno per le malattie, rispettivamente nelle cartelle "Learning Set/Disorders data" e "Learning Set/Genes data". 

Entrambi i corpus sono stati divisi casualmente in training set e validation set in proporzione 80-20. I training sets sono nelle corrispondenti cartelle ("Genes data/Training set" e  "Disorders data/Training set") in due file di frasi e annotazioni come descritto da Neji, mentre per i validation sets sono state create due cartelle che contengono un singolo file di testo per ogni frase del set, perchè servono come validation input per Neji.

Infine vi sono due cartelle "Metafiles" che contengono 
- un file ("Metafiles/statisticsdises.txt" per le malattie e "Metafiles/statisticsgenes.txt" per i geni) che descrive il contenuto dei due corpus
- un altro file ("Metafiles/validationcodesdises.txt" per le malattie e "Metafiles/validationcodesgenes.txt" per i geni) che raccoglie le annotazioni corrette per i file del validation set.

Prendiamo da esempio il file "statisticsdises.txt"

>Number of available sentences: 28129  
>Sentences in the training set: 9256 (9524 annotated disorders)  
>Sentences in the validation set: 2419 (2494 annotated disorders)  
>Sentences with missing diso id: 3962  
>Sentences with no label match: 12492  

Sono interessanti le ultime due voci.

- quella relativa al missing id si riferisce alla mancanza dell'id della malattia presente nell'assertion per una determinata frase, in particolare il numero si riferisce alle frasi che potrebbero essere aggiunte al corpus se il dizionario delle malattie avesse più concetti di quelli che ha, tuttavia non è di facile soluzione perchè spesso sono URI non funzionanti e comunque non porterebbero un guadagno ampio di nuove frasi.

- quella relativa al no label match si riferisce a frasi il cui id della malattia è presente nel dizionario, però nessuna delle label nel dizionario per quella malattia è trovata nella frase. Un dizionario più preciso porterebbe ad un guadagno di frasi, però richiederebbe una compilazione di fatto manuale del dizionario e da un utente esperto in ambito medico poichè le differenze dietro al mancato match sono relative a variazioni di forma come a volte trattini o sigle particolari.

Un esempio relativo ai geni è questo

Gene nella frase - MicroRNA-26a  
Labels presenti nel dizionario - microRNA 26a-1, MIR26A1, microRNA 26a-1, hsa-mir-26a-1MIR26A, MIRN26A1, mir-26a-1  

Invece per il file "statisticsgenes.txt" si ha

>Number of available sentences: 28129  
>Sentences in the training set: 12916 (13861 annotated genes)  
>Sentences in the validation set: 3364 (3598 annotated genes)  
>Sentences with missing diso id: 5  
>Sentences with no label match: 11844  

Secondo le specifiche di Neji, i due dataset dovrebbero essere abbastanza ampi per trainare dei modelli che non presentino overfitting, sono stati trainati 8 modelli per geni e 8 per malattie che sono stati poi validati. Sono stati variati vari parametri per il training tra cui l'ordine del CRF utilizzato e le features prese in considerazione.

## Pipeline

Nella cartella "Pipeline" ci sono tre cartelle, 
- "input-files" che contiene i file di testo da annotare, in generale gli abstract, mentre per la validazione dei modelli ci saranno i file del validation set corrispondente
- "output-files" che contiene l'output di Neji, come file di annotazione nella directory "a-files" e la loro mappatura ad  assertions nella cartella "nanopubs-assertion"
- "neji-2.0.0" che contiene il programma (non nella versione su GitHub) e file sui modelli trainati

La procedura si avvia con lo script bash "Pipeline.sh" che si occupa di invocare Neji con il modello richiesto

Inoltre, nella cartella "neji-2.0.0/resources/models" sono presenti tutti i modelli trainati divisi in singole cartelle,
in ogni singola cartella, nella sottocartella "model" è presente un file "model_name.config" che elenca la configurazione di features utilizzate da ogni modello in fase di training.

Di seguito la breve documentazione sulla pagina di Neji sul significato delle singole features, però si può darne un'interpretazione

>The definition of CRF models' characteristics is performed using a simple configuration file. Through it, you can specify:  
>  
>- the features that the model will use
>- the order of the CRF
>- the parsing direction
>- the target entity type
>
>token=1  
>stem=0  
>lemma=1  
>pos=1                         
>chunk=1  
>nlp=1  
>capitalization=0  
>counting=1  
>symbols=1  
>ngrams=1  
>suffix=1  
>prefix=1  
>morphology=1  
>greek=1  
>roman=0  
>prge=1   
>concepts=1   
>verbs=1  
>window=1   
>conjunctions=0  
>order=1  
>parsing=FW  
>entity=gene  

## Validation 

In questa cartella sono presenti i file riguardanti la validation dei differenti modelli, poichè non è possibile far eseguire una validazione automatica a Neji. 

Riassumendo, il file "ModelValidator" si occupa di confrontare i file di annotazione presenti in "model_output" con gli id previsti per ognuno di essi. Gli id corretti sono contenuti nei file "validationcodesgenes.tsv" e "validationcodesdises.tsv" per geni e malattie rispettivamente. Quello che fa il programma è caricare i dizionari e verificare se l'annotazione trovata da Neji corrisponde ad una label presente nel dizionario, alla voce id corretta. 

Sono stati validati i 16 modelli, rispettivamente con i propri validation sets e sono state calcolate le misure di precision e recall, **Exact match** si riferisce alla situazione in cui l'annotazione che si ottiene in output è precisamente presente nel dizionario e **Partial match** si riferisce a quando l'annotazione *è contenuta* o *contiene* una label del dizionario all'id corretto, che per il nostro scopo va in generale bene (es. "Clinical, biochemical, and genetic findings in a large pedigree of male and female patients with 5 alpha-reductase 2 deficiency" Label annotated by Neji: steroid 5 alpha-reductase 2).

Infine "missing label" si riferisce più in general ad una label che non è presente nel dizionario (quello che non è exact match), cioè può essere un annotazione che
- si riferisce ad un'entità già annotata correttamente (verrebbe aumentata la precisione e non il recall)
- si riferisce ad un'annotazione che fa parte del partial match (non aumenta ne recall ne precision)
- si riferisce ad un'entità non già annotata ma corretta (aumentaterebbero sia precisione che recall)
- si riferisce ad un'annotazione sbagliata (non aumenterebbero nè precisione nè recall)

Di fatto però per sfruttare questo margine di miglioramente concesso dalle missing label bisognerebbe avere dei dizionari curati a mano. Ad esempio la frase
"A new Leu253Arg mutation in the RP2 gene in a Japanese family with X-linked retinitis pigmentosa" e la label del dizionario
"retinitis pigmentosa 2" portano ad un missing label qualora Neji annotaase "retinitis pigmentosa".

I risultati sono di seguito

**MODELS GENES**

**model_a**

Exact match - precision: 0.79744524, recall: 0.7287382  
Partial match - precision: 0.8506691, recall: 0.7773763  
missing label: 335/3288  

**model_b**

Exact match - precision: 0.7846013, recall: 0.71372986  
Partial match - precision: 0.852429, recall: 0.7754308  
missing label: 388/3273  

**model_c**

Exact match - precision: 0.7758827, recall: 0.72067815  
Partial match - precision: 0.84859365, recall: 0.7882157  
missing label: 412/3342  

**model_d**

Exact match - precision: 0.78005505, recall: 0.70872706  
Partial match - precision: 0.84735394, recall: 0.7698721  
missing label: 399/3269  

**model_e**

Exact match - precision: 0.78808564, recall: 0.70594776  
Partial match - precision: 0.8575861, recall: 0.76820457  
missing label: 381/3223  

**model_f**

Exact match - precision: 0.78808564, recall: 0.70594776  
Partial match - precision: 0.8575861, recall: 0.76820457  
missing label: 381/3223  

**model_g**

Exact match - precision: 0.7902633, recall: 0.717343  
Partial match - precision: 0.85088795, recall: 0.77237356  
missing label: 351/3266  

**model_h**

Exact match - precision: 0.7970744, recall: 0.7117843  
Partial match - precision: 0.8565204, recall: 0.7648694  
missing label: 342/3213  

**MODELS DISEASES**

**model_a1**

Exact match - precision: 0.8012259, recall: 0.733761  
Partial match - precision: 0.9067426, recall: 0.83039296  
missing label: 239/2284  

**model_b1**

Exact match - precision: 0.7881579, recall: 0.72052926  
Partial match - precision: 0.9, recall: 0.82277465  
missing label: 251/2280  

**model_c1**

Exact match - precision: 0.80209243, recall: 0.7377707  
Partial match - precision: 0.90627724, recall: 0.83360064  
missing label: 241/2294  

**model_d1**

Exact match - precision: 0.8022648, recall: 0.7385726  
Partial match - precision: 0.9063589, recall: 0.83440256  
missing label: 241/2296  

**model_e1**

Exact match - precision: 0.79741186, recall: 0.71651965  
Partial match - precision: 0.90361446, recall: 0.81194866  
missing label: 220/2241  

**model_f1**

Exact match - precision: 0.79794824, recall: 0.7173216  
Partial match - precision: 0.90410346, recall: 0.8127506  
missing label: 220/2242  

**model_g1**

Exact match - precision: 0.7986871, recall: 0.7317562  
Partial match - precision: 0.90853393, recall: 0.83239776  
missing label: 239/2285  

**model_h1**

Exact match - precision: 0.79402584, recall: 0.7141139  
Partial match - precision: 0.90592957, recall: 0.81475544  
missing label: 241/2243  























