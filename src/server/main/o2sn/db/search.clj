(ns o2sn.db.search
  (:require [o2sn.db.core :as db]))

(defn stories [user-id v]
  (let [q-str "for c in 1..1 outbound @userid subscribe
                for l in 0..5 outbound c.location contains
                  for s in stories
                    filter s.location == l._id
                    and contains(lower(s.title),lower(@search))
                    let parent_loc =
                      first(for p in 1..1 inbound l._id contains
                            return p)
                    return distinct {channel : parent_loc.name,
                                    _key : s._key,
                                    _id : s._id,
                                    title : s.title}"]
    (db/query! q-str {:userid user-id :search v})))

(defn users [v]
  (let [q-str "for u in users
                let p = document(u.profile)
                filter contains(lower(u.username),lower(@search))
                or contains(lower(p.fullname),lower(@search))
                return {_key : u._key,
                        _id : u._id,
                        username : u.username,
                        fullname : p.fullname,
                        avatar : u.avatar}"]
    (db/query! q-str {:search v})))


