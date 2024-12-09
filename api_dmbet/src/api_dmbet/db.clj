(ns api-dmbet.db)
(def banco-dados (atom {:transacoes []}))

(defn registrar-transacao
  ([tipo valor odd id-aposta vencedor]
   (swap! banco-dados update :transacoes conj 
          {:tipo tipo 
           :valor valor 
           :odd odd
           :id-aposta id-aposta
           :vencedor vencedor
           :timestamp (System/currentTimeMillis)})))


(defn obter-transacoes []
  @banco-dados)
