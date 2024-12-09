(ns teste.core
  (:gen-class))

(require '[clj-http.client :as client])
(require '[cheshire.core :refer :all])

(def chave_betano "b490bb9571msh9aa170a17a01584p122b5fjsn91e8db7f3a7e")
(def betano_url "https://betano.p.rapidapi.com/odds_betano")

(def partidas (:body (client/get "https://betano.p.rapidapi.com/events" {:headers {:x-rapidapi-key "4bad39c6c1msh62febbf04cf4315p110756jsnac40f68f18f3"
                                                              :x-rapidapi-host "betano.p.rapidapi.com"}
                                                    :query-params {:tournamentId "325"}})))



(declare menu)

(defn obter-saldo []
  (let [resposta (client/get "http://localhost:3009/saldo" {:as :json})
        saldo (:body resposta)]
    (println "Seu saldo atual:")
    (doseq [[k v] saldo] 
      (println (str k ": " v))))) 



(defn obter-bd []
  (let [resposta (client/get "http://localhost:3009/transacoes" {:as :json})
        transacoes (:body resposta)]
    (println "Acoes da conta:")
    (doseq [[k v] transacoes] 
      (println (str k ": " v))))) 


(defn realizar-deposito [valor]
  (let [resposta (client/post "http://localhost:3009/deposito"
                              {:headers {"Content-Type" "application/json"}
                               :body (generate-string {:valor valor})})
        corpo-resposta (cheshire.core/parse-string (:body resposta) true)] 
    (doseq [[k v] corpo-resposta] 
      (println (str k ": " v))))) 


(defn realizar-aposta [odd valor id-aposta vencedor]
  (let [resposta (client/post "http://localhost:3009/apostar"
                              {:headers {"Content-Type" "application/json"}
                               :body (generate-string {:odd odd
                                                       :valor valor
                                                       :id-aposta id-aposta
                                                       :vencedor vencedor})})]
    (println "Aposta Realizada!")))


(defn gerencia-saldo []
  (println "[1] Depositar Valor.")
  (println "[2] Consultar Saldo.")
  (let [escolha (read-line)]
    (cond
      (= escolha "1") 
      (do
        (println "Digite o valor a depositar:")
        (let [valor (read-line)] 
          (if (and (not (empty? valor)) 
                   (re-matches #"\d+" valor)) 
            (do
              (realizar-deposito (Integer/parseInt valor)))
            (do
              (println "Valor invalido. Por favor, insira um numero.")
              (gerencia-saldo)))) 
        (menu))
      (= escolha "2") 
      (do
        (obter-saldo) 
        (menu)) 

      :else
      (do
        (println "Entrada invalida. Tente novamente.")
        (gerencia-saldo))))) 




(defn apostaGolMarcado [idJogo odd escolha]
  (println "Selecione o valor a ser apostado:")
  (def valor (read))
    (println "\n=================================\n")
    (cond
      (= (str escolha) "1") (realizar-aposta odd valor (-> (parse-string partidas)(get-in ["events" (str idJogo) "eventId"])) "Yes")
      (= (str escolha) "2") (realizar-aposta odd valor (-> (parse-string partidas)(get-in ["events" (str idJogo) "eventId"])) "No")
    )
)


(defn golMarcado [idJogo]
  
  (def odds (:body (client/get "https://betano.p.rapidapi.com/odds_betano" {:headers {:x-rapidapi-key "Type your key here"
                                                                   :x-rapidapi-host "betano.p.rapidapi.com"}
                                                         :query-params {:eventId (-> (parse-string partidas)(get-in ["events" (str (- idJogo 1)) "eventId"]))
                                                                        :oddsFormat "decimal"
                                                                        :raw "false"}})))
  (def odd01 (-> (parse-string odds)(get-in ["markets" "104" "outcomes" "104" "bookmakers" "bestPrice" "price"])))
  (def odd02 (-> (parse-string odds)(get-in ["markets" "104" "outcomes" "105" "bookmakers" "bestPrice" "price"])))

  (println "[1] Odds de Ambos marcarem: " odd01)
  (println "[2] Odds de Ambos nao marcarem: " odd02)

  (println "[0] Voltar a escolha de modalidade")
  (println "\nEscolha qual time ganha:")
  (let [escolha (read)]
    (println "\n=================================\n")
    (cond
      (= (str escolha) "1") (do (apostaGolMarcado idJogo odd01 "1") ())
      (= (str escolha) "2") (do (apostaGolMarcado idJogo odd02 "2") ())
      (= (str escolha) "0") ()
      :else (do (println "Entrada invalida") (golMarcado idJogo)))))


(defn apostaTimeGanha [idJogo odd escolha]
  (println "Selecione o valor a ser apostado:")
  (def valor (read))
    (println "\n=================================\n")

    (cond
      (= (str escolha) "1") (realizar-aposta odd valor (-> (parse-string partidas)(get-in ["events" (str idJogo) "eventId"])) "1")
      (= (str escolha) "2") (realizar-aposta odd valor (-> (parse-string partidas)(get-in ["events" (str idJogo) "eventId"])) "X")
      (= (str escolha) "3") (realizar-aposta odd valor (-> (parse-string partidas)(get-in ["events" (str idJogo) "eventId"])) "2")
    )
)

(defn timeGanha [idJogo]
  
  (def odds (:body (client/get "https://betano.p.rapidapi.com/odds_betano" {:headers {:x-rapidapi-key "4bad39c6c1msh62febbf04cf4315p110756jsnac40f68f18f3"
                                                                   :x-rapidapi-host "betano.p.rapidapi.com"}
                                                         :query-params {:eventId (-> (parse-string partidas)(get-in ["events" (str (- idJogo 1)) "eventId"]))
                                                                        :oddsFormat "decimal"
                                                                        :raw "false"}})))
  (def odd01 (-> (parse-string odds)(get-in ["markets" "101" "outcomes" "101" "bookmakers" "bestPrice" "price"])))
  (def odd02 (-> (parse-string odds)(get-in ["markets" "101" "outcomes" "102" "bookmakers" "bestPrice" "price"])))
  (def odd03 (-> (parse-string odds)(get-in ["markets" "101" "outcomes" "103" "bookmakers" "bestPrice" "price"])))

  (println "[1] Odds do time 1 ganhar o jogo: " odd01)
  (println "[2] Odds do empate: " odd02)
  (println "[3] Odds do time 2 ganhar o jogo: " odd03)

  (println "[0] Voltar a escolha de modalidade")
  (println "\nEscolha qual time ganha:")
  (let [escolha (read)]
    (println "\n=================================\n")
    (cond
      (= (str escolha) "1") (do (apostaTimeGanha idJogo odd01 "1") ())
      (= (str escolha) "2") (do (apostaTimeGanha idJogo odd02 "2") ())
      (= (str escolha) "3") (do (apostaTimeGanha idJogo odd03 "3") ())
      (= (str escolha) "0") ()
      :else (do (println "Entrada invalida") (timeGanha idJogo)))))

(defn modalidadeEscolha [idJogo]
  (println "[1] Time vencendor.")
  (println "[2] Ambos marcam.")
  (println "[0] Voltar aos jogos.")
  (println (format "\nEscolha uma modalidade para apostar do jogo: %d" idJogo))
  (let [escolha (read)]
    (println "\n=================================\n")
    (cond
      (= (str escolha) "1") (do (timeGanha idJogo) (modalidadeEscolha idJogo))
      (= (str escolha) "2") (do (golMarcado idJogo) (modalidadeEscolha idJogo))
      (= (str escolha) "0") ()
      :else (do (println "Entrada invalida") (modalidadeEscolha idJogo)))))
(defn jogo [num]
  (println (format "-| Jogo %d |---------" (+ num 1)))
  (println(-> (parse-string partidas)(get-in ["events" (str num) "participant1"])))
  (println(-> (parse-string partidas)(get-in ["events" (str num) "participant2"])))
  (println "-------------------\n"))

(defn jogos []
  (println "\n======| JOGOS DISPONIVEIS |======\n")
  (dorun (map jogo (range 0 5)))
  (println "Escolha um jogo ou [0] para voltar ao menu: ")
  (let [escolha (read)]
    (println "\n=================================\n")
    (cond
      (= (str escolha) "1") (do (modalidadeEscolha 1) (jogos))
      (= (str escolha) "2") (do (modalidadeEscolha 2) (jogos))
      (= (str escolha) "3") (do (modalidadeEscolha 3) (jogos))
      (= (str escolha) "4") (do (modalidadeEscolha 4) (jogos))
      (= (str escolha) "5") (do (modalidadeEscolha 5) (jogos))
      (= (str escolha) "0") ()
      :else (do (println "Entrada invalida") (jogos)))))

(defn menu []
  (println "\n============| DMBET |============\n")
  (println "[1] Gerenciar saldo.")
  (println "[2] Realizar aposta.")
  (println "[3] Ver resultados.")
  (println "[0] Fechar programa.\n")
  (println "Escolha uma opcao: ")
  (let [escolha (read-line)]
    (println "\n=================================\n")
    (cond
      (= (str escolha) "1") (do (gerencia-saldo) (menu))
      (= (str escolha) "2") (do (jogos) (menu))
      (= (str escolha) "3") (do (do (obter-bd)) (menu))
      (= (str escolha) "0") (do (println "Fechando programa...") (System/exit 0))
      :else (do (println "Entrada invalida") (menu)))))

(defn -main
  "Teste Betano"
  [& args]

  (menu)

)
