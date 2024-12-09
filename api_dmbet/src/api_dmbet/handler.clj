(ns api-dmbet.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :as json]
            [api-dmbet.db :refer [registrar-transacao]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(def saldo (atom 0))

(defn saldo-como-json []
  {:headers {"Content-Type" "application/json; charset=utf-8"}
   :body (json/generate-string {:saldo @saldo})})

(defn adicionar-ao-saldo [valor]
  (if (pos? valor)
    (do
      (swap! saldo + valor)
      (registrar-transacao :deposito valor nil nil nil)
      {:status 200
       :headers {"Content-Type" "application/json; charset=utf-8"}
       :body (json/generate-string {:message "Deposito realizado com sucesso"
                                    :novo-saldo @saldo})})
    {:status 400
     :headers {"Content-Type" "application/json; charset=utf-8"}
     :body (json/generate-string {:erro "O valor do deposito deve ser positivo"})}))

(defn resetar-saldo []
  (reset! saldo 0))

(defn realizar-aposta [odd valor-apostado id-aposta vencedor]
  (if (>= @saldo valor-apostado)
    (do
      (registrar-transacao :aposta valor-apostado odd id-aposta vencedor)
      (swap! saldo #(- % valor-apostado))
              {:status 200
         :headers {"Content-Type" "application/json; charset=utf-8"}
         :body (json/generate-string {:message "Aposta realizada com sucesso"})}
      )

    {:status 400
     :headers {"Content-Type" "application/json; charset=utf-8"}
     :body (json/generate-string {:erro "Saldo insuficiente para realizar a aposta"})}))

;; (defn liquidar-aposta [id-aposta]
;;   (let [transacoes (:transacoes @api-dmbet.db/banco-dados)
;;         aposta (first (filter #(= (:id-aposta %) id-aposta) transacoes))]
;;     (if aposta
;;       (let [resultado-betano (000000 (:id-aposta aposta))
;;             vencedor-externo (:vencedor resultado-externo)]
;;         (if (= (:vencedor aposta) vencedor-externo)
;;           (let [ganho (* (:valor aposta) (:odd aposta))]
;;             (swap! saldo + ganho)
;;             (registrar-transacao :liquidacao ganho (:odd aposta) (:id-aposta aposta) (:vencedor aposta))
;;             (println (str "Aposta liquidada com sucesso. Ganho: " ganho)))
;;           (println "Aposta falhou")))
;;       (println "Aposta não encontrada"))))


(defroutes app-routes
  (GET "/" [] "Bem-vindo a API de apostas!")

  (GET "/saldo" [] (saldo-como-json))

  (POST "/deposito" {body :body}
        (let [dados (json/parse-stream (clojure.java.io/reader body) true)
              valor (:valor dados)]
          (adicionar-ao-saldo valor)))

  (POST "/apostar" {body :body}
        (let [dados (json/parse-stream (clojure.java.io/reader body) true)
              odd (:odd dados)
              valor (:valor dados)
              id-aposta (:id-aposta dados)
              vencedor (:vencedor dados)]
          (realizar-aposta odd valor id-aposta vencedor)))
  
  (GET "/transacoes" [] 
  {:status 200
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body (json/generate-string (api-dmbet.db/obter-transacoes))})


  (route/not-found "Recurso nao encontrado"))

(def app
  (wrap-defaults app-routes api-defaults))
