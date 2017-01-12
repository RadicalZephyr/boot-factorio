(ns radicalzephyr.boot-factorio
  {:boot/export-tasks true}
  (:require [boot.core          :as core]
            [boot.pod           :as pod]
            [boot.jar           :as jar]
            [boot.task-helpers  :as helpers]
            [boot.tmpdir        :as tmpd]
            [boot.util          :as util]
            [boot.task.built-in :as built-in]
            [clojure.java.io    :as io]))

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
        mod-dir-file   (io/file tgt mod-name)
        info-json-file (io/file mod-dir-file "info.json")
        move-mod-files (helpers/sift-action false
                                        :move
                                        {(re-pattern (format "^%s" mod-name))
                                         mod-dir-name})]
    (spit-info-json! info-json-file opts)
    (core/with-pre-wrap [fs]
      (util/info "Writing %s/%s...\n"
                 (.. info-json-file getParentFile getName)
                 (.getName info-json-file))
      (util/info "Moving mod files from %s/ to %s/...\n" mod-name mod-dir-name)
      (-> fs
          (core/add-resource tgt :meta {:mod-name mod-name})
          move-mod-files
          core/commit!))))

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
  (let [old-fses (atom {})
        tgt (core/tmp-dir!)]
    (core/with-pre-wrap [fs]
      (doseq [info-json (core/by-name ["info.json"] (core/input-files fs))]
        (let [mod-name (info-json->mod-name info-json)
              not-mod-files (core/not-by-re [(re-pattern mod-name)]
                                            (core/input-files fs))
              new-fs (tmpd/rm fs not-mod-files)
              mod-package (str mod-name ".zip")
              mod-package-out (io/file tgt mod-package)
              old-fs (get @old-fses mod-name)]

          (util/info "Writing %s...\n" mod-package)
          (jar/update-zip! mod-package-out old-fs new-fs)
          (swap! old-fses assoc mod-name new-fs)))
      (-> fs (core/add-resource tgt) core/commit!))))
