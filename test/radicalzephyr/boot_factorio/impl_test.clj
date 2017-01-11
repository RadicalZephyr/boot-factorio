(ns radicalzephyr.boot-factorio.impl-test
  (:require [radicalzephyr.boot-factorio.impl :as sut]
            [clojure.test :as t]))

(t/deftest test-spit-info-json!
  (t/is (= "{
  \"name\" : \"FooMod\",
  \"version\" : \"0.1.0\",
  \"title\" : \"TitleThing\",
  \"author\" : \"Me!\",
  \"contact\" : \"contact@me.com\",
  \"homepage\" : \"homepage\",
  \"description\" : \"The Most Awesome Mod\",
  \"factorio_version\" : \"0.14\",
  \"dependencies\" : [ \"base >= 0.4.1\", \"scenario-pack\", \"? bar = 0.3\" ]
}"
           (with-out-str
             (sut/spit-info-json! *out* {:project "FooMod"
                                         :version "0.1.0"
                                         :title "TitleThing"
                                         :author "Me!"
                                         :contact "contact@me.com"
                                         :homepage "homepage"
                                         :description "The Most Awesome Mod"
                                         :factorio-version "0.14"
                                         :dependencies ["base >= 0.4.1"
                                                        "scenario-pack"
                                                        "? bar = 0.3"]})))))
