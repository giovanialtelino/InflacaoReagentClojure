(ns inflacao-pedestal-service.deflate)

(defn deflate [index-1 index-2 value]
  (* (/ index-2 index-1) value))