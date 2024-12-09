(ns api-dmbet.handler-test
  (:require [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [api-dmbet.handler :refer :all]
            [api-dmbet.db :refer [banco-dados obter-transacoes]]
            [cheshire.core :as json]))

(facts "Saldo inicial é 0"
  (resetar-saldo)
  (let [response (app (mock/request :get "/saldo"))]
    (fact "o formato é 'application/json'"
      (get-in response [:headers "Content-Type"]) => "application/json; charset=utf-8")
    (fact "o status da resposta é 200"
      (:status response) => 200)
    (fact "o texto do corpo é um JSON cuja chave é saldo e o valor é 0"
      (:body response) => "{\"saldo\":0}")))

(facts "Adicionar ao saldo"
  (let [response (app (-> (mock/request :post "/deposito")
                          (mock/json-body {:valor 100})))]

    (fact "o status da resposta é 200"
      (:status response) => 200)

    (fact "o texto do corpo confirma o depósito"
      (:body response) => "{\"message\":\"Depósito realizado com sucesso\",\"novo-saldo\":100}"))

  (let [response (app (-> (mock/request :post "/deposito")
                          (mock/json-body {:valor -50})))]

    (fact "o status da resposta é 400"
      (:status response) => 400)

    (fact "o texto do corpo informa que o valor é inválido"
      (:body response) => "{\"erro\":\"O valor do depósito deve ser positivo\"}")))

(facts "Realizar uma aposta"
  (resetar-saldo)
  (let [_ (app (-> (mock/request :post "/deposito")
                   (mock/json-body {:valor 200})))
        response (app (-> (mock/request :post "/apostar")
                          (mock/json-body {:odd 2 :valor 50 :id-aposta "1234" :vencedor "time-a"})))]

    (fact "o status da resposta é 200"
      (:status response) => 200)

    (fact "o texto do corpo confirma a realização da aposta"
      (:body response) => "{\"message\":\"Aposta realizada com sucesso\"}")))

(facts "Aposta com saldo insuficiente"
  (resetar-saldo)
  (let [_ (app (-> (mock/request :post "/deposito")
                   (mock/json-body {:valor 50})))
        response (app (-> (mock/request :post "/apostar")
                          (mock/json-body {:odd 2 :valor 100 :id-aposta "1235" :vencedor "time-b"})))]

    (fact "o status da resposta é 400"
      (:status response) => 400)

    (fact "o texto do corpo informa saldo insuficiente"
      (:body response) => "{\"erro\":\"Saldo insuficiente para realizar a aposta\"}")))

(facts "Registro de transações no banco de dados"
  (let [_ (reset! api-dmbet.db/banco-dados {:transacoes []})
        _ (app (-> (mock/request :post "/deposito") (mock/json-body {:valor 100})))
        _ (app (-> (mock/request :post "/apostar")
                   (mock/json-body {:odd 2.0 :valor 50 :id-aposta "1234" :vencedor "time-a"})))
        transacoes (api-dmbet.db/obter-transacoes)]

    (fact "O banco de dados contém 2 transações"
      (count (:transacoes transacoes)) => 2)

    (fact "A primeira transação é um depósito de 100"
      (first (:transacoes transacoes)) => (contains {:tipo :deposito :valor 100}))

    (fact "A segunda transação é uma aposta com detalhes completos"
      (second (:transacoes transacoes)) => 
        (contains {:tipo :aposta
                   :valor 50
                   :odd 2.0
                   :id-aposta "1234"
                   :vencedor "time-a"}))))
