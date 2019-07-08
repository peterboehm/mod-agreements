
databaseChangeLog = {
    changeSet(author: "kurt", id: "2019-07-08-0001") {
        createTable(tableName: "log") {
            column(name: "log_id", type: "VARCHAR(36)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "log_message", type: "VARCHAR(255)")

            column(name: "log_origin", type: "VARCHAR(255)")

            column(name: "log_datecreated", type: "timestamp")

            column(name: "log_detail", type: "VARCHAR(255)")
        }

    }

    changeSet(author: "kurt", id: "2019-07-08-0002") {
        addPrimaryKey(columnNames: "log_id", constraintName: "logPK", tableName: "log")
    }

}


