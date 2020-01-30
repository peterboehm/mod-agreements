databaseChangeLog = {
  changeSet(author: "ethanfreestone (manual)", id: "202001141001-001") {
    addColumn(tableName: "title_instance") {
      column(name: "ti_date_monograph_published", type: "VARCHAR(36)")
      column(name: "ti_first_author", type: "VARCHAR(36)")
      column(name: "ti_monograph_edition", type: "VARCHAR(36)")
      column(name: "ti_monograph_volume", type: "VARCHAR(36)")
    }
  }

  changeSet(author: "ethanfreestone (manual)", id: "202001211524-001") {
    addColumn(tableName: "title_instance") {
      column(name: "ti_first_editor", type: "VARCHAR(36)")
    }
  }
    changeSet(author: "peter (generated)", id: "1579093826683-42") {
        addColumn(tableName: "remotekb") {
            column(name: "rkb_readonly", type: "BOOLEAN")
        }

        grailsChange {
          change {
            sql.execute("""
            UPDATE ${database.defaultSchemaName}.remotekb
            SET rkb_readonly=TRUE
            WHERE rkb_name LIKE '%LOCAL%'
            """.toString())
          }
        }

        grailsChange {
          change {
            sql.execute("""
              UPDATE ${database.defaultSchemaName}.remotekb
              SET rkb_readonly=FALSE
              WHERE rkb_name NOT LIKE '%LOCAL%'
              """.toString())
          }
        }
    }

}
