(ns config.migrate-config
  (:use [drift.builder :only [incremental-migration-number-generator]]
        [korma core db])
  (:require [environ.core :refer [env]]))

(defdb db (postgres {:db   (:db-name env)
                     :host (:db-host env)
                     :port (:db-port env)
                     :user (:db-user env)
                     :password (:db-password env)}))

(defn- maybe-create-schema-table
  "Creates the schema table if it doesn't already exist."
  [args]
  (exec-raw "CREATE TABLE IF NOT EXISTS schema_version (version INTEGER NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now())"))

(defn current-db-version []
  (or (:version (first (select :schema_version (fields :version) (order :created_at :DESC) (limit 1)))) 0))

(defn update-db-version [version]
  (insert :schema_version (values {:version version})))

(defn migrate-config []
  {:directory "src/migrations/"
   :ns-content "\n (:use [korma core db])"
   :namespace-prefix "migrations"
   :migration-number-generator incremental-migration-number-generator
   :init maybe-create-schema-table
   :current-version current-db-version
   :update-version update-db-version })
