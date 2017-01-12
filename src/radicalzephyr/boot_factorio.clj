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

(defn- info-json->mod-name [info-json-file]
  (->> info-json-file
       .getParentFile
       .getName))

(defn- zip-mod-files [old-fses tgt fs release-mod-dir-name]
  (let [not-mod-files (core/not-by-re [(re-pattern (str "^" release-mod-dir-name "/"))]
                                      (core/input-files fs))
        old-fs (get @old-fses release-mod-dir-name)
        new-fs (tmpd/rm fs not-mod-files)
        mod-package (str release-mod-dir-name ".zip")
        mod-package-out (io/file tgt mod-package)]

    (util/info "Writing %s...\n" mod-package)
    (jar/update-zip! mod-package-out old-fs new-fs)
    (swap! old-fses assoc release-mod-dir-name new-fs)))

(defn- make-packager [old-fses tgt]
  (fn [fs info-json]
    (let [info-json-file (core/tmp-file info-json)
          mod-dir-name (info-json->mod-name info-json-file)
          {mod-name "name" version "version"} (core/json-parse (slurp info-json-file))
          release-mod-dir-name (format "%s_%s" mod-name version)
          move-mod-files (helpers/sift-action false
                                              :move
                                              {mod-dir-name release-mod-dir-name})]

      (util/info "Moving mod files from %s/ to %s/...\n" mod-dir-name release-mod-dir-name)
      (let [fs (-> fs move-mod-files core/commit!)]
        (zip-mod-files old-fses tgt fs release-mod-dir-name)

        fs))))

(core/deftask package-mods
  "Create mod zip files out of directories that look like mods.

  Searches the fileset for all info.json files, then zips each
  directory containing one into an archive with the same name with
  .zip appended."
  []
  (let [old-fses (atom {})
        tgt (core/tmp-dir!)]
    (core/with-pre-wrap [fs]
      (let [package-mod (make-packager old-fses tgt)
            info-jsons (core/by-name ["info.json"] (core/input-files fs))]
        (-> (reduce package-mod fs info-jsons)
            (core/add-resource tgt)
            core/commit!)))))
