databaseChangeLog = {

    changeSet(author: "peter (generated)", id: "1579093826683-42") {
        createTable(tableName: "remotekb") {
            column(name: "readonly", type: "BOOLEAN")
        }
    }
}
