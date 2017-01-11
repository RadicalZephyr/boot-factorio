(ns radicalzephyr.boot-factorio
  {:boot/export-tasks true}
  (:require [boot.core :as core]
            [boot.pod :as pod]
            [boot.util :as util]
            [clojure.java.io :as io]))

(defn- spit-info-json! [info-json-file opts]
  (let [dependencies (-> (core/get-env) :dependencies (conj '[cheshire "5.6.3"]))
        worker-pod (pod/make-pod {:dependencies dependencies
                                  :directories ["src"]})
        info-json-path (.getPath info-json-file)]
    (pod/with-eval-in worker-pod
      (require '[clojure.java.io :as io]
               '[radicalzephyr.boot-factorio.impl :as impl])
      (impl/spit-info-json! (doto (io/file ~info-json-path) io/make-parents) ~opts))
    (pod/destroy-pod worker-pod)))

(core/deftask info-json
  "Create the mod info.json file.

  The mod-name and version must be specified to make an info.json."

  [m mod-name NAME str "The mod name (e.g. modfoo)"
   v version VER str "The mod version"
   t title TITLE str "The mod title"
   a author AUTHOR str "The mod author's name"
   c contact CONTACT str "The author's contact email"
   H homepage HOMEPAGE str "The url of the web home for this mod"
   d description DESCRIPTION str "A description of what the mod does"
   f factorio-version VERSION str "The version of factorio this mod requires"
   D dependencies DEP [str] "The list of other mods/versions this mod depends on"]

  (when-not (and mod-name version)
    (throw (Exception. "need mod-name and version to create info.json")))
  (let [tgt (core/tmp-dir!)
        opts *opts*
        mod-dir-name   (format "%s_%s" mod-name version)
        mod-dir-file   (io/file tgt mod-dir-name)
        info-json-file (io/file mod-dir-file "info.json")]
    (spit-info-json! info-json-file opts)
    (core/with-pre-wrap [fs]
      (util/info "Writing %s/%s...\n"
                 (.. info-json-file getParentFile getName)
                 (.getName info-json-file))
      (-> fs (core/add-resource tgt :meta {:mod-name mod-name}) core/commit!))))
