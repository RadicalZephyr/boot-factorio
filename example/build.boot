(set-env! :source-paths #{"mods"}
          :dependencies '[[org.clojure/clojure   "1.8.0" :scope "provided"]
                          [radicalzephyr/boot-factorio "0.1.0-SNAPSHOT"]])

(require '[radicalzephyr.boot-factorio :refer [info-json package-mods]])

(deftask build-foo []
  (info-json :mod-name "FooMod"
             :version "0.1.0"))

(deftask build-bar []
  (info-json :mod-name "BarMod"
             :version "0.2.0"))

(deftask make-all []
  (comp (build-foo)
        (build-bar)
        (package-mods)
        (target)))
