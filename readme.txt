Kompilacja:
mvn compile


Uruchamianie:
Do uruchomienia poszczególnych programów można skorzystać z dostarczonych skryptów

RMI registry:
./registry [port]
port – opcjonalny numer port, domyślnie 1099

./registry 1100

Agent:
./agent.sh <config_file>
config_file – plik (JSON) z konfiguracją serwera i wejściowymi danymi dla strefy reprezentowanej daną maszynę

./agent.sh violet07.json

Fetcher:
./fetcher.sh <port>
port – port rejestru RMI agenta

./fetcher.sh 1100

Query signer:
./signer.sh <port>
host – opcjonalny adres hosta, może być adresem IP lub nazwą hosta, domyślnie brana jest nazwa lokalnego hosta
post – port rejestru RMI

./signer.sh 1101

Client:
./client.sh <registry_host> <registry_port> <query_signer_host> <query_signer_port>
query_signer_host – adres IP lub nazwa maszyny, na której uruchomiony jest query signer
query_signer_port - port rejestru RMI

./client.sh 10.0.0.1 1101

Po uruchomieniu klient będzie dostępny w przeglądarce pod adresem: 
http://0.0.0.0:4567/

Plik konfiguracyjny dla agenta podany jest w formacie JSON. 
Wymaganymi polami są: path, expiry, contacts. 
contacts musi zawierać pola: path, host (hostname/ip), port
