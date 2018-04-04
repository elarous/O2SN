(ns o2sn.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [o2sn.core-test]))

(doo-tests 'o2sn.core-test)

