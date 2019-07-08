databaseChangeLog = {  

  // columns for linked usage data provider
  changeSet(author: "claudia (manual)", id: "2019-07-08-00001") {
	  addColumn (tableName: "subscription_agreement" ) {
		  column(name: "sa_udp_fk", type: "VARCHAR(36)")
		  column(name: "sa_udp_note", type: "VARCHAR(255)")
	  }
  }
}
