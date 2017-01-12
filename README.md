# boot-factorio

A set of Boot tasks to facilitate Factorio mod development.

## Usage

First you need to [install boot].

[install boot]: https://github.com/boot-clj/boot#install

To use this in your project, add `[radicalzephyr/boot-factorio
"0.1.0-SNAPSHOT"]` to your `:dependencies` and require the task:

``` clojure
(set-env! :source-files #{"src"}
          :dependencies [[radicalzephyr/boot-factorio "0.1.0-SNAPSHOT"]])

(require '[radicalzephyr.boot-factorio :refer [package-mods])
```

Now you can run this in your shell and the mode will be continuously
repackaged into a `.zip` file every time you change a file.

``` shell
$ boot watch package-mods target
```

## License

Copyright © 2017 Geoff Shannon

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
