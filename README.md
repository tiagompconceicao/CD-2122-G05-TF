# CD-2122-G05-TF
Trabalho prático de avaliação final da unidade curricular de Computação Distribuída no semestre inverno 21/22 no Instituto Superior de Engenharia de Lisboa.

Este trabalho prático é composto por 7 projetos:
  - UserImpl: 	      Aplicação cliente
  - SensorSimulator:  Aplicação de simulação de sensores
  - FrontEndImpl:     Aplicação servidor do grupo de Front-End
  - FrontEndContract: Contrato gRPC para comunicação entre aplicação cliente e 
servidores Front-End
  - EventProcessor:   Aplicação servidor do grupo de processamento de eventos
  - EventLogger:      Aplicação de logging dos eventos recebidos no segundo nível
  - Configurator:     Aplicação para configurar servidor RabbitMQ no segundo nível


Modo de execução:
Primeiramente deverá compilar e publicar no repositório local do Maven o projeto do contrato de comunicação entre as 
aplicação cliente e servidores Front-End.
Para tal apenas é necessário correr num terminal de comandos na diretoria raiz de cada projeto o comando maven "package" 
para compilar o código e o comando "install" para publicar o projeto.
Com o contrato publicado agora é necessário gerar os artefactos das restantes aplicações, 
num terminal de comandos na diretoria raiz de cada projeto deverá executar o comando maven "package".

Para executar as aplicações deve, após gerar os respetivos artefactos, de executar os seguintes comandos,
na respetiva diretoria "target" gerada pela compilação dos artefactos:

  Aplicação de simulação de eventos:
	java -cp SensorSimulator-1.0-jar-with-dependencies.jar simulator.SensorSimulator <mode> <brokerIP> <eventsNumber>
  
  Aplicação configuração do servidor RabbitMQ:
	java -cp Configurator-1.0-jar-with-dependencies.jar rmqconfigurator.RabbitConfigurator <brokerIP>

  Aplicação de Logging:
	java -cp EventLogger-1.0-jar-with-dependencies logger.Logger <brokerIP>

  Aplicação servidor EPG:
	java -cp EventProcessor-1.0-jar-with-dependencies.jar eventsProcessor.EventProcessor <id> <brokerIP> <daemonIP>

  Aplicação servidor de Front-End:
	java -cp FrontEndImpl-1.0-jar-with-dependencies.jar frontEnd.Main <id> <daemonIP> <serverPort>

  Aplicação cliente:	
	java -cp EventsUser-1.0-jar-with-dependencies.jar EventsUser <serverIP> <serverPort>

- <mode> representa o modo de operação do simulator, valor 1 para executar modo automático, qualquer outro valor não nulo para executar em modo manual.
- <eventsNumber> representa o número de eventos a gerar pelo simulador, este argumento é opcional.
- <id> representa o identificador numérico único do processo.
- <brokerIP> representa o IP do servidor RabbitMQ.
- <daemonIP> representa o IP do servidor Daemon do Spread.
- <serverPort> representa o porto do servidor gRPC no qual ficará à escuta.
- <serverIP> representa o IP do servidor gRPC.
