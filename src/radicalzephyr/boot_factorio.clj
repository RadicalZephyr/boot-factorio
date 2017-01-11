(ns radicalzephyr.boot-factorio
  {:boot/export-tasks true}
  (:require [boot.core   :as core]
            [boot.pod    :as pod]
            [boot.jar    :as jar]
            [boot.tmpdir :as tmpd]
            [boot.util   :as util]
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

(defn- info-json->mod-name [info-json]
  (->> info-json
       core/tmp-file
       .getParentFile
       .getName))

(core/deftask package-mods
  "Create mod zip files out of directories that look like mods.

  Searches the fileset for all info.json files, then zips each
  directory containing one into an archive with the same name with
  .zip appended."
  []
  (let [old-fs (atom nil)
        tgt (core/tmp-dir!)]
    (core/with-pre-wrap [fs]
      (doseq [info-json (->> fs
                             core/input-files
                             (core/by-name ["info.json"]))]
        (let [mod-name (info-json->mod-name info-json)
              new-fs (tmpd/restrict-dirs fs #{mod-name})
              mod-package (str mod-name ".zip")
              mod-package-out (io/file tgt mod-package)]
          (util/info "Writing %s...\n" mod-package)
          (jar/update-zip! mod-package-out @old-fs (reset! old-fs new-fs))))
      (-> fs (core/add-resource tgt) core/commit!))))