(def project 'radicalzephyr/boot-factorio)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure   "1.8.0" :scope "provided"]
                            [boot/core             "2.7.1" :scope "provided"]
                            [metosin/boot-alt-test "0.2.1" :scope "test"]
                            [cheshire              "5.6.3" :scope "test"]])

(task-options!
 pom {:project     project
      :version     version
      :description "A set of Boot tasks to facilitate Factorio mod development."
      :scm         {:url "https://github.com/RadicalZephyr/boot-factorio"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})

(require '[metosin.boot-alt-test :refer [alt-test]])

(deftask build
  "Build and install the project locally."
  []
  (comp (pom) (jar) (install)))
