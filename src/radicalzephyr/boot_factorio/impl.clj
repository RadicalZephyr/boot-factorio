(ns radicalzephyr.boot-factorio.impl
  (:require [cheshire.core :refer [generate-string]]))

(defn- info-json-options [{:keys [mod-name
                                  version
                                  title
                                  author
                                  contact
                                  homepage
                                  description
                                  factorio-version
                                  dependencies]}]

  (array-map :name             mod-name
             :version          version
             :title            (or title            "")
             :author           (or author           "")
             :contact          (or contact          "")
             :homepage         (or homepage         "")
             :description      (or description      "")
             :factorio_version (or factorio-version "")
             :dependencies     (or dependencies     [])))

(defn spit-info-json! [info-json-file opts]
  (as-> opts $
    (info-json-options $)
    (generate-string $ {:pretty true})
    (spit info-json-file $)))
