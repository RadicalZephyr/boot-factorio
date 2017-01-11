(ns radicalzephyr.boot-factorio.impl
  (:require [cheshire.core :refer [generate-string]]))

(defn- info-json-options [{:keys [project
                                  version
                                  title
                                  author
                                  contact
                                  homepage
                                  description
                                  factorio-version
                                  dependencies]}]
  (array-map :name project
             :version version
             :title title
             :author author
             :contact contact
             :homepage homepage
             :description description
             :factorio_version factorio-version
             :dependencies dependencies))

(defn spit-info-json! [info-json-file opts]
  (as-> opts $
    (info-json-options $)
    (generate-string $ {:pretty true})
    (spit info-json-file $)))
