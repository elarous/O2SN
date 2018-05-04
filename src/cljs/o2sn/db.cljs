(ns o2sn.db)

(def default-db
  {:page :home
   :user {:logged-in? false}
   :welcome-form :login-form
   :login-form {:username ""
                :password ""
                :errors? false
                :processing? false}})
