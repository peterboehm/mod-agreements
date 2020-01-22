databaseChangeLog = {

    changeSet(author: "peter (generated)", id: "1579093826683-42") {
        addColumn(tableName: "remotekb") {
            column(name: "readonly", type: "BOOLEAN")
        }
    }
}
