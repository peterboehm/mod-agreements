
databaseChangeLog = {
    changeSet(author: "kurt", id: "2019-06-17-0001") {
        createTable(tableName: "event_log") {
            column(name: "el_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "el_message", type: "VARCHAR(255)")

            column(name: "el_record_id", type: "VARCHAR(255)")

            column(name: "el_timestamp", type: "timestamp")

            column(name: "el_record_data", type: "VARCHAR(255)")
        }

    }

    changeSet(author: "kurt", id: "2019-06-17-0002") {
        addPrimaryKey(columnNames: "el_id", constraintName: "event_logPK", tableName: "event_log")
    }

}


