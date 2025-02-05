(ns quickdoc.impl
  {:no-doc true}
  (:require
   [clojure.edn :as edn]
   [clojure.pprint :as pprint]
   [clojure.string :as str]))

(defn debug [& xs]
  (binding [*out* *err*]
    (apply println xs)))

(defn- var-filter [var]
  (let [mvar (:meta var)]
    (and (not (:no-doc mvar))
         (not (:skip-wiki mvar))
         (not (:private var))
         (not (= 'clojure.core/defrecord (:defined-by var))))))

(defn mini-markdown [s]
  (str/replace s #"`(.*?)`" (fn [[_ s]]
                              (format "<code>%s</code>" s))))

(defn var-summary [var]
  (when-let [first-line (some-> (:doc var) (str/split-lines) (first))]
    (let [first-sentence (-> (str/split first-line #"\. ") first)]
      (mini-markdown (subs first-sentence 0 (min (count first-sentence) 80))))))

(defn print-var [var _source {:keys [github/repo git/branch collapse-vars]}]
  (when (var-filter var)
    (when collapse-vars (println "<details>\n\n"))
    (when collapse-vars
      (println (str "<summary><code>" (:name var) "</code>"
                    (when-let [summary (var-summary var)]
                      (str " - " summary)))
               "</summary>\n\n"))
    (println "##" (format "`%s`" (:name var)))
    (when-let [arg-lists (seq (:arglist-strs var))]
      (println "``` clojure\n")
      (doseq [arglist arg-lists]
        (let [arglist (format "(%s %s)" (:name var) arglist)
              arglist (try (binding [pprint/*print-miser-width* nil
                                     pprint/*print-right-margin* 120]
                             (with-out-str (pprint/pprint (edn/read-string arglist))))
                           (catch Exception _ arglist))]
          (print arglist)))
      (println "```\n"))
    (when-let [doc (:doc var)]
      (println)
      (when (:macro var)
        (println "Macro.\n\n"))
      (println doc))
    (println)
    (println
     (format
      "[Source](%s/blob/%s/%s#L%s-L%s)"
      repo
      branch
      (:filename var)
      (:row var)
      (:end-row var)))
    (when collapse-vars (println "</details>\n\n"))))

(defn print-namespace [ns-defs ns-name vars opts]
  (let [ns (get-in ns-defs [ns-name 0])
        filename (:filename ns)
        source (try (slurp filename)
                    (catch Exception _ nil))
        mns (get ns :meta)]
    (when (and (not (:no-doc mns))
               (not (:skip-wiki mns)))
      (when-let [vars (seq (filter var-filter vars))]
        (let [ana (group-by :name vars)
              collapse-nss (:collapse-nss opts)]
          (when collapse-nss (println "<details>\n\n"))
          (when collapse-nss (println "<summary><code>" ns-name "</code></summary>\n\n"))
          (println "#" ns-name "\n\n")
          (run! (fn [[_ vars]]
                  (let [var (last vars)]
                    (print-var var source opts)))
                (sort-by first ana))
          (when collapse-nss (println "</details>\n\n")))))))

(defn md-munge [s]
  (str/replace s #"[\*\.!]" ""))

(defn print-toc [nss ns-defs opts]
  (when (:toc opts)
    (let [memo (atom {})
          with-idx (fn [s]
                     (let [v (swap! memo update s (fnil inc -1))
                           c (get v s)]
                       (if (zero? c)
                         s
                         (str s "-" c))))]
      (println "# Table of contents")
      (doseq [[ns-name vars] (sort-by first nss)]
        (let [ns (get-in ns-defs [ns-name 0])
              mns (get ns :meta)]
          (when (and (not (:no-doc mns))
                     (not (:skip-wiki mns)))
            (println "- " (format "[`%s`](#%s)" ns-name (with-idx (md-munge ns-name))))
            (let [vars (group-by :name vars)
                  vars (sort-by first vars)]
              (doseq [[var-name var-infos] vars]
                (let [v (last var-infos)]
                  (when (var-filter v)
                    (println
                     "    - "
                     (str (format "[`%s`](#%s)" var-name (with-idx (md-munge var-name)))
                          (when-let [summary (var-summary v)]
                            (str " - " summary))))))))))))))
