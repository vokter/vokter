
AIRI - Assignment 02

Realizado por: Eduardo Duarte - emod@ua.pt - 60658


=== EXECUÇÃO ===========================================

Este projeto trata-se de um projeto Maven, pelo que deve ser executado como tal.

A aplicação poderá ser executada a partir do ficheiro Main.java, disponível em
/src/main/java/edduarte/airi/assignment02/.

A aplicação tem dois modos de execução: modo normal / servidor e o modo de avaliação.

No modo normal, a aplicação instancia um servidor com um website, disponível em localhost:<port especificado>, e indexa o corpus especificado aonde se irão realizar as queries.
No modo de avaliação, a aplicação indexa o corpus especificado e realiza um conjunto de queries-teste e avaliações baseadas no ficheiro "evaluate.txt" (o ficheiro entregue pelos docentes).

Em ambos os casos, este deverá receber argumentos de input quando executado, segundo a seguinte intrução de utilização:

-c <corpus1> <corpus2> ... -nocase -stop -stem

	-port (opcional)	indica o port do servidor web

	-t (opcional)		indicia a thread-pool máxima para o servidor

	-c (obrigatório)	deverá incluir uma diretoria 'root' que correspondem ao conjunto de
						documentos a indexar

	-nocase (opcional)	configura o processamento para ignorar maíusculas de palavras, reduzindo
						todos os termos encontrados a minúsculas

	-stop (opcional)	configura o processamento para recorrer a filtração de stopwords, utilizando o ficheiro por defeito

	-stop_file (opcional) carrega um ficheiro de stopwords a utilizar, em vez das stopwords por defeito

	-stem (opcional)	configura o processamento para efetuar stemming, recorrendo ao Portuguese
						Porter stemmer


Recomenda-se ao docente que este execute a aplicação da seguinte forma para o modo servidor:

java -Xmx2048m -Xms2048m -jar airi.assignment02-release.jar -c <path_do_corpus_EMEA> -nocase -stop -stem

e da seguinte forma para o modo avaliação:

java -Xmx2048m -Xms2048m -jar airi.assignment02-release.jar -c <path_do_corpus_EMEA> -nocase -stop -stem -ev


Em todo o caso, no modo servidor, o utilizador pode modificar em "real-time", as propriedades "-nocase", "-stop", "-stop_file" e "-stem", clicando no ícone de roda dentada na página web.




=== IMPLEMENTAÇÃO =====================

O código implementado contém documentação e comentários de forma extensiva, explicando de forma detalhada o procedimento "passo-a-passo" em cada função da aplicação.

Relativamente ao diagrama de classes, recomenda-se que se leia as classes de baixo para cima, visto a aplicação começar a execução na classe Main e expandir a partir dessa classe.

As classes do pacote 'evaluation' correspondem às classes utilizadas para realizar avaliações às queries.
As classes do pacote 'filter' correspondem às classes utilizadas para filtrar texto, em cada pipeline de documentos e de queries.
As classes do pacote 'index' correspondem às classes utilizadas para representar unidades de informação, para indexar documentos, para gerir os sistemas de cache utilizados e para realizar pesquisas à colecção (utilizando uma query).
As classes do pacote 'query' correspondem às classes utilizadas para representar queries de pesquisa. O sub-pacote 'vectormodel' constitúem diferentes estados dos termos encontrados para uma query e para um documento ao longo do processo de pesquisa e de forma a representar de forma eficiente o espaço vetorial.
As classes do pacote 'reader' correspondem às classes utilizadas para ler documentos.
As classes do pacote 'rest' corresponde à estrutura / arquitetura REST utilizada pela aplicação para disponibilizar uma página web que efetua pedidos em "real-time" e utilizando AJAX.
As classes do pacote 'stemmer' correspondem às classes utilizadas para efectuar stemming de conteudos.
A classe do pacote 'tokenizer' corresponde ao Tokenizador.
As classes do pacote 'util' correspondem a utilizades comuns à aplicação.








