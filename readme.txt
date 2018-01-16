Kompilacja:
mvn compile


Uruchamianie:
Do uruchomienia poszczególnych programów można skorzystać z dostarczonych skryptów

RMI registry:
./registry [port]
port – opcjonalny numer port, domyślnie 1099

./registry 1100

Agent:
./agent.sh <host> <config_file>
host – nazwa hosta lub adres ip
config_file – plik (JSON) z konfiguracją serwera i wejściowymi danymi dla strefy reprezentowanej daną maszynę

./agent.sh violet07 violet07.json

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

./client.sh violet07 1100 khaki13 1101

Po uruchomieniu klient będzie dostępny w przeglądarce pod adresem: 
http://0.0.0.0:4567/

Plik konfiguracyjny dla agenta podana jest w formacie JSON. 
Wymaganymi polami są: path, gossip, expiry, contacts. 
Gossip musi zawierać pola: port, gossip_timeout, update_timeout, strategy, levels, switches, max_contacts
contacts musi zawierać pola: path, host (hostname/ip), port

Na każdej maszynie, na której zostanie uruchomiony Agent lub Query signer musi zostać uruchomiony rejestr RMI. Obiekty Agenta oraz Query signer mogą zostać przypisane wyłącznie do rejestru uruchomionego na lokalnej maszynie.