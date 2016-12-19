# Gosh-hawk

GoshawkDB client for Clojure

## Quick Start

Install [boot](http://boot-clj.com/)

```
$ boot repl

boot.user=> (require 'gosh-hawk.api)
boot.user=> (in-ns 'gosh-hawk.api)
gosh-hawk.api=> (def conn (connect (read-certs "...") "localhost"))
gosh-hawk.api=> (with-transaction conn ...)
```

## License

Apache 2.0 - See LICENSE

