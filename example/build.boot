(set-env! :source-paths #{"mods"}
          :dependencies '[[org.clojure/clojure   "1.8.0" :scope "provided"]
                          [radicalzephyr/boot-factorio "0.1.0-SNAPSHOT"]])

(require '[radicalzephyr.boot-factorio :refer [package-mods]])

(deftask make-all []
  (comp (package-mods)
        (target)))
